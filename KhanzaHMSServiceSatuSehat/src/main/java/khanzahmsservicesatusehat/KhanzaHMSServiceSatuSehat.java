package khanzahmsservicesatusehat;

import khanzautils.logger.SystemLogger;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author windiartonugroho
 */
public class KhanzaHMSServiceSatuSehat {

    public static final Path logPath = Paths.get("log/khanzahmsservicesatusehat");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SystemLogger.configure(logPath, "service-log");
        new frmUtama().setVisible(true);
    }

}
