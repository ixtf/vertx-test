package org.jzb.test.graphql;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ResponseContentTypeHandler;
import org.apache.commons.io.FileUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * @author jzb 2019-09-23
 */
public class GraphqlJavaServer extends AbstractVerticle {
    public static Injector INJECTOR;

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        INJECTOR = Guice.createInjector(new GraphqlJavaModule());
        vertx.deployVerticle(GraphqlJavaServer.class.getName());
        vertx.deployVerticle(GraphqlJavaWorker.class.getName(), new DeploymentOptions().setWorker(true));
    }

    @Override
    public void start() throws Exception {
        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setUploadsDirectory(FileUtils.getTempDirectoryPath()));
        router.route().handler(ResponseContentTypeHandler.create());
        router.route("/graphql").produces(APPLICATION_JSON).handler(rc -> {
            final JsonObject query = rc.getBodyAsJson();
            final Map<String, Object> variables = extractVariables(query);
            final DeliveryOptions deliveryOptions = new DeliveryOptions();
            Optional.ofNullable(variables.get("timeout"))
                    .map(Integer.class::cast)
                    .filter(it -> it > DeliveryOptions.DEFAULT_TIMEOUT)
                    .ifPresent(deliveryOptions::setSendTimeout);
            vertx.eventBus().<String>request("graphql", query, deliveryOptions, ar -> {
                if (ar.succeeded()) {
                    rc.response().end(ar.result().body());
                } else {
                    rc.fail(ar.cause());
                }
            });
        });
        vertx.createHttpServer().requestHandler(router).listen(8080);
    }

    private Map<String, Object> extractVariables(JsonObject request) {
        JsonObject variables = request.getJsonObject("variables");
        if (variables == null) {
            return Collections.emptyMap();
        } else {
            return variables.getMap();
        }
    }
}
