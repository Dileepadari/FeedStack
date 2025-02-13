```java
package com.sismics.util.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Memory appender for Log4J.
 *
 * @author jtremeaux
 */
public class MemoryAppender extends AppenderSkeleton {

    /**
     * Maximum size of the queue.
     */
    private int size = 1000;

    /**
     * Queue of log entries.
     */
    private final LogEntryQueue logEntries = new LogEntryQueue(size);

    @Override
    public boolean requiresLayout() {
        return false;
    }

    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
    }

    @Override
    public synchronized void append(LoggingEvent event) {
        if (closed) {
            return;
        }

        logEntries.add(new LogEntry(event));
    }

    /**
     * Getter of log entries.
     *
     * @return logEntries
     */
    public Queue<LogEntry> getLogEntries() {
        return logEntries;
    }
}
```
====FILE_DELIMITER====
```java
package com.sismics.util.log4j;

import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Queue of log entries.
 *
 * @author jtremeaux
 */
class LogEntryQueue {

    /**
     * Maximum size of the queue.
     */
    private final int size;

    /**
     * Queue of log entries.
     */
    private final Queue<LogEntry> logEntries = new ConcurrentLinkedDeque<>();

    /**
     * Constructor.
     *
     * @param size Maximum size of the queue
     */
    LogEntryQueue(int size) {
        this.size = size;
    }

    /**
     * Adds a log entry to the queue.
     *
     * @param logEntry Log entry
     */
    public void add(LogEntry logEntry) {
        if (logEntries.size() == size) {
            logEntries.remove();
        }
        logEntries.add(logEntry);
    }
}
```