package au.com.gumtree.ms.workshop;

import au.com.gumtree.ms.workshop.domain.MessageDescriptor;
import au.com.gumtree.ms.workshop.service.MailServer;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.core.composable.Stream;
import reactor.core.spec.Reactors;
import reactor.event.Event;
import reactor.net.NetServer;
import reactor.net.config.ServerSocketOptions;
import reactor.net.netty.NettyServerSocketOptions;
import reactor.net.netty.tcp.NettyTcpServer;
import reactor.net.tcp.spec.TcpServerSpec;
import reactor.spring.context.config.EnableReactor;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static reactor.event.selector.Selectors.$;

/**
 * @author mdarapour
 */
@Configuration
@EnableAutoConfiguration
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
        Reactor r = Reactors.reactor(env, Environment.THREAD_POOL);

        // Wire an event handler to execute messages
        r.on($("mail.execute"), (Event<MessageDescriptor> ev) -> {
            mailServer.execute(ev.getData());
            log.info("Executed message: {}", ev.getData());
        });

        return r;
    }

    @Bean
    public ServerSocketOptions serverSocketOptions() {
        return new NettyServerSocketOptions()
                .pipelineConfigurer(pipeline -> pipeline.addLast(new HttpServerCodec())
                        .addLast(new HttpObjectAggregator(16 * 1024 * 1024)));
    }

    @Bean
    public NetServer<FullHttpRequest, FullHttpResponse> restApi(Environment env,
                                                                ServerSocketOptions opts,
                                                                Reactor reactor,
                                                                CountDownLatch closeLatch) throws InterruptedException {
        AtomicReference<Path> thumbnail = new AtomicReference<>();

        NetServer<FullHttpRequest, FullHttpResponse> server = new TcpServerSpec<FullHttpRequest, FullHttpResponse>(NettyTcpServer.class)
                .env(env).dispatcher("sync").options(opts)
                .consume(ch -> {
                    // attach an error handler
                    ch.when(Throwable.class, ImageThumbnailerRestApi.errorHandler(ch));

                    // filter requests by URI
                    Stream<FullHttpRequest> in = ch.in();

                    // serve image thumbnail to browser
                    in.filter((FullHttpRequest req) -> ImageThumbnailerRestApi.IMG_THUMBNAIL_URI.equals(req.getUri()))
                            .consume(ImageThumbnailerRestApi.serveThumbnailImage(ch, thumbnail));

                    // take uploaded data and thumbnail it
                    in.filter((FullHttpRequest req) -> ImageThumbnailerRestApi.THUMBNAIL_REQ_URI.equals(req.getUri()))
                            .consume(ImageThumbnailerRestApi.thumbnailImage(ch, thumbnail, reactor));

                    // shutdown this demo app
                    in.filter((FullHttpRequest req) -> "/shutdown".equals(req.getUri()))
                            .consume(req -> closeLatch.countDown());
                })
                .get();

        server.start().await();

        return server;
    }

    @Bean
    public CountDownLatch closeLatch() {
        return new CountDownLatch(1);
    }

    public static void main(String... args) {
        SpringApplication.run(BootMailServer.class, args);
    }
}
