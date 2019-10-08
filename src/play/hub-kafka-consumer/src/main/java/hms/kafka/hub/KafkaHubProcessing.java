package hms.kafka.hub;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.common.IHMSExecutorContext;
import hms.dto.Coordinate;
import hms.dto.GeoQuery;
import hms.hub.IHubServiceProcessor;
import hms.hub.KafkaHubMeta;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
import hms.kafka.streamming.KafkaStreamSplitNodeBase;
public class KafkaHubProcessing implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(KafkaHubProcessing.class);
	private IHubServiceProcessor hubService;
	IHMSExecutorContext ec;
	
	KafkaStreamNodeBase<hms.dto.Coordinate, UUID>  getHubByCoordinateProcessor; 
	KafkaStreamNodeBase<hms.dto.GeoQuery, List<UUID>>  getCoveringHubsProcessor; 	 
	KafkaStreamSplitNodeBase<hms.dto.GeoQuery, UUID>  getAndSplitCoveringHubsProcessor;
	
	private String kafkaserver;
	private String hubGroup;
	
	
	private static String applyHubIdTemplateToRepForTopic(String topic, Object value) {
		return topic.replaceAll("\\{hubid\\}", value!=null ? value.toString() : "");
	}
	
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
		
		@Override
		protected Executor getExecutorService() {
			return ec.getExecutor();
		}	
		
		@Override
		protected Executor getPollingService() {
			return ec.getExecutor();
		}			
		
	
		@Override
		protected String applyTemplateToRepForTopic(String topic, Object value) {
			return applyHubIdTemplateToRepForTopic(topic,value);
		} 		
	}
	
	@Inject
	public KafkaHubProcessing(Config config, IHubServiceProcessor hubService,IHMSExecutorContext ec) {
		this.hubService = hubService;
		this.ec= ec;
		
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
		
		this.buildMappingHubProcessor();
		this.buildGetCoveringHubsProcessor();
		this.buildGetAndSplitCoveringHubsProcessor();
	}
	
	private void buildMappingHubProcessor() {
		this.getHubByCoordinateProcessor = new HubProcessingNode<hms.dto.Coordinate, UUID>() {
			@Override
			protected UUID processRequest(HMSMessage<hms.dto.Coordinate> request) {
				return hubService.getHostingHubId(request.getData().getLatitude(), request.getData().getLongitude());
			}

			@Override
			protected Class<? extends Coordinate> getTConsumeManifest() {
				return Coordinate.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaHubMeta.MappingHubMessage;
			}
		};
	}
	

	private void buildGetCoveringHubsProcessor() {
		this.getCoveringHubsProcessor = new HubProcessingNode<hms.dto.GeoQuery, List<UUID>>() {
			@Override
			protected List<UUID> processRequest(HMSMessage<hms.dto.GeoQuery> request) {
				return hubService.getConveringHubs(request.getData());
			}

			@Override
			protected Class<? extends hms.dto.GeoQuery> getTConsumeManifest() {
				return hms.dto.GeoQuery.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaHubMeta.FindCoveringHubsMessage;
			}
		};
	}
	
	private void buildGetAndSplitCoveringHubsProcessor() {
		this.getAndSplitCoveringHubsProcessor = new KafkaStreamSplitNodeBase<GeoQuery, UUID>() {
			
			@Override
			protected List<UUID> processRequest(HMSMessage<GeoQuery> request) {
				return hubService.getConveringHubs(request.getData());
			}
			
			@Override
			protected Class<? extends GeoQuery> getTConsumeManifest() {
				return GeoQuery.class;
			}
			
			@Override
			protected String getServer() {
				return kafkaserver;
			}
			
			@Override
			protected Logger getLogger() {
				return logger;
			}
			
			@Override
			protected String getGroupid() {
				return hubGroup;
			}
			
			@Override
			protected String getForwardTopic() {
				return this.getConsumeTopic()+"{hubid}"+KafkaHMSMeta.ReturnTopicSuffix;
			}
			
			@Override
			protected Executor getExecutorService() {
				return ec.getExecutor();
			}
			
			@Override
			protected Executor getPollingService() {
				return ec.getExecutor();
			}				
			
			@Override
			protected String getConsumeTopic() {
				return KafkaHubMeta.FindAndSplitCoveringHubsMessage;
			}		

			@Override
			protected String applyTemplateToRepForTopic(String topic, Object value) {
				return applyHubIdTemplateToRepForTopic(topic,value);
			} 					
		};
	}

	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		this.getHubByCoordinateProcessor.shutDown();
		this.getCoveringHubsProcessor.shutDown();
		this.getAndSplitCoveringHubsProcessor.shutDown();
	}

}
