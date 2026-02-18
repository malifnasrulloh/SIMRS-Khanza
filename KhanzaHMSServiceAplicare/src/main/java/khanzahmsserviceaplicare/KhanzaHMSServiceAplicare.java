package khanzahmsserviceaplicare;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.nio.file.Path;
import java.nio.file.Paths;
import khanzautils.logger.SystemLogger;

/**
 *
 * @author windiartonugroho
 */
public class KhanzaHMSServiceAplicare {

    public static final Path logPath = Paths.get("log/khanzahmsserviceaplicare");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SystemLogger.configure(logPath, "service-log");
        new frmUtama().setVisible(true);
    }

}
