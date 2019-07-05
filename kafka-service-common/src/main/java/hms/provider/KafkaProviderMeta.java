package hms.provider;

public class KafkaProviderMeta {

	private static final String TopicPrefix = "hms_provider_";
	public static final String TrackingMessage  = TopicPrefix+"tracking";
	public static final String TrackingWithHubMessage  = TopicPrefix+"tracking_with_hub";
	public static final String QueryProvidersMessage  = TopicPrefix+"query";
	public static final String QueryProvidersWithHubsMessage  = TopicPrefix+"query_with_hub";	

	public static final String InMemTrackingMessage  = TopicPrefix+"inmem_tracking";
	public static final String InMemTrackingWithHubMessage  = TopicPrefix+"inmem_tracking_with_hub";
	public static final String InMemQueryProvidersMessage  = TopicPrefix+"inmem_query";
	public static final String InMemQueryProvidersWithHubsMessage  = TopicPrefix+"inmem_query_with_hub";
	
	public static final String ProviderGroupConfigKey = "kafka.provider.group";
	public static final String ProviderInmemHubIdConfigKey = "kafka.provider.inmem-hubid";
	public static final String NumOfPollingThreads = "kafka.num-of-polling-threads";
}
