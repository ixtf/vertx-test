package org.jzb.test.jspare.cdi;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;

/**
 * @author jzb 2019-06-05
 */
public class MyAppTest {
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx(new VertxOptions().setEventLoopPoolSize(2));
//        vertx.runOnContext(it -> {
//        });
//        vertx.deployVerticle(MyApp.class.getName());
        for (int i = 0; i < 20; i++) {
            int index = i;
            vertx.setTimer(1, timerID -> {
                final Object test = Vertx.currentContext().get("test");
                System.out.println("test=" + test);
                System.out.println(index + ":" + Thread.currentThread());
                Vertx.currentContext().put("test", 1);
            });
        }
    }
}

