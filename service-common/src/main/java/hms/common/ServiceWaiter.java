package hms.common;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class ServiceWaiter {	
	public static interface IServiceChecker<T>{
		boolean isReady();
		T getResult();
		boolean isError();
		Throwable getError();
	}
	
	private class IntervalStickObservable extends Observable{
		public void onStick() {
			this.setChanged();
			this.notifyObservers();
		}
	}
	
	private static ServiceWaiter instance = new ServiceWaiter();
	public static ServiceWaiter getInstance() {
		return instance;
	}
	
	
	private IntervalStickObservable serviceWaiterObservable = new IntervalStickObservable();
	private final int IdleDuration = 100;
	private boolean shuttingdown = false;
	
	private void sleeping() {
		while(!this.shuttingdown) {
			try {
				long prevstick = this.currentStick;
				this.currentStick = java.lang.System.currentTimeMillis();
				if(this.currentStick - prevstick < this.IdleDuration) {
					Thread.sleep(this.IdleDuration - this.currentStick + prevstick);
				}
				this.currentStick = java.lang.System.currentTimeMillis();
				if(!this.shuttingdown) {				
					serviceWaiterObservable.onStick();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	private ServiceWaiter() {
		new Thread(()->this.sleeping()).start();
	}
	
	public void shutdown() {
		this.shuttingdown = true;
	}
	
	private class WaitingWrapper<T> implements Observer{
		final long start =  java.lang.System.currentTimeMillis();
		final IServiceChecker<T> checker;
		final long timeout;
		final CompletableFuture<T> locker;
		public WaitingWrapper(IServiceChecker<T> checker, long timeout, CompletableFuture<T> locker) {
			this.checker = checker;
			this.timeout = timeout;
			this.locker = locker;
			serviceWaiterObservable.addObserver(this);
		}
		
		@Override
		public void update(Observable arg0, Object arg1) {
			if(currentStick - start > timeout) {
				serviceWaiterObservable.deleteObserver(this);
				locker.completeExceptionally(new TimeoutException());				
			}else {
				try {
					if(checker.isReady()) {
						serviceWaiterObservable.deleteObserver(this);
						locker.complete(checker.getResult());						
					}else if(checker.isError()){
						serviceWaiterObservable.deleteObserver(this);
						locker.completeExceptionally(checker.getError());						
					}
				} catch (Exception e) {
					serviceWaiterObservable.deleteObserver(this);
					locker.completeExceptionally(e);					
				}
			}
		}
	}
	
	private long currentStick = java.lang.System.currentTimeMillis();
	
	public <T> CompletableFuture<T> waitForSignal(IServiceChecker<T> checker, long timeout){
		CompletableFuture<T> locker = new CompletableFuture<T>();
		new WaitingWrapper<T>(checker, timeout, locker);
		return locker;
	}
	
	public void addHeartbeatObserver(Observer observer) {
		this.serviceWaiterObservable.addObserver(observer);
	}
	
	public void removeHeartbeatObserver(Observer observer) {
		this.serviceWaiterObservable.deleteObserver(observer);
	}
}
