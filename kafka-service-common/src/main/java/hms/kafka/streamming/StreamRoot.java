package hms.kafka.streamming;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;

public class StreamRoot<TReq,TRes> extends KafkaStreamNodeBase<TRes,Void>{
	private String requestTopic;
	
	public StreamRoot(Class<TRes> manifiestTRes,  Logger logger, String rootid, String kafkaServer, String topic) {
		super(logger, manifiestTRes, kafkaServer, rootid, topic+".return");
		this.requestTopic = topic;
	}
	
	protected void processRequest(HMSMessage<TRes> response) {
		handleResponse(response);
	}		

	private Map<UUID, StreamResponse> waiters = new Hashtable<UUID, StreamResponse>();
	private synchronized UUID nextId() {
		return UUID.randomUUID();
	} 
	
	public void handleResponse(HMSMessage<TRes> reponse) {
		if(waiters.containsKey(reponse.getRequestId())) {
			StreamResponse waiter = waiters.remove(reponse.getRequestId()) ;
			waiter.setData(reponse);
			this.logger.info("Notify waiter " + this.requestTopic + " " + reponse.getRequestId());
			synchronized(waiter) {
				waiter.notifyAll();
			}
		}else {
			this.logger.warn("Stream response without waiter " + this.requestTopic + " " + reponse.getRequestId());
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
	
	public StreamResponse startStream(java.util.function.Function<UUID,HMSMessage<TReq>> createRequest) {
		return this.startStream(createRequest, this.timeout);
	}
	
	public StreamResponse startStream(java.util.function.Function<UUID,HMSMessage<TReq>> createRequest, int timeout) {
		UUID id = this.nextId();
		StreamResponse waiter = new StreamResponse(id);
		this.waiters.put(id, waiter);
		HMSMessage<TReq> request = createRequest.apply(id);
		if(request != null) {
			request.addReponsePoint(this.consumeTopic);
			try {
				ProducerRecord<String, byte[]> record = KafkaMessageUtils.getProcedureRecord(request, this.requestTopic);
				this.logger.info("Start stream "+this.requestTopic + " " + request.getRequestId());
				this.producer.send(record).get(timeout, TimeUnit.MILLISECONDS);
			} catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
				this.handleRequestError(id, "Request error: "+e.getMessage());
				return waiter;
			}
			
			try {
				if(waiter.needWaiting()) {
					synchronized(waiter) {
						waiter.wait(timeout);				
						this.logger.info("Get stream response "+this.requestTopic + " " + request.getRequestId());
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
