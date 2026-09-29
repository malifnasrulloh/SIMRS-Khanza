package bridging;

import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.CardLayout;
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
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import me.friwi.jcefmaven.CefAppBuilder;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import widget.Button;
import widget.InternalFrame;
import widget.panelisi;

/**
 * Dialog form for viewing SATUSEHAT Rekam Medis Elektronik (SSRME) V2.0.
 * Embeds full Chromium browser (JCEF) in-app with native WebCrypto/HPKE decryption support,
 * combined with dynamic QR verification, polling, emergency bypass, and automatic browser fallback.
 */
public class SatuSehatRMEView extends JDialog {
    private static final String CARD_VERIFY = "CARD_VERIFY";
    private static final String CARD_VIEWER = "CARD_VIEWER";
    private static final String VERIFY_MODE_WEB = "VERIFY_MODE_WEB";
    private static final String VERIFY_MODE_QR = "VERIFY_MODE_QR";
    private static final int POLLING_INTERVAL_MS = 5000;
    private static final int MAX_POLLING_SECONDS = 90;

    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final SatuSehatRMEApi api = new SatuSehatRMEApi();
    private final SatuSehatCekNIK cekNik = new SatuSehatCekNIK();
    private final QrRenderer qrRenderer;
    private Connection koneksi = koneksiDB.condb();

    private CardLayout cardLayout;
    private JPanel mainCardPanel;

    // Verify card components
    private JLabel lblStatusPasien;
    private JLabel lblStatusDokter;
    private CardLayout verifyCardLayout;
    private JPanel pnlVerifyCards;
    private JPanel pnlVerifyBrowserContainer;
    private Button btnSwitchToQr;
    private Button btnSwitchToWeb;
    private JLabel lblQrImage;
    private JTextField txtVerificationUrl;
    private JLabel lblPollingStatus;
    private JProgressBar progressBar;
    private Button btnPeriksaManual;
    private Button btnEmergencyBypass;
    private Button btnSalinUrlConsent;
    private Button btnBukaBrowserConsent;
    private Button btnDetailPayload;

    // Verify JCEF instance
    private CefBrowser cefVerifyBrowser;
    private Component cefVerifyComponent;

    // Viewer card components
    private JLabel lblViewerTitle;
    private JPanel pnlChromiumContainer;
    private JPanel pnlFallbackInfo;
    private JLabel lblViewerPasienInfo;
    private JLabel lblViewerDokterInfo;
    private JLabel lblViewerFaskesInfo;
    private JLabel lblViewerConsentInfo;
    private JLabel lblViewerExpiryInfo;
    private JTextField txtViewerShlinkUrl;
    private Button btnBukaBrowser;
    private Button btnRefreshViewer;
    private Button btnSalinUrlViewer;
    private Button btnKembaliQr;
    private Button btnDetailPayloadViewer;

    // JCEF client instance for this dialog
    private CefClient cefClient;
    private CefBrowser cefBrowser;
    private Component cefComponent;

    // State data
    private String noRawat = "";
    private String noRkmMedis = "";
    private String nmPasien = "";
    private String noKtpPasien = "";
    private String ihsPasien = "";

    private String kdDokter = "";
    private String nmDokter = "";
    private String noKtpDokter = "";
    private String ihsDokter = "";

    private String orgId = "";
    private String orgName = "";

    private String currentShlinkUrl = "";
    private String currentVerificationUrl = "";
    private String currentConsentId = "";
    private String currentExpiry = "";
    private Timer pollingTimer;
    private int remainingPollingSeconds = MAX_POLLING_SECONDS;

    private SatuSehatRMEResponse lastResponse;
    private String lastRequestBody = "";
    private String lastResponseBody = "";

    public SatuSehatRMEView(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        this.qrRenderer = initRenderer();
        initComponents();
        setupListeners();
    }

    public SatuSehatRMEView(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        this.qrRenderer = initRenderer();
        initComponents();
        setupListeners();
    }

    private QrRenderer initRenderer() {
        try {
            return new QrRendererZxing();
        } catch (Throwable t) {
            return QrRendererFactory.buat();
        }
    }

    private ImageIcon safeIcon(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            return url != null ? new ImageIcon(url) : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(980, 680);
        setTitle("SATUSEHAT Rekam Medis Elektronik (SSRME) Nasional");

        InternalFrame internalFrame = new InternalFrame();
        internalFrame.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
            "::[ Rekam Medis Elektronik (RME) Nasional SATUSEHAT ]::",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            new java.awt.Font("Tahoma", 0, 11),
            new java.awt.Color(50, 50, 50)
        ));
        internalFrame.setName("internalFrame1");
        internalFrame.setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setOpaque(false);

        mainCardPanel.add(buildVerifyPanel(), CARD_VERIFY);
        mainCardPanel.add(buildViewerPanel(), CARD_VIEWER);

        internalFrame.add(mainCardPanel, BorderLayout.CENTER);
        getContentPane().add(internalFrame);
    }

    private JPanel buildVerifyPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(new Color(250, 252, 254));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

        // Top info header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 225, 230)));

        JLabel lblTitle = new JLabel("Verifikasi Persetujuan Akses Rekam Medis Nasional");
        lblTitle.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblTitle.setForeground(new Color(30, 60, 90));
        headerPanel.add(lblTitle, BorderLayout.NORTH);

        JPanel infoGrid = new JPanel(new GridBagLayout());
        infoGrid.setOpaque(false);
        infoGrid.setBorder(BorderFactory.createEmptyBorder(6, 0, 8, 0));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(2, 5, 2, 5);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        lblStatusPasien = new JLabel("Pasien: -");
        lblStatusPasien.setFont(new Font("Tahoma", Font.PLAIN, 12));
        infoGrid.add(lblStatusPasien, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
        lblStatusDokter = new JLabel("Dokter: -");
        lblStatusDokter.setFont(new Font("Tahoma", Font.PLAIN, 12));
        infoGrid.add(lblStatusDokter, gbc);

        headerPanel.add(infoGrid, BorderLayout.CENTER);
        panel.add(headerPanel, BorderLayout.NORTH);

        // Center: CardLayout containing Web Form (Default) and QR Code
        verifyCardLayout = new CardLayout();
        pnlVerifyCards = new JPanel(verifyCardLayout);
        pnlVerifyCards.setOpaque(false);

        pnlVerifyCards.add(buildVerifyWebPanel(), VERIFY_MODE_WEB);
        pnlVerifyCards.add(buildVerifyQrPanel(), VERIFY_MODE_QR);

        panel.add(pnlVerifyCards, BorderLayout.CENTER);

        // South: Status, progress bar, and action buttons
        JPanel southPanel = new JPanel(new BorderLayout(5, 5));
        southPanel.setOpaque(false);

        JPanel statusRow = new JPanel();
        statusRow.setLayout(new BoxLayout(statusRow, BoxLayout.Y_AXIS));
        statusRow.setOpaque(false);
        statusRow.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 5));

        lblPollingStatus = new JLabel("Menunggu persetujuan pasien...");
        lblPollingStatus.setAlignmentX(CENTER_ALIGNMENT);
        lblPollingStatus.setFont(new Font("Tahoma", Font.ITALIC, 11));
        lblPollingStatus.setForeground(new Color(80, 90, 100));
        statusRow.add(lblPollingStatus);
        statusRow.add(Box.createVerticalStrut(3));

        progressBar = new JProgressBar();
        progressBar.setMaximumSize(new Dimension(350, 12));
        progressBar.setIndeterminate(true);
        progressBar.setAlignmentX(CENTER_ALIGNMENT);
        statusRow.add(progressBar);

        southPanel.add(statusRow, BorderLayout.NORTH);

        panelisi bottomPanel = new panelisi();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 8, 6));

        btnPeriksaManual = new Button();
        btnPeriksaManual.setIcon(safeIcon("/picture/refresh.png"));
        btnPeriksaManual.setText("Periksa Persetujuan");
        btnPeriksaManual.setPreferredSize(new Dimension(160, 30));

        btnEmergencyBypass = new Button();
        btnEmergencyBypass.setIcon(safeIcon("/picture/011.png"));
        btnEmergencyBypass.setText("Bypass Darurat (Emergency)");
        btnEmergencyBypass.setPreferredSize(new Dimension(210, 30));
        btnEmergencyBypass.setForeground(new Color(160, 30, 30));

        btnDetailPayload = new Button();
        btnDetailPayload.setText("Detail Payload");
        btnDetailPayload.setPreferredSize(new Dimension(120, 30));

        btnBukaBrowserConsent = new Button();
        btnBukaBrowserConsent.setIcon(safeIcon("/picture/190.png"));
        btnBukaBrowserConsent.setText("Buka di Browser Luar");
        btnBukaBrowserConsent.setPreferredSize(new Dimension(165, 30));

        Button btnBatal = new Button();
        btnBatal.setIcon(safeIcon("/picture/exit.png"));
        btnBatal.setText("Batal / Tutup");
        btnBatal.setPreferredSize(new Dimension(120, 30));
        btnBatal.addActionListener(e -> dispose());

        bottomPanel.add(btnPeriksaManual);
        bottomPanel.add(btnEmergencyBypass);
        bottomPanel.add(btnDetailPayload);
        bottomPanel.add(btnBukaBrowserConsent);
        bottomPanel.add(btnBatal);

        southPanel.add(bottomPanel, BorderLayout.SOUTH);
        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildVerifyWebPanel() {
        JPanel pnl = new JPanel(new BorderLayout(5, 5));
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel topBanner = new JPanel(new BorderLayout(5, 5));
        topBanner.setOpaque(false);

        JLabel lblHint = new JLabel("<html><b>Mode Input Kode Akses:</b> Masukkan <b>6 digit Kode Akses</b> dari SATUSEHAT Mobile pasien pada formulir web di bawah:</html>");
        lblHint.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblHint.setForeground(new Color(40, 60, 80));
        topBanner.add(lblHint, BorderLayout.WEST);

        btnSwitchToQr = new Button();
        btnSwitchToQr.setIcon(safeIcon("/picture/accept.png"));
        btnSwitchToQr.setText("Peralihkan ke Pindai QR Code");
        btnSwitchToQr.setPreferredSize(new Dimension(215, 28));
        btnSwitchToQr.addActionListener(e -> verifyCardLayout.show(pnlVerifyCards, VERIFY_MODE_QR));
        topBanner.add(btnSwitchToQr, BorderLayout.EAST);

        pnl.add(topBanner, BorderLayout.NORTH);

        pnlVerifyBrowserContainer = new JPanel(new BorderLayout());
        pnlVerifyBrowserContainer.setBackground(Color.WHITE);
        pnlVerifyBrowserContainer.setBorder(BorderFactory.createLineBorder(new Color(215, 225, 235), 1));
        pnlVerifyBrowserContainer.add(buildVerifyFallbackPanel(), BorderLayout.CENTER);
        pnl.add(pnlVerifyBrowserContainer, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel buildVerifyQrPanel() {
        JPanel pnl = new JPanel(new BorderLayout(5, 5));
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel topBanner = new JPanel(new BorderLayout(5, 5));
        topBanner.setOpaque(false);

        JLabel lblHint = new JLabel("<html><b>Mode Pindai QR Code:</b> Pasien memindai QR Code menggunakan aplikasi SATUSEHAT Mobile:</html>");
        lblHint.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblHint.setForeground(new Color(40, 60, 80));
        topBanner.add(lblHint, BorderLayout.WEST);

        btnSwitchToWeb = new Button();
        btnSwitchToWeb.setIcon(safeIcon("/picture/190.png"));
        btnSwitchToWeb.setText("Kembali ke Form Kode Akses (Web)");
        btnSwitchToWeb.setPreferredSize(new Dimension(245, 28));
        btnSwitchToWeb.addActionListener(e -> verifyCardLayout.show(pnlVerifyCards, VERIFY_MODE_WEB));
        topBanner.add(btnSwitchToWeb, BorderLayout.EAST);

        pnl.add(topBanner, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        lblQrImage = new JLabel();
        lblQrImage.setAlignmentX(CENTER_ALIGNMENT);
        lblQrImage.setPreferredSize(new Dimension(260, 260));
        lblQrImage.setMaximumSize(new Dimension(260, 260));
        lblQrImage.setBorder(BorderFactory.createLineBorder(new Color(210, 220, 230), 1));
        lblQrImage.setHorizontalAlignment(SwingConstants.CENTER);
        lblQrImage.setText("Membuat tautan persetujuan...");
        centerPanel.add(lblQrImage);
        centerPanel.add(Box.createVerticalStrut(12));

        JPanel urlRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        urlRow.setOpaque(false);
        txtVerificationUrl = new JTextField(40);
        txtVerificationUrl.setEditable(false);
        txtVerificationUrl.setFont(new Font("Tahoma", Font.PLAIN, 11));
        btnSalinUrlConsent = new Button();
        btnSalinUrlConsent.setText("Salin Tautan");
        btnSalinUrlConsent.setPreferredSize(new Dimension(100, 25));
        urlRow.add(txtVerificationUrl);
        urlRow.add(btnSalinUrlConsent);
        centerPanel.add(urlRow);

        pnl.add(centerPanel, BorderLayout.CENTER);

        return pnl;
    }

    private JPanel buildVerifyFallbackPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(250, 252, 254));
        card.setBorder(BorderFactory.createEmptyBorder(40, 30, 40, 30));

        JLabel lblHeader = new JLabel("Formulir Verifikasi Kode Akses SATUSEHAT");
        lblHeader.setFont(new Font("Tahoma", Font.BOLD, 15));
        lblHeader.setForeground(new Color(40, 60, 80));
        lblHeader.setAlignmentX(CENTER_ALIGNMENT);
        card.add(lblHeader);
        card.add(Box.createVerticalStrut(10));

        JLabel lblMsg = new JLabel("<html><center>Memuat formulir verifikasi Kemenkes SATUSEHAT...<br>Jika formulir belum tampil, silakan tunggu atau buka melalui peramban web eksternal.</center></html>");
        lblMsg.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblMsg.setForeground(new Color(90, 100, 110));
        lblMsg.setAlignmentX(CENTER_ALIGNMENT);
        card.add(lblMsg);
        card.add(Box.createVerticalStrut(20));

        Button btnOpenExt = new Button();
        btnOpenExt.setText("Buka Form di Browser Eksternal (Chrome/Edge)");
        btnOpenExt.setPreferredSize(new Dimension(300, 32));
        btnOpenExt.setMaximumSize(new Dimension(300, 32));
        btnOpenExt.setAlignmentX(CENTER_ALIGNMENT);
        btnOpenExt.addActionListener(e -> openVerificationInExternalBrowser());
        card.add(btnOpenExt);

        return card;
    }

    private void loadVerifyBrowserOrFallback(String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }

        CefApp app = SatuSehatBrowserManager.getCefApp();
        if (app != null) {
            try {
                if (cefClient == null) {
                    cefClient = app.createClient();
                }
                if (cefVerifyBrowser == null) {
                    cefVerifyBrowser = cefClient.createBrowser(url, false, false);
                    cefVerifyComponent = cefVerifyBrowser.getUIComponent();
                    pnlVerifyBrowserContainer.removeAll();
                    pnlVerifyBrowserContainer.add(cefVerifyComponent, BorderLayout.CENTER);
                    pnlVerifyBrowserContainer.revalidate();
                    pnlVerifyBrowserContainer.repaint();
                } else {
                    cefVerifyBrowser.loadURL(url);
                }
                return;
            } catch (Throwable t) {
                System.err.println("Error displaying CefBrowser for verification: " + t.getMessage());
            }
        }

        pnlVerifyBrowserContainer.removeAll();
        pnlVerifyBrowserContainer.add(buildVerifyFallbackPanel(), BorderLayout.CENTER);
        pnlVerifyBrowserContainer.revalidate();
        pnlVerifyBrowserContainer.repaint();
    }

    private void openVerificationInExternalBrowser() {
        if (currentVerificationUrl == null || currentVerificationUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tautan verifikasi belum tersedia.");
            return;
        }
        SatuSehatBrowserManager.openInAppModeOrBrowser(currentVerificationUrl, this);
    }

    private JPanel buildViewerPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        // Top toolbar
        panelisi topToolbar = new panelisi();
        topToolbar.setLayout(new BorderLayout(5, 5));

        lblViewerTitle = new JLabel(" SATUSEHAT Rekam Medis Elektronik Nasional");
        lblViewerTitle.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblViewerTitle.setIcon(safeIcon("/picture/category.png"));
        topToolbar.add(lblViewerTitle, BorderLayout.WEST);

        JPanel toolButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 2));
        toolButtons.setOpaque(false);

        btnBukaBrowser = new Button();
        btnBukaBrowser.setIcon(safeIcon("/picture/190.png"));
        btnBukaBrowser.setText("Buka di Browser Eksternal (Chrome/Edge)");
        btnBukaBrowser.setPreferredSize(new Dimension(270, 28));

        btnRefreshViewer = new Button();
        btnRefreshViewer.setIcon(safeIcon("/picture/refresh.png"));
        btnRefreshViewer.setText("Muat Ulang");
        btnRefreshViewer.setPreferredSize(new Dimension(110, 28));

        btnSalinUrlViewer = new Button();
        btnSalinUrlViewer.setText("Salin URL");
        btnSalinUrlViewer.setPreferredSize(new Dimension(90, 28));

        btnDetailPayloadViewer = new Button();
        btnDetailPayloadViewer.setText("Detail Payload");
        btnDetailPayloadViewer.setPreferredSize(new Dimension(120, 28));

        btnKembaliQr = new Button();
        btnKembaliQr.setText("Info Sesi / QR");
        btnKembaliQr.setPreferredSize(new Dimension(110, 28));

        Button btnTutupViewer = new Button();
        btnTutupViewer.setIcon(safeIcon("/picture/exit.png"));
        btnTutupViewer.setText("Tutup");
        btnTutupViewer.setPreferredSize(new Dimension(85, 28));
        btnTutupViewer.addActionListener(e -> dispose());

        toolButtons.add(btnBukaBrowser);
        toolButtons.add(btnRefreshViewer);
        toolButtons.add(btnSalinUrlViewer);
        toolButtons.add(btnDetailPayloadViewer);
        toolButtons.add(btnKembaliQr);
        toolButtons.add(btnTutupViewer);

        topToolbar.add(toolButtons, BorderLayout.EAST);
        panel.add(topToolbar, BorderLayout.NORTH);

        // Center: JCEF Container with Fallback panel
        pnlChromiumContainer = new JPanel(new BorderLayout());
        pnlChromiumContainer.setBackground(Color.WHITE);

        pnlFallbackInfo = buildFallbackPanel();
        pnlChromiumContainer.add(pnlFallbackInfo, BorderLayout.CENTER);

        panel.add(pnlChromiumContainer, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildFallbackPanel() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(250, 252, 254));
        card.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel lblSuccessHeader = new JLabel("✓ Rekam Medis Elektronik Nasional Terbuka di Browser");
        lblSuccessHeader.setFont(new Font("Tahoma", Font.BOLD, 17));
        lblSuccessHeader.setForeground(new Color(25, 125, 60));
        lblSuccessHeader.setAlignmentX(CENTER_ALIGNMENT);
        card.add(lblSuccessHeader);
        card.add(Box.createVerticalStrut(8));

        JLabel lblDesc = new JLabel("<html><center>Data Rekam Medis Elektronik Nasional didekripsi menggunakan enkripsi HPKE dan ditampilkan secara lengkap pada peramban web eksternal (Chrome/Edge).<br>Gunakan tombol di bawah ini jika ingin membuka ulang atau menyalin tautan rekam medis.</center></html>");
        lblDesc.setFont(new Font("Tahoma", Font.PLAIN, 12));
        lblDesc.setForeground(new Color(80, 90, 100));
        lblDesc.setAlignmentX(CENTER_ALIGNMENT);
        card.add(lblDesc);
        card.add(Box.createVerticalStrut(20));

        JPanel infoBox = new JPanel(new GridBagLayout());
        infoBox.setBackground(Color.WHITE);
        infoBox.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(215, 225, 235), 1),
            BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        infoBox.setMaximumSize(new Dimension(720, 160));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 8, 3, 8);

        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.5;
        lblViewerPasienInfo = new JLabel("Pasien: -");
        lblViewerPasienInfo.setFont(new Font("Tahoma", Font.PLAIN, 12));
        infoBox.add(lblViewerPasienInfo, gbc);

        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5;
        lblViewerDokterInfo = new JLabel("Dokter: -");
        lblViewerDokterInfo.setFont(new Font("Tahoma", Font.PLAIN, 12));
        infoBox.add(lblViewerDokterInfo, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        lblViewerFaskesInfo = new JLabel("Fasyankes: -");
        lblViewerFaskesInfo.setFont(new Font("Tahoma", Font.PLAIN, 12));
        infoBox.add(lblViewerFaskesInfo, gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        lblViewerConsentInfo = new JLabel("Consent ID: -");
        lblViewerConsentInfo.setFont(new Font("Tahoma", Font.PLAIN, 12));
        infoBox.add(lblViewerConsentInfo, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        lblViewerExpiryInfo = new JLabel("Masa Berlaku Sesi: -");
        lblViewerExpiryInfo.setFont(new Font("Tahoma", Font.ITALIC, 11));
        lblViewerExpiryInfo.setForeground(new Color(100, 110, 120));
        infoBox.add(lblViewerExpiryInfo, gbc);

        card.add(infoBox);
        card.add(Box.createVerticalStrut(18));

        JPanel urlRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        urlRow.setOpaque(false);
        txtViewerShlinkUrl = new JTextField(46);
        txtViewerShlinkUrl.setEditable(false);
        txtViewerShlinkUrl.setFont(new Font("Tahoma", Font.PLAIN, 11));

        Button btnSalinUrlInline = new Button();
        btnSalinUrlInline.setText("Salin Tautan");
        btnSalinUrlInline.setPreferredSize(new Dimension(100, 25));
        btnSalinUrlInline.addActionListener(e -> {
            if (!currentShlinkUrl.isEmpty()) {
                StringSelection sel = new StringSelection(currentShlinkUrl);
                getToolkit().getSystemClipboard().setContents(sel, sel);
                JOptionPane.showMessageDialog(this, "Tautan Smart Health Link disalin ke clipboard!");
            }
        });
        urlRow.add(txtViewerShlinkUrl);
        urlRow.add(btnSalinUrlInline);
        card.add(urlRow);
        card.add(Box.createVerticalStrut(20));

        Button btnBukaUlang = new Button();
        btnBukaUlang.setIcon(safeIcon("/picture/190.png"));
        btnBukaUlang.setText("Buka Kembali di Browser (Chrome/Edge)");
        btnBukaUlang.setPreferredSize(new Dimension(300, 36));
        btnBukaUlang.setFont(new Font("Tahoma", Font.BOLD, 12));
        btnBukaUlang.setAlignmentX(CENTER_ALIGNMENT);
        btnBukaUlang.addActionListener(e -> openInExternalBrowser());
        card.add(btnBukaUlang);

        return card;
    }

    private void setupListeners() {
        btnBukaBrowserConsent.addActionListener(e -> openVerificationInExternalBrowser());
        btnPeriksaManual.addActionListener(e -> checkConsentStatusManual());
        btnEmergencyBypass.addActionListener(e -> handleEmergencyBypass());
        btnDetailPayload.addActionListener(e -> showDetailPayloadDialog());
        btnDetailPayloadViewer.addActionListener(e -> showDetailPayloadDialog());

        btnSalinUrlConsent.addActionListener(e -> {
            if (!txtVerificationUrl.getText().isEmpty()) {
                StringSelection sel = new StringSelection(txtVerificationUrl.getText());
                getToolkit().getSystemClipboard().setContents(sel, sel);
                JOptionPane.showMessageDialog(this, "Tautan verifikasi disalin ke clipboard!");
            }
        });

        btnBukaBrowser.addActionListener(e -> openInExternalBrowser());

        btnRefreshViewer.addActionListener(e -> {
            if (cefBrowser != null && !currentShlinkUrl.isEmpty()) {
                cefBrowser.reload();
            } else if (!currentShlinkUrl.isEmpty()) {
                openInExternalBrowser();
            }
        });

        btnSalinUrlViewer.addActionListener(e -> {
            if (!currentShlinkUrl.isEmpty()) {
                StringSelection sel = new StringSelection(currentShlinkUrl);
                getToolkit().getSystemClipboard().setContents(sel, sel);
                JOptionPane.showMessageDialog(this, "Tautan Smart Health Link disalin ke clipboard!");
            }
        });

        btnKembaliQr.addActionListener(e -> cardLayout.show(mainCardPanel, CARD_VERIFY));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanupOnClose();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                cleanupOnClose();
            }
        });
    }

    private void cleanupOnClose() {
        stopPolling();
        if (cefVerifyBrowser != null) {
            SatuSehatBrowserManager.safeCloseBrowser(cefVerifyBrowser);
            cefVerifyBrowser = null;
            cefVerifyComponent = null;
        }
        if (cefBrowser != null) {
            SatuSehatBrowserManager.safeCloseBrowser(cefBrowser);
            cefBrowser = null;
            cefComponent = null;
        }
    }

    /**
     * Initializes the dialog for a specific patient encounter and attending doctor.
     */
    public void setPasien(String noRawat, String kdDokterVisit) {
        this.noRawat = noRawat != null ? noRawat.trim() : "";
        this.kdDokter = kdDokterVisit != null ? kdDokterVisit.trim() : "";

        this.orgId = koneksiDB.IDSATUSEHAT();
        this.orgName = akses.getnamars();

        resolvePatientData();
        resolvePractitionerData();

        lblStatusPasien.setText("Pasien: " + nmPasien + " (RM: " + noRkmMedis + " | NIK: " + (noKtpPasien.isEmpty() ? "-" : noKtpPasien) + ")");
        lblStatusDokter.setText("Dokter: " + nmDokter + " (IHS: " + (ihsDokter.isEmpty() ? "Belum Terdaftar" : ihsDokter) + ")");

        // Check if there is an active valid session in database (cross-encounter for same patient & doctor)
        String cachedUrl = findActiveSession(this.noRawat, this.ihsPasien, this.ihsDokter);
        if (cachedUrl != null && !cachedUrl.isEmpty()) {
            this.currentShlinkUrl = cachedUrl;
            updateViewerCard(cachedUrl, this.currentConsentId, this.currentExpiry);
            cardLayout.show(mainCardPanel, CARD_VIEWER);
            loadChromiumOrFallback(currentShlinkUrl);
            return;
        }

        // Validate prerequisites
        if (this.ihsPasien.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Pasien " + nmPasien + " belum memiliki Nomor IHS SATUSEHAT.\nPastikan NIK 16 digit terisi valid pada data rekam medis pasien.",
                "Peringatan Data Pasien", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (this.ihsDokter.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Dokter " + nmDokter + " belum memiliki Nomor IHS Praktisi SATUSEHAT.\nPastikan NIK dokter terisi valid pada data pegawai.",
                "Peringatan Data Dokter", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Initiate new Consent Health Link
        initiateConsentHealthLink();
    }

    private void ensureConnection() {
        try {
            if (koneksi == null || koneksi.isClosed()) {
                koneksi = koneksiDB.condb();
            }
        } catch (Exception e) {
            koneksi = koneksiDB.condb();
        }
    }

    private void resolvePatientData() {
        ensureConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                 "select reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.no_ktp, " +
                 "ifnull(satu_sehat_ihs_patient.ihspasien, '') as ihspasien " +
                 "from reg_periksa " +
                 "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                 "left join satu_sehat_ihs_patient on satu_sehat_ihs_patient.nikpasien=pasien.no_ktp " +
                 "where reg_periksa.no_rawat=?");
            ps.setString(1, this.noRawat);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.noRkmMedis = rs.getString("no_rkm_medis");
                this.nmPasien = rs.getString("nm_pasien");
                this.noKtpPasien = rs.getString("no_ktp");
                this.ihsPasien = rs.getString("ihspasien");
            }
        } catch (Exception e) {
            System.out.println("Error resolvePatientData: " + e);
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }

        // On-the-fly IHS lookup if missing
        if ((this.ihsPasien == null || this.ihsPasien.isEmpty()) && noKtpPasien != null && noKtpPasien.trim().length() == 16) {
            try {
                this.ihsPasien = cekNik.tampilIDPasien(noKtpPasien.trim());
            } catch (Exception e) {
                System.out.println("Error tampilIDPasien: " + e);
            }
        }
    }

    private void resolvePractitionerData() {
        String loggedUserCode = akses.getkode();
        boolean isLoggedDoctor = false;

        // Check if logged-in user is a doctor
        if (loggedUserCode != null && !loggedUserCode.isEmpty()) {
            String checkDokter = Sequel.cariIsi("select dokter.kd_dokter from dokter where dokter.kd_dokter=?", loggedUserCode);
            if (checkDokter != null && !checkDokter.isEmpty()) {
                this.kdDokter = checkDokter;
                isLoggedDoctor = true;
            }
        }

        // Fallback to visit doctor if not logged-in doctor
        if (!isLoggedDoctor && (this.kdDokter == null || this.kdDokter.isEmpty())) {
            this.kdDokter = Sequel.cariIsi("select reg_periksa.kd_dokter from reg_periksa where reg_periksa.no_rawat=?", this.noRawat);
        }

        ensureConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                 "select pegawai.nama, pegawai.no_ktp, " +
                 "ifnull(satu_sehat_ihs_practitioner.ihspegawai, '') as ihspegawai " +
                 "from pegawai " +
                 "left join satu_sehat_ihs_practitioner on satu_sehat_ihs_practitioner.nikpegawai=pegawai.no_ktp " +
                 "where pegawai.nik=?");
            ps.setString(1, this.kdDokter);
            rs = ps.executeQuery();
            if (rs.next()) {
                this.nmDokter = rs.getString("nama");
                this.noKtpDokter = rs.getString("no_ktp");
                this.ihsDokter = rs.getString("ihspegawai");
            }
        } catch (Exception e) {
            System.out.println("Error resolvePractitionerData: " + e);
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }

        // On-the-fly lookup if missing
        if ((this.ihsDokter == null || this.ihsDokter.isEmpty()) && noKtpDokter != null && noKtpDokter.trim().length() == 16) {
            try {
                this.ihsDokter = cekNik.tampilIDParktisi(noKtpDokter.trim());
            } catch (Exception e) {
                System.out.println("Error tampilIDParktisi: " + e);
            }
        }
    }

    private String findActiveSession(String noRawat, String ihsPasien, String ihsDokter) {
        String activeUrl = null;
        ensureConnection();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = koneksi.prepareStatement(
                 "select no_rawat, shlink_url, consent_id, expired_at from satu_sehat_rme_akses " +
                 "where (no_rawat=? or (id_pasien_satusehat=? and id_praktisi_satusehat=? and id_pasien_satusehat != '' and id_praktisi_satusehat != '')) " +
                 "and expired_at > NOW() " +
                 "order by id desc limit 1");
            ps.setString(1, noRawat);
            ps.setString(2, ihsPasien != null ? ihsPasien : "");
            ps.setString(3, ihsDokter != null ? ihsDokter : "");
            rs = ps.executeQuery();
            if (rs.next()) {
                activeUrl = rs.getString("shlink_url");
                this.currentConsentId = rs.getString("consent_id");
                this.currentExpiry = rs.getString("expired_at");
                String foundNoRawat = rs.getString("no_rawat");
                if (foundNoRawat != null && !foundNoRawat.equals(noRawat) && !noRawat.isEmpty()) {
                    linkActiveSessionToCurrentRawat(noRawat, ihsPasien, ihsDokter, activeUrl, this.currentConsentId, this.currentExpiry);
                }
            }
        } catch (Exception e) {
            System.out.println("Error findActiveSession: " + e);
        } finally {
            if (rs != null) try { rs.close(); } catch (Exception ignored) {}
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }
        return activeUrl;
    }

    private void linkActiveSessionToCurrentRawat(String noRawat, String ihsPasien, String ihsDokter, String shlinkUrl, String consentId, String expiredAt) {
        ensureConnection();
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement(
                "insert into satu_sehat_rme_akses (" +
                "no_rawat, id_pasien_satusehat, id_praktisi_satusehat, shlink_id, shlink_url, " +
                "consent_id, tipe_akses, alasan_darurat, nama_pengantar, waktu_akses, expired_at, user_akses" +
                ") values (?, ?, ?, '', ?, ?, 'NORMAL', 'Reused Active Session', '', NOW(), ?, ?)");
            ps.setString(1, noRawat);
            ps.setString(2, ihsPasien != null ? ihsPasien : "");
            ps.setString(3, ihsDokter != null ? ihsDokter : "");
            ps.setString(4, shlinkUrl);
            ps.setString(5, consentId);
            ps.setString(6, expiredAt);
            ps.setString(7, akses.getkode() != null ? akses.getkode() : "system");
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error linkActiveSessionToCurrentRawat: " + e);
        } finally {
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }
    }

    private void renderQrCode(String url) {
        try {
            BufferedImage qrImg = qrRenderer.render(url, 260);
            lblQrImage.setText("");
            lblQrImage.setIcon(new ImageIcon(qrImg));
        } catch (Exception qre) {
            lblQrImage.setIcon(null);
            lblQrImage.setText("Gagal merender QR: " + qre.getMessage());
        }
    }

    private void initiateConsentHealthLink() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblPollingStatus.setText("Memeriksa persetujuan akses SATUSEHAT...");
        progressBar.setVisible(true);

        new Thread(() -> {
            try {
                // 1. Optimistic fast-path check: Is consent already granted?
                SatuSehatRMEResponse checkRes = api.openSmartHealthLink(
                    ihsPasien, nmPasien, ihsDokter, nmDokter, orgId, orgName
                );
                this.lastResponse = checkRes;
                this.lastRequestBody = checkRes.getRawRequestBody();
                this.lastResponseBody = checkRes.getRawResponseBody();

                if (checkRes.isSuccess() && !checkRes.getShlinkUrl().isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        setCursor(Cursor.getDefaultCursor());
                        progressBar.setVisible(false);
                        handleAccessGranted(checkRes, "NORMAL", "", "");
                    });
                    return;
                }

                // 2. Consent not yet granted, create new consent link
                SwingUtilities.invokeLater(() -> {
                    lblPollingStatus.setText("Menyiapkan QR Code persetujuan SATUSEHAT...");
                });

                SatuSehatRMEResponse chlRes = api.createConsentHealthLink(
                    ihsPasien, nmPasien, ihsDokter, nmDokter, orgId, orgName
                );
                this.lastResponse = chlRes;
                this.lastRequestBody = chlRes.getRawRequestBody();
                this.lastResponseBody = chlRes.getRawResponseBody();

                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());

                    if (chlRes.isSuccess() && !chlRes.getVerificationUrl().isEmpty()) {
                        this.currentVerificationUrl = chlRes.getVerificationUrl();
                        txtVerificationUrl.setText(currentVerificationUrl);

                        renderQrCode(currentVerificationUrl);
                        loadVerifyBrowserOrFallback(currentVerificationUrl);
                        if (verifyCardLayout != null && pnlVerifyCards != null) {
                            verifyCardLayout.show(pnlVerifyCards, VERIFY_MODE_WEB);
                        }
                        lblPollingStatus.setText("Form kode akses siap. Masukkan 6 digit kode dari SATUSEHAT Mobile...");
                        startPolling();
                    } else if (chlRes.isDuplicateKeyError()) {
                        // 3. Duplicate key error: ticket already launched in Kemenkes MongoDB
                        // Check /shl again in case patient approved
                        new Thread(() -> {
                            SatuSehatRMEResponse retryShl = api.openSmartHealthLink(
                                ihsPasien, nmPasien, ihsDokter, nmDokter, orgId, orgName
                            );
                            SwingUtilities.invokeLater(() -> {
                                if (retryShl.isSuccess() && !retryShl.getShlinkUrl().isEmpty()) {
                                    handleAccessGranted(retryShl, "NORMAL", "", "");
                                } else if (!chlRes.getVerificationUrl().isEmpty()) {
                                    // Render reconstructed QR code and load embedded browser
                                    this.currentVerificationUrl = chlRes.getVerificationUrl();
                                    txtVerificationUrl.setText(currentVerificationUrl);
                                    renderQrCode(currentVerificationUrl);
                                    loadVerifyBrowserOrFallback(currentVerificationUrl);
                                    if (verifyCardLayout != null && pnlVerifyCards != null) {
                                        verifyCardLayout.show(pnlVerifyCards, VERIFY_MODE_WEB);
                                    }
                                    lblPollingStatus.setText("Melanjutkan sesi persetujuan aktif. Form kode akses web siap...");
                                    startPolling();
                                } else {
                                    lblQrImage.setIcon(null);
                                    lblQrImage.setText("Tiket persetujuan telah aktif di SATUSEHAT Mobile");
                                    lblPollingStatus.setText("Sesi persetujuan telah terdaftar di ponsel pasien. Silakan minta pasien membuka SATUSEHAT Mobile.");
                                    progressBar.setVisible(false);
                                }
                            });
                        }).start();
                    } else {
                        lblQrImage.setIcon(null);
                        lblQrImage.setText("Gagal membuat tautan persetujuan");
                        lblPollingStatus.setText("Gagal: " + chlRes.getMessage());
                        progressBar.setVisible(false);
                        JOptionPane.showMessageDialog(this,
                            "Gagal membuat Consent Health Link SATUSEHAT:\n" + chlRes.getMessage() +
                            (chlRes.getErrorCode().isEmpty() ? "" : " (" + chlRes.getErrorCode() + ")"),
                            "Pemberitahuan SATUSEHAT", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    setCursor(Cursor.getDefaultCursor());
                    lblPollingStatus.setText("Kesalahan: " + e.getMessage());
                    progressBar.setVisible(false);
                });
            }
        }).start();
    }

    private void startPolling() {
        stopPolling();
        remainingPollingSeconds = MAX_POLLING_SECONDS;
        progressBar.setVisible(true);
        progressBar.setIndeterminate(true);

        pollingTimer = new Timer(POLLING_INTERVAL_MS, (ActionEvent e) -> {
            remainingPollingSeconds -= (POLLING_INTERVAL_MS / 1000);
            if (remainingPollingSeconds <= 0) {
                stopPolling();
                lblPollingStatus.setText("Waktu tunggu habis. Pasien dapat scan kembali atau klik 'Periksa Persetujuan'.");
                progressBar.setVisible(false);
                return;
            }

            lblPollingStatus.setText("Menunggu persetujuan pasien di SATUSEHAT Mobile... (" + remainingPollingSeconds + "s)");

            // Try opening Smart Health Link in background thread
            new Thread(() -> {
                SatuSehatRMEResponse res = api.openSmartHealthLink(
                    ihsPasien, nmPasien, ihsDokter, nmDokter, orgId, orgName
                );
                this.lastResponse = res;
                this.lastRequestBody = res.getRawRequestBody();
                this.lastResponseBody = res.getRawResponseBody();

                if (res.isSuccess() && !res.getShlinkUrl().isEmpty()) {
                    SwingUtilities.invokeLater(() -> {
                        stopPolling();
                        handleAccessGranted(res, "NORMAL", "", "");
                    });
                }
            }).start();
        });

        pollingTimer.start();
    }

    private void stopPolling() {
        if (pollingTimer != null && pollingTimer.isRunning()) {
            pollingTimer.stop();
        }
        pollingTimer = null;
    }

    private void checkConsentStatusManual() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblPollingStatus.setText("Memeriksa status persetujuan ke SATUSEHAT...");

        new Thread(() -> {
            SatuSehatRMEResponse res = api.openSmartHealthLink(
                ihsPasien, nmPasien, ihsDokter, nmDokter, orgId, orgName
            );
            this.lastResponse = res;
            this.lastRequestBody = res.getRawRequestBody();
            this.lastResponseBody = res.getRawResponseBody();

            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                if (res.isSuccess() && !res.getShlinkUrl().isEmpty()) {
                    stopPolling();
                    handleAccessGranted(res, "NORMAL", "", "");
                } else if ("CONSENT_REQUIRED".equalsIgnoreCase(res.getErrorCode()) || res.getStatusCode() == 403) {
                    JOptionPane.showMessageDialog(this,
                        "Pasien belum menyetujui akses rekam medis di SATUSEHAT Mobile.\nPastikan pasien telah memindai QR Code dan memilih 'Setujui'.",
                        "Persetujuan Belum Diberikan", JOptionPane.INFORMATION_MESSAGE);
                    lblPollingStatus.setText("Persetujuan belum diberikan pasien. Melanjutkan pemantauan status...");
                    startPolling();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Respons SATUSEHAT:\n" + res.getMessage() +
                        (res.getErrorCode().isEmpty() ? "" : " (" + res.getErrorCode() + ")"),
                        "Pemeriksaan Persetujuan", JOptionPane.WARNING_MESSAGE);
                }
            });
        }).start();
    }

    private void handleEmergencyBypass() {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(4, 5, 4, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        form.add(new JLabel("Alasan Darurat (Emergency):"), gbc);

        gbc.gridx = 1; gbc.gridy = 0;
        String[] alasanOptions = {
            "Pasien Kritis / Tidak Sadar",
            "Kegawatdaruratan Medis Mengancam Jiwa",
            "Pasien Gangguan Jiwa / Gaduh Gelisah",
            "Penurunan Kesadaran Akut",
            "Lainnya..."
        };
        JComboBox<String> cmbAlasan = new JComboBox<>(alasanOptions);
        form.add(cmbAlasan, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        form.add(new JLabel("Pengantar / Wali Pasien:"), gbc);

        gbc.gridx = 1; gbc.gridy = 1;
        JTextField txtWali = new JTextField("Keluarga Pasien", 20);
        form.add(txtWali, gbc);

        int confirm = JOptionPane.showConfirmDialog(this, form,
            "Konfirmasi Pembukaan Akses Rekam Medis Darurat (Emergency)",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.OK_OPTION) {
            return;
        }

        String alasan = cmbAlasan.getSelectedItem() != null ? cmbAlasan.getSelectedItem().toString() : "Kegawatdaruratan Medis";
        String pengantar = txtWali.getText().trim();

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        lblPollingStatus.setText("Mengajukan pembuatan tautan darurat (Emergency) ke SATUSEHAT...");

        new Thread(() -> {
            SatuSehatRMEResponse res = api.createEmergencyConsentHealthLink(
                ihsPasien, nmPasien, ihsDokter, nmDokter, orgId, orgName
            );
            this.lastResponse = res;
            this.lastRequestBody = res.getRawRequestBody();
            this.lastResponseBody = res.getRawResponseBody();

            SwingUtilities.invokeLater(() -> {
                setCursor(Cursor.getDefaultCursor());
                if (res.isSuccess() && !res.getVerificationUrl().isEmpty()) {
                    this.currentVerificationUrl = res.getVerificationUrl();
                    txtVerificationUrl.setText(currentVerificationUrl);

                    renderQrCode(currentVerificationUrl);
                    loadVerifyBrowserOrFallback(currentVerificationUrl);
                    if (verifyCardLayout != null && pnlVerifyCards != null) {
                        verifyCardLayout.show(pnlVerifyCards, VERIFY_MODE_WEB);
                    }

                    lblPollingStatus.setText("Form darurat Kemenkes dimuat. Silakan lengkapi konfirmasi darurat di web.");
                    startPolling();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Gagal membuat tautan persetujuan darurat:\n" + res.getMessage() +
                        (res.getErrorCode().isEmpty() ? "" : " (" + res.getErrorCode() + ")"),
                        "Kegagalan Akses Darurat", JOptionPane.ERROR_MESSAGE);
                }
            });
        }).start();
    }

    private void handleAccessGranted(SatuSehatRMEResponse res, String tipeAkses, String alasanDarurat, String namaPengantar) {
        this.currentShlinkUrl = res.getShlinkUrl();
        this.currentConsentId = res.getConsentId();

        // Calculate expiration timestamp (default 2 hours if not specified)
        String expiredAtStr = res.getExpiredAt();
        String expiredAtDb = calculateDbExpiry(expiredAtStr);
        this.currentExpiry = expiredAtDb;

        // Record audit entry in satu_sehat_rme_akses
        ensureConnection();
        PreparedStatement ps = null;
        try {
            ps = koneksi.prepareStatement(
                 "insert into satu_sehat_rme_akses (" +
                 "no_rawat, id_pasien_satusehat, id_praktisi_satusehat, shlink_id, shlink_url, " +
                 "consent_id, tipe_akses, alasan_darurat, nama_pengantar, waktu_akses, expired_at, user_akses" +
                 ") values (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?, ?)");
            ps.setString(1, this.noRawat);
            ps.setString(2, this.ihsPasien);
            ps.setString(3, this.ihsDokter);
            ps.setString(4, res.getShlinkId());
            ps.setString(5, res.getShlinkUrl());
            ps.setString(6, res.getConsentId());
            ps.setString(7, tipeAkses);
            ps.setString(8, alasanDarurat);
            ps.setString(9, namaPengantar);
            ps.setString(10, expiredAtDb);
            ps.setString(11, akses.getkode() != null ? akses.getkode() : "system");
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("Error saving audit satu_sehat_rme_akses: " + e);
        } finally {
            if (ps != null) try { ps.close(); } catch (Exception ignored) {}
        }

        updateViewerCard(currentShlinkUrl, currentConsentId, expiredAtDb);
        if (cefVerifyBrowser != null) {
            SatuSehatBrowserManager.safeCloseBrowser(cefVerifyBrowser);
            cefVerifyBrowser = null;
            cefVerifyComponent = null;
        }
        toFront();
        requestFocus();
        try {
            java.awt.Toolkit.getDefaultToolkit().beep();
        } catch (Throwable ignored) {}
        cardLayout.show(mainCardPanel, CARD_VIEWER);
        loadChromiumOrFallback(currentShlinkUrl);
    }

    private void updateViewerCard(String shlinkUrl, String consentId, String expiry) {
        lblViewerTitle.setText(" SATUSEHAT RME - Pasien: " + nmPasien + " (RM: " + noRkmMedis + ")");
        lblViewerPasienInfo.setText("Pasien: " + nmPasien + " (RM: " + noRkmMedis + " | NIK: " + (noKtpPasien.isEmpty() ? "-" : noKtpPasien) + ")");
        lblViewerDokterInfo.setText("Dokter: " + nmDokter + " (IHS: " + ihsDokter + ")");
        lblViewerFaskesInfo.setText("Fasyankes: " + orgName + " (ID: " + orgId + ")");
        lblViewerConsentInfo.setText("Consent ID: " + (consentId.isEmpty() ? "-" : consentId));
        lblViewerExpiryInfo.setText("Masa Berlaku Sesi: s.d. " + (expiry.isEmpty() ? "-" : expiry) + " WIB");
        txtViewerShlinkUrl.setText(shlinkUrl);
    }

    private String calculateDbExpiry(String isoExpiry) {
        if (isoExpiry != null && !isoExpiry.isEmpty()) {
            try {
                // Example: 2026-08-15T14:35:16Z
                String normalized = isoExpiry.replace("Z", "").replace("T", " ");
                if (normalized.length() >= 19) {
                    return normalized.substring(0, 19);
                }
            } catch (Exception ignored) {
            }
        }
        // Fallback: 2 hours from now
        return LocalDateTime.now().plusHours(2).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
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
                System.err.println("Error displaying CefBrowser: " + t.getMessage());
            }
        }

        // Fallback: Show success card and automatically open in external browser
        pnlChromiumContainer.removeAll();
        pnlChromiumContainer.add(pnlFallbackInfo, BorderLayout.CENTER);
        pnlChromiumContainer.revalidate();
        pnlChromiumContainer.repaint();
        openInExternalBrowser();
    }

    private void openInExternalBrowser() {
        if (currentShlinkUrl == null || currentShlinkUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Tautan Smart Health Link belum tersedia.");
            return;
        }
        SatuSehatBrowserManager.openInAppModeOrBrowser(currentShlinkUrl, this);
    }

    private void showDetailPayloadDialog() {
        JDialog dlg = new JDialog(this, "Detail Payload & Respons SATUSEHAT", true);
        dlg.setSize(760, 520);
        dlg.setLocationRelativeTo(this);
        dlg.setLayout(new BorderLayout(5, 5));

        JPanel header = new JPanel(new java.awt.GridLayout(2, 2, 8, 4));
        header.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        header.setBackground(new Color(245, 248, 252));

        int statusCode = lastResponse != null ? lastResponse.getStatusCode() : 0;
        String reqId = lastResponse != null ? lastResponse.getRequestId() : "-";
        String errCode = lastResponse != null ? lastResponse.getErrorCode() : "-";
        String msg = lastResponse != null ? lastResponse.getMessage() : "-";

        header.add(new JLabel("HTTP Status : " + (statusCode > 0 ? statusCode : "-")));
        header.add(new JLabel("Request ID  : " + (reqId.isEmpty() ? "-" : reqId)));
        header.add(new JLabel("Error Code  : " + (errCode.isEmpty() ? "-" : errCode)));
        header.add(new JLabel("Message     : " + (msg.isEmpty() ? "-" : msg)));

        dlg.add(header, BorderLayout.NORTH);

        javax.swing.JTabbedPane tabs = new javax.swing.JTabbedPane();

        javax.swing.JTextArea txtReq = new javax.swing.JTextArea();
        txtReq.setEditable(false);
        txtReq.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtReq.setText(lastRequestBody != null && !lastRequestBody.isEmpty() ? SatuSehatRMEApi.prettyPrintJson(lastRequestBody) : "(Belum ada request yang dikirim)");
        tabs.addTab("Request Payload (JSON)", new javax.swing.JScrollPane(txtReq));

        javax.swing.JTextArea txtResp = new javax.swing.JTextArea();
        txtResp.setEditable(false);
        txtResp.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtResp.setText(lastResponseBody != null && !lastResponseBody.isEmpty() ? SatuSehatRMEApi.prettyPrintJson(lastResponseBody) : "(Belum ada respons yang diterima)");
        tabs.addTab("Response Body (JSON)", new javax.swing.JScrollPane(txtResp));

        dlg.add(tabs, BorderLayout.CENTER);

        panelisi bottom = new panelisi();
        bottom.setLayout(new FlowLayout(FlowLayout.RIGHT, 8, 6));

        Button btnSalinAll = new Button();
        btnSalinAll.setText("Salin Log Lengkap");
        btnSalinAll.addActionListener(e -> {
            StringBuilder sb = new StringBuilder();
            sb.append("=== SATUSEHAT SSRME TRACE ===\n");
            sb.append("HTTP Status : ").append(statusCode).append("\n");
            sb.append("Request ID  : ").append(reqId).append("\n");
            sb.append("Error Code  : ").append(errCode).append("\n");
            sb.append("Message     : ").append(msg).append("\n\n");
            sb.append("--- REQUEST BODY ---\n").append(txtReq.getText()).append("\n\n");
            sb.append("--- RESPONSE BODY ---\n").append(txtResp.getText()).append("\n");
            StringSelection sel = new StringSelection(sb.toString());
            getToolkit().getSystemClipboard().setContents(sel, sel);
            JOptionPane.showMessageDialog(dlg, "Log lengkap berhasil disalin ke clipboard!");
        });

        Button btnTutupDlg = new Button();
        btnTutupDlg.setText("Tutup");
        btnTutupDlg.addActionListener(e -> dlg.dispose());

        bottom.add(btnSalinAll);
        bottom.add(btnTutupDlg);
        dlg.add(bottom, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }
}
