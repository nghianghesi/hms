package hms.ksql.provider;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;

import hms.KafkaHMSMeta;
import hms.kafka.provider.KafkaProviderProcessing;
import hms.provider.IProviderServiceProcessor;
import hms.provider.KafkaProviderMeta;

public class KSQLProviderProcessing {	
	private static final Logger logger = LoggerFactory.getLogger(KSQLProviderProcessing.class);

	private IProviderServiceProcessor providerService;
	private String kafkaserver;
	private String providerGroup;
	@Inject
	public KSQLProviderProcessing(Config config, IProviderServiceProcessor providerService) {
		this.providerService = providerService;	

		if(config.hasPath(KafkaHMSMeta.ServerConfigKey)) {
			this.kafkaserver = config.getString(KafkaHMSMeta.ServerConfigKey);
		}else {
			logger.error("Missing {} configuration",KafkaHMSMeta.ServerConfigKey);
			throw new Error(String.format("Missing {} configuration",KafkaHMSMeta.ServerConfigKey));
		}
		
		this.providerGroup = "hms.provider";
		if(config.hasPath(KafkaProviderMeta.ProviderGroupConfigKey)) {
			this.providerGroup = config.getString(KafkaProviderMeta.ProviderGroupConfigKey);
		}
		
		this.buildClearProcessor();
		this.buildInitProviderProcessor();
		this.buildTrackingProviderProcessor();
		this.buildTrackingProviderHubProcessor();	

		logger.info("Provider processing is ready");
	}
	
	
	
}
