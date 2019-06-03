package hms.kafka.streamming;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;

public abstract class KafkaStreamSplitNodeBase <TCon, TRepItem> 
	extends KafkaStreamNodeBase<TCon, java.util.List<TRepItem>> {
		
	
	@Override
	protected void reply(HMSMessage<TCon> request, java.util.List<TRepItem> valuelist) {
		for(TRepItem value:valuelist) {
			HMSMessage<TRepItem> replymsg = request.forwardRequest();
			replymsg.setData(value);
			String replytop = applyTemplateToRepForTopic(request.getCurrentResponsePoint(this.getForwardTopic()), value);
			try {
				//this.getLogger().info("Replying to {}", replytop);
				ProducerRecord<UUID, byte[]> record = KafkaMessageUtils.getProcedureRecord(replymsg, replytop);
				this.producer.send(record).get();
			} catch (IOException | InterruptedException | ExecutionException e) {
				this.getLogger().error("Reply message error {} {}", replytop, e.getMessage());
			}
		}
	}
	
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
