package hms.kafka.streamming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;


public abstract class SplitStreamRoot<TItemStart, TItemRes> 
	extends AccumulateStreamRoot<List<TItemStart>, TItemRes>{

	protected abstract String getZone(TItemStart data); 
	@Override
	public CompletableFuture<ArrayList<TItemRes>> startStream(List<TItemStart> data, int timeout) {
		UUID id = this.nextId();
		StreamResponse<ArrayList<TItemRes>> waiter = this.createReponseInstance(id, timeout);
		for(TItemStart di : data) {
			HMSMessage<TItemStart> request = new HMSMessage<TItemStart>(id, di);

			KafkaStreamRootNode rootNode = this.rootNodes.get(this.getZone(di));
			request.addReponsePoint(rootNode.getConsumeTopic());			
			request.setTotalRequests(data.size());
			try {
				String startTopic = rootNode.applyTemplateToRepForTopic(this.getStartTopic(), di); 
				ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(request, startTopic);
				//this.getLogger().info("Start stream {} {}", startTopic, request.getRequestId());
				rootNode.producer.send(record, (meta, ex) -> {
					if(ex!=null) {
						this.getLogger().error("********** Request error {}", ex.getMessage());
						this.handleRequestError(id, "Request error:");
					}
				});
			} catch (IOException e) {

				this.getLogger().error("********** Request error {}", e.getMessage());				
				this.handleRequestError(id, "Request error");
			}
		}
		return waiter.getWaiterTask();
	}
}
