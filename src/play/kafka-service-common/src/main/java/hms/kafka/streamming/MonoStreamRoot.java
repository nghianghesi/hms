package hms.kafka.streamming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;


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
			this.getLogger().warn("Stream response without waiter {}, {}, {}", this.getStartTopic(), response.getRequestId(), this._waiters.get(keyrange).size());
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

	protected abstract String getZone(TStart data); 
	@Override
	public CompletableFuture<TRes> startStream(TStart data, int timeout) {
		UUID id = this.nextId();
		StreamResponse<TRes> waiter = this.createReponseInstance(id,timeout);	
		HMSMessage<TStart> request = new HMSMessage<TStart>(id, data);
		KafkaStreamRootNode rootNode = this.rootNodes.get(this.getZone(data));
		request.addReponsePoint(rootNode.getConsumeTopic());			
		try {				
			String startTopic = rootNode.applyTemplateToRepForTopic(this.getStartTopic(), data); 			
			ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(request, startTopic);
			//this.getLogger().info("Start stream {} {}", startTopic, request.getRequestId());
			rootNode.producer.send(record,  (RecordMetadata metadata, Exception exception) -> {
				if(exception!=null) {
					this.getLogger().info("**** Request error..."+exception.getMessage());
					this.handleRequestError(id, "Request error");
				}
			});
		} catch (IOException e) {
			this.handleRequestError(id, "Request error");
		}
		return waiter.getWaiterTask();
	}	
}
