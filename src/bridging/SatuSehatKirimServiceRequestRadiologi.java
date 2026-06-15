/*
  by Mas Elkhanza
 */
package bridging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.TableColumnAdjuster;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * Dialog for sending radiology Service Requests to Satu Sehat, and for managing
 * the associated Orthanc DICOM workflow (update ACSN, send to router).
 *
 * <h3>Table column index reference (22 columns):</h3>
 * <pre>
 *  0  Pilih (checkbox)
 *  1  No.Rawat
 *  2  No.RM
 *  3  Nama Pasien
 *  4  No.KTP Pasien
 *  5  Kode Dokter
 *  6  Nama Dokter Perujuk
 *  7  No.KTP Dokter
 *  8  ID Encounter
 *  9  No.Permintaan
 * 10  Tgl & Jam Permintaan
 * 11  Diagnosa Klinis
 * 12  Nama Pemeriksaan
 * 13  Radiologi Code
 * 14  Radiologi System
 * 15  Radiologi Display
 * 16  ID Service Request
 * 17  Kode Pemeriksaan  (hidden – used as key)
 * 18  ACSN
 * 19  ID Imaging Study
 * 20  Modality           (DICOM type: CT, US, CR …)
 * 21  Status Orthanc     (runtime feedback for DICOM operations)
 * </pre>
 *
 * @author dosen
 */
public final class SatuSehatKirimServiceRequestRadiologi extends javax.swing.JDialog {

    // -------------------------------------------------------------------------
    // Column index constants – avoids "magic numbers" spread across methods
    // -------------------------------------------------------------------------
    private static final int COL_PILIH = 0;
    private static final int COL_NO_RAWAT = 1;
    private static final int COL_NO_RM = 2;
    private static final int COL_NAMA_PASIEN = 3;
    private static final int COL_KTP_PASIEN = 4;
    private static final int COL_KD_DOKTER = 5;
    private static final int COL_NAMA_DOKTER = 6;
    private static final int COL_KTP_DOKTER = 7;
    private static final int COL_ID_ENCOUNTER = 8;
    private static final int COL_NOORDER = 9;
    private static final int COL_TGL_JAM = 10;
    private static final int COL_DIAGNOSA = 11;
    private static final int COL_NM_PERAWATAN = 12;
    private static final int COL_RAD_CODE = 13;
    private static final int COL_RAD_SYSTEM = 14;
    private static final int COL_RAD_DISPLAY = 15;
    private static final int COL_ID_SR = 16;
    private static final int COL_KD_JENIS_PRW = 17;
    private static final int COL_LOKASI_IMAGE = 18;
    private static final int COL_ACSN = 19;
    private static final int COL_ID_IMAGING = 20;
    private static final int COL_MODALITY = 21;
    private static final int COL_STATUS_ORTHANC = 22;
    private static final int COL_LOKASI_FILE_IMAGE = 23;
    private static final int COL_TGL_LAHIR = 24;
    private static final int COL_JK = 25;
    private static final int COL_NM_POLI = 26;
    private static final int COL_NM_DOKTER_PERUJUK = 27;
    /**
     * yyyy-MM-dd from permintaan_radiologi.tgl_permintaan (for DICOM
     * scheduling).
     */
    private static final int COL_TGL_PERMINTAAN = 28;
    /**
     * HH:mm:ss or empty when jam is 00:00:00 / unknown.
     */
    private static final int COL_JAM_PERMINTAAN = 29;
    private static final int TOTAL_COLUMNS = 30;

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------
    private final DefaultTableModel tabMode;
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps, ps2;
    private ResultSet rs, rs2;
    private int i = 0;
    private String link = "", json = "", iddokter = "", idpasien = "";
    private ApiSatuSehat api = new ApiSatuSehat();
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private final ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private SatuSehatCekNIK cekViaSatuSehat = new SatuSehatCekNIK();
    private StringBuilder htmlContent;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;

    // FIX: Use singleton so the mapping file is loaded exactly once per JVM.
    private final ApiOrthanc orthanc = new ApiOrthanc();
    private final RadiologyModalityMapper modalityMapper = RadiologyModalityMapper.getInstance();

    // =========================================================================
    // Constructor
    // =========================================================================
    /**
     * Creates new form SatuSehatKirimServiceRequestRadiologi
     */
    public SatuSehatKirimServiceRequestRadiologi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocation(10, 2);
        setSize(628, 674);

        tabMode = new DefaultTableModel(null, new String[]{
            "P", "No.Rawat", "No.RM", "Nama Pasien", "No.KTP Pasien",
            "Kode Dokter", "Nama Dokter Perujuk", "No.KTP Dokter",
            "ID Encounter", "No.Permintaan", "Tgl & Jam Permintaan",
            "Diagnosa Klinis", "Nama Pemeriksaan",
            "Radiologi Code", "Radiologi System", "Radiologi Display",
            "ID Service Request", "Kode Pemeriksaan", "Lokasi Image",
            "ASCN", "ID Imaging Study", "Modality", "Status Orthanc", "Lokasi File Image",
            "Tgl Lahir", "JK", "Nm Poli", "Nm Dokter Perujuk",
            "Tgl Permintaan", "Jam Permintaan"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return colIndex == COL_PILIH;
            }

            final Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbObat.setModel(tabMode);
        tbObat.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Column widths
        TableColumnAdjuster tca = new TableColumnAdjuster(tbObat);
        tca.setColumnHeaderIncluded(true);
        tca.setColumnDataIncluded(true);
        tca.setDynamicAdjustment(true);
        tca.setOnlyAdjustLarger(false);
        tca.adjustColumns();

        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));

        ppPilihLokasiWebapps = new javax.swing.JMenuItem();
        ppPilihLokasiWebapps.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihLokasiWebapps.setFont(new java.awt.Font("Tahoma", 0, 11));
        ppPilihLokasiWebapps.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihLokasiWebapps.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihLokasiWebapps.setText("Pilih Lokasi Webapps");
        ppPilihLokasiWebapps.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihLokasiWebapps.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihLokasiWebapps.setName("ppPilihLokasiWebapps");
        ppPilihLokasiWebapps.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihLokasiWebapps.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihLokasiWebappsActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihLokasiWebapps);

        ppPilihLokasiOrthanc = new javax.swing.JMenuItem();
        ppPilihLokasiOrthanc.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihLokasiOrthanc.setFont(new java.awt.Font("Tahoma", 0, 11));
        ppPilihLokasiOrthanc.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihLokasiOrthanc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihLokasiOrthanc.setText("Pilih Lokasi Orthanc");
        ppPilihLokasiOrthanc.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihLokasiOrthanc.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihLokasiOrthanc.setName("ppPilihLokasiOrthanc");
        ppPilihLokasiOrthanc.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihLokasiOrthanc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihLokasiOrthancActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihLokasiOrthanc);

        try {
            link = koneksiDB.URLFHIRSATUSEHAT();
        } catch (Exception e) {
            System.out.println("SatuSehatKirimServiceRequestRadiologi init : " + e);
        }

        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(
                ".isi td{border-right:1px solid #e2e7dd;font:8.5px tahoma;height:12px;border-bottom:1px solid #e2e7dd;background:#ffffff;color:#323232;}"
                + ".isi2 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#323232;}"
                + ".isi3 td{border-right:1px solid #e2e7dd;font:8.5px tahoma;height:12px;border-top:1px solid #e2e7dd;background:#ffffff;color:#323232;}"
                + ".isi4 td{font:11px tahoma;height:12px;border-top:1px solid #e2e7dd;background:#ffffff;color:#323232;}"
                + ".isi5 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#AA0000;}"
                + ".isi6 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#FF0000;}"
                + ".isi7 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#C8C800;}"
                + ".isi8 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#00AA00;}"
                + ".isi9 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#969696;}"
        );
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);
    }

    // =========================================================================
    // Generated UI (NetBeans)
    // =========================================================================
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        ppPilihSemua = new javax.swing.JMenuItem();
        ppPilihBelumTerkirim = new javax.swing.JMenuItem();
        ppPilihBelumTerkirim1 = new javax.swing.JMenuItem();
        ppBersihkan = new javax.swing.JMenuItem();
        LoadHTML = new widget.editorpane();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbObat = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        BtnAll = new widget.Button();
        BtnKirim = new widget.Button();
        BtnUpdate = new widget.Button();
        BtnPrint = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel15 = new widget.Label();
        DTPCari1 = new widget.Tanggal();
        jLabel17 = new widget.Label();
        DTPCari2 = new widget.Tanggal();
        jLabel16 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        BtnGetIDImagingStudiAuto = new widget.Button();
        BtnGetIDImagingStudiManual = new widget.Button();
        BtnKirimOrthanc = new widget.Button();
        BtnUpdateACSNOrthanc = new widget.Button();
        BtnKirimDICOMRouter = new widget.Button();
        BtnUpdateDanKirim = new widget.Button();

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N
        jPopupMenu1.setPreferredSize(new java.awt.Dimension(222, 116));

        ppPilihSemua.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihSemua.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihSemua.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihSemua.setText("Pilih Semua");
        ppPilihSemua.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihSemua.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihSemua.setName("ppPilihSemua"); // NOI18N
        ppPilihSemua.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihSemua.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihSemuaActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihSemua);

        ppPilihBelumTerkirim.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihBelumTerkirim.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihBelumTerkirim.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihBelumTerkirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihBelumTerkirim.setText("Pilih Service Req Radiologi Belum Terkirim");
        ppPilihBelumTerkirim.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihBelumTerkirim.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihBelumTerkirim.setName("ppPilihBelumTerkirim"); // NOI18N
        ppPilihBelumTerkirim.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihBelumTerkirim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihBelumTerkirimActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihBelumTerkirim);

        ppPilihBelumTerkirim1.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihBelumTerkirim1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihBelumTerkirim1.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihBelumTerkirim1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihBelumTerkirim1.setText("Pilih ID Imaging Study Belum Ada");
        ppPilihBelumTerkirim1.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppPilihBelumTerkirim1.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppPilihBelumTerkirim1.setName("ppPilihBelumTerkirim1"); // NOI18N
        ppPilihBelumTerkirim1.setPreferredSize(new java.awt.Dimension(150, 26));
        ppPilihBelumTerkirim1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppPilihBelumTerkirim1ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppPilihBelumTerkirim1);

        ppBersihkan.setBackground(new java.awt.Color(255, 255, 254));
        ppBersihkan.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppBersihkan.setForeground(new java.awt.Color(50, 50, 50));
        ppBersihkan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppBersihkan.setText("Hilangkan Pilihan");
        ppBersihkan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ppBersihkan.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ppBersihkan.setName("ppBersihkan"); // NOI18N
        ppBersihkan.setPreferredSize(new java.awt.Dimension(150, 26));
        ppBersihkan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ppBersihkanActionPerformed(evt);
            }
        });
        jPopupMenu1.add(ppBersihkan);

        LoadHTML.setBorder(null);
        LoadHTML.setName("LoadHTML"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setIconImage(null);
        setIconImages(null);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Pengiriman Data Service Request Radiologi Satu Sehat ]::",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setComponentPopupMenu(jPopupMenu1);
        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbObat.setComponentPopupMenu(jPopupMenu1);
        tbObat.setName("tbObat"); // NOI18N
        Scroll.setViewportView(tbObat);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(53, 23));
        panelGlass8.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(60, 23));
        panelGlass8.add(LCount);

        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png"))); // NOI18N
        BtnAll.setMnemonic('M');
        BtnAll.setText("Semua");
        BtnAll.setToolTipText("Alt+M");
        BtnAll.setName("BtnAll"); // NOI18N
        BtnAll.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnAllActionPerformed(evt);
            }
        });
        BtnAll.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnAllKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnAll);

        BtnKirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/34.png"))); // NOI18N
        BtnKirim.setMnemonic('K');
        BtnKirim.setText("Kirim");
        BtnKirim.setToolTipText("Alt+K");
        BtnKirim.setName("BtnKirim"); // NOI18N
        BtnKirim.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKirim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnKirim);

        BtnUpdate.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/edit_f2.png"))); // NOI18N
        BtnUpdate.setMnemonic('U');
        BtnUpdate.setText("Update");
        BtnUpdate.setToolTipText("Alt+U");
        BtnUpdate.setName("BtnUpdate"); // NOI18N
        BtnUpdate.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnUpdate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnUpdate);

        BtnPrint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/b_print.png"))); // NOI18N
        BtnPrint.setMnemonic('T');
        BtnPrint.setText("Cetak");
        BtnPrint.setToolTipText("Alt+T");
        BtnPrint.setName("BtnPrint"); // NOI18N
        BtnPrint.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnPrint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnPrintActionPerformed(evt);
            }
        });
        panelGlass8.add(BtnPrint);

        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png"))); // NOI18N
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Keluar");
        BtnKeluar.setToolTipText("Alt+K");
        BtnKeluar.setName("BtnKeluar"); // NOI18N
        BtnKeluar.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnKeluar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKeluarActionPerformed(evt);
            }
        });
        BtnKeluar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnKeluarKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnKeluar);

        jPanel3.add(panelGlass8, java.awt.BorderLayout.CENTER);

        panelGlass9.setName("panelGlass9"); // NOI18N
        panelGlass9.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        jLabel15.setText("Tgl.Registrasi :");
        jLabel15.setName("jLabel15"); // NOI18N
        jLabel15.setPreferredSize(new java.awt.Dimension(85, 23));
        panelGlass9.add(jLabel15);

        DTPCari1.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"16-04-2026"}));
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setName("DTPCari1"); // NOI18N
        DTPCari1.setOpaque(false);
        DTPCari1.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari1);

        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("s.d.");
        jLabel17.setName("jLabel17"); // NOI18N
        jLabel17.setPreferredSize(new java.awt.Dimension(24, 23));
        panelGlass9.add(jLabel17);

        DTPCari2.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"16-04-2026"}));
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setName("DTPCari2"); // NOI18N
        DTPCari2.setOpaque(false);
        DTPCari2.setPreferredSize(new java.awt.Dimension(95, 23));
        panelGlass9.add(DTPCari2);

        jLabel16.setText("Key Word :");
        jLabel16.setName("jLabel16"); // NOI18N
        jLabel16.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel16);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(210, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('6');
        BtnCari.setToolTipText("Alt+6");
        BtnCari.setName("BtnCari"); // NOI18N
        BtnCari.setPreferredSize(new java.awt.Dimension(28, 23));
        BtnCari.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnCariActionPerformed(evt);
            }
        });
        BtnCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnCariKeyPressed(evt);
            }
        });
        panelGlass9.add(BtnCari);

        BtnGetIDImagingStudiAuto.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/36.png"))); // NOI18N
        BtnGetIDImagingStudiAuto.setMnemonic('K');
        BtnGetIDImagingStudiAuto.setText("Auto Get ID Imaging Study");
        BtnGetIDImagingStudiAuto.setToolTipText("Alt+K");
        BtnGetIDImagingStudiAuto.setName("BtnGetIDImagingStudiAuto"); // NOI18N
        BtnGetIDImagingStudiAuto.setPreferredSize(new java.awt.Dimension(190, 30));
        BtnGetIDImagingStudiAuto.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGetIDImagingStudiAutoActionPerformed(evt);
            }
        });
        panelGlass9.add(BtnGetIDImagingStudiAuto);

        BtnGetIDImagingStudiManual.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/36.png"))); // NOI18N
        BtnGetIDImagingStudiManual.setText("Manual Get ID Imaging Study");
        BtnGetIDImagingStudiManual.setToolTipText("");
        BtnGetIDImagingStudiManual.setName("BtnGetIDImagingStudiManual"); // NOI18N
        BtnGetIDImagingStudiManual.setPreferredSize(new java.awt.Dimension(210, 30));
        BtnGetIDImagingStudiManual.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnGetIDImagingStudiManualActionPerformed(evt);
            }
        });
        panelGlass9.add(BtnGetIDImagingStudiManual);

        BtnKirimOrthanc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnKirimOrthanc.setText("Kirim ke Orthanc");
        BtnKirimOrthanc.setToolTipText("Kirim study ke Orthanc untuk baris yang dipilih");
        BtnKirimOrthanc.setName("BtnKirimOrthanc"); // NOI18N
        BtnKirimOrthanc.setPreferredSize(new java.awt.Dimension(170, 30));
        BtnKirimOrthanc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimOrthancActionPerformed(evt);
            }
        });
        panelGlass9.add(BtnKirimOrthanc);

        BtnUpdateACSNOrthanc.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnUpdateACSNOrthanc.setText("Update ACSN Orthanc");
        BtnUpdateACSNOrthanc.setToolTipText("Update Accession Number di Orthanc untuk baris yang dipilih");
        BtnUpdateACSNOrthanc.setName("BtnUpdateACSNOrthanc"); // NOI18N
        BtnUpdateACSNOrthanc.setPreferredSize(new java.awt.Dimension(170, 30));
        BtnUpdateACSNOrthanc.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateACSNOrthancActionPerformed(evt);
            }
        });
        panelGlass9.add(BtnUpdateACSNOrthanc);

        BtnKirimDICOMRouter.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/34.png"))); // NOI18N
        BtnKirimDICOMRouter.setText("Kirim ke DICOM Router");
        BtnKirimDICOMRouter.setToolTipText("Kirim study dari Orthanc ke DICOM Router untuk baris yang dipilih");
        BtnKirimDICOMRouter.setName("BtnKirimDICOMRouter"); // NOI18N
        BtnKirimDICOMRouter.setPreferredSize(new java.awt.Dimension(180, 30));
        BtnKirimDICOMRouter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnKirimDICOMRouterActionPerformed(evt);
            }
        });
        panelGlass9.add(BtnKirimDICOMRouter);

        BtnUpdateDanKirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/36.png"))); // NOI18N
        BtnUpdateDanKirim.setText("Update ACSN & Kirim DICOM");
        BtnUpdateDanKirim.setToolTipText("Update ACSN di Orthanc lalu kirim ke DICOM Router secara otomatis");
        BtnUpdateDanKirim.setName("BtnUpdateDanKirim"); // NOI18N
        BtnUpdateDanKirim.setPreferredSize(new java.awt.Dimension(210, 30));
        BtnUpdateDanKirim.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateDanKirimActionPerformed(evt);
            }
        });
        panelGlass9.add(BtnUpdateDanKirim);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // =========================================================================
    // Window / navigation events
    // =========================================================================

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            dispose();
        } else {
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    autoSearch();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    autoSearch();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    autoSearch();
                }

                private void autoSearch() {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }
            });
        }
    }//GEN-LAST:event_formWindowOpened

    // =========================================================================
    // Search / display
    // =========================================================================

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER || evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_UP) {
            BtnKeluar.requestFocus();
        } else if (evt.getKeyCode() == KeyEvent.VK_UP) {
            tbObat.requestFocus();
        }
    }//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        runBackground(() -> tampil());
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnCariActionPerformed(null);
        } else {
            Valid.pindah(evt, TCari, BtnPrint);
        }
    }//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() -> tampil());
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            TCari.setText("");
            runBackground(() -> tampil());
        } else {
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    // =========================================================================
    // Satu Sehat service request (POST / PUT)
    // =========================================================================

    private void BtnKirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimActionPerformed
        runBackground(() -> {
            for (int r = 0; r < tbObat.getRowCount(); r++) {
                final int row = r;
                if (tbObat.getValueAt(row, COL_PILIH).toString().equals("true")
                        && !tbObat.getValueAt(row, COL_KTP_PASIEN).toString().equals("")
                        && !tbObat.getValueAt(row, COL_KTP_DOKTER).toString().equals("")
                        && tbObat.getValueAt(row, COL_ID_SR).toString().equals("")) {
                    try {
                        String idDokterLoc = cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(row, COL_KTP_DOKTER).toString());
                        String idPasienLoc = cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(row, COL_KTP_PASIEN).toString());
                        if (idDokterLoc.isEmpty() || idPasienLoc.isEmpty()) {
                            continue;
                        }
                        iddokter = idDokterLoc;
                        idpasien = idPasienLoc;
                        try {
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
                            String jsonPayload = buildServiceRequestJson(row, null);
                            System.out.println("URL Kirim : " + link + "/ServiceRequest");
                            System.out.println("Request JSON Kirim : " + jsonPayload);
                            requestEntity = new HttpEntity(jsonPayload, headers);
                            String resJson = api.getRest().exchange(link + "/ServiceRequest", HttpMethod.POST, requestEntity, String.class).getBody();
                            System.out.println("Result JSON Kirim : " + resJson);
                            JsonNode rootNode = mapper.readTree(resJson);
                            String responseId = rootNode.path("id").asText();
                            if (!responseId.isEmpty()) {
                                if (Sequel.menyimpantf2("satu_sehat_servicerequest_radiologi", "?,?,?", "No.Order", 3,
                                        new String[]{tbObat.getValueAt(row, COL_NOORDER).toString(),
                                            tbObat.getValueAt(row, COL_KD_JENIS_PRW).toString(),
                                            responseId}) == true) {
                                    SwingUtilities.invokeLater(() -> {
                                        tbObat.setValueAt(responseId, row, COL_ID_SR);
                                        tbObat.setValueAt(false, row, COL_PILIH);
                                    });
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi Bridging Kirim : " + e);
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi Kirim : " + e);
                    }
                }
            }
        });
    }//GEN-LAST:event_BtnKirimActionPerformed

    private void BtnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateActionPerformed
        runBackground(() -> {
            for (int r = 0; r < tbObat.getRowCount(); r++) {
                final int row = r;
                if (tbObat.getValueAt(row, COL_PILIH).toString().equals("true")
                        && !tbObat.getValueAt(row, COL_KTP_PASIEN).toString().equals("")
                        && !tbObat.getValueAt(row, COL_KTP_DOKTER).toString().equals("")
                        && !tbObat.getValueAt(row, COL_ID_SR).toString().equals("")) {
                    try {
                        String idDokterLoc = cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(row, COL_KTP_DOKTER).toString());
                        String idPasienLoc = cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(row, COL_KTP_PASIEN).toString());
                        if (idDokterLoc.isEmpty() || idPasienLoc.isEmpty()) {
                            continue;
                        }
                        iddokter = idDokterLoc;
                        idpasien = idPasienLoc;
                        try {
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
                            String jsonPayload = buildServiceRequestJson(row, tbObat.getValueAt(row, COL_ID_SR).toString());
                            String url = link + "/ServiceRequest/" + tbObat.getValueAt(row, COL_ID_SR);
                            System.out.println("URL Update : " + url);
                            System.out.println("Request JSON Update : " + jsonPayload);
                            requestEntity = new HttpEntity(jsonPayload, headers);
                            String resJson = api.getRest().exchange(url, HttpMethod.PUT, requestEntity, String.class).getBody();
                            System.out.println("Result JSON Update : " + resJson);
                            SwingUtilities.invokeLater(() -> tbObat.setValueAt(false, row, COL_PILIH));
                        } catch (Exception e) {
                            System.out.println("Notifikasi Bridging Update : " + e);
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi Update : " + e);
                    }
                }
            }
        });
    }//GEN-LAST:event_BtnUpdateActionPerformed

    // =========================================================================
    // Imaging Study ID (Satu Sehat)
    // =========================================================================

    private void BtnGetIDImagingStudiAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGetIDImagingStudiAutoActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        runBackground(() -> {
            int sukses = 0, gagal = 0;
            for (int i = 0; i < tbObat.getRowCount(); i++) {
                final int row = i;
                if (!Boolean.parseBoolean(tbObat.getValueAt(row, COL_PILIH).toString())) {
                    continue;
                }

                String noorder = tbObat.getValueAt(row, COL_NOORDER).toString();
                String kdJenisPrw = tbObat.getValueAt(row, COL_KD_JENIS_PRW).toString();
                String idServicerequest = tbObat.getValueAt(row, COL_ID_SR).toString();
                String acsn = buildAcsn(noorder, kdJenisPrw);

                System.out.println("Auto ACSN baris " + row + " : " + acsn);
                SwingUtilities.invokeLater(() -> tbObat.setValueAt("Checking...", row, COL_STATUS_ORTHANC));
                
                String imagingId = getImagingStudyID(acsn);
                if (imagingId != null && !imagingId.isEmpty()) {
                    final String finalImgId = imagingId;
                    SwingUtilities.invokeLater(() -> {
                        tbObat.setValueAt(acsn, row, COL_ACSN);
                        tbObat.setValueAt(finalImgId, row, COL_ID_IMAGING);
                        tbObat.setValueAt("Terkirim & Synced", row, COL_STATUS_ORTHANC);
                        tbObat.setValueAt(false, row, COL_PILIH);
                    });
                    simpanImagingStudy(noorder, kdJenisPrw, idServicerequest, acsn, imagingId);
                    sukses++;
                    System.out.println("ImagingStudy ID untuk noorder " + noorder + " : " + imagingId);
                } else {
                    SwingUtilities.invokeLater(() -> tbObat.setValueAt("ID Tidak Ditemukan", row, COL_STATUS_ORTHANC));
                    gagal++;
                    System.out.println("Gagal mendapatkan ImagingStudy ID untuk ACSN " + acsn);
                }
            }
            final int finalSukses = sukses;
            final int finalGagal = gagal;
            SwingUtilities.invokeLater(() -> {
                this.setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this,
                        "Cek ID Imaging Study selesai.\nBerhasil: " + finalSukses + "   Gagal/Belum Ada: " + finalGagal,
                        "Cek ID Imaging Study", JOptionPane.INFORMATION_MESSAGE);
            });
        });
    }//GEN-LAST:event_BtnGetIDImagingStudiAutoActionPerformed

    private void BtnGetIDImagingStudiManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGetIDImagingStudiManualActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        runBackground(() -> {
            int sukses = 0, gagal = 0;
            for (int i = 0; i < tbObat.getRowCount(); i++) {
                final int row = i;
                if (!tbObat.getValueAt(row, COL_PILIH).toString().equals("true")) {
                    continue;
                }

                String noorder = tbObat.getValueAt(row, COL_NOORDER).toString();
                String kdJenisPrw = tbObat.getValueAt(row, COL_KD_JENIS_PRW).toString();
                String idServicerequest = tbObat.getValueAt(row, COL_ID_SR).toString();

                final String[] inputAcsn = new String[1];
                try {
                    SwingUtilities.invokeAndWait(() -> {
                        inputAcsn[0] = JOptionPane.showInputDialog(this,
                                "Masukkan ACSN untuk No.Order " + noorder + " :",
                                "Manual ACSN", JOptionPane.QUESTION_MESSAGE);
                    });
                } catch (Exception ignored) {}

                String acsn = inputAcsn[0];
                if (acsn == null || acsn.trim().isEmpty()) {
                    System.out.println("Input ACSN dibatalkan untuk baris " + row);
                    continue;
                }
                acsn = acsn.trim();
                System.out.println("Manual ACSN baris " + row + " : " + acsn);

                final String finalAcsn = acsn;
                SwingUtilities.invokeLater(() -> tbObat.setValueAt("Checking...", row, COL_STATUS_ORTHANC));
                String imagingId = getImagingStudyID(finalAcsn);
                if (imagingId != null && !imagingId.isEmpty()) {
                    final String finalImgId = imagingId;
                    SwingUtilities.invokeLater(() -> {
                        tbObat.setValueAt(finalAcsn, row, COL_ACSN);
                        tbObat.setValueAt(finalImgId, row, COL_ID_IMAGING);
                        tbObat.setValueAt("Terkirim & Synced", row, COL_STATUS_ORTHANC);
                        tbObat.setValueAt(false, row, COL_PILIH);
                    });
                    simpanImagingStudy(noorder, kdJenisPrw, idServicerequest, finalAcsn, imagingId);
                    sukses++;
                    System.out.println("Berhasil ImagingStudy ID untuk noorder " + noorder + " : " + imagingId);
                } else {
                    SwingUtilities.invokeLater(() -> tbObat.setValueAt("ID Tidak Ditemukan", row, COL_STATUS_ORTHANC));
                    gagal++;
                    System.out.println("Gagal mendapatkan ImagingStudy ID untuk ACSN " + finalAcsn);
                }
            }
            final int finalSukses = sukses;
            final int finalGagal = gagal;
            SwingUtilities.invokeLater(() -> {
                this.setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this,
                        "Cek ID Imaging Study selesai.\nBerhasil: " + finalSukses + "   Gagal/Belum Ada: " + finalGagal,
                        "Cek ID Imaging Study Manual", JOptionPane.INFORMATION_MESSAGE);
            });
        });
    }//GEN-LAST:event_BtnGetIDImagingStudiManualActionPerformed

    // =========================================================================
    // Orthanc DICOM workflow buttons
    // =========================================================================
    /**
     * Button: Update ACSN Orthanc. For each checked row: 1. Resolve modality →
     * find the matching Orthanc study 2. Update its AccessionNumber
     * (KeepSource=false) Shows a single summary dialog after processing all
     * rows.
     */
    private void BtnUpdateACSNOrthancActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateACSNOrthancActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        runBackground(() -> {
            int sukses = 0, gagal = 0;
            for (int r = 0; r < tbObat.getRowCount(); r++) {
                final int row = r;
                if (!tbObat.getValueAt(row, COL_PILIH).toString().equals("true")) {
                    continue;
                }

                String noorder = tbObat.getValueAt(row, COL_NOORDER).toString();
                String kdJenisPrw = tbObat.getValueAt(row, COL_KD_JENIS_PRW).toString();
                String acsn = buildAcsn(noorder, kdJenisPrw);

                try {
                    String orthancStudyId = resolveOrthancStudyId(row);
                    if (orthancStudyId.isEmpty()) {
                        SwingUtilities.invokeLater(() -> tbObat.setValueAt("Study Tidak Ditemukan", row, COL_STATUS_ORTHANC));
                        gagal++;
                        continue;
                    }

                    if (orthanc.UbahAccession(orthancStudyId, acsn, true)) {
                        SwingUtilities.invokeLater(() -> {
                            tbObat.setValueAt(acsn, row, COL_ACSN);
                            tbObat.setValueAt("ACSN Updated", row, COL_STATUS_ORTHANC);
                            tbObat.setValueAt(false, row, COL_PILIH);
                        });
                        sukses++;
                        System.out.println("Orthanc ACSN updated : noorder=" + noorder + " → " + acsn);
                    } else {
                        SwingUtilities.invokeLater(() -> tbObat.setValueAt("Gagal Update ACSN", row, COL_STATUS_ORTHANC));
                        gagal++;
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> tbObat.setValueAt("Error", row, COL_STATUS_ORTHANC));
                    gagal++;
                    System.out.println("BtnUpdateACSNOrthanc error baris " + row + " : " + e);
                }
            }

            final int finalSukses = sukses;
            final int finalGagal = gagal;
            SwingUtilities.invokeLater(() -> {
                this.setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this,
                        "Update ACSN Orthanc selesai.\nBerhasil: " + finalSukses + "   Gagal/Skip: " + finalGagal,
                        "Update ACSN Orthanc", JOptionPane.INFORMATION_MESSAGE);
            });
        });
    }//GEN-LAST:event_BtnUpdateACSNOrthancActionPerformed

    private void BtnKirimDICOMRouterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimDICOMRouterActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        runBackground(() -> {
            int sukses = 0, gagal = 0;
            for (int r = 0; r < tbObat.getRowCount(); r++) {
                final int row = r;
                if (!tbObat.getValueAt(row, COL_PILIH).toString().equals("true")) {
                    continue;
                }

                String noorder = tbObat.getValueAt(row, COL_NOORDER).toString();
                String kdJenisPrw = tbObat.getValueAt(row, COL_KD_JENIS_PRW).toString();
                String acsn = tbObat.getValueAt(row, COL_ACSN).toString().trim();
                if (acsn.isEmpty()) {
                    acsn = buildAcsn(noorder, kdJenisPrw);
                }

                try {
                    String orthancStudyId = orthanc.findStudyByAccession(acsn);
                    if (orthancStudyId.isEmpty()) {
                        Object lokObj = tbObat.getValueAt(row, COL_LOKASI_IMAGE);
                        String lok = lokObj == null ? "" : lokObj.toString().trim();
                        if (lok.equalsIgnoreCase("webapps")) {
                            System.out.println("Orthanc : Study not found in Orthanc, attempting auto-upload from webapps first for ACSN=" + acsn);
                            if (uploadSingleRowToOrthanc(row)) {
                                orthancStudyId = orthanc.findStudyByAccession(acsn);
                            }
                        }
                    }

                    if (orthancStudyId.isEmpty()) {
                        SwingUtilities.invokeLater(() -> tbObat.setValueAt("Study Tidak Ditemukan", row, COL_STATUS_ORTHANC));
                        gagal++;
                        System.out.println("Orthanc Skip : study tidak ditemukan untuk ACSN=" + acsn);
                        continue;
                    }

                    int maxRetries = 3;
                    boolean sentToRouter = false;
                    for (int attempt = 1; attempt <= maxRetries; attempt++) {
                        if (orthanc.kirimKeModality(orthancStudyId, true)) {
                            sentToRouter = true;
                            break;
                        }
                        System.out.println("Orthanc : Send attempt " + attempt + " failed for study " + orthancStudyId + ", retrying...");
                        if (attempt < maxRetries) {
                            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                        }
                    }

                    if (sentToRouter) {
                        String idServicerequest = tbObat.getValueAt(row, COL_ID_SR).toString();
                        String finalAcsn = acsn;
                        setWebhookPending(noorder, kdJenisPrw, idServicerequest, finalAcsn);
                        String imagingId = getImagingStudyID(finalAcsn, 5, row);

                        if (imagingId != null && !imagingId.isEmpty() && !imagingId.equals("FAILED_STATUS")) {
                            simpanImagingStudy(noorder, kdJenisPrw, idServicerequest, finalAcsn, imagingId);
                            final String finalImgId = imagingId;
                            SwingUtilities.invokeLater(() -> {
                                tbObat.setValueAt(finalAcsn, row, COL_ACSN);
                                tbObat.setValueAt(finalImgId, row, COL_ID_IMAGING);
                                tbObat.setValueAt("Terkirim & Synced", row, COL_STATUS_ORTHANC);
                                tbObat.setValueAt(false, row, COL_PILIH);
                            });
                        } else if ("FAILED_STATUS".equals(imagingId)) {
                            SwingUtilities.invokeLater(() -> {
                                tbObat.setValueAt(false, row, COL_PILIH);
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                tbObat.setValueAt("Terkirim ke Router", row, COL_STATUS_ORTHANC);
                                tbObat.setValueAt(false, row, COL_PILIH);
                            });
                        }
                        sukses++;
                        System.out.println("Orthanc : Study " + orthancStudyId + " dikirim ke DICOM Router");
                    } else {
                        SwingUtilities.invokeLater(() -> tbObat.setValueAt("Gagal Kirim DICOM", row, COL_STATUS_ORTHANC));
                        gagal++;
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> tbObat.setValueAt("Error", row, COL_STATUS_ORTHANC));
                    gagal++;
                    System.out.println("BtnKirimDICOMRouter error baris " + row + " : " + e);
                }
            }

            final int finalSukses = sukses;
            final int finalGagal = gagal;
            SwingUtilities.invokeLater(() -> {
                this.setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this,
                        "Kirim DICOM Router selesai.\nBerhasil: " + finalSukses + "   Gagal/Skip: " + finalGagal,
                        "Kirim ke DICOM Router", JOptionPane.INFORMATION_MESSAGE);
            });
        });
    }//GEN-LAST:event_BtnKirimDICOMRouterActionPerformed

    private void BtnUpdateDanKirimActionPerformed(java.awt.event.ActionEvent evt) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        runBackground(() -> {
            int sukses = 0, gagal = 0;
            for (int r = 0; r < tbObat.getRowCount(); r++) {
                final int row = r;
                if (!tbObat.getValueAt(row, COL_PILIH).toString().equals("true")) {
                    continue;
                }

                String noorder = tbObat.getValueAt(row, COL_NOORDER).toString();
                String kdJenisPrw = tbObat.getValueAt(row, COL_KD_JENIS_PRW).toString();
                String acsn = buildAcsn(noorder, kdJenisPrw);
                String patientId = tbObat.getValueAt(row, COL_NO_RM).toString();
                String procedureDesc = valueAtString(row, COL_NM_PERAWATAN);
                String clinicalDiag = valueAtString(row, COL_DIAGNOSA);

                Object modObj = tbObat.getValueAt(row, COL_MODALITY);
                String modality = modObj == null ? "-" : modObj.toString().trim();
                if (modality.isEmpty() || "-".equals(modality)) {
                    modality = "OT";
                }

                Object tglP = tbObat.getValueAt(row, COL_TGL_PERMINTAAN);
                Object jamP = tbObat.getValueAt(row, COL_JAM_PERMINTAAN);
                String tglPermStr = tglP == null ? "" : tglP.toString();
                String jamNorm = normalizeJamPermintaan(jamP == null ? "" : jamP.toString());
                String scheduledDate = dicomStudyDateFromYmd(tglPermStr);
                String scheduledTime = toDicomTimeFromSqlTime(jamNorm);

                String physicianName = valueAtString(row, COL_NM_DOKTER_PERUJUK);
                String stationName = valueAtString(row, COL_NM_POLI);
                String aeTitle = modalityMapper.getAeTitle(kdJenisPrw, modality, koneksiDB.AETITLE_DICOMROUTER());

                Map<String, Object> sqItem = new LinkedHashMap<>();
                sqItem.put("Modality", modality);
                sqItem.put("ScheduledStationAETitle", aeTitle);
                putDicomIfNonempty(sqItem, "ScheduledProcedureStepStartDate", scheduledDate);
                sqItem.put("ScheduledProcedureStepStartTime", scheduledTime);
                putDicomIfNonempty(sqItem, "ScheduledPerformingPhysicianName", physicianName);
                sqItem.put("ScheduledProcedureStepDescription", procedureDesc);
                sqItem.put("ScheduledProcedureStepID", noorder);
                putDicomIfNonempty(sqItem, "ScheduledStationName", stationName);
                putDicomIfNonempty(sqItem, "CommentsOnTheScheduledProcedureStep", clinicalDiag);

                String orthancModifyJson;
                try {
                    orthancModifyJson = buildOrthancModifyPayloadJson(acsn, patientId, modality, procedureDesc, clinicalDiag,
                            noorder, scheduledDate, scheduledTime,
                            physicianName, stationName, aeTitle,
                            sanitizeDicomPersonName(valueAtString(row, COL_NAMA_PASIEN)),
                            valueAtString(row, COL_TGL_LAHIR), valueAtString(row, COL_JK),
                            Collections.singletonList(sqItem));
                } catch (JsonProcessingException ex) {
                    System.out.println("buildOrthancModifyPayloadJson : " + ex);
                    SwingUtilities.invokeLater(() -> tbObat.setValueAt("Gagal bikin payload", row, COL_STATUS_ORTHANC));
                    gagal++;
                    continue;
                }

                try {
                    // Step 1: Find study in Orthanc using strict modality filter
                    String orthancStudyId = resolveOrthancStudyId(row);
                    boolean isNewUpload = false;
                    if (orthancStudyId.isEmpty()) {
                        Object lokObj = tbObat.getValueAt(row, COL_LOKASI_IMAGE);
                        String lok = lokObj == null ? "" : lokObj.toString().trim();
                        if (lok.equalsIgnoreCase("webapps")) {
                            System.out.println("Orthanc : Study not found in Orthanc, attempting auto-upload from webapps first");
                            if (uploadSingleRowToOrthanc(row)) {
                                orthancStudyId = resolveOrthancStudyId(row);
                                isNewUpload = true;
                            }
                        }
                    }

                    if (orthancStudyId.isEmpty()) {
                        SwingUtilities.invokeLater(() -> tbObat.setValueAt("Study Tidak Ditemukan", row, COL_STATUS_ORTHANC));
                        gagal++;
                        continue;
                    }

                    // Step 2: Update all tags via UbahTagsStudy (KeepSource=false -> new study ID created)
                    if (isNewUpload) {
                        System.out.println("Orthanc : Skipping redundant UbahTagsStudy for newly uploaded study. noorder=" + noorder);
                    } else {
                        if (!orthanc.UbahTagsStudy(orthancStudyId, orthancModifyJson, true)) {
                            SwingUtilities.invokeLater(() -> tbObat.setValueAt("Gagal Update Tags", row, COL_STATUS_ORTHANC));
                            gagal++;
                            System.out.println("Orthanc : Gagal update tags, skip kirim DICOM. noorder=" + noorder);
                            continue;
                        }
                    }
                    SwingUtilities.invokeLater(() -> tbObat.setValueAt(acsn, row, COL_ACSN));
                    System.out.println("Orthanc : Tags updated/skipped, ACSN -> " + acsn);

                    // Step 3: Re-find the study by ACSN to get the new Orthanc study ID
                    String newStudyId = orthanc.findStudyByAccession(acsn);
                    if (newStudyId.isEmpty()) {
                        SwingUtilities.invokeLater(() -> tbObat.setValueAt("Study Baru Tidak Ditemukan", row, COL_STATUS_ORTHANC));
                        gagal++;
                        System.out.println("Orthanc : Study baru tidak ditemukan setelah update tags ACSN=" + acsn);
                        continue;
                    }

                    // Step 4: Send the updated study to the DICOM router with retries
                    int maxRetries = 3;
                    boolean sentToRouter = false;
                    for (int attempt = 1; attempt <= maxRetries; attempt++) {
                        if (orthanc.kirimKeModality(newStudyId, true)) {
                            sentToRouter = true;
                            break;
                        }
                        System.out.println("Orthanc : Send attempt " + attempt + " failed for study " + newStudyId + ", retrying...");
                        if (attempt < maxRetries) {
                            try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
                        }
                    }

                    if (sentToRouter) {
                        String idServicerequest = tbObat.getValueAt(row, COL_ID_SR).toString();
                        String finalAcsn = acsn;
                        setWebhookPending(noorder, kdJenisPrw, idServicerequest, finalAcsn);
                        String imagingId = getImagingStudyID(finalAcsn, 5, row);

                        if (imagingId != null && !imagingId.isEmpty() && !imagingId.equals("FAILED_STATUS")) {
                            simpanImagingStudy(noorder, kdJenisPrw, idServicerequest, finalAcsn, imagingId);
                            final String finalImgId = imagingId;
                            SwingUtilities.invokeLater(() -> {
                                tbObat.setValueAt(finalAcsn, row, COL_ACSN);
                                tbObat.setValueAt(finalImgId, row, COL_ID_IMAGING);
                                tbObat.setValueAt("Terkirim & Synced", row, COL_STATUS_ORTHANC);
                                tbObat.setValueAt(false, row, COL_PILIH);
                            });
                        } else if ("FAILED_STATUS".equals(imagingId)) {
                            SwingUtilities.invokeLater(() -> {
                                tbObat.setValueAt(false, row, COL_PILIH);
                            });
                        } else {
                            SwingUtilities.invokeLater(() -> {
                                tbObat.setValueAt("Terkirim ke Router", row, COL_STATUS_ORTHANC);
                                tbObat.setValueAt(false, row, COL_PILIH);
                            });
                        }
                        sukses++;
                        System.out.println("Orthanc : Study " + newStudyId + " dikirim ke DICOM Router");
                    } else {
                        SwingUtilities.invokeLater(() -> tbObat.setValueAt("Gagal Kirim DICOM", row, COL_STATUS_ORTHANC));
                        gagal++;
                    }
                } catch (Exception e) {
                    SwingUtilities.invokeLater(() -> tbObat.setValueAt("Error", row, COL_STATUS_ORTHANC));
                    gagal++;
                    System.out.println("BtnUpdateDanKirim error baris " + row + " : " + e);
                }
            }

            final int finalSukses = sukses;
            final int finalGagal = gagal;
            SwingUtilities.invokeLater(() -> {
                this.setCursor(Cursor.getDefaultCursor());
                JOptionPane.showMessageDialog(this,
                        "Update ACSN & Kirim DICOM selesai.\nBerhasil: " + finalSukses + "   Gagal/Skip: " + finalGagal,
                        "Update ACSN & Kirim DICOM", JOptionPane.INFORMATION_MESSAGE);
            });
        });
    }

    private void BtnKirimOrthancActionPerformed(java.awt.event.ActionEvent evt) {
        List<Integer> rows = new ArrayList<>();
        for (int r = 0; r < tbObat.getRowCount(); r++) {
            if (!isRowCheckboxSelected(r)) {
                continue;
            }
            Object lokObj = tbObat.getValueAt(r, COL_LOKASI_IMAGE);
            String lok = lokObj == null ? "" : lokObj.toString().trim();
            if (!lok.equalsIgnoreCase("webapps")) {
                continue;
            }
            rows.add(r);
        }
        if (rows.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Centang satu atau lebih baris dengan Lokasi Image = webapps.",
                    "Kirim ke Orthanc", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (executor.isShutdown() || executor.isTerminated()) {
            return;
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        executor.submit(() -> {
            int[] sukgag = new int[]{0, 0};
            try {
                for (Integer rowIdx : rows) {
                    final int i = rowIdx.intValue();
                    if (uploadSingleRowToOrthanc(i)) {
                        sukgag[0]++;
                    } else {
                        sukgag[1]++;
                    }
                }
            } finally {
                final int ok = sukgag[0];
                final int fail = sukgag[1];
                SwingUtilities.invokeLater(() -> {
                    if (isDisplayable()) {
                        setCursor(Cursor.getDefaultCursor());
                    }
                    if (ok > 0 || fail > 0) {
                        JOptionPane.showMessageDialog(SatuSehatKirimServiceRequestRadiologi.this,
                                "Konversi & kirim webapps → Orthanc selesai.\nBerhasil: " + ok + "   Gagal: " + fail,
                                "Kirim ke Orthanc", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        });
    }

    private boolean uploadSingleRowToOrthanc(int i) {
        SwingUtilities.invokeLater(() -> tbObat.setValueAt("Mengunduh...", i, COL_STATUS_ORTHANC));

        String noRawat = valueAtString(i, COL_NO_RAWAT);
        String noorder = tbObat.getValueAt(i, COL_NOORDER).toString();
        String kdJenis = tbObat.getValueAt(i, COL_KD_JENIS_PRW).toString();

        List<String> listLokasi = new ArrayList<>();
        try {
            PreparedStatement psImg = koneksi.prepareStatement(
                    "select gambar_radiologi.lokasi_gambar from periksa_radiologi "
                    + "inner join gambar_radiologi on gambar_radiologi.no_rawat=periksa_radiologi.no_rawat "
                    + "and gambar_radiologi.tgl_periksa=periksa_radiologi.tgl_periksa "
                    + "and gambar_radiologi.jam=periksa_radiologi.jam "
                    + "inner join permintaan_radiologi on permintaan_radiologi.no_rawat=periksa_radiologi.no_rawat "
                    + "and permintaan_radiologi.tgl_hasil=periksa_radiologi.tgl_periksa "
                    + "where periksa_radiologi.no_rawat=? and periksa_radiologi.kd_jenis_prw=? and permintaan_radiologi.noorder=?");
            try {
                psImg.setString(1, noRawat);
                psImg.setString(2, kdJenis);
                psImg.setString(3, noorder);
                ResultSet rsImg = psImg.executeQuery();
                while (rsImg.next()) {
                    String lf = rsImg.getString("lokasi_gambar");
                    if (lf != null && !lf.trim().isEmpty()) {
                        listLokasi.add(lf.trim());
                    }
                }
            } finally {
                if (psImg != null) {
                    psImg.close();
                }
            }
        } catch (Exception ex) {
            System.out.println("Error fetch gambar: " + ex);
        }

        if (listLokasi.isEmpty()) {
            Object fnObj = tbObat.getValueAt(i, COL_LOKASI_FILE_IMAGE);
            String lf = fnObj == null ? "" : fnObj.toString().trim();
            if (!lf.isEmpty()) {
                listLokasi.add(lf);
            }
        }

        if (listLokasi.isEmpty()) {
            SwingUtilities.invokeLater(() -> tbObat.setValueAt("File image tidak ada", i, COL_STATUS_ORTHANC));
            return false;
        }

        String acsn = buildAcsn(noorder, kdJenis);
        String patientId = tbObat.getValueAt(i, COL_NO_RM).toString();
        String procedureDesc = valueAtString(i, COL_NM_PERAWATAN);
        String clinicalDiag = valueAtString(i, COL_DIAGNOSA);

        Object modObj = tbObat.getValueAt(i, COL_MODALITY);
        String modality = modObj == null ? "-" : modObj.toString().trim();
        if (modality.isEmpty() || "-".equals(modality)) {
            modality = "OT";
        }

        Object tglP = tbObat.getValueAt(i, COL_TGL_PERMINTAAN);
        Object jamP = tbObat.getValueAt(i, COL_JAM_PERMINTAAN);
        String tglPermStr = tglP == null ? "" : tglP.toString();
        String jamNorm = normalizeJamPermintaan(jamP == null ? "" : jamP.toString());
        String scheduledDate = dicomStudyDateFromYmd(tglPermStr);
        String scheduledTime = toDicomTimeFromSqlTime(jamNorm);

        String physicianName = valueAtString(i, COL_NM_DOKTER_PERUJUK);
        String stationName = valueAtString(i, COL_NM_POLI);
        String aeTitle = modalityMapper.getAeTitle(kdJenis, modality, koneksiDB.AETITLE_DICOMROUTER());

        Map<String, Object> sqItem = new LinkedHashMap<>();
        sqItem.put("Modality", modality);
        sqItem.put("ScheduledStationAETitle", aeTitle);
        putDicomIfNonempty(sqItem, "ScheduledProcedureStepStartDate", scheduledDate);
        sqItem.put("ScheduledProcedureStepStartTime", scheduledTime);
        putDicomIfNonempty(sqItem, "ScheduledPerformingPhysicianName", physicianName);
        sqItem.put("ScheduledProcedureStepDescription", procedureDesc);
        sqItem.put("ScheduledProcedureStepID", noorder);
        putDicomIfNonempty(sqItem, "ScheduledStationName", stationName);
        putDicomIfNonempty(sqItem, "CommentsOnTheScheduledProcedureStep", clinicalDiag);

        String orthancModifyJson;
        try {
            orthancModifyJson = buildOrthancModifyPayloadJson(acsn, patientId, modality, procedureDesc, clinicalDiag,
                    noorder, scheduledDate, scheduledTime,
                    physicianName, stationName, aeTitle,
                    sanitizeDicomPersonName(valueAtString(i, COL_NAMA_PASIEN)),
                    valueAtString(i, COL_TGL_LAHIR), valueAtString(i, COL_JK),
                    Collections.singletonList(sqItem));
        } catch (JsonProcessingException ex) {
            System.out.println("buildOrthancModifyPayloadJson : " + ex);
            SwingUtilities.invokeLater(() -> tbObat.setValueAt("Gagal bikin payload", i, COL_STATUS_ORTHANC));
            return false;
        }

        String pn = sanitizeDicomPersonName(valueAtString(i, COL_NAMA_PASIEN));
        List<String> keys = new ArrayList<>();
        keys.add("Modality=" + modality);
        keys.add("PatientID=" + patientId.replace("=", ""));
        if (!scheduledDate.isEmpty()) {
            keys.add("StudyDate=" + scheduledDate);
        }
        if (!pn.isEmpty() && !pn.contains("=")) {
            keys.add("PatientName=" + pn);
        }
        if (!acsn.isEmpty() && !acsn.contains("=")) {
            keys.add("AccessionNumber=" + acsn);
        }
        String dob = valueAtString(i, COL_TGL_LAHIR);
        String sex = valueAtString(i, COL_JK);
        if (!dob.isEmpty() && !dob.contains("=")) {
            keys.add("PatientBirthDate=" + dob);
        }
        if (!sex.isEmpty() && !sex.contains("=")) {
            keys.add("PatientSex=" + sex);
        }

        Map<String, Object> paramMap = new LinkedHashMap<>();
        paramMap.put("output_sop_class", "sec-capture");
        paramMap.put("keys", keys);
        String parametersJson;
        try {
            parametersJson = mapper.writeValueAsString(paramMap);
        } catch (JsonProcessingException ex) {
            parametersJson = "{\"output_sop_class\":\"sec-capture\",\"keys\":[\"Modality=" + modality + "\"]}";
        }

        List<String> listUrls = new ArrayList<>();
        for (String lokasiFile : listLokasi) {
            String url = "http://" + koneksiDB.HOSTHYBRIDWEB() + ":" + koneksiDB.PORTWEB()
                    + "/" + koneksiDB.HYBRIDWEB() + "/radiologi/" + lokasiFile;
            listUrls.add(url);
        }

        SwingUtilities.invokeLater(() -> tbObat.setValueAt("Mengirim ke API...", i, COL_STATUS_ORTHANC));
        JsonNode result = orthanc.KirimKeDicomConverterFromURLs(listUrls, parametersJson, orthancModifyJson);

        if (result != null && "success".equalsIgnoreCase(result.path("status").asText().trim())) {
            final String acsnF = acsn;
            final String finalTxt = "Sukses konversi & Orthanc (" + listUrls.size() + " img)";
            SwingUtilities.invokeLater(() -> {
                tbObat.setValueAt(finalTxt, i, COL_STATUS_ORTHANC);
                tbObat.setValueAt(acsnF, i, COL_ACSN);
                tbObat.setValueAt("orthanc", i, COL_LOKASI_IMAGE);
                tbObat.setValueAt(false, i, COL_PILIH);
            });
            return true;
        } else {
            String lastErr = summarizeDicomConverterApiError(result);
            final String errOut = "Gagal: " + lastErr;
            SwingUtilities.invokeLater(() -> tbObat.setValueAt(errOut, i, COL_STATUS_ORTHANC));
            return false;
        }
    }

    // =========================================================================
    // Popup menu
    // =========================================================================

    private void ppPilihSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihSemuaActionPerformed
        for (i = 0; i < tbObat.getRowCount(); i++) {
            tbObat.setValueAt(true, i, COL_PILIH);
        }
    }//GEN-LAST:event_ppPilihSemuaActionPerformed

    private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
        for (i = 0; i < tbObat.getRowCount(); i++) {
            tbObat.setValueAt(false, i, COL_PILIH);
        }
    }//GEN-LAST:event_ppBersihkanActionPerformed

    private void ppPilihBelumTerkirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihBelumTerkirimActionPerformed
        for (int i = 0; i < tbObat.getRowCount(); i++) {
            String val = tbObat.getValueAt(i, COL_ID_SR) == null
                    ? "" : tbObat.getValueAt(i, COL_ID_SR).toString().trim();
            tbObat.setValueAt(val.isEmpty() || val.equals("-"), i, COL_PILIH);
        }
    }//GEN-LAST:event_ppPilihBelumTerkirimActionPerformed

    private void ppPilihBelumTerkirim1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihBelumTerkirim1ActionPerformed
        for (int i = 0; i < tbObat.getRowCount(); i++) {
            Object srObj = tbObat.getValueAt(i, COL_ID_SR);
            Object imgObj = tbObat.getValueAt(i, COL_ID_IMAGING);
            String srVal = srObj == null ? "" : srObj.toString().trim();
            String imgVal = imgObj == null ? "" : imgObj.toString().trim();
            tbObat.setValueAt(!srVal.isEmpty() && !srVal.equals("-") && (imgVal.isEmpty() || imgVal.equals("-")), i, COL_PILIH);
        }
    }//GEN-LAST:event_ppPilihBelumTerkirim1ActionPerformed

    private void ppPilihLokasiOrthancActionPerformed(java.awt.event.ActionEvent evt) {
        for (int i = 0; i < tbObat.getRowCount(); i++) {
            String val = tbObat.getValueAt(i, COL_LOKASI_IMAGE) == null
                    ? "" : tbObat.getValueAt(i, COL_LOKASI_IMAGE).toString().trim();
            tbObat.setValueAt(!val.isEmpty() && val.equalsIgnoreCase("orthanc"), i, COL_PILIH);
        }
    }

    private void ppPilihLokasiWebappsActionPerformed(java.awt.event.ActionEvent evt) {
        for (int i = 0; i < tbObat.getRowCount(); i++) {
            String val = tbObat.getValueAt(i, COL_LOKASI_IMAGE) == null
                    ? "" : tbObat.getValueAt(i, COL_LOKASI_IMAGE).toString().trim();
            tbObat.setValueAt(!val.isEmpty() && val.equalsIgnoreCase("webapps"), i, COL_PILIH);
        }
    }

    // =========================================================================
    // Print
    // =========================================================================

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            htmlContent = new StringBuilder();
            htmlContent.append(
                    "<tr class='isi'>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.Rawat</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.RM</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Pasien</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.KTP Pasien</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Kode Dokter</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Dokter Perujuk</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.KTP Dokter</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Encounter</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>No.Permintaan</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Tgl &amp; Jam Permintaan</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Diagnosa Klinis</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Nama Pemeriksaan</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Radiologi Code</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Radiologi System</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>Radiologi Display</b></td>"
                    + "<td valign='middle' bgcolor='#FFFAFA' align='center'><b>ID Service Request</b></td>"
                    + "</tr>");
            for (i = 0; i < tabMode.getRowCount(); i++) {
                htmlContent.append("<tr class='isi'>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_NO_RAWAT) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_NO_RM) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_NAMA_PASIEN) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_KTP_PASIEN) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_KD_DOKTER) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_NAMA_DOKTER) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_KTP_DOKTER) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_ID_ENCOUNTER) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_NOORDER) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_TGL_JAM) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_DIAGNOSA) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_NM_PERAWATAN) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_RAD_CODE) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_RAD_SYSTEM) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_RAD_DISPLAY) + "</td>"
                        + "<td valign='top'>" + tbObat.getValueAt(i, COL_ID_SR) + "</td>"
                        + "</tr>");
            }
            String cssRules
                    = ".isi td{border-right:1px solid #e2e7dd;font:8.5px tahoma;height:12px;"
                    + "border-bottom:1px solid #e2e7dd;background:#ffffff;color:#323232;}"
                    + ".isi2 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#323232;}"
                    + ".isi3 td{border-right:1px solid #e2e7dd;font:8.5px tahoma;height:12px;"
                    + "border-top:1px solid #e2e7dd;background:#ffffff;color:#323232;}"
                    + ".isi4 td{font:11px tahoma;height:12px;border-top:1px solid #e2e7dd;background:#ffffff;color:#323232;}"
                    + ".isi5 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#AA0000;}"
                    + ".isi6 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#FF0000;}"
                    + ".isi7 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#C8C800;}"
                    + ".isi8 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#00AA00;}"
                    + ".isi9 td{font:8.5px tahoma;border:none;height:12px;background:#ffffff;color:#969696;}";

            LoadHTML.setText("<html><table width='100%' border='0' align='center' "
                    + "cellpadding='1px' cellspacing='0' class='tbl_form'>"
                    + htmlContent + "</table></html>");
            htmlContent = null;

            File cssFile = new File("file2.css");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(cssFile))) {
                bw.write(cssRules);
            }

            File htmlFile = new File("DataSatuSehatServiceRequestRadiologi.html");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(htmlFile))) {
                bw.write(LoadHTML.getText().replaceAll("<head>", "<head>"
                        + "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />"
                        + "<table width='100%' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>"
                        + "<tr class='isi2'><td valign='top' align='center'>"
                        + "<font size='4' face='Tahoma'>" + akses.getnamars() + "</font><br>"
                        + akses.getalamatrs() + ", " + akses.getkabupatenrs() + ", " + akses.getpropinsirs() + "<br>"
                        + akses.getkontakrs() + ", E-mail : " + akses.getemailrs() + "<br><br>"
                        + "<font size='2' face='Tahoma'>DATA PENGIRIMAN SATU SEHAT SERVICE REQUEST RADIOLOGI<br><br></font>"
                        + "</td></tr></table>"));
            }
            Desktop.getDesktop().browse(htmlFile.toURI());
        } catch (Exception e) {
            System.out.println("BtnPrint error : " + e);
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnPrintActionPerformed

    // =========================================================================
    // Public API
    // =========================================================================
    /**
     * Applies role-based enable/disable to all action buttons.
     */
    public void isCek() {
        boolean allowed = akses.getsatu_sehat_kirim_servicerequest_radiologi();
        BtnKirim.setEnabled(allowed);
        BtnUpdate.setEnabled(allowed);
        BtnPrint.setEnabled(allowed);
        BtnKirimOrthanc.setEnabled(allowed);
        BtnUpdateACSNOrthanc.setEnabled(allowed);
        BtnKirimDICOMRouter.setEnabled(allowed);
        BtnUpdateDanKirim.setEnabled(allowed);
    }

    public JTable getTable() {
        return tbObat;
    }

    // =========================================================================
    // Private helpers
    // =========================================================================
    /**
     * Builds the ACSN string from order number and procedure code. Example:
     * noorder="PR240001", kdJenisPrw="CT01" → "240001CT01"
     */
    private String buildAcsn(String noorder, String kdJenisPrw) {
        String base = noorder.replace("PR", "") + kdJenisPrw;
        return base.replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }

    private boolean isRowCheckboxSelected(int row) {
        Object v = tbObat.getValueAt(row, COL_PILIH);
        return Boolean.TRUE.equals(v) || "true".equalsIgnoreCase(String.valueOf(v));
    }

    private String valueAtString(int row, int col) {
        Object o = tbObat.getValueAt(row, col);
        return o == null ? "" : o.toString();
    }

    private String summarizeDicomConverterApiError(JsonNode result) {
        if (result == null) {
            return "Tanpa jawaban dari server";
        }
        String msg = result.path("error").asText().trim();
        String code = result.path("code").asText().trim();
        String details = result.path("details").asText().trim();
        StringBuilder sb = new StringBuilder();
        if (!code.isEmpty()) {
            sb.append(code);
        }
        if (!msg.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(" — ");
            }
            sb.append(msg);
        }
        if (!details.isEmpty()) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append('(').append(details).append(')');
        }
        return sb.length() > 0 ? sb.toString() : result.toString();
    }

    private static String normalizeJamPermintaan(String jamSql) {
        if (jamSql == null) {
            return "";
        }
        String j = jamSql.trim();
        if (j.isEmpty() || "00:00:00".equals(j)) {
            return "";
        }
        return j;
    }

    private static String dicomStudyDateFromYmd(String tglHyphen) {
        if (tglHyphen == null || tglHyphen.length() < 10) {
            return "";
        }
        return tglHyphen.substring(0, 10).replace("-", "");
    }

    private static String toDicomTimeFromSqlTime(String hhmmSs) {
        if (hhmmSs == null || hhmmSs.isEmpty()) {
            return "000000";
        }
        String[] p = hhmmSs.trim().split(":");
        int h = 0;
        int m = 0;
        int s = 0;
        try {
            if (p.length >= 1) {
                h = Integer.parseInt(p[0]);
            }
            if (p.length >= 2) {
                m = Integer.parseInt(p[1]);
            }
            if (p.length >= 3) {
                s = Integer.parseInt(p[2]);
            }
        } catch (NumberFormatException ex) {
            return "000000";
        }
        return String.format("%02d%02d%02d", h, m, s);
    }

    /**
     * Basic cleanup for PN; avoids control chars.
     */
    private static String sanitizeDicomPersonName(String nm) {
        if (nm == null) {
            return "";
        }
        return nm.replace('\r', ' ').replace('\n', ' ').trim();
    }

    private static String sanitizeAeTitle(String modalityBasis) {
        String m = modalityBasis == null ? "OT" : modalityBasis.trim().toUpperCase();
        if (m.isEmpty() || "-".equals(m)) {
            m = "OT";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < m.length(); i++) {
            char c = m.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_') {
                sb.append(c);
            }
        }
        if (sb.length() == 0) {
            sb.append("OT");
        }
        String out = sb + "_STATION";
        return out.length() <= 16 ? out : out.substring(0, 16);
    }

    private String buildOrthancModifyPayloadJson(
            String acsn,
            String patientId,
            String modality,
            String procedureDesc,
            String clinicalDiag,
            String noorder,
            String scheduledDate,
            String scheduledTime,
            String physicianName,
            String stationName,
            String aeTitle,
            String patientName,
            String patientBirthDate,
            String patientSex,
            List<Map<String, Object>> scheduledProcedureStepSequence
    ) throws JsonProcessingException {
        // Generate a production-grade, globally unique, deterministic ISO 2.25 OID based on Patient ID and Accession Number
        String source = "PATIENT:" + (patientId != null ? patientId.trim() : "") + "|ACCESSION:" + (acsn != null ? acsn.trim() : "");
        java.util.UUID uuid = java.util.UUID.nameUUIDFromBytes(source.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        java.nio.ByteBuffer bb = java.nio.ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        java.math.BigInteger bigInt = new java.math.BigInteger(1, bb.array());
        String studyUid = "2.25." + bigInt.toString();

        Map<String, Object> replace = new LinkedHashMap<>();
        putDicomIfNonempty(replace, "StudyInstanceUID", studyUid);
        putDicomIfNonempty(replace, "AccessionNumber", acsn);
        putDicomIfNonempty(replace, "PatientName", patientName);
        putDicomIfNonempty(replace, "PatientID", patientId);
        putDicomIfNonempty(replace, "PatientBirthDate", patientBirthDate);
        putDicomIfNonempty(replace, "PatientSex", patientSex);
        putDicomIfNonempty(replace, "RequestedProcedureDescription", procedureDesc);
        putDicomIfNonempty(replace, "RequestedProcedureID", noorder);
        putDicomIfNonempty(replace, "ReasonForTheRequestedProcedure", clinicalDiag);
        putDicomIfNonempty(replace, "ReferringPhysicianName", sanitizeDicomPersonName(physicianName));
        putDicomIfNonempty(replace, "RequestingPhysician", sanitizeDicomPersonName(physicianName));
        putDicomIfNonempty(replace, "StudyDate", scheduledDate);
        putDicomIfNonempty(replace, "StudyTime", scheduledTime);
        String instName = Sequel.cariIsi("select setting.nama_instansi from setting limit 1");
        if (instName == null || instName.trim().isEmpty()) {
            instName = "SIMRS KHANZA";
        }
        putDicomIfNonempty(replace, "InstitutionName", instName);
        putDicomIfNonempty(replace, "Modality", modality);
        putDicomIfNonempty(replace, "ScheduledStationAETitle", aeTitle);
        putDicomIfNonempty(replace, "ScheduledProcedureStepStartDate", scheduledDate);
        if (scheduledTime != null && !scheduledTime.trim().isEmpty()) {
            replace.put("ScheduledProcedureStepStartTime", scheduledTime);
        }
        putDicomIfNonempty(replace, "ScheduledPerformingPhysicianName", physicianName);
        putDicomIfNonempty(replace, "ScheduledProcedureStepDescription", procedureDesc);
        putDicomIfNonempty(replace, "ScheduledProcedureStepID", noorder);
        putDicomIfNonempty(replace, "ScheduledStationName", stationName);
        replace.put("ScheduledProcedureStepSequence", scheduledProcedureStepSequence);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("Replace", replace);
        body.put("Remove", Collections.emptyList());
        body.put("Keep", Collections.emptyList());
        body.put("KeepSource", Boolean.FALSE);
        body.put("KeepLabels", Boolean.TRUE);
        body.put("Force", Boolean.TRUE);
        return mapper.writeValueAsString(body);
    }

    private static void putDicomIfNonempty(Map<String, Object> dest, String tag, String value) {
        if (value != null && !value.trim().isEmpty()) {
            dest.put(tag, value.trim());
        }
    }

    private boolean orthancStudyExistsCached(String noRM, String studyDateYYYYMMDD, String modality,
            Map<String, Boolean> cache) {
        if (modality == null || modality.isEmpty() || "-".equals(modality)) {
            return false;
        }
        String key = noRM + "|" + studyDateYYYYMMDD + "|" + modality;
        Boolean hit = cache.get(key);
        if (hit != null) {
            return hit;
        }
        JsonNode studies = orthanc.AmbilSeriesDenganModality(noRM, studyDateYYYYMMDD,
                studyDateYYYYMMDD, modality);
        boolean ok = studies != null && studies.isArray() && studies.size() > 0;
        cache.put(key, ok);
        return ok;
    }

    private int timeToSeconds(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return -1;
        }
        timeStr = timeStr.trim().replaceAll("[^0-9]", "");
        if (timeStr.length() >= 4) {
            try {
                int hr = Integer.parseInt(timeStr.substring(0, 2));
                int min = Integer.parseInt(timeStr.substring(2, 4));
                int sec = 0;
                if (timeStr.length() >= 6) {
                    sec = Integer.parseInt(timeStr.substring(4, 6));
                }
                return hr * 3600 + min * 60 + sec;
            } catch (Exception e) {
                return -1;
            }
        }
        return -1;
    }

    /**
     * Resolves the Orthanc internal study ID for a given table row. Uses an
     * Initial Query Strategy (Accession-first) and a High-Confidence Matching
     * Engine (PatientID + date + ModalitiesInStudy with scoring) to prevent cross-matching.
     *
     * @param row table row index
     * @return Orthanc internal study ID, or empty string if not found / no
     * mapping
     */
    private String resolveOrthancStudyId(int row) {
        String noRM = tbObat.getValueAt(row, COL_NO_RM).toString().trim();
        String kdJenisPrw = tbObat.getValueAt(row, COL_KD_JENIS_PRW).toString().trim();
        String noorder = tbObat.getValueAt(row, COL_NOORDER).toString().trim();
        String acsn = buildAcsn(noorder, kdJenisPrw);

        // 1. Initial Query Strategy: Query Orthanc by Accession Number first
        String studyByAcsn = orthanc.findStudyByAccession(acsn);
        if (studyByAcsn != null && !studyByAcsn.isEmpty()) {
            System.out.println("Orthanc : Study ditemukan langsung via AccessionNumber=" + acsn + " -> Study ID: " + studyByAcsn);
            return studyByAcsn;
        }

        String tglPermintaan = valueAtString(row, COL_TGL_PERMINTAAN);
        if (tglPermintaan.length() < 10) {
            tglPermintaan = tbObat.getValueAt(row, COL_TGL_JAM).toString().trim();
        }

        // Resolve DICOM modality from the procedure code mapping
        String modality = modalityMapper.getModality(kdJenisPrw);
        if (modality == null || modality.isEmpty()) {
            System.out.println("Orthanc Skip : Mapping modality tidak ditemukan untuk kd_jenis_prw=" + kdJenisPrw);
            return "";
        }

        // Extract and convert date to DICOM format yyyyMMdd
        if (tglPermintaan.length() < 10) {
            System.out.println("Orthanc Skip : Tanggal permintaan tidak valid untuk baris " + row);
            return "";
        }
        String tanggal = dicomStudyDateFromYmd(tglPermintaan);

        // Query Orthanc with modality filter
        JsonNode studies = orthanc.AmbilSeriesDenganModality(noRM, tanggal, tanggal, modality);
        if (studies == null || !studies.isArray() || studies.size() == 0) {
            System.out.println("Orthanc Skip : Tidak ditemukan study untuk RM=" + noRM
                    + ", Tanggal=" + tanggal + ", Modality=" + modality);
            return "";
        }

        // 2. High-Confidence Matching Engine
        // Resolve SIMRS exam time
        String jamPermStr = valueAtString(row, COL_JAM_PERMINTAAN);
        if (jamPermStr.isEmpty() || "00:00:00".equals(jamPermStr)) {
            String tglJam = valueAtString(row, COL_TGL_JAM);
            if (tglJam.length() >= 19) {
                jamPermStr = tglJam.substring(11, 19);
            }
        }
        int examSeconds = timeToSeconds(jamPermStr);
        String nmPerawatan = valueAtString(row, COL_NM_PERAWATAN).trim().toLowerCase();

        String bestMatchId = "";
        int bestScore = -1;
        int candidateCount = 0;

        for (int i = 0; i < studies.size(); i++) {
            JsonNode s = studies.get(i);
            String sId = s.path("ID").asText();
            if (sId.isEmpty()) continue;

            // Exclude study if already assigned to a different accession number
            String studyAcsn = s.path("MainDicomTags").path("AccessionNumber").asText().trim();
            if (!studyAcsn.isEmpty() && !"-".equals(studyAcsn) && !studyAcsn.equals(acsn)) {
                System.out.println("Orthanc Match Skip Study " + sId + " karena AccessionNumber '" + studyAcsn + "' tidak cocok");
                continue;
            }

            int score = 0;

            // Match 1: Time Proximity (max 60 minutes / 3600 seconds absolute difference)
            String studyTime = s.path("MainDicomTags").path("StudyTime").asText().trim();
            int studySeconds = timeToSeconds(studyTime);
            if (examSeconds >= 0 && studySeconds >= 0) {
                int diff = Math.abs(studySeconds - examSeconds);
                if (diff <= 3600) {
                    score += 50;
                    System.out.println("Orthanc Match Study " + sId + " cocok waktu: " + studyTime + " vs exam: " + jamPermStr + " (diff: " + diff + "s) (+50 pts)");
                } else {
                    System.out.println("Orthanc Match Study " + sId + " beda waktu terlalu jauh: " + studyTime + " vs exam: " + jamPermStr + " (diff: " + diff + "s)");
                }
            }

            // Match 2: Procedure Description
            String studyDesc = s.path("MainDicomTags").path("StudyDescription").asText().trim().toLowerCase();
            if (!studyDesc.isEmpty() && !nmPerawatan.isEmpty()) {
                if (studyDesc.contains(nmPerawatan) || nmPerawatan.contains(studyDesc)) {
                    score += 50;
                    System.out.println("Orthanc Match Study " + sId + " cocok deskripsi: '" + studyDesc + "' vs '" + nmPerawatan + "' (+50 pts)");
                }
            }

            if (score >= 50) {
                candidateCount++;
                if (score > bestScore) {
                    bestScore = score;
                    bestMatchId = sId;
                } else if (score == bestScore) {
                    bestMatchId = ""; // Ambiguity
                }
            }
        }

        if (!bestMatchId.isEmpty() && candidateCount == 1) {
            System.out.println("Orthanc : High-confidence match ditemukan Study ID = " + bestMatchId);
            return bestMatchId;
        } else if (candidateCount > 1) {
            System.out.println("Orthanc Skip : Ditemukan lebih dari 1 match atau ambigu.");
            return "";
        }

        System.out.println("Orthanc Skip : Tidak ada High-confidence match ditemukan di PACS.");
        return "";
    }

    /**
     * Fetches the Satu Sehat ImagingStudy resource ID matching the given ACSN.
     *
     * @param acsn the AccessionNumber to search for
     * @return the ImagingStudy ID, or empty string if not found
     */
    private String getImagingStudyIDLocal(String acsn) {
        String idImaging = "";
        try {
            String sql = "SELECT id_imaging FROM satu_sehat_imagingstudy_radiologi WHERE acsn = ?";
            try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                ps.setString(1, acsn);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        idImaging = rs.getString("id_imaging");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("getImagingStudyIDLocal error : " + e);
        }
        return (idImaging == null || idImaging.equals("-")) ? "" : idImaging;
    }

    private void setWebhookPending(String noorder, String kdJenisPrw, String idServiceRequest, String acsn) {
        try {
            String sqlCheck = "SELECT acsn FROM satu_sehat_imagingstudy_radiologi WHERE noorder = ? AND kd_jenis_prw = ?";
            try (PreparedStatement psCheck = koneksi.prepareStatement(sqlCheck)) {
                psCheck.setString(1, noorder);
                psCheck.setString(2, kdJenisPrw);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        String sqlUpdate = "UPDATE satu_sehat_imagingstudy_radiologi SET id_servicerequest = ?, acsn = ?, status_webhook = 'PENDING', message_webhook = NULL WHERE noorder = ? AND kd_jenis_prw = ?";
                        try (PreparedStatement psUpdate = koneksi.prepareStatement(sqlUpdate)) {
                            psUpdate.setString(1, idServiceRequest);
                            psUpdate.setString(2, acsn);
                            psUpdate.setString(3, noorder);
                            psUpdate.setString(4, kdJenisPrw);
                            psUpdate.executeUpdate();
                        }
                    } else {
                        String sqlInsert = "INSERT INTO satu_sehat_imagingstudy_radiologi (noorder, kd_jenis_prw, id_servicerequest, acsn, id_imaging, status_webhook, message_webhook) VALUES (?, ?, ?, ?, '-', 'PENDING', NULL)";
                        try (PreparedStatement psInsert = koneksi.prepareStatement(sqlInsert)) {
                            psInsert.setString(1, noorder);
                            psInsert.setString(2, kdJenisPrw);
                            psInsert.setString(3, idServiceRequest);
                            psInsert.setString(4, acsn);
                            psInsert.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("setWebhookPending error : " + e);
        }
    }

    /**
     * Fetches the Satu Sehat ImagingStudy resource ID matching the given ACSN.
     * Checks the local database first before falling back to SatuSehat API.
     *
     * @param acsn the AccessionNumber to search for
     * @return the ImagingStudy ID, or empty string if not found
     */
    private String getImagingStudyID(String acsn) {
        // 1. Check local database cache first
        String localId = getImagingStudyIDLocal(acsn);
        if (localId != null && !localId.isEmpty()) {
            System.out.println("getImagingStudyID: Found cached local ID for ACSN " + acsn + " : " + localId);
            return localId;
        }

        // 2. Fallback to external SatuSehat API
        try {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.add("Authorization", "Bearer " + api.TokenSatuSehat());
            HttpEntity<String> entity = new HttpEntity<>(h);

            String url = link + "/ImagingStudy?identifier=http://sys-ids.kemkes.go.id/acsn/"
                    + koneksiDB.IDSATUSEHAT() + "|" + acsn;
            System.out.println("URL ImagingStudy : " + url);

            String jsonResponse = api.getRest().exchange(url, HttpMethod.GET, entity, String.class).getBody();
            System.out.println("JSON ImagingStudy : " + jsonResponse);

            JsonNode r = mapper.readTree(jsonResponse);
            for (JsonNode entry : r.path("entry")) {
                String id = entry.path("resource").path("id").asText();
                if (!id.isEmpty()) {
                    System.out.println("ImagingStudy ID untuk ACSN " + acsn + " : " + id);
                    return id;
                }
            }
        } catch (Exception e) {
            System.out.println("getImagingStudyID error : " + e);
        }
        return "";
    }

    private String getImagingStudyID(String acsn, int maxRetries) {
        return getImagingStudyID(acsn, maxRetries, -1);
    }

    private String getImagingStudyID(String acsn, int maxRetries, int row) {
        String imagingId = "";
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            // Check local database for webhook update (SUCCESS or FAILED)
            try {
                String sql = "SELECT id_imaging, status_webhook, message_webhook FROM satu_sehat_imagingstudy_radiologi WHERE acsn = ?";
                try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
                    ps.setString(1, acsn);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            String status = rs.getString("status_webhook");
                            String message = rs.getString("message_webhook");
                            imagingId = rs.getString("id_imaging");

                            if ("SUCCESS".equals(status)) {
                                System.out.println("Satu Sehat : Webhook success detected for ACSN=" + acsn);
                                return (imagingId == null) ? "" : imagingId;
                            } else if ("FAILED".equals(status)) {
                                System.out.println("Satu Sehat : Webhook failure detected for ACSN=" + acsn + " Msg: " + message);
                                if (row >= 0) {
                                    final String finalMsg = (message != null && !message.isEmpty()) ? "Gagal: " + message : "Gagal Kirim";
                                    SwingUtilities.invokeLater(() -> tbObat.setValueAt(finalMsg, row, COL_STATUS_ORTHANC));
                                }
                                return "FAILED_STATUS";
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("Error polling webhook status locally: " + e);
            }

            if (attempt < maxRetries) {
                int sleepSec = 1 + (attempt * 2); // 3s, 5s, 7s...
                System.out.println("Satu Sehat : Waiting " + sleepSec + " seconds before query attempt " + attempt + "/" + maxRetries + " for ACSN=" + acsn);
                try { Thread.sleep(sleepSec * 1000); } catch (InterruptedException ignored) {}
            }
        }

        // Final fallback: if local polling yields nothing (pending), query external SatuSehat API
        System.out.println("Satu Sehat : Local polling finished. Executing fallback external API call for ACSN=" + acsn);
        return getImagingStudyID(acsn);
    }

    /**
     * Upserts an ImagingStudy record in the local database.
     *
     * @param noorder order number
     * @param kdJenisPrw procedure code
     * @param idServiceRequest Satu Sehat service request ID
     * @param acsn AccessionNumber
     * @param idImaging Satu Sehat ImagingStudy ID
     */
    private void simpanImagingStudy(String noorder, String kdJenisPrw,
            String idServiceRequest, String acsn, String idImaging) {
        try {
            String sqlCheck = "SELECT id_imaging FROM satu_sehat_imagingstudy_radiologi "
                    + "WHERE noorder = ? AND kd_jenis_prw = ?";
            try (PreparedStatement psCheck = koneksi.prepareStatement(sqlCheck)) {
                psCheck.setString(1, noorder);
                psCheck.setString(2, kdJenisPrw);
                try (ResultSet rsCheck = psCheck.executeQuery()) {
                    if (rsCheck.next()) {
                        // Update existing record
                        String sqlUpdate = "UPDATE satu_sehat_imagingstudy_radiologi "
                                + "SET id_servicerequest = ?, acsn = ?, id_imaging = ? "
                                + "WHERE noorder = ? AND kd_jenis_prw = ?";
                        try (PreparedStatement psUpdate = koneksi.prepareStatement(sqlUpdate)) {
                            psUpdate.setString(1, idServiceRequest);
                            psUpdate.setString(2, acsn);
                            psUpdate.setString(3, idImaging);
                            psUpdate.setString(4, noorder);
                            psUpdate.setString(5, kdJenisPrw);
                            psUpdate.executeUpdate();
                            System.out.println("Update ImagingStudy record : noorder=" + noorder);
                        }
                    } else {
                        // Insert new record
                        String sqlInsert = "INSERT INTO satu_sehat_imagingstudy_radiologi "
                                + "(noorder, kd_jenis_prw, id_servicerequest, acsn, id_imaging) "
                                + "VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement psInsert = koneksi.prepareStatement(sqlInsert)) {
                            psInsert.setString(1, noorder);
                            psInsert.setString(2, kdJenisPrw);
                            psInsert.setString(3, idServiceRequest);
                            psInsert.setString(4, acsn);
                            psInsert.setString(5, idImaging);
                            psInsert.executeUpdate();
                            System.out.println("Insert ImagingStudy record : noorder=" + noorder);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("simpanImagingStudy error : " + e);
        }
    }

    private String escapeJson(Object val) {
        if (val == null) {
            return "";
        }
        return val.toString().replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * Builds the FHIR ServiceRequest JSON body for POST (new) or PUT (update).
     *
     * @param row table row index
     * @param existingId the existing service request ID for PUT; {@code null}
     * for POST
     * @return JSON string
     */
    private String buildServiceRequestJson(int row, String existingId) {
        String idBlock = existingId != null && !existingId.isEmpty()
                ? "\"id\": \"" + existingId + "\"," : "";
        return "{"
                + "\"resourceType\": \"ServiceRequest\","
                + idBlock
                + "\"identifier\": [{"
                + "\"system\": \"http://sys-ids.kemkes.go.id/acsn/" + koneksiDB.IDSATUSEHAT() + "\","
                + "\"value\": \"" + buildAcsn(
                        tbObat.getValueAt(row, COL_NOORDER).toString(),
                        tbObat.getValueAt(row, COL_KD_JENIS_PRW).toString()) + "\""
                + "}],"
                + "\"status\": \"active\","
                + "\"intent\": \"order\","
                + "\"category\": [{"
                + "\"coding\": [{"
                + "\"system\": \"http://snomed.info/sct\","
                + "\"code\": \"363679005\","
                + "\"display\": \"Imaging\""
                + "}]"
                + "}],"
                + "\"code\": {"
                + "\"coding\": [{"
                + "\"system\": \"" + tbObat.getValueAt(row, COL_RAD_SYSTEM) + "\","
                + "\"code\": \"" + tbObat.getValueAt(row, COL_RAD_CODE) + "\","
                + "\"display\": \"" + tbObat.getValueAt(row, COL_RAD_DISPLAY) + "\""
                + "}],"
                + "\"text\": \"" + escapeJson(tbObat.getValueAt(row, COL_NM_PERAWATAN)) + "\""
                + "},"
                + "\"subject\": {\"reference\": \"Patient/" + idpasien + "\"},"
                + "\"encounter\": {"
                + "\"reference\": \"Encounter/" + tbObat.getValueAt(row, COL_ID_ENCOUNTER) + "\","
                + "\"display\": \"Permintaan " + escapeJson(tbObat.getValueAt(row, COL_NM_PERAWATAN))
                + " atas nama pasien " + escapeJson(tbObat.getValueAt(row, COL_NAMA_PASIEN))
                + " No.RM " + escapeJson(tbObat.getValueAt(row, COL_NO_RM))
                + " No.Rawat " + escapeJson(tbObat.getValueAt(row, COL_NO_RAWAT))
                + ", pada tanggal " + escapeJson(tbObat.getValueAt(row, COL_TGL_JAM)) + "\""
                + "},"
                + "\"authoredOn\": \"" + tbObat.getValueAt(row, COL_TGL_JAM).toString().replaceAll(" ", "T") + "+07:00\","
                + "\"requester\": {"
                + "\"reference\": \"Practitioner/" + iddokter + "\","
                + "\"display\": \"" + escapeJson(tbObat.getValueAt(row, COL_NAMA_DOKTER)) + "\""
                + "},"
                + "\"performer\": [{"
                + "\"reference\": \"Organization/" + koneksiDB.IDSATUSEHAT() + "\","
                + "\"display\": \"Ruang Radiologi/Petugas Radiologi\""
                + "}],"
                + "\"reasonCode\": [{"
                + "\"text\": \"" + escapeJson(tbObat.getValueAt(row, COL_DIAGNOSA)) + "\""
                + "}]"
                + "}";
    }

    /**
     * Loads data into the table from the database. Lokasi Image prefers a live
     * Orthanc match (PatientID + study date + modality); id_imaging (Satu
     * Sehat) does not imply physical storage in Orthanc. Webapps file path
     * comes from {@code gambar_radiologi} joined on hasil (tgl_hasil /
     * jam_hasil).
     */
    private void tampil() {
        Valid.tabelKosong(tabMode);
        Map<String, Boolean> orthancHitCache = new HashMap<>(128);
        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.no_ktp,pasien.tgl_lahir,pasien.jk,reg_periksa.kd_dokter,pegawai.nama,pegawai.no_ktp as ktpdokter,"
                    + "satu_sehat_encounter.id_encounter,permintaan_radiologi.noorder,permintaan_radiologi.tgl_permintaan,permintaan_radiologi.jam_permintaan,permintaan_radiologi.diagnosa_klinis,"
                    + "jns_perawatan_radiologi.nm_perawatan,ifnull(satu_sehat_mapping_radiologi.code,'') as code,ifnull(satu_sehat_mapping_radiologi.system,'') as system,ifnull(satu_sehat_mapping_radiologi.display,'') as display,"
                    + "ifnull(satu_sehat_servicerequest_radiologi.id_servicerequest,'') as id_servicerequest,permintaan_pemeriksaan_radiologi.kd_jenis_prw, "
                    + "ifnull(gambar_radiologi.lokasi_gambar, '') as lokasi_gambar, "
                    + "ifnull(satu_sehat_imagingstudy_radiologi.acsn,'') as acsn, ifnull(satu_sehat_imagingstudy_radiologi.id_imaging,'') as id_imaging, "
                    + "ifnull(poliklinik.nm_poli,'') as nm_poli, ifnull(dokter.nm_dokter,'') as nm_dokter_perujuk "
                    + "from permintaan_radiologi "
                    + "inner join reg_periksa on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "
                    + "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "inner join pegawai on pegawai.nik=reg_periksa.kd_dokter "
                    + "inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat "
                    + "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "
                    + "inner join jns_perawatan_radiologi on jns_perawatan_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw "
                    + "left join satu_sehat_mapping_radiologi on satu_sehat_mapping_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                    + "left join poliklinik on reg_periksa.kd_poli=poliklinik.kd_poli "
                    + "left join dokter on permintaan_radiologi.dokter_perujuk=dokter.kd_dokter "
                    + "left join satu_sehat_servicerequest_radiologi on satu_sehat_servicerequest_radiologi.noorder=permintaan_pemeriksaan_radiologi.noorder and satu_sehat_servicerequest_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw "
                    + "left join periksa_radiologi on periksa_radiologi.no_rawat=permintaan_radiologi.no_rawat and periksa_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw "
                    + "and periksa_radiologi.tgl_periksa=permintaan_radiologi.tgl_hasil "
                    + "left join gambar_radiologi on gambar_radiologi.no_rawat=periksa_radiologi.no_rawat and gambar_radiologi.tgl_periksa=periksa_radiologi.tgl_periksa and gambar_radiologi.jam=periksa_radiologi.jam "
                    + "left join satu_sehat_imagingstudy_radiologi on satu_sehat_imagingstudy_radiologi.noorder=permintaan_pemeriksaan_radiologi.noorder "
                    + "and satu_sehat_imagingstudy_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw "
                    + "where permintaan_radiologi.tgl_permintaan between ? and ? "
                    + (TCari.getText().equals("") ? ""
                    : "and (permintaan_radiologi.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "
                    + "pasien.nm_pasien like ? or pasien.no_ktp like ? or pegawai.nama like ? or "
                    + "jns_perawatan_radiologi.nm_perawatan like ? or "
                    + "satu_sehat_mapping_radiologi.code like ? or permintaan_radiologi.noorder like ?) ")
                    + "group by permintaan_pemeriksaan_radiologi.noorder, permintaan_pemeriksaan_radiologi.kd_jenis_prw");
            try {
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
                if (!TCari.getText().equals("")) {
                    for (int p = 3; p <= 10; p++) {
                        ps.setString(p, "%" + TCari.getText() + "%");
                    }
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    String kdJenisPrw = rs.getString("kd_jenis_prw");
                    String mappedModality = modalityMapper.getModality(kdJenisPrw);
                    String modalityCol = mappedModality != null && !mappedModality.isEmpty() ? mappedModality : "-";

                    String lokasiGambar = rs.getString("lokasi_gambar");
                    if (lokasiGambar == null) {
                        lokasiGambar = "";
                    }
                    lokasiGambar = lokasiGambar.trim();

                    String tglPermRaw = rs.getString("tgl_permintaan");
                    String jamPerm = normalizeJamPermintaan(rs.getString("jam_permintaan"));
                    String tglJamDisplay = (tglPermRaw == null ? "" : tglPermRaw)
                            + (jamPerm.isEmpty() ? "" : (" " + jamPerm));

                    String noRm = rs.getString("no_rkm_medis");
                    String tanggalDic = dicomStudyDateFromYmd(tglPermRaw == null ? "" : tglPermRaw);

                    String lokasiDisplay = "-";
                    if (mappedModality != null && !mappedModality.isEmpty()
                            && orthancStudyExistsCached(noRm, tanggalDic, mappedModality, orthancHitCache)) {
                        lokasiDisplay = "orthanc";
                    } else if (!lokasiGambar.isEmpty()) {
                        lokasiDisplay = "webapps";
                    }

                    String tglLahirVal = rs.getString("tgl_lahir");
                    tglLahirVal = tglLahirVal == null ? "" : tglLahirVal.replaceAll("-", "");
                    String jkVal = rs.getString("jk");
                    String jkDisplay = "L".equals(jkVal) ? "M" : "F";

                    tabMode.addRow(new Object[]{
                        false,
                        rs.getString("no_rawat"),
                        noRm,
                        rs.getString("nm_pasien"),
                        rs.getString("no_ktp"),
                        rs.getString("kd_dokter"),
                        rs.getString("nama"),
                        rs.getString("ktpdokter"),
                        rs.getString("id_encounter"),
                        rs.getString("noorder"),
                        tglJamDisplay,
                        rs.getString("diagnosa_klinis"),
                        rs.getString("nm_perawatan"),
                        rs.getString("code"),
                        rs.getString("system"),
                        rs.getString("display"),
                        rs.getString("id_servicerequest"),
                        kdJenisPrw,
                        lokasiDisplay,
                        rs.getString("acsn"),
                        rs.getString("id_imaging"),
                        modalityCol,
                        "",
                        lokasiGambar,
                        tglLahirVal,
                        jkDisplay,
                        rs.getString("nm_poli"),
                        rs.getString("nm_dokter_perujuk"),
                        tglPermRaw == null ? "" : tglPermRaw,
                        jamPerm
                    });
                }
            } catch (Exception e) {
                System.out.println("tampil query error : " + e);
            } finally {
                if (rs != null) try {
                    rs.close();
                } catch (Exception ignored) {
                }
                if (ps != null) try {
                    ps.close();
                } catch (Exception ignored) {
                }
            }
        } catch (Exception e) {
            System.out.println("tampil error : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    // =========================================================================
    // Background task runner
    // =========================================================================
    private void runBackground(Runnable task) {
        if (ceksukses) {
            return;
        }
        if (executor.isShutdown() || executor.isTerminated()) {
            return;
        }
        if (!isDisplayable()) {
            return;
        }

        ceksukses = true;
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        try {
            executor.submit(() -> {
                try {
                    task.run();
                } finally {
                    ceksukses = false;
                    SwingUtilities.invokeLater(() -> {
                        if (isDisplayable()) {
                            setCursor(Cursor.getDefaultCursor());
                        }
                    });
                }
            });
        } catch (RejectedExecutionException ex) {
            ceksukses = false;
        }
    }

    // =========================================================================
    // Lifecycle
    // =========================================================================
    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SatuSehatKirimServiceRequestRadiologi dialog
                    = new SatuSehatKirimServiceRequestRadiologi(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // =========================================================================
    // Variables declaration (NetBeans GEN-BEGIN — do not modify)
    // =========================================================================

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnAll;
    private widget.Button BtnCari;
    private widget.Button BtnGetIDImagingStudiAuto;
    private widget.Button BtnGetIDImagingStudiManual;
    private widget.Button BtnKeluar;
    private widget.Button BtnKirim;
    private widget.Button BtnPrint;
    private widget.Button BtnUpdate;
    private widget.Button BtnKirimOrthanc;
    private widget.Button BtnUpdateACSNOrthanc;
    private widget.Button BtnKirimDICOMRouter;
    private widget.Button BtnUpdateDanKirim;
    private widget.Tanggal DTPCari1;
    private widget.Tanggal DTPCari2;
    private widget.Label LCount;
    private widget.editorpane LoadHTML;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel7;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPopupMenu jPopupMenu1;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private javax.swing.JMenuItem ppBersihkan;
    private javax.swing.JMenuItem ppPilihBelumTerkirim;
    private javax.swing.JMenuItem ppPilihBelumTerkirim1;
    private javax.swing.JMenuItem ppPilihSemua;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JMenuItem ppPilihLokasiWebapps;
    private javax.swing.JMenuItem ppPilihLokasiOrthanc;
}
