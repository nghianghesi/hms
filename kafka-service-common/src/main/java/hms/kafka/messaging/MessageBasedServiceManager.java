package hms.kafka.messaging;

import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;

import com.dslplatform.json.DslJson;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;

import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.Settings;

public class MessageBasedServiceManager {
	
	private static final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private static final String ForwarPointdHeaderName = "forward-points";
	private static final String ForwarDataHeaderName = "forward-data";
	public byte[] convertObjecttoByteArray(Object data) throws IOException {
		if(data != null) {
			JsonWriter writer = dslJson.newWriter();
			dslJson.serialize(writer, data);
			return writer.getByteBuffer();
		}else {
			return new byte[] {};
		}
	}
	
	public <R> R convertByteArrayToObject(Class<R> manifest, byte[] data) throws IOException{
		if(data != null && data.length > 0) {
			return dslJson.deserialize(manifest, data, data.length);
		}else {
			return null;
		}
	}	
	
	public byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}

	public long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}	
	
	public int bytesToInt(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getInt();
	}
	
	public byte[] intToBytes(int x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}	
	
	public <T> MessageBasedRequest<T> getRequestObject (Class<T> manifest, ConsumerRecord<String, byte[]> record) throws IOException{
		MessageBasedRequest<T> req = new MessageBasedRequest<T>();
		Iterator<Header> hiPoints = record.headers().headers(ForwarPointdHeaderName).iterator();
		Iterator<Header> hiData = record.headers().headers(ForwarDataHeaderName).iterator();
		
		req.setData(convertByteArrayToObject(manifest, record.value()));
		while(hiPoints.hasNext() && hiData.hasNext()) {
			req.internalAddReponsePoint(new String(hiPoints.next().value()), hiData.next().value());
		}
		return req;
	}
	
	public <T> ProducerRecord<String, byte[]> getProcedureRecord(Class<T> manifest, MessageBasedRequest<T> req, String topic, String key) throws IOException{
		byte[] body = convertObjecttoByteArray(req.getData());
		ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, key, body);
			
		for(MessageBasedRequest.BinaryResponsePoint respoint:req.getReponsePoints()) {
			record.headers().add(ForwarPointdHeaderName, respoint.point.getBytes());			
			record.headers().add(ForwarDataHeaderName, respoint.data);
		}
		return record;
	}	
}
