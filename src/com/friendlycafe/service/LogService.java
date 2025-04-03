package com.friendlycafe.service;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton class for logging simulation events
 */
public class LogService {
    private static LogService instance;
    private StringBuilder log = new StringBuilder();
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private LogService() {
        // Private constructor for singleton pattern
        log("Log service initialized");
    }
    
    public static synchronized LogService getInstance() {
        if (instance == null) {
            instance = new LogService();
        }
        return instance;
    }
    
    public synchronized void log(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        log.append(timestamp).append(" - ").append(message).append("\n");
        System.out.println(timestamp + " - " + message); // Also print to console for debugging
    }
    
    public void writeLogToFile(String fileName) {
        try (FileWriter writer = new FileWriter(fileName)) {
            writer.write(log.toString());
            System.out.println("Log written to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
