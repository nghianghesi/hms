package hms;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.GenericApplicationContext;

import hms.kafka.provider.IProcessingService;

@SpringBootApplication(scanBasePackages = {"hms"})
public class ProcessorApp implements CommandLineRunner{
	public static void main(String[] args) {
	}

	@Autowired
	private GenericApplicationContext context;
	
	@PostConstruct
	public void registerBeans() {
		(new InMemoryMode()).registerBeans(this.context);
	}
	
	@Override
	public void run(String... args) throws Exception {
		this.context.getBean(IProcessingService.class).start();	
	}
}