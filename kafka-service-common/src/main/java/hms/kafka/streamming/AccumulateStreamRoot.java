package hms.kafka.streamming;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

public abstract class AccumulateStreamRoot<TStart, TItemRes> 
	extends MonoStreamRoot<TStart, List<TItemRes>>{
	
	
	private LinkedHashMap<UUID, AccumulateStreamResponse<TItemRes>> _waiters = new LinkedHashMap<>();
	@Override
	protected LinkedHashMap<UUID, ? extends StreamResponse<List<TItemRes>>> getWaiters(){
		return _waiters;
	}
	
	@Override
	protected StreamResponse<List<TItemRes>> createReponseInstance(UUID id, int timeout) {
		AccumulateStreamResponse<TItemRes>  waiter = new AccumulateStreamResponse<>(timeout);
		synchronized (this._waiters) {
			this._waiters.put(id, waiter);
		}
		return waiter;
	}	
	
	
	@Override
	public void handleResponse(HMSMessage<? extends List<TItemRes>> response) {
		AccumulateStreamResponse<TItemRes> waiter=null;
		synchronized (this._waiters) {
			waiter = this._waiters.getOrDefault(response.getRequestId(),null) ;	
			if(waiter!=null && waiter.getNumberOfReceivedPackages()+1 >= response.getTotalRequests()) {			
				this._waiters.remove(response.getRequestId());
			}
		}
		if(waiter!=null) {
			waiter.collectData(response.getData(), response.getTotalRequests());
		}
		else{
			this.getLogger().warn("Stream response without waiter {} {}", this.getStartTopic(), response.getRequestId());
		}
	}
}
