package hms.kafka.provider;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Executor;

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
import hms.dto.LatLongLocation;
import hms.dto.Provider;
import hms.dto.ProviderTracking;
import hms.dto.ProvidersGeoQueryResponse;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
import hms.kafka.streamming.PollChainning;
import hms.provider.KafkaProviderMeta;

public class InMemoryProviderTrackingWithHubProcessing implements Closeable{
	private static final Logger logger = LoggerFactory.getLogger(InMemoryProviderTrackingWithHubProcessing.class);
	IHMSExecutorContext ec;
	
	KafkaStreamNodeBase<UUID, Boolean>  trackingProviderHubProcessor;
	KafkaStreamNodeBase<UUID, hms.dto.ProvidersGeoQueryResponse>  queryProvidersHubProcessor;

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
	
	private class ProviderDistanceFunction implements DistanceFunction<LatLongLocation> {
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
	public InMemoryProviderTrackingWithHubProcessing(Config config, IHMSExecutorContext ec) {
		this.ec = ec;

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
		this.trackingProviderHubProcessor = new ProviderProcessingNode<UUID, Boolean>() {
			{
				this.subchains.add(buildQueryProvidersHubProcessor());
			}
			
			private final List<PollChainning> subchains = new LinkedList<PollChainning>();
			@Override
			protected List<? extends PollChainning> getSubChains() {
				return subchains;
			}
			
			@Override
			protected void configConsummer(Properties consumerProps) {
				consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
			}
			
			@Override
			protected Boolean processRequest(HMSMessage<UUID> request) {
				UUID hubid = request.getData();
				if(hubid == InMemoryProviderTrackingWithHubProcessing.this.hubid) {
					try {
						ProviderTracking trackingdto = request.popReponsePoint(hms.dto.ProviderTracking.class).data;						
						InMemProviderTracking newdata = new InMemProviderTracking(trackingdto);
						InMemProviderTracking trackeddata = myproviders.putIfAbsent(trackingdto.getProviderid(), newdata);							
						if(newdata != trackeddata) { // existing --> remove tracking to be re-indexed.
							providerTrackingVPTree.remove(trackeddata);
							trackeddata.setLatLong(trackingdto);
						}
						providerTrackingVPTree.add(trackeddata);
						expiredTrackings.add(trackeddata);
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
			protected Class<UUID> getTConsumeManifest() {
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
	
	private KafkaStreamNodeBase<UUID, hms.dto.ProvidersGeoQueryResponse> buildQueryProvidersHubProcessor(){
		return this.queryProvidersHubProcessor = new ProviderProcessingNode<UUID, hms.dto.ProvidersGeoQueryResponse>() {
			@Override
			protected hms.dto.ProvidersGeoQueryResponse processRequest(HMSMessage<UUID> request) {
				UUID hubid = request.getData();
				if(hubid == InMemoryProviderTrackingWithHubProcessing.this.hubid) {
					try {
						hms.dto.GeoQuery querydto = request.popReponsePoint(hms.dto.GeoQuery.class).data;
						ProvidersGeoQueryResponse res = new ProvidersGeoQueryResponse();
						List<InMemProviderTracking> nearTrackings = providerTrackingVPTree.getAllWithinDistance(querydto, querydto.getDistance());
						for (InMemProviderTracking i : nearTrackings){
							res.add(new Provider(i.getProviderId(), "Should be loaded by provider id"));
						}
						return res;
					} catch (IOException e) {
						logger.error("Query Providers Hub Error {}", e.getMessage());
						return null;
					}
				}else {
					return new ProvidersGeoQueryResponse();
				}
			}

			@Override
			protected Class<UUID> getTConsumeManifest() {
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
