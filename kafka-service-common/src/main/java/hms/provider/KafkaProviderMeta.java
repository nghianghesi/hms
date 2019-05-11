package hms.provider;

public class KafkaProviderMeta {

	private static final String TopicPrefix = "hms.provider.";
	public static  final String ClearMessage = TopicPrefix+"clear";
	public static final String InitproviderMessage  = TopicPrefix+"initprovider";
	public static final String TrackingMessage  = TopicPrefix+"tracking";
	public static final String TrackingWithHubMessage  = TopicPrefix+"tracking-with-hub";
	public static final String QueryProvidersMessage  = TopicPrefix+"query";
	public static final String QueryProvidersWithHubsMessage  = TopicPrefix+"query-with-hub";
	
	public static final String ProviderGroupConfigKey = "kafka.provider.group";
	public static final String ProviderInmemHubIdConfigKey = "kafka.provider.inmem-hubid";
}
