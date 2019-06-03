package hms.kafka.provider;

import com.google.inject.Inject;

import hms.provider.KafkaProviderMeta;

public class InMemKafkaProviderSettings extends KafkaProviderSettings{
	
	@Inject()
	public InMemKafkaProviderSettings() {}
	public String getTrackingTopic() {
		return KafkaProviderMeta.InMemTrackingMessage;
	}
	public String getQueryTopic() {
		return KafkaProviderMeta.InMemQueryProvidersMessage;
	}
}
