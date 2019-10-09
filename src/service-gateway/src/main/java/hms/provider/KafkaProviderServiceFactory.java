package hms.provider;

/*
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import hms.kafka.provider.KafkaProviderService;
import hms.kafka.provider.KafkaProviderTopics;
import play.inject.ApplicationLifecycle;

public class KafkaProviderServiceFactory extends hms.commons.KafkaProcessingProvider<KafkaProviderService>{

	@Inject()
	public KafkaProviderServiceFactory(ApplicationLifecycle app, Injector injector) {
		super(app, injector);
	}

	@Override
	protected KafkaProviderService internalGet() {
		return new KafkaProviderService(
				this.injector.getInstance(Config.class),
				this.injector.getInstance(hms.common.IHMSExecutorContext.class),
				this.injector.getInstance(KafkaProviderTopics.class));
	}
}*/