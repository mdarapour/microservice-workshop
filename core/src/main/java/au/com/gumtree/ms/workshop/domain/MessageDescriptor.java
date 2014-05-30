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
    }}
