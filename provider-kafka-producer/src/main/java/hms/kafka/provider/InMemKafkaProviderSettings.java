package hms.kafka.provider;

import hms.provider.KafkaProviderMeta;

public class InMemKafkaProviderSettings extends KafkaProviderSettings{

	public String getTrackingTopic() {
		return KafkaProviderMeta.InMemTrackingMessage;
	}
	public String getQueryTopic() {
		return KafkaProviderMeta.InMemQueryProvidersMessage;
	}
}
