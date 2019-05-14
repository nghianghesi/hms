package hms.kafka.streamming;

import java.util.concurrent.CompletableFuture;

public interface PollChainning {
	CompletableFuture<Void> hookPolling(CompletableFuture<Void> parent);
}
