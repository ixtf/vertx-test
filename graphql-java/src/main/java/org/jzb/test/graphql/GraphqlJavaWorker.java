package org.jzb.test.graphql;

import com.github.ixtf.persistence.mongo.Jmongo;
import com.mongodb.client.model.Filters;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import org.jzb.test.graphql.domain.Operator;
import reactor.core.publisher.Mono;

import java.io.File;

import static com.github.ixtf.persistence.mongo.Jmongo.ID_COL;
import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-09-23
 */
public class GraphqlJavaWorker extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        vertx.eventBus().<JsonObject>consumer("graphql", reply -> Mono.fromCallable(() -> {
            final JsonObject queryJson = reply.body();
            final String query = queryJson.getString("query");
            final ExecutionResult executionResult = getGraphQL().execute(query);
            return executionResult.getData().toString();
//                reply.reply(executionResult.getData().toString());
//                final ExecutionInput input = new ExecutionInput(query, null, queryJson, null, extractVariables(queryJson));
        }).subscribe(it -> {
            reply.reply(it);
        }, err -> {
            reply.fail(400, err.getMessage());
        }));
    }

    private GraphQL getGraphQL() {
        final String schemaFilePath = this.getClass().getResource("/mes-auto.graphql").getFile();
        final File schemaFile = new File(schemaFilePath);
        final SchemaParser schemaParser = new SchemaParser();
        final TypeDefinitionRegistry typeDefinitionRegistry = schemaParser.parse(schemaFile);
        final RuntimeWiring runtimeWiring = newRuntimeWiring()
                .type("Query", builder -> builder
                        .dataFetcher("hello", env -> "world")
                        .dataFetcher("operators", env -> {
                            final Jmongo jmongo = Jmongo.of(JmongoDev.class);
                            int first = env.getArgument("first");
                            int pageSize = env.getArgument("pageSize");
                            return jmongo.query(Operator.class, Filters.exists(ID_COL), first, pageSize)
                                    .toStream()
                                    .collect(toList());
                        })
                )
                .build();
        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }
}
