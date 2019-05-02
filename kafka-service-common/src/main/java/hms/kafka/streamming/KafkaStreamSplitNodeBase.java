package hms.kafka.streamming;

import java.io.IOException;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;

public abstract class KafkaStreamSplitNodeBase <TCon, TRepItem> 
	extends KafkaStreamNodeBase<TCon, java.util.List<TRepItem>> {
		
	@Override
	protected void forward(HMSMessage<TCon> request, java.util.List<TRepItem> valuelist) {
		for(TRepItem value:valuelist) {
			HMSMessage<TRepItem> forwardReq = request.forwardRequest(valuelist.size());
			forwardReq.setData(value);
			try {
				forwardReq.addReponsePoint(this.getForwardBackTopic(), request.getData());
				//this.getLogger().info("forwarding:" + forwardReq.DebugInfo());			
				String forwardtopic = applyTemplateToRepForTopic(this.getForwardTopic(), value);
				ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(forwardReq, forwardtopic);					
				this.producer.send(record);
			} catch (IOException e) {
				this.getLogger().error("Forward request error {}", e.getMessage());
			}
		}
	}
}
