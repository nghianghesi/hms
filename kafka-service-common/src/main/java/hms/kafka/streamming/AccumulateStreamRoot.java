package hms.kafka.streamming;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

public abstract class AccumulateStreamRoot<TStart, TItemRes> 
	extends AbstractStreamRoot<TStart, ArrayList<TItemRes>>{	
	
	private ArrayList<LinkedHashMap<UUID, AccumulateStreamResponse<TItemRes>>> _waiters 
			= new ArrayList<LinkedHashMap<UUID,  AccumulateStreamResponse<TItemRes>>>(KEY_RANGE);
	public AccumulateStreamRoot() {
		for(int i=0;i<KEY_RANGE;i++) {
			this._waiters.add(new LinkedHashMap<UUID, AccumulateStreamResponse<TItemRes>>());
		}
	}
	
	@Override
	protected ArrayList<? extends LinkedHashMap<UUID, ? extends StreamResponse<? extends ArrayList<TItemRes>>>> getAllWaiters() {
		return this._waiters;
	}
	
	@Override
	protected StreamResponse<? extends ArrayList<TItemRes>> removeWaiter(int keyrange, UUID id) {
		return this._waiters.get(keyrange).remove(id);
	}
	
	@Override
	protected StreamResponse<ArrayList<TItemRes>> createReponseInstance(UUID id, int timeout) {
		AccumulateStreamResponse<TItemRes>  waiter = new AccumulateStreamResponse<>(timeout);
		int keyrange = RequestIdToKeyRange(id);
		synchronized (this.getWaiters(keyrange)) {
			this._waiters.get(keyrange).put(id, waiter);
		}
		return waiter;
	}	
	
	
	@Override
	public void handleResponse(HMSMessage<ArrayList<TItemRes>> response) {
		AccumulateStreamResponse<TItemRes> waiter=null;
		int keyrange = RequestIdToKeyRange(response.getRequestId());
		
		synchronized (this.getWaiters(keyrange)) {
			waiter = this._waiters.get(keyrange).getOrDefault(response.getRequestId(),null) ;	
			if(waiter!=null && waiter.getNumberOfReceivedPackages()+1 >= response.getTotalRequests()) {			
				this._waiters.get(keyrange).remove(response.getRequestId());
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
