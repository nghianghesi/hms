package hms.kafka.provider;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eatthepath.jvptree.DistanceFunction;
import com.eatthepath.jvptree.VPTree;
import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.common.DistanceUtils;
import hms.dto.HubProviderTracking;
import hms.dto.LatLongLocation;
import hms.dto.Provider;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
import hms.provider.KafkaProviderMeta;
import hms.provider.repositories.IProviderRepository;

public class InMemoryProviderProcessing implements Closeable{
	private static final Logger logger = LoggerFactory.getLogger(InMemoryProviderProcessing.class);
	IProviderRepository repo;
	
	List<KafkaStreamNodeBase<hms.dto.HubProviderTracking, Boolean>>  trackingProviderHubProcessors
	 = new ArrayList<KafkaStreamNodeBase<HubProviderTracking,Boolean>>();
	List<KafkaStreamNodeBase<hms.dto.HubProviderGeoQuery, List<hms.dto.Provider>>>  queryProvidersHubProcessors
	 = new ArrayList<>();

	private final String kafkaserver;
	private final String providerGroup;
	private final int keepAliveDuration = 30000; // 30s;
	private final List<UUID> hubids = new ArrayList<UUID>();
	
	private final Map<UUID,VPTree<LatLongLocation, InMemProviderTracking>> providerTrackingVPTrees =
	        new HashMap<UUID,VPTree<LatLongLocation, InMemProviderTracking>>();
	private final Map<UUID,LinkedHashMap<UUID, InMemProviderTracking>> myHubProviders =
	        		new HashMap<UUID,LinkedHashMap<UUID, InMemProviderTracking>>();
	
	private Map<UUID,ExecutorService> executors = new HashMap<>(); 
	private Map<UUID,ExecutorService> pollingExs = new HashMap<>();//Executors.newFixedThreadPool(1);
	
	private class InMemProviderTracking implements LatLongLocation {
		private  double latitude;
		private  double longitude;
		private final UUID providerId;
		private final LinkedList<Long> expiredTicks = new LinkedList<Long>();
		
		public double getLatitude() {
			return latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public void setLatLong(LatLongLocation loc) {
			this.latitude = loc.getLatitude();
			this.longitude = loc.getLongitude();
			this.expiredTicks.add(java.lang.System.currentTimeMillis() + keepAliveDuration);
		}
		
		public Long getCurrentMostRecentExpiration() {
			return !this.expiredTicks.isEmpty() ? this.expiredTicks.getFirst() : null;
		}
		
		public void expireMostRecent() {
			if(!this.expiredTicks.isEmpty()) {
				this.expiredTicks.removeFirst();
			}
		}
		
		public boolean isAlive() {
			return !this.expiredTicks.isEmpty();
		}
		
		public UUID getProviderId() {
			return providerId;
		}
		
		public InMemProviderTracking(HubProviderTracking dto) {		
			this.providerId = dto.getProviderid();
			this.setLatLong(dto);
		}		
	}
	
	private static DistanceFunction<LatLongLocation> distanceFunction = new DistanceFunction<LatLongLocation>() {
	    public double getDistance(final LatLongLocation firstPoint, final LatLongLocation secondPoint) {
	        return DistanceUtils.geoDistance(firstPoint.getLatitude(), firstPoint.getLongitude(), 
	        									secondPoint.getLatitude(), secondPoint.getLongitude());
	    }
	};
	
	private abstract class ProviderProcessingNode<TCon,TRep> extends KafkaStreamNodeBase<TCon,TRep>{
			
		@Override
		protected Logger getLogger() {
			return logger;
		}
				
		@Override
		protected String getServer() {
			return kafkaserver;
		}

		@Override
		protected String getForwardTopic() {
			return this.getConsumeTopic()+KafkaHMSMeta.ReturnTopicSuffix;
		}
		
	}
	
	@Inject
	public InMemoryProviderProcessing(Config config, IProviderRepository repo) {
		this.repo = repo;

		if(config.hasPath(KafkaHMSMeta.ServerConfigKey)) {
			this.kafkaserver = config.getString(KafkaHMSMeta.ServerConfigKey);
		}else {
			logger.error("Missing {} configuration", KafkaHMSMeta.ServerConfigKey);
			throw new Error(String.format("Missing {} configuration", KafkaHMSMeta.ServerConfigKey));
		}
		
		if(config.hasPath(KafkaProviderMeta.ProviderGroupConfigKey)) {
			this.providerGroup = config.getString(KafkaProviderMeta.ProviderGroupConfigKey);
		}else {
			this.providerGroup = "hms.provider";
		}
		
		if(config.hasPath(KafkaProviderMeta.ProviderInmemHubIdConfigKey)) {
			String strHubids = config.getString(KafkaProviderMeta.ProviderInmemHubIdConfigKey);
			for(String sh : strHubids.split(",")) {				
				hubids.add(UUID.fromString(sh));
			}
		}else {
			logger.error("Missing {} configuration", KafkaProviderMeta.ProviderInmemHubIdConfigKey);
			throw new Error(String.format("Missing {} configuration", KafkaProviderMeta.ProviderInmemHubIdConfigKey));
		}
		
		for(UUID hubid: this.hubids) {
			this.myHubProviders.put(hubid, 
					new LinkedHashMap<UUID, InMemoryProviderProcessing.InMemProviderTracking>());
			this.providerTrackingVPTrees.put(hubid, 
					new VPTree<LatLongLocation, InMemoryProviderProcessing.InMemProviderTracking>(distanceFunction));
			this.executors.put(hubid, Executors.newFixedThreadPool(1));
			this.pollingExs.put(hubid, Executors.newFixedThreadPool(1));
			this.buildTrackingProviderHubProcessor(hubid);
			this.buildQueryProvidersHubProcessor(hubid);
		}
		logger.info("Provider processing is ready");

		for(UUID hubid: this.hubids) {
			logger.info("{}",hubid);
		}
	}
	
	
	private void buildTrackingProviderHubProcessor(final UUID trackingHubid) {
		ProviderProcessingNode<hms.dto.HubProviderTracking, Boolean>t = new ProviderProcessingNode<hms.dto.HubProviderTracking, Boolean>() {			
			private final VPTree<LatLongLocation, InMemProviderTracking> providerTrackingVPTree = providerTrackingVPTrees.get(trackingHubid);
			private final LinkedHashMap<UUID, InMemProviderTracking> myproviders = myHubProviders.get(trackingHubid);

			
			@Override
			protected Boolean processRequest(HMSMessage<hms.dto.HubProviderTracking> request) {
				UUID hubid = request.getData().getHubid();
				if(hubid.equals(trackingHubid)) {
					HubProviderTracking trackingdto = request.getData();						
					InMemProviderTracking newdata = new InMemProviderTracking(trackingdto);
					InMemProviderTracking existingdata = myproviders.putIfAbsent(trackingdto.getProviderid(), newdata);							
					if(existingdata!=null) { // existing --> remove tracking to be re-indexed.
						providerTrackingVPTree.remove(existingdata);
						existingdata.setLatLong(trackingdto);
					}else {
						existingdata = newdata;
					}

					providerTrackingVPTree.add(existingdata);
					return true;
				}else {
					return false;
				}
			}
			
			private long prevCleanup = System.currentTimeMillis();
			@Override
			protected void intervalCleanup() {
				if(System.currentTimeMillis() - prevCleanup > 2000) {
					prevCleanup = java.lang.System.currentTimeMillis();
					
					InMemProviderTracking candidate;
					do {
						candidate = myproviders.isEmpty() ? null : myproviders.entrySet().iterator().next().getValue();
						if(candidate!=null && candidate.getCurrentMostRecentExpiration() < prevCleanup) {
							candidate.expireMostRecent();
							if(!candidate.isAlive()) {
								providerTrackingVPTree.remove(candidate);
								myproviders.remove(candidate.getProviderId());
							}
						}else {
							break;
						}
					}while(candidate != null);
				}
			}
			
			@Override
			protected Class<? extends HubProviderTracking> getTConsumeManifest() {
				return HubProviderTracking.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.InMemTrackingMessage + trackingHubid.toString().replace('.', '_').replace('-', '_');
			}		
			
			@Override
			protected String getGroupid() {
				return providerGroup + trackingHubid.toString()+"-tracking"+UUID.randomUUID();
			}
			
			@Override
			protected String getForwardTopic() {				
				return KafkaProviderMeta.InMemTrackingMessage + KafkaHMSMeta.ReturnTopicSuffix;
			}	
			
			private Executor myex = executors.get(trackingHubid);
			private Executor myPollEx = pollingExs.get(trackingHubid);
			@Override
			protected Executor getExecutorService() {
				return myex;
			}			
			
			@Override
			protected Executor getPollingService() {
				return myPollEx;
			}
		};
		this.trackingProviderHubProcessors.add(t);
		t.run();
	}	
	
	private void buildQueryProvidersHubProcessor(final UUID trackingHubid){
		ProviderProcessingNode<hms.dto.HubProviderGeoQuery, List<hms.dto.Provider>> q = new ProviderProcessingNode<hms.dto.HubProviderGeoQuery, List<hms.dto.Provider>>() {
			private final VPTree<LatLongLocation, InMemProviderTracking> providerTrackingVPTree = providerTrackingVPTrees.get(trackingHubid);
			
			@Override
			protected List<hms.dto.Provider> processRequest(HMSMessage<hms.dto.HubProviderGeoQuery> request) {
				UUID hubid = request.getData().getHubid();
				if(hubid.equals(trackingHubid)) {
					hms.dto.HubProviderGeoQuery querydto = request.getData();
					List<InMemProviderTracking> nearTrackings = providerTrackingVPTree.getAllWithinDistance(querydto, querydto.getDistance());
					if(nearTrackings!=null && nearTrackings.size()>0) {
						List<UUID> providerids = nearTrackings.stream().map(t -> t.getProviderId()).collect(Collectors.toList());
						return repo.getProvidersByIds(providerids).stream()
								.map(p -> new Provider(p.getProviderid(), p.getZone(), p.getName()))
								.collect(Collectors.toList());
					}
				}
				return new ArrayList<hms.dto.Provider>();
			}

			@Override
			protected Class<? extends hms.dto.HubProviderGeoQuery> getTConsumeManifest() {
				return hms.dto.HubProviderGeoQuery.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.InMemQueryProvidersMessage + trackingHubid.toString().replace('.', '_').replace('-', '_');
			}		
			
			@Override
			protected String getForwardTopic() {				
				return KafkaProviderMeta.InMemQueryProvidersMessage + KafkaHMSMeta.ReturnTopicSuffix;
			}	
						
			@Override
			protected String getGroupid() {
				return providerGroup + trackingHubid.toString();
			}
			
			private Executor myex = executors.get(trackingHubid);
			private Executor myPollEx = pollingExs.get(trackingHubid);
			@Override
			protected Executor getExecutorService() {
				return myex;
			}			
			
			@Override
			protected Executor getPollingService() {
				return myPollEx;
			}		
		};	
		
		this.queryProvidersHubProcessors.add(q);
		q.run();
	}

	@Override
	public void close() {	
		
		for(KafkaStreamNodeBase<hms.dto.HubProviderTracking, Boolean> t : this.trackingProviderHubProcessors) {
			t.shutDown();
		}
		
		for(KafkaStreamNodeBase<hms.dto.HubProviderGeoQuery, List<hms.dto.Provider>> q: this.queryProvidersHubProcessors) {
			q.shutDown();
		}
		
		for(ExecutorService ex:this.executors.values()) {
			try {
				ex.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}	

}
