package hms.provider;
/*
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import hms.hub.IHubService;
import hms.kafka.provider.KafkaHubProviderService;
import hms.kafka.provider.KafkaProviderTopics;
import play.inject.ApplicationLifecycle;

public class InMemProviderServiceFactory extends hms.commons.KafkaProcessingProvider<KafkaHubProviderService>{

	@Inject()
	public InMemProviderServiceFactory(ApplicationLifecycle app, Injector injector) {
		super(app, injector);
	}

	@Override
	protected KafkaHubProviderService internalGet() {
		return new KafkaHubProviderService(
				this.injector.getInstance(Config.class),
				this.injector.getInstance(hms.common.IHMSExecutorContext.class),
				this.injector.getInstance(KafkaProviderTopics.class),
				this.injector.getInstance(IHubService.class));
	}
}*/