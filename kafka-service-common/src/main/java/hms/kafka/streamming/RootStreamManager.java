package hms.kafka.streamming;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;

import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;

import com.typesafe.config.Config;

public abstract class RootStreamManager extends KafkaNodeBase{
	protected RootStreamManager(Logger logger, Config config) {
		super(logger, config);
	}

	private Map<Long, MessageBasedReponse> waiters = new Hashtable<Long, MessageBasedReponse>();
	private Long lastWaiteId = Long.MIN_VALUE;
	private synchronized Long nextId() {
		return lastWaiteId == Long.MAX_VALUE ? Long.MIN_VALUE : lastWaiteId++;
	} 
	
	public void handleResponse(Long id, byte[] data) {
		if(waiters.containsKey(id)) {
			MessageBasedReponse waiter = waiters.remove(id) ;
			waiter.setData(data);
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
	
	public MessageBasedReponse startStream(java.util.function.Function<Long,Future<RecordMetadata>> doRequest, int timeout) {
		Long id = this.nextId();
		MessageBasedReponse waiter = new MessageBasedReponse();
		waiter.setRequestId(id);
		this.waiters.put(id, waiter);		

		if(doRequest.apply(id) != null) {
			try {
				if(waiter.IsWaiting()) {
					waiter.wait(timeout);
				}
			} catch (InterruptedException e) {
				this.handleRequestError(id, "Request timeout");
			}					
		}else {
			waiter.setError("Request Error");
		}
		return waiter;
	}
}
