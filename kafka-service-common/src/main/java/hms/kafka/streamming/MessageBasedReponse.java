package hms.kafka.streamming;

public class MessageBasedReponse{
	private long requestId;	
	private byte[] data;
	private boolean isWaiting = true;
	private boolean isError = false;
	private String error;
	
	public long getRequestId() {
		return requestId;
	}
	public void setRequestId(long requestId) {
		this.requestId = requestId;
	}
	public byte[] getData() {
		return data;
	}
	public synchronized void setData(byte[] data) {
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
