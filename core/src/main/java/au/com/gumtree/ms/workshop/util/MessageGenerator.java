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

    public static <T> void ifSet(T t, Predicate<T> predicate, Consumer<T> consumer) throws MessagingException {
        if(predicate.test(t))
            consumer.accept(t);
    }
}
