package hms.kafka.provider;

import com.google.inject.Inject;

import hms.provider.KafkaProviderMeta;

public class KafkaProviderSettings {	
	@Inject()
	public KafkaProviderSettings() {}
	public String getTrackingTopic() {
		return KafkaProviderMeta.TrackingMessage;
	}
	public String getQueryTopic() {
		return KafkaProviderMeta.QueryProvidersMessage;
	}
}
