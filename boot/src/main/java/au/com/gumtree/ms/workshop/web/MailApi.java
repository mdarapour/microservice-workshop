package au.com.gumtree.ms.workshop.web;

import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.function.Consumer;
import reactor.net.NetChannel;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author mdarapour
 */
public class MailApi {

    public static Consumer<FullHttpRequest> save(NetChannel<FullHttpRequest, FullHttpResponse> channel,
                                                           AtomicReference<Path> thumbnail,
                                                           Reactor reactor) {
        return req -> {
            if (req.getMethod() != HttpMethod.POST) {
                channel.send(badRequest(req.getMethod() + " not supported for this URI"));
                return;
            }

            // write to db
            Path imgIn = null;
            try {
                imgIn = readUpload(req.content());
            } catch (IOException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            // Asynchronously thumbnail the image to 250px on the long side
            reactor.sendAndReceive("thumbnail", Event.wrap(imgIn), ev -> {
                thumbnail.set(ev.getData());
                channel.send(redirect());
            });
        };
    }

   /*
    * Create an HTTP 400 bad request response.
    */
    public static FullHttpResponse badRequest(String msg) {
        DefaultFullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
        resp.content().writeBytes(msg.getBytes());
        resp.headers().set(CONTENT_TYPE, "text/plain");
        resp.headers().set(CONTENT_LENGTH, resp.content().readableBytes());
        return resp;
    }
}
