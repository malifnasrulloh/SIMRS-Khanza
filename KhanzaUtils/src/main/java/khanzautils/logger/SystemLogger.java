/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils.logger;

import java.nio.file.Path;

/**
 *
 * @author malifnasrulloh
 */
public class SystemLogger {

    private static FileLogger LOGGER;
    private static Path logPath;
    private static String prefixLogName;

    private SystemLogger() {
    }

    public static void reconfigure() {
        if (logPath != null && prefixLogName != null) {
            LOGGER = new FileLogger(SystemLogger.logPath, SystemLogger.prefixLogName);
        }
    }

    public static void configure(Path logPath, String prefixLogName) {
        SystemLogger.logPath = logPath;
        SystemLogger.prefixLogName = prefixLogName;
        LOGGER = new FileLogger(SystemLogger.logPath, SystemLogger.prefixLogName);
    }

    private static void checkInit() {
        if (LOGGER == null) {
            throw new IllegalStateException("SystemLogger belum dikonfigurasi. Panggil configure() dulu.");
        }
    }

    public static void info(Object msg) {
        checkInit();
        LOGGER.log(msg, LogType.INFO);
    }

    public static void sql(Object msg) {
        checkInit();
        LOGGER.log(msg, LogType.SQL);
    }

    public static void http(Object msg) {
        checkInit();
        LOGGER.log(msg, LogType.HTTP);
    }

    public static void error(Throwable msg) {
        checkInit();
        LOGGER.log(msg, LogType.EXCEPTION);
    }
}
