package hms.kafka.provider;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Executor;
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
import hms.provider.KafkaProviderMeta;

public class InMemoryProviderProcessing implements Closeable{
	private static final Logger logger = LoggerFactory.getLogger(InMemoryProviderProcessing.class);
	IHMSExecutorContext ec;
	
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

		@Override
		protected Executor getExecutorService() {
			return ec.getExecutor();
		}
		
		@Override
		protected Executor getPollingService() {
			return ec.getExecutor();
		}
	}	
	
	KafkaStreamNodeBase<hms.dto.ProviderTracking, Coordinate>  trackingProviderProcessor;	
	KafkaStreamNodeBase<hms.dto.GeoQuery, hms.dto.GeoQuery>  queryProvidersProcessor;	

	@Inject
	public InMemoryProviderProcessing(Config config, IHMSExecutorContext ec) {
		this.ec = ec;

		if(config.hasPath(KafkaHMSMeta.ServerConfigKey)) {
			this.kafkaserver = config.getString(KafkaHMSMeta.ServerConfigKey);
		}else {
			logger.error("Missing {} configuration", KafkaHMSMeta.ServerConfigKey);
			throw new Error(String.format("Missing {} configuration",KafkaHMSMeta.ServerConfigKey));
		}
		
		this.providerGroup = "hms.provider";
		if(config.hasPath(KafkaProviderMeta.ProviderGroupConfigKey)) {
			this.providerGroup = config.getString(KafkaProviderMeta.ProviderGroupConfigKey);
		}
				
		this.buildTrackingProviderProcessor();
		this.buildQueryProvidersProcessor();
		
		this.trackingProviderProcessor.run();
		this.queryProvidersProcessor.run();
	}
	

	private void buildTrackingProviderProcessor() {
		this.trackingProviderProcessor = new ProviderProcessingNode<hms.dto.ProviderTracking, Coordinate>() {
			@Override
			protected Coordinate processRequest(HMSMessage<hms.dto.ProviderTracking> request) {	
				return new Coordinate(request.getData().getLatitude(), request.getData().getLongitude());
			}
			
			@Override
			protected Class<? extends ProviderTracking> getTConsumeManifest() {
				return ProviderTracking.class;
			}
			
			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.InMemTrackingMessage;
			}		
			
			@Override
			protected String getForwardTopic() {
				return KafkaHubMeta.MappingHubMessage;
			}	
			
			@Override
			protected String getForwardBackTopic() {
				return KafkaProviderMeta.InMemTrackingWithHubMessage+"{hubid}";
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
			protected Class<? extends hms.dto.GeoQuery> getTConsumeManifest() {
				return hms.dto.GeoQuery.class;
			}
			
			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.InMemQueryProvidersMessage;
			}		
			
			@Override
			protected String getForwardTopic() {
				return KafkaHubMeta.FindAndSplitCoveringHubsMessage;
			}	
			
			@Override
			protected String getForwardBackTopic() {
				return KafkaProviderMeta.InMemQueryProvidersWithHubsMessage+"{hubid}";
			}				
		};
	}


	@Override
	public void close() throws IOException {
		trackingProviderProcessor.shutDown();	
		queryProvidersProcessor.shutDown();		
	}	
}
