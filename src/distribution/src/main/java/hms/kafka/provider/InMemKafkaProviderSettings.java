package hms.kafka.provider;

import hms.provider.KafkaProviderMeta;

public class InMemKafkaProviderSettings implements KafkaProviderTopics{

	public String getTrackingTopic() {
		return KafkaProviderMeta.InMemTrackingMessage;
	}
	public String getQueryTopic() {
		return KafkaProviderMeta.InMemQueryProvidersMessage;
	}
}
