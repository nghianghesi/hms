package hms.kafka.streamming;

import java.util.LinkedHashMap;
import java.util.UUID;

public abstract class MonoStreamRoot<TStart, TRes> 
	extends AbstractStreamRoot<TStart, TRes>{ // consume TRes & forward to none.
	private LinkedHashMap<UUID, StreamResponse<TRes>> _waiters = new LinkedHashMap<UUID, StreamResponse<TRes>>();
	
	@Override
	protected LinkedHashMap<UUID, ? extends StreamResponse<TRes>> getWaiters(){
		return _waiters;
	}
	
	@Override
	protected StreamResponse<TRes> createReponseInstance(UUID id, int timeout) {
		StreamResponse<TRes>  waiter = new StreamResponse<>(timeout);
		this._waiters.put(id, waiter);
		return waiter;
	}
}
