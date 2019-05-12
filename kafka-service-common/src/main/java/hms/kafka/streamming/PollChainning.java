package hms.kafka.streamming;

import java.util.concurrent.CompletableFuture;

public interface PollChainning {
	void hookPolling(CompletableFuture<Void> parent);
}
