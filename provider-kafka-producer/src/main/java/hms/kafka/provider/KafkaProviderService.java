package hms.kafka.provider;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.dto.GeoQuery;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.StreamRoot;
import hms.provider.IProviderService;
import hms.provider.KafkaProviderMeta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaProviderService implements IProviderService, Closeable{
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderService.class);

	StreamRoot<Void, Boolean>  clearStream; 
	StreamRoot<hms.dto.Provider, Boolean>  initProviderStream;
	StreamRoot<hms.dto.ProviderTracking, Boolean>  trackingProviderStream;
	StreamRoot<hms.dto.GeoQuery, hms.dto.ProvidersGeoQueryResponse>  queryProvidersStream;
	String server, rootid;	

	private abstract class ProviderStreamRoot<TStart,TRes> extends StreamRoot<TStart,TRes>{
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
	}
	
	@Inject
	public KafkaProviderService(Config config) {	
		if(config.hasPath(KafkaHMSMeta.ServerConfigKey)
				&& config.hasPath(KafkaHMSMeta.RootIdConfigKey)) {
			server = config.getString(KafkaHMSMeta.ServerConfigKey);
			rootid = config.getString(KafkaHMSMeta.RootIdConfigKey);
			
			clearStream = new ProviderStreamRoot<Void, Boolean>(){
				@Override
				protected String getStartTopic() {
					return KafkaProviderMeta.ClearMessage;
				}

				@Override
				protected Class<Boolean> getTConsumeManifest() {
					return Boolean.class;
				}				
			};
			
			initProviderStream = new ProviderStreamRoot<hms.dto.Provider, Boolean>(){
				@Override
				protected String getStartTopic() {
					return KafkaProviderMeta.InitproviderMessage;
				}

				@Override
				protected Class<Boolean> getTConsumeManifest() {
					return Boolean.class;
				}				
			};				
			
			trackingProviderStream = new ProviderStreamRoot<hms.dto.ProviderTracking, Boolean>(){
				@Override
				protected String getStartTopic() {
					return KafkaProviderMeta.TrackingMessage;
				}

				@Override
				protected Class<Boolean> getTConsumeManifest() {
					return Boolean.class;
				}				
			};
			
			queryProvidersStream = new ProviderStreamRoot<hms.dto.GeoQuery, hms.dto.ProvidersGeoQueryResponse>(){
				@Override
				protected String getStartTopic() {
					return KafkaProviderMeta.QueryProvidersMessage;
				}

				@Override
				protected Class<hms.dto.ProvidersGeoQueryResponse> getTConsumeManifest() {
					return hms.dto.ProvidersGeoQueryResponse.class;
				}
			};				
			
		}else {
			logger.error("Missing {} {} configuration", KafkaHMSMeta.ServerConfigKey, KafkaHMSMeta.RootIdConfigKey);
			throw new Error(String.format("Missing {} {} configuration",KafkaHMSMeta.ServerConfigKey,KafkaHMSMeta.RootIdConfigKey));
		}		
	}
	
	@Override
	public CompletableFuture<Boolean> clear() {
		return clearStream.startStream((requestid)->{
			return new HMSMessage<Void>(requestid);
		});
	}

	@Override
	public CompletableFuture<Boolean> initprovider(hms.dto.Provider providerdto) {
		return initProviderStream.startStream((requestid)->{
			return new HMSMessage<hms.dto.Provider>(requestid, providerdto);
 		});
	}

	@Override
	public CompletableFuture<Boolean> tracking(hms.dto.ProviderTracking trackingdto) {
		return trackingProviderStream.startStream((requestid)->{		
			return new HMSMessage<hms.dto.ProviderTracking>(requestid, trackingdto);
		});
	}

	@Override
	public void close() throws IOException {
		this.clearStream.shutDown();
		this.initProviderStream.shutDown();
		this.trackingProviderStream.shutDown();		
		this.queryProvidersStream.shutDown();
	}

	@Override
	public CompletableFuture<hms.dto.ProvidersGeoQueryResponse> queryProviders(GeoQuery query) {
		return queryProvidersStream.startStream((requestid)->{		
			return new HMSMessage<hms.dto.GeoQuery>(requestid, query);
		});
	}		
	
}

