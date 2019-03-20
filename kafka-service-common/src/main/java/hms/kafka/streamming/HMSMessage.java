package hms.kafka.streamming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HMSMessage<T> {	
	
	public static class ResponsePoint<R> {
		public final String point;
		public final R data;
		ResponsePoint(String point, R data) {
			this.point = point;
			this.data = data;
		}
	}
	
	static class BinaryResponsePoint {
		final String point;
		final byte[] data;
		BinaryResponsePoint(String point, byte[] data) {
			this.point = point;
			this.data = data;
		}
	}
	
	public HMSMessage(long requestid, String messageKey) {
		this.requestId = requestid;
		this.messageKey = messageKey;
	}
	
	public HMSMessage(long requestid, String messageKey, T reqdata) {
		this(requestid, messageKey);
		this.data = reqdata;
	}
	
	private String messageKey;
	private long requestId;	
	private List<BinaryResponsePoint> responsePoints = new ArrayList<>();	
	private T data;
	public long getRequestId() {
		return requestId;
	}	
	
	public String getMessageKey() {
		return messageKey;
	}
	
	
	public T getData() {
		return data;
	}
	
	public void setData(T data) {
		this.data = data;
	}
	
	List<BinaryResponsePoint> getReponsePoints() {
		return this.responsePoints;
	}	
		
	void internalAddReponsePoint(String point, byte[] data) {
		this.responsePoints.add(new BinaryResponsePoint(point, data));
	}
	
	public void addReponsePoint(String point) {
		this.internalAddReponsePoint(point, null);
	}	
	
	public <R> void addReponsePoint(String point, R data) throws IOException {
		this.internalAddReponsePoint(point, KafkaMessageUtils.convertObjecttoByteArray(data));
	}
	
	public <R> ResponsePoint<R> popReponsePoint(Class<R> manifest) throws IOException{		
		BinaryResponsePoint p = this.responsePoints.remove(this.responsePoints.size()-1);
		if(p.data != null) {
			return new ResponsePoint<R>(p.point, KafkaMessageUtils.convertByteArrayToObject(manifest, p.data));
		}else {
			return new ResponsePoint<R>(p.point, null);
		}
	}
}
