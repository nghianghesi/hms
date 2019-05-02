package hms.kafka.streamming;

import hms.common.ServiceWaiter.IServiceChecker;

public class StreamResponse<T> implements IServiceChecker<T>{
	protected T data;
	protected enum STATUS{waiting, error, ready};
	protected STATUS status = STATUS.waiting;	
	protected String error; 
	
	
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
