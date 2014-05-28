package com.mdarapour.ms.workshop.service;

import com.mdarapour.ms.workshop.domain.MessageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;


/**
 * Created by mdarapour on 23/05/14.
 */
public class MailServer {
    private static final Logger LOG       = LoggerFactory.getLogger(MailServer.class);

    private final BlockingQueue<MessageDescriptor> messages = new LinkedTransferQueue<>();
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final JavaMailSender mailer = new JavaMailSenderImpl();

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
            return create(incoming);
        } catch (MessagingException e) {
            LOG.error("Could not convert incoming message", e);
            return null;
        }
    }

    public MessageDescriptor nextMail() {
        return new MessageDescriptor.Builder(SYMBOLS[RANDOM.nextInt(SYMBOLS.length)].concat(DOMAIN),
                SYMBOLS[RANDOM.nextInt(SYMBOLS.length)],
                SYMBOLS[RANDOM.nextInt(SYMBOLS.length)].concat(DOMAIN)).build();
    }

    public void stop() {
        this.active.set(false);
    }

    private MimeMessage create(MessageDescriptor incoming) throws MessagingException {
        MimeMessage message = mailer.createMimeMessage();
        boolean isMultipartMessage = incoming.getAttachments() != null && incoming.getAttachments().size() > 0;
        MimeMessageHelper helper = new MimeMessageHelper(message, isMultipartMessage, "UTF-8");

        helper.setFrom(incoming.getFrom());
        helper.setTo(incoming.getTo());
        // TODO update these
        if (incoming.getCc() != null) {
            helper.setCc(incoming.getCc());
        }
        if (incoming.getBcc() != null) {
            helper.setBcc(incoming.getBcc());
        }
        if (incoming.getSubject() != null) {
            helper.setSubject(incoming.getSubject());
        }

        if(!Objects.isNull(incoming.getCustomHeaders()))
            incoming.getCustomHeaders().forEach((key, value) -> {
                try {
                    value = MimeUtility.encodeText(value);
                } catch (UnsupportedEncodingException e) {
                    LOG.error("Could not encode Mail header Field " + key + ": " + value, e);
                }
                try {
                    message.addHeader(key, value);
                } catch (MessagingException e) {
                    LOG.error("Could not map Mail header Field " + key + ": " + value, e);
                }
            });

        if (isMultipartMessage) {
            Set<Map.Entry<String, DataSource>> attachments = incoming.getAttachments().entrySet();
            for (Map.Entry<String, DataSource> attachment : attachments) {
                helper.addAttachment(attachment.getKey(), attachment.getValue());
            }
        }
        // the true flag indicates that the included text is html
        helper.setText(incoming.getPayload());

        message.saveChanges();
        return message;
    }
}
