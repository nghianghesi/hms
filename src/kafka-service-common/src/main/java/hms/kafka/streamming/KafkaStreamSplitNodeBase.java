package hms.kafka.streamming;

import java.io.IOException;
import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

public abstract class KafkaStreamSplitNodeBase <TCon, TRepItem> 
	extends KafkaStreamNodeBase<TCon, java.util.List<TRepItem>> {
		
	
	@Override
	protected void reply(HMSMessage<TCon> request, java.util.List<TRepItem> valuelist) {
		for(TRepItem value:valuelist) {
			HMSMessage<TRepItem> replymsg = request.forwardRequest();
			replymsg.setData(value);
			String replytop = applyTemplateToRepForTopic(request.getCurrentResponsePoint(this.getForwardTopic()), value);
				//this.getLogger().info("Replying to {}", replytop);
				ProducerRecord<UUID, byte[]> record;
				try {
					record = KafkaMessageUtils.getProcedureRecord(replymsg, replytop);
					this.producer.send(record,  (RecordMetadata metadata, Exception exception) -> {
						if(exception!=null) {
							this.getLogger().error("Reply request error {}", exception.getMessage());
						}
					});					
				} catch (IOException e) {
					this.getLogger().error("Reply request error {}", e.getMessage());
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
				this.producer.send(record,  (RecordMetadata metadata, Exception exception) -> {
					if(exception!=null) {
						this.getLogger().error("Reply request error {}", exception.getMessage());
					}
				});
			} catch (IOException e) {
				this.getLogger().error("Forward request error {}", e.getMessage());
			}
		}
	}
}
