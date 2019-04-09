package hms.ksql;

import java.time.Duration;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;

import hms.StreamResponse;


public abstract class KSQLRoot<TR, TRes> {
	protected KafkaConsumer<UUID, TRes> consumer;
	protected KafkaProducer<UUID, TR> producer;
	
	protected int timeout = 5000;
	private boolean shutdownNode = false; 

	
	
	protected abstract Logger getLogger();
	protected abstract Class<TRes> getMessageManifest();	
	protected abstract String getKafkaServer();	
	protected abstract String getServerid();
	
	protected String getConsumeTopic() {
		return this.getGetReqestTopic()+".return";
	}
	
	protected abstract String getGetReqestTopic();
	
	protected KSQLRoot() {
		this.ensureTopics();
		this.createProducer();
		this.createConsummer();
	}

	public void setTimeout(int timeoutInMillisecons) {
		this.timeout = timeoutInMillisecons;
	}
	
	protected String[] getRelatedTopics() {
		return new String[] {this.getGetReqestTopic(), this.getConsumeTopic()};
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

	protected void createProducer() {
		Properties props = KafkaConfigFactory.<UUID,TR>getProduceConfigs(this.getKafkaServer());
		this.producer = new KafkaProducer<>(props);
	}	

	protected void createConsummer() {
		Properties consumerProps = KafkaConfigFactory.getConsumerConfigs(UUID.class, this.getMessageManifest(), this.getKafkaServer(), this.getServerid());
		this.consumer = new KafkaConsumer<>(consumerProps);
		this.consumer.subscribe(Collections.singletonList(this.getConsumeTopic()));

		new Thread(() -> {		
			while (!shutdownNode) {
				ConsumerRecords<UUID, TRes> records = this.consumer.poll(Duration.ofMillis(500));
				for (ConsumerRecord<UUID, TRes> record : records) {
					this.getLogger().info("Get {} {}", this.getConsumeTopic() , record.key());						 
					this.handleResponse(record.key(), record.value());
				}
			}
		}).start();
		
		this.getLogger().info("KSQL root {} is ready", this.getGetReqestTopic());						 
	}


	private Map<UUID, StreamResponse> waiters = new Hashtable<UUID, StreamResponse>();
	private synchronized UUID nextId() {
		return UUID.randomUUID();
	} 
	
	public void handleResponse(UUID requestid, TRes reponse) {
		if(waiters.containsKey(requestid)) {
			StreamResponse waiter = waiters.remove(requestid) ;
			waiter.setData(reponse);
			synchronized(waiter) {
				waiter.notifyAll();
			}
		}
	}
	
	public void handleRequestError(UUID id, String error) {
		if(waiters.containsKey(id)) {			
			StreamResponse waiter = waiters.remove(id) ;
			waiter.setError(error);
			synchronized(waiter) {
				waiter.notifyAll();
			}
		}
	}	
	
	public StreamResponse startStream(TR request) {
		return this.startStream(request, this.timeout);
	}
	
	public StreamResponse startStream(TR request, int timeout) {
		UUID id = this.nextId();
		StreamResponse waiter = new StreamResponse(id);
		this.waiters.put(id, waiter);
		ProducerRecord<UUID, TR> record = new ProducerRecord<>(this.getGetReqestTopic(), id, request);
		if(request != null) {
			try {
				this.producer.send(record).get(timeout, TimeUnit.MILLISECONDS);
			} catch (InterruptedException | ExecutionException | TimeoutException e) {
				this.handleRequestError(id, String.format("Request error: {}", e.getMessage()));
				return waiter;
			}
			
			try {
				if(waiter.needWaiting()) {
					synchronized(waiter) {
						waiter.wait(timeout);				
					}
				}
			} catch (InterruptedException e) {
				this.handleRequestError(id, String.format("Request error: {}", e.getMessage()));
				return waiter;
			}					
		}else {
			waiter.setError("Empty request");
		}
		return waiter;
	}	
	
}
