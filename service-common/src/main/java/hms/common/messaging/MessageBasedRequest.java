package hms.common.messaging;

public class MessageBasedRequest {
	private long requestId;	
	private String Data;
	public long getRequestId() {
		return requestId;
	}
	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}
	public String getData() {
		return Data;
	}
	public void setData(String data) {
		Data = data;
	}

}
