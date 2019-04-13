package hms.kafka.provider;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.dto.Coordinate;
import hms.dto.Provider;
import hms.dto.ProviderTracking;
import hms.hub.KafkaHubMeta;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
import hms.kafka.streamming.HMSMessage.ResponsePoint;
import hms.provider.IProviderServiceProcessor;
import hms.provider.KafkaProviderMeta;

public class KafkaProviderProcessing {
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderProcessing.class);
	private IProviderServiceProcessor providerService;
	
	KafkaStreamNodeBase<Void, Boolean>  clearProcessor; 
	KafkaStreamNodeBase<hms.dto.Provider, Boolean>  initProviderProcessor;
	KafkaStreamNodeBase<hms.dto.ProviderTracking, Coordinate>  trackingProviderProcessor;	
	KafkaStreamNodeBase<UUID, Boolean>  trackingProviderHubProcessor;

	private String kafkaserver;
	private String providerGroup;
	
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
	}
	
	@Inject
	public KafkaProviderProcessing(Config config, IProviderServiceProcessor providerService) {
		this.providerService = providerService;	

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
		
		this.buildClearProcessor();
		this.buildInitProviderProcessor();
		this.buildTrackingProviderProcessor();
		this.buildTrackingProviderHubProcessor();	

		logger.info("Provider processing is ready");
	}
		
	private void buildClearProcessor() {
		this.clearProcessor = new ProviderProcessingNode<Void, Boolean>() {
			@Override
			protected Boolean processRequest(HMSMessage<Void> request) {
				try {
					return providerService.clear().get();					
				} catch (InterruptedException | ExecutionException e) {
					logger.error("clear provider error {}", e.getMessage());
					return false;
				}				
			}

			@Override
			protected Class<Void> getReqManifest() {
				return Void.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.ClearMessage;
			}

		};
	}
	
	private void buildInitProviderProcessor() {		
		this.initProviderProcessor = new ProviderProcessingNode<hms.dto.Provider, Boolean>() {
			@Override
			protected Boolean processRequest(HMSMessage<hms.dto.Provider> request) {
				try {
					return providerService.initprovider(request.getData()).get();
				} catch (InterruptedException | ExecutionException e) {
					logger.error("Init provider error {}", e.getMessage());
					return false;
				}				
			}

			@Override
			protected Class<Provider> getReqManifest() {
				return Provider.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.InitproviderMessage;
			}
		};
	}		
	
	private void buildTrackingProviderProcessor() {
		this.trackingProviderProcessor = new ProviderProcessingNode<hms.dto.ProviderTracking, Coordinate>() {
			@Override
			protected Coordinate processRequest(HMSMessage<hms.dto.ProviderTracking> request) {	
				return new Coordinate(request.getData().getLatitude(), request.getData().getLongitude());
			}
			
			@Override
			protected void ensureTopics() {
		        super.ensureTopics();
			}
			
			@Override
			protected Class<ProviderTracking> getReqManifest() {
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
					return providerService.tracking(trackingdto.data, hubid).join();
				} catch (IOException e) {
					logger.error("Tracking Provider Hub Error {}", e.getMessage());
					return false;
				}
			}

			@Override
			protected Class<UUID> getReqManifest() {
				return UUID.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaHubMeta.MappingHubMessage;
			}		
			
			@Override
			protected String getForwardTopic() {				
				return KafkaProviderMeta.TrackingMessage + KafkaHMSMeta.ReturnTopicSuffix;
			}	
		};
	}	
}
