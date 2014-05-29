package au.com.gumtree.ms.workshop;

import au.com.gumtree.ms.workshop.domain.MessageDescriptor;
import au.com.gumtree.ms.workshop.service.MailServer;
import au.com.gumtree.ms.workshop.util.MessageGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.composable.Deferred;
import reactor.core.composable.spec.Streams;

import javax.mail.internet.MimeMessage;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by mdarapour on 26/05/14.
 */
public class Stream {
    private static final Logger LOG         = LoggerFactory.getLogger(Stream.class);
    private static       int totalMessages = 10000000;

    private static long startTime;

    public static void main(String[] args) throws InterruptedException {
        Environment env = new Environment();
        final MailServer server = new MailServer();
        final CountDownLatch latch = new CountDownLatch(totalMessages);

        // Rather than handling emails as events, each e is accessible via Stream.
        Deferred<MessageDescriptor, reactor.core.composable.Stream<MessageDescriptor>> mails = Streams.defer(env);

        // We compose an action to turn a MessageDescriptor into an MimeMessage by calling server.execute(MessageDescriptor).
        reactor.core.composable.Stream<MimeMessage> messages = mails.compose()
                .map(server::execute)
                .consume(o -> latch.countDown());

        // Start a throughput timer.
        startTimer();

        // Publish one event per message.
        for (int i = 0; i < totalMessages; i++) {
            // Pull next randomly-generated MessageDescriptor from server into the Composable,
            // Notify the Composable this MessageDescriptor is ready to be executed
            mails.accept(MessageGenerator.nextMessage());
        }

        // Wait for all messages to pass through
        latch.await(30, TimeUnit.SECONDS);

        // Stop throughput timer and output metrics.
        endTimer();

        server.stop();
    }

    private static void startTimer() {
        LOG.info("Starting throughput test with {} messages...", totalMessages);
        startTime = System.currentTimeMillis();
    }

    private static void endTimer() throws InterruptedException {
        long endTime = System.currentTimeMillis();
        double elapsed = endTime - startTime;
        double throughput = totalMessages / (elapsed / 1000);

        LOG.info("Executed {} messages/sec in {}ms", (int) throughput, (int) elapsed);
    }
}