package hms.kafka.provider;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import com.typesafe.config.Config;

import hms.dto.Provider;
import hms.dto.ProviderTracking;
import hms.kafka.streamming.KafkaMessageUtils;
import hms.kafka.streamming.StreamReponse;
import hms.kafka.streamming.HMSMessage;
import hms.kafka.streamming.RootStreamManager;
import hms.provider.IProviderService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaProviderProceduer extends RootStreamManager implements hms.provider.IProviderService{
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderProceduer.class);

	@Inject
	public KafkaProviderProceduer(Config config) {
		super(KafkaProviderProceduer.logger, config);
	}
	
	@Override
	protected void loadConfig(Config config) {
		if(config.hasPath("kafka.provider.groupid")) {
			this.groupid = config.getString("kafka.provider.groupid");
		}else {
			this.groupid = "hms.provider.responselistener";
		}
		
		if(config.hasPath("kafka.provider.topic")) {
			this.requestTopic = config.getString("kafka.provider.topic");
		}else {
			this.requestTopic = "hms.provider";
		}
		this.consumeTopic= "return." + this.requestTopic;
		
		if(config.hasPath("kafka.provider.timeout")) {
			this.timeout = config.getInt("kafka.provider.timeout");
		}	
		
		if(config.hasPath("kafka.provider.topic")) {
			this.requestTopic = config.getString("kafka.provider.topic");
		}else {
			this.requestTopic = "hms.provider";
		}		
	}	
	
	@Override
	public CompletableFuture<Boolean> clear() {
		return CompletableFuture.supplyAsync(()->{
			StreamReponse response = this.callStream((requestid)->{
				return new HMSMessage<Void>(requestid, IProviderService.ClearMessage);
			});			
			return !response.isError();
		});
	}

	@Override
	public CompletableFuture<Boolean> initprovider(Provider providerdto) {
		return CompletableFuture.supplyAsync(()->{
			StreamReponse response = this.callStream((requestid)->{
				return new HMSMessage<Provider>(requestid, IProviderService.InitproviderMessage, providerdto);
 			});			
			return !response.isError();
		});
	}

	@Override
	public CompletableFuture<Boolean> tracking(ProviderTracking trackingdto) {
		return CompletableFuture.supplyAsync(()->{
			StreamReponse response = this.callStream((requestid)->{		
				return new HMSMessage<ProviderTracking>(requestid, IProviderService.TrackingMessage, trackingdto);
			});
			return !response.isError();
		});
	}
	
	@Override
	protected void processRequest(ConsumerRecord<String, byte[]> record) {
		switch (record.key()) {
			case IProviderService.ClearMessage:
				case IProviderService.InitproviderMessage:
				case IProviderService.TrackingMessage:
			try {
				HMSMessage<Boolean> result = KafkaMessageUtils.getHMSMessage(Boolean.class, record);
				this.handleResponse(result);
			} catch (IOException e) {
				logger.error(IProviderService.ClearMessage, e.getMessage());
				this.handleRequestError(KafkaMessageUtils.getRequestId(record), "Response error");
			}
			break;
		}
	}

	
	
}
