package hms;

import java.util.UUID;

import hms.common.ServiceWaiter.IServiceChecker;

public class StreamResponse<T> implements IServiceChecker<T>{
	private UUID requestId;	
	private T data;
	private enum STATUS{waiting, error, ready};
	private STATUS status = STATUS.waiting;
	
	//TODO: Need support relay response
	private String error; 
	
	public StreamResponse(UUID requestId) {
		this.requestId = requestId;
	}
	
	public UUID getRequestId() {
		return requestId;
	}
	
	public T getResult() {
		return data;
	}
	
	public void setData(T data) {
		this.data = data;
		this.status = STATUS.ready;		
	}
	
	public boolean isError() {
		return this.status == STATUS.error;
	}
	
	public boolean isReady() {
		return this.status == STATUS.ready;
	}
	
	public void setError(String error) {
		this.error = error;		
		this.status = STATUS.error;
	}
	
	public Throwable getError() {
		return new Exception(this.error);
	}	
}
