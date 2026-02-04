package com.example.database.replication.routing;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceRoutingTest {

    @Test
    void shouldDefaultToReaderDataSource() {
        assertThat(DataSourceContextHolder.getCurrentDataSource())
                .isEqualTo(DataSourceType.READER);
    }

    @Test
    void shouldUseWriterDataSourceWithinRunWithWriter() {
        DataSourceContextHolder.runWithWriter(() -> {
            assertThat(DataSourceContextHolder.getCurrentDataSource())
                    .isEqualTo(DataSourceType.WRITER);
        });
    }

    @Test
    void shouldUseWriterDataSourceWithinCallWithWriter() {
        var result = DataSourceContextHolder.callWithWriter(() -> {
            assertThat(DataSourceContextHolder.getCurrentDataSource())
                    .isEqualTo(DataSourceType.WRITER);
            return "success";
        });

        assertThat(result).isEqualTo("success");
    }

    @Test
    void shouldRevertToReaderAfterWriterScope() {
        assertThat(DataSourceContextHolder.getCurrentDataSource())
                .isEqualTo(DataSourceType.READER);

        DataSourceContextHolder.runWithWriter(() -> {
            assertThat(DataSourceContextHolder.getCurrentDataSource())
                    .isEqualTo(DataSourceType.WRITER);
        });

        // After scope ends, should revert to default (READER)
        assertThat(DataSourceContextHolder.getCurrentDataSource())
                .isEqualTo(DataSourceType.READER);
    }

    @Test
    void shouldSupportNestedWriterScopes() {
        DataSourceContextHolder.runWithWriter(() -> {
            assertThat(DataSourceContextHolder.getCurrentDataSource())
                    .isEqualTo(DataSourceType.WRITER);

            DataSourceContextHolder.runWithWriter(() -> {
                assertThat(DataSourceContextHolder.getCurrentDataSource())
                        .isEqualTo(DataSourceType.WRITER);
            });

            // Still WRITER after nested scope
            assertThat(DataSourceContextHolder.getCurrentDataSource())
                    .isEqualTo(DataSourceType.WRITER);
        });
    }

    @Test
    void shouldIsolateContextBetweenThreads() {
        var otherThreadDataSource = new AtomicReference<DataSourceType>();

        DataSourceContextHolder.runWithWriter(() -> {
            // Main thread should have WRITER
            assertThat(DataSourceContextHolder.getCurrentDataSource())
                    .isEqualTo(DataSourceType.WRITER);

            var thread = new Thread(() -> {
                // Other thread should default to READER (ScopedValue isolation)
                otherThreadDataSource.set(DataSourceContextHolder.getCurrentDataSource());
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertThat(otherThreadDataSource.get())
                .isEqualTo(DataSourceType.READER);
    }

    @Test
    void shouldRevertToReaderEvenOnException() {
        try {
            DataSourceContextHolder.runWithWriter(() -> {
                assertThat(DataSourceContextHolder.getCurrentDataSource())
                        .isEqualTo(DataSourceType.WRITER);
                throw new RuntimeException("test exception");
            });
        } catch (RuntimeException ignored) {
            // Expected
        }

        // Should revert to READER even after exception
        assertThat(DataSourceContextHolder.getCurrentDataSource())
                .isEqualTo(DataSourceType.READER);
    }
}
