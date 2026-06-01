package com.example.pasir_ihor_kotenko.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import jakarta.validation.ConstraintViolationException;
import org.springframework.graphql.execution.DataFetcherExceptionResolver;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import java.util.List;

@Component
public class GraphQLExceptionHandler implements DataFetcherExceptionResolver {
    @Override
    public @NonNull Mono<List<GraphQLError>> resolveException(@NonNull Throwable ex, @NonNull DataFetchingEnvironment env) {
        if (ex instanceof ConstraintViolationException validationEx) {
            List<GraphQLError> errors = validationEx.getConstraintViolations().stream()
                    .map(v -> GraphqlErrorBuilder.newError(env).message(v.getMessage()).build())
                    .toList();
            return Mono.just(errors);
        }
        return Mono.just(List.of(GraphqlErrorBuilder.newError(env).message(ex.getMessage()).build()));
    }
}
