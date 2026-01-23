package com.coloza.demo.graphql.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class GraphQLErrorHandler extends DataFetcherExceptionResolverAdapter {

    private static final String MASKED_ERROR_MESSAGE = "An internal error occurred. Please contact support.";
    private static final boolean MASK_TECHNICAL_ERRORS = true;

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        log.error("GraphQL error occurred at path: {}", env.getExecutionStepInfo().getPath(), ex);

        if (ex instanceof GraphQLException graphQLException) {
            return handleGraphQLException(graphQLException, env);
        }

        if (ex instanceof IllegalArgumentException) {
            return buildError(env, ErrorCode.INVALID_INPUT, ex.getMessage(), ErrorType.BAD_REQUEST);
        }

        return handleUnexpectedException(ex, env);
    }

    private GraphQLError handleGraphQLException(GraphQLException ex, DataFetchingEnvironment env) {
        var errorCode = ex.getErrorCode();

        if (shouldMaskError(errorCode)) {
            log.error("Technical error masked - Original: {}", ex.getMessage(), ex);
            return buildMaskedError(env);
        }

        if (ex instanceof ValidationException validationEx && !validationEx.getFieldErrors().isEmpty()) {
            return buildValidationError(validationEx, env);
        }

        var errorType = determineErrorType(errorCode);
        return buildError(env, errorCode, ex.getUserMessage(), errorType);
    }

    private GraphQLError handleUnexpectedException(Throwable ex, DataFetchingEnvironment env) {
        log.error("Unexpected error occurred", ex);

        if (MASK_TECHNICAL_ERRORS) {
            return buildMaskedError(env);
        }

        return buildError(env, ErrorCode.INTERNAL_SERVER_ERROR,
                ex.getMessage(), ErrorType.INTERNAL_ERROR);
    }

    private boolean shouldMaskError(ErrorCode errorCode) {
        return MASK_TECHNICAL_ERRORS && errorCode.isTechnicalError();
    }

    private GraphQLError buildError(DataFetchingEnvironment env, ErrorCode errorCode,
                                    String message, ErrorType errorType) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("errorCode", errorCode.getCode());
        extensions.put("statusCode", errorCode.getStatusCode());

        return GraphqlErrorBuilder.newError(env)
                .message(message)
                .errorType(errorType)
                .extensions(extensions)
                .build();
    }

    private GraphQLError buildValidationError(ValidationException ex, DataFetchingEnvironment env) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("errorCode", ex.getErrorCode().getCode());
        extensions.put("statusCode", ex.getErrorCode().getStatusCode());
        extensions.put("fieldErrors", ex.getFieldErrors());

        return GraphqlErrorBuilder.newError(env)
                .message(ex.getUserMessage())
                .errorType(ErrorType.BAD_REQUEST)
                .extensions(extensions)
                .build();
    }

    private GraphQLError buildMaskedError(DataFetchingEnvironment env) {
        Map<String, Object> extensions = new HashMap<>();
        extensions.put("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
        extensions.put("statusCode", 500);

        return GraphqlErrorBuilder.newError(env)
                .message(MASKED_ERROR_MESSAGE)
                .errorType(ErrorType.INTERNAL_ERROR)
                .extensions(extensions)
                .build();
    }

    private ErrorType determineErrorType(ErrorCode errorCode) {
        return switch (errorCode.getStatusCode() / 100) {
            case 4 -> switch (errorCode.getStatusCode()) {
                case 404 -> ErrorType.NOT_FOUND;
                case 400 -> ErrorType.BAD_REQUEST;
                default -> ErrorType.BAD_REQUEST;
            };
            case 5 -> ErrorType.INTERNAL_ERROR;
            default -> ErrorType.INTERNAL_ERROR;
        };
    }
}
