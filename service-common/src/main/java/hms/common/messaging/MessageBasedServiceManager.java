package hms.common.messaging;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Future;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonWriter;
import com.dslplatform.json.runtime.Settings;

public class MessageBasedServiceManager {
	public final String REQUEST_ID_KEY = "request-id";
	public final String RETURN_TOPIC_KEY = "return-topic-key";
	private final DslJson<Object> dslJson = new DslJson<>(Settings.withRuntime().allowArrayFormat(true).includeServiceLoader());
	private Map<Long, MessageBasedReponse> waiters = new Hashtable<Long, MessageBasedReponse>();
	private Long lastWaiteId = Long.MIN_VALUE;
	private synchronized Long nextId() {
		return lastWaiteId == Long.MAX_VALUE ? Long.MIN_VALUE : lastWaiteId++;
	} 


	public void handlerResponse(Long id, byte[] data) {
		if(waiters.containsKey(id)) {
			MessageBasedReponse waiter = waiters.remove(id) ;
			waiter.setData(data);
			waiter.notify();			
		}
	}
	
	public void handlerRequestError(Long id, String error) {
		if(waiters.containsKey(id)) {			
			MessageBasedReponse waiter = waiters.remove(id) ;
			waiter.setError(error);
			waiter.notify();			
		}
	}	
	
	public <MT> MessageBasedReponse request(java.util.function.Function<MessageBasedRequest,Future<MT>> doRequest, int timeout) {
		Long id = this.nextId();
		MessageBasedReponse waiter = new MessageBasedReponse();
		waiter.setRequestId(id);
		this.waiters.put(id, waiter);
		

		MessageBasedRequest request = new MessageBasedRequest();
		request.setRequestId(id);
		if(doRequest.apply(request) != null) {
			try {
				if(waiter.IsWaiting()) {
					waiter.wait(timeout);
				}
			} catch (InterruptedException e) {
				this.handlerRequestError(id, "Request timeout");
			}					
		}else {
			waiter.setError("Request Error");
		}
		return waiter;
	}

	public byte[] toRequestBody(Object data) throws IOException {
		JsonWriter writer = dslJson.newWriter();
		dslJson.serialize(writer, data);
		return writer.getByteBuffer();
	}
	
	public Object toResponse(final Type manifest, byte[] data) throws IOException{
		return dslJson.deserialize(manifest, data, data.length);
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

}
