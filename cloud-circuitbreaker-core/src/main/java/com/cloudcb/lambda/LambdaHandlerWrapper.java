package com.cloudcb.lambda;

import software.amazon.awssdk.core.interceptor.Context;

/**
 * @author Clinton Fernandes
 */
@FunctionalInterface
public interface LambdaHandlerWrapper<I, O> {
    O handle(I input, Context context) throws Exception;
}
