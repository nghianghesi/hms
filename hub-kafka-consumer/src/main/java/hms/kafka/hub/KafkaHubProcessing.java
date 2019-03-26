package hms.kafka.hub;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.hub.IHubServiceProcessor;
import hms.hub.KafkaHubMeta;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
public class KafkaHubProcessing{
	private static final Logger logger = LoggerFactory.getLogger(KafkaHubProcessing.class);
	private IHubServiceProcessor hubService;
	
	KafkaStreamNodeBase<hms.dto.Coordinate, UUID>  getHubByCoordinateProcessor; 

	private String kafkaserver;
	private String hubGroup;
	
	@Inject
	public KafkaHubProcessing(Config config, IHubServiceProcessor hubService) {
		this.hubService = hubService;
		
		if(config.hasPath(KafkaHMSMeta.ServerConfigKey)) {
			this.kafkaserver = config.getString(KafkaHMSMeta.ServerConfigKey);
		}else {
			logger.error("Missing "+KafkaHMSMeta.ServerConfigKey+" configuration");
		}
		
		this.hubGroup = "hms.hub";
		if(config.hasPath(KafkaHubMeta.GroupConfigKey)) {
			this.hubGroup = config.getString(KafkaHubMeta.GroupConfigKey);
		}
		
		this.buildGetHubCoordinateProcessor();
	}
	
	private void buildGetHubCoordinateProcessor() {
		this.getHubByCoordinateProcessor = new KafkaStreamNodeBase<hms.dto.Coordinate, UUID>(
				logger,hms.dto.Coordinate.class, kafkaserver, hubGroup, KafkaHubMeta.MappingHubMessage) {
			@Override
			protected void processRequest(HMSMessage<hms.dto.Coordinate> request) {
				try {
					this.reply(request, hubService.getHostingHubId(request.getData().getLatitude(), request.getData().getLongitude()).get());
				} catch (InterruptedException | ExecutionException e) {
					logger.error("Clear provider error", e.getMessage());
				}				
			}
		};
	}

}
