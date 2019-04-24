package hms.common;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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
	
	private class Result<T>{
		T data;
		Throwable ex;	
		Runnable wrap;
		long start =  java.lang.System.currentTimeMillis();
	}
	
	public <T> CompletableFuture<T> waitForSignal(IServiceChecker<T> waiter, int timeout){
		CompletableFuture<T> finalWaiter = new CompletableFuture<T>();
		final Result<T> waiting = new Result<>();
		waiting.wrap = () -> {
			if(java.lang.System.currentTimeMillis() - waiting.start > timeout) {
				finalWaiter.completeExceptionally(new TimeoutException());
			}else {
				try {
					if(waiter.isReady()) {
						finalWaiter.complete(waiter.getResult());
					}else if(waiter.isError()){
						finalWaiter.completeExceptionally(waiter.getError());
					}else {
						CompletableFuture.runAsync(waiting.wrap, this.ec);
					}
				} catch (Exception e) {
					finalWaiter.completeExceptionally(e);
				}				
			}
		};
		waiting.wrap.run();
		return finalWaiter;
	}

	
	public <T> CompletableFuture<T> waitForSignal(CompletableFuture<T> task, Callable<Boolean> quickcheck, int timeout){
		CompletableFuture<T> finalWaiter = new CompletableFuture<T>();
		final Result<T> waiting = new Result<>();
		try {
			waiting.data = task.get(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			waiting.ex = e;
		}
		
		waiting.wrap = () -> {
			if(java.lang.System.currentTimeMillis() - waiting.start > timeout) {
				finalWaiter.completeExceptionally(new TimeoutException());
			}else if(waiting.data != null) {
				try {
					if(quickcheck.call()) {
						finalWaiter.complete(waiting.data);
					}else {
						CompletableFuture.runAsync(waiting.wrap, this.ec);
					}
				} catch (Exception e) {
					finalWaiter.completeExceptionally(e);
				}				
			}else if(waiting.ex!=null) {
				finalWaiter.completeExceptionally(waiting.ex);
			}else {
				finalWaiter.complete(waiting.data);
			}
		};
		waiting.wrap.run();
		return finalWaiter;
	}
}
