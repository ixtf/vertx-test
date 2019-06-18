package org.jzb.test.proton;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;

public class Receiver extends AbstractVerticle {

  private String address = "examples";

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(Receiver.class.getName());
  }

  @Override
  public void start() throws Exception {
    ProtonClient client = ProtonClient.create(vertx);

    client.connect(
        "localhost",
        5672,
        res -> {
          if (!res.succeeded()) {
            System.out.println("Connect failed: " + res.cause());
            return;
          }

          ProtonConnection connection = res.result();
          connection.open();

          connection
              .createReceiver(address)
              .handler(
                  (delivery, msg) -> {
                    String content = (String) ((AmqpValue) msg.getBody()).getValue();
                    System.out.println("Received message with content: " + content);

                    // By default, receivers automatically accept (and settle) the delivery
                    // when the handler returns, if no other disposition has been applied.
                    // To change this and always manage dispositions yourself, use the
                    // setAutoAccept method on the receiver.
                  })
              .open();
        });
  }
}
