package hms.kafka.messaging;

import java.util.HashMap;
import java.util.Map;

public class MessageBasedRequest<T> {
	private long requestId;	
	private Map<String, byte[]> responsePoints = new HashMap<>();	
	private T data;
	public long getRequestId() {
		return requestId;
	}
	
	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}
	
	public T getData() {
		return data;
	}
	
	public void setData(T data) {
		this.data = data;
	}
	
	Map<String, byte[]> getReponsePoints(String point) {
		return this.responsePoints;
	}
	
	public void addReponsePoint(String point) {
		this.responsePoints.put(point, null);
	}
}
