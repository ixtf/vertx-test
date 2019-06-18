package org.jzb.test.jspare.cdi;

import io.vertx.core.Vertx;

/**
 * @author jzb 2019-06-05
 */
public class MyAppTest {
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(MyApp.class.getName());
    }
}

