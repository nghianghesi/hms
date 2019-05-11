package hms.kafka.provider;

import java.io.Closeable;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.inject.Inject;

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
import hms.provider.KafkaProviderMeta;

public class InMemoryProviderProcessing implements Closeable{
	private static final Logger logger = LoggerFactory.getLogger(InMemoryProviderProcessing.class);
	IHMSExecutorContext ec;
	
	KafkaStreamNodeBase<UUID, Boolean>  trackingProviderHubProcessor;
	KafkaStreamNodeBase<UUID, hms.dto.ProvidersGeoQueryResponse>  queryProvidersHubProcessor;

	private String kafkaserver;
	private String providerGroup;
	private UUID hubid;
	private final VPTree<LatLongLocation, InMemProviderTracking> providerTrackingVPTree =
	        new VPTree<LatLongLocation, InMemProviderTracking>(
	                new ProviderDistanceFunction());
	private final Map<UUID, InMemProviderTracking> myproviders =
	        		new HashMap<UUID, InMemProviderTracking>();
	private class InMemProviderTracking implements LatLongLocation {
		private final double latitude;
		private final double longitude;
		private final UUID providerId;
		
		public double getLatitude() {
			return latitude;
		}

		public double getLongitude() {
			return longitude;
		}

		public UUID getProviderId() {
			return providerId;
		}
		
		public InMemProviderTracking(hms.dto.ProviderTracking dto) {
			this.latitude = dto.getLatitude();
			this.longitude = dto.getLongitude();
			this.providerId = dto.getProviderid();
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
	public InMemoryProviderProcessing(Config config, IHMSExecutorContext ec) {
		this.ec = ec;

		if(config.hasPath(KafkaHMSMeta.ServerConfigKey)) {
			this.kafkaserver = config.getString(KafkaHMSMeta.ServerConfigKey);
		}else {
			logger.error("Missing {} configuration", KafkaHMSMeta.ServerConfigKey);
			throw new Error(String.format("Missing {} configuration", KafkaHMSMeta.ServerConfigKey));
		}
		
		this.providerGroup = "hms.provider";
		if(config.hasPath(KafkaProviderMeta.ProviderGroupConfigKey)) {
			this.providerGroup = config.getString(KafkaProviderMeta.ProviderGroupConfigKey);
		}
		
		if(config.hasPath(KafkaProviderMeta.ProviderInmemHubIdConfigKey)) {
			this.hubid = UUID.fromString(config.getString(KafkaProviderMeta.ProviderInmemHubIdConfigKey));
		}else {
			logger.error("Missing {} configuration", KafkaProviderMeta.ProviderInmemHubIdConfigKey);
			throw new Error(String.format("Missing {} configuration", KafkaProviderMeta.ProviderInmemHubIdConfigKey));
		}
		
		this.buildTrackingProviderHubProcessor();	
		this.buildQueryProvidersHubProcessor();	

		logger.info("Provider processing is ready");
	}
	
	
	private void buildTrackingProviderHubProcessor() {
		this.trackingProviderHubProcessor = new ProviderProcessingNode<UUID, Boolean>() {
			@Override
			protected Boolean processRequest(HMSMessage<UUID> request) {
				UUID hubid = request.getData();
				if(hubid == InMemoryProviderProcessing.this.hubid) {
					try {
						ProviderTracking trackingdto = request.popReponsePoint(hms.dto.ProviderTracking.class).data;
						InMemProviderTracking data = new InMemProviderTracking(trackingdto);
						providerTrackingVPTree.add(data);
						if(!myproviders.containsKey(trackingdto.getProviderid())) {
							myproviders.put(data.getProviderId(), data);
						}
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
			protected Class<UUID> getTConsumeManifest() {
				return UUID.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.TrackingWithHubMessage+hubid.toString();
			}		
			
			@Override
			protected String getForwardTopic() {				
				return KafkaProviderMeta.TrackingMessage + KafkaHMSMeta.ReturnTopicSuffix;
			}	
		};
	}	
	
	private void buildQueryProvidersHubProcessor(){
		this.queryProvidersHubProcessor = new ProviderProcessingNode<UUID, hms.dto.ProvidersGeoQueryResponse>() {
			@Override
			protected hms.dto.ProvidersGeoQueryResponse processRequest(HMSMessage<UUID> request) {
				UUID hubid = request.getData();
				if(hubid == InMemoryProviderProcessing.this.hubid) {
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
				return KafkaProviderMeta.QueryProvidersWithHubsMessage+hubid.toString();
			}		
			
			@Override
			protected String getForwardTopic() {				
				return KafkaProviderMeta.QueryProvidersMessage + KafkaHMSMeta.ReturnTopicSuffix;
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
