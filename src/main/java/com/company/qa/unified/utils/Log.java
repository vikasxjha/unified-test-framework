package com.company.qa.unified.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Centralized logging wrapper around SLF4J.
 *
 * Usage:
 *   private static final Log log = Log.get(MyClass.class);
 *   log.info("Message {}", param);
 *   log.debug("Debug {}", param);
 *   log.warn("Warning {}", param);
 *   log.error("Error {}", param);
 */
public final class Log {

    private final Logger logger;

    private Log(Logger logger) {
        this.logger = logger;
    }

    /**
     * Create a logger for the given class.
     */
    public static Log get(Class<?> clazz) {
        return new Log(LoggerFactory.getLogger(clazz));
    }

    /**
     * Log an info message with optional parameters.
     */
    public void info(String message, Object... args) {
        logger.info(message, args);
    }

    /**
     * Log a debug message with optional parameters.
     */
    public void debug(String message, Object... args) {
        logger.debug(message, args);
    }

    /**
     * Log a warning message with optional parameters.
     */
    public void warn(String message, Object... args) {
        logger.warn(message, args);
    }

    /**
     * Log a warning message with an exception.
     */
    public void warn(String message, Throwable throwable) {
        logger.warn(message, throwable);
    }

    /**
     * Log an error message with optional parameters.
     */
    public void error(String message, Object... args) {
        logger.error(message, args);
    }

    /**
     * Log an error message with an exception.
     */
    public void error(String message, Throwable throwable) {
        logger.error(message, throwable);
    }

    /**
     * Log a trace message with optional parameters.
     */
    public void trace(String message, Object... args) {
        logger.trace(message, args);
    }
}

