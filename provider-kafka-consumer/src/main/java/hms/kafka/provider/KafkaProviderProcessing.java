package hms.kafka.provider;

import java.io.IOException;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;

import hms.dto.ProviderTracking;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
import hms.kafka.streamming.HMSMessage.ResponsePoint;
import hms.provider.IProviderService;
import hms.provider.IProviderServiceProcessor;

public class KafkaProviderProcessing {
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderProcessing.class);
	private IProviderServiceProcessor providerService;
	
	KafkaStreamNodeBase<Void, Boolean>  clearProcessor; 
	KafkaStreamNodeBase<hms.dto.Provider, Boolean>  initProviderProcessor;
	KafkaStreamNodeBase<hms.dto.ProviderTracking, UUID>  trackingProviderProcessor;	
	KafkaStreamNodeBase<UUID, Boolean>  trackingProviderHubProcessor;

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
	
	private String getFullMessageName(String message) {
		return providerTopicPrefix+message;
	}
	
	private void buildClearProcessor() {
		this.clearProcessor = new KafkaStreamNodeBase<Void, Boolean>(
				logger,Void.class, kafkaserver, providerTopicGroup, 
				getFullMessageName(IProviderService.ClearMessage)) {
			@Override
			protected void processRequest(HMSMessage<Void> request) {
				providerService.clear();				
			}
		};
	}
	
	private void buildInitProviderProcessor() {
		this.initProviderProcessor = new KafkaStreamNodeBase<hms.dto.Provider, Boolean>(
				logger, hms.dto.Provider.class, kafkaserver, providerTopicGroup, 
				getFullMessageName(IProviderService.InitproviderMessage)) {
			@Override
			protected void processRequest(HMSMessage<hms.dto.Provider> request) {
				providerService.initprovider(request.getData());				
			}
		};
	}		
	
	private void buildTrackingProviderProcessor() {
		this.trackingProviderProcessor = new KafkaStreamNodeBase<hms.dto.ProviderTracking, UUID>(
				logger, hms.dto.ProviderTracking.class, kafkaserver, providerTopicGroup, 
				getFullMessageName(IProviderService.TrackingMessage)) {
			@Override
			protected void processRequest(HMSMessage<hms.dto.ProviderTracking> request) {	
				
			}
		};
	}	
	
	private void buildTrackingProviderHubProcessor() {
		this.trackingProviderHubProcessor = new KafkaStreamNodeBase<UUID, Boolean>(
				logger, UUID.class, kafkaserver, providerTopicGroup, 
				getFullMessageName(IProviderServiceProcessor.TrackingWithHubMessage)) {
			@Override
			protected void processRequest(HMSMessage<UUID> request) {
				UUID hubid = request.getData();
				ResponsePoint<ProviderTracking> trackingdto;
				try {
					trackingdto = request.popReponsePoint(hms.dto.ProviderTracking.class);
					providerService.tracking(trackingdto.data, hubid);				
				} catch (IOException e) {
					logger.error("Tracking Provider Hub Error", e.getMessage());
				}
			}
		};
	}	
}
