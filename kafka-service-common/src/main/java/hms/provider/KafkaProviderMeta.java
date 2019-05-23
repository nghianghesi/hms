package hms.provider;

public class KafkaProviderMeta {

	private static final String TopicPrefix = "hms.provider.";
	public static final String TrackingMessage  = TopicPrefix+"tracking";
	public static final String TrackingWithHubMessage  = TopicPrefix+"tracking-with-hub";
	public static final String QueryProvidersMessage  = TopicPrefix+"query";
	public static final String QueryProvidersWithHubsMessage  = TopicPrefix+"query-with-hub";	

	public static final String InMemTrackingMessage  = TopicPrefix+"inmem-tracking";
	public static final String InMemTrackingWithHubMessage  = TopicPrefix+"inmem-tracking-with-hub";
	public static final String InMemQueryProvidersMessage  = TopicPrefix+"inmem-query";
	public static final String InMemQueryProvidersWithHubsMessage  = TopicPrefix+"inmem-query-with-hub";
	
	public static final String ProviderGroupConfigKey = "kafka.provider.group";
	public static final String ProviderInmemHubIdConfigKey = "kafka.provider.inmem-hubid";
}
