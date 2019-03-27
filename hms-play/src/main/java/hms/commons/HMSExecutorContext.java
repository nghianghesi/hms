package hms.commons;

import java.util.concurrent.Executor;

import javax.inject.Inject;

import hms.common.IHMSExecutorContext;
import play.libs.concurrent.HttpExecutionContext;

public class HMSExecutorContext implements IHMSExecutorContext{
	private HttpExecutionContext httpExecutionContext;

	@Inject
	public HMSExecutorContext(HttpExecutionContext ec) {
		this.httpExecutionContext = ec;
	}
	
	@Override
	public Executor getExecutor() {
		// TODO Auto-generated method stub		
		return this.httpExecutionContext.current();
	}
}


