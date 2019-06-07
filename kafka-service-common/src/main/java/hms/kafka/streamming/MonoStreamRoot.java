package hms.kafka.streamming;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

public abstract class MonoStreamRoot<TStart, TRes> 
	extends AbstractStreamRoot<TStart, TRes>{ // consume TRes & forward to none.
	private ArrayList<LinkedHashMap<UUID, StreamResponse<TRes>>> _waiters = new ArrayList<LinkedHashMap<UUID, StreamResponse<TRes>>>(KEY_RANGE);
	public MonoStreamRoot() {
		for(int i=0;i<KEY_RANGE;i++) {
			this._waiters.add(new LinkedHashMap<UUID, StreamResponse<TRes>>());
		}
	}
	
	@Override
	protected ArrayList<? extends LinkedHashMap<UUID, ? extends StreamResponse<? extends TRes>>> getAllWaiters() {
		// TODO Auto-generated method stub
		return this._waiters;
	}
	
	@Override
	protected StreamResponse<? extends TRes> removeWaiter(int keyrange, UUID id) {
		return this._waiters.get(keyrange).remove(id);
	}
	
	public void handleResponse(HMSMessage<TRes> response) {
		StreamResponse<TRes> waiter = null;
		int keyrange = RequestIdToKeyRange(response.getRequestId());
		synchronized (this.getWaiters(keyrange)) {
			waiter = this._waiters.get(keyrange).remove(response.getRequestId());
		}
		if(waiter!=null) {
			waiter.setData(response.getData());
		}else {
			this.getLogger().warn("Stream response without waiter {}, {}, {}", this.getStartTopic(), response.getRequestId(), this._waiters.size());
		}
	}
	
	
	@Override
	protected StreamResponse<TRes> createReponseInstance(UUID id, int timeout) {
		StreamResponse<TRes>  waiter = new StreamResponse<>(timeout);
		int keyrange = RequestIdToKeyRange(id);
		synchronized (this.getWaiters(keyrange)) {			
			this._waiters.get(keyrange).put(id, waiter);
		}
		return waiter;
	}
}
