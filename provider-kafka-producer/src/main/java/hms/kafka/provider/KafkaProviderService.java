package hms.kafka.provider;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import com.typesafe.config.Config;

import hms.kafka.streamming.StreamReponse;
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
	
	@Inject
	public KafkaProviderService(Config config) {			
		String server;
		if(config.hasPath("kafka.server")) {
			server = config.getString("kafka.server");
			clearStream = new StreamRoot<Void, Boolean>(Boolean.class, logger, server, KafkaProviderMeta.ClearMessage);
			initProviderStream = new StreamRoot<hms.dto.Provider, Boolean>(Boolean.class, logger, server, KafkaProviderMeta.InitproviderMessage);						
			trackingProviderStream = new StreamRoot<hms.dto.ProviderTracking, Boolean>(Boolean.class, logger, server, KafkaProviderMeta.TrackingMessage);						
		}else {
			logger.error("Missing kafka.server configuration");
			throw new Error("Invalid configuration");
		}		
	}
	
	@Override
	public CompletableFuture<Boolean> clear() {
		return CompletableFuture.supplyAsync(()->{
			StreamReponse response = clearStream.startStream((requestid)->{
				return new HMSMessage<Void>(requestid, KafkaProviderMeta.ClearMessage);
			});			
			return !response.isError();
		});
	}

	@Override
	public CompletableFuture<Boolean> initprovider(hms.dto.Provider providerdto) {
		return CompletableFuture.supplyAsync(()->{
			StreamReponse response = initProviderStream.startStream((requestid)->{
				return new HMSMessage<hms.dto.Provider>(requestid, KafkaProviderMeta.InitproviderMessage, providerdto);
 			});
			return !response.isError();
		});
	}

	@Override
	public CompletableFuture<Boolean> tracking(hms.dto.ProviderTracking trackingdto) {
		return CompletableFuture.supplyAsync(()->{
			StreamReponse response = trackingProviderStream.startStream((requestid)->{		
				return new HMSMessage<hms.dto.ProviderTracking>(requestid, KafkaProviderMeta.TrackingMessage, trackingdto);
			});
			return !response.isError();
		});
	}		
	
}

