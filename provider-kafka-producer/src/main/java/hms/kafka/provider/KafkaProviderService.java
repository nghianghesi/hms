package hms.kafka.provider;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;
import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.common.IHMSExecutorContext;
import hms.dto.GeoQuery;
import hms.kafka.streamming.MonoStreamRoot;
import hms.provider.IAsynProviderService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaProviderService implements IAsynProviderService, Closeable{
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderService.class);

	private KafkaProviderTopics topicSettings;
	MonoStreamRoot<hms.dto.ProviderTracking, Boolean>  trackingProviderStream;
	MonoStreamRoot<hms.dto.GeoQuery, List<hms.dto.Provider>>  queryProvidersStream;
	String server, rootid;	
	
	
	IHMSExecutorContext ec;
	private ExecutorService pollingEx = Executors.newFixedThreadPool(1);
	private abstract class ProviderStreamRoot<TStart,TRes> extends MonoStreamRoot<TStart,TRes>{
		@Override
		protected Logger getLogger() {
			return logger;
		}
		
		@Override
		protected String getGroupid() {
			return rootid;
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
	public KafkaProviderService(Config config,IHMSExecutorContext ec, KafkaProviderTopics settings) {	
		this.topicSettings = settings;
		this.ec = ec;
		if(config.hasPath(KafkaHMSMeta.ServerConfigKey)
				&& config.hasPath(KafkaHMSMeta.RootIdConfigKey)) {
			server = config.getString(KafkaHMSMeta.ServerConfigKey);
			rootid = config.getString(KafkaHMSMeta.RootIdConfigKey);
						
			trackingProviderStream = new ProviderStreamRoot<hms.dto.ProviderTracking, Boolean>(){
				@Override
				protected String getStartTopic() {
					return topicSettings.getTrackingTopic();
				}
				
				@Override
				protected String getReturnTopic() {
					return topicSettings.getTrackingTopic()+KafkaHMSMeta.ReturnTopicSuffix;
				}
				
				@Override
				protected Class<Boolean> getTConsumeManifest() {
					return Boolean.class;
				}

				@Override
				protected <TData> String getZone(TData data) {
					return "none";
				}				
			};
			trackingProviderStream.addZones("none", this.server);
			
			queryProvidersStream = new ProviderStreamRoot<hms.dto.GeoQuery, List<hms.dto.Provider>>(){
				@SuppressWarnings("unchecked")
				private Class<? extends List<hms.dto.Provider>> template = (Class<? extends List<hms.dto.Provider>>)(new ArrayList<hms.dto.Provider>()).getClass();
				@Override
				protected String getStartTopic() {
					return topicSettings.getQueryTopic();
				}				

				@Override
				protected String getReturnTopic() {
					return topicSettings.getQueryTopic()+KafkaHMSMeta.ReturnTopicSuffix;
				}
				
				@Override
				protected Class<? extends List<hms.dto.Provider>> getTConsumeManifest() {
					return template;
				}

				@Override
				protected <TData> String getZone(TData data) {
					return "none";
				}
			};			
			queryProvidersStream.addZones("none", this.server);
			trackingProviderStream.run();
			queryProvidersStream.run();
		}else {
			logger.error("Missing {} {} configuration", KafkaHMSMeta.ServerConfigKey, KafkaHMSMeta.RootIdConfigKey);
			throw new Error(String.format("Missing {} {} configuration",KafkaHMSMeta.ServerConfigKey,KafkaHMSMeta.RootIdConfigKey));
		}		
	}

	@Override
	public CompletableFuture<Boolean> asynTracking(hms.dto.ProviderTracking trackingdto) {
		return trackingProviderStream.startStream(trackingdto);
	}

	@Override
	public CompletableFuture<? extends List<hms.dto.Provider>> asynQueryProviders(GeoQuery query) {
		return queryProvidersStream.startStream(query);
	}			

	@Override
	public void close() throws IOException {
		this.trackingProviderStream.shutDown();		
		this.queryProvidersStream.shutDown();
	}	
}

