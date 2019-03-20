package hms.kafka.streamming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MessageBasedRequest<T> {	
	
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
	private long requestId;	
	private List<BinaryResponsePoint> responsePoints = new ArrayList<>();	
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
		this.internalAddReponsePoint(point, MessageKafkaIntegration.convertObjecttoByteArray(data));
	}
	
	public <R> ResponsePoint<R> popReponsePoint(Class<R> manifest) throws IOException{		
		BinaryResponsePoint p = this.responsePoints.remove(this.responsePoints.size()-1);
		if(p.data != null) {
			return new ResponsePoint<R>(p.point, MessageKafkaIntegration.convertByteArrayToObject(manifest, p.data));
		}else {
			return new ResponsePoint<R>(p.point, null);
		}
	}
}
