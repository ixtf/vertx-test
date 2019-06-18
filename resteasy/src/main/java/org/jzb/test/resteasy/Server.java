package org.jzb.test.resteasy;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.DeploymentOptions;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.core.eventbus.Message;
import io.vertx.reactivex.ext.web.Router;
import org.jboss.resteasy.plugins.server.vertx.VertxRequestHandler;
import org.jboss.resteasy.plugins.server.vertx.VertxResteasyDeployment;

/**
 * @author jzb 2019-06-04
 */
public class Server extends AbstractVerticle {
    public static Injector INJECTOR;

    // Convenience method so you can run it in your IDE
    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        INJECTOR = Guice.createInjector(new GuiceModule(vertx));
        vertx.deployVerticle(Server.class.getName());
        vertx.deployVerticle(Backend.class.getName(), new DeploymentOptions().setWorker(true));
    }

    @Override
    public void start() throws Exception {
        // Build the Jax-RS hello world deployment
        final VertxResteasyDeployment deployment = new VertxResteasyDeployment();
        deployment.setAsyncJobServiceEnabled(true);
        deployment.start();
        deployment.getRegistry().addPerInstanceResource(HelloWorldResource.class);

        final Router router = Router.router(vertx);
        router.get("/vertx").handler(rc -> vertx.eventBus().<String>rxSend("backend", null)
                .map(Message::body)
                .subscribe(rc.response()::end, rc::fail));
        router.get("/reactive").handler(rc -> vertx.eventBus().<String>rxSend("backend-reactive", null)
                .map(Message::body)
                .subscribe(rc.response()::end, rc::fail));

        // Start the front end server using the Jax-RS controller
        vertx.getDelegate().createHttpServer()
                .requestHandler(new VertxRequestHandler(vertx.getDelegate(), deployment))
//                .requestHandler(router.getDelegate())
                .listen(8080, ar -> {
                    System.out.println("Server started on port " + ar.result().actualPort());
                });
    }
}
