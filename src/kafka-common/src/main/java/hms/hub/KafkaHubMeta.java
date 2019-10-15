package hms.hub;

public class KafkaHubMeta {
	
	private static final String TopicPrefix = "hms.hub.";
	public static final String MappingHubMessage = TopicPrefix+"mapping-hub";
	public static final String FindCoveringHubsMessage = TopicPrefix+"find-covering-hubs";
	public static final String FindAndSplitCoveringHubsMessage = TopicPrefix+"find-and-split-covering-hubs";
	public static final String GroupConfigKey = "kafka.hub.group";
}
