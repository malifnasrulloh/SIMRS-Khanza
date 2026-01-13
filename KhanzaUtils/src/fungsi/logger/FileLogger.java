/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fungsi.logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

/**
 *
 * @author malifnasrulloh
 */
public class FileLogger {

    private final Path logPath;
    private final String logName;

    private final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy HH-mm-ss.SSS");

    public DateTimeFormatter getDateTimeFormatter() {
        return FORMATTER;
    }

    public FileLogger(Path logPath, String prefixLogName) {
        this.logPath = logPath;
        this.logName = prefixLogName + "-" + LocalDateTime.now(ZoneId.of("Asia/Jakarta")).format(FORMATTER) + ".txt";
        try {
            Files.createDirectories(logPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create log directory", e);
        }
    }

    public synchronized void log(Object message, LogType type) {
        String time = LocalDateTime.now(ZoneId.of("Asia/Jakarta")).format(FORMATTER);

        String cleanMessage = (message != null ? message.toString() : "null").replace("\n", " ");
        String line = String.format("[%s] [%s] %s%n", time, type, cleanMessage);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(logPath.resolve(logName).toFile(), true))) {
            bw.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Object message) {
        log(message, LogType.LOG);
    }

}
