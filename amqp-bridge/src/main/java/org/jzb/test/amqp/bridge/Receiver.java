package org.jzb.test.amqp.bridge;

import io.reactivex.Completable;
import io.vertx.amqpbridge.AmqpConstants;
import io.vertx.core.json.JsonObject;
import io.vertx.reactivex.amqpbridge.AmqpBridge;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.eventbus.MessageConsumer;

import static org.jzb.test.amqp.bridge.Sender.AMQP_HOST;

/**
 * @author jzb 2019-06-01
 */
public class Receiver extends AbstractVerticle {
    private AmqpBridge bridge;

    @Override
    public Completable rxStart() {
        bridge = AmqpBridge.create(vertx);
        return bridge.rxStart(AMQP_HOST, 15672, "admin", "tomking").flatMapCompletable(it -> {
            final MessageConsumer<JsonObject> consumer = bridge.createConsumer("vertx-test:amqp-bridge:Receiver");
            return consumer.handler(vertxMsg -> {
                final JsonObject amqpMsgPayload = vertxMsg.body();
                final Object amqpBody = amqpMsgPayload.getValue(AmqpConstants.BODY);
                System.out.println("Received a message with body: " + amqpBody);
            }).rxCompletionHandler();
        });
    }
}
