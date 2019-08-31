package org.jzb.test.reactor;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.persistence.mongo.Jmongo;
import io.reactivex.Flowable;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.stream.IntStream;

import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM;

/**
 * @author jzb 2019-06-08
 */
public class ReactorServer extends AbstractVerticle {
    public static final Jmongo jmongo = Jmongo.of(JmongoDev.class);

    private void handle(RoutingContext rc, AsyncResult<Message<String>> ar) {
        if (ar.succeeded()) {
            final String body = ar.result().body();
            if (J.nonBlank(body)) {
                rc.response().end(body);
            } else {
                rc.response().end();
            }
        } else {
            rc.fail(ar.cause());
        }
    }

    @Override
    public void start(Future<Void> future) throws Exception {
        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().handler(ResponseContentTypeHandler.create());
        router.route().handler(CookieHandler.create());

//        System.out.println(System.getProperty("java.io.tmpdir"));
//        router.route().handler(BodyHandler.create().setUploadsDirectory(System.getProperty("java.io.tmpdir")));

        router.post("/uploads").produces(APPLICATION_OCTET_STREAM).handler(BodyHandler.create().setUploadsDirectory(System.getProperty("java.io.tmpdir"))).handler(rc -> {
            final io.vertx.reactivex.core.Vertx vertx = io.vertx.reactivex.core.Vertx.newInstance(this.vertx);
            vertx.rxExecuteBlocking(f -> Flowable.fromIterable(rc.fileUploads())
                    .map(FileUpload::uploadedFileName)
                    .flatMapSingle(vertx.fileSystem()::rxReadFile)
                    .map(io.vertx.reactivex.core.buffer.Buffer::getBytes)
                    .toList()
                    .map(list -> {
                        final JsonArray array = new JsonArray();
                        list.stream().forEach(array::add);
                        return array;
                    })
                    .subscribe(f::complete, f::fail), false)
                    .flatMapSingle(it -> vertx.eventBus().<byte[]>rxSend("mes-auto:report:statisticReport:combines", it))
                    .subscribe(reply -> {
                        final HttpServerResponse response = rc.response();
                        final io.vertx.reactivex.core.MultiMap headers = reply.headers();
                        headers.entries().forEach(it -> response.putHeader(it.getKey(), it.getValue()));
                        response.end(Buffer.buffer(reply.body()));
                    }, rc::fail);
        });

        router.get("/test").handler(rc -> {
            IntStream.rangeClosed(1, 10).mapToObj(it -> "id:" + it).forEach(it -> vertx.eventBus().send("dyeing", it));
            rc.response().end();

//            vertx.eventBus().<String>send("", null, ar -> handle(rc, ar));
//            Flux.from(jmongo.collection("T_SilkCar").find())
//                    .map(Document::toJson)
//                    .map(JsonObject::new)
//                    .collectList()
//                    .map(JsonArray::new)
//                    .map(JsonArray::encode)
//                    .doOnNext(it -> {
//                        System.out.println(Thread.currentThread());
//                    })
//                    .delayElement(Duration.ofSeconds(35))
//                    .subscribe(rc.response()::end, rc::fail);
//            System.out.println("test");
        });

        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080, ar -> future.handle(ar.mapEmpty()));
    }
    public static final JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "192.168.0.38");

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(ReactorServer.class.getName());
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
        vertx.deployVerticle(BackendVerticle.class.getName(), deploymentOptions);
    }
}
