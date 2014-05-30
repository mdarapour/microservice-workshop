package au.com.gumtree.ms.workshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.spring.context.config.EnableReactor;

/**
 * @author mdarapour
 */
@Configuration
@EnableAutoConfiguration
@ConfigurationProperties
@ComponentScan
@EnableReactor
public class BootMailServer {

    public static void main(String... args) throws InterruptedException {
        SpringApplication.run(BootMailServer.class, args);
    }
}
