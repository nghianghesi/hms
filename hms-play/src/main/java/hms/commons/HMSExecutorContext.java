package hms.commons;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import hms.common.IHMSExecutorContext;
import hms.common.ServiceWaiter;
import play.inject.ApplicationLifecycle;
import play.libs.concurrent.HttpExecutionContext;

public class HMSExecutorContext implements IHMSExecutorContext{
	private HttpExecutionContext httpExecutionContext;

	@Inject
	public HMSExecutorContext(HttpExecutionContext ec,ApplicationLifecycle app) {
		this.httpExecutionContext = ec;
		
		app.addStopHook(() ->{
			return CompletableFuture.runAsync(()->{
				ServiceWaiter.getInstance().shutdown();
			});
		});		
	}
	
	@Override
	public Executor getExecutor() {
		// TODO Auto-generated method stub		
		return this.httpExecutionContext.current();
	}
}


