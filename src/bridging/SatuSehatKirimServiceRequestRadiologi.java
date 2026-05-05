/*
  by Mas Elkhanza
 */
package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
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
    private static final int COL_KD_JENIS_PRW = 17; // hidden column
    private static final int COL_ACSN = 18;
    private static final int COL_ID_IMAGING = 19;
    private static final int COL_MODALITY = 20;
    private static final int COL_STATUS_ORTHANC = 21;
    private static final int TOTAL_COLUMNS = 22;

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------
    private final DefaultTableModel tabMode;
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
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
            "ID Service Request", "Kode Pemeriksaan",
            "ASCN", "ID Imaging Study", "Modality", "Status Orthanc"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return colIndex == COL_PILIH;
            }

            final Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class
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
        int[] widths = {
            20, // 0  Pilih
            105, // 1  No.Rawat
            70, // 2  No.RM
            150, // 3  Nama Pasien
            110, // 4  No.KTP Pasien
            80, // 5  Kode Dokter
            150, // 6  Nama Dokter Perujuk
            110, // 7  No.KTP Dokter
            210, // 8  ID Encounter
            110, // 9  No.Permintaan
            120, // 10 Tgl & Jam Permintaan
            150, // 11 Diagnosa Klinis
            150, // 12 Nama Pemeriksaan
            150, // 13 Radiologi Code
            150, // 14 Radiologi System
            150, // 15 Radiologi Display
            210, // 16 ID Service Request
            0, // 17 Kode Pemeriksaan (hidden)
            150, // 18 ACSN
            210, // 19 ID Imaging Study
            70, // 20 Modality
            150 // 21 Status Orthanc
        };
        for (int col = 0; col < TOTAL_COLUMNS; col++) {
            TableColumn column = tbObat.getColumnModel().getColumn(col);
            if (widths[col] == 0) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else {
                column.setPreferredWidth(widths[col]);
            }
        }

        tbObat.setDefaultRenderer(Object.class, new WarnaTable());
        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));

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
        for (i = 0; i < tbObat.getRowCount(); i++) {
            if (tbObat.getValueAt(i, COL_PILIH).toString().equals("true")
                    && !tbObat.getValueAt(i, COL_KTP_PASIEN).toString().equals("")
                    && !tbObat.getValueAt(i, COL_KTP_DOKTER).toString().equals("")
                    && tbObat.getValueAt(i, COL_ID_SR).toString().equals("")) {
                try {
                    iddokter = cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(i, COL_KTP_DOKTER).toString());
                    idpasien = cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(i, COL_KTP_PASIEN).toString());
                    try {
                        headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
                        json = buildServiceRequestJson(i, null);
                        System.out.println("URL Kirim : " + link + "/ServiceRequest");
                        System.out.println("Request JSON Kirim : " + json);
                        requestEntity = new HttpEntity(json, headers);
                        json = api.getRest().exchange(link + "/ServiceRequest", HttpMethod.POST, requestEntity, String.class).getBody();
                        System.out.println("Result JSON Kirim : " + json);
                        root = mapper.readTree(json);
                        response = root.path("id");
                        if (!response.asText().equals("")) {
                            if (Sequel.menyimpantf2("satu_sehat_servicerequest_radiologi", "?,?,?", "No.Order", 3,
                                    new String[]{tbObat.getValueAt(i, COL_NOORDER).toString(),
                                        tbObat.getValueAt(i, COL_KD_JENIS_PRW).toString(),
                                        response.asText()}) == true) {
                                tbObat.setValueAt(response.asText(), i, COL_ID_SR);
                                tbObat.setValueAt(false, i, COL_PILIH);
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
    }//GEN-LAST:event_BtnKirimActionPerformed

    private void BtnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateActionPerformed
        for (i = 0; i < tbObat.getRowCount(); i++) {
            if (tbObat.getValueAt(i, COL_PILIH).toString().equals("true")
                    && !tbObat.getValueAt(i, COL_KTP_PASIEN).toString().equals("")
                    && !tbObat.getValueAt(i, COL_KTP_DOKTER).toString().equals("")
                    && !tbObat.getValueAt(i, COL_ID_SR).toString().equals("")) {
                try {
                    iddokter = cekViaSatuSehat.tampilIDParktisi(tbObat.getValueAt(i, COL_KTP_DOKTER).toString());
                    idpasien = cekViaSatuSehat.tampilIDPasien(tbObat.getValueAt(i, COL_KTP_PASIEN).toString());
                    try {
                        headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
                        json = buildServiceRequestJson(i, tbObat.getValueAt(i, COL_ID_SR).toString());
                        String url = link + "/ServiceRequest/" + tbObat.getValueAt(i, COL_ID_SR);
                        System.out.println("URL Update : " + url);
                        System.out.println("Request JSON Update : " + json);
                        requestEntity = new HttpEntity(json, headers);
                        json = api.getRest().exchange(url, HttpMethod.PUT, requestEntity, String.class).getBody();
                        System.out.println("Result JSON Update : " + json);
                        tbObat.setValueAt(false, i, COL_PILIH);
                    } catch (Exception e) {
                        System.out.println("Notifikasi Bridging Update : " + e);
                    }
                } catch (Exception e) {
                    System.out.println("Notifikasi Update : " + e);
                }
            }
        }
    }//GEN-LAST:event_BtnUpdateActionPerformed

    // =========================================================================
    // Imaging Study ID (Satu Sehat)
    // =========================================================================

    private void BtnGetIDImagingStudiAutoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGetIDImagingStudiAutoActionPerformed
        for (int i = 0; i < tbObat.getRowCount(); i++) {
            if (!Boolean.parseBoolean(tbObat.getValueAt(i, COL_PILIH).toString())) {
                continue;
            }

            String noorder = tbObat.getValueAt(i, COL_NOORDER).toString();
            String kdJenisPrw = tbObat.getValueAt(i, COL_KD_JENIS_PRW).toString();
            String idServicerequest = tbObat.getValueAt(i, COL_ID_SR).toString();
            String acsn = buildAcsn(noorder, kdJenisPrw);

            System.out.println("Auto ACSN baris " + i + " : " + acsn);
            String imagingId = getImagingStudyID(acsn);
            if (imagingId != null && !imagingId.isEmpty()) {
                tbObat.setValueAt(acsn, i, COL_ACSN);
                tbObat.setValueAt(imagingId, i, COL_ID_IMAGING);
                tbObat.setValueAt(false, i, COL_PILIH);
                simpanImagingStudy(noorder, kdJenisPrw, idServicerequest, acsn, imagingId);
                System.out.println("ImagingStudy ID untuk noorder " + noorder + " : " + imagingId);
            } else {
                System.out.println("Gagal mendapatkan ImagingStudy ID untuk ACSN " + acsn);
            }
        }
    }//GEN-LAST:event_BtnGetIDImagingStudiAutoActionPerformed

    private void BtnGetIDImagingStudiManualActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnGetIDImagingStudiManualActionPerformed
        for (int i = 0; i < tbObat.getRowCount(); i++) {
            if (!tbObat.getValueAt(i, COL_PILIH).toString().equals("true")) {
                continue;
            }

            String noorder = tbObat.getValueAt(i, COL_NOORDER).toString();
            String kdJenisPrw = tbObat.getValueAt(i, COL_KD_JENIS_PRW).toString();
            String idServicerequest = tbObat.getValueAt(i, COL_ID_SR).toString();

            String acsn = JOptionPane.showInputDialog(this,
                    "Masukkan ACSN untuk No.Order " + noorder + " :",
                    "Manual ACSN", JOptionPane.QUESTION_MESSAGE);
            if (acsn == null || acsn.trim().isEmpty()) {
                System.out.println("Input ACSN dibatalkan untuk baris " + i);
                continue;
            }
            acsn = acsn.trim();
            System.out.println("Manual ACSN baris " + i + " : " + acsn);

            String imagingId = getImagingStudyID(acsn);
            if (imagingId != null && !imagingId.isEmpty()) {
                tbObat.setValueAt(acsn, i, COL_ACSN);
                tbObat.setValueAt(imagingId, i, COL_ID_IMAGING);
                tbObat.setValueAt(false, i, COL_PILIH);
                simpanImagingStudy(noorder, kdJenisPrw, idServicerequest, acsn, imagingId);
                System.out.println("Berhasil ImagingStudy ID untuk noorder " + noorder + " : " + imagingId);
            } else {
                System.out.println("Gagal mendapatkan ImagingStudy ID untuk ACSN " + acsn);
            }
        }
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
    private void BtnUpdateACSNOrthancActionPerformed(java.awt.event.ActionEvent evt) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int sukses = 0, gagal = 0;

        for (int i = 0; i < tbObat.getRowCount(); i++) {
            if (!tbObat.getValueAt(i, COL_PILIH).toString().equals("true")) {
                continue;
            }

            String noorder = tbObat.getValueAt(i, COL_NOORDER).toString();
            String kdJenisPrw = tbObat.getValueAt(i, COL_KD_JENIS_PRW).toString();
            String acsn = buildAcsn(noorder, kdJenisPrw);

            try {
                String orthancStudyId = resolveOrthancStudyId(i);
                if (orthancStudyId.isEmpty()) {
                    tbObat.setValueAt("Study Tidak Ditemukan", i, COL_STATUS_ORTHANC);
                    gagal++;
                    continue;
                }

                if (orthanc.UbahAccession(orthancStudyId, acsn, true)) {
                    tbObat.setValueAt(acsn, i, COL_ACSN);
                    tbObat.setValueAt("ACSN Updated", i, COL_STATUS_ORTHANC);
                    tbObat.setValueAt(false, i, COL_PILIH);
                    sukses++;
                    System.out.println("Orthanc ACSN updated : noorder=" + noorder + " → " + acsn);
                } else {
                    tbObat.setValueAt("Gagal Update ACSN", i, COL_STATUS_ORTHANC);
                    gagal++;
                }
            } catch (Exception e) {
                tbObat.setValueAt("Error", i, COL_STATUS_ORTHANC);
                gagal++;
                System.out.println("BtnUpdateACSNOrthanc error baris " + i + " : " + e);
            }
        }

        this.setCursor(Cursor.getDefaultCursor());
        JOptionPane.showMessageDialog(this,
                "Update ACSN Orthanc selesai.\nBerhasil: " + sukses + "   Gagal/Skip: " + gagal,
                "Update ACSN Orthanc", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Button: Kirim ke DICOM Router. For each checked row: 1. Read the ACSN
     * (compute if blank) 2. Look up the study in Orthanc by that ACSN 3. Send
     * the study to the DICOM router Shows a single summary dialog after
     * processing all rows.
     */
    private void BtnKirimDICOMRouterActionPerformed(java.awt.event.ActionEvent evt) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int sukses = 0, gagal = 0;

        for (int i = 0; i < tbObat.getRowCount(); i++) {
            if (!tbObat.getValueAt(i, COL_PILIH).toString().equals("true")) {
                continue;
            }

            String noorder = tbObat.getValueAt(i, COL_NOORDER).toString();
            String kdJenisPrw = tbObat.getValueAt(i, COL_KD_JENIS_PRW).toString();
            String acsn = tbObat.getValueAt(i, COL_ACSN).toString().trim();
            if (acsn.isEmpty()) {
                acsn = buildAcsn(noorder, kdJenisPrw);
            }

            try {
                String orthancStudyId = orthanc.findStudyByAccession(acsn);
                if (orthancStudyId.isEmpty()) {
                    tbObat.setValueAt("Study Tidak Ditemukan", i, COL_STATUS_ORTHANC);
                    gagal++;
                    System.out.println("Orthanc Skip : study tidak ditemukan untuk ACSN=" + acsn);
                    continue;
                }

                if (orthanc.kirimKeModality(orthancStudyId, true)) {
                    tbObat.setValueAt("Terkirim ke Router", i, COL_STATUS_ORTHANC);
                    tbObat.setValueAt(false, i, COL_PILIH);
                    sukses++;
                    System.out.println("Orthanc : Study " + orthancStudyId + " dikirim ke DICOM Router");
                } else {
                    tbObat.setValueAt("Gagal Kirim DICOM", i, COL_STATUS_ORTHANC);
                    gagal++;
                }
            } catch (Exception e) {
                tbObat.setValueAt("Error", i, COL_STATUS_ORTHANC);
                gagal++;
                System.out.println("BtnKirimDICOMRouter error baris " + i + " : " + e);
            }
        }

        this.setCursor(Cursor.getDefaultCursor());
        JOptionPane.showMessageDialog(this,
                "Kirim DICOM Router selesai.\nBerhasil: " + sukses + "   Gagal/Skip: " + gagal,
                "Kirim ke DICOM Router", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Button: Update ACSN &amp; Kirim DICOM (automated combined workflow).
     *
     * <p>
     * For each checked row, executes the full automated pipeline:
     * <ol>
     * <li>Compute ACSN from noorder + kd_jenis_prw.</li>
     * <li>Resolve DICOM modality via {@link RadiologyModalityMapper}.</li>
     * <li>Find the matching Orthanc study (filtered by PatientID + date +
     * modality).</li>
     * <li>Update the AccessionNumber in Orthanc (KeepSource=false → new study
     * ID).</li>
     * <li>Re-find the study by the new ACSN to get the fresh Orthanc study
     * ID.</li>
     * <li>Send the study to the DICOM router.</li>
     * </ol>
     * Steps 4–5 handle the Orthanc behaviour where modifying with
     * KeepSource=false creates a new study with a different internal ID. Shows
     * a single summary dialog after processing all rows.
     */
    private void BtnUpdateDanKirimActionPerformed(java.awt.event.ActionEvent evt) {
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        int sukses = 0, gagal = 0;

        for (int i = 0; i < tbObat.getRowCount(); i++) {
            if (!tbObat.getValueAt(i, COL_PILIH).toString().equals("true")) {
                continue;
            }

            String noorder = tbObat.getValueAt(i, COL_NOORDER).toString();
            String kdJenisPrw = tbObat.getValueAt(i, COL_KD_JENIS_PRW).toString();
            String acsn = buildAcsn(noorder, kdJenisPrw);

            try {
                // Step 1: Find study in Orthanc using strict modality filter
                String orthancStudyId = resolveOrthancStudyId(i);
                if (orthancStudyId.isEmpty()) {
                    tbObat.setValueAt("Study Tidak Ditemukan", i, COL_STATUS_ORTHANC);
                    gagal++;
                    continue;
                }

                // Step 2: Update AccessionNumber (KeepSource=false → new study ID created)
                if (!orthanc.UbahAccession(orthancStudyId, acsn, true)) {
                    tbObat.setValueAt("Gagal Update ACSN", i, COL_STATUS_ORTHANC);
                    gagal++;
                    System.out.println("Orthanc : Gagal update ACSN, skip kirim DICOM. noorder=" + noorder);
                    continue;
                }
                tbObat.setValueAt(acsn, i, COL_ACSN);
                System.out.println("Orthanc : ACSN updated → " + acsn);

                // Step 3: Re-find the study by ACSN to get the new Orthanc study ID
                String newStudyId = orthanc.findStudyByAccession(acsn);
                if (newStudyId.isEmpty()) {
                    tbObat.setValueAt("Study Baru Tidak Ditemukan", i, COL_STATUS_ORTHANC);
                    gagal++;
                    System.out.println("Orthanc : Study baru tidak ditemukan setelah update ACSN=" + acsn);
                    continue;
                }

                // Step 4: Send the updated study to the DICOM router
                if (orthanc.kirimKeModality(newStudyId, true)) {
                    tbObat.setValueAt("Terkirim ke Router", i, COL_STATUS_ORTHANC);
                    tbObat.setValueAt(false, i, COL_PILIH);
                    sukses++;
                    System.out.println("Orthanc : Study " + newStudyId + " dikirim ke DICOM Router");
                } else {
                    tbObat.setValueAt("Gagal Kirim DICOM", i, COL_STATUS_ORTHANC);
                    gagal++;
                }
            } catch (Exception e) {
                tbObat.setValueAt("Error", i, COL_STATUS_ORTHANC);
                gagal++;
                System.out.println("BtnUpdateDanKirim error baris " + i + " : " + e);
            }
        }

        this.setCursor(Cursor.getDefaultCursor());
        JOptionPane.showMessageDialog(this,
                "Update ACSN & Kirim DICOM selesai.\nBerhasil: " + sukses + "   Gagal/Skip: " + gagal,
                "Update ACSN & Kirim DICOM", JOptionPane.INFORMATION_MESSAGE);
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
            String val = tbObat.getValueAt(i, COL_ID_IMAGING) == null
                    ? "" : tbObat.getValueAt(i, COL_ID_IMAGING).toString().trim();
            tbObat.setValueAt(val.isEmpty() || val.equals("-"), i, COL_PILIH);
        }
    }//GEN-LAST:event_ppPilihBelumTerkirim1ActionPerformed

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
        return noorder.replaceAll("PR", "") + kdJenisPrw;
    }

    /**
     * Resolves the Orthanc internal study ID for a given table row. Uses a
     * strict modality-filtered search (PatientID + date + ModalitiesInStudy) to
     * prevent cross-matching when a patient has multiple modality exams on the
     * same day.
     *
     * @param row table row index
     * @return Orthanc internal study ID, or empty string if not found / no
     * mapping
     */
    private String resolveOrthancStudyId(int row) {
        String noRM = tbObat.getValueAt(row, COL_NO_RM).toString().trim();
        String kdJenisPrw = tbObat.getValueAt(row, COL_KD_JENIS_PRW).toString().trim();
        String tglPermintaan = tbObat.getValueAt(row, COL_TGL_JAM).toString().trim();

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
        String tanggal = tglPermintaan.substring(0, 10).replaceAll("-", "");

        // Query Orthanc with modality filter
        JsonNode studies = orthanc.AmbilSeriesDenganModality(noRM, tanggal, tanggal, modality);
        if (studies == null || !studies.isArray() || studies.size() == 0) {
            System.out.println("Orthanc Skip : Tidak ditemukan study untuk RM=" + noRM
                    + ", Tanggal=" + tanggal + ", Modality=" + modality);
            return "";
        }

        String orthancStudyId = studies.get(0).path("ID").asText();
        System.out.println("Orthanc : Ditemukan study " + orthancStudyId
                + " untuk RM=" + noRM + ", Modality=" + modality);
        return orthancStudyId;
    }

    /**
     * Fetches the Satu Sehat ImagingStudy resource ID matching the given ACSN.
     *
     * @param acsn the AccessionNumber to search for
     * @return the ImagingStudy ID, or empty string if not found
     */
    private String getImagingStudyID(String acsn) {
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
                + "\"text\": \"" + tbObat.getValueAt(row, COL_NM_PERAWATAN) + "\""
                + "},"
                + "\"subject\": {\"reference\": \"Patient/" + idpasien + "\"},"
                + "\"encounter\": {"
                + "\"reference\": \"Encounter/" + tbObat.getValueAt(row, COL_ID_ENCOUNTER) + "\","
                + "\"display\": \"Permintaan " + tbObat.getValueAt(row, COL_NM_PERAWATAN)
                + " atas nama pasien " + tbObat.getValueAt(row, COL_NAMA_PASIEN)
                + " No.RM " + tbObat.getValueAt(row, COL_NO_RM)
                + " No.Rawat " + tbObat.getValueAt(row, COL_NO_RAWAT)
                + ", pada tanggal " + tbObat.getValueAt(row, COL_TGL_JAM) + "\""
                + "},"
                + "\"authoredOn\": \"" + tbObat.getValueAt(row, COL_TGL_JAM).toString().replaceAll(" ", "T") + "+07:00\","
                + "\"requester\": {"
                + "\"reference\": \"Practitioner/" + iddokter + "\","
                + "\"display\": \"" + tbObat.getValueAt(row, COL_NAMA_DOKTER) + "\""
                + "},"
                + "\"performer\": [{"
                + "\"reference\": \"Organization/" + koneksiDB.IDSATUSEHAT() + "\","
                + "\"display\": \"Ruang Radiologi/Petugas Radiologi\""
                + "}],"
                + "\"reasonCode\": [{"
                + "\"text\": \"" + tbObat.getValueAt(row, COL_DIAGNOSA) + "\""
                + "}]"
                + "}";
    }

    /**
     * Loads data into the table from the database. The "Modality" column (20)
     * is resolved from the local mapping file. "Status Orthanc" column (21) is
     * initialised to empty — it is populated at runtime by the Orthanc workflow
     * buttons.
     */
    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            ps = koneksi.prepareStatement(
                    "select reg_periksa.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,pasien.no_ktp,reg_periksa.kd_dokter,pegawai.nama,pegawai.no_ktp as ktpdokter,"
                    + "satu_sehat_encounter.id_encounter,permintaan_radiologi.noorder,permintaan_radiologi.tgl_permintaan,permintaan_radiologi.jam_permintaan,permintaan_radiologi.diagnosa_klinis,"
                    + "jns_perawatan_radiologi.nm_perawatan,satu_sehat_mapping_radiologi.code,satu_sehat_mapping_radiologi.system,satu_sehat_mapping_radiologi.display,"
                    + "ifnull(satu_sehat_servicerequest_radiologi.id_servicerequest,'') as id_servicerequest,permintaan_pemeriksaan_radiologi.kd_jenis_prw, "
                    + "ifnull(satu_sehat_imagingstudy_radiologi.acsn,'') as acsn, ifnull(satu_sehat_imagingstudy_radiologi.id_imaging,'') as id_imaging "
                    + "from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis "
                    + "inner join pegawai on pegawai.nik=reg_periksa.kd_dokter "
                    + "inner join satu_sehat_encounter on satu_sehat_encounter.no_rawat=reg_periksa.no_rawat "
                    + "inner join permintaan_radiologi on permintaan_radiologi.no_rawat=reg_periksa.no_rawat "
                    + "inner join permintaan_pemeriksaan_radiologi on permintaan_pemeriksaan_radiologi.noorder=permintaan_radiologi.noorder "
                    + "inner join jns_perawatan_radiologi on jns_perawatan_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw "
                    + "inner join satu_sehat_mapping_radiologi on satu_sehat_mapping_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw "
                    + "left join satu_sehat_servicerequest_radiologi on satu_sehat_servicerequest_radiologi.noorder=permintaan_pemeriksaan_radiologi.noorder "
                    + "left join satu_sehat_imagingstudy_radiologi on satu_sehat_imagingstudy_radiologi.noorder=permintaan_pemeriksaan_radiologi.noorder "
                    + "and satu_sehat_servicerequest_radiologi.kd_jenis_prw=permintaan_pemeriksaan_radiologi.kd_jenis_prw "
                    + "where reg_periksa.tgl_registrasi between ? and ? "
                    + (TCari.getText().equals("") ? ""
                    : "and (reg_periksa.no_rawat like ? or reg_periksa.no_rkm_medis like ? or "
                    + "pasien.nm_pasien like ? or pasien.no_ktp like ? or pegawai.nama like ? or "
                    + "jns_perawatan_radiologi.nm_perawatan like ? or "
                    + "satu_sehat_mapping_radiologi.code like ? or permintaan_radiologi.noorder like ?)"));
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
                    String modality = modalityMapper.getModality(kdJenisPrw);
                    tabMode.addRow(new Object[]{
                        false,
                        rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"),
                        rs.getString("no_ktp"),
                        rs.getString("kd_dokter"),
                        rs.getString("nama"),
                        rs.getString("ktpdokter"),
                        rs.getString("id_encounter"),
                        rs.getString("noorder"),
                        rs.getString("tgl_permintaan") + " " + rs.getString("jam_permintaan"),
                        rs.getString("diagnosa_klinis"),
                        rs.getString("nm_perawatan"),
                        rs.getString("code"),
                        rs.getString("system"),
                        rs.getString("display"),
                        rs.getString("id_servicerequest"),
                        kdJenisPrw,
                        rs.getString("acsn"),
                        rs.getString("id_imaging"),
                        modality != null ? modality : "-", // col 20: Modality
                        "" // col 21: Status Orthanc (runtime)
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
}
