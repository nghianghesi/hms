package hms.kafka.streamming;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;

import hms.KafkaHMSMeta;
import hms.StreamResponse;

public abstract class StreamRoot<TStart, TRes> 
	extends KafkaStreamNodeBase<TRes,Void>{ // consume & forward to none.
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
	
	public StreamRoot() {
		super(1);
	}
	
	protected Void processRequest(HMSMessage<TRes> response) {
		handleResponse(response);
		return null;
	}		

	private Map<UUID, StreamResponse> waiters = new Hashtable<UUID, StreamResponse>();
	private synchronized UUID nextId() {
		return UUID.randomUUID();
	} 
	
	public void handleResponse(HMSMessage<TRes> reponse) {
		if(waiters.containsKey(reponse.getRequestId())) {
			StreamResponse waiter = waiters.remove(reponse.getRequestId()) ;
			waiter.setData(reponse);
			this.getLogger().info("Notify waiter " + this.getStartTopic() + " " + reponse.getRequestId());
			synchronized(waiter) {
				waiter.notifyAll();
			}
		}else {
			this.getLogger().warn("Stream response without waiter " + this.getStartTopic() + " " + reponse.getRequestId());
		}
	}
	
	public void handleRequestError(UUID id, String error) {
		if(waiters.containsKey(id)) {			
			StreamResponse waiter = waiters.remove(id) ;
			waiter.setError(error);
			synchronized(waiter) {
				waiter.notifyAll();
			}
		}
	}	
	
	public StreamResponse startStream(java.util.function.Function<UUID,HMSMessage<TStart>> createRequest) {
		return this.startStream(createRequest, this.timeout);
	}
	
	public StreamResponse startStream(java.util.function.Function<UUID,HMSMessage<TStart>> createRequest, int timeout) {
		UUID id = this.nextId();
		StreamResponse waiter = new StreamResponse(id);
		this.waiters.put(id, waiter);
		HMSMessage<TStart> request = createRequest.apply(id);
		if(request != null) {
			request.addReponsePoint(this.getConsumeTopic());
			try {
				ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(request, this.getStartTopic());
				this.getLogger().info("Start stream "+this.getStartTopic() + " " + request.getRequestId());
				this.producer.send(record).get(timeout, TimeUnit.MILLISECONDS);
			} catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
				this.handleRequestError(id, "Request error: "+e.getMessage());
				return waiter;
			}
			
			try {
				if(waiter.needWaiting()) {
					synchronized(waiter) {
						waiter.wait(timeout);				
						this.getLogger().info("Get stream response "+this.getStartTopic() + " " + request.getRequestId());
					}
				}
			} catch (InterruptedException e) {
				this.handleRequestError(id, "Request error: "+e.getMessage());
				return waiter;
			}					
		}else {
			waiter.setError("Empty request");
		}
		return waiter;
	}	
}
