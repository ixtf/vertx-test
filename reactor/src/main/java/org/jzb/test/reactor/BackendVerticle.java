package org.jzb.test.reactor;

import io.reactivex.Single;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author jzb 2019-06-08
 */
public class BackendVerticle extends AbstractVerticle {
    @Override
    public void start(Future<Void> future) throws Exception {
        vertx.eventBus().<String>consumer("dyeing", reply -> {
            final String body = reply.body();
            final int delay = new Random().nextInt(5);
//            Mono.just(body).delayElement(Duration.ofSeconds(5)).subscribe(it -> System.out.println(body + " handled"));
            Single.just(body)
//                    .subscribeOn(Schedulers.single())
//                    .subscribeOn(Schedulers.single())
//                    .doOnSuccess(it -> System.out.println(it + " success"))
                    .delay(delay, TimeUnit.SECONDS)
                    .doOnSuccess(it -> System.out.println(it + " handled"))
                    .subscribe();
//                    .blockingGet();
        });
    }


}
