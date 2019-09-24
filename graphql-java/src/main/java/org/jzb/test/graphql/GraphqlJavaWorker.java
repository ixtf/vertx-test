package org.jzb.test.graphql;

import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static org.jzb.test.graphql.GraphqlJavaServer.INJECTOR;

/**
 * @author jzb 2019-09-23
 */
@Slf4j
public class GraphqlJavaWorker extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        vertx.eventBus().<JsonObject>consumer("graphql", reply -> Mono.fromCallable(() -> {
            final JsonObject queryJson = reply.body();
            final String query = queryJson.getString("query");

            final GraphQL graphQL = INJECTOR.getInstance(GraphQL.class);
            final ExecutionInput executionInput = ExecutionInput.newExecutionInput(query)
                    .context(queryJson)
                    .variables(Map.of())
                    .build();
            final ExecutionResult executionResult = graphQL.execute(executionInput);
//            final ExecutionResult executionResult = graphQL.execute(query);
            final Object data = executionResult.getData();
            return MAPPER.writeValueAsString(data);
//            return new JsonObject().put("data", data).encode();
//                final ExecutionInput input = new ExecutionInput(query, null, queryJson, null, extractVariables(queryJson));
        }).subscribe(it -> {
            reply.reply(it);
        }, err -> {
            log.error("", err);
            reply.fail(400, err.getMessage());
        }));
    }
}
