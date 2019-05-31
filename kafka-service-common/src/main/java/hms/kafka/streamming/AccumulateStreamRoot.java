package hms.kafka.streamming;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class AccumulateStreamRoot<TStart, TItemRes> 
	extends StreamRoot<TStart, List<TItemRes>>{
	
	
	private Map<UUID, AccumulateStreamResponse<TItemRes>> _waiters = new Hashtable<>();
	protected Map<UUID, ? extends StreamResponse<List<TItemRes>>> getWaiters(){
		return _waiters;
	}
	
	protected StreamResponse<List<TItemRes>> createReponseInstance(UUID id) {
		AccumulateStreamResponse<TItemRes>  waiter = new AccumulateStreamResponse<>();
		this._waiters.put(id, waiter);
		return waiter;				
	}	
	
	
	public void handleResponse(HMSMessage<? extends List<TItemRes>> response) {
		if(this._waiters.containsKey(response.getRequestId())) {
			AccumulateStreamResponse<TItemRes> waiter = this._waiters.get(response.getRequestId()) ;
			waiter.setData(response.getData());
			if(waiter.checkStatus(response.getTotalRequests())) {
				this._waiters.remove(response.getRequestId());
			}
		}else {
			this.getLogger().warn("Stream response without waiter {} {}", this.getStartTopic(), response.getRequestId());
		}
	}
}
