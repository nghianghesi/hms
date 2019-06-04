package hms.kafka.streamming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;

public abstract class SplitStreamRoot<TItemStart, TItemRes> 
	extends AccumulateStreamRoot<List<TItemStart>, TItemRes>{
	
	@Override
	public CompletableFuture<ArrayList<TItemRes>> startStream(List<TItemStart> data, int timeout) {
		UUID id = this.nextId();
		StreamResponse<ArrayList<TItemRes>> waiter = this.createReponseInstance(id, timeout);
		for(TItemStart di : data) {
			HMSMessage<TItemStart> request = new HMSMessage<TItemStart>(id, di);
			request.addReponsePoint(this.getConsumeTopic());			
			request.setTotalRequests(data.size());
			try {
				String startTopic = this.applyTemplateToRepForTopic(this.getStartTopic(), di); 
				ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(request, startTopic);
				//this.getLogger().info("Start stream {} {}", startTopic, request.getRequestId());
				this.producer.send(record, (meta, ex) -> {
					if(ex!=null) {
						this.getLogger().error("Request error {}", ex.getMessage());
						this.handleRequestError(id, "Request error:");
					}
				});
			} catch (IOException e) {
				this.handleRequestError(id, "Request error");
			}
		}
		return waiter.getWaiterTask();
	}
}
