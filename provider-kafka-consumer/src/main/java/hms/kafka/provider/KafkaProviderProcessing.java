package hms.kafka.provider;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;

import hms.dto.ProviderTracking;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaConsumerBase;
import hms.kafka.streamming.HMSMessage.ResponsePoint;
import hms.provider.IProviderService;
import hms.provider.IProviderServiceProcessor;

public class KafkaProviderProcessing {
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderProcessing.class);
	private IProviderServiceProcessor providerService;
	
	KafkaConsumerBase<Void>  clearProcessor; 
	KafkaConsumerBase<hms.dto.Provider>  initProviderProcessor;
	KafkaConsumerBase<hms.dto.ProviderTracking>  trackingProviderProcessor;	
	KafkaConsumerBase<UUID>  trackingProviderHubProcessor;

	private String kafkaserver;
	private String providerTopicPrefix;
	private String providerTopicGroup;
	@Inject
	public KafkaProviderProcessing(Config config, IProviderServiceProcessor providerService) {
		this.providerService = providerService;
		this.buildClearProcessor();
		this.buildInitProviderProcessor();
		this.buildTrackingProviderProcessor();
		this.buildTrackingProviderHubProcessor();
	}
	
	private void buildClearProcessor() {
		this.clearProcessor = new KafkaConsumerBase<Void>(
				logger,Void.class,
				kafkaserver, providerTopicGroup, providerTopicPrefix+IProviderService.ClearMessage) {
			@Override
			protected void processRequest(HMSMessage<Void> request) {
				providerService.clear();				
			}
		};
	}
	
	private void buildInitProviderProcessor() {
		this.initProviderProcessor = new KafkaConsumerBase<hms.dto.Provider>(
				logger, hms.dto.Provider.class,
				kafkaserver, providerTopicGroup, providerTopicPrefix+IProviderService.InitproviderMessage) {
			@Override
			protected void processRequest(HMSMessage<hms.dto.Provider> request) {
				providerService.initprovider(request.getData());				
			}
		};
	}		
	
	private void buildTrackingProviderProcessor() {
		this.trackingProviderProcessor = new KafkaConsumerBase<hms.dto.ProviderTracking>(
				logger,hms.dto.ProviderTracking.class,
				kafkaserver, providerTopicGroup, providerTopicPrefix+IProviderService.TrackingMessage) {
			@Override
			protected void processRequest(HMSMessage<hms.dto.ProviderTracking> request) {						
			}
		};
	}	
	private void buildTrackingProviderHubProcessor() {
		this.trackingProviderHubProcessor = new KafkaConsumerBase<UUID>(
				logger,
				UUID.class,
				kafkaserver, providerTopicGroup, providerTopicPrefix+IProviderService.TrackingMessage) {
			@Override
			protected void processRequest(HMSMessage<UUID> request) {
				UUID hubid = request.getData();
				ResponsePoint<ProviderTracking> trackingdto;
				try {
					trackingdto = request.popReponsePoint(hms.dto.ProviderTracking.class);
					providerService.tracking(trackingdto.data, hubid);				
				} catch (IOException e) {
					// TODO Auto-generated catch block
					logger.error("Tracking Provider Hub Error", e.getMessage());
				}
			}
		};
	}	
}
