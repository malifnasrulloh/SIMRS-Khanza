/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package khanzahmsservicemobilejknerm;

import fungsi.logger.SystemLogger;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author windiartonugroho
 */
public class KhanzaHMSServiceMobileJKNERM {

    public static final Path logPath = Paths.get("log/khanzahmsservicemobilejkn");

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SystemLogger.configure(Paths.get("log/khanzahmsservicemobilejknerm"), "service-log");
        new frmUtama().setVisible(true);
    }

}
