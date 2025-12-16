package com.metro.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.JOptionPane;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class LoggerUtil {

    private static final Logger logger = LogManager.getLogger(LoggerUtil.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private LoggerUtil() {
        throw new AssertionError("Utility class cannot be instantiated");
    }

    public static void logApplicationStart() {
        String startTime = LocalDateTime.now().format(DATE_FORMATTER);
        logger.info("=== Metro Navigator application started ===");
        logger.info("Start time: {}", startTime);
    }

    public static void logApplicationShutdown() {
        logger.info("=== Metro Navigator application finished ===");
    }

    public static void showErrorDialog(String title, String message) {
        logger.error("Error dialog - {}: {}", title, message);
        JOptionPane.showMessageDialog(
                null,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    public static void logInfo(String message) {
        logger.info(message);
    }

    public static void logWarning(String message) {
        logger.warn(message);
    }

    public static void logDebug(String message) {
        logger.debug(message);
    }
}