package hms.kafka.streamming;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.slf4j.Logger;

public abstract class AbstractStreamRoot<TStart, TRes>{ 
	protected static final int KEY_RANGE = 1000;
	private int timeout = 20000;
	private int cleanupInterval = 5000;	
	
	protected abstract Logger getLogger() ;
	protected abstract String getStartTopic();
	protected abstract String getReturnTopic();
	protected abstract Class<? extends TRes> getTConsumeManifest();
	protected abstract String getGroupid();
	protected abstract Executor getPollingService();
	protected abstract Executor getExecutorService();
	
	protected Map<String, KafkaStreamRootNode> rootNodes = new HashMap<String, KafkaStreamRootNode>();
	public abstract class KafkaStreamRootNode extends KafkaStreamNodeBase<TRes, Void>{ // consume TRes & forward to none.
		
		protected String getStartTopic() {
			return AbstractStreamRoot.this.getStartTopic();
		}
	
		public KafkaStreamRootNode() {
			this.timeout = AbstractStreamRoot.this.timeout;
		}
		
		protected String getConsumeTopic() {
			return AbstractStreamRoot.this.getReturnTopic();
		}
		
		protected String getForwardTopic() {
			return null;
		}
		
		@Override
		protected void ensureTopics() {
			super.ensureTopics();
			this.ensureTopic(this.getStartTopic());				
		}	
		
		protected Void processRequest(HMSMessage<TRes> response) {
			handleResponse(response);
			return null;
		}

		@Override
		protected Logger getLogger() {
			return AbstractStreamRoot.this.getLogger();
		}

		@Override
		protected Class<? extends TRes> getTConsumeManifest() {
			return AbstractStreamRoot.this.getTConsumeManifest();
		}

		@Override
		protected String getGroupid() {
			return AbstractStreamRoot.this.getGroupid();
		}

		@Override
		protected Executor getExecutorService() {
			return AbstractStreamRoot.this.getExecutorService();
		}

		@Override
		protected Executor getPollingService() {
			return AbstractStreamRoot.this.getPollingService();
		}		
		
		@Override
		protected void intervalCleanup() {
			super.intervalCleanup();
			AbstractStreamRoot.this.intervalCleanup();
		}
		
		@Override
		protected String applyTemplateToRepForTopic(String topic, Object value) {
			return AbstractStreamRoot.this.applyTemplateToRepForTopic(topic, value);
		}	
	}
	
	public void addZones(String zone, String kafkaServer) {
		if(!this.rootNodes.containsKey(zone)) {
			KafkaStreamRootNode existingServer = null; 
			for(Map.Entry<String,KafkaStreamRootNode>z:this.rootNodes.entrySet()) {
				if(z.getValue().getServer().equals(kafkaServer)) {
					existingServer = z.getValue();
					break;
				}
			};
			if(existingServer == null) {
				this.rootNodes.put(zone, new KafkaStreamRootNode() {			
					@Override
					protected String getServer() {
						return kafkaServer;
					}
				});
			}else {
				this.rootNodes.put(zone, existingServer);
			}
		}
	}	

	protected String applyTemplateToRepForTopic(String topic, Object value) {
		return topic;		
	}
	
	public void run() {
		this.rootNodes.forEach((key,value) -> {value.run();});
	}
	
	public void shutDown() {
		this.rootNodes.forEach((key,value) -> {value.shutDown();});
	}
	
	protected abstract ArrayList<? extends LinkedHashMap<UUID,? extends StreamResponse<? extends TRes>>> getAllWaiters();
	protected abstract StreamResponse<? extends TRes> removeWaiter(int keyrange, UUID id);
	protected final LinkedHashMap<UUID,? extends  StreamResponse<? extends TRes>> getWaiters(int keyrange){
		return this.getAllWaiters().get(keyrange);
	}
	
	protected UUID nextId() {
		return UUID.randomUUID();
	} 
	
	protected int RequestIdToKeyRange(UUID id) {
		return Math.abs(id.hashCode()) % KEY_RANGE;
	}
	
	public abstract void handleResponse(HMSMessage<TRes> response); 
	
	public void handleRequestError(UUID id, String error) {
		StreamResponse<? extends TRes> waiter = null;
		int keyrange = RequestIdToKeyRange(id);
		synchronized (this.getWaiters(keyrange)) {
			if(this.getWaiters(keyrange).containsKey(id)) {			
				waiter = this.removeWaiter(keyrange, id) ;				
			}
		}
		if(waiter!=null) {
			waiter.setError(error);
			this.getLogger().info("************ request error ***********");
		}
	}	
	
	public CompletableFuture<TRes> startStream(TStart data) {
		return this.startStream(data, this.timeout);
	}	
	
	protected abstract StreamResponse<TRes> createReponseInstance(UUID id, int timeout) ;
	
	public abstract CompletableFuture<TRes> startStream(TStart data, int timeout) ;
	
	private long previousClean = System.currentTimeMillis();
	protected void intervalCleanup() { // clean up timeout.
		if(System.currentTimeMillis()-previousClean > cleanupInterval) {
			previousClean = System.currentTimeMillis();
			for(int keyrange=0;keyrange<KEY_RANGE;keyrange++) {
				synchronized (this.getWaiters(keyrange)) {
					do{	
						Map.Entry<UUID, ? extends StreamResponse<? extends TRes>> w = null;
						if(!this.getWaiters(keyrange).isEmpty()) {
							w = this.getWaiters(keyrange).entrySet().iterator().next();
							if(w!=null && w.getValue().isTimeout()) {
								this.getWaiters(keyrange).remove(w.getKey());	
								this.getLogger().info("************ time out ***********");
								w.getValue().setError("Time out");
							}else {
								break;
							}
						}else {
							break;
						}
					}while(true);
				}
			}
		}
	}
}
