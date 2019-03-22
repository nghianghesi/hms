package hms.kafka.streamming;

import com.dslplatform.json.DslJson;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;

import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.Settings;

public class KafkaMessageUtils {
	
	private static final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private static final String ForwarPointdHeaderName = "forward-points";
	private static final String ForwarDataHeaderName = "forward-data";
	private static final String RequestIdHeaderName = "forward-data";
	public static byte[] convertObjecttoByteArray(Object data) throws IOException {
		if(data != null) {
			JsonWriter writer = dslJson.newWriter();
			dslJson.serialize(writer, data);
			return writer.getByteBuffer();
		}else {
			return new byte[] {};
		}
	}
	
	public static <R> R convertByteArrayToObject(Class<R> manifest, byte[] data) throws IOException{
		if(data != null && data.length > 0) {
			return dslJson.deserialize(manifest, data, data.length);
		}else {
			return null;
		}
	}	
	
	public static byte[] longToBytes(long x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}

	public static long bytesToLong(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getLong();
	}	
	
	public static int bytesToInt(byte[] bytes) {
	    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
	    buffer.put(bytes);
	    buffer.flip();//need flip 
	    return buffer.getInt();
	}
	
	public static byte[] intToBytes(int x) {
	    ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
	    buffer.putLong(x);
	    return buffer.array();
	}	
	
	  public static UUID bytesToUuid(byte[] bytes) {
		    ByteBuffer bb = ByteBuffer.wrap(bytes);
		    long firstLong = bb.getLong();
		    long secondLong = bb.getLong();
		    return new UUID(firstLong, secondLong);
		  }

		  public static byte[] UuidToBytes(UUID uuid) {
		    ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
		    bb.putLong(uuid.getMostSignificantBits());
		    bb.putLong(uuid.getLeastSignificantBits());
		    return bb.array();
		  }	
	
	public static UUID getRequestId(ConsumerRecord<String, byte[]> record) {
		Iterator<Header> i= record.headers().headers(RequestIdHeaderName).iterator();
		return i.hasNext() ? KafkaMessageUtils.bytesToUuid(i.next().value()):null;
	}
	public static<T> HMSMessage<T> getHMSMessage (Class<T> manifest, ConsumerRecord<String, byte[]> record) throws IOException{
		UUID requestid = getRequestId(record);
		HMSMessage<T> req = new HMSMessage<T>(requestid, record.key());
		Iterator<Header> hiPoints = record.headers().headers(ForwarPointdHeaderName).iterator();
		Iterator<Header> hiData = record.headers().headers(ForwarDataHeaderName).iterator();		
		req.setData(convertByteArrayToObject(manifest, record.value()));
		while(hiPoints.hasNext() && hiData.hasNext()) {
			req.internalAddReponsePoint(new String(hiPoints.next().value()), hiData.next().value());
		}
		return req;
	}
	
	public static <T> ProducerRecord<String, byte[]> getProcedureRecord(HMSMessage<T> req, String topic) throws IOException{
		byte[] body = convertObjecttoByteArray(req.getData());
		ProducerRecord<String, byte[]> record = new ProducerRecord<>(topic, req.getMessageKey(), body);
		record.headers().add(RequestIdHeaderName, KafkaMessageUtils.UuidToBytes(req.getRequestId()));
		for(HMSMessage.BinaryResponsePoint respoint:req.getReponsePoints()) {
			record.headers().add(ForwarPointdHeaderName, respoint.point.getBytes());			
			record.headers().add(ForwarDataHeaderName, respoint.data);
		}
		return record;
	}		
	
	public static <T> ProducerRecord<String, byte[]> getProcedureRecord(UUID requestd, T reqdata, String topic, String key) throws IOException{
		HMSMessage<T> req= new HMSMessage<T>(requestd, key, reqdata);
		return getProcedureRecord(req,topic);
	}
}
