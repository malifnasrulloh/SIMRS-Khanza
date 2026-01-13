/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fungsi.logger;

import java.nio.file.Path;
import java.util.Arrays;

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
        LOGGER = new FileLogger(SystemLogger.logPath, SystemLogger.prefixLogName);
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
        LOGGER.log(msg, LogType.LOG);
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
        LOGGER.log(msg + "\n" + Arrays.toString(msg.getStackTrace()).replace(", ", "at "), LogType.EXCEPTION);
    }
}
