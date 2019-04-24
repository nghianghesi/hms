package hms.provider;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import hms.kafka.provider.KafkaProviderService;
import play.inject.ApplicationLifecycle;

public class KafkaProducerServiceProvider extends hms.commons.KafkaProcessingProvider<KafkaProviderService>{

	@Inject()
	public KafkaProducerServiceProvider(ApplicationLifecycle app, Injector injector) {
		super(app, injector);
	}

	@Override
	protected KafkaProviderService internalGet() {
		return new KafkaProviderService(this.injector.getInstance(Config.class)
		);
	}
}
