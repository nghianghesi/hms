package hms.kafka.provider;

import hms.provider.KafkaProviderMeta;

public class KafkaProviderSettings {
	public String getTrackingTopic() {
		return KafkaProviderMeta.TrackingMessage;
	}
	public String getQueryTopic() {
		return KafkaProviderMeta.QueryProvidersMessage;
	}
}
