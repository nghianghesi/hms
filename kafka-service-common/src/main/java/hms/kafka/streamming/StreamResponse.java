package hms.kafka.streamming;

import java.util.concurrent.CompletableFuture;


public class StreamResponse<T>{
	protected CompletableFuture<T> waiterTask=new CompletableFuture<T>();
	protected int timeout;
	protected long start;
	public StreamResponse(int timeout) {
		this.timeout = timeout;
		this.start = System.currentTimeMillis();
	}
	
	public boolean isTimeout() {
		return System.currentTimeMillis() - this.start > this.timeout;
	}
	public CompletableFuture<T> getWaiterTask(){
		return this.waiterTask;
	}
	
	public void setData(T data) {
		this.waiterTask.complete(data);
	}

	
	public void setError(String error) {
		this.waiterTask.completeExceptionally(new Exception(error));
	}	
}
