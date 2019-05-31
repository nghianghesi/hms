package hms.kafka.streamming;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerRecord;

public abstract class SplitStreamRoot<TItemStart, TItemRes> 
	extends AccumulateStreamRoot<List<TItemStart>, TItemRes>{
	
	@Override
	public CompletableFuture<List<TItemRes>> startStream(List<TItemStart> data, int timeout) {
		UUID id = this.nextId();
		StreamResponse<List<TItemRes>> waiter = this.createReponseInstance(id);
		for(TItemStart di : data) {
			HMSMessage<TItemStart> request = new HMSMessage<TItemStart>(id, di);
			request.addReponsePoint(this.getConsumeTopic());			
			request.setTotalRequests(data.size());
			try {
				String startTopic = this.applyTemplateToRepForTopic(this.getStartTopic(), di); 
				ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(request, startTopic);
				//this.getLogger().info("Start stream {} {}", startTopic, request.getRequestId());
				this.producer.send(record).get(timeout, TimeUnit.MILLISECONDS);
			} catch (IOException | InterruptedException | ExecutionException | TimeoutException e) {
				this.handleRequestError(id, "Request error:"+e.getMessage());
			}
		}
		return hms.common.ServiceWaiter.getInstance().waitForSignal(waiter,timeout);
	}
}
