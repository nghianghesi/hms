package hms.common.messaging;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;
public class MessageBasedServiceManager {

	private Map<Long, MessageBasedReponse> waiters = new Hashtable<Long, MessageBasedReponse>();
	private Long lastWaiteId = Long.MIN_VALUE;
	private synchronized Long nextId() {
		return lastWaiteId == Long.MAX_VALUE ? Long.MIN_VALUE : lastWaiteId++;
	} 

	public void handlerResponse(Long id, String data) {
		if(waiters.containsKey(id)) {
			MessageBasedReponse waiter = waiters.remove(id) ;
			waiter.setData(data);
			waiter.notify();
		}
	}
	
	public void handlerRequestError(Long id, String error) {
		if(waiters.containsKey(id)) {			
			MessageBasedReponse waiter = waiters.remove(id) ;
			waiter.setError(error);
			waiter.notify();			
		}
	}	
	
	public <T> MessageBasedReponse request(java.util.function.Function<MessageBasedRequest,Future<T>> doRequest, int timeout) {
		Long id = this.nextId();
		MessageBasedReponse waiter = new MessageBasedReponse();
		waiter.setRequestId(id);
		this.waiters.put(id, waiter);
		

		MessageBasedRequest request = new MessageBasedRequest();
		request.setRequestId(id);
		doRequest.apply(request);
		try {
			if(waiter.IsWaiting()) {
				waiter.wait(timeout);
			}
		} catch (InterruptedException e) {
			this.handlerRequestError(id, "Request timeout");
		}					
		return waiter;
	}
}
