package hms.ksql;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.KeyValue;
import org.slf4j.Logger;

public abstract class KSQLNode<TReq, TRes> {
	
	protected abstract TRes process(UUID key, TReq request);
	protected abstract String getKafkaServer();
	protected abstract String getSourceTopic();
	protected abstract Class<TReq> getRequestManifest();			
	protected abstract Class<TRes> getResponseManifest();
	protected abstract String getTransformTopic();
	protected abstract String getStorageName();
	protected abstract Logger getLogger();
	protected int timeout = 5000;
	
	private class MyTranformer implements Transformer<UUID, TReq, KeyValue<UUID, TRes>>{

		@Override
		public void init(ProcessorContext context) {
			// TODO Auto-generated method stub			
		}

		@Override
		public KeyValue<UUID, TRes> transform(UUID key, TReq value) {
			return new KeyValue<UUID, TRes>(key, process(key, value));
		}

		@Override
		public void close() {
			// TODO Auto-generated method stub		
		}		
	}

	public KSQLNode() {
		this.ensureTopics();
		this.createTransformer();
	}	

	public void setTimeout(int timeoutInMillisecons) {
		this.timeout = timeoutInMillisecons;
	}
	
	protected String[] getRelatedTopics() {
		return new String[] {this.getSourceTopic(), this.getTransformTopic()};
	}

	protected void ensureTopics() { 
		for(String topic:this.getRelatedTopics()) {
			try {
				KafkaConfigFactory.ensureTopic(this.getKafkaServer(), topic);			
			} catch (InterruptedException | ExecutionException e) {
				this.getLogger().error("Create topic {} error {}", topic, e.getMessage());
			}			
		}	
	}	

	protected void createTransformer() {
	    final StreamsBuilder builder = new StreamsBuilder();
	    final KStream<UUID, TReq> stream = builder.stream(this.getSourceTopic(),
	    		Consumed.with(new DslJsonSerDes<>(UUID.class), 
	    					new DslJsonSerDes<>(this.getRequestManifest())));
	    stream.transform(MyTranformer::new, this.getStorageName())
	    .to(this.getTransformTopic(), 
	    		Produced.with(
		    		new DslJsonSerDes<>(UUID.class), 
					new DslJsonSerDes<>(this.getResponseManifest())));
	}
}
