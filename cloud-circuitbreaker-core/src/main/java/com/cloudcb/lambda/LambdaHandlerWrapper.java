package com.cloudcb.lambda;

import software.amazon.awssdk.core.interceptor.Context;

/**
 * Functional interface for wrapping AWS Lambda handlers with custom logic,
 * such as circuit breaker checks, metrics, or logging.
 *
 * <p>
 * This interface is intended to be used as a functional wrapper around the standard AWS Lambda handler,
 * allowing you to inject cross-cutting concerns without modifying the business logic directly.
 * </p>
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 * LambdaHandlerWrapper<Request, Response> wrapper = (input, context) -> {
 *     // Pre-processing logic (e.g., circuit breaker check)
 *     return actualHandler.handle(input, context);
 * };
 * }</pre>
 *
 * @param <I> the input type expected by the Lambda function
 * @param <O> the output type returned by the Lambda function
 * @author Clinton Fernandes
 */
@FunctionalInterface
public interface LambdaHandlerWrapper<I, O> {

    /**
     * Handles the Lambda input using the provided context.
     *
     * @param input   the input payload received by the Lambda function
     * @param context the Lambda context object containing metadata and runtime info
     * @return the output result of the Lambda function
     * @throws Exception if an error occurs during execution
     */
    O handle(I input, Context context) throws Exception;
}
