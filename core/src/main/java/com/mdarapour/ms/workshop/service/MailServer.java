package com.mdarapour.ms.workshop.service;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import com.mdarapour.ms.workshop.model.MessageDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataSource;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

/**
 * Created by mdarapour on 23/05/14.
 */
public class MailServer {
    private static final Logger LOG       = LoggerFactory.getLogger(MailServer.class);

    private final BlockingQueue<MessageDescriptor> messages = new LinkedTransferQueue<>();
    private final AtomicBoolean active = new AtomicBoolean(true);
    private final TemplateParser parser;

    private static final Random   RANDOM  = new Random();
    private static final String   CHARS   = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
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
        String payload = templateParser.parse(descriptor.getTemplateDefinition().getFilename(),descriptor.getLocale(),
                descriptor.getModel());
        MimeMessage message = mailSender.createMimeMessage();
        boolean isMultipartMessage = descriptor.getAttachments() != null && descriptor.getAttachments().size() > 0;
        MimeMessageHelper helper = new MimeMessageHelper(message, isMultipartMessage, "UTF-8");

        // default from is specified through Spring configuration, overwrite it only if necessary
        if (descriptor.getFrom() != null)
            helper.setFrom(descriptor.getFrom());
        else
            helper.setFrom(defaultFrom);

        helper.setTo(descriptor.getTo());
        if (descriptor.getCc() != null) {
            helper.setCc(descriptor.getCc());
        }
        if (descriptor.getBcc() != null) {
            helper.setBcc(descriptor.getBcc());
        }
        if (descriptor.getSubject() != null) {
            helper.setSubject(descriptor.getSubject());
        }
        if (descriptor.getReplyTo() != null) {
            helper.setReplyTo(descriptor.getReplyTo());
        }

        for (String customHeader : descriptor.getCustomHeaders().keySet()) {
            String val = descriptor.getCustomHeader(customHeader);
            // encode text to make it charset aware
            try {
                val = MimeUtility.encodeText(val);
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("Could not encode Mail header Field " + customHeader + ": " + val, e);
            }
            message.addHeader(customHeader, val);
        }

        if (isMultipartMessage) {
            Set<Map.Entry<String, DataSource>> attachments = descriptor.getAttachments().entrySet();
            for (Map.Entry<String, DataSource> attachment : attachments) {
                helper.addAttachment(attachment.getKey(), attachment.getValue());
            }
        }
        // the true flag indicates that the included text is html
        helper.setText(payload, descriptor.getTemplateDefinition().isHtml());

        message.saveChanges();
        return message;
    }

    public MessageDescriptor nextMail() {
        return null;//new MessageDescriptor.Builder.(SYMBOLS[RANDOM.nextInt(SYMBOLS.length)]);
    }

    public void stop() {
        this.active.set(false);
    }
}
