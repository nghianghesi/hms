package hms.kafka.streamming;

public class MessageBasedReponse{
	private long requestId;	
	private Object data;
	private boolean isWaiting = true;
	private boolean isError = false;
	private String error;
	
	public MessageBasedReponse(long requestId) {
		this.requestId = requestId;
	}
	public long getRequestId() {
		return requestId;
	}
	public Object getData() {
		return data;
	}
	public synchronized void setData(Object data) {
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
		this.error = error;
	}
	public String getError() {
		return this.error;
	}
}
