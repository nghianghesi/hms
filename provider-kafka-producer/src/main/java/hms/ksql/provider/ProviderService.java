package hms.ksql.provider;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.StreamResponse;
import hms.common.IHMSExecutorContext;
import hms.dto.Provider;
import hms.dto.ProviderTracking;
import hms.ksql.KSQLRoot;
import hms.provider.IProviderService;
import hms.provider.KafkaProviderMeta;

public class ProviderService implements IProviderService{
	private static final Logger logger = LoggerFactory.getLogger(ProviderService.class);
	
	KSQLRoot<Void, Boolean>  clearStream; 
	KSQLRoot<hms.dto.Provider, Boolean>  initProviderStream;
	KSQLRoot<hms.dto.ProviderTracking, Boolean>  trackingProviderStream;
	private IHMSExecutorContext execContext;
	
	private String server;
	private String rootid;
	private abstract class ProviderKSQLRoot<TR,TRes> extends KSQLRoot<TR,TRes>{		
		@Override
		protected Logger getLogger() {
			return logger;
		}

		@Override
		protected String getKafkaServer() {
			// TODO Auto-generated method stub
			return server;
		}

		@Override
		protected String getServerid() {
			// TODO Auto-generated method stub
			return rootid;
		}
	}
	
	@Inject
	public ProviderService(IHMSExecutorContext ec, Config config) {
		this.execContext = ec;	
		if(config.hasPath(KafkaHMSMeta.ServerConfigKey)
				&& config.hasPath(KafkaHMSMeta.RootIdConfigKey)) {
			server = config.getString(KafkaHMSMeta.ServerConfigKey);
			rootid = config.getString(KafkaHMSMeta.RootIdConfigKey);
			
			clearStream = new ProviderKSQLRoot<Void, Boolean>(){
				@Override
				protected Class<Boolean> getMessageManifest() {
					return Boolean.class;
				}

				@Override
				protected String getGetReqestTopic() {
					return KafkaProviderMeta.ClearMessage;
				}
				
			};
			
			initProviderStream = new ProviderKSQLRoot<hms.dto.Provider, Boolean>(){

				@Override
				protected Class<Boolean> getMessageManifest() {
					return Boolean.class;
				}

				@Override
				protected String getGetReqestTopic() {
					return KafkaProviderMeta.InitproviderMessage;
				}
				
			};						
		
			trackingProviderStream = new ProviderKSQLRoot<hms.dto.ProviderTracking, Boolean>(){

				@Override
				protected Class<Boolean> getMessageManifest() {
					return Boolean.class;
				}

				@Override
				protected String getGetReqestTopic() {
					// TODO Auto-generated method stub
					return KafkaProviderMeta.TrackingMessage;
				}
				
			};
		}else {
			logger.error("Missing {} {} configuration",KafkaHMSMeta.ServerConfigKey,KafkaHMSMeta.RootIdConfigKey);
			throw new Error(String.format("Missing {} {} configuration",KafkaHMSMeta.ServerConfigKey,KafkaHMSMeta.RootIdConfigKey));
		}			
	}
	@Override
	public CompletableFuture<Boolean> clear() {
		return CompletableFuture.supplyAsync(()->{
			StreamResponse response = clearStream.startStream(null);			
			return !response.isError() && response.getData()!=null;
		}, this.execContext.getExecutor());
	}

	@Override
	public CompletableFuture<Boolean> initprovider(Provider providerdto) {
		return CompletableFuture.supplyAsync(()->{
			StreamResponse response = initProviderStream.startStream(providerdto);
			return !response.isError() && response.getData()!=null;
		}, this.execContext.getExecutor());
	}

	@Override
	public CompletableFuture<Boolean> tracking(ProviderTracking trackingdto) {
		return CompletableFuture.supplyAsync(()->{
			StreamResponse response = trackingProviderStream.startStream(trackingdto);
			return !response.isError() && response.getData()!=null;
		}, this.execContext.getExecutor());
	}

}
