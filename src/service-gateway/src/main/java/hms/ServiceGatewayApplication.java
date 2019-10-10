package hms;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringBootApplication(scanBasePackages = {"hms"})
@EnableAsync
@EnableWebMvc
public class ServiceGatewayApplication {
    @Autowired
    private GenericWebApplicationContext context;
        
	public static void main(String[] args) {
		SpringApplication.run(ServiceGatewayApplication.class, args);
	}
	
	@PostConstruct
	public void registerBeens() {
		(new InMemMicroserviceMode()).registerBeans(context);
	}
}
