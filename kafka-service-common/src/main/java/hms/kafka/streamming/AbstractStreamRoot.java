package hms.kafka.streamming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;

import hms.KafkaHMSMeta;

public abstract class AbstractStreamRoot<TStart, TRes> 
	extends KafkaStreamNodeBase<TRes, Void>{ // consume TRes & forward to none.
	protected abstract String getStartTopic();

	protected static final int KEY_RANGE = 1000;
	public AbstractStreamRoot() {
		this.timeout=10000;
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
			this.producer.send(record).get(timeout, TimeUnit.MILLISECONDS);
		} catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
			this.handleRequestError(id, "Request error:"+e.getMessage());
		}
		return waiter.getWaiterTask();
	}	
	
	private int intervalIdx=0;
	@Override
	protected void intervalCleanup() { // clean up timeout.
		super.intervalCleanup();
		intervalIdx+=1;
		if(intervalIdx>10) {
			intervalIdx=0;
			for(int keyrange=0;keyrange<this.KEY_RANGE;keyrange++) {
				synchronized (this.getWaiters(keyrange)) {
					do{	
						Map.Entry<UUID, ? extends StreamResponse<? extends TRes>> w = null;
						if(!this.getWaiters(keyrange).isEmpty()) {
							w = this.getWaiters(keyrange).entrySet().iterator().next();
							if(w!=null && w.getValue().isTimeout()) {
								this.getWaiters(keyrange).remove(w.getKey());		
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
