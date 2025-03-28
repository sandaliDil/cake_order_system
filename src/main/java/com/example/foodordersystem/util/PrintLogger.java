package com.example.foodordersystem.util;

import java.io.IOException;
import java.util.logging.*;

public class PrintLogger {
    private static final Logger logger = Logger.getLogger(PrintLogger.class.getName());

    static {
        try {
            // Create a log file in the project directory
            FileHandler fileHandler = new FileHandler("print_log.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false); // Prevent logging to console
        } catch (IOException e) {
            System.out.println("Failed to initialize logger: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void logInfo(String message) {
        System.out.println("[INFO] " + message); // Print to console
        logger.info(message);
    }

    public static void logError(String message, Exception e) {
        System.out.println("[ERROR] " + message + " - " + e.getMessage()); // Print to console
        logger.severe(message + " - " + e.getMessage());
    }

    public static void logOrderInfo(int orderId, String details) {
        String logMessage = "Order ID: " + orderId + " - " + details;
        System.out.println("[ORDER] " + logMessage); // Print to console
        logger.info(logMessage);
    }

    public static void logPrintStatus(int orderId, boolean isSuccess) {
        if (isSuccess) {
            logOrderInfo(orderId, "Printout successful.");
        } else {
            logError("Failed to print Order ID: " + orderId, new Exception("Print error occurred"));
        }
    }

    // New method to log the HTML content
    public static void logHtmlContent(String htmlContent) {
        try {
            FileHandler fileHandler = new FileHandler("html_output_log.txt", true);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.info("[HTML CONTENT] \n" + htmlContent);
        } catch (IOException e) {
            System.out.println("Failed to log HTML content: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
