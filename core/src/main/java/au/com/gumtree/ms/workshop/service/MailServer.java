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
import java.util.function.Consumer;
import java.util.function.Predicate;

import static au.com.gumtree.ms.workshop.util.MessageGenerator.ifSet;


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
        // TODO update these
        ifSet(incoming, in -> in.getCc() != null, in -> {
            try {
                helper.setCc(in.getCc());
            } catch (MessagingException e) {
                LOG.error("Could not set message CC", e);
            }
        });
        ifSet(incoming, in -> in.getBcc() != null, in -> {
            try {
                helper.setCc(in.getBcc());
            } catch (MessagingException e) {
                LOG.error("Could not set message CC", e);
            }
        });
        ifSet(incoming, in -> in.getSubject() != null, in -> {
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
