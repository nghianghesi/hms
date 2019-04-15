package hms;

import java.util.List;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.io.Closeable;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;

import play.inject.ApplicationLifecycle;
public abstract class KafkaProcessingProvider<T extends Closeable> implements Provider<T> {
	protected Injector injector;
	List<Closeable> instances = new ArrayList<>();
	@Inject()
	public KafkaProcessingProvider(ApplicationLifecycle app, Injector injector) {
		this.injector = injector;
		app.addStopHook(()->{
			return CompletableFuture.runAsync(()->{
				for(Closeable p: instances) {
					try {
						p.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			});
		});
	}
	@Override
	public T get() {
		T t = this.internalGet();
		instances.add(t);
		return t;
	}

	protected abstract T internalGet();
}
