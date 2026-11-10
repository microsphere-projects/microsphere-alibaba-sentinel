/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.alibaba.sentinel.common;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.function.ThrowableAction;
import io.microsphere.lang.function.ThrowableConsumer;
import io.microsphere.lang.function.ThrowableFunction;
import io.microsphere.logging.Logger;

import java.util.function.Consumer;
import java.util.function.Function;

import static com.alibaba.csp.sentinel.Tracer.trace;
import static com.alibaba.csp.sentinel.slots.block.BlockException.isBlockException;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ExceptionUtils.throwTarget;

/**
 * The common operations for Sentinel:
 * <ul>
 *     <li>One-Time Operations :
 *      <ul>
 *          <li>execution with result:
 *              <ul>
 *                  <li>{@link #call(String, String, String, ThrowableFunction)}</li>
 *                  <li>{@link #execute(String, String, String, Function)}</li>
 *              </ul>
 *          </li>
 *          <li>execution without result:
 *              <ul>
 *                  <li>{@link #call(String, String, String, ThrowableAction)}</li>
 *                  <li>{@link #call(String, String, String, ThrowableConsumer)}</li>
 *                  <li>{@link #execute(String, String, String, Runnable)}</li>
 *                  <li>{@link #execute(String, String, String, Consumer)}</li>
 *              </ul>
 *          </li>
 *      </ul>
 *     </li>
 *     <li>Two-Phase Operations :
 *      <ul>
 *          <li>{@link #begin(String, String, String) the first phase}</li>
 *          <li>{@link #end(SentinelContext) the second phase}</li>
 *      </ul>
 *     </li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public interface SentinelOperations {

    // The One-Time Operations

    /**
     * Execute the callback without result
     *
     * @param resourceName the name of the resource
     * @param callback     the callback to be executed
     */
    default void execute(@Nonnull String resourceName, Runnable callback) {
        execute(resourceName, null, null, callback);
    }

    /**
     * Execute the callback without result
     *
     * @param resourceName the name of the resource
     * @param contextName  the name of the context
     * @param origin       the origin of the execution
     * @param callback     the callback to be executed
     */
    default void execute(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin, Runnable callback) {
        execute(resourceName, contextName, origin, context -> {
            callback.run();
        });
    }

    /**
     * Execute the callback without result
     *
     * @param resourceName the name of the resource
     * @param callback     the callback to be executed
     */
    default void execute(@Nonnull String resourceName, Consumer<SentinelContext> callback) {
        execute(resourceName, null, null, callback);
    }

    /**
     * Execute the callback without result
     *
     * @param resourceName the name of the resource
     * @param contextName  the name of the context
     * @param origin       the origin of the execution
     * @param callback     the callback to be executed
     */
    default void execute(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin, Consumer<SentinelContext> callback) {
        execute(resourceName, contextName, origin, context -> {
            callback.accept(context);
            return null;
        });
    }

    /**
     * Execute the callback with result
     *
     * @param resourceName the name of the resource
     * @param callback     the callback to be executed
     */
    default <R> R execute(@Nonnull String resourceName, Function<SentinelContext, R> callback) {
        return execute(resourceName, null, null, callback);
    }

    /**
     * Execute the callback with result
     *
     * @param resourceName the name of the resource
     * @param contextName  the name of the context
     * @param origin       the origin of the execution
     * @param callback     the callback to be executed
     */
    default <R> R execute(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin, Function<SentinelContext, R> callback) {
        return call(resourceName, contextName, origin, callback::apply, RuntimeException.class);
    }

    /**
     * Call the callback without result, and may throw any error
     *
     * @param resourceName the name of the resource
     * @param callback     the callback to be executed
     * @throws Throwable any error caused by the execution of the callback
     */
    default void call(@Nonnull String resourceName, @Nonnull ThrowableAction callback) throws Throwable {
        call(resourceName, null, null, callback);
    }

    /**
     * Call the callback without result, and may throw any error
     *
     * @param resourceName the name of the resource
     * @param contextName  the name of the context
     * @param origin       the origin of the execution
     * @param callback     the callback to be executed
     * @throws Throwable any error caused by the execution of the callback
     */
    default void call(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin, @Nonnull ThrowableAction callback) throws Throwable {
        call(resourceName, contextName, origin, context -> {
            callback.execute();
            return null;
        });
    }

    /**
     * Call the callback without result, and may throw any error
     *
     * @param resourceName the name of the resource
     * @param callback     the callback to be executed
     * @throws Throwable any error caused by the execution of the callback
     */
    default void call(@Nonnull String resourceName, @Nonnull ThrowableConsumer<SentinelContext> callback) throws Throwable {
        call(resourceName, null, null, callback);
    }

    /**
     * Call the callback without result, and may throw any error
     *
     * @param resourceName the name of the resource
     * @param contextName  the name of the context
     * @param origin       the origin of the execution
     * @param callback     the callback to be executed
     * @throws Throwable any error caused by the execution of the callback
     */
    default void call(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin, @Nonnull ThrowableConsumer<SentinelContext> callback) throws Throwable {
        call(resourceName, contextName, origin, context -> {
            callback.accept(context);
            return null;
        });
    }

    /**
     * Call the callback with result, and may throw any error
     *
     * @param resourceName   the name of the resource
     * @param contextName    the name of the context
     * @param origin         the origin of the execution
     * @param callback       the callback to be executed
     * @param throwableClass the sub-class of the {@link Throwable}
     * @param <R>            the type of result
     * @param <TR>           the sub-class of the {@link Throwable}
     * @throws Throwable any error caused by the execution of the callback
     */
    default <R, TR extends Throwable> R call(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin,
                                             @Nonnull ThrowableFunction<SentinelContext, R> callback, Class<TR> throwableClass) throws TR {
        try {
            return call(resourceName, contextName, origin, callback);
        } catch (Throwable t) {
            throw throwTarget(t, throwableClass);
        }
    }

    /**
     * Call the callback with result, and may throw any error
     *
     * @param resourceName the name of the resource
     * @param callback     the callback to be executed
     * @param <R>          the type of result
     * @throws Throwable any error caused by the execution of the callback
     */
    default <R> R call(@Nonnull String resourceName, @Nonnull ThrowableFunction<SentinelContext, R> callback) throws Throwable {
        return call(resourceName, null, null, callback);
    }

    /**
     * Call the callback with result, and may throw any error
     *
     * @param resourceName the name of the resource
     * @param contextName  the name of the context
     * @param origin       the origin of the execution
     * @param callback     the callback to be executed
     * @param <R>          the type of result
     * @throws Throwable any error caused by the execution of the callback
     */
    default <R> R call(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin,
                       @Nonnull ThrowableFunction<SentinelContext, R> callback) throws Throwable {
        SentinelContext context = begin(resourceName, contextName, origin);
        try {
            return callback.apply(context);
        } catch (Throwable e) {
            if (isBlockException(e)) {
                // BlockException is the normal flow-control result of Sentinel rather than
                // a failure, thus it's traced as a block event without noisy error logs
                Logger logger = getLogger(this.getClass());
                if (logger.isDebugEnabled()) {
                    logger.debug("The callback '{}' of Sentinel resource[name : '{}'] was blocked", callback, resourceName);
                }
            } else {
                trace(e);
                Logger logger = getLogger(this.getClass());
                if (logger.isErrorEnabled()) {
                    logger.error("A callback '{}' of Sentinel context[name :'{}' , origin : '{}'] resource[name :'{}'] execution is failed", callback, contextName, origin, resourceName, e);
                }
            }
            throw e;
        } finally {
            end(context);
        }
    }

    // The Two-Phase Operations

    /**
     * Begin the execution in the first phase.
     *
     * @param resourceName the name of the resource
     * @param contextName  the name of the context
     * @param origin       the origin of the execution
     * @return {@link SentinelContext} with the entry and its name
     * @throws Exception any error caused by the execution of begin
     */
    @Nonnull
    SentinelContext begin(@Nonnull String resourceName, @Nullable String contextName, @Nullable String origin) throws Exception;

    /**
     * End the execution in the second phase.
     *
     * @param context {@link SentinelContext}
     */
    void end(SentinelContext context);
}