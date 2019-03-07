package hms.common.messaging;

public class MessageBasedReponse{
	private long requestId;	
	private String data;
	private boolean isWaiting = true;
	private boolean isError = false;
	
	public long getRequestId() {
		return requestId;
	}
	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}
	public String getData() {
		return data;
	}
	public synchronized void setData(String data) {
		this.isWaiting = false;
		this.data = data;
	}
	public synchronized boolean isError() {
		return isError;
	}
	synchronized boolean IsWaiting() {
		return this.isWaiting;
	}
	public synchronized void setError(String error) {
		this.isWaiting = false;
		this.isError = true;
		this.data = error;
	}
}
