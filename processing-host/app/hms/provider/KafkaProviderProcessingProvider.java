package hms.provider;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.typesafe.config.Config;

import hms.kafka.provider.KafkaProviderProcessing;
import play.inject.ApplicationLifecycle;

public class KafkaProviderProcessingProvider extends hms.commons.KafkaProcessingProvider<KafkaProviderProcessing> {

	@Inject
	public KafkaProviderProcessingProvider(ApplicationLifecycle app, Injector injector) {
		super(app, injector);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected KafkaProviderProcessing internalGet() {
		return new KafkaProviderProcessing(
				this.injector.getInstance(Config.class),
				this.injector.getInstance(IProviderServiceProcessor.class),
				this.injector.getInstance(hms.common.IHMSExecutorContext));
	}

}
