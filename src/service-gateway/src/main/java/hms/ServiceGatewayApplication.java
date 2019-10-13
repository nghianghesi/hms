package hms;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication(scanBasePackages = {"hms"})
@EnableAsync
@EnableWebMvc
public class ServiceGatewayApplication{
    @Autowired
    private GenericWebApplicationContext context;
        
	public static void main(String[] args) {
		SpringApplication.run(ServiceGatewayApplication.class, args);
	}
	
	@PostConstruct
	public void registerBeens() {
		(new InMemMicroserviceMode()).registerBeans(context);

		this.context.getBeansOfType(IProcessingService.class).values().forEach(
				(s) -> {
					s.start();
				}
		);		
	}

	
	@PreDestroy
	public void onExit() {
		this.context.getBeansOfType(IProcessingService.class).forEach(
				(n, s) -> {try {
					s.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}}
		);
	}	
}
