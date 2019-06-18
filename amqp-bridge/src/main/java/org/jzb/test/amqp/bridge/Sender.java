package org.jzb.test.amqp.bridge;

import io.reactivex.Completable;
import io.vertx.amqpbridge.AmqpConstants;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.amqpbridge.AmqpBridge;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.MessageProducer;

/**
 * @author jzb 2019-06-01
 */
public class Sender extends AbstractVerticle {
    public static final String AMQP_HOST = "192.168.0.38";
    private int count = 1;
    private AmqpBridge bridge;

    @Override
    public Completable rxStart() {
        bridge = AmqpBridge.create(vertx);
        return bridge.rxStart(AMQP_HOST, 15672,"admin","tomking").doOnSuccess(it -> {
            final MessageProducer<Object> producer = bridge.createProducer("vertx-test:amqp-bridge:Receiver");
            final JsonObject amqpMsgPayload = new JsonObject();
            amqpMsgPayload.put(AmqpConstants.BODY, "myStringContent" + count);
            producer.send(amqpMsgPayload);
            System.out.println("Sent message: " + count++);
        }).ignoreElement();
    }

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(Sender.class.getName());
        vertx.deployVerticle(Receiver.class.getName());
    }
}
