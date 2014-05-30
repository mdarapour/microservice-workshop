package au.com.gumtree.ms.workshop;

import au.com.gumtree.ms.workshop.domain.MessageDescriptor;
import au.com.gumtree.ms.workshop.service.MailServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.spring.context.config.EnableReactor;
import static reactor.event.selector.Selectors.$;

/**
 * @author mdarapour
 */
@Configuration
@EnableAutoConfiguration
@ConfigurationProperties
@ComponentScan
@EnableReactor
public class BootMailServer {
    @Bean
    public MailServer mailServer() {
        return new MailServer();
    }

    @Bean
    public Reactor reactor(Environment env, MailServer mailServer) {
        Logger log = LoggerFactory.getLogger("mail.server");
        Reactor r = Reactors.reactor(env);

        // Wire an event handler to execute messages
        r.on($("mail.execute"), (Event<MessageDescriptor> ev) -> {
            mailServer.execute(ev.getData());
            log.info("Executed message: {}", ev.getData());
        });

        return r;
    }

    public static void main(String... args) throws InterruptedException {
        SpringApplication.run(BootMailServer.class, args);
    }
}
