package com.techprimers.graphql.springbootgraphqlexample.service;


import com.techprimers.graphql.springbootgraphqlexample.model.Book;
import com.techprimers.graphql.springbootgraphqlexample.repository.BookRepository;
import com.techprimers.graphql.springbootgraphqlexample.service.dataFetcher.AllBooksDataFetcher;
import com.techprimers.graphql.springbootgraphqlexample.service.dataFetcher.BookDataFetcher;
import graphql.GraphQL;
import graphql.schema.DataFetcher;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.springframework.core.io.Resource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

@Service
public class GraphQLService {

    @Value("classpath:books.graphql")
    Resource resource;

    private GraphQL graphQL;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    private AllBooksDataFetcher allBooksDataFetcher;
    @Autowired
    private BookDataFetcher bookDataFetcher;

    // load schema at startup
    @PostConstruct
    private void loadSchema() throws IOException {

        // load book into repo
        loadDataIntoHSQL();

        // get the schema
        File schemaFile = resource.getFile();
        //parse file
        TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(schemaFile);
        RuntimeWiring writing = buildRuntimeWiring();
        GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, writing);
        graphQL = GraphQL.newGraphQL(schema).build();
    }

    private void loadDataIntoHSQL() {
        Stream.of(
                new Book("123", "Book of clouds", "Kindle edition", new String[] {
                        "Chloe Aridis"
                }, "Nov 2017"),
                new Book("124", "Clouds Arc n engg", "Kindle edition more check", new String[] {
                        "Peter Aridis"
                }, "Dec 2017"),
                new Book("125", "Book of computers", "Kindle edition checking", new String[] {
                        "O Relly Aridis", "Venkat", "Sam"
                }, "Jan 2017")
        ).forEach( book -> {
            bookRepository.save(book);
        });
    }

    private RuntimeWiring buildRuntimeWiring() {
        return RuntimeWiring.newRuntimeWiring()
                .type("Query", typeWiring ->
                    typeWiring
                            .dataFetcher("allBooks",allBooksDataFetcher)
                            .dataFetcher("book", bookDataFetcher))
                .build();
    }

    public GraphQL getGraphQL() {
        return graphQL;
    }
}
