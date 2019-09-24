package org.jzb.test.graphql;

import com.github.ixtf.persistence.mongo.Jmongo;
import com.mongodb.reactivestreams.client.MongoCollection;
import graphql.schema.DataFetcher;
import graphql.schema.idl.RuntimeWiring;
import org.bson.Document;
import org.jzb.test.graphql.domain.Operator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * @author jzb 2019-09-24
 */
public class DataFetchers {
    private static final Jmongo jmongo = Jmongo.of(JmongoDev.class);
    private static DataFetcher operators = env -> {
        int first = env.getArgument("first");
        int pageSize = env.getArgument("pageSize");
        final MongoCollection<Document> t_operator = jmongo.collection(Operator.class);
        final Mono<Long> count$ = Mono.from(t_operator.countDocuments());
        final Mono<List<Operator>> operators$ = Flux.from(t_operator.find().skip(first).limit(pageSize).batchSize(pageSize))
                .map(it -> jmongo.toEntity(Operator.class, it))
                .collectList();
        return Mono.zip(count$, operators$).map(tuple -> {
            final Long count = tuple.getT1();
            final List<Operator> operators = tuple.getT2();
            return Map.of("count", count, "first", first, "pageSize", pageSize, "operators", operators);
        }).block();
    };

    public static RuntimeWiring buildWiring() {
        return RuntimeWiring.newRuntimeWiring().type("Query", builder -> builder
                .dataFetcher("hello", env -> "world")
                .dataFetcher("operators", operators)
        ).build();
    }
}
