/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils.logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author malifnasrulloh
 */
public class FileLogger {

    private static final String STARTUP_TIME = LocalDateTime.now(ZoneId.of("Asia/Jakarta")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

    private final Path logPath;
    private final String prefixLogName;

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss.SSS");
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FileLogger(Path logPath, String prefixLogName) {
        this.logPath = logPath.resolve(STARTUP_TIME);
        this.prefixLogName = prefixLogName;
        try {
            Files.createDirectories(this.logPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create log directory", e);
        }
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return FORMATTER;
    }

    public synchronized void log(Object message, LogType type) {
        String time = LocalDateTime.now(ZoneId.of("Asia/Jakarta")).format(FORMATTER);

        Map<String, Object> logEntry = new HashMap<>();
        logEntry.put("timestamp", time);
        logEntry.put("type", type.name());

        if (message instanceof Throwable) {
            Throwable t = (Throwable) message;
            logEntry.put("message", t.getMessage());

            ArrayList<String> sb = new ArrayList<>();
            for (StackTraceElement element : t.getStackTrace()) {
                sb.add(element.toString());
            }
            logEntry.put("stackTrace", sb);
        } else {
            logEntry.put("message", message != null ? message.toString() : "null");
        }

        String jsonLine = "";
        try {
            jsonLine = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(logEntry) + System.lineSeparator();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String fileName = type == LogType.EXCEPTION ? prefixLogName + "-error.json" : prefixLogName + ".json";

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logPath.resolve(fileName).toFile(), true))) {
            bw.write(jsonLine);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Object message) {
        log(message, LogType.INFO);
    }
}
