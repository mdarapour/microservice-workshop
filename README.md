#Microservice Workshop
=====================

##Topic 1 : Microservice Architecture
    * What is it?
    * Microservice vs SOA vs Monolithic
    * How can we use it?
##Topic 2 : Spring Boot
    * What is it?
    * How can it help?
##Topic 3 : Demo 1 (Echo service) 
    Step 1 : GET
    Step 2 : POST
    Step 3 : DELETE
    Step 4 : PUT


###Step 1 : update the User class 

<pre>
package au.com.gumtree.ms.workshop.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author mdarapour
 */
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long   id;
    @Column(nullable = false)
    private String mail;
    @Column
    private Long   messageCount;

    protected User() {
    }

    public User(String mail) {
        this.mail = mail;
    }

    public Long getId() {
        return id;
    }

    public String getMail() {
        return mail;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public User setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
        return this;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + mail + '\'' +
                ", messageCount=" + messageCount +
                '}';
    }
}
</pre>

###Step 2 : update the schema (import.sql)
<pre>
insert into user(id, mail, message_count) values (1, 'mdarapour@ebay.com', 0)
</pre>

###Step 3 : Add MessageDescriptor to the core module
<pre>
package au.com.gumtree.ms.workshop.domain;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Created by mdarapour on 23/05/14.
 */
public class MessageDescriptor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageDescriptor.class);

    private String subject;
    private String from;
    private String[] to;
    private Optional<String[]> cc;
    private Optional<String[]> bcc;
    private String body;

    private MessageDescriptor(Builder builder) {
        this.setSubject(builder.subject);
        this.to = builder.to.clone();
        this.from = builder.from;
        this.body = builder.body;
        this.cc = builder.cc.map(strings -> strings.clone());
        this.bcc = builder.bcc.map(strings -> strings.clone());
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = Strings.nullToEmpty(subject);
        this.subject = subject.replaceAll("[\n\r]","").trim();
    }

    public String getFrom() {
        return from;
    }

    public String[] getTo() {
        return to;
    }

    public String[] getCc() {
        return cc.orElse(null);
    }

    public void setCc(String[] cc) {
        this.cc = Optional.ofNullable(cc);
    }

    public String[] getBcc() {
        return bcc.orElse(null);
    }

    public void setBcc(String[] bcc) {
        this.bcc = Optional.ofNullable(bcc);
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "MessageDescriptor{" +
                "subject='" + subject + '\'' +
                ", from='" + from + '\'' +
                ", to=" + Arrays.toString(to) +
                ", cc=" + (cc.orElse(null)) +
                ", bcc=" + (bcc.orElse(null)) +
                ", body=" + body +
                '}';
    }

    public static class Builder {
        private final String subject;
        private final String[] to;
        private final String from;
        private final String body;
        private Optional<String[]> cc = Optional.empty();;
        private Optional<String[]> bcc = Optional.empty();;

        public Builder(String subject, String to, String from, String body) {
            this(subject, new String[] {to}, from, body);
        }


        public Builder(String subject, String[] to, String from, String body) {
            LOG.info("Creating a message from [{}] & [{}]", subject, to);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(subject));
            Preconditions.checkArgument(Objects.nonNull(to) && to.length > 0);
            this.subject = subject.trim();
            this.to = to.clone();
            this.from = from;
            this.body = body;
        }

        public Builder cc(String[] cc) {
            this.cc = Optional.ofNullable(cc);
            return this;
        }

        public Builder bcc(String[] bcc) {
            this.bcc = Optional.ofNullable(bcc);
            return this;
        }

        public MessageDescriptor build() {
            return new MessageDescriptor(this);
        }
    }
}
</pre>

###Step 4 : Add MessageGenerator to the core module
<pre>
package au.com.gumtree.ms.workshop.util;

import au.com.gumtree.ms.workshop.domain.MessageDescriptor;

import javax.mail.MessagingException;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * @author mdarapour
 */
public class MessageGenerator {
    private static final Random   RANDOM  = new Random();
    private static final String   CHARS   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String   DOMAIN  = "@DOMAIN.COM";
    private static final String[] SYMBOLS = new String[1000];
    private static final int      LEN     = CHARS.length();

    static {
        for (int i = 0; i < SYMBOLS.length; i++) {
            SYMBOLS[i] = nextSymbol();
        }
    }

    private static String nextSymbol() {
        char[] chars = new char[4];
        for (int i = 0; i < 4; i++) {
            chars[i] = CHARS.charAt(RANDOM.nextInt(LEN));
        }
        return new String(chars);
    }

    public static MessageDescriptor buildMessage(String subject, String to, String from, String body) {
        return new MessageDescriptor.Builder(subject, to, from, body).build();
    }

    public static MessageDescriptor nextMessage() {
        return new MessageDescriptor.Builder(nextString(),
                nextMail(),
                nextMail(),
                nextString()).build();
    }

    public static String nextMail() {
        return nextString().concat(DOMAIN);
    }

    public static String nextString() {
        return SYMBOLS[RANDOM.nextInt(SYMBOLS.length)];
    }

    public static <T> void ifThen(T t, Predicate<T> predicate, Consumer<T> consumer) throws MessagingException {
        if(predicate.test(t))
            consumer.accept(t);
    }
}
</pre>

###Step 5 : Add MailServer to the core module
<pre>
package au.com.gumtree.ms.workshop.service;

import au.com.gumtree.ms.workshop.domain.MessageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static au.com.gumtree.ms.workshop.util.MessageGenerator.ifThen;


/**
 * Created by mdarapour on 23/05/14.
 */
public class MailServer {
    private static final Logger LOG       = LoggerFactory.getLogger(MailServer.class);

    private final BlockingQueue<MessageDescriptor> messages = new LinkedTransferQueue<>();
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final JavaMailSender mailer = new JavaMailSenderImpl();

    private final Thread queueDrain = new Thread(() -> {
        while(active.get()) {
            try {
                // Pull Mails off the queue and process them
                messages.poll(100, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    });

    public MailServer() {
        queueDrain.start();
    }

    public MimeMessage execute(MessageDescriptor incoming) {
        try {
            return convert(incoming);
        } catch (MessagingException e) {
            LOG.error("Could not convert incoming message", e);
            return null;
        }
    }

    public void stop() {
        this.active.set(false);
    }

    private MimeMessage convert(MessageDescriptor incoming) throws MessagingException {
        MimeMessage message = mailer.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

        helper.setFrom(incoming.getFrom());
        helper.setTo(incoming.getTo());
        ifThen(incoming, in -> in.getCc() != null, in -> {
            try {
                helper.setCc(in.getCc());
            } catch (MessagingException e) {
                LOG.error("Could not set message CC", e);
            }
        });
        ifThen(incoming, in -> in.getBcc() != null, in -> {
            try {
                helper.setCc(in.getBcc());
            } catch (MessagingException e) {
                LOG.error("Could not set message CC", e);
            }
        });
        ifThen(incoming, in -> in.getSubject() != null, in -> {
            try {
                helper.setSubject(in.getSubject());
            } catch (MessagingException e) {
                LOG.error("Could not set message Subject", e);
            }
        });

        helper.setText(incoming.getBody());

        message.saveChanges();
        return message;
    }


}
</pre>


###Step 6 : Update BootMailServer
<pre>
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
</pre>

###Step 7 : Inject the reactor in your controller
<pre>
    private final UserRepository users;
    private final Reactor reactor;

    @Autowired
    public MailController(UserRepository users,
                          Reactor reactor) {
        this.users = users;
        this.reactor = reactor;
    }
</pre>

###Step 8 : Add the sent mail method
<pre>
    @RequestMapping(value = "/user/{id}/to/{mail}/{subject}/{body}", method = RequestMethod.PUT, produces = "text/plain")
    @ResponseBody
    public String send(@PathVariable Long id, @PathVariable String mail, @PathVariable String subject, @PathVariable String body) {
        // Retrieve user by id
        User user = users.findOne(id);
        if(Objects.isNull(user))
            return "User id '"+id+"' not found.";

        // Send un-registration notification
        MessageDescriptor message = MessageGenerator.buildMessage(subject,
                mail,
                user.getMail(),
                body);
        reactor.notify("mail.execute", Event.wrap(message));

        // Update message count
        user = users.save(user.setMessageCount(user.getMessageCount() + 1));

        // Return result
        return "User " + user.getMail() + " has sent a message.";
    }
</pre>

###Step 9  : Run the boot app
###Step 10 : Send a message
<pre>
curl -X PUT -H "Content-Type: application/json" http://localhost:8080/user/1/to/buyer@email.com/hi/hello
</pre>