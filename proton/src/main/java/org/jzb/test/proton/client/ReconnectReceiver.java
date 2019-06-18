package org.jzb.test.proton.client;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.proton.ProtonClient;
import io.vertx.proton.ProtonConnection;
import io.vertx.proton.ProtonReceiver;
import java.time.Instant;
import org.apache.qpid.proton.amqp.messaging.AmqpValue;

public class ReconnectReceiver extends AbstractVerticle {

  private static final String ADDRESS = "examples";

  private ConnectionControl connectionControl =
      new ConnectionControl("localhost:5672", "localhost:15672");

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    final Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(ReconnectReceiver.class.getName());
  }

  @Override
  public void start() throws Exception {
    ProtonClient client = ProtonClient.create(vertx);

    setupConnection(client);
  }

  private void setupConnection(ProtonClient client) {
    String peer = connectionControl.nextPeer();

    System.out.println("Attempting to connect to peer: " + peer);

    String[] peerDetails = peer.split(":");
    client.connect(
        peerDetails[0],
        Integer.parseInt(peerDetails[1]),
        res -> {
          if (!res.succeeded()) {
            System.out.println("Connect failed: " + res.cause());

            handleConnectionFailure(client, null, false);
            return;
          }

          ProtonConnection connection = res.result();

          connection
              .openHandler(
                  x -> {
                    connectionControl.connected();
                    setupReceiver(connection);
                  })
              .closeHandler(
                  x -> {
                    handleConnectionFailure(client, connection, true);
                  })
              .disconnectHandler(
                  x -> {
                    handleConnectionFailure(client, connection, false);
                  })
              .open();
        });
  }

  private void handleConnectionFailure(
      ProtonClient client, ProtonConnection oldConnection, boolean remoteClose) {
    try {
      if (oldConnection != null) {
        oldConnection.closeHandler(null);
        oldConnection.disconnectHandler(null);

        if (remoteClose) {
          oldConnection.close();
          oldConnection.disconnect();
        }
      }
    } finally {
      if (connectionControl.shouldReconnect()) {
        connectionControl.scheduleReconnect(client);
      }
    }
  }

  private void setupReceiver(ProtonConnection connection) {
    ProtonReceiver receiver = connection.createReceiver(ADDRESS);

    receiver
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
  }

  private class ConnectionControl {
    private final int MAX_DELAY = 30_000; // ms
    private final int STARTING_DELAY = 10; // ms, for 2nd overall attempt onward.
    private final int BACKOFF = 2;

    private final String[] peers;
    private int peerIndex = 0;
    private int currentDelay = 0;
    private int nextDelay = 0;

    public ConnectionControl(String... peers) {
      this.peers = peers;
    }

    public void connected() {
      peerIndex = 0;
      currentDelay = 0;
      nextDelay = 0;
    }

    public String nextPeer() {
      String peer = peers[peerIndex++];
      if (peerIndex == peers.length) {
        // Tried all peers, restart next time round, after a delay.
        peerIndex = 0;

        if (nextDelay == 0) {
          nextDelay = STARTING_DELAY;
        } else {
          nextDelay = Math.min(nextDelay * BACKOFF, MAX_DELAY);
        }

        currentDelay = nextDelay;
      } else {
        currentDelay = 0;
      }

      return peer;
    }

    boolean shouldReconnect() {
      // For this example we just continue receiving.
      // Could use any metric here, e.g a retry limit etc.
      return true;
    }

    void scheduleReconnect(ProtonClient client) {
      if (currentDelay <= 0) {
        Vertx.currentContext()
            .runOnContext(
                x -> {
                  setupConnection(client);
                });
      } else {
        System.out.println(
            "# Scheduling connect attempt in "
                + currentDelay
                + "ms at "
                + Instant.now().plusMillis(currentDelay));
        vertx.setTimer(
            currentDelay,
            x -> {
              setupConnection(client);
            });
      }
    }
  }
}
