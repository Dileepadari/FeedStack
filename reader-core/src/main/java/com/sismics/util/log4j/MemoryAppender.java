```java
package com.sismics.util.log4j;

import java.time.Instant;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.logging.Level;
import java.util.stream.Collectors;

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
    private Queue<LogEntry> logEntries = new ConcurrentLinkedDeque<>();

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
        removeOldEntries();
        if (closed) {
            return;
        }

        LogEntry logEntry = new LogEntry(Instant.now().toEpochMilli(), event.getLevel().toString(), event.getLoggerName(),
                event.getMessage().toString());
        logEntries.add(logEntry);
    }

    /**
     * Removes old entries if the queue is full.
     */
    private void removeOldEntries() {
        while (logEntries.size() > size) {
            logEntries.remove();
        }
    }

    /**
     * Getter of log entries.
     *
     * @return logEntries
     */
    public Queue<LogEntry> getLogEntries() {
        return Collections.unmodifiableQueue(logEntries);
    }
}
```
====FILE_DELIMITER====
```java
package com.sismics.util.log4j.model;

import java.time.Instant;

/**
 * Log entry.
 *
 * @author jtremeaux
 */
public class LogEntry {

    /**
     * Timestamp.
     */
    private long timestamp;

    /**
     * Log level.
     */
    private Level level;

    /**
     * Logger name.
     */
    private String logger;

    /**
     * Message.
     */
    private String message;

    /**
     * Constructor.
     *
     * @param timestamp Timestamp
     * @param level     Log level
     * @param logger    Logger name
     * @param message   Message
     */
    public LogEntry(long timestamp, String level, String logger, String message) {
        this.timestamp = timestamp;
        this.level = Level.parse(level);
        this.logger = logger;
        this.message = message;
    }

    /**
     * Getter of timestamp.
     *
     * @return timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Getter of level.
     *
     * @return level
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Getter of logger.
     *
     * @return logger
     */
    public String getLogger() {
        return logger;
    }

    /**
     * Getter of message.
     *
     * @return message
     */
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return String.format("%s - %s - %s - %s", Instant.ofEpochMilli(timestamp), level, logger, message);
    }
}
```