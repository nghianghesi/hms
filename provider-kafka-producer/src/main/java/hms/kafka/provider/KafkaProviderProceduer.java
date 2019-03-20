package hms.kafka.provider;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.inject.Inject;
import com.typesafe.config.Config;

import hms.dto.Provider;
import hms.dto.ProviderTracking;
import hms.kafka.streamming.KafkaMessageUtils;
import hms.kafka.streamming.KafkaNodeBase;
import hms.kafka.streamming.MessageBasedReponse;
import hms.kafka.streamming.MessageBasedRequest;
import hms.kafka.streamming.RootStreamManager;
import hms.provider.IProviderService;


import org.apache.kafka.clients.producer.ProducerRecord;
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
	
	@Override
	public CompletableFuture<Boolean> clear() {
		return CompletableFuture.supplyAsync(()->{
			MessageBasedReponse response = this.startStream((requestid)->{
				ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(
						this.requestTopic, IProviderService.ClearMessage, new byte[] {});
				this.setCommonInfo(record, requestid);
				return this.producer.send(record);
			}, this.timeout);			
			return !response.isError();
		});
	}

	@Override
	public CompletableFuture<Boolean> initprovider(Provider providerdto) {
		return CompletableFuture.supplyAsync(()->{
			MessageBasedReponse response = this.startStream((requestid)->{		
				ProducerRecord<String, byte[]> record = new ProducerRecord<String, byte[]>(
						this.requestTopic, IProviderService.InitproviderMessage, new byte[] {});
				this.setCommonInfo(record, requestid);
				return this.producer.send(record);
			}, this.timeout);			
			return !response.isError();
		});
	}

	@Override
	public CompletableFuture<Boolean> tracking(ProviderTracking trackingdto) {
		return CompletableFuture.supplyAsync(()->{
			MessageBasedReponse response = this.startStream((requestid)->{		
				byte[] requestBody = this.toRequestBody(trackingdto);
				if(requestBody != null) {
					try {					
						MessageBasedRequest<ProviderTracking> request = new MessageBasedRequest<ProviderTracking>();
						request.setData(trackingdto);
						ProducerRecord<String, byte[]> record;
	
							record = KafkaMessageUtils.getProcedureRecord(request, this.requestTopic, IProviderService.TrackingMessage);
						this.setCommonInfo(record, requestid);
						return this.producer.send(record);
					} catch (IOException e) {
						logger.error("Create request error", e.getMessage());
						return null;
					}					
				}else {
					return null;
				}
			}, this.timeout);
			return !response.isError();
		});
	}

	
	
}
