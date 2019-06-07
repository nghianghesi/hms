package hms.kafka.provider;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.common.IHMSExecutorContext;
import hms.dto.Coordinate;
import hms.dto.ProviderTracking;
import hms.hub.KafkaHubMeta;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
import hms.kafka.streamming.HMSMessage.ResponsePoint;
import hms.provider.IProviderServiceProcessor;
import hms.provider.KafkaProviderMeta;

public class KafkaProviderProcessing implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderProcessing.class);
	private IProviderServiceProcessor providerService;
	IHMSExecutorContext ec;
	
	KafkaStreamNodeBase<hms.dto.ProviderTracking, Coordinate>  trackingProviderProcessor;	
	KafkaStreamNodeBase<UUID, Boolean>  trackingProviderHubProcessor;
	KafkaStreamNodeBase<hms.dto.GeoQuery, hms.dto.GeoQuery>  queryProvidersProcessor;	
	KafkaStreamNodeBase<List<UUID>, List<hms.dto.Provider>>  queryProvidersHubProcessor;

	private String kafkaserver;
	private String providerGroup;
	private Executor pollingEx = Executors.newFixedThreadPool(1);
	
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
		
		@Override
		protected Executor getPollingService() {
			return pollingEx;
		}
	}
	
	@Inject
	public KafkaProviderProcessing(Config config, IProviderServiceProcessor providerService, IHMSExecutorContext ec) {
		this.providerService = providerService;	
		this.ec = ec;

		if(config.hasPath(KafkaHMSMeta.ServerConfigKey)) {
			this.kafkaserver = config.getString(KafkaHMSMeta.ServerConfigKey);
		}else {
			logger.error("Missing {} configuration",KafkaHMSMeta.ServerConfigKey);
			throw new Error(String.format("Missing {} configuration",KafkaHMSMeta.ServerConfigKey));
		}
		
		this.providerGroup = "hms.provider";
		if(config.hasPath(KafkaProviderMeta.ProviderGroupConfigKey)) {
			this.providerGroup = config.getString(KafkaProviderMeta.ProviderGroupConfigKey);
		}
		
		this.buildTrackingProviderProcessor();
		this.buildTrackingProviderHubProcessor();	
		this.buildQueryProvidersProcessor();
		this.buildQueryProvidersHubProcessor();	

		logger.info("Provider processing is ready");
	}
		
	
	private void buildTrackingProviderProcessor() {
		this.trackingProviderProcessor = new ProviderProcessingNode<hms.dto.ProviderTracking, Coordinate>() {
			@Override
			protected Coordinate processRequest(HMSMessage<hms.dto.ProviderTracking> request) {	
				return new Coordinate(request.getData().getLatitude(), request.getData().getLongitude());
			}
			
			@Override
			protected Class<ProviderTracking> getTConsumeManifest() {
				return ProviderTracking.class;
			}
			
			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.TrackingMessage;
			}		
			
			@Override
			protected String getForwardTopic() {
				return KafkaHubMeta.MappingHubMessage;
			}	
			
			@Override
			protected String getForwardBackTopic() {
				return KafkaProviderMeta.TrackingWithHubMessage;
			}				
		};
	}	
	
	private void buildTrackingProviderHubProcessor() {
		this.trackingProviderHubProcessor = new ProviderProcessingNode<UUID, Boolean>() {
			@Override
			protected Boolean processRequest(HMSMessage<UUID> request) {
				UUID hubid = request.getData();
				ResponsePoint<ProviderTracking> trackingdto;
				try {
					trackingdto = request.popReponsePoint(hms.dto.ProviderTracking.class);
					return providerService.tracking(trackingdto.data, hubid);
				} catch (IOException e) {
					logger.error("Tracking Provider Hub Error {}", e.getMessage());
					return false;
				}
			}

			@Override
			protected Class<UUID> getTConsumeManifest() {
				return UUID.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.TrackingWithHubMessage;
			}		
			
			@Override
			protected String getForwardTopic() {				
				return KafkaProviderMeta.TrackingMessage + KafkaHMSMeta.ReturnTopicSuffix;
			}	
		};
	}
	
	

	private void buildQueryProvidersProcessor() {
		this.queryProvidersProcessor = new ProviderProcessingNode<hms.dto.GeoQuery, hms.dto.GeoQuery>() {
			@Override
			protected hms.dto.GeoQuery processRequest(HMSMessage<hms.dto.GeoQuery> request) {	
				return new hms.dto.GeoQuery(request.getData().getLatitude(),request.getData().getLongitude(), request.getData().getDistance());
			}
			
			@Override
			protected Class<hms.dto.GeoQuery> getTConsumeManifest() {
				return hms.dto.GeoQuery.class;
			}
			
			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.QueryProvidersMessage;
			}		
			
			@Override
			protected String getForwardTopic() {
				return KafkaHubMeta.FindCoveringHubsMessage;
			}	
			
			@Override
			protected String getForwardBackTopic() {
				return KafkaProviderMeta.QueryProvidersWithHubsMessage;
			}				
		};
	}
	
	private void buildQueryProvidersHubProcessor(){
		this.queryProvidersHubProcessor = new ProviderProcessingNode<List<UUID>, List<hms.dto.Provider>>() {
			@SuppressWarnings("unchecked")
			private Class<? extends List<UUID>> template = (Class<? extends List<UUID>>)(new ArrayList<UUID>()).getClass();
			@Override
			protected List<hms.dto.Provider> processRequest(HMSMessage<List<UUID>> request) {
				List<UUID> hubids = request.getData();
				hms.dto.GeoQuery querydto;
				try {
					querydto = request.popReponsePoint(hms.dto.GeoQuery.class).data;
					return providerService.queryProviders(hubids, querydto);
				} catch (IOException e) {
					logger.error("Query Providers Hub Error {}", e.getMessage());
					return null;
				}
			}

			@Override
			protected Class<? extends List<UUID>> getTConsumeManifest() {
				return template;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.QueryProvidersWithHubsMessage;
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
		this.trackingProviderProcessor.shutDown();
		this.trackingProviderHubProcessor.shutDown();
		this.queryProvidersProcessor.shutDown();
		this.queryProvidersHubProcessor.shutDown();
	}	
}
