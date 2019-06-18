package org.jzb.test.graphql;

import graphql.GraphQL;
import graphql.schema.DataFetchingEnvironment;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import io.reactivex.Completable;
import io.vertx.core.Future;
import io.vertx.core.WorkerExecutor;
import io.vertx.ext.web.handler.graphql.VertxDataFetcher;
import io.vertx.reactivex.core.AbstractVerticle;
import io.vertx.reactivex.core.Vertx;
import io.vertx.reactivex.ext.web.Router;
import io.vertx.reactivex.ext.web.handler.graphql.GraphQLHandler;
import lombok.SneakyThrows;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static graphql.schema.idl.RuntimeWiring.newRuntimeWiring;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-06-08
 */
public class GraphqlServer extends AbstractVerticle {
    private List<Link> links;

    @Override
    public Completable rxStart() {
        prepareData();

        Router router = Router.router(vertx);
        router.route("/graphql").handler(GraphQLHandler.create(createGraphQL()));

        return vertx.createHttpServer()
                .requestHandler(router)
                .rxListen(8080)
                .ignoreElement();
    }

    private GraphQL createGraphQL() {
        final String schema = vertx.fileSystem().readFileBlocking("links.graphql").toString();
        final TypeDefinitionRegistry typeDefinitionRegistry = new SchemaParser().parse(schema);

        final RuntimeWiring runtimeWiring = newRuntimeWiring().type("Query", builder -> {
            VertxDataFetcher<List<Link>> getAllLinks = new VertxDataFetcher<>(this::getAllLinks);
            VertxDataFetcher<List<Link>> syncBlock = new VertxDataFetcher<>(this::syncBlock);
            return builder.dataFetcher("allLinks", getAllLinks)
                    .dataFetcher("syncBlock", syncBlock);
        }).build();

        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    private void getAllLinks(DataFetchingEnvironment env, Future<List<Link>> future) {
        final WorkerExecutor workerExecutor = vertx.getDelegate().createSharedWorkerExecutor("test", 1000, 5, TimeUnit.SECONDS);
        System.out.println(workerExecutor);

        boolean secureOnly = env.getArgument("secureOnly");
        List<Link> result = links.stream()
                .filter(link -> !secureOnly || link.getUrl().startsWith("https://"))
                .collect(toList());
        future.complete(result);
    }

    @SneakyThrows
    private void syncBlock(DataFetchingEnvironment env, Future<List<Link>> future) {
        Flux.fromIterable(links).subscribeOn(Schedulers.elastic())
                .collectList()
                .delayElement(Duration.ofSeconds(100))
                .subscribe(future::complete, future::fail);
    }

    private void prepareData() {
        User peter = new User("Peter");
        User paul = new User("Paul");
        User jack = new User("Jack");

        links = new ArrayList<>();
        links.add(new Link("https://vertx.io", "Vert.x project", peter));
        links.add(new Link("https://www.eclipse.org", "Eclipse Foundation", paul));
        links.add(new Link("http://reactivex.io", "ReactiveX libraries", jack));
        links.add(new Link("https://www.graphql-java.com", "GraphQL Java implementation", peter));
    }

    public static void main(String[] args) {
        final Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(GraphqlServer.class.getName());
    }

}
