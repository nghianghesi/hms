package hms.commons;

import com.google.inject.AbstractModule;

import hms.common.IHMSExecutorContext;


public class HMSExecutorModule extends AbstractModule {
	@Override
	protected void configure() {
        bind(IHMSExecutorContext.class).to(HMSExecutorContext.class);        
	}
}