package hms;

import java.util.UUID;

public class StreamResponse{
	private UUID requestId;	
	private Object data;
	private boolean needWaiting = true;
	private boolean isError = false;
	
	//TODO: Need support relay response
	private String error; 
	
	public StreamResponse(UUID requestId) {
		this.requestId = requestId;
	}
	
	public UUID getRequestId() {
		return requestId;
	}
	
	public Object getData() {
		return data;
	}
	
	public synchronized void setData(Object data) {
		this.needWaiting = false;
		this.data = data;
	}
	
	public synchronized boolean isError() {
		return isError;
	}
	
	public synchronized boolean needWaiting() {
		return this.needWaiting;
	}
	
	public synchronized void setError(String error) {
		this.needWaiting = false;
		this.isError = true;
		this.error = error;
	}
	
	public String getError() {
		return this.error;
	}
}
