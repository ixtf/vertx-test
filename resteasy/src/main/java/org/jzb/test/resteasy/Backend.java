package org.jzb.test.resteasy;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisFuture;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.reactive.RedisReactiveCommands;
import io.lettuce.core.api.sync.RedisCommands;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.core.AbstractVerticle;
import lombok.Cleanup;

import java.util.Map;

import static org.jzb.test.resteasy.Server.INJECTOR;

/**
 * @author jzb 2019-06-04
 */
public class Backend extends AbstractVerticle {
    private RedisClient redisClient;

    @Override
    public void start() throws Exception {
        redisClient = INJECTOR.getInstance(RedisClient.class);

        vertx.eventBus().<String>consumer("backend", reply -> {
            final StatefulRedisConnection<String, String> connect = redisClient.connect();
            final RedisAsyncCommands<String, String> async = connect.async();
            final RedisFuture<Map<String, String>> hgetall = async.hgetall("Dyeing-First[C7-104-6-5ca4a31cf3d60c149847469c]");
            hgetall.whenComplete((map, err) -> {
                if (err != null) {
                    reply.fail(400, err.getLocalizedMessage());
                } else {
                    final JsonObject jsonObject = JsonObject.mapFrom(map).put("backend", "backend");
                    reply.reply(jsonObject.encode());
//                    reply.reply(new UserPrincipal("test"));
                }
            });
        });

        vertx.eventBus().<String>localConsumer("backend-async", reply -> {
            final StatefulRedisConnection<String, String> connect = redisClient.connect();
            final RedisAsyncCommands<String, String> async = connect.async();
            final RedisFuture<Map<String, String>> hgetall = async.hgetall("Dyeing-First[C7-104-6-5ca4a31cf3d60c149847469c]");
            hgetall.whenComplete((map, err) -> {
                if (err != null) {
                    reply.fail(400, err.getLocalizedMessage());
                } else {
                    final JsonObject jsonObject = JsonObject.mapFrom(map).put("backend", "backend-async");
                    reply.reply(jsonObject.encode());
//                    reply.reply(new UserPrincipal("test"));
                }
            });
        });

        vertx.eventBus().<String>consumer("backend-sync", reply -> {
            @Cleanup final StatefulRedisConnection<String, String> connect = redisClient.connect();
            final RedisCommands<String, String> sync = connect.sync();
            final Map<String, String> map = sync.hgetall("Dyeing-First[C7-104-6-5ca4a31cf3d60c149847469c]");
            final JsonObject jsonObject = JsonObject.mapFrom(map).put("backend", "backend-sync");
            reply.reply(jsonObject.encode());
        });

        vertx.eventBus().<String>consumer("backend-reactive", reply -> {
            final RedisReactiveCommands<String, String> reactive = redisClient.connect().reactive();
            reactive.hgetall("Dyeing-First[C7-104-6-5ca4a31cf3d60c149847469c]")
                    .subscribe(map -> {
                        final JsonObject jsonObject = JsonObject.mapFrom(map).put("backend", "backend-sync");
                        reply.reply(jsonObject.encode());
                    }, err -> reply.fail(400, err.getLocalizedMessage()));
        });
    }
}
