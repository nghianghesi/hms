package hms.kafka.streamming;

import java.io.IOException;
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

	protected abstract LinkedHashMap<UUID, ? extends StreamResponse<TRes>> getWaiters();
	
	protected UUID nextId() {
		return UUID.randomUUID();
	} 
	
	public void handleResponse(HMSMessage<? extends TRes> response) {
		if(this.getWaiters().containsKey(response.getRequestId())) {
			StreamResponse<TRes> waiter = this.getWaiters().remove(response.getRequestId()) ;
			waiter.setData(response.getData());
		}else {
			this.getLogger().warn("Stream response without waiter " + this.getStartTopic() + " " + response.getRequestId());
		}
	}
	
	public void handleRequestError(UUID id, String error) {
		if(this.getWaiters().containsKey(id)) {			
			StreamResponse<TRes> waiter = this.getWaiters().remove(id) ;
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
	
	@Override
	protected void intervalCleanup() {		
		LinkedHashMap<UUID, ? extends StreamResponse<TRes>> waiters = this.getWaiters();
		do {
			Map.Entry<UUID, ? extends StreamResponse<TRes>> w = waiters.isEmpty() ? null : waiters.entrySet().iterator().next();		
			if(w!=null && w.getValue().isTimeout()) {
				w.getValue().setError("Time out");
				this.getWaiters().remove(w.getKey());
			}else {
				break;
			}
		}while (true);
	}
}
