package com.example.database.replication.routing;

import java.util.concurrent.Callable;

/**
 * Context holder for the current datasource routing key using ScopedValue (Java 25+).
 * ScopedValue is preferred over ThreadLocal for virtual thread compatibility and better performance.
 *
 * <p>The default datasource is READER. Use {@link #runWithWriter(Runnable)} or
 * {@link #callWithWriter(Callable)} to execute operations on the writer datasource.
 *
 * <p>Benefits of ScopedValue over ThreadLocal:
 * <ul>
 *   <li>Immutable bindings - prevents accidental mutation</li>
 *   <li>Automatic cleanup - no need for try-finally to clear values</li>
 *   <li>Better performance with virtual threads</li>
 *   <li>Structured concurrency friendly</li>
 * </ul>
 */
public final class DataSourceContextHolder {

    private static final ScopedValue<DataSourceType> CURRENT_DATASOURCE = ScopedValue.newInstance();

    private DataSourceContextHolder() {
    }

    /**
     * Returns the current datasource type. Defaults to READER if not set.
     */
    public static DataSourceType getCurrentDataSource() {
        return CURRENT_DATASOURCE.orElse(DataSourceType.READER);
    }

    /**
     * Executes the given runnable with the writer datasource.
     *
     * @param runnable the operation to execute with writer datasource
     */
    public static void runWithWriter(Runnable runnable) {
        ScopedValue.where(CURRENT_DATASOURCE, DataSourceType.WRITER).run(runnable);
    }

    /**
     * Executes the given callable with the writer datasource and returns the result.
     *
     * @param callable the operation to execute with writer datasource
     * @param <T>      the return type
     * @return the result of the callable
     */
    public static <T, X extends Throwable> T callWithWriter(ScopedValue.CallableOp<T, X> callable) throws X {
        return ScopedValue.where(CURRENT_DATASOURCE, DataSourceType.WRITER).call(callable);
    }
}
