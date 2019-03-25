package hms.kafka.provider;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;

import hms.dto.Coordinate;
import hms.dto.ProviderTracking;
import hms.hub.KafkaHubMeta;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
import hms.kafka.streamming.HMSMessage.ResponsePoint;
import hms.kafka.streamming.KafkaMessageUtils;
import hms.provider.IProviderServiceProcessor;
import hms.provider.KafkaProviderMeta;

public class KafkaProviderProcessing {
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderProcessing.class);
	private IProviderServiceProcessor providerService;
	
	KafkaStreamNodeBase<Void, Boolean>  clearProcessor; 
	KafkaStreamNodeBase<hms.dto.Provider, Boolean>  initProviderProcessor;
	KafkaStreamNodeBase<hms.dto.ProviderTracking, UUID>  trackingProviderProcessor;	
	KafkaStreamNodeBase<UUID, Boolean>  trackingProviderHubProcessor;

	private String kafkaserver;
	private String providerGroup;
	
	@Inject
	public KafkaProviderProcessing(Config config, IProviderServiceProcessor providerService) {
		this.providerService = providerService;
		this.buildClearProcessor();
		this.buildInitProviderProcessor();
		this.buildTrackingProviderProcessor();
		this.buildTrackingProviderHubProcessor();
	}
		
	private void buildClearProcessor() {
		this.clearProcessor = new KafkaStreamNodeBase<Void, Boolean>(
				logger,Void.class, kafkaserver, providerGroup, KafkaProviderMeta.ClearMessage) {
			@Override
			protected void processRequest(HMSMessage<Void> request) {
				try {
					this.reply(request, providerService.clear().get());
				} catch (InterruptedException | ExecutionException e) {
					logger.error("clear provider error", e.getMessage());
				}				
			}
		};
	}
	
	private void buildInitProviderProcessor() {		
		this.initProviderProcessor = new KafkaStreamNodeBase<hms.dto.Provider, Boolean>(
				logger, hms.dto.Provider.class, kafkaserver, providerGroup, 
				KafkaProviderMeta.InitproviderMessage) {
			@Override
			protected void processRequest(HMSMessage<hms.dto.Provider> request) {
				try {
					this.reply(request, providerService.initprovider(request.getData()).get());
				} catch (InterruptedException | ExecutionException e) {
					logger.error("Init provider error", e.getMessage());
				}				
			}
		};
	}		
	
	private void buildTrackingProviderProcessor() {
		this.trackingProviderProcessor = new KafkaStreamNodeBase<hms.dto.ProviderTracking, UUID>(
				logger, hms.dto.ProviderTracking.class, kafkaserver, providerGroup, 
				KafkaProviderMeta.TrackingMessage) {
			@Override
			protected void processRequest(HMSMessage<hms.dto.ProviderTracking> request) {	
				HMSMessage<hms.dto.Coordinate> getHubIdReg = request.forwardRequest();
				getHubIdReg.setData(new Coordinate(request.getData().getLatitude(), request.getData().getLongitude()));
				try {//forward to find hubid, then back to TrackingWithHubMessage
					getHubIdReg.addReponsePoint(KafkaProviderMeta.TrackingWithHubMessage, request.getData());					
					ProducerRecord<String, byte[]> record = KafkaMessageUtils.getProcedureRecord(request, KafkaHubMeta.MappingHubMessage);					
					this.producer.send(record);
				} catch (IOException e) {
					logger.error("Forward To Hub Error", e.getMessage());
				}
			}
		};
	}	
	
	private void buildTrackingProviderHubProcessor() {
		this.trackingProviderHubProcessor = new KafkaStreamNodeBase<UUID, Boolean>(
				logger, UUID.class, kafkaserver, providerGroup, 
				KafkaProviderMeta.TrackingWithHubMessage) {
			@Override
			protected void processRequest(HMSMessage<UUID> request) {
				UUID hubid = request.getData();
				ResponsePoint<ProviderTracking> trackingdto;
				try {
					trackingdto = request.popReponsePoint(hms.dto.ProviderTracking.class);
					Boolean trackingRes = providerService.tracking(trackingdto.data, hubid).join();
					this.reply(request, trackingRes);
				} catch (IOException e) {
					logger.error("Tracking Provider Hub Error", e.getMessage());
				}
			}
		};
	}	
}
