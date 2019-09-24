package org.jzb.test.graphql;

import com.google.common.io.Resources;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import lombok.SneakyThrows;

import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author jzb 2019-09-24
 */
public class GraphqlJavaModule extends AbstractModule {

    @Provides
    @Singleton
    private GraphQL GraphQL() throws IOException {
        final SchemaParser schemaParser = new SchemaParser();
        final TypeDefinitionRegistry typeDefinitionRegistry = new TypeDefinitionRegistry();
        Stream.of("mes-auto.graphql", "mes-auto-command.graphql", "mes-auto-type.graphql")
                .map(this::loadSdl)
                .map(schemaParser::parse)
                .forEach(typeDefinitionRegistry::merge);
        final SchemaGenerator schemaGenerator = new SchemaGenerator();
        final RuntimeWiring runtimeWiring = DataFetchers.buildWiring();
        final GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeDefinitionRegistry, runtimeWiring);
        return GraphQL.newGraphQL(graphQLSchema).build();
    }

    @SneakyThrows
    private String loadSdl(String fileName) {
        final URL url = Resources.getResource(fileName);
        return Resources.toString(url, UTF_8);
    }

}
