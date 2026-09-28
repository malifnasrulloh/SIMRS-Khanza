package bridging;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.StringSelection;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import me.friwi.jcefmaven.CefAppBuilder;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import kepegawaian.DlgCariPegawai;
import widget.Button;
import widget.InternalFrame;
import widget.panelisi;

/**
 * Dialog form for SATUSEHAT KYC (Know Your Customer) patient profile verification.
 * Embeds full Chromium browser (JCEF) in-app with fallback to external Chrome/Edge App Mode,
 * automatic operator resolution, audit logging, and challenge code simulator.
 */
public class SatuSehatKYCView extends JDialog {
    private static final String TITLE_TEXT = "::[ SATUSEHAT - Verifikasi Profil KYC Pasien ]::";

    private final sekuel Sequel = new sekuel();
    private final SatuSehatKYCApi api = new SatuSehatKYCApi();
    private Connection koneksi = koneksiDB.condb();

    // Patient state
    private String noRkmMedis = "";
    private String namaPasien = "";
    private String nikPasien = "";

    // Operator state
    private String agentNik = "";
    private String agentName = "";

    // Verification state
    private String currentToken = "";
    private String currentValidationUrl = "";
    private SatuSehatKYCResponse lastResponse;

    // JCEF Browser instance
    private CefClient cefClient;
    private CefBrowser cefBrowser;
    private Component cefComponent;

    // UI Components
    private InternalFrame internalFrame1;
    private JLabel lblPasienInfo;
    private JLabel lblPetugasInfo;
    private JLabel lblStatus;
    private JProgressBar progressBar;
    private JPanel pnlChromiumContainer;
    private JPanel pnlFallbackInfo;
    private JTextField txtValidationUrl;
    private Button btnSimulasiKode;
    private Button btnBukaBrowser;
    private Button btnMuatUlang;
    private Button btnSalinUrl;
    private Button btnDetailPayload;

    public SatuSehatKYCView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    public SatuSehatKYCView(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    private ImageIcon loadIcon(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            return url != null ? new ImageIcon(url) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private void initComponents() {
        setTitle(TITLE_TEXT);
        setUndecorated(true);
        setResizable(false);

        internalFrame1 = new InternalFrame();
        internalFrame1.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(240, 245, 235)),
            TITLE_TEXT,
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new Font("Tahoma", 0, 11),
            new Color(50, 50, 50)
        ));
        internalFrame1.setLayout(new BorderLayout());

        // Top info bar
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBackground(new Color(245, 248, 245));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));

        JPanel pnlTopLeft = new JPanel();
        pnlTopLeft.setLayout(new BoxLayout(pnlTopLeft, BoxLayout.Y_AXIS));
        pnlTopLeft.setOpaque(false);

        lblPasienInfo = new JLabel("Pasien: -");
        lblPasienInfo.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblPasienInfo.setForeground(new Color(40, 70, 40));

        lblPetugasInfo = new JLabel("Petugas: Memuat...");
        lblPetugasInfo.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblPetugasInfo.setForeground(new Color(80, 80, 80));

        pnlTopLeft.add(lblPasienInfo);
        pnlTopLeft.add(Box.createVerticalStrut(2));
        pnlTopLeft.add(lblPetugasInfo);

        JPanel pnlTopRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlTopRight.setOpaque(false);

        Button btnGantiPetugas = new Button();
        btnGantiPetugas.setIcon(loadIcon("/picture/190.png"));
        btnGantiPetugas.setText("Ganti Petugas");
        btnGantiPetugas.setPreferredSize(new Dimension(130, 26));
        btnGantiPetugas.addActionListener(e -> gantiPetugasManual());

        btnSimulasiKode = new Button();
        btnSimulasiKode.setIcon(loadIcon("/picture/category.png"));
        btnSimulasiKode.setText("⚡ Simulasi Kode Akses (UAT)");
        btnSimulasiKode.setPreferredSize(new Dimension(190, 26));
        btnSimulasiKode.addActionListener(e -> simulasiKodeAkses());

        pnlTopRight.add(btnGantiPetugas);
        pnlTopRight.add(btnSimulasiKode);

        pnlTop.add(pnlTopLeft, BorderLayout.WEST);
        pnlTop.add(pnlTopRight, BorderLayout.EAST);
        internalFrame1.add(pnlTop, BorderLayout.NORTH);

        // Center: Chromium / Fallback Container
        pnlChromiumContainer = new JPanel(new BorderLayout());
        pnlChromiumContainer.setBackground(Color.WHITE);

        pnlFallbackInfo = buildFallbackPanel();
        pnlChromiumContainer.add(pnlFallbackInfo, BorderLayout.CENTER);
        internalFrame1.add(pnlChromiumContainer, BorderLayout.CENTER);

        // Bottom toolbar
        panelisi pnlBottom = new panelisi();
        pnlBottom.setLayout(new BorderLayout());
        pnlBottom.setPreferredSize(new Dimension(100, 42));

        JPanel pnlStatusLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        pnlStatusLeft.setOpaque(false);

        progressBar = new JProgressBar();
        progressBar.setPreferredSize(new Dimension(120, 16));
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);

        lblStatus = new JLabel("Menyiapkan sesi verifikasi...");
        lblStatus.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblStatus.setForeground(new Color(60, 60, 60));

        pnlStatusLeft.add(progressBar);
        pnlStatusLeft.add(lblStatus);

        JPanel pnlActionsRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 6));
        pnlActionsRight.setOpaque(false);

        btnMuatUlang = new Button();
        btnMuatUlang.setIcon(loadIcon("/picture/refresh.png"));
        btnMuatUlang.setText("Muat Ulang");
        btnMuatUlang.setPreferredSize(new Dimension(110, 28));
        btnMuatUlang.addActionListener(e -> requestValidationUrl());

        btnBukaBrowser = new Button();
        btnBukaBrowser.setIcon(loadIcon("/picture/190.png"));
        btnBukaBrowser.setText("Buka di Browser Luar");
        btnBukaBrowser.setPreferredSize(new Dimension(160, 28));
        btnBukaBrowser.addActionListener(e -> openInExternalBrowser());

        btnSalinUrl = new Button();
        btnSalinUrl.setText("Salin URL");
        btnSalinUrl.setPreferredSize(new Dimension(90, 28));
        btnSalinUrl.addActionListener(e -> salinUrlKeClipboard());

        btnDetailPayload = new Button();
        btnDetailPayload.setText("Detail Payload");
        btnDetailPayload.setPreferredSize(new Dimension(115, 28));
        btnDetailPayload.addActionListener(e -> showDetailPayload());

        Button btnKeluar = new Button();
        btnKeluar.setIcon(loadIcon("/picture/exit.png"));
        btnKeluar.setText("Keluar");
        btnKeluar.setPreferredSize(new Dimension(90, 28));
        btnKeluar.addActionListener(e -> dispose());

        pnlActionsRight.add(btnMuatUlang);
        pnlActionsRight.add(btnBukaBrowser);
        pnlActionsRight.add(btnSalinUrl);
        pnlActionsRight.add(btnDetailPayload);
        pnlActionsRight.add(btnKeluar);

        pnlBottom.add(pnlStatusLeft, BorderLayout.WEST);
        pnlBottom.add(pnlActionsRight, BorderLayout.EAST);
        internalFrame1.add(pnlBottom, BorderLayout.SOUTH);

        getContentPane().add(internalFrame1);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanupBrowser();
            }
            @Override
            public void windowClosed(WindowEvent e) {
                cleanupBrowser();
            }
        });
    }

    private JPanel buildFallbackPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(250, 252, 254));
        card.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel title = new JLabel("Portal Verifikasi Profil SATUSEHAT KYC");
        title.setFont(new Font("Tahoma", Font.BOLD, 15));
        title.setForeground(new Color(20, 90, 45));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel desc = new JLabel("<html><center>Petugas verifikasi dapat membuka portal validasi di bawah ini untuk memeriksa kesesuaian KTP fisik dengan profil SATUSEHAT Mobile pasien.</center></html>");
        desc.setFont(new Font("Tahoma", Font.PLAIN, 12));
        desc.setForeground(new Color(80, 80, 80));
        desc.setAlignmentX(Component.CENTER_ALIGNMENT);

        txtValidationUrl = new JTextField();
        txtValidationUrl.setEditable(false);
        txtValidationUrl.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtValidationUrl.setMaximumSize(new Dimension(800, 30));
        txtValidationUrl.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);

        Button btnAppMode = new Button();
        btnAppMode.setIcon(new ImageIcon(getClass().getResource("/picture/190.png")));
        btnAppMode.setText("Buka di Browser Dedicated (Chrome/Edge App Mode)");
        btnAppMode.setPreferredSize(new Dimension(340, 32));
        btnAppMode.addActionListener(e -> openInExternalBrowser());

        Button btnCopy = new Button();
        btnCopy.setText("Salin URL Validasi");
        btnCopy.setPreferredSize(new Dimension(150, 32));
        btnCopy.addActionListener(e -> salinUrlKeClipboard());

        btnRow.add(btnAppMode);
        btnRow.add(btnCopy);

        card.add(Box.createVerticalGlue());
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(desc);
        card.add(Box.createVerticalStrut(16));
        card.add(txtValidationUrl);
        card.add(Box.createVerticalStrut(16));
        card.add(btnRow);
        card.add(Box.createVerticalGlue());

        return card;
    }

    public void setPasien(String noRkmMedis, String namaPasien, String nikPasien) {
        this.noRkmMedis = noRkmMedis != null ? noRkmMedis.trim() : "";
        this.namaPasien = namaPasien != null ? namaPasien.trim() : "";
        this.nikPasien = nikPasien != null ? nikPasien.trim() : "";

        lblPasienInfo.setText("Pasien: [ " + this.noRkmMedis + " ] " + this.namaPasien + " | NIK: " + (this.nikPasien.isEmpty() ? "-" : this.nikPasien));

        resolveOperatorData();
        requestValidationUrl();
    }

    public String getNoRkmMedis() { return noRkmMedis; }
    public String getNamaPasien() { return namaPasien; }
    public String getNikPasien() { return nikPasien; }
    public String getAgentNik() { return agentNik; }
    public String getAgentName() { return agentName; }

    private void ensureConnection() {
        try {
            if (this.koneksi == null || this.koneksi.isClosed()) {
                this.koneksi = koneksiDB.condb();
            }
        } catch (Exception e) {
            this.koneksi = koneksiDB.condb();
        }
    }

    private void resolveOperatorData() {
        String loggedUserCode = akses.getkode();
        if (loggedUserCode == null || loggedUserCode.isEmpty()) {
            loggedUserCode = "admin";
        }

        ensureConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement("select pegawai.nama, ifnull(pegawai.no_ktp, '') as no_ktp from pegawai where pegawai.nik=?");
            ps.setString(1, loggedUserCode);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.agentName = rs.getString("nama");
                this.agentNik = rs.getString("no_ktp");
            }
        } catch (Exception e) {
            System.out.println("Error resolveOperatorData: " + e);
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }

        // If no_ktp is missing from pegawai, check user table or keep prompt
        if (this.agentName == null || this.agentName.isEmpty()) {
            this.agentName = loggedUserCode;
        }

        updatePetugasHeader();
    }

    private void updatePetugasHeader() {
        String maskedNik = "-";
        if (agentNik != null && agentNik.length() >= 16) {
            maskedNik = agentNik.substring(0, 6) + "******" + agentNik.substring(12);
        } else if (agentNik != null && !agentNik.isEmpty()) {
            maskedNik = agentNik;
        }
        lblPetugasInfo.setText("Petugas Verifikasi: " + agentName + " (NIK: " + maskedNik + ")");
    }

    private void gantiPetugasManual() {
        DlgCariPegawai dlg = new DlgCariPegawai(null, false);
        dlg.setSize(800, 500);
        dlg.setLocationRelativeTo(this);
        dlg.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                if (dlg.getTable().getSelectedRow() != -1) {
                    agentName = dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(), 1).toString();
                    String nikPegawai = dlg.getTable().getValueAt(dlg.getTable().getSelectedRow(), 0).toString();
                    agentNik = Sequel.cariIsi("select no_ktp from pegawai where nik=?", nikPegawai);
                    updatePetugasHeader();
                    requestValidationUrl();
                }
            }
        });
        dlg.setVisible(true);
    }

    private void requestValidationUrl() {
        if (java.awt.GraphicsEnvironment.isHeadless()) {
            return;
        }

        if (agentNik == null || agentNik.trim().length() < 16) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "NIK Petugas belum terdaftar (minimal 16 digit) pada data Pegawai.\nApakah Anda ingin memilih petugas yang memiliki NIK valid sekarang?",
                "NIK Petugas Tidak Lengkap", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                gantiPetugasManual();
                return;
            }
        }

        progressBar.setVisible(true);
        lblStatus.setText("Menghubungi SATUSEHAT KYC untuk membuat URL validasi...");
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        new Thread(() -> {
            SatuSehatKYCResponse res = api.generateUrl(agentNik, agentName);
            this.lastResponse = res;

            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                progressBar.setVisible(false);

                if (res.isSuccess() && !res.getValidationUrl().isEmpty()) {
                    this.currentToken = res.getToken();
                    this.currentValidationUrl = res.getValidationUrl();
                    txtValidationUrl.setText(currentValidationUrl);
                    lblStatus.setText("Portal verifikasi siap.");

                    // Record audit log
                    saveAuditLog(res);

                    // Load in JCEF or Fallback
                    loadChromiumOrFallback(currentValidationUrl);
                } else {
                    lblStatus.setText("Gagal: " + res.getMessage());
                    JOptionPane.showMessageDialog(this,
                        "Gagal membuat URL Validasi SATUSEHAT KYC:\n" + res.getMessage() +
                        (res.getErrorCode().isEmpty() ? "" : " (" + res.getErrorCode() + ")"),
                        "Pemberitahuan SATUSEHAT KYC", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    private void saveAuditLog(SatuSehatKYCResponse res) {
        ensureConnection();
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement(
                "insert into satu_sehat_kyc_log (" +
                "no_rkm_medis, nik_pasien, nama_pasien, nik_petugas, nama_petugas, " +
                "tanggal_akses, token, url_validasi, status" +
                ") values (?, ?, ?, ?, ?, NOW(), ?, ?, 'Proses')");
            ps.setString(1, this.noRkmMedis.isEmpty() ? null : this.noRkmMedis);
            ps.setString(2, this.nikPasien);
            ps.setString(3, this.namaPasien);
            ps.setString(4, this.agentNik);
            ps.setString(5, this.agentName);
            ps.setString(6, res.getToken());
            ps.setString(7, res.getValidationUrl());
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error saving satu_sehat_kyc_log: " + e);
        } finally {
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }
    }

    private void loadChromiumOrFallback(String url) {
        CefApp app = SatuSehatBrowserManager.getCefApp();

        if (app != null) {
            try {
                if (cefClient == null) {
                    cefClient = app.createClient();
                }
                if (cefBrowser == null) {
                    cefBrowser = cefClient.createBrowser(url, false, false);
                    cefComponent = cefBrowser.getUIComponent();
                    pnlChromiumContainer.removeAll();
                    pnlChromiumContainer.add(cefComponent, BorderLayout.CENTER);
                    pnlChromiumContainer.revalidate();
                    pnlChromiumContainer.repaint();
                } else {
                    cefBrowser.loadURL(url);
                }
                return;
            } catch (Throwable t) {
                System.err.println("Error displaying CefBrowser in KYC: " + t.getMessage());
            }
        }

        // Fallback: Show fallback card
        pnlChromiumContainer.removeAll();
        pnlChromiumContainer.add(pnlFallbackInfo, BorderLayout.CENTER);
        pnlChromiumContainer.revalidate();
        pnlChromiumContainer.repaint();
        openInExternalBrowser();
    }

    private void openInExternalBrowser() {
        if (currentValidationUrl == null || currentValidationUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tautan validasi belum tersedia.");
            return;
        }
        SatuSehatBrowserManager.openInAppModeOrBrowser(currentValidationUrl, this);
    }

    private void salinUrlKeClipboard() {
        if (currentValidationUrl != null && !currentValidationUrl.isEmpty()) {
            java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                new StringSelection(currentValidationUrl), null);
            JOptionPane.showMessageDialog(this, "URL Validasi berhasil disalin ke clipboard.", "Info", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "URL Validasi belum tersedia.");
        }
    }

    private void simulasiKodeAkses() {
        if (nikPasien == null || nikPasien.isEmpty()) {
            JOptionPane.showMessageDialog(this, "NIK Pasien tidak valid atau belum diisi.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblStatus.setText("Menghasilkan simulasi kode akses (UAT)...");

        new Thread(() -> {
            SatuSehatKYCResponse res = api.generateChallengeCode(nikPasien, namaPasien, currentToken);
            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                lblStatus.setText("Selesai simulasi.");
                if (res.isSuccess() && !res.getChallengeCode().isEmpty()) {
                    JDialog simDialog = new JDialog(this, "Simulasi Kode Akses SATUSEHAT Mobile (UAT)", true);
                    simDialog.setLayout(new BorderLayout());
                    simDialog.setSize(450, 220);
                    simDialog.setLocationRelativeTo(this);

                    JPanel pnl = new JPanel();
                    pnl.setLayout(new BoxLayout(pnl, BoxLayout.Y_AXIS));
                    pnl.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
                    pnl.setBackground(new Color(250, 252, 250));

                    JLabel lblT = new JLabel("Kode Akses Simulasi SATUSEHAT Mobile:");
                    lblT.setFont(new Font("Tahoma", Font.BOLD, 12));
                    lblT.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JLabel lblCode = new JLabel(res.getChallengeCode());
                    lblCode.setFont(new Font("Tahoma", Font.BOLD, 36));
                    lblCode.setForeground(new Color(20, 120, 50));
                    lblCode.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JLabel lblExp = new JLabel("Kadaluarsa: " + res.getExpiredTimestamp());
                    lblExp.setFont(new Font("Tahoma", Font.PLAIN, 11));
                    lblExp.setForeground(Color.GRAY);
                    lblExp.setAlignmentX(Component.CENTER_ALIGNMENT);

                    JPanel btnPnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
                    btnPnl.setOpaque(false);
                    Button btnCopyCode = new Button();
                    btnCopyCode.setText("Salin Kode");
                    btnCopyCode.setPreferredSize(new Dimension(100, 28));
                    btnCopyCode.addActionListener(ev -> {
                        java.awt.Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(res.getChallengeCode()), null);
                        JOptionPane.showMessageDialog(simDialog, "Kode akses berhasil disalin!");
                    });

                    Button btnTutup = new Button();
                    btnTutup.setText("Tutup");
                    btnTutup.setPreferredSize(new Dimension(90, 28));
                    btnTutup.addActionListener(ev -> simDialog.dispose());

                    btnPnl.add(btnCopyCode);
                    btnPnl.add(btnTutup);

                    pnl.add(lblT);
                    pnl.add(Box.createVerticalStrut(10));
                    pnl.add(lblCode);
                    pnl.add(Box.createVerticalStrut(6));
                    pnl.add(lblExp);
                    pnl.add(btnPnl);

                    simDialog.add(pnl, BorderLayout.CENTER);
                    simDialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Gagal mendapatkan kode akses simulasi:\n" + res.getMessage());
                }
            });
        }).start();
    }

    private void showDetailPayload() {
        JDialog payloadDialog = new JDialog(this, "Detail Payload Transaksi SATUSEHAT KYC", true);
        payloadDialog.setSize(750, 520);
        payloadDialog.setLocationRelativeTo(this);
        payloadDialog.setLayout(new BorderLayout());

        JPanel pnl = new JPanel(new BorderLayout(8, 8));
        pnl.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextArea txt = new JTextArea();
        txt.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txt.setEditable(false);

        StringBuilder sb = new StringBuilder();
        sb.append("=== SATUSEHAT KYC DIAGNOSTIC PAYLOAD ===\n\n");
        if (lastResponse != null) {
            sb.append("HTTP Status: ").append(lastResponse.getStatusCode()).append("\n");
            sb.append("Success    : ").append(lastResponse.isSuccess()).append("\n");
            sb.append("Token      : ").append(lastResponse.getToken()).append("\n");
            sb.append("URL        : ").append(lastResponse.getValidationUrl()).append("\n\n");
            sb.append("--- REQUEST BODY ---\n");
            sb.append(SatuSehatKYCApi.prettyPrintJson(lastResponse.getRawRequestBody())).append("\n\n");
            sb.append("--- RESPONSE BODY ---\n");
            sb.append(SatuSehatKYCApi.prettyPrintJson(lastResponse.getRawResponseBody())).append("\n");
        } else {
            sb.append("Belum ada data transaksi.");
        }

        txt.setText(sb.toString());
        pnl.add(new JScrollPane(txt), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        Button btnClose = new Button();
        btnClose.setText("Tutup");
        btnClose.addActionListener(e -> payloadDialog.dispose());
        bottom.add(btnClose);
        pnl.add(bottom, BorderLayout.SOUTH);

        payloadDialog.add(pnl);
        payloadDialog.setVisible(true);
    }

    private void cleanupBrowser() {
        if (cefBrowser != null) {
            SatuSehatBrowserManager.safeCloseBrowser(cefBrowser);
            cefBrowser = null;
        }
    }
}
