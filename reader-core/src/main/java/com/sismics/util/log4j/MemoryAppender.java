```java
package com.sismics.util.log4j;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

import com.sismics.reader.core.util.jpa.PaginatedList;
import com.sismics.util.log4j.model.LogEntry;

/**
 * Memory appender for Log4J.
 *
 * @author jtremeaux
 */
public class MemoryAppender extends AppenderSkeleton {

    /**
     * Maximum size of the queue.
     */
    private int size;

    /**
     * Queue of log entries.
     */
    private Queue<LogEntry> logEntries = new ConcurrentLinkedQueue<>();

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

        LogEntry logEntry = new LogEntry(System.currentTimeMillis(), event.getLevel().toString(), event.getLoggerName(),
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
     * Getter of logEntries.
     *
     * @return logEntries
     */
    public Queue<LogEntry> getLogEntries() {
        return logEntries;
    }

    /**
     * Setter of size.
     *
     * @param size size
     */
    public void setSize(int size) {
        this.size = size;
    }

}
```
====FILE_DELIMITER====
```java
package com.sismics.util.log4j.model;

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
    private String level;

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
        this.level = level;
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
    public String getLevel() {
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

}
```