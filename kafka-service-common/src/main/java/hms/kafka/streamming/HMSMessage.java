package hms.kafka.streamming;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
	
	public HMSMessage(UUID requestid) {
		this.requestId = requestid;
	}
	
	public HMSMessage(UUID requestid, T reqdata) {
		this(requestid);
		this.data = reqdata;
	}
	
	private UUID requestId;	
	private List<BinaryResponsePoint> responsePoints = new ArrayList<>();	
	private T data;
	public UUID getRequestId() {
		return requestId;
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
	
	public String getCurrentResponsePoint(String defaultRepsonseTopic) {	
		if(this.responsePoints.size()>0) {
			return this.responsePoints.get(this.responsePoints.size()-1).point;
		}else {
			return defaultRepsonseTopic;
		}
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
	
	public <F> HMSMessage<F> forwardRequest(){
		HMSMessage<F> r = new HMSMessage<F>(this.requestId);
		r.responsePoints.addAll(this.responsePoints);
		return r;
	}	
	
	public String DebugInfo() {
		java.util.StringJoiner str = new java.util.StringJoiner (",");
		for(BinaryResponsePoint p: this.responsePoints) {
			str.add(p.point);
			if(p.data!=null) {
				str.add(new String(p.data));
			}
		}
		
		try {
			str.add(new String(KafkaMessageUtils.convertObjecttoByteArray(this.data)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			str.add(e.getMessage());
		}
		return str.toString();
	}	
}
