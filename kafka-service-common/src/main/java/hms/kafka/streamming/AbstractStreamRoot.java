package hms.kafka.streamming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import hms.KafkaHMSMeta;

public abstract class AbstractStreamRoot<TStart, TRes> 
	extends KafkaStreamNodeBase<TRes, Void>{ // consume TRes & forward to none.
	protected abstract String getStartTopic();

	protected static final int KEY_RANGE = 1000;
	public AbstractStreamRoot() {
		this.timeout=20000;
	}
	protected String getConsumeTopic() {
		return this.getStartTopic()+KafkaHMSMeta.ReturnTopicSuffix;
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
	
	public CompletableFuture<TRes> startStream(TStart data, int timeout) {
		UUID id = this.nextId();
		StreamResponse<TRes> waiter = this.createReponseInstance(id,timeout);	
		HMSMessage<TStart> request = new HMSMessage<TStart>(id, data);
		request.addReponsePoint(this.getConsumeTopic());			
		try {				
			String startTopic = this.applyTemplateToRepForTopic(this.getStartTopic(), data); 			
			ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(request, startTopic);
			//this.getLogger().info("Start stream {} {}", startTopic, request.getRequestId());
			this.producer.send(record,  (RecordMetadata metadata, Exception exception) -> {
				if(exception!=null) {
					this.getLogger().info("**** Request error..."+exception.getMessage());
					this.handleRequestError(id, "Request error");
				}
			});
		} catch (IOException e) {
			this.handleRequestError(id, "Request error");
		}
		return waiter.getWaiterTask();
	}	
	
	@Override
	protected void intervalCleanup() { // clean up timeout.
		super.intervalCleanup();
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
