package hms.kafka.provider;


import hms.provider.KafkaProviderMeta;

public class KafkaProviderSettings implements KafkaProviderTopics {	

	@Override
	public String getTrackingTopic() {
		return KafkaProviderMeta.TrackingMessage;
	}
	/* (non-Javadoc)
	 * @see hms.kafka.provider.KafkaProviderTopics#getQueryTopic()
	 */
	@Override
	public String getQueryTopic() {
		return KafkaProviderMeta.QueryProvidersMessage;
	}
}
