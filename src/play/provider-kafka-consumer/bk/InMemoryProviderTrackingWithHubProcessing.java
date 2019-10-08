package hms.kafka.provider;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eatthepath.jvptree.DistanceFunction;
import com.eatthepath.jvptree.VPTree;
import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.common.DistanceUtils;
import hms.dto.LatLongLocation;
import hms.dto.Provider;
import hms.dto.ProviderTracking;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
import hms.provider.KafkaProviderMeta;
import hms.provider.models.ProviderModel;
import hms.provider.repositories.IProviderRepository;

public class InMemoryProviderTrackingWithHubProcessing implements Closeable{
	private static final Logger logger = LoggerFactory.getLogger(InMemoryProviderTrackingWithHubProcessing.class);
	IProviderRepository repo;
	
	KafkaStreamNodeBase<UUID, Boolean>  trackingProviderHubProcessor;
	KafkaStreamNodeBase<UUID, List<hms.dto.Provider>>  queryProvidersHubProcessor;

	private final String kafkaserver;
	private final String providerGroup;
	private final int keepAliveDuration = 30000; // 30s;
	private final UUID hubid;
	private final VPTree<LatLongLocation, InMemProviderTracking> providerTrackingVPTree =
	        new VPTree<LatLongLocation, InMemProviderTracking>(
	                new ProviderDistanceFunction());
	private final Map<UUID, InMemProviderTracking> myproviders =
	        		new HashMap<UUID, InMemProviderTracking>();
	private final LinkedList<InMemProviderTracking> expiredTrackings = new LinkedList<InMemProviderTracking>();
	
	
	private ExecutorService ex = Executors.newFixedThreadPool(1);
	private Executor pollingEx = Executors.newFixedThreadPool(1);
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
		
		public InMemProviderTracking(hms.dto.ProviderTracking dto) {		
			this.providerId = dto.getProviderid();
			this.setLatLong(dto);
		}		
	}
	
	private static class ProviderDistanceFunction implements DistanceFunction<LatLongLocation> {
	    public double getDistance(final LatLongLocation firstPoint, final LatLongLocation secondPoint) {
	        return DistanceUtils.geoDistance(firstPoint.getLatitude(), firstPoint.getLongitude(), 
	        									secondPoint.getLatitude(), secondPoint.getLongitude());
	    }
	}
	
	private abstract class ProviderProcessingNode<TCon,TRep> extends KafkaStreamNodeBase<TCon,TRep>{
		
		@Override
		protected Logger getLogger() {
			return logger;
		}
		
		@Override
		protected String getGroupid() {
			return providerGroup;
		}
		
		@Override
		protected String getServer() {
			return kafkaserver;
		}

		@Override
		protected String getForwardTopic() {
			return this.getConsumeTopic()+KafkaHMSMeta.ReturnTopicSuffix;
		}

		@Override
		protected Executor getExecutorService() {
			return ex;
		}		
		
		@Override
		protected Executor getPollingService() {
			return pollingEx;
		}
	}
	
	@Inject
	public InMemoryProviderTrackingWithHubProcessing(Config config, IProviderRepository repo) {
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
			this.hubid = UUID.fromString(config.getString(KafkaProviderMeta.ProviderInmemHubIdConfigKey));
		}else {
			logger.error("Missing {} configuration", KafkaProviderMeta.ProviderInmemHubIdConfigKey);
			throw new Error(String.format("Missing {} configuration", KafkaProviderMeta.ProviderInmemHubIdConfigKey));
		}

		
		this.buildTrackingProviderHubProcessor();
		this.buildQueryProvidersHubProcessor();
		this.trackingProviderHubProcessor.run();
		this.queryProvidersHubProcessor.run();
		
		logger.info("Provider processing is ready");
	}
	
	
	private void buildTrackingProviderHubProcessor() {
		this.trackingProviderHubProcessor = new ProviderProcessingNode<UUID, Boolean>() {
						
			@Override
			protected Boolean processRequest(HMSMessage<UUID> request) {
				UUID hubid = request.getData();
				if(hubid.equals(InMemoryProviderTrackingWithHubProcessing.this.hubid)) {
					try {
						ProviderTracking trackingdto = request.popReponsePoint(hms.dto.ProviderTracking.class).data;						
						InMemProviderTracking newdata = new InMemProviderTracking(trackingdto);
						InMemProviderTracking existingdata = myproviders.putIfAbsent(trackingdto.getProviderid(), newdata);							
						if(existingdata!=null) { // existing --> remove tracking to be re-indexed.
							providerTrackingVPTree.remove(existingdata);
							existingdata.setLatLong(trackingdto);
						}else {
							existingdata = newdata;
						}

						providerTrackingVPTree.add(existingdata);
						expiredTrackings.add(existingdata);
						return true;
					} catch (IOException e) {
						logger.error("Tracking Provider Hub Error {}", e.getMessage());
						return false;
					}
				}else {
					return false;
				}
			}
			
			@Override
			protected void intervalCleanup() {
				long stick = java.lang.System.currentTimeMillis();
				
				InMemProviderTracking candidate;
				do {
					candidate = expiredTrackings.isEmpty() ? null : expiredTrackings.getFirst();
					if(candidate!=null && candidate.getCurrentMostRecentExpiration() < stick) {
						candidate.expireMostRecent();
						expiredTrackings.removeFirst();						
						if(!candidate.isAlive()) {
							providerTrackingVPTree.remove(candidate);
							myproviders.remove(candidate.getProviderId());
						}
					}else {
						break;
					}
				}while(candidate != null);
			}

			@Override
			protected Class<? extends UUID> getTConsumeManifest() {
				return UUID.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.InMemTrackingWithHubMessage+hubid.toString();
			}		
			
			@Override
			protected String getForwardTopic() {				
				return KafkaProviderMeta.InMemTrackingMessage + KafkaHMSMeta.ReturnTopicSuffix;
			}	
		};
	}	
	
	private KafkaStreamNodeBase<UUID, List<hms.dto.Provider>> buildQueryProvidersHubProcessor(){
		return this.queryProvidersHubProcessor = new ProviderProcessingNode<UUID, List<hms.dto.Provider>>() {
		
			@Override
			protected List<hms.dto.Provider> processRequest(HMSMessage<UUID> request) {
				UUID hubid = request.getData();
				if(hubid.equals(InMemoryProviderTrackingWithHubProcessing.this.hubid)) {
					try {
						hms.dto.GeoQuery querydto = request.popReponsePoint(hms.dto.GeoQuery.class).data;
						List<hms.dto.Provider> res = new ArrayList<hms.dto.Provider>();
						List<InMemProviderTracking> nearTrackings = providerTrackingVPTree.getAllWithinDistance(querydto, querydto.getDistance());
						if(nearTrackings!=null && nearTrackings.size()>0) {
							List<UUID> providerids= nearTrackings.stream().map(t -> t.getProviderId()).collect(Collectors.toList());
							List<ProviderModel> providers = repo.getProvidersByIds(providerids);
							for (ProviderModel p : providers){
								res.add(new Provider(p.getProviderid(), p.getZone(), p.getName()));
							}
						}
						return res;
					} catch (IOException e) {
						logger.error("Query Providers Hub Error {}", e.getMessage());
						return null;
					}
				}else {
					return new ArrayList<hms.dto.Provider>();
				}
			}

			@Override
			protected Class<? extends UUID> getTConsumeManifest() {
				return UUID.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.InMemQueryProvidersWithHubsMessage+hubid.toString();
			}		
			
			@Override
			protected String getForwardTopic() {				
				return KafkaProviderMeta.InMemQueryProvidersMessage + KafkaHMSMeta.ReturnTopicSuffix;
			}	
		};	
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		this.trackingProviderHubProcessor.shutDown();
		this.queryProvidersHubProcessor.shutDown();
	}	

}
