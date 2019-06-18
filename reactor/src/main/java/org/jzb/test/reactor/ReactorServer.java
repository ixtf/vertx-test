package org.jzb.test.reactor;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.JmongoOptions;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.stream.IntStream;

/**
 * @author jzb 2019-06-08
 */
public class ReactorServer extends AbstractVerticle {
    @Override
    public void start(Future<Void> future) throws Exception {
        final Router router = Router.router(vertx);
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

    public static final Jmongo jmongo = new Jmongo(new JmongoOptions() {
        private final MongoClient mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyConnectionString(new ConnectionString("mongodb://192.168.0.38"))
//                        .applyToClusterSettings(builder -> builder.maxWaitQueueSize(1000))
                        .build()
        );

        @Override
        public MongoClient client() {
            return mongoClient;
        }

        @Override
        public String dbName() {
            return "mes-auto";
        }
    });
    public static final JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "192.168.0.38");

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(ReactorServer.class.getName());
        final DeploymentOptions deploymentOptions = new DeploymentOptions().setWorker(true);
        vertx.deployVerticle(BackendVerticle.class.getName(), deploymentOptions);
    }
}
