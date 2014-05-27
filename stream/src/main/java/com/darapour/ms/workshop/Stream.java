package com.darapour.ms.workshop;

import com.mdarapour.ms.workshop.model.MessageDescriptor;
import com.mdarapour.ms.workshop.service.MailServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.composable.Deferred;
import reactor.core.composable.spec.Streams;

import javax.mail.internet.MimeMessage;
import java.util.concurrent.CountDownLatch;

/**
 * Created by mdarapour on 26/05/14.
 */
public class Stream {
    private static final Logger LOG         = LoggerFactory.getLogger(Stream.class);
    private static       int    totalEmails = 10000000;

    public static void main(String[] args) {
        Environment env = new Environment();
        final MailServer server = new MailServer();
        final CountDownLatch latch = new CountDownLatch(totalEmails);

        // Rather than handling emails as events, each e is accessible via Stream.
        Deferred<MessageDescriptor, reactor.core.composable.Stream<MessageDescriptor>> mails = Streams.defer(env);

        // We compose an action to turn a MessageDescriptor into an MimeMessage by calling server.execute(Trade).
        reactor.core.composable.Stream<MimeMessage> orders = mails.compose()
                .map(server::execute)
                .consume(o -> latch.countDown());
    }
}