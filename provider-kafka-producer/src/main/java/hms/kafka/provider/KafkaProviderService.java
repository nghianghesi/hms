package hms.kafka.provider;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

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

	private KafkaProviderSettings topicSettings;
	MonoStreamRoot<hms.dto.ProviderTracking, Boolean>  trackingProviderStream;
	MonoStreamRoot<hms.dto.GeoQuery, List<hms.dto.Provider>>  queryProvidersStream;
	String server, rootid;	
	
	
	IHMSExecutorContext ec;
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
		protected String getServer() {
			return server;
		}

		@Override
		protected String getForwardTopic() {
			return null;
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
	
	@Inject
	public KafkaProviderService(Config config,IHMSExecutorContext ec, KafkaProviderSettings settings) {	
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
				protected Class<Boolean> getTConsumeManifest() {
					return Boolean.class;
				}				
			};
			
			queryProvidersStream = new ProviderStreamRoot<hms.dto.GeoQuery, List<hms.dto.Provider>>(){
				@SuppressWarnings("unchecked")
				private Class<? extends List<hms.dto.Provider>> template = (Class<? extends List<hms.dto.Provider>>)(new ArrayList<hms.dto.Provider>()).getClass();
				@Override
				protected String getStartTopic() {
					return topicSettings.getQueryTopic();
				}

				@Override
				protected Class<? extends List<hms.dto.Provider>> getTConsumeManifest() {
					return template;
				}
			};				
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

