package hms.kafka.provider;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.common.IHMSExecutorContext;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.KafkaStreamNodeBase;
import hms.provider.IProviderService;
import hms.provider.KafkaProviderMeta;

public class KafkaProviderProcessing implements Closeable {
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderProcessing.class);
	private IProviderService providerService;
	IHMSExecutorContext ec;
	
	KafkaStreamNodeBase<hms.dto.ProviderTracking, Boolean>  trackingProviderHubProcessor;
	KafkaStreamNodeBase<hms.dto.GeoQuery, List<hms.dto.Provider>>  queryProvidersHubProcessor;

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
		
		private Executor pollingEx = Executors.newFixedThreadPool(1);
		@Override
		protected Executor getPollingService() {
			return pollingEx;
		}
	}
	
	@Inject
	public KafkaProviderProcessing(Config config, IProviderService providerService, IHMSExecutorContext ec) {
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
		
		this.buildTrackingProviderHubProcessor();	
		this.buildQueryProvidersHubProcessor();	

		logger.info("Provider processing is ready");
	}
		
	
	private void buildTrackingProviderHubProcessor() {
		this.trackingProviderHubProcessor = new ProviderProcessingNode<hms.dto.ProviderTracking, Boolean>() {
			@Override
			protected Boolean processRequest(HMSMessage<hms.dto.ProviderTracking> request) {
				hms.dto.ProviderTracking trackingdto = request.getData();
				return providerService.tracking(trackingdto);
			}

			@Override
			protected Class<hms.dto.ProviderTracking> getTConsumeManifest() {
				return hms.dto.ProviderTracking.class;
			}

			@Override
			protected String getConsumeTopic() {
				return KafkaProviderMeta.TrackingMessage;
			}		
			
			@Override
			protected String getForwardTopic() {				
				return KafkaProviderMeta.TrackingMessage + KafkaHMSMeta.ReturnTopicSuffix;
			}	
		};
		
		this.trackingProviderHubProcessor.run();
	}
	
	
	private void buildQueryProvidersHubProcessor(){
		this.queryProvidersHubProcessor = new ProviderProcessingNode<hms.dto.GeoQuery, List<hms.dto.Provider>>() {
			@Override
			protected List<hms.dto.Provider> processRequest(HMSMessage<hms.dto.GeoQuery> request) {
				hms.dto.GeoQuery querydto = request.getData();
				return providerService.queryProviders(querydto);
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
				return KafkaProviderMeta.QueryProvidersMessage + KafkaHMSMeta.ReturnTopicSuffix;
			}	
		};
		
		this.queryProvidersHubProcessor.run();
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		this.trackingProviderHubProcessor.shutDown();
		this.queryProvidersHubProcessor.shutDown();
	}	
}
