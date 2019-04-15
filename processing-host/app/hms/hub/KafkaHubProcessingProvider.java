package hms.hub;

import com.google.inject.Injector;
import com.typesafe.config.Config;

import hms.kafka.hub.KafkaHubProcessing;
import play.inject.ApplicationLifecycle;

public class KafkaHubProcessingProvider extends hms.KafkaProcessingProvider<KafkaHubProcessing>{

	public KafkaHubProcessingProvider(ApplicationLifecycle app, Injector injector) {
		super(app, injector);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected KafkaHubProcessing internalGet() {
		return new KafkaHubProcessing(
				this.injector.getInstance(Config.class),
				this.injector.getInstance(IHubServiceProcessor.class));
	}

}
