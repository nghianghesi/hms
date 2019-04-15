package hms.kafka.hub;

import java.io.Closeable;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.dto.Coordinate;
import hms.hub.IHubServiceProcessor;
import hms.hub.KafkaHubMeta;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
public class KafkaHubProcessing implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(KafkaHubProcessing.class);
	private IHubServiceProcessor hubService;
	
	KafkaStreamNodeBase<hms.dto.Coordinate, UUID>  getHubByCoordinateProcessor; 

	private String kafkaserver;
	private String hubGroup;
	
	private abstract class HubProcessingNode<TCon,TRep> extends KafkaStreamNodeBase<TCon,TRep>{
		
		@Override
		protected Logger getLogger() {
			return logger;
		}
		
		@Override
		protected String getGroupid() {
			return hubGroup;
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
	public KafkaHubProcessing(Config config, IHubServiceProcessor hubService) {
		this.hubService = hubService;
		
		if(config.hasPath(KafkaHMSMeta.ServerConfigKey)) {
			this.kafkaserver = config.getString(KafkaHMSMeta.ServerConfigKey);
		}else {
			logger.error("Missing {} configuration",KafkaHMSMeta.ServerConfigKey);
			throw new Error(String.format("Missing {} configuration",KafkaHMSMeta.ServerConfigKey));
		}
		
		this.hubGroup = "hms.hub";
		if(config.hasPath(KafkaHubMeta.GroupConfigKey)) {
			this.hubGroup = config.getString(KafkaHubMeta.GroupConfigKey);
		}
		
		this.buildHubByProviderCoordidateProcessor();
	}
	
	private void buildHubByProviderCoordidateProcessor() {
		this.getHubByCoordinateProcessor = new HubProcessingNode<hms.dto.Coordinate, UUID>() {
			@Override
			protected UUID processRequest(HMSMessage<hms.dto.Coordinate> request) {
				try {
					return hubService.getHostingHubId(request.getData().getLatitude(), request.getData().getLongitude()).get();
				} catch (InterruptedException | ExecutionException e) {
					logger.error("Get bub by provider coordinater error: {}", e.getMessage());
					return null;
				}				
			}

			@Override
			protected Class<Coordinate> getTConsumeManifest() {
				return Coordinate.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaHubMeta.MappingHubMessage;
			}
		};
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		this.getHubByCoordinateProcessor.shutDown();
	}

}
