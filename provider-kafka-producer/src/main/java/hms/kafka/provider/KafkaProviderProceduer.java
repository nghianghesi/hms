package hms.kafka.provider;

import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import com.typesafe.config.Config;

import hms.common.messaging.MessageBasedReponse;
import hms.common.messaging.MessageBasedServiceManager;
import hms.dto.Provider;
import hms.dto.ProviderTracking;
import hms.kafka.KafkaProducerBase;
import hms.provider.IProviderService;


import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KafkaProviderProceduer extends KafkaProducerBase implements hms.provider.IProviderService{
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderProceduer.class);

	@Override
	protected void loadConfig(Config config) {
		if(config.hasPath("kafka.provider.groupid")) {
			this.groupid = config.getString("hms.provider.responselistener");
		}else {
			this.groupid = "hms.provider.responselistener";
		}
		
		if(config.hasPath("kafka.provider.topic")) {
			this.requestTopic = config.getString("kafka.provider.topic");
		}else {
			this.requestTopic = "hms.provider";
		}
		this.returnTopic= "return." + this.requestTopic;
		
		if(config.hasPath("kafka.provider.timeout")) {
			this.timeout = (short)config.getInt("kafka.provider.timeout");
		}	
		
		if(config.hasPath("kafka.provider.topic")) {
			this.requestTopic = config.getString("kafka.provider.topic");
		}else {
			this.requestTopic = "hms.provider";
		}		
	}
	
	@Inject
	public KafkaProviderProceduer(Config config, MessageBasedServiceManager messageManager) {
		super(KafkaProviderProceduer.logger, config, messageManager);
	}
	
	@Override
	public CompletableFuture<Boolean> clear() {
		return CompletableFuture.supplyAsync(()->{
			MessageBasedReponse response = this.messageManager.request((request)->{
				ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(
						this.requestTopic, IProviderService.ClearMessage, new byte[] {});
				this.setCommonInfo(record, request.getRequestId());
				return this.producer.send(record);
			}, this.timeout);			
			return !response.isError();
		});
	}

	@Override
	public CompletableFuture<Boolean> initprovider(Provider providerdto) {
		return CompletableFuture.supplyAsync(()->{
			MessageBasedReponse response = this.messageManager.request((request)->{		
				ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(
						this.requestTopic, IProviderService.InitproviderMessage, new byte[] {});
				this.setCommonInfo(record, request.getRequestId());
				return this.producer.send(record);
			}, this.timeout);			
			return !response.isError();
		});
	}

	@Override
	public CompletableFuture<Boolean> tracking(ProviderTracking trackingdto) {
		return CompletableFuture.supplyAsync(()->{
			MessageBasedReponse response = this.messageManager.request((request)->{		
				byte[] requestBody = this.toRequestBody(trackingdto);
				if(requestBody != null) {
					ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(
							this.requestTopic, IProviderService.TrackingMessage, requestBody);
					this.setCommonInfo(record, request.getRequestId());
					return this.producer.send(record);
				}else {
					return null;
				}
			}, this.timeout);			
			return !response.isError();
		});
	}

	
	
}
