package org.jzb.test.graphql;

import graphql.ExecutionResult;
import graphql.GraphQL;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import reactor.core.publisher.Mono;

import static org.jzb.test.graphql.GraphqlJavaServer.INJECTOR;

/**
 * @author jzb 2019-09-23
 */
public class GraphqlJavaWorker extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        vertx.eventBus().<JsonObject>consumer("graphql", reply -> Mono.fromCallable(() -> {
            final JsonObject queryJson = reply.body();
            final String query = queryJson.getString("query");
            final GraphQL graphQL = INJECTOR.getInstance(GraphQL.class);
            final ExecutionResult executionResult = graphQL.execute(query);
            final Object data = executionResult.getData();
            return new JsonObject().put("data", data).encode();
//                final ExecutionInput input = new ExecutionInput(query, null, queryJson, null, extractVariables(queryJson));
        }).subscribe(it -> {
            reply.reply(it);
        }, err -> {
            reply.fail(400, err.getMessage());
        }));
    }
}
