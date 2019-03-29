package hms.kafka.provider;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import com.typesafe.config.Config;

import hms.kafka.streamming.StreamResponse;
import hms.KafkaHMSMeta;
import hms.common.IHMSExecutorContext;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.StreamRoot;
import hms.provider.IProviderService;
import hms.provider.KafkaProviderMeta;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaProviderService implements IProviderService{
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderService.class);

	StreamRoot<Void, Boolean>  clearStream; 
	StreamRoot<hms.dto.Provider, Boolean>  initProviderStream;
	StreamRoot<hms.dto.ProviderTracking, Boolean>  trackingProviderStream;
	private IHMSExecutorContext execContext;

	@Inject
	public KafkaProviderService(IHMSExecutorContext ec, Config config) {	
		this.execContext = ec;
		String server, rootid;
		if(config.hasPath(KafkaHMSMeta.ServerConfigKey)
				&& config.hasPath(KafkaHMSMeta.RootIdConfigKey)) {
			server = config.getString(KafkaHMSMeta.ServerConfigKey);
			rootid = config.getString(KafkaHMSMeta.RootIdConfigKey);
			clearStream = new StreamRoot<Void, Boolean>(Boolean.class, logger, rootid, server, KafkaProviderMeta.ClearMessage);
			initProviderStream = new StreamRoot<hms.dto.Provider, Boolean>(Boolean.class, logger, rootid, server, KafkaProviderMeta.InitproviderMessage);						
			trackingProviderStream = new StreamRoot<hms.dto.ProviderTracking, Boolean>(Boolean.class, logger, rootid, server, KafkaProviderMeta.TrackingMessage);						
		}else {
			logger.error("Missing {} {} configuration",KafkaHMSMeta.ServerConfigKey,KafkaHMSMeta.RootIdConfigKey);
			throw new Error(String.format("Missing {} {} configuration",KafkaHMSMeta.ServerConfigKey,KafkaHMSMeta.RootIdConfigKey));
		}		
	}
	
	@Override
	public CompletableFuture<Boolean> clear() {
		return CompletableFuture.supplyAsync(()->{
			StreamResponse response = clearStream.startStream((requestid)->{
				return new HMSMessage<Void>(requestid, KafkaProviderMeta.ClearMessage);
			});			
			return !response.isError() && response.getData()!=null;
		}, this.execContext.getExecutor());
	}

	@Override
	public CompletableFuture<Boolean> initprovider(hms.dto.Provider providerdto) {
		return CompletableFuture.supplyAsync(()->{
			StreamResponse response = initProviderStream.startStream((requestid)->{
				return new HMSMessage<hms.dto.Provider>(requestid, KafkaProviderMeta.InitproviderMessage, providerdto);
 			});
			return !response.isError() && response.getData()!=null;
		}, this.execContext.getExecutor());
	}

	@Override
	public CompletableFuture<Boolean> tracking(hms.dto.ProviderTracking trackingdto) {
		return CompletableFuture.supplyAsync(()->{
			StreamResponse response = trackingProviderStream.startStream((requestid)->{		
				return new HMSMessage<hms.dto.ProviderTracking>(requestid, KafkaProviderMeta.TrackingMessage, trackingdto);
			});
			return !response.isError() && response.getData()!=null;
		}, this.execContext.getExecutor());
	}		
	
}

