package hms.kafka.provider;

import java.io.IOException;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.typesafe.config.Config;

import hms.kafka.KafkaConsumerBase;
import hms.kafka.messaging.KafkaMessageUtils;
import hms.provider.IProviderService;

public class KafkaProviderConsumer extends KafkaConsumerBase{
	private static final Logger logger = LoggerFactory.getLogger(KafkaProviderConsumer.class);
	private IProviderService productservice;
	@Inject
	public KafkaProviderConsumer(Config config, KafkaMessageUtils messageManager, IProviderService productservice) {
		super(logger,config,messageManager);
		this.productservice = productservice;
	}

	@Override
	protected void loadConfig(Config config) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Runnable processRequest(String key, long id, byte[] data) {
		return ()->{
			switch (key){
				case IProviderService.ClearMessage :
					this.productservice.clear();
					break;
				case IProviderService.InitproviderMessage :
				hms.dto.Provider providerdto;
					try {
						providerdto = (hms.dto.Provider) this.messageManager.convertByteArrayToObject(hms.dto.Provider.class, data);
						this.productservice.initprovider(providerdto);
					} catch (IOException e) {
						logger.error("Request error");
					}
					break;						
				case IProviderService.TrackingMessage :
						hms.dto.ProviderTracking trackingdto;
						try {
							trackingdto = (hms.dto.ProviderTracking) this.messageManager.convertByteArrayToObject(hms.dto.ProviderTracking.class, data);					
							this.productservice.tracking(trackingdto);
						} catch (IOException e) {
							logger.error("Request error");
						}
						break;				
			}
		};
	}
}
