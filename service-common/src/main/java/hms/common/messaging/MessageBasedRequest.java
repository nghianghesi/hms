package hms.common.messaging;

public class MessageBasedRequest {
	private long requestId;	
	private byte[] Data;
	public long getRequestId() {
		return requestId;
	}
	
	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}
	
	public byte[] getData() {
		return Data;
	}
	
	public void setData(byte[] data) {
		Data = data;
	}
}
