package org.jzb.test.resteasy;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.lettuce.core.RedisClient;
import io.vertx.reactivex.core.Vertx;

/**
 * @author jzb 2019-06-07
 */
public class GuiceModule extends AbstractModule {
    private final Vertx vertx;

    public GuiceModule(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    protected void configure() {
        super.configure();
    }

    @Provides
    @Singleton
    private RedisClient RedisClient() {
//        final JsonObject config = new JsonObject()
//                .put("host", "192.168.0.38");
//        return RedisClient.create(vertx, config);
//        final JsonObject config = new JsonObject()
//                .put("host", "192.168.0.38");
        return RedisClient.create("redis://192.168.0.38");
    }

}
