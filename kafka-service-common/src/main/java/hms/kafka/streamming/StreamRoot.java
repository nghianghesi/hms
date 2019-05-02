package hms.kafka.streamming;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;

import hms.KafkaHMSMeta;

public abstract class StreamRoot<TStart, TRes> 
	extends KafkaStreamNodeBase<TRes, Void>{ // consume & forward to none.
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

	private Map<UUID, StreamResponse<TRes>> _waiters = new Hashtable<UUID, StreamResponse<TRes>>();
	protected Map<UUID, ? extends StreamResponse<TRes>> getWaiters(){
		return _waiters;
	}
	
	private UUID nextId() {
		return UUID.randomUUID();
	} 
	
	public void handleResponse(HMSMessage<TRes> reponse) {
		if(this.getWaiters().containsKey(reponse.getRequestId())) {
			StreamResponse<TRes> waiter = this.getWaiters().remove(reponse.getRequestId()) ;
			waiter.setData(reponse.getData());
		}else {
			this.getLogger().warn("Stream response without waiter " + this.getStartTopic() + " " + reponse.getRequestId());
		}
	}
	
	public void handleRequestError(UUID id, String error) {
		if(this.getWaiters().containsKey(id)) {			
			StreamResponse<TRes> waiter = this.getWaiters().remove(id) ;
			waiter.setError(error);
		}
	}	
	
	public CompletableFuture<TRes> startStream(java.util.function.Function<UUID,HMSMessage<TStart>> createRequest) {
		return this.startStream(createRequest, this.timeout);
	}
	
	
	protected StreamResponse<TRes> createReponseInstance(UUID id) {
		StreamResponse<TRes>  waiter = new StreamResponse<>();
		this._waiters.put(id, waiter);
		return waiter;
	}
	
	public CompletableFuture<TRes> startStream(java.util.function.Function<UUID,HMSMessage<TStart>> createRequest, int timeout) {
		UUID id = this.nextId();
		StreamResponse<TRes> waiter = this.createReponseInstance(id);	
		HMSMessage<TStart> request = createRequest.apply(id);
		if(request != null) {
			request.addReponsePoint(this.getConsumeTopic());			
			try {				
				ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(request, this.getStartTopic());
				this.getLogger().info("Start stream {} {}", this.getStartTopic(), request.getRequestId());
				this.producer.send(record).get(timeout, TimeUnit.MILLISECONDS);
			} catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
				this.handleRequestError(id, "Request error:"+e.getMessage());
			}
		}else {
			this.handleRequestError(id, "Empty Request");
		}
		return hms.common.ServiceWaiter.getInstance().waitForSignal(waiter,timeout);
	}	
}
