package hms.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class ServiceWaiter {
	
	public static interface IServiceChecker<T>{
		boolean isReady();
		T getResult();
		boolean isError();
		Throwable getError();
	}
	
	private static ServiceWaiter instance = new ServiceWaiter();
	public static ServiceWaiter getInstance() {
		return instance;
	}
	
	private ExecutorService ec = Executors.newFixedThreadPool(1);
	private final int IdleDuration = 100;
	private boolean shuttingdown = false;
	
	private void sleeping() {
		if(!this.shuttingdown) {
			try {
				Thread.sleep(this.IdleDuration);
				this.currentStick = java.lang.System.currentTimeMillis();
				CompletableFuture.runAsync(()->this.sleeping(),this.ec);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private ServiceWaiter() {	
		CompletableFuture.runAsync(()->this.sleeping(),this.ec);
	}
	
	public void shutdown() {
		this.shuttingdown = true;
	}
	
	private class WaitingWrapper{
		Runnable wrap;
		final long start =  java.lang.System.currentTimeMillis();
	}
	
	private long currentStick = java.lang.System.currentTimeMillis();
	
	public <T> CompletableFuture<T> waitForSignal(IServiceChecker<T> checker, int timeout){
		CompletableFuture<T> locker = new CompletableFuture<T>();
		final WaitingWrapper wrapper = new WaitingWrapper();
		wrapper.wrap = () -> {
			if(this.currentStick - wrapper.start > timeout) {
				locker.completeExceptionally(new TimeoutException());
			}else {
				try {
					if(checker.isReady()) {
						locker.complete(checker.getResult());
					}else if(checker.isError()){
						locker.completeExceptionally(checker.getError());
					}else {
						CompletableFuture.runAsync(wrapper.wrap, this.ec);
					}
				} catch (Exception e) {
					locker.completeExceptionally(e);
				}
			}
		};
		wrapper.wrap.run();
		return locker;
	}
}
