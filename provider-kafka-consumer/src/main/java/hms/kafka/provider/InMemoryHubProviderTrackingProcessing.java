package hms.kafka.provider;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eatthepath.jvptree.DistanceFunction;
import com.eatthepath.jvptree.VPTree;
import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.common.DistanceUtils;
import hms.common.IHMSExecutorContext;
import hms.dto.HubProviderTracking;
import hms.dto.LatLongLocation;
import hms.dto.Provider;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
import hms.kafka.streamming.PollChainning;
import hms.provider.KafkaProviderMeta;
import hms.provider.repositories.IProviderRepository;

public class InMemoryHubProviderTrackingProcessing implements Closeable{
	private static final Logger logger = LoggerFactory.getLogger(InMemoryHubProviderTrackingProcessing.class);
	IHMSExecutorContext ec;
	IProviderRepository repo;
	
	KafkaStreamNodeBase<hms.dto.HubProviderTracking, Boolean>  trackingProviderHubProcessor;
	KafkaStreamNodeBase<hms.dto.HubProviderGeoQuery, List<hms.dto.Provider>>  queryProvidersHubProcessor;

	private final String kafkaserver;
	private final String providerGroup;
	private final int keepAliveDuration = 30000; // 30s;
	private final UUID hubid;
	private final VPTree<LatLongLocation, InMemProviderTracking> providerTrackingVPTree =
	        new VPTree<LatLongLocation, InMemProviderTracking>(
	                new ProviderDistanceFunction());
	private final LinkedHashMap<UUID, InMemProviderTracking> myproviders =
	        		new LinkedHashMap<UUID, InMemProviderTracking>();
	
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
			return ec.getExecutor();
		}
	}
	
	@Inject
	public InMemoryHubProviderTrackingProcessing(Config config, IHMSExecutorContext ec, IProviderRepository repo) {
		this.ec = ec;
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
		logger.info("Provider processing is ready");
	}
	
	
	private void buildTrackingProviderHubProcessor() {
		this.trackingProviderHubProcessor = new ProviderProcessingNode<hms.dto.HubProviderTracking, Boolean>() {
			
			private final List<PollChainning> querychain = new LinkedList<PollChainning>();
			{
				this.querychain.add(buildQueryProvidersHubProcessor());
			}			
			@Override
			protected List<? extends PollChainning> getSubChains() {
				return querychain;
			}
			
			@Override
			protected void configConsummer(Properties consumerProps) {
				consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
			}
			
			@Override
			protected Boolean processRequest(HMSMessage<hms.dto.HubProviderTracking> request) {
				UUID hubid = request.getData().getHubid();
				if(hubid.equals(InMemoryHubProviderTrackingProcessing.this.hubid)) {
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
			
			@Override
			protected void intervalCleanup() {
				long stick = java.lang.System.currentTimeMillis();
				
				InMemProviderTracking candidate;
				do {
					candidate = myproviders.isEmpty() ? null : myproviders.entrySet().iterator().next().getValue();
					if(candidate!=null && candidate.getCurrentMostRecentExpiration() < stick) {
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

			@Override
			protected Class<? extends HubProviderTracking> getTConsumeManifest() {
				return HubProviderTracking.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.InMemTrackingMessage + hubid.toString();
			}		
			
			@Override
			protected String getForwardTopic() {				
				return KafkaProviderMeta.InMemTrackingMessage + KafkaHMSMeta.ReturnTopicSuffix;
			}	
		};
	}	
	
	private KafkaStreamNodeBase<hms.dto.HubProviderGeoQuery, List<hms.dto.Provider>> buildQueryProvidersHubProcessor(){
		return this.queryProvidersHubProcessor = new ProviderProcessingNode<hms.dto.HubProviderGeoQuery, List<hms.dto.Provider>>() {		
			@Override
			protected List<hms.dto.Provider> processRequest(HMSMessage<hms.dto.HubProviderGeoQuery> request) {
				UUID hubid = request.getData().getHubid();
				if(hubid.equals(InMemoryHubProviderTrackingProcessing.this.hubid)) {
					hms.dto.HubProviderGeoQuery querydto = request.getData();
					List<InMemProviderTracking> nearTrackings = providerTrackingVPTree.getAllWithinDistance(querydto, querydto.getDistance());
					if(nearTrackings!=null && nearTrackings.size()>0) {
						List<UUID> providerids= nearTrackings.stream().map(t -> t.getProviderId()).collect(Collectors.toList());
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
				return KafkaProviderMeta.InMemQueryProvidersMessage+hubid.toString();
			}		
			
			@Override
			protected String getForwardTopic() {				
				return KafkaProviderMeta.InMemQueryProvidersMessage + KafkaHMSMeta.ReturnTopicSuffix;
			}	
			
			@Override
			protected boolean isChained() {
				return true;
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
