package hms;

import java.util.Scanner;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.GenericApplicationContext;

import hms.kafka.provider.IProcessingService;

@SpringBootApplication(scanBasePackages = {"hms"})
public class ProcessorApp implements CommandLineRunner{
	public static void main(String[] args) {
		SpringApplication.run(ProcessorApp.class, args);
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
		System.out.println("Processor started, type 'exit' to stop");
		Scanner sc = new Scanner(System.in);
		String cmd=sc.nextLine();
		while(!cmd.equals("exit")) {	
			cmd=sc.nextLine();
		}
		sc.close();
		this.context.getBean(IProcessingService.class).close();
		//SpringApplication.exit(this.appContext);
	}
}