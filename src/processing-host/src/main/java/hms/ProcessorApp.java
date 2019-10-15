package hms;

import java.io.IOException;
import java.util.Scanner;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.stereotype.Component;

@SpringBootApplication(scanBasePackages = {"hms"})
@Component
public class ProcessorApp implements CommandLineRunner{
	public static void main(String[] args) {
		SpringApplication.run(ProcessorApp.class, args).close();
	}

	@Autowired
	private GenericApplicationContext context;
	
	@PostConstruct
	public void registerBeans() {
		(new InMemoryMode()).registerBeans(this.context);
	}
	
	@Override
	public void run(String... args) throws Exception {
		this.context.getBeansOfType(IProcessingService.class).values().forEach(
				(s) -> {s.start();}
		);	
		System.out.println("Processor started, type 'exit' to stop");
		Scanner sc = new Scanner(System.in);
		String cmd=sc.nextLine();
		while(!cmd.equals("exit")) {	
			cmd=sc.nextLine();
		}
		sc.close();
	}
	
	@PreDestroy
	public void onExit() {
		this.context.getBeansOfType(IProcessingService.class).values().forEach(
				(s) -> {try {
					s.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}}
		);
	}	
}