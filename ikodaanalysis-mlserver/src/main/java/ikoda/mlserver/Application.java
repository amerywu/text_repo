package ikoda.mlserver;

import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
    	
    	
    	
    	
        SpringApplication.run(Application.class, args);
    }
    
    
    @PreDestroy
    public void tearDown() {
       // some destroy code
    }

}
