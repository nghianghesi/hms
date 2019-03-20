package hms.kafka.streamming;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;

import com.typesafe.config.Config;

public abstract class RootStreamManager extends KafkaNodeBase{
	protected RootStreamManager(Logger logger, Config config) {
		super(logger, config);
	}

	private Map<Long, MessageBasedReponse> waiters = new Hashtable<Long, MessageBasedReponse>();
	private Long lastWaiteId = Long.MIN_VALUE;
	private synchronized Long nextId() {
		return lastWaiteId == Long.MAX_VALUE ? Long.MIN_VALUE + 1 : lastWaiteId++;
	} 
	
	public <T> void handleResponse(MessageBasedRequest<T> reponse) {
		if(waiters.containsKey(reponse.getRequestId())) {
			MessageBasedReponse waiter = waiters.remove(reponse.getRequestId()) ;
			waiter.setData(reponse);
			waiter.notify();
		}
	}
	
	public void handleRequestError(Long id, String error) {
		if(waiters.containsKey(id)) {			
			MessageBasedReponse waiter = waiters.remove(id) ;
			waiter.setError(error);
			waiter.notify();			
		}
	}	
	public <T> MessageBasedReponse startStream(java.util.function.Function<Long,MessageBasedRequest<T>> createRequest) {
		return this.startStream(createRequest, this.timeout);
	}

	public <T> MessageBasedReponse startStream(java.util.function.Function<Long,MessageBasedRequest<T>> createRequest, int timeout) {
		Long id = this.nextId();
		MessageBasedReponse waiter = new MessageBasedReponse(id);
		this.waiters.put(id, waiter);
		MessageBasedRequest<T> request = createRequest.apply(id);
		if(request != null) {
			request.addReponsePoint(this.consumeTopic);
			try {
				ProducerRecord<String, byte[]> record = KafkaMessageUtils.getProcedureRecord(request, this.requestTopic);
				this.producer.send(record);
			} catch (IOException e1) {
				this.handleRequestError(id, "Request error");
				return waiter;
			}
			try {
				if(waiter.IsWaiting()) {
					waiter.wait(timeout);
				}
			} catch (InterruptedException e) {
				this.handleRequestError(id, "Request timeout");
				return waiter;
			}					
		}else {
			waiter.setError("Empty request");
		}
		return waiter;
	}	
}
