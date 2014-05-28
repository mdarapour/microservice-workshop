package com.mdarapour.ms.workshop.domain;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataSource;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by mdarapour on 23/05/14.
 */
public class MessageDescriptor {
    private static final Logger LOG = LoggerFactory.getLogger(MessageDescriptor.class);

    private static final String CUSTOM_HEADER_PREFIX="X-";
    private String subject;
    private String from;
    private String[] to;
    private Optional<String[]> cc;
    private Optional<String[]> bcc;
    private Optional<Map<String,String>> customHeaders;
    private Optional<Map<String, DataSource>> attachments;
    private String payload;

    public MessageDescriptor() {
        customHeaders = Optional.of(Maps.newHashMap());
    }

    private MessageDescriptor(Builder builder) {
        this.setSubject(builder.subject);
        this.to = builder.to.clone();
        this.from = builder.from;
        this.cc = builder.cc.map(strings -> strings.clone());
        this.bcc = builder.bcc.map(strings -> strings.clone());
        this.payload = builder.body.orElse(null);
        this.customHeaders = builder.customHeaders.map(stringStringMap -> Collections.unmodifiableMap(stringStringMap));
        this.attachments = builder.attachments.map(stringDataSourceMap -> Collections.unmodifiableMap(stringDataSourceMap));
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

    public void setFrom(String from) {
        this.from = from;
    }

    public String[] getTo() {
        return to;
    }

    public void setTo(String[] to) {
        this.to = to;
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

    public Map<String, DataSource> getAttachments() {
        return attachments.orElse(null);
    }

    public void addAttachment(String name, DataSource dataSource) {
        attachments.orElse(Maps.newHashMap()).put(name, dataSource);
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public void setCustomHeader(String name, String value) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        checkArgument(!name.startsWith(CUSTOM_HEADER_PREFIX), "Custom headers have to start with 'X-'");
        customHeaders.orElse(Maps.newHashMap()).put(name, value);
    }

    public String getCustomHeader(String name) {
        if (!name.startsWith(CUSTOM_HEADER_PREFIX)) {
            LOG.warn("Trying to get a custom header without a 'X-' prefix: {}", name);
        }
        return customHeaders.isPresent() ? customHeaders.get().get(name) : null;
    }

    public void removeCustomHeader(String name) {
        Objects.requireNonNull(name);
        checkArgument(!name.startsWith(CUSTOM_HEADER_PREFIX), "Custom headers have to start with 'X-'");
        customHeaders.ifPresent(strings -> strings.remove(name));
    }

    public boolean hasCustomHeader(String customHeader) {
        if (!customHeader.startsWith(CUSTOM_HEADER_PREFIX)) {
            LOG.warn("Trying to check a custom header without a 'X-' prefix: {}", customHeader);
        }
        return customHeaders.isPresent() ? customHeaders.get().containsKey(customHeader) : false;
    }

    public Map<String, String> getCustomHeaders() {
        return customHeaders.orElse(null);
    }

    @Override
    public String toString() {
        return "MessageDescriptor{" +
                "subject='" + subject + '\'' +
                ", from='" + from + '\'' +
                ", to=" + to +
                ", cc=" + (cc.orElse(null)) +
                ", bcc=" + (bcc.orElse(null)) +
                ", customHeaders=" + customHeaders +
                ", attachments=" + attachments +
                ", body=" + payload +
                '}';
    }

    public static class Builder {
        private final String subject;
        private final String[] to;
        private String from;
        private Optional<String> body = Optional.empty();
        private Optional<String[]> cc = Optional.empty();;
        private Optional<String[]> bcc = Optional.empty();;
        private Optional<Map<String,String>> customHeaders = Optional.empty();
        private Optional<Map<String, DataSource>> attachments = Optional.empty();

        public Builder(String subject, String to, String from) {
            this(subject, new String[] {to}, from);
        }


        public Builder(String subject, String[] to, String from) {
            LOG.info("Creating a message from ["+subject+"] & ["+to+"]");
            checkArgument(!Strings.isNullOrEmpty(subject));
            checkArgument(Objects.nonNull(to) && to.length > 0);
            this.subject = subject.trim();
            this.to = to.clone();
            this.from = from;
        }

        public Builder from(String from) {
            this.from = from;
            return this;
        }

        public Builder cc(String[] cc) {
            this.cc = Optional.ofNullable(cc);
            return this;
        }

        public Builder bcc(String[] bcc) {
            this.bcc = Optional.ofNullable(bcc);
            return this;
        }

        public Builder attachment(String name, DataSource dataSource) {
            checkArgument(!Strings.isNullOrEmpty(name));
            checkArgument(Objects.nonNull(dataSource));
            attachments.orElse(Maps.newHashMap()).put(name, dataSource);
            return this;
        }

        public Builder attachments(Map<String, DataSource> attachments) {
            attachments.entrySet().stream().forEach(entry -> attachment(entry.getKey(), entry.getValue()));
            return this;
        }

        public Builder customHeader(String name, String value) {
            checkArgument(!Strings.isNullOrEmpty(name));
            checkArgument(!Strings.isNullOrEmpty(value));
            checkArgument(name.startsWith(CUSTOM_HEADER_PREFIX), "Custom headers have to start with " + CUSTOM_HEADER_PREFIX);
            customHeaders.orElse(Maps.newHashMap()).put(name, value);
            return this;
        }

        public Builder body(String body) {
            this.body = Optional.ofNullable(body);
            return this;
        }

        public Builder customHeaders(Map<String, String> headers) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                customHeader(entry.getKey(), entry.getValue());
            }
            return this;
        }

        public MessageDescriptor build() {
            return new MessageDescriptor(this);
        }
    }}
