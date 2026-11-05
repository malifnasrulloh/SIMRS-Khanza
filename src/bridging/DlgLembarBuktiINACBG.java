package bridging;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import fungsi.WarnaTable;
import fungsi.akses;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import kepegawaian.DlgCariDokter;
import laporan.DlgCariICD9;
import laporan.DlgCariPenyakit;
import widget.Button;
import widget.CekBox;
import widget.ComboBox;
import widget.InternalFrame;
import widget.Label;
import widget.PanelBiasa;
import widget.ScrollPane;
import widget.Table;
import widget.Tanggal;
import widget.TextBox;
import widget.panelisi;

/**
 * Dialog Form Lembar Bukti Pelayanan Variabel INACBG
 * Mendukung Multi-Model Cetakan:
 * - Model 1: Format Standar 2 Halaman (Tabel Bordered Grid)
 * - Model 2: Format Form INA 1 Halaman (Presisi 100% Acuan FORM INA.pdf)
 * Terintegrasi penuh dengan Root Window SIMRS Khanza
 */
public class DlgLembarBuktiINACBG extends javax.swing.JDialog {
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final DecimalFormat df = new DecimalFormat("#,##0");
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    // Dialog Master Pickers
    private DlgCariDokter dlgDokter = null;
    private DlgCariPenyakit dlgPenyakit = null;
    private DlgCariICD9 dlgICD9 = null;

    private TextBox targetKdDiag = null;
    private TextBox targetNmDiag = null;
    private Button targetBtnDiag = null;

    private TextBox targetKdTindakan = null;
    private TextBox targetNmTindakan = null;
    private Button targetBtnTindakan = null;

    // UI Containers
    private InternalFrame internalFrame;
    private JTabbedPane tabPane;
    private JPanel panelInput, panelPreview, panelData;
    private ScrollPane scrollInput, scrollPreview, scrollData;
    private javax.swing.JPanel panelPdfPreview;
    private javax.swing.JLabel lblPdfPage;
    private java.util.List<java.awt.image.BufferedImage> pdfPreviewPages = new java.util.ArrayList<>();

    // Pilihan Model Cetakan
    private ComboBox cmbModelCetakTab1;
    private ComboBox cmbModelCetakTab2;

    // Table Tab 3
    private Table tbData;
    private DefaultTableModel tabMode;
    private Tanggal DTPCari1, DTPCari2;
    private TextBox TCari;
    private Button BtnCari, BtnAll, BtnMuat, BtnCetakTabel, BtnHapusTabel, BtnKeluarTabel;
    private Label LCount;

    // Buttons Tab 1
    private Button btnSimpan, btnHapus, btnBaru, btnPreview, btnCetakTab1, btnKeluar;

    // Buttons Tab 2
    private Button btnKembali, btnRefresh, btnGeneratePDF;

    // Administrative Inputs
    private TextBox txtNoRawat, txtNoRM, txtNmPasien, txtJK, txtTglLahir;
    private TextBox txtNoPeserta, txtNoSEP, txtJenisPerawatan, txtKelas;
    private TextBox txtTglMasuk, txtTglKeluar;
    private ComboBox cmbCaraPulang;
    private TextBox txtKdDokter, txtNmDokter, txtSIPDokter;
    private Button btnDokter;
    private TextBox txtBeratLahir, txtTotalTarif, txtSkorADL;
    private TextBox txtPenandatanganPasien;

    // Spesial Prosedur Checkboxes
    private CekBox cbPCL, cbColangiography, cbRepairSeptal, cbTimekotomy, cbKeratoplasty;
    private CekBox cbTorakotomi, cbTumorPineal, cbVitrectomy, cbPhacoemulsification;
    private CekBox cbMicrolaringoscopi, cbHipKnee, cbAirPlumbage, cbLobektomi;
    private CekBox cbStereotactic, cbPancreatectomy;

    // Spesial Drug Checkboxes
    private CekBox cbStreptokinase, cbDeferifrone, cbDeferoksamin, cbDeferasirox, cbHumanAlbumin;

    // Spesial Investigation Checkboxes
    private CekBox cbOtherCTScan, cbNuclearMedicine, cbMRI, cbDiagnosticEye;

    // Spesial Protheis Checkboxes
    private CekBox cbSubduratGrid, cbCoteGraft, cbLiquidEmbolic, cbHipImplant;

    // Diagnosa Fields & Buttons
    private TextBox txtKdDiagPrimer, txtNmDiagPrimer;
    private Button btnDiagPrimer;
    private TextBox txtKdDiagSekunder1, txtNmDiagSekunder1;
    private Button btnDiagSekunder1;
    private TextBox txtKdDiagSekunder2, txtNmDiagSekunder2;
    private Button btnDiagSekunder2;
    private TextBox txtKdDiagSekunder3, txtNmDiagSekunder3;
    private Button btnDiagSekunder3;
    private TextBox txtKdDiagSekunder4, txtNmDiagSekunder4;
    private Button btnDiagSekunder4;
    private TextBox txtKdDiagSekunder5, txtNmDiagSekunder5;
    private Button btnDiagSekunder5;

    // Tindakan Fields & Buttons
    private TextBox txtKdTindakanPrimer, txtNmTindakanPrimer;
    private Button btnTindakanPrimer;
    private TextBox txtKdTindakanSekunder1, txtNmTindakanSekunder1;
    private Button btnTindakanSekunder1;
    private TextBox txtKdTindakanSekunder2, txtNmTindakanSekunder2;
    private Button btnTindakanSekunder2;
    private TextBox txtKdTindakanSekunder3, txtNmTindakanSekunder3;
    private Button btnTindakanSekunder3;
    private TextBox txtKdTindakanSekunder4, txtNmTindakanSekunder4;
    private Button btnTindakanSekunder4;
    private TextBox txtKdTindakanSekunder5, txtNmTindakanSekunder5;
    private Button btnTindakanSekunder5;

    // Penunjang Fields (Primer + Sekunder 1 s/d 5)
    private TextBox txtKdPenunjangPrimer, txtNmPenunjangPrimer;
    private TextBox txtKdPenunjangSekunder1, txtNmPenunjangSekunder1;
    private TextBox txtKdPenunjangSekunder2, txtNmPenunjangSekunder2;
    private TextBox txtKdPenunjangSekunder3, txtNmPenunjangSekunder3;
    private TextBox txtKdPenunjangSekunder4, txtNmPenunjangSekunder4;
    private TextBox txtKdPenunjangSekunder5, txtNmPenunjangSekunder5;

    private static final Font FONT_REGULAR = new Font("Tahoma", Font.PLAIN, 11);
    private static final Font FONT_BOLD = new Font("Tahoma", Font.BOLD, 11);
    private static final Font FONT_TITLE = new Font("Tahoma", Font.BOLD, 12);

    static {
        // Cegah PDFBox memanggil Class.forName("sun.java2d.cmm.kcms.KcmsServiceProvider")
        // yang memicu IllegalAccessError pada JDK 9+ (module java.desktop tidak mengekspor
        // paket sun.java2d.cmm). Dengan properti ini, PDFRenderer melewati pemanggilan itu.
        try {
            if (System.getProperty("sun.java2d.cmm") == null) {
                System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Konstruktor untuk kepemilikan Frame utama
     */
    public DlgLembarBuktiINACBG(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /**
     * Konstruktor untuk kepemilikan Dialog pemanggil (menyatukan window dengan root)
     */
    public DlgLembarBuktiINACBG(java.awt.Dialog parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setSize(1040, 700);
        setLocationRelativeTo(null);

        internalFrame = new InternalFrame();
        internalFrame.setBorder(javax.swing.BorderFactory.createTitledBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
            "::[ Lembar Bukti Pelayanan Variabel INACBG ]::",
            javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
            javax.swing.border.TitledBorder.DEFAULT_POSITION,
            FONT_REGULAR,
            new java.awt.Color(50, 50, 50)
        ));
        internalFrame.setLayout(new BorderLayout());
        getContentPane().add(internalFrame);

        tabPane = new JTabbedPane();
        tabPane.setFont(FONT_REGULAR);

        // ====================================================================
        // TAB 1: INPUT VARIABEL INA
        // ====================================================================
        panelInput = new JPanel();
        panelInput.setLayout(new BoxLayout(panelInput, BoxLayout.Y_AXIS));
        panelInput.setBackground(new Color(250, 255, 250));

        initInputFields();

        scrollInput = new ScrollPane();
        scrollInput.setViewportView(panelInput);
        scrollInput.getVerticalScrollBar().setUnitIncrement(16);

        JPanel panelTab1 = new JPanel(new BorderLayout());
        panelTab1.add(scrollInput, BorderLayout.CENTER);

        // Bilah Tombol Aksi Tab 1
        panelisi panelTombolTab1 = new panelisi();
        panelTombolTab1.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        Label lblModel1 = new Label();
        lblModel1.setText("Model Cetakan :");
        lblModel1.setFont(FONT_REGULAR);

        cmbModelCetakTab1 = new ComboBox();
        cmbModelCetakTab1.setFont(FONT_REGULAR);
        cmbModelCetakTab1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "Model 2 (Form INA 1 Halaman)", "Model 1 (Format Standar 2 Halaman)"
        }));
        cmbModelCetakTab1.setPreferredSize(new Dimension(215, 30));
        cmbModelCetakTab1.addActionListener(e -> {
            if (cmbModelCetakTab2 != null && cmbModelCetakTab2.getSelectedIndex() != cmbModelCetakTab1.getSelectedIndex()) {
                cmbModelCetakTab2.setSelectedIndex(cmbModelCetakTab1.getSelectedIndex());
            }
        });

        btnSimpan = new Button();
        btnSimpan.setIcon(getSafeIcon("/picture/save-16x16.png"));
        btnSimpan.setText("Simpan");
        btnSimpan.setFont(FONT_REGULAR);
        btnSimpan.setPreferredSize(new Dimension(95, 30));
        btnSimpan.addActionListener(e -> simpan());

        btnHapus = new Button();
        btnHapus.setIcon(getSafeIcon("/picture/stop_f2.png"));
        btnHapus.setText("Hapus");
        btnHapus.setFont(FONT_REGULAR);
        btnHapus.setPreferredSize(new Dimension(90, 30));
        btnHapus.addActionListener(e -> hapus());

        btnBaru = new Button();
        btnBaru.setIcon(getSafeIcon("/picture/Cancel-16x16.png"));
        btnBaru.setText("Reset Auto-Fill");
        btnBaru.setFont(FONT_REGULAR);
        btnBaru.setPreferredSize(new Dimension(125, 30));
        btnBaru.addActionListener(e -> autoFill(txtNoRawat.getText().trim()));

        btnPreview = new Button();
        btnPreview.setIcon(getSafeIcon("/picture/Search-16x16.png"));
        btnPreview.setText("Preview");
        btnPreview.setFont(FONT_REGULAR);
        btnPreview.setPreferredSize(new Dimension(100, 30));
        btnPreview.addActionListener(e -> {
            renderHTMLPreview();
            tabPane.setSelectedIndex(1);
        });

        btnCetakTab1 = new Button();
        btnCetakTab1.setIcon(getSafeIcon("/picture/b_print.png"));
        btnCetakTab1.setText("Cetak PDF");
        btnCetakTab1.setFont(FONT_BOLD);
        btnCetakTab1.setPreferredSize(new Dimension(115, 30));
        btnCetakTab1.addActionListener(e -> generatePDF());

        btnKeluar = new Button();
        btnKeluar.setIcon(getSafeIcon("/picture/exit.png"));
        btnKeluar.setText("Keluar");
        btnKeluar.setFont(FONT_REGULAR);
        btnKeluar.setPreferredSize(new Dimension(90, 30));
        btnKeluar.addActionListener(e -> dispose());

        panelTombolTab1.add(lblModel1);
        panelTombolTab1.add(cmbModelCetakTab1);
        panelTombolTab1.add(btnSimpan);
        panelTombolTab1.add(btnHapus);
        panelTombolTab1.add(btnBaru);
        panelTombolTab1.add(btnPreview);
        panelTombolTab1.add(btnCetakTab1);
        panelTombolTab1.add(btnKeluar);
        panelTab1.add(panelTombolTab1, BorderLayout.SOUTH);

        tabPane.addTab("  1. Input Variabel INA  ", panelTab1);

        // ====================================================================
        // TAB 2: PREVIEW & CETAK BERKAS
        // ====================================================================
        initPreviewTab();

        // ====================================================================
        // TAB 3: DATA BERKAS INA TERSIMPAN
        // ====================================================================
        initDataTab();

        internalFrame.add(tabPane, BorderLayout.CENTER);
    }

    private void initPreviewTab() {
        panelPreview = new JPanel(new BorderLayout());

        panelisi panelTombolTab2 = new panelisi();
        panelTombolTab2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        btnKembali = new Button();
        btnKembali.setIcon(getSafeIcon("/picture/011.png"));
        btnKembali.setText("Kembali");
        btnKembali.setFont(FONT_REGULAR);
        btnKembali.setPreferredSize(new Dimension(100, 30));
        btnKembali.addActionListener(e -> tabPane.setSelectedIndex(0));

        Label lblModel2 = new Label();
        lblModel2.setText("Model Cetakan :");
        lblModel2.setFont(FONT_REGULAR);

        cmbModelCetakTab2 = new ComboBox();
        cmbModelCetakTab2.setFont(FONT_REGULAR);
        cmbModelCetakTab2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "Model 2 (Form INA 1 Halaman)", "Model 1 (Format Standar 2 Halaman)"
        }));
        cmbModelCetakTab2.setPreferredSize(new Dimension(215, 30));
        cmbModelCetakTab2.addActionListener(e -> {
            if (cmbModelCetakTab1 != null && cmbModelCetakTab1.getSelectedIndex() != cmbModelCetakTab2.getSelectedIndex()) {
                cmbModelCetakTab1.setSelectedIndex(cmbModelCetakTab2.getSelectedIndex());
            }
            renderHTMLPreview();
        });

        btnRefresh = new Button();
        btnRefresh.setIcon(getSafeIcon("/picture/inventaris.png"));
        btnRefresh.setText("Perbarui Tampilan");
        btnRefresh.setFont(FONT_REGULAR);
        btnRefresh.setPreferredSize(new Dimension(140, 30));
        btnRefresh.addActionListener(e -> renderHTMLPreview());

        btnGeneratePDF = new Button();
        btnGeneratePDF.setIcon(getSafeIcon("/picture/b_print.png"));
        btnGeneratePDF.setText("Generate PDF / Cetak");
        btnGeneratePDF.setFont(FONT_BOLD);
        btnGeneratePDF.setPreferredSize(new Dimension(170, 30));
        btnGeneratePDF.addActionListener(e -> generatePDF());

        panelTombolTab2.add(btnKembali);
        panelTombolTab2.add(lblModel2);
        panelTombolTab2.add(cmbModelCetakTab2);
        panelTombolTab2.add(btnRefresh);
        panelTombolTab2.add(btnGeneratePDF);
        panelPreview.add(panelTombolTab2, BorderLayout.NORTH);

        // Preview dirender langsung dari PDF (bukan HTML) agar 100% identik hasil cetak
        panelPdfPreview = new JPanel();
        panelPdfPreview.setLayout(new BoxLayout(panelPdfPreview, BoxLayout.Y_AXIS));
        panelPdfPreview.setBackground(new Color(235, 238, 235));

        lblPdfPage = new JLabel();
        lblPdfPage.setAlignmentX(JPanel.CENTER_ALIGNMENT);

        scrollPreview = new ScrollPane();
        scrollPreview.setViewportView(panelPdfPreview);
        scrollPreview.getVerticalScrollBar().setUnitIncrement(24);
        panelPreview.add(scrollPreview, BorderLayout.CENTER);

        tabPane.addTab("  2. Preview & Cetak Berkas  ", panelPreview);
    }

    private void initDataTab() {
        panelData = new JPanel(new BorderLayout());

        // Bilah Pencarian Atas
        panelisi panelTopData = new panelisi();
        panelTopData.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        Label lblTgl = new Label();
        lblTgl.setText("Tgl. Masuk :");
        lblTgl.setFont(FONT_REGULAR);

        DTPCari1 = new Tanggal();
        DTPCari1.setDisplayFormat("dd-MM-yyyy");
        DTPCari1.setPreferredSize(new Dimension(95, 23));

        Label lblSampai = new Label();
        lblSampai.setText("s.d.");
        lblSampai.setFont(FONT_REGULAR);

        DTPCari2 = new Tanggal();
        DTPCari2.setDisplayFormat("dd-MM-yyyy");
        DTPCari2.setPreferredSize(new Dimension(95, 23));

        Label lblKey = new Label();
        lblKey.setText("Key Word :");
        lblKey.setFont(FONT_REGULAR);

        TCari = new TextBox();
        TCari.setPreferredSize(new Dimension(200, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampilData();
                }
            }
        });

        BtnCari = new Button();
        BtnCari.setIcon(getSafeIcon("/picture/accept.png"));
        BtnCari.setText("Cari");
        BtnCari.setPreferredSize(new Dimension(80, 23));
        BtnCari.addActionListener(e -> tampilData());

        BtnAll = new Button();
        BtnAll.setIcon(getSafeIcon("/picture/Search-16x16.png"));
        BtnAll.setText("Semua");
        BtnAll.setPreferredSize(new Dimension(85, 23));
        BtnAll.addActionListener(e -> {
            TCari.setText("");
            tampilData();
        });

        Label lblRec = new Label();
        lblRec.setText("Record :");
        lblRec.setFont(FONT_REGULAR);

        LCount = new Label();
        LCount.setText("0");
        LCount.setFont(FONT_BOLD);
        LCount.setPreferredSize(new Dimension(40, 23));

        panelTopData.add(lblTgl);
        panelTopData.add(DTPCari1);
        panelTopData.add(lblSampai);
        panelTopData.add(DTPCari2);
        panelTopData.add(lblKey);
        panelTopData.add(TCari);
        panelTopData.add(BtnCari);
        panelTopData.add(BtnAll);
        panelTopData.add(lblRec);
        panelTopData.add(LCount);

        panelData.add(panelTopData, BorderLayout.NORTH);

        // Tabel Data
        tbData = new Table();
        tabMode = new DefaultTableModel(null, new Object[]{
            "No. Rawat", "No. RM", "Nama Pasien", "No. SEP", "Tgl Masuk", "Tgl Keluar",
            "Jns Rawat", "Kelas", "Cara Pulang", "DPJP", "Total Tarif", "Diagnosa Primer", "Tgl Input"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return false;
            }
        };
        tbData.setModel(tabMode);
        tbData.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbData.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        int[] colWidth = {115, 75, 170, 130, 80, 80, 85, 55, 95, 160, 95, 170, 120};
        for (int i = 0; i < colWidth.length; i++) {
            TableColumn col = tbData.getColumnModel().getColumn(i);
            col.setPreferredWidth(colWidth[i]);
        }

        tbData.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2 && tbData.getSelectedRow() != -1) {
                    muatDataDariTabel();
                }
            }
        });

        scrollData = new ScrollPane();
        scrollData.setViewportView(tbData);
        panelData.add(scrollData, BorderLayout.CENTER);

        // Bilah Tombol Bawah Tab 3
        panelisi panelBottomData = new panelisi();
        panelBottomData.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        BtnMuat = new Button();
        BtnMuat.setIcon(getSafeIcon("/picture/add-file-16x16.png"));
        BtnMuat.setText("Buka / Edit");
        BtnMuat.setFont(FONT_REGULAR);
        BtnMuat.setPreferredSize(new Dimension(115, 30));
        BtnMuat.addActionListener(e -> muatDataDariTabel());

        BtnCetakTabel = new Button();
        BtnCetakTabel.setIcon(getSafeIcon("/picture/b_print.png"));
        BtnCetakTabel.setText("Cetak PDF");
        BtnCetakTabel.setFont(FONT_BOLD);
        BtnCetakTabel.setPreferredSize(new Dimension(115, 30));
        BtnCetakTabel.addActionListener(e -> {
            if (tbData.getSelectedRow() != -1) {
                muatDataDariTabel();
                generatePDF();
            } else {
                JOptionPane.showMessageDialog(null, "Silakan pilih salah satu data di tabel terlebih dahulu!");
            }
        });

        BtnHapusTabel = new Button();
        BtnHapusTabel.setIcon(getSafeIcon("/picture/stop_f2.png"));
        BtnHapusTabel.setText("Hapus");
        BtnHapusTabel.setFont(FONT_REGULAR);
        BtnHapusTabel.setPreferredSize(new Dimension(95, 30));
        BtnHapusTabel.addActionListener(e -> {
            if (tbData.getSelectedRow() != -1) {
                txtNoRawat.setText(tbData.getValueAt(tbData.getSelectedRow(), 0).toString());
                hapus();
                tampilData();
            } else {
                JOptionPane.showMessageDialog(null, "Silakan pilih data yang ingin dihapus!");
            }
        });

        BtnKeluarTabel = new Button();
        BtnKeluarTabel.setIcon(getSafeIcon("/picture/exit.png"));
        BtnKeluarTabel.setText("Keluar");
        BtnKeluarTabel.setFont(FONT_REGULAR);
        BtnKeluarTabel.setPreferredSize(new Dimension(95, 30));
        BtnKeluarTabel.addActionListener(e -> dispose());

        panelBottomData.add(BtnMuat);
        panelBottomData.add(BtnCetakTabel);
        panelBottomData.add(BtnHapusTabel);
        panelBottomData.add(BtnKeluarTabel);
        panelData.add(panelBottomData, BorderLayout.SOUTH);

        tabPane.addTab("  3. Data Berkas INA  ", panelData);

        tabPane.addChangeListener(e -> {
            if (tabPane.getSelectedIndex() == 2) {
                tampilData();
            }
        });
    }

    private void muatDataDariTabel() {
        if (tbData.getSelectedRow() != -1) {
            String nr = tbData.getValueAt(tbData.getSelectedRow(), 0).toString();
            setNoRawat(nr);
            tabPane.setSelectedIndex(0);
        }
    }

    private void tampilData() {
        tabMode.setRowCount(0);
        try {
            String sql = "SELECT lembar_bukti_pelayanan_inacbg.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, " +
                         "lembar_bukti_pelayanan_inacbg.no_sep, lembar_bukti_pelayanan_inacbg.tgl_masuk, lembar_bukti_pelayanan_inacbg.tgl_keluar, " +
                         "lembar_bukti_pelayanan_inacbg.jenis_perawatan, lembar_bukti_pelayanan_inacbg.kelas, lembar_bukti_pelayanan_inacbg.cara_pulang, " +
                         "lembar_bukti_pelayanan_inacbg.dokter_penanggung_jawab, lembar_bukti_pelayanan_inacbg.total_tarif, " +
                         "lembar_bukti_pelayanan_inacbg.diagnosa_primer, lembar_bukti_pelayanan_inacbg.tgl_input " +
                         "FROM lembar_bukti_pelayanan_inacbg " +
                         "INNER JOIN reg_periksa ON lembar_bukti_pelayanan_inacbg.no_rawat=reg_periksa.no_rawat " +
                         "INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                         "WHERE lembar_bukti_pelayanan_inacbg.tgl_masuk BETWEEN ? AND ? ";
            if (!TCari.getText().trim().isEmpty()) {
                sql += "AND (lembar_bukti_pelayanan_inacbg.no_rawat LIKE ? OR reg_periksa.no_rkm_medis LIKE ? " +
                       "OR pasien.nm_pasien LIKE ? OR lembar_bukti_pelayanan_inacbg.no_sep LIKE ? " +
                       "OR lembar_bukti_pelayanan_inacbg.dokter_penanggung_jawab LIKE ? " +
                       "OR lembar_bukti_pelayanan_inacbg.diagnosa_primer LIKE ?) ";
            }
            sql += "ORDER BY lembar_bukti_pelayanan_inacbg.tgl_input DESC";

            PreparedStatement ps = koneksi.prepareStatement(sql);
            ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
            ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
            if (!TCari.getText().trim().isEmpty()) {
                String q = "%" + TCari.getText().trim() + "%";
                ps.setString(3, q);
                ps.setString(4, q);
                ps.setString(5, q);
                ps.setString(6, q);
                ps.setString(7, q);
                ps.setString(8, q);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                tabMode.addRow(new Object[]{
                    rs.getString("no_rawat"),
                    rs.getString("no_rkm_medis"),
                    rs.getString("nm_pasien"),
                    rs.getString("no_sep") == null ? "-" : rs.getString("no_sep"),
                    rs.getString("tgl_masuk") == null ? "-" : rs.getString("tgl_masuk"),
                    rs.getString("tgl_keluar") == null ? "-" : rs.getString("tgl_keluar"),
                    rs.getString("jenis_perawatan") == null ? "-" : rs.getString("jenis_perawatan"),
                    rs.getString("kelas") == null ? "-" : rs.getString("kelas"),
                    rs.getString("cara_pulang") == null ? "-" : rs.getString("cara_pulang"),
                    rs.getString("dokter_penanggung_jawab") == null ? "-" : rs.getString("dokter_penanggung_jawab"),
                    df.format(rs.getDouble("total_tarif")),
                    rs.getString("diagnosa_primer") == null ? "-" : rs.getString("diagnosa_primer"),
                    rs.getString("tgl_input")
                });
            }
            rs.close();
            ps.close();
            LCount.setText(tabMode.getRowCount() + "");
        } catch (Exception e) {
            System.out.println("Error tampilData Lembar INA: " + e);
        }
    }

    private void initInputFields() {
        // 1. Data Administratif & Pasien
        JPanel pAdm = createSectionPanel(" Data Administratif & Pasien ");
        pAdm.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3, 4, 3, 4);
        g.fill = GridBagConstraints.HORIZONTAL;

        txtNoRawat = createTextBox(120);
        txtNoRawat.setEditable(false);
        txtNoRM = createTextBox(80);
        txtNoRM.setEditable(false);
        txtNmPasien = createTextBox(180);
        txtJK = createTextBox(35);
        txtTglLahir = createTextBox(90);

        txtNoPeserta = createTextBox(140);
        txtNoSEP = createTextBox(160);
        txtJenisPerawatan = createTextBox(100);
        txtKelas = createTextBox(60);

        txtTglMasuk = createTextBox(90);
        txtTglKeluar = createTextBox(90);
        cmbCaraPulang = new ComboBox();
        cmbCaraPulang.setFont(FONT_REGULAR);
        cmbCaraPulang.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
            "SEMBUH", "PULANG PAKSA", "RUJUK", "MENINGGAL DUNIA", "LAIN-LAIN"
        }));

        txtKdDokter = createTextBox(70);
        txtNmDokter = createTextBox(180);
        btnDokter = createPickerButton();
        btnDokter.setToolTipText("Cari DPJP");
        btnDokter.addActionListener(e -> pilihDokter());
        txtSIPDokter = createTextBox(140);

        txtBeratLahir = createTextBox(70);
        txtTotalTarif = createTextBox(110);
        txtSkorADL = createTextBox(70);
        txtPenandatanganPasien = createTextBox(180);

        // Baris 1: No. Rawat, No. RM, Nama Pasien, JK, Tgl Lahir
        addGrid(pAdm, g, createLabel("No. Rawat :"), 0, 0, 1);
        addGrid(pAdm, g, txtNoRawat, 1, 0, 1);
        addGrid(pAdm, g, createLabel("No. RM :"), 2, 0, 1);
        addGrid(pAdm, g, txtNoRM, 3, 0, 1);
        addGrid(pAdm, g, createLabel("Nama Pasien :"), 4, 0, 1);
        addGrid(pAdm, g, txtNmPasien, 5, 0, 1);
        addGrid(pAdm, g, createLabel("JK :"), 6, 0, 1);
        addGrid(pAdm, g, txtJK, 7, 0, 1);
        addGrid(pAdm, g, createLabel("Tgl. Lahir :"), 8, 0, 1);
        addGrid(pAdm, g, txtTglLahir, 9, 0, 1);

        // Baris 2: No. Peserta, No. SEP, Jenis Perawatan, Kelas
        addGrid(pAdm, g, createLabel("No. Peserta :"), 0, 1, 1);
        addGrid(pAdm, g, txtNoPeserta, 1, 1, 1);
        addGrid(pAdm, g, createLabel("No. SEP :"), 2, 1, 1);
        addGrid(pAdm, g, txtNoSEP, 3, 1, 1);
        addGrid(pAdm, g, createLabel("Jns. Perawatan :"), 4, 1, 1);
        addGrid(pAdm, g, txtJenisPerawatan, 5, 1, 1);
        addGrid(pAdm, g, createLabel("Kelas :"), 6, 1, 1);
        addGrid(pAdm, g, txtKelas, 7, 1, 3);

        // Baris 3: Tgl Masuk, Tgl Keluar, Cara Pulang, Berat Lahir
        addGrid(pAdm, g, createLabel("Tgl. Masuk :"), 0, 2, 1);
        addGrid(pAdm, g, txtTglMasuk, 1, 2, 1);
        addGrid(pAdm, g, createLabel("Tgl. Keluar :"), 2, 2, 1);
        addGrid(pAdm, g, txtTglKeluar, 3, 2, 1);
        addGrid(pAdm, g, createLabel("Cara Pulang :"), 4, 2, 1);
        addGrid(pAdm, g, cmbCaraPulang, 5, 2, 1);
        addGrid(pAdm, g, createLabel("Berat Lahir (g) :"), 6, 2, 1);
        addGrid(pAdm, g, txtBeratLahir, 7, 2, 3);

        // Baris 4: DPJP + Tombol Picker, SIP Dokter, ADL Skor
        JPanel pDokter = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        pDokter.setOpaque(false);
        pDokter.add(txtKdDokter);
        pDokter.add(txtNmDokter);
        pDokter.add(btnDokter);

        addGrid(pAdm, g, createLabel("DPJP (Dokter) :"), 0, 3, 1);
        addGrid(pAdm, g, pDokter, 1, 3, 3);
        addGrid(pAdm, g, createLabel("SIP Dokter :"), 4, 3, 1);
        addGrid(pAdm, g, txtSIPDokter, 5, 3, 1);
        addGrid(pAdm, g, createLabel("ADL Skor :"), 6, 3, 1);
        addGrid(pAdm, g, txtSkorADL, 7, 3, 3);

        // Baris 5: Total Tarif RS, Nama Pasien/Keluarga TTD
        addGrid(pAdm, g, createLabel("Total Tarif RS :"), 0, 4, 1);
        addGrid(pAdm, g, txtTotalTarif, 1, 4, 1);
        addGrid(pAdm, g, createLabel("Keluarga TTD :"), 2, 4, 1);
        addGrid(pAdm, g, txtPenandatanganPasien, 3, 4, 3);

        panelInput.add(pAdm);

        // 2. SPESIAL CMG
        JPanel pCMG = createSectionPanel(" SPESIAL CMG ");
        pCMG.setLayout(new BoxLayout(pCMG, BoxLayout.Y_AXIS));

        // Sub 1: Prosedur
        JPanel pProsedur = new JPanel(new GridLayout(0, 3, 4, 2));
        pProsedur.setBorder(BorderFactory.createTitledBorder(null, "1. Spesial Prosedur", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, new Color(50, 50, 50)));
        pProsedur.setOpaque(false);

        cbPCL = createCheckBox("PCL");
        cbColangiography = createCheckBox("Colangiography");
        cbRepairSeptal = createCheckBox("Repair Of Septal Defect Of Heart");
        cbTimekotomy = createCheckBox("Timekotomi");
        cbKeratoplasty = createCheckBox("Keratoplasty");
        cbTorakotomi = createCheckBox("Torakotomi");
        cbTumorPineal = createCheckBox("Tumor Pineal Endoscopy");
        cbVitrectomy = createCheckBox("Vitrectomy");
        cbPhacoemulsification = createCheckBox("Phacoemulsification");
        cbMicrolaringoscopi = createCheckBox("Microlaringoscopi");
        cbHipKnee = createCheckBox("HIP / Knee Replacement");
        cbAirPlumbage = createCheckBox("Air Plumbage");
        cbLobektomi = createCheckBox("Lobektomi / Bilobektomi");
        cbStereotactic = createCheckBox("Stereotactic Surgery Radiotherapy");
        cbPancreatectomy = createCheckBox("Pancreatectomy");

        pProsedur.add(cbPCL);
        pProsedur.add(cbColangiography);
        pProsedur.add(cbRepairSeptal);
        pProsedur.add(cbTimekotomy);
        pProsedur.add(cbKeratoplasty);
        pProsedur.add(cbTorakotomi);
        pProsedur.add(cbTumorPineal);
        pProsedur.add(cbVitrectomy);
        pProsedur.add(cbPhacoemulsification);
        pProsedur.add(cbMicrolaringoscopi);
        pProsedur.add(cbHipKnee);
        pProsedur.add(cbAirPlumbage);
        pProsedur.add(cbLobektomi);
        pProsedur.add(cbStereotactic);
        pProsedur.add(cbPancreatectomy);
        pCMG.add(pProsedur);

        // Sub 2, 3, 4: Drug, Investigation, Protheis
        JPanel pMidCMG = new JPanel(new GridLayout(1, 3, 6, 2));
        pMidCMG.setOpaque(false);

        // 2. Drug
        JPanel pDrug = new JPanel(new GridLayout(0, 1, 2, 2));
        pDrug.setBorder(BorderFactory.createTitledBorder(null, "2. Spesial Drug", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, new Color(50, 50, 50)));
        pDrug.setOpaque(false);
        cbStreptokinase = createCheckBox("Streptokinase");
        cbDeferifrone = createCheckBox("Deferifrone");
        cbDeferoksamin = createCheckBox("Deferoksamin");
        cbDeferasirox = createCheckBox("Deferasirox");
        cbHumanAlbumin = createCheckBox("Human Albumin");
        pDrug.add(cbStreptokinase);
        pDrug.add(cbDeferifrone);
        pDrug.add(cbDeferoksamin);
        pDrug.add(cbDeferasirox);
        pDrug.add(cbHumanAlbumin);
        pMidCMG.add(pDrug);

        // 3. Investigation
        JPanel pInvest = new JPanel(new GridLayout(0, 1, 2, 2));
        pInvest.setBorder(BorderFactory.createTitledBorder(null, "3. Spesial Investigation", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, new Color(50, 50, 50)));
        pInvest.setOpaque(false);
        cbOtherCTScan = createCheckBox("Other CT Scan");
        cbNuclearMedicine = createCheckBox("Nuclear Medicine");
        cbMRI = createCheckBox("MRI");
        cbDiagnosticEye = createCheckBox("Diagnostic & Imaging Eye");
        pInvest.add(cbOtherCTScan);
        pInvest.add(cbNuclearMedicine);
        pInvest.add(cbMRI);
        pInvest.add(cbDiagnosticEye);
        pMidCMG.add(pInvest);

        // 4. Protheis
        JPanel pProtheis = new JPanel(new GridLayout(0, 1, 2, 2));
        pProtheis.setBorder(BorderFactory.createTitledBorder(null, "4. Spesial Protheis", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, new Color(50, 50, 50)));
        pProtheis.setOpaque(false);
        cbSubduratGrid = createCheckBox("Subdurat Grid Electrode");
        cbCoteGraft = createCheckBox("Cote Graft");
        cbLiquidEmbolic = createCheckBox("Liquid Embolic (For AVM)");
        cbHipImplant = createCheckBox("HIP / Knee Implant");
        pProtheis.add(cbSubduratGrid);
        pProtheis.add(cbCoteGraft);
        pProtheis.add(cbLiquidEmbolic);
        pProtheis.add(cbHipImplant);
        pMidCMG.add(pProtheis);

        pCMG.add(pMidCMG);
        panelInput.add(pCMG);

        // 3. DIAGNOSA & TINDAKAN
        JPanel pDiagTind = new JPanel(new GridLayout(1, 2, 6, 2));
        pDiagTind.setOpaque(false);

        // Kolom Kiri: Diagnosa ICD-10
        JPanel pDiagnosa = new JPanel(new GridBagLayout());
        pDiagnosa.setBorder(BorderFactory.createTitledBorder(null, "Diagnosa (ICD-10)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, new Color(50, 50, 50)));
        pDiagnosa.setOpaque(false);
        GridBagConstraints gd = new GridBagConstraints();
        gd.insets = new Insets(2, 3, 2, 3);
        gd.fill = GridBagConstraints.HORIZONTAL;

        txtKdDiagPrimer = createTextBox(65);
        txtNmDiagPrimer = createTextBox(200);
        btnDiagPrimer = createPickerButton();
        btnDiagPrimer.addActionListener(e -> pilihPenyakit(txtKdDiagPrimer, txtNmDiagPrimer, btnDiagPrimer));

        txtKdDiagSekunder1 = createTextBox(65);
        txtNmDiagSekunder1 = createTextBox(200);
        btnDiagSekunder1 = createPickerButton();
        btnDiagSekunder1.addActionListener(e -> pilihPenyakit(txtKdDiagSekunder1, txtNmDiagSekunder1, btnDiagSekunder1));

        txtKdDiagSekunder2 = createTextBox(65);
        txtNmDiagSekunder2 = createTextBox(200);
        btnDiagSekunder2 = createPickerButton();
        btnDiagSekunder2.addActionListener(e -> pilihPenyakit(txtKdDiagSekunder2, txtNmDiagSekunder2, btnDiagSekunder2));

        txtKdDiagSekunder3 = createTextBox(65);
        txtNmDiagSekunder3 = createTextBox(200);
        btnDiagSekunder3 = createPickerButton();
        btnDiagSekunder3.addActionListener(e -> pilihPenyakit(txtKdDiagSekunder3, txtNmDiagSekunder3, btnDiagSekunder3));

        txtKdDiagSekunder4 = createTextBox(65);
        txtNmDiagSekunder4 = createTextBox(200);
        btnDiagSekunder4 = createPickerButton();
        btnDiagSekunder4.addActionListener(e -> pilihPenyakit(txtKdDiagSekunder4, txtNmDiagSekunder4, btnDiagSekunder4));

        txtKdDiagSekunder5 = createTextBox(65);
        txtNmDiagSekunder5 = createTextBox(200);
        btnDiagSekunder5 = createPickerButton();
        btnDiagSekunder5.addActionListener(e -> pilihPenyakit(txtKdDiagSekunder5, txtNmDiagSekunder5, btnDiagSekunder5));

        addDiagRow(pDiagnosa, gd, 0, "Primer :", txtKdDiagPrimer, txtNmDiagPrimer, btnDiagPrimer);
        addDiagRow(pDiagnosa, gd, 1, "Sekunder 1 :", txtKdDiagSekunder1, txtNmDiagSekunder1, btnDiagSekunder1);
        addDiagRow(pDiagnosa, gd, 2, "Sekunder 2 :", txtKdDiagSekunder2, txtNmDiagSekunder2, btnDiagSekunder2);
        addDiagRow(pDiagnosa, gd, 3, "Sekunder 3 :", txtKdDiagSekunder3, txtNmDiagSekunder3, btnDiagSekunder3);
        addDiagRow(pDiagnosa, gd, 4, "Sekunder 4 :", txtKdDiagSekunder4, txtNmDiagSekunder4, btnDiagSekunder4);
        addDiagRow(pDiagnosa, gd, 5, "Sekunder 5 :", txtKdDiagSekunder5, txtNmDiagSekunder5, btnDiagSekunder5);
        pDiagTind.add(pDiagnosa);

        // Kolom Kanan: Tindakan ICD-9
        JPanel pTindakan = new JPanel(new GridBagLayout());
        pTindakan.setBorder(BorderFactory.createTitledBorder(null, "Tindakan / Prosedur (ICD-9)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, new Color(50, 50, 50)));
        pTindakan.setOpaque(false);
        GridBagConstraints gt = new GridBagConstraints();
        gt.insets = new Insets(2, 3, 2, 3);
        gt.fill = GridBagConstraints.HORIZONTAL;

        txtKdTindakanPrimer = createTextBox(65);
        txtNmTindakanPrimer = createTextBox(200);
        btnTindakanPrimer = createPickerButton();
        btnTindakanPrimer.addActionListener(e -> pilihICD9(txtKdTindakanPrimer, txtNmTindakanPrimer, btnTindakanPrimer));

        txtKdTindakanSekunder1 = createTextBox(65);
        txtNmTindakanSekunder1 = createTextBox(200);
        btnTindakanSekunder1 = createPickerButton();
        btnTindakanSekunder1.addActionListener(e -> pilihICD9(txtKdTindakanSekunder1, txtNmTindakanSekunder1, btnTindakanSekunder1));

        txtKdTindakanSekunder2 = createTextBox(65);
        txtNmTindakanSekunder2 = createTextBox(200);
        btnTindakanSekunder2 = createPickerButton();
        btnTindakanSekunder2.addActionListener(e -> pilihICD9(txtKdTindakanSekunder2, txtNmTindakanSekunder2, btnTindakanSekunder2));

        txtKdTindakanSekunder3 = createTextBox(65);
        txtNmTindakanSekunder3 = createTextBox(200);
        btnTindakanSekunder3 = createPickerButton();
        btnTindakanSekunder3.addActionListener(e -> pilihICD9(txtKdTindakanSekunder3, txtNmTindakanSekunder3, btnTindakanSekunder3));

        txtKdTindakanSekunder4 = createTextBox(65);
        txtNmTindakanSekunder4 = createTextBox(200);
        btnTindakanSekunder4 = createPickerButton();
        btnTindakanSekunder4.addActionListener(e -> pilihICD9(txtKdTindakanSekunder4, txtNmTindakanSekunder4, btnTindakanSekunder4));

        txtKdTindakanSekunder5 = createTextBox(65);
        txtNmTindakanSekunder5 = createTextBox(200);
        btnTindakanSekunder5 = createPickerButton();
        btnTindakanSekunder5.addActionListener(e -> pilihICD9(txtKdTindakanSekunder5, txtNmTindakanSekunder5, btnTindakanSekunder5));

        addDiagRow(pTindakan, gt, 0, "Primer :", txtKdTindakanPrimer, txtNmTindakanPrimer, btnTindakanPrimer);
        addDiagRow(pTindakan, gt, 1, "Sekunder 1 :", txtKdTindakanSekunder1, txtNmTindakanSekunder1, btnTindakanSekunder1);
        addDiagRow(pTindakan, gt, 2, "Sekunder 2 :", txtKdTindakanSekunder2, txtNmTindakanSekunder2, btnTindakanSekunder2);
        addDiagRow(pTindakan, gt, 3, "Sekunder 3 :", txtKdTindakanSekunder3, txtNmTindakanSekunder3, btnTindakanSekunder3);
        addDiagRow(pTindakan, gt, 4, "Sekunder 4 :", txtKdTindakanSekunder4, txtNmTindakanSekunder4, btnTindakanSekunder4);
        addDiagRow(pTindakan, gt, 5, "Sekunder 5 :", txtKdTindakanSekunder5, txtNmTindakanSekunder5, btnTindakanSekunder5);
        pDiagTind.add(pTindakan);

        panelInput.add(pDiagTind);

        // 4. PEMERIKSAAN PENUNJANG (Primer + Sekunder 1 s/d 5)
        JPanel pPenunjang = createSectionPanel(" Pemeriksaan Penunjang ");
        pPenunjang.setLayout(new GridBagLayout());
        GridBagConstraints gp = new GridBagConstraints();
        gp.insets = new Insets(2, 4, 2, 4);
        gp.fill = GridBagConstraints.HORIZONTAL;

        txtKdPenunjangPrimer = createTextBox(70);
        txtNmPenunjangPrimer = createTextBox(380);
        txtKdPenunjangSekunder1 = createTextBox(70);
        txtNmPenunjangSekunder1 = createTextBox(380);
        txtKdPenunjangSekunder2 = createTextBox(70);
        txtNmPenunjangSekunder2 = createTextBox(380);
        txtKdPenunjangSekunder3 = createTextBox(70);
        txtNmPenunjangSekunder3 = createTextBox(380);
        txtKdPenunjangSekunder4 = createTextBox(70);
        txtNmPenunjangSekunder4 = createTextBox(380);
        txtKdPenunjangSekunder5 = createTextBox(70);
        txtNmPenunjangSekunder5 = createTextBox(380);

        addPenunjangRow(pPenunjang, gp, 0, "Primer :", txtKdPenunjangPrimer, txtNmPenunjangPrimer);
        addPenunjangRow(pPenunjang, gp, 1, "Sekunder 1 :", txtKdPenunjangSekunder1, txtNmPenunjangSekunder1);
        addPenunjangRow(pPenunjang, gp, 2, "Sekunder 2 :", txtKdPenunjangSekunder2, txtNmPenunjangSekunder2);
        addPenunjangRow(pPenunjang, gp, 3, "Sekunder 3 :", txtKdPenunjangSekunder3, txtNmPenunjangSekunder3);
        addPenunjangRow(pPenunjang, gp, 4, "Sekunder 4 :", txtKdPenunjangSekunder4, txtNmPenunjangSekunder4);
        addPenunjangRow(pPenunjang, gp, 5, "Sekunder 5 :", txtKdPenunjangSekunder5, txtNmPenunjangSekunder5);

        panelInput.add(pPenunjang);
    }

    private void addDiagRow(JPanel p, GridBagConstraints g, int row, String label, TextBox txtKd, TextBox txtNm, Button btn) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        p.add(createLabel(label), g);
        g.gridx = 1; g.gridy = row; g.gridwidth = 1;
        p.add(txtKd, g);
        g.gridx = 2; g.gridy = row; g.gridwidth = 1;
        p.add(txtNm, g);
        g.gridx = 3; g.gridy = row; g.gridwidth = 1;
        p.add(btn, g);
    }

    private void addPenunjangRow(JPanel p, GridBagConstraints g, int row, String label, TextBox txtKd, TextBox txtNm) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        p.add(createLabel(label), g);
        g.gridx = 1; g.gridy = row; g.gridwidth = 1;
        p.add(txtKd, g);
        g.gridx = 2; g.gridy = row; g.gridwidth = 1;
        p.add(txtNm, g);
    }

    private Button createPickerButton() {
        Button b = new Button();
        b.setIcon(getSafeIcon("/picture/190.png"));
        b.setPreferredSize(new Dimension(28, 23));
        return b;
    }

    private void pilihDokter() {
        if (dlgDokter == null || !dlgDokter.isDisplayable()) {
            dlgDokter = new DlgCariDokter(null, false);
            dlgDokter.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            dlgDokter.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (dlgDokter.getTable().getSelectedRow() != -1) {
                        String kd = dlgDokter.getTable().getValueAt(dlgDokter.getTable().getSelectedRow(), 0).toString();
                        String nm = dlgDokter.getTable().getValueAt(dlgDokter.getTable().getSelectedRow(), 1).toString();
                        txtKdDokter.setText(kd);
                        txtNmDokter.setText(nm);
                        txtSIPDokter.setText(Sequel.cariIsi("select no_ijn_praktek from dokter where kd_dokter=?", kd));
                    }
                    btnDokter.requestFocus();
                }
            });
            dlgDokter.setSize(internalFrame.getWidth() - 20, internalFrame.getHeight() - 20);
            dlgDokter.setLocationRelativeTo(internalFrame);
        }
        if (!dlgDokter.isVisible()) {
            dlgDokter.isCek();
            dlgDokter.emptTeks();
            dlgDokter.setVisible(true);
        }
    }

    private void pilihPenyakit(TextBox txtKd, TextBox txtNm, Button btnSource) {
        targetKdDiag = txtKd;
        targetNmDiag = txtNm;
        targetBtnDiag = btnSource;

        if (dlgPenyakit == null || !dlgPenyakit.isDisplayable()) {
            dlgPenyakit = new DlgCariPenyakit(null, false);
            dlgPenyakit.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            dlgPenyakit.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (dlgPenyakit.getTable().getSelectedRow() != -1 && targetKdDiag != null) {
                        targetKdDiag.setText(dlgPenyakit.getTable().getValueAt(dlgPenyakit.getTable().getSelectedRow(), 0).toString());
                        targetNmDiag.setText(dlgPenyakit.getTable().getValueAt(dlgPenyakit.getTable().getSelectedRow(), 1).toString());
                    }
                    if (targetBtnDiag != null) {
                        targetBtnDiag.requestFocus();
                    }
                }
            });
            dlgPenyakit.setSize(internalFrame.getWidth() - 20, internalFrame.getHeight() - 20);
            dlgPenyakit.setLocationRelativeTo(internalFrame);
        }
        if (!dlgPenyakit.isVisible()) {
            dlgPenyakit.isCek();
            dlgPenyakit.emptTeks();
            dlgPenyakit.setVisible(true);
        }
    }

    private void pilihICD9(TextBox txtKd, TextBox txtNm, Button btnSource) {
        targetKdTindakan = txtKd;
        targetNmTindakan = txtNm;
        targetBtnTindakan = btnSource;

        if (dlgICD9 == null || !dlgICD9.isDisplayable()) {
            dlgICD9 = new DlgCariICD9(null, false);
            dlgICD9.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
            dlgICD9.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (dlgICD9.getTable().getSelectedRow() != -1 && targetKdTindakan != null) {
                        targetKdTindakan.setText(dlgICD9.getTable().getValueAt(dlgICD9.getTable().getSelectedRow(), 0).toString());
                        targetNmTindakan.setText(dlgICD9.getTable().getValueAt(dlgICD9.getTable().getSelectedRow(), 1).toString());
                    }
                    if (targetBtnTindakan != null) {
                        targetBtnTindakan.requestFocus();
                    }
                }
            });
            dlgICD9.setSize(internalFrame.getWidth() - 20, internalFrame.getHeight() - 20);
            dlgICD9.setLocationRelativeTo(internalFrame);
        }
        if (!dlgICD9.isVisible()) {
            dlgICD9.isCek();
            dlgICD9.emptTeks();
            dlgICD9.setVisible(true);
        }
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 210, 200)),
            title,
            TitledBorder.DEFAULT_JUSTIFICATION,
            TitledBorder.DEFAULT_POSITION,
            FONT_TITLE,
            new Color(40, 60, 40)
        ));
        p.setOpaque(false);
        return p;
    }

    private ImageIcon getSafeIcon(String path) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                return new ImageIcon(url);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private TextBox createTextBox(int width) {
        TextBox t = new TextBox();
        t.setFont(FONT_REGULAR);
        t.setPreferredSize(new Dimension(width, 23));
        return t;
    }

    private Label createLabel(String text) {
        Label l = new Label();
        l.setText(text);
        l.setFont(FONT_REGULAR);
        return l;
    }

    private CekBox createCheckBox(String label) {
        CekBox c = new CekBox();
        c.setText(label);
        c.setFont(FONT_REGULAR);
        c.setOpaque(false);
        return c;
    }

    private void addGrid(JPanel p, GridBagConstraints g, java.awt.Component c, int x, int y, int width) {
        g.gridx = x;
        g.gridy = y;
        g.gridwidth = width;
        p.add(c, g);
    }

    public void setNoRawat(String noRawat) {
        emptTeks();
        txtNoRawat.setText(noRawat);
        isCek();
        loadData(noRawat);
    }

    public void emptTeks() {
        txtNoRawat.setText("");
        txtNoRM.setText("");
        txtNmPasien.setText("");
        txtJK.setText("");
        txtTglLahir.setText("");
        txtNoPeserta.setText("");
        txtNoSEP.setText("");
        txtJenisPerawatan.setText("");
        txtKelas.setText("");
        txtTglMasuk.setText("");
        txtTglKeluar.setText("");
        cmbCaraPulang.setSelectedIndex(0);
        txtKdDokter.setText("");
        txtNmDokter.setText("");
        txtSIPDokter.setText("");
        txtBeratLahir.setText("");
        txtTotalTarif.setText("0");
        txtSkorADL.setText("");
        txtPenandatanganPasien.setText("");

        cbPCL.setSelected(false);
        cbColangiography.setSelected(false);
        cbRepairSeptal.setSelected(false);
        cbTimekotomy.setSelected(false);
        cbKeratoplasty.setSelected(false);
        cbTorakotomi.setSelected(false);
        cbTumorPineal.setSelected(false);
        cbVitrectomy.setSelected(false);
        cbPhacoemulsification.setSelected(false);
        cbMicrolaringoscopi.setSelected(false);
        cbHipKnee.setSelected(false);
        cbAirPlumbage.setSelected(false);
        cbLobektomi.setSelected(false);
        cbStereotactic.setSelected(false);
        cbPancreatectomy.setSelected(false);

        cbStreptokinase.setSelected(false);
        cbDeferifrone.setSelected(false);
        cbDeferoksamin.setSelected(false);
        cbDeferasirox.setSelected(false);
        cbHumanAlbumin.setSelected(false);

        cbOtherCTScan.setSelected(false);
        cbNuclearMedicine.setSelected(false);
        cbMRI.setSelected(false);
        cbDiagnosticEye.setSelected(false);

        cbSubduratGrid.setSelected(false);
        cbCoteGraft.setSelected(false);
        cbLiquidEmbolic.setSelected(false);
        cbHipImplant.setSelected(false);

        txtKdDiagPrimer.setText("");
        txtNmDiagPrimer.setText("");
        txtKdDiagSekunder1.setText("");
        txtNmDiagSekunder1.setText("");
        txtKdDiagSekunder2.setText("");
        txtNmDiagSekunder2.setText("");
        txtKdDiagSekunder3.setText("");
        txtNmDiagSekunder3.setText("");
        txtKdDiagSekunder4.setText("");
        txtNmDiagSekunder4.setText("");
        txtKdDiagSekunder5.setText("");
        txtNmDiagSekunder5.setText("");

        txtKdTindakanPrimer.setText("");
        txtNmTindakanPrimer.setText("");
        txtKdTindakanSekunder1.setText("");
        txtNmTindakanSekunder1.setText("");
        txtKdTindakanSekunder2.setText("");
        txtNmTindakanSekunder2.setText("");
        txtKdTindakanSekunder3.setText("");
        txtNmTindakanSekunder3.setText("");
        txtKdTindakanSekunder4.setText("");
        txtNmTindakanSekunder4.setText("");
        txtKdTindakanSekunder5.setText("");
        txtNmTindakanSekunder5.setText("");

        txtKdPenunjangPrimer.setText("");
        txtNmPenunjangPrimer.setText("");
        txtKdPenunjangSekunder1.setText("");
        txtNmPenunjangSekunder1.setText("");
        txtKdPenunjangSekunder2.setText("");
        txtNmPenunjangSekunder2.setText("");
        txtKdPenunjangSekunder3.setText("");
        txtNmPenunjangSekunder3.setText("");
        txtKdPenunjangSekunder4.setText("");
        txtNmPenunjangSekunder4.setText("");
        txtKdPenunjangSekunder5.setText("");
        txtNmPenunjangSekunder5.setText("");

        tabPane.setSelectedIndex(0);
        clearPdfPreview();
    }

    public void isCek() {
        btnSimpan.setEnabled(akses.getberkas_ina());
        btnHapus.setEnabled(akses.getberkas_ina());
    }

    private void loadData(String noRawat) {
        if (noRawat.isEmpty()) return;
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "SELECT * FROM lembar_bukti_pelayanan_inacbg WHERE no_rawat=?"
            );
            ps.setString(1, noRawat);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // Muat snapshot tersimpan
                txtNoSEP.setText(rs.getString("no_sep") == null ? "" : rs.getString("no_sep"));
                txtNoPeserta.setText(rs.getString("no_peserta") == null ? "" : rs.getString("no_peserta"));
                txtTglMasuk.setText(rs.getString("tgl_masuk") == null ? "" : rs.getString("tgl_masuk"));
                txtTglKeluar.setText(rs.getString("tgl_keluar") == null ? "" : rs.getString("tgl_keluar"));
                txtJenisPerawatan.setText(rs.getString("jenis_perawatan") == null ? "" : rs.getString("jenis_perawatan"));
                txtKelas.setText(rs.getString("kelas") == null ? "" : rs.getString("kelas"));
                cmbCaraPulang.setSelectedItem(rs.getString("cara_pulang") == null ? "SEMBUH" : rs.getString("cara_pulang"));
                txtKdDokter.setText(rs.getString("kd_dokter") == null ? "" : rs.getString("kd_dokter"));
                txtNmDokter.setText(rs.getString("dokter_penanggung_jawab") == null ? "" : rs.getString("dokter_penanggung_jawab"));
                txtSIPDokter.setText(rs.getString("sip_dokter") == null ? "" : rs.getString("sip_dokter"));
                txtBeratLahir.setText(rs.getString("berat_lahir") == null ? "" : rs.getString("berat_lahir"));
                txtTotalTarif.setText(df.format(rs.getDouble("total_tarif")));
                txtSkorADL.setText(rs.getString("adl_skor") == null ? "" : rs.getString("adl_skor"));
                txtPenandatanganPasien.setText(rs.getString("nama_pasien_keluarga") == null ? "" : rs.getString("nama_pasien_keluarga"));

                // Checklist CMG
                String sp = rs.getString("spesial_prosedur") == null ? "" : rs.getString("spesial_prosedur");
                cbPCL.setSelected(sp.contains("PCL"));
                cbColangiography.setSelected(sp.contains("Colangiography"));
                cbRepairSeptal.setSelected(sp.contains("Repair Of Septal"));
                cbTimekotomy.setSelected(sp.contains("Timekotomi"));
                cbKeratoplasty.setSelected(sp.contains("Keratoplasty"));
                cbTorakotomi.setSelected(sp.contains("Torakotomi"));
                cbTumorPineal.setSelected(sp.contains("Tumor Pineal"));
                cbVitrectomy.setSelected(sp.contains("Vitrectomy"));
                cbPhacoemulsification.setSelected(sp.contains("Phacoemulsification"));
                cbMicrolaringoscopi.setSelected(sp.contains("Microlaringoscopi"));
                cbHipKnee.setSelected(sp.contains("HIP / Knee Replacement") || sp.contains("HIP Replacement"));
                cbAirPlumbage.setSelected(sp.contains("Air Plumbage"));
                cbLobektomi.setSelected(sp.contains("Lobektomi"));
                cbStereotactic.setSelected(sp.contains("Stereotactic"));
                cbPancreatectomy.setSelected(sp.contains("Pancreatectomy"));

                String sd = rs.getString("spesial_drug") == null ? "" : rs.getString("spesial_drug");
                cbStreptokinase.setSelected(sd.contains("Streptokinase"));
                cbDeferifrone.setSelected(sd.contains("Deferifrone"));
                cbDeferoksamin.setSelected(sd.contains("Deferoksamin"));
                cbDeferasirox.setSelected(sd.contains("Deferasirox"));
                cbHumanAlbumin.setSelected(sd.contains("Human Albumin"));

                String si = rs.getString("spesial_investigation") == null ? "" : rs.getString("spesial_investigation");
                cbOtherCTScan.setSelected(si.contains("Other CT Scan"));
                cbNuclearMedicine.setSelected(si.contains("Nuclear Medicine"));
                cbMRI.setSelected(si.contains("MRI"));
                cbDiagnosticEye.setSelected(si.contains("Diagnostic"));

                String sprot = rs.getString("spesial_protheis") == null ? "" : rs.getString("spesial_protheis");
                cbSubduratGrid.setSelected(sprot.contains("Subdurat Grid"));
                cbCoteGraft.setSelected(sprot.contains("Cote Graft"));
                cbLiquidEmbolic.setSelected(sprot.contains("Liquid Embolic"));
                cbHipImplant.setSelected(sprot.contains("HIP Implant"));

                // Diagnosa
                txtKdDiagPrimer.setText(rs.getString("kd_diagnosa_primer") == null ? "" : rs.getString("kd_diagnosa_primer"));
                txtNmDiagPrimer.setText(rs.getString("diagnosa_primer") == null ? "" : rs.getString("diagnosa_primer"));
                txtKdDiagSekunder1.setText(rs.getString("kd_diagnosa_sekunder1") == null ? "" : rs.getString("kd_diagnosa_sekunder1"));
                txtNmDiagSekunder1.setText(rs.getString("diagnosa_sekunder1") == null ? "" : rs.getString("diagnosa_sekunder1"));
                txtKdDiagSekunder2.setText(rs.getString("kd_diagnosa_sekunder2") == null ? "" : rs.getString("kd_diagnosa_sekunder2"));
                txtNmDiagSekunder2.setText(rs.getString("diagnosa_sekunder2") == null ? "" : rs.getString("diagnosa_sekunder2"));
                txtKdDiagSekunder3.setText(rs.getString("kd_diagnosa_sekunder3") == null ? "" : rs.getString("kd_diagnosa_sekunder3"));
                txtNmDiagSekunder3.setText(rs.getString("diagnosa_sekunder3") == null ? "" : rs.getString("diagnosa_sekunder3"));
                txtKdDiagSekunder4.setText(rs.getString("kd_diagnosa_sekunder4") == null ? "" : rs.getString("kd_diagnosa_sekunder4"));
                txtNmDiagSekunder4.setText(rs.getString("diagnosa_sekunder4") == null ? "" : rs.getString("diagnosa_sekunder4"));
                txtKdDiagSekunder5.setText(rs.getString("kd_diagnosa_sekunder5") == null ? "" : rs.getString("kd_diagnosa_sekunder5"));
                txtNmDiagSekunder5.setText(rs.getString("diagnosa_sekunder5") == null ? "" : rs.getString("diagnosa_sekunder5"));

                // Tindakan
                txtKdTindakanPrimer.setText(rs.getString("kd_tindakan_primer") == null ? "" : rs.getString("kd_tindakan_primer"));
                txtNmTindakanPrimer.setText(rs.getString("tindakan_primer") == null ? "" : rs.getString("tindakan_primer"));
                txtKdTindakanSekunder1.setText(rs.getString("kd_tindakan_sekunder1") == null ? "" : rs.getString("kd_tindakan_sekunder1"));
                txtNmTindakanSekunder1.setText(rs.getString("tindakan_sekunder1") == null ? "" : rs.getString("tindakan_sekunder1"));
                txtKdTindakanSekunder2.setText(rs.getString("kd_tindakan_sekunder2") == null ? "" : rs.getString("kd_tindakan_sekunder2"));
                txtNmTindakanSekunder2.setText(rs.getString("tindakan_sekunder2") == null ? "" : rs.getString("tindakan_sekunder2"));
                txtKdTindakanSekunder3.setText(rs.getString("kd_tindakan_sekunder3") == null ? "" : rs.getString("kd_tindakan_sekunder3"));
                txtNmTindakanSekunder3.setText(rs.getString("tindakan_sekunder3") == null ? "" : rs.getString("tindakan_sekunder3"));
                txtKdTindakanSekunder4.setText(rs.getString("kd_tindakan_sekunder4") == null ? "" : rs.getString("kd_tindakan_sekunder4"));
                txtNmTindakanSekunder4.setText(rs.getString("tindakan_sekunder4") == null ? "" : rs.getString("tindakan_sekunder4"));
                txtKdTindakanSekunder5.setText(rs.getString("kd_tindakan_sekunder5") == null ? "" : rs.getString("kd_tindakan_sekunder5"));
                txtNmTindakanSekunder5.setText(rs.getString("tindakan_sekunder5") == null ? "" : rs.getString("tindakan_sekunder5"));

                // Penunjang (Primer + Sekunder 1 s/d 5)
                txtKdPenunjangPrimer.setText(rs.getString("kd_penunjang_primer") == null ? "" : rs.getString("kd_penunjang_primer"));
                txtNmPenunjangPrimer.setText(rs.getString("penunjang_primer") == null ? "" : rs.getString("penunjang_primer"));
                txtKdPenunjangSekunder1.setText(rs.getString("kd_penunjang_sekunder1") == null ? "" : rs.getString("kd_penunjang_sekunder1"));
                txtNmPenunjangSekunder1.setText(rs.getString("penunjang_sekunder1") == null ? "" : rs.getString("penunjang_sekunder1"));
                txtKdPenunjangSekunder2.setText(rs.getString("kd_penunjang_sekunder2") == null ? "" : rs.getString("kd_penunjang_sekunder2"));
                txtNmPenunjangSekunder2.setText(rs.getString("penunjang_sekunder2") == null ? "" : rs.getString("penunjang_sekunder2"));
                txtKdPenunjangSekunder3.setText(rs.getString("kd_penunjang_sekunder3") == null ? "" : rs.getString("kd_penunjang_sekunder3"));
                txtNmPenunjangSekunder3.setText(rs.getString("penunjang_sekunder3") == null ? "" : rs.getString("penunjang_sekunder3"));
                try {
                    txtKdPenunjangSekunder4.setText(rs.getString("kd_penunjang_sekunder4") == null ? "" : rs.getString("kd_penunjang_sekunder4"));
                    txtNmPenunjangSekunder4.setText(rs.getString("penunjang_sekunder4") == null ? "" : rs.getString("penunjang_sekunder4"));
                    txtKdPenunjangSekunder5.setText(rs.getString("kd_penunjang_sekunder5") == null ? "" : rs.getString("kd_penunjang_sekunder5"));
                    txtNmPenunjangSekunder5.setText(rs.getString("penunjang_sekunder5") == null ? "" : rs.getString("penunjang_sekunder5"));
                } catch (Exception ignored) {}

                // Data Pasien dasar
                loadPasienBase(noRawat);
                rs.close();
                ps.close();
            } else {
                rs.close();
                ps.close();
                // Jika belum ada data tersimpan, auto fill dari SIMRS
                autoFill(noRawat);
            }
        } catch (Exception e) {
            System.out.println("Error loadData Lembar INA: " + e);
        }
    }

    private void loadPasienBase(String noRawat) {
        try {
            PreparedStatement ps = koneksi.prepareStatement(
                "SELECT reg_periksa.no_rkm_medis, pasien.nm_pasien, pasien.jk, pasien.tgl_lahir " +
                "FROM reg_periksa INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "WHERE reg_periksa.no_rawat=?"
            );
            ps.setString(1, noRawat);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNoRM.setText(rs.getString("no_rkm_medis"));
                txtNmPasien.setText(rs.getString("nm_pasien"));
                txtJK.setText(rs.getString("jk"));
                txtTglLahir.setText(rs.getString("tgl_lahir"));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            System.out.println("Error loadPasienBase: " + e);
        }
    }

    private void autoFill(String noRawat) {
        if (noRawat.isEmpty()) return;
        try {
            // 1. Data Registrasi & Pasien
            PreparedStatement ps = koneksi.prepareStatement(
                "SELECT reg_periksa.no_rkm_medis, reg_periksa.tgl_registrasi, reg_periksa.status_lanjut, " +
                "reg_periksa.kd_dokter, dokter.nm_dokter, dokter.no_ijn_praktek, " +
                "pasien.nm_pasien, pasien.jk, pasien.tgl_lahir, pasien.no_peserta " +
                "FROM reg_periksa " +
                "INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis " +
                "INNER JOIN dokter ON reg_periksa.kd_dokter=dokter.kd_dokter " +
                "WHERE reg_periksa.no_rawat=?"
            );
            ps.setString(1, noRawat);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtNoRM.setText(rs.getString("no_rkm_medis"));
                txtNmPasien.setText(rs.getString("nm_pasien"));
                txtJK.setText(rs.getString("jk"));
                txtTglLahir.setText(rs.getString("tgl_lahir"));
                txtNoPeserta.setText(rs.getString("no_peserta") == null ? "" : rs.getString("no_peserta"));
                txtTglMasuk.setText(rs.getString("tgl_registrasi"));
                txtKdDokter.setText(rs.getString("kd_dokter"));
                txtNmDokter.setText(rs.getString("nm_dokter"));
                txtSIPDokter.setText(rs.getString("no_ijn_praktek") == null ? "" : rs.getString("no_ijn_praktek"));
                txtPenandatanganPasien.setText(rs.getString("nm_pasien"));

                String statusLanjut = rs.getString("status_lanjut");
                if ("Ranap".equalsIgnoreCase(statusLanjut)) {
                    txtJenisPerawatan.setText("RAWAT INAP");
                } else {
                    txtJenisPerawatan.setText("RAWAT JALAN");
                }
            }
            rs.close();
            ps.close();

            // 2. Data SEP (jika ada)
            ps = koneksi.prepareStatement(
                "SELECT no_sep, no_kartu, klsrawat, jnspelayanan, tglsep, tglpulang, nmdpdjp, nmdpjplayanan " +
                "FROM bridging_sep WHERE no_rawat=? ORDER BY tglsep DESC LIMIT 1"
            );
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            if (rs.next()) {
                txtNoSEP.setText(rs.getString("no_sep"));
                if (txtNoPeserta.getText().isEmpty() && rs.getString("no_kartu") != null) {
                    txtNoPeserta.setText(rs.getString("no_kartu"));
                }
                if (rs.getString("klsrawat") != null && !rs.getString("klsrawat").isEmpty()) {
                    txtKelas.setText(rs.getString("klsrawat"));
                }
                if (rs.getString("jnspelayanan") != null) {
                    if ("1".equals(rs.getString("jnspelayanan"))) {
                        txtJenisPerawatan.setText("RAWAT INAP");
                    } else if ("2".equals(rs.getString("jnspelayanan"))) {
                        txtJenisPerawatan.setText("RAWAT JALAN");
                    }
                }
                if (rs.getString("tglpulang") != null && !rs.getString("tglpulang").contains("0000")) {
                    String tp = rs.getString("tglpulang");
                    if (tp.length() >= 10) txtTglKeluar.setText(tp.substring(0, 10));
                }
            }
            rs.close();
            ps.close();

            // 3. Data Rawat Inap (kamar_inap & DPJP Ranap jika Ranap)
            if ("RAWAT INAP".equalsIgnoreCase(txtJenisPerawatan.getText())) {
                ps = koneksi.prepareStatement(
                    "SELECT kamar_inap.tgl_masuk, kamar_inap.tgl_keluar, kamar_inap.stts_pulang, kamar.kelas " +
                    "FROM kamar_inap INNER JOIN kamar ON kamar_inap.kd_kamar=kamar.kd_kamar " +
                    "WHERE kamar_inap.no_rawat=? ORDER BY kamar_inap.tgl_masuk DESC LIMIT 1"
                );
                ps.setString(1, noRawat);
                rs = ps.executeQuery();
                if (rs.next()) {
                    if (rs.getString("tgl_masuk") != null) txtTglMasuk.setText(rs.getString("tgl_masuk"));
                    if (rs.getString("tgl_keluar") != null && !"0000-00-00".equals(rs.getString("tgl_keluar"))) {
                        txtTglKeluar.setText(rs.getString("tgl_keluar"));
                    }
                    if (txtKelas.getText().isEmpty() && rs.getString("kelas") != null) {
                        txtKelas.setText(rs.getString("kelas").replace("Kelas ", ""));
                    }
                    String stts = rs.getString("stts_pulang");
                    if (stts != null) {
                        if (stts.toLowerCase().contains("sembuh") || stts.toLowerCase().contains("membaik") || stts.toLowerCase().contains("sehat")) {
                            cmbCaraPulang.setSelectedItem("SEMBUH");
                        } else if (stts.toLowerCase().contains("paksa") || stts.toLowerCase().contains("aps") || stts.toLowerCase().contains("sendiri")) {
                            cmbCaraPulang.setSelectedItem("PULANG PAKSA");
                        } else if (stts.toLowerCase().contains("rujuk")) {
                            cmbCaraPulang.setSelectedItem("RUJUK");
                        } else if (stts.toLowerCase().contains("meninggal") || stts.contains("+")) {
                            cmbCaraPulang.setSelectedItem("MENINGGAL DUNIA");
                        } else {
                            cmbCaraPulang.setSelectedItem("LAIN-LAIN");
                        }
                    }
                }
                rs.close();
                ps.close();

                // Cek DPJP Ranap
                ps = koneksi.prepareStatement(
                    "SELECT dpjp_ranap.kd_dokter, dokter.nm_dokter, dokter.no_ijn_praktek " +
                    "FROM dpjp_ranap INNER JOIN dokter ON dpjp_ranap.kd_dokter=dokter.kd_dokter " +
                    "WHERE dpjp_ranap.no_rawat=? LIMIT 1"
                );
                ps.setString(1, noRawat);
                rs = ps.executeQuery();
                if (rs.next()) {
                    txtKdDokter.setText(rs.getString("kd_dokter"));
                    txtNmDokter.setText(rs.getString("nm_dokter"));
                    txtSIPDokter.setText(rs.getString("no_ijn_praktek") == null ? "" : rs.getString("no_ijn_praktek"));
                }
                rs.close();
                ps.close();
            }

            // 4. Hitung Total Billing
            double totalBilling = 0;
            ps = koneksi.prepareStatement(
                "SELECT SUM(totalbiaya) as total FROM billing WHERE no_rawat=?"
            );
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            if (rs.next()) {
                totalBilling = rs.getDouble("total");
            }
            rs.close();
            ps.close();
            txtTotalTarif.setText(df.format(totalBilling));

            // 5. Diagnosa ICD-10
            ps = koneksi.prepareStatement(
                "SELECT diagnosa_pasien.prioritas, diagnosa_pasien.kd_penyakit, penyakit.nm_penyakit " +
                "FROM diagnosa_pasien INNER JOIN penyakit ON diagnosa_pasien.kd_penyakit=penyakit.kd_penyakit " +
                "WHERE diagnosa_pasien.no_rawat=? ORDER BY diagnosa_pasien.prioritas ASC"
            );
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            int idxDiag = 0;
            while (rs.next()) {
                idxDiag++;
                String kd = rs.getString("kd_penyakit");
                String nm = rs.getString("nm_penyakit");
                if (idxDiag == 1) {
                    txtKdDiagPrimer.setText(kd);
                    txtNmDiagPrimer.setText(nm);
                } else if (idxDiag == 2) {
                    txtKdDiagSekunder1.setText(kd);
                    txtNmDiagSekunder1.setText(nm);
                } else if (idxDiag == 3) {
                    txtKdDiagSekunder2.setText(kd);
                    txtNmDiagSekunder2.setText(nm);
                } else if (idxDiag == 4) {
                    txtKdDiagSekunder3.setText(kd);
                    txtNmDiagSekunder3.setText(nm);
                } else if (idxDiag == 5) {
                    txtKdDiagSekunder4.setText(kd);
                    txtNmDiagSekunder4.setText(nm);
                } else if (idxDiag == 6) {
                    txtKdDiagSekunder5.setText(kd);
                    txtNmDiagSekunder5.setText(nm);
                }
            }
            rs.close();
            ps.close();

            // 6. Prosedur ICD-9
            ps = koneksi.prepareStatement(
                "SELECT prosedur_pasien.prioritas, prosedur_pasien.kode, icd9.deskripsi_panjang " +
                "FROM prosedur_pasien INNER JOIN icd9 ON prosedur_pasien.kode=icd9.kode " +
                "WHERE prosedur_pasien.no_rawat=? ORDER BY prosedur_pasien.prioritas ASC"
            );
            ps.setString(1, noRawat);
            rs = ps.executeQuery();
            int idxProc = 0;
            while (rs.next()) {
                idxProc++;
                String kd = rs.getString("kode");
                String nm = rs.getString("deskripsi_panjang");
                if (idxProc == 1) {
                    txtKdTindakanPrimer.setText(kd);
                    txtNmTindakanPrimer.setText(nm);
                } else if (idxProc == 2) {
                    txtKdTindakanSekunder1.setText(kd);
                    txtNmTindakanSekunder1.setText(nm);
                } else if (idxProc == 3) {
                    txtKdTindakanSekunder2.setText(kd);
                    txtNmTindakanSekunder2.setText(nm);
                } else if (idxProc == 4) {
                    txtKdTindakanSekunder3.setText(kd);
                    txtNmTindakanSekunder3.setText(nm);
                } else if (idxProc == 5) {
                    txtKdTindakanSekunder4.setText(kd);
                    txtNmTindakanSekunder4.setText(nm);
                } else if (idxProc == 6) {
                    txtKdTindakanSekunder5.setText(kd);
                    txtNmTindakanSekunder5.setText(nm);
                }
            }
            rs.close();
            ps.close();

            // 7. Pemeriksaan Penunjang (Radiologi & Laboratorium)
            List<String[]> listPenunjang = new ArrayList<>();
            // Radiologi
            try {
                PreparedStatement psRad = koneksi.prepareStatement(
                    "SELECT periksa_radiologi.kd_jenis_prw, jns_perawatan_radiologi.nm_perawatan " +
                    "FROM periksa_radiologi " +
                    "INNER JOIN jns_perawatan_radiologi ON periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw " +
                    "WHERE periksa_radiologi.no_rawat=? ORDER BY periksa_radiologi.tgl_periksa, periksa_radiologi.jam"
                );
                psRad.setString(1, noRawat);
                ResultSet rsRad = psRad.executeQuery();
                while (rsRad.next()) {
                    String kd = rsRad.getString("kd_jenis_prw");
                    String nm = rsRad.getString("nm_perawatan");
                    boolean exists = false;
                    for (String[] item : listPenunjang) {
                        if (item[0].equalsIgnoreCase(kd)) { exists = true; break; }
                    }
                    if (!exists) {
                        listPenunjang.add(new String[]{kd, nm});
                    }
                }
                rsRad.close();
                psRad.close();
            } catch (Exception eRad) {
                System.out.println("Error autoFill Penunjang Radiologi: " + eRad);
            }

            // Laboratorium
            try {
                PreparedStatement psLab = koneksi.prepareStatement(
                    "SELECT periksa_lab.kd_jenis_prw, jns_perawatan_lab.nm_perawatan " +
                    "FROM periksa_lab " +
                    "INNER JOIN jns_perawatan_lab ON periksa_lab.kd_jenis_prw=jns_perawatan_lab.kd_jenis_prw " +
                    "WHERE periksa_lab.no_rawat=? ORDER BY periksa_lab.tgl_periksa, periksa_lab.jam"
                );
                psLab.setString(1, noRawat);
                ResultSet rsLab = psLab.executeQuery();
                while (rsLab.next()) {
                    String kd = rsLab.getString("kd_jenis_prw");
                    String nm = rsLab.getString("nm_perawatan");
                    boolean exists = false;
                    for (String[] item : listPenunjang) {
                        if (item[0].equalsIgnoreCase(kd)) { exists = true; break; }
                    }
                    if (!exists) {
                        listPenunjang.add(new String[]{kd, nm});
                    }
                }
                rsLab.close();
                psLab.close();
            } catch (Exception eLab) {
                System.out.println("Error autoFill Penunjang Lab: " + eLab);
            }

            if (listPenunjang.size() > 0) {
                txtKdPenunjangPrimer.setText(listPenunjang.get(0)[0]);
                txtNmPenunjangPrimer.setText(listPenunjang.get(0)[1]);
            }
            if (listPenunjang.size() > 1) {
                txtKdPenunjangSekunder1.setText(listPenunjang.get(1)[0]);
                txtNmPenunjangSekunder1.setText(listPenunjang.get(1)[1]);
            }
            if (listPenunjang.size() > 2) {
                txtKdPenunjangSekunder2.setText(listPenunjang.get(2)[0]);
                txtNmPenunjangSekunder2.setText(listPenunjang.get(2)[1]);
            }
            if (listPenunjang.size() > 3) {
                txtKdPenunjangSekunder3.setText(listPenunjang.get(3)[0]);
                txtNmPenunjangSekunder3.setText(listPenunjang.get(3)[1]);
            }
            if (listPenunjang.size() > 4) {
                txtKdPenunjangSekunder4.setText(listPenunjang.get(4)[0]);
                txtNmPenunjangSekunder4.setText(listPenunjang.get(4)[1]);
            }
            if (listPenunjang.size() > 5) {
                txtKdPenunjangSekunder5.setText(listPenunjang.get(5)[0]);
                txtNmPenunjangSekunder5.setText(listPenunjang.get(5)[1]);
            }

        } catch (Exception e) {
            System.out.println("Error autoFill Lembar INA: " + e);
        }
    }

    private void simpan() {
        if (txtNoRawat.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No. Rawat tidak boleh kosong!");
            return;
        }

        // Serialisasi Checklist CMG
        List<String> listSP = new ArrayList<>();
        if (cbPCL.isSelected()) listSP.add("PCL");
        if (cbColangiography.isSelected()) listSP.add("Colangiography");
        if (cbRepairSeptal.isSelected()) listSP.add("Repair Of Septal Defect Of Heart With Protheis");
        if (cbTimekotomy.isSelected()) listSP.add("Timekotomi");
        if (cbKeratoplasty.isSelected()) listSP.add("Keratoplasty");
        if (cbTorakotomi.isSelected()) listSP.add("Torakotomi");
        if (cbTumorPineal.isSelected()) listSP.add("Tumor Pineal Endoscopy");
        if (cbVitrectomy.isSelected()) listSP.add("Vitrectomy");
        if (cbPhacoemulsification.isSelected()) listSP.add("Phacoemulsification");
        if (cbMicrolaringoscopi.isSelected()) listSP.add("Microlaringoscopi");
        if (cbHipKnee.isSelected()) listSP.add("HIP Replacement/Knee Replacement");
        if (cbAirPlumbage.isSelected()) listSP.add("Air Plumbage");
        if (cbLobektomi.isSelected()) listSP.add("Lobektomi/Bilobektomi");
        if (cbStereotactic.isSelected()) listSP.add("Stereotactic Surgery Radiotherapy");
        if (cbPancreatectomy.isSelected()) listSP.add("Pancreatectomy");

        List<String> listSD = new ArrayList<>();
        if (cbStreptokinase.isSelected()) listSD.add("Streptokinase");
        if (cbDeferifrone.isSelected()) listSD.add("Deferifrone");
        if (cbDeferoksamin.isSelected()) listSD.add("Deferoksamin");
        if (cbDeferasirox.isSelected()) listSD.add("Deferasirox");
        if (cbHumanAlbumin.isSelected()) listSD.add("Human Albumin");

        List<String> listSI = new ArrayList<>();
        if (cbOtherCTScan.isSelected()) listSI.add("Other CT Scan");
        if (cbNuclearMedicine.isSelected()) listSI.add("Nuclear Medicine");
        if (cbMRI.isSelected()) listSI.add("MRI");
        if (cbDiagnosticEye.isSelected()) listSI.add("Diagnostic and Imaging Prosedure Of eye");

        List<String> listSProt = new ArrayList<>();
        if (cbSubduratGrid.isSelected()) listSProt.add("Subdurat Grid Electrode");
        if (cbCoteGraft.isSelected()) listSProt.add("Cote Graft");
        if (cbLiquidEmbolic.isSelected()) listSProt.add("Liquid Embolic (For AVM)");
        if (cbHipImplant.isSelected()) listSProt.add("HIP Implant/Knee Implant");

        double tarif = 0;
        try {
            tarif = Double.parseDouble(txtTotalTarif.getText().trim().replace(".", "").replace(",", "."));
        } catch (Exception ignored) {}

        try {
            String checkSql = "SELECT count(*) FROM lembar_bukti_pelayanan_inacbg WHERE no_rawat=?";
            PreparedStatement psCheck = koneksi.prepareStatement(checkSql);
            psCheck.setString(1, txtNoRawat.getText().trim());
            ResultSet rsCheck = psCheck.executeQuery();
            boolean exists = false;
            if (rsCheck.next()) {
                exists = rsCheck.getInt(1) > 0;
            }
            rsCheck.close();
            psCheck.close();

            String query;
            if (exists) {
                query = "UPDATE lembar_bukti_pelayanan_inacbg SET " +
                    "tgl_registrasi=?, no_sep=?, no_peserta=?, tgl_masuk=?, tgl_keluar=?, " +
                    "jenis_perawatan=?, kelas=?, cara_pulang=?, kd_dokter=?, dokter_penanggung_jawab=?, " +
                    "sip_dokter=?, berat_lahir=?, total_tarif=?, adl_skor=?, spesial_prosedur=?, " +
                    "spesial_drug=?, spesial_investigation=?, spesial_protheis=?, " +
                    "diagnosa_primer=?, kd_diagnosa_primer=?, diagnosa_sekunder1=?, kd_diagnosa_sekunder1=?, " +
                    "diagnosa_sekunder2=?, kd_diagnosa_sekunder2=?, diagnosa_sekunder3=?, kd_diagnosa_sekunder3=?, " +
                    "diagnosa_sekunder4=?, kd_diagnosa_sekunder4=?, diagnosa_sekunder5=?, kd_diagnosa_sekunder5=?, " +
                    "tindakan_primer=?, kd_tindakan_primer=?, tindakan_sekunder1=?, kd_tindakan_sekunder1=?, " +
                    "tindakan_sekunder2=?, kd_tindakan_sekunder2=?, tindakan_sekunder3=?, kd_tindakan_sekunder3=?, " +
                    "tindakan_sekunder4=?, kd_tindakan_sekunder4=?, tindakan_sekunder5=?, kd_tindakan_sekunder5=?, " +
                    "penunjang_primer=?, kd_penunjang_primer=?, penunjang_sekunder1=?, kd_penunjang_sekunder1=?, " +
                    "penunjang_sekunder2=?, kd_penunjang_sekunder2=?, penunjang_sekunder3=?, kd_penunjang_sekunder3=?, " +
                    "penunjang_sekunder4=?, kd_penunjang_sekunder4=?, penunjang_sekunder5=?, kd_penunjang_sekunder5=?, " +
                    "nama_pasien_keluarga=? WHERE no_rawat=?";
            } else {
                query = "INSERT INTO lembar_bukti_pelayanan_inacbg (" +
                    "tgl_registrasi, no_sep, no_peserta, tgl_masuk, tgl_keluar, " +
                    "jenis_perawatan, kelas, cara_pulang, kd_dokter, dokter_penanggung_jawab, " +
                    "sip_dokter, berat_lahir, total_tarif, adl_skor, spesial_prosedur, " +
                    "spesial_drug, spesial_investigation, spesial_protheis, " +
                    "diagnosa_primer, kd_diagnosa_primer, diagnosa_sekunder1, kd_diagnosa_sekunder1, " +
                    "diagnosa_sekunder2, kd_diagnosa_sekunder2, diagnosa_sekunder3, kd_diagnosa_sekunder3, " +
                    "diagnosa_sekunder4, kd_diagnosa_sekunder4, diagnosa_sekunder5, kd_diagnosa_sekunder5, " +
                    "tindakan_primer, kd_tindakan_primer, tindakan_sekunder1, kd_tindakan_sekunder1, " +
                    "tindakan_sekunder2, kd_tindakan_sekunder2, tindakan_sekunder3, kd_tindakan_sekunder3, " +
                    "tindakan_sekunder4, kd_tindakan_sekunder4, tindakan_sekunder5, kd_tindakan_sekunder5, " +
                    "penunjang_primer, kd_penunjang_primer, penunjang_sekunder1, kd_penunjang_sekunder1, " +
                    "penunjang_sekunder2, kd_penunjang_sekunder2, penunjang_sekunder3, kd_penunjang_sekunder3, " +
                    "penunjang_sekunder4, kd_penunjang_sekunder4, penunjang_sekunder5, kd_penunjang_sekunder5, " +
                    "nama_pasien_keluarga, no_rawat) VALUES (" +
                    "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            }

            PreparedStatement ps = koneksi.prepareStatement(query);
            ps.setString(1, txtTglMasuk.getText().trim().isEmpty() ? null : txtTglMasuk.getText().trim());
            ps.setString(2, txtNoSEP.getText().trim());
            ps.setString(3, txtNoPeserta.getText().trim());
            ps.setString(4, txtTglMasuk.getText().trim().isEmpty() ? null : txtTglMasuk.getText().trim());
            ps.setString(5, txtTglKeluar.getText().trim().isEmpty() ? null : txtTglKeluar.getText().trim());
            ps.setString(6, txtJenisPerawatan.getText().trim());
            ps.setString(7, txtKelas.getText().trim());
            ps.setString(8, cmbCaraPulang.getSelectedItem().toString());
            ps.setString(9, txtKdDokter.getText().trim());
            ps.setString(10, txtNmDokter.getText().trim());
            ps.setString(11, txtSIPDokter.getText().trim());
            ps.setString(12, txtBeratLahir.getText().trim());
            ps.setDouble(13, tarif);
            ps.setString(14, txtSkorADL.getText().trim());
            ps.setString(15, String.join(", ", listSP));
            ps.setString(16, String.join(", ", listSD));
            ps.setString(17, String.join(", ", listSI));
            ps.setString(18, String.join(", ", listSProt));

            ps.setString(19, txtNmDiagPrimer.getText().trim());
            ps.setString(20, txtKdDiagPrimer.getText().trim());
            ps.setString(21, txtNmDiagSekunder1.getText().trim());
            ps.setString(22, txtKdDiagSekunder1.getText().trim());
            ps.setString(23, txtNmDiagSekunder2.getText().trim());
            ps.setString(24, txtKdDiagSekunder2.getText().trim());
            ps.setString(25, txtNmDiagSekunder3.getText().trim());
            ps.setString(26, txtKdDiagSekunder3.getText().trim());
            ps.setString(27, txtNmDiagSekunder4.getText().trim());
            ps.setString(28, txtKdDiagSekunder4.getText().trim());
            ps.setString(29, txtNmDiagSekunder5.getText().trim());
            ps.setString(30, txtKdDiagSekunder5.getText().trim());

            ps.setString(31, txtNmTindakanPrimer.getText().trim());
            ps.setString(32, txtKdTindakanPrimer.getText().trim());
            ps.setString(33, txtNmTindakanSekunder1.getText().trim());
            ps.setString(34, txtKdTindakanSekunder1.getText().trim());
            ps.setString(35, txtNmTindakanSekunder2.getText().trim());
            ps.setString(36, txtKdTindakanSekunder2.getText().trim());
            ps.setString(37, txtNmTindakanSekunder3.getText().trim());
            ps.setString(38, txtKdTindakanSekunder3.getText().trim());
            ps.setString(39, txtNmTindakanSekunder4.getText().trim());
            ps.setString(40, txtKdTindakanSekunder4.getText().trim());
            ps.setString(41, txtNmTindakanSekunder5.getText().trim());
            ps.setString(42, txtKdTindakanSekunder5.getText().trim());

            ps.setString(43, txtNmPenunjangPrimer.getText().trim());
            ps.setString(44, txtKdPenunjangPrimer.getText().trim());
            ps.setString(45, txtNmPenunjangSekunder1.getText().trim());
            ps.setString(46, txtKdPenunjangSekunder1.getText().trim());
            ps.setString(47, txtNmPenunjangSekunder2.getText().trim());
            ps.setString(48, txtKdPenunjangSekunder2.getText().trim());
            ps.setString(49, txtNmPenunjangSekunder3.getText().trim());
            ps.setString(50, txtKdPenunjangSekunder3.getText().trim());
            ps.setString(51, txtNmPenunjangSekunder4.getText().trim());
            ps.setString(52, txtKdPenunjangSekunder4.getText().trim());
            ps.setString(53, txtNmPenunjangSekunder5.getText().trim());
            ps.setString(54, txtKdPenunjangSekunder5.getText().trim());

            ps.setString(55, txtPenandatanganPasien.getText().trim());
            ps.setString(56, txtNoRawat.getText().trim());

            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(null, "Data Berkas INA berhasil disimpan!");
        } catch (Exception e) {
            System.out.println("Error simpan Lembar INA: " + e);
            JOptionPane.showMessageDialog(null, "Gagal menyimpan data: " + e.getMessage());
        }
    }

    private void hapus() {
        if (txtNoRawat.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No. Rawat kosong, tidak ada data yang dihapus!");
            return;
        }

        int jwb = JOptionPane.showConfirmDialog(null, "Yakin data Lembar INA untuk No. Rawat " + txtNoRawat.getText() + " akan dihapus?", "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION);
        if (jwb == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement ps = koneksi.prepareStatement(
                    "DELETE FROM lembar_bukti_pelayanan_inacbg WHERE no_rawat=?"
                );
                ps.setString(1, txtNoRawat.getText().trim());
                ps.executeUpdate();
                ps.close();
                JOptionPane.showMessageDialog(null, "Data berhasil dihapus!");
                emptTeks();
            } catch (Exception e) {
                System.out.println("Error hapus Lembar INA: " + e);
                JOptionPane.showMessageDialog(null, "Gagal menghapus data: " + e.getMessage());
            }
        }
    }

    /**
     * Dispatcher konten HTML berdasarkan pilihan Model Cetakan aktif
     */
    private String buildHTMLContent() {
        int modelIdx = (cmbModelCetakTab1 != null) ? cmbModelCetakTab1.getSelectedIndex() : 0;
        if (modelIdx == 1) {
            return buildHTMLContentModel1();
        } else {
            return buildHTMLContentModel2();
        }
    }

    /**
     * MODEL 1: Format "tabel resmi" 1 halaman, mengikuti acuan
     * penyesuaian/FORM INA 2/rapi_berkas_ina.html (referensi yang sudah dirapikan).
     * A4, margin 1.27cm, Times New Roman. Kop 11pt tanpa kotak + garis ganda,
     * identitas pasien tanpa border, blok CMG 9pt, checkbox identik Model 2.
     */
    private String buildHTMLContentModel1() {
        String tglMasukRaw = txtTglMasuk.getText().trim();
        String tahun = tglMasukRaw.length() >= 4 ? tglMasukRaw.substring(0, 4)
                : new SimpleDateFormat("yyyy").format(new Date());

        String kodeRS = akses.getkodeppkkemenkes() != null ? akses.getkodeppkkemenkes() : "";
        String namaRS = akses.getnamars() != null ? akses.getnamars() : "";
        String alamatRS = akses.getalamatrs() != null ? akses.getalamatrs() : "";
        String kabProp = (akses.getkabupatenrs() != null ? akses.getkabupatenrs() : "")
                + " / " + (akses.getpropinsirs() != null ? akses.getpropinsirs() : "");

        String caraPulangVal = cmbCaraPulang.getSelectedItem() == null ? ""
                : cmbCaraPulang.getSelectedItem().toString();

        String jkText = "L".equalsIgnoreCase(txtJK.getText().trim()) ? "Laki-laki"
                : ("P".equalsIgnoreCase(txtJK.getText().trim()) ? "Perempuan" : txtJK.getText().trim());

        StringBuilder sb = new StringBuilder(16384);
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset='utf-8'/>\n<style>\n");
        sb.append("@page { size: 595.3pt 841.9pt; margin: 36pt 36pt 36pt 36pt; }\n");
        sb.append("body { font-family: 'Times New Roman', 'Liberation Serif', serif;"
                + " font-size: 9pt; line-height: 1.1; margin: 0; padding: 0; color: #000; }\n");
        sb.append("table { border-collapse: collapse; width: 100%; table-layout: fixed;"
                + " page-break-inside: avoid; }\n");
        sb.append("td { vertical-align: top; font-size: 9pt; padding: 0; }\n");
        sb.append(".b { font-weight: bold; }\n");
        sb.append(".c { text-align: center; }\n");
        // Kop 11pt
        sb.append(".kop-t { font-size: 11pt; font-weight: bold; line-height: 1.2; letter-spacing: 0.2pt; }\n");
        sb.append(".kop-l { font-size: 11pt; font-weight: bold; white-space: nowrap; }\n");
        sb.append(".kop-v { font-size: 11pt; font-weight: bold; white-space: nowrap; }\n");
        sb.append(".kop-v2 { font-size: 10pt; font-weight: bold; white-space: nowrap; }\n");
        sb.append(".kop-v3 { font-size: 9pt; font-weight: bold; white-space: nowrap; }\n");
        sb.append(".kop-vw { font-size: 9pt; font-weight: bold; word-wrap: break-word; }\n");
        sb.append(".rt { border-top: 0.7pt solid #000; height: 0.1pt; margin-top: 5pt; }\n");
        sb.append(".rk { border-top: 2.2pt solid #000; height: 0.1pt; margin-top: 1.4pt; }\n");
        // Tabel bergaris
        sb.append(".bx td { border: 0.6pt solid #000; padding: 1.4pt 3pt; font-size: 9pt; }\n");
        sb.append(".bx th { border: 0.6pt solid #000; padding: 1.8pt 3pt;"
                + " text-align: left; font-weight: bold; font-size: 9pt; }\n");
        // Identitas pasien: TANPA border
        sb.append(".nb td { border: none; padding: 0.6pt 0; font-size: 9pt; }\n");
        sb.append(".sec { font-size: 11pt; font-weight: bold; margin-top: 7pt; margin-bottom: 1pt; }\n");
        sb.append(".secr { border-top: 0.7pt solid #000; height: 0.1pt; margin-bottom: 4pt; }\n");
        sb.append(".sub { font-size: 10pt; font-weight: bold; margin-bottom: 2pt; }\n");
        // Blok CMG 9pt
        sb.append(".cmg td { border: none; padding: 0.4pt 6pt 0.4pt 0; font-size: 9pt; }\n");
        // Checkbox identik Model 2
        sb.append(".cb { display: inline-block; width: 7.2pt; height: 7.2pt; border: 0.6pt solid #000;"
                + " vertical-align: -1.1pt; margin-right: 2.2pt; text-align: center;"
                + " font-size: 6.6pt; font-weight: bold; line-height: 7.2pt; }\n");
        sb.append("</style>\n</head>\n<body>\n");

        // ==================== KOP ====================
        sb.append("<table>\n");
        sb.append("<colgroup><col style='width:38%'/><col style='width:62%'/></colgroup>\n");
        sb.append("<tr>\n");
        sb.append("<td class='kop-t'>LEMBAR BUKTI PELAYANAN<br/>VARIABEL INACBG<br/>TAHUN ")
                .append(tahun).append("</td>\n");
        sb.append("<td>\n<table>\n");
        sb.append("<colgroup><col style='width:26%'/><col style='width:4%'/><col style='width:70%'/></colgroup>\n");
        appendKopRow(sb, "Kode RS", kodeRS);
        appendKopRow(sb, "Nama RS", namaRS);
        appendKopRow(sb, "Alamat", alamatRS);
        appendKopRow(sb, "Kab / Prop", kabProp);
        sb.append("</table>\n</td>\n</tr>\n</table>\n");

        sb.append("<div class='rt'></div>\n<div class='rk'></div>\n");

        // ==================== IDENTITAS PASIEN (tanpa border) ====================
        sb.append("<table class='nb' style='margin-top:5pt'>\n");
        sb.append("<colgroup><col style='width:22%'/><col style='width:2%'/><col style='width:26%'/>"
                + "<col style='width:22%'/><col style='width:2%'/><col style='width:26%'/></colgroup>\n");

        appendAdmPair(sb, "Nomor RM", txtNoRM.getText(), "Jenis kelamin", jkText);
        appendAdmSpan(sb, "Nama Lengkap", txtNmPasien.getText());
        appendAdmPair(sb, "Tanggal Lahir", formatTglINA(txtTglLahir.getText()),
                "Nomor Peserta", txtNoPeserta.getText());
        appendAdmSpan(sb, "Nomor SEP", txtNoSEP.getText());
        appendAdmPair(sb, "Jenis Perawatan", txtJenisPerawatan.getText(), "Kelas", txtKelas.getText());
        appendAdmPair(sb, "Tanggal Masuk", formatTglINA(txtTglMasuk.getText()),
                "Tanggal Keluar", formatTglINA(txtTglKeluar.getText()));

        sb.append("<tr><td class='b'>Cara Pulang</td><td>:</td><td colspan='4'>");
        sb.append(cb1("SEMBUH".equalsIgnoreCase(caraPulangVal))).append("Sembuh&nbsp;&nbsp;");
        sb.append(cb1("PULANG PAKSA".equalsIgnoreCase(caraPulangVal))).append("Pulang Paksa&nbsp;&nbsp;");
        sb.append(cb1("RUJUK".equalsIgnoreCase(caraPulangVal))).append("Rujuk&nbsp;&nbsp;");
        sb.append(cb1("MENINGGAL DUNIA".equalsIgnoreCase(caraPulangVal))).append("Meninggal Dunia");
        sb.append("</td></tr>\n");

        appendAdmSpan(sb, "DPJP", txtNmDokter.getText());
        appendAdmPair(sb, "Berat Lahir", txtBeratLahir.getText().trim().isEmpty() ? "-" : txtBeratLahir.getText(),
                "Total Tarif RS", txtTotalTarif.getText());

        sb.append("<tr><td class='b'>ADL Skor (12 - 60)</td><td>:</td><td colspan='4'>")
                .append(txtSkorADL.getText()).append("</td></tr>\n");
        sb.append("</table>\n");

        // ==================== SPESIAL CMG ====================
        sb.append("<div class='sec'>Spesial CMG</div>\n<div class='secr'></div>\n");

        sb.append("<div class='sub'>1. Spesial Prosedur</div>\n");
        sb.append("<table class='cmg'>\n");
        sb.append("<colgroup><col style='width:29%'/><col style='width:33%'/><col style='width:38%'/></colgroup>\n");
        appendCmg1Row(sb,
                "PCL", cbPCL.isSelected(),
                "Colangiography", cbColangiography.isSelected(),
                "Repair Of Septal Defect Of Heart With Protheis", cbRepairSeptal.isSelected());
        appendCmg1Row(sb,
                "TIMEKOTOMY", cbTimekotomy.isSelected(),
                "Keratoplasty", cbKeratoplasty.isSelected(),
                "Torakotomi", cbTorakotomi.isSelected());
        appendCmg1Row(sb,
                "Tumor Pineal Endoscopy", cbTumorPineal.isSelected(),
                "Vitrectomy", cbVitrectomy.isSelected(),
                "Phacoemulsification", cbPhacoemulsification.isSelected());
        appendCmg1Row(sb,
                "Microlaringoscopi", cbMicrolaringoscopi.isSelected(),
                "HIP / Knee Replacement", cbHipKnee.isSelected(),
                "Air Plumbage", cbAirPlumbage.isSelected());
        appendCmg1Row(sb,
                "Lobektomi / Bilobektomi", cbLobektomi.isSelected(),
                "Stereotactic Surgery Radiotherapy", cbStereotactic.isSelected(),
                "Pancreatectomy", cbPancreatectomy.isSelected());
        sb.append("</table>\n");

        // --- 2. DRUG & 3. INVESTIGATION berdampingan ---
        sb.append("<table style='margin-top:6pt'>\n");
        sb.append("<colgroup><col style='width:50%'/><col style='width:50%'/></colgroup>\n");
        sb.append("<tr>\n");

        sb.append("<td style='padding-right:8pt'>\n<div class='sub'>2. Spesial Drug</div>\n");
        sb.append("<table class='cmg'>\n<colgroup><col style='width:50%'/><col style='width:50%'/></colgroup>\n");
        appendCmg2Row(sb, "Streptokinase", cbStreptokinase.isSelected(), "Deferifrone", cbDeferifrone.isSelected());
        appendCmg2Row(sb, "Deferoksamin", cbDeferoksamin.isSelected(), "Deferasirox", cbDeferasirox.isSelected());
        appendCmg2Row(sb, "Human Albumin", cbHumanAlbumin.isSelected(), null, false);
        sb.append("</table>\n</td>\n");

        sb.append("<td style='padding-left:8pt'>\n<div class='sub'>3. Spesial Investigation</div>\n");
        sb.append("<table class='cmg'>\n<colgroup><col style='width:50%'/><col style='width:50%'/></colgroup>\n");
        appendCmg2Row(sb, "Other CT Scan", cbOtherCTScan.isSelected(), "Nuclear Medicine", cbNuclearMedicine.isSelected());
        appendCmg2Row(sb, "MRI", cbMRI.isSelected(), "Diagnostic &amp; Imaging Eye", cbDiagnosticEye.isSelected());
        sb.append("</table>\n</td>\n</tr>\n</table>\n");

        // --- 4. PROTHEIS ---
        sb.append("<div class='sub' style='margin-top:6pt'>4. Spesial Protheis</div>\n");
        sb.append("<table class='cmg'>\n<colgroup><col style='width:50%'/><col style='width:50%'/></colgroup>\n");
        appendCmg2Row(sb, "Subdurat Grid Electrode", cbSubduratGrid.isSelected(), "Cote Graft", cbCoteGraft.isSelected());
        appendCmg2Row(sb, "Liquid Embolic (For AVM)", cbLiquidEmbolic.isSelected(), "HIP Implant / Knee Implant", cbHipImplant.isSelected());
        sb.append("</table>\n");

        // ==================== DIAGNOSA & TINDAKAN ====================
        sb.append("<div class='sec'>Diagnosa &amp; Tindakan</div>\n");
        sb.append("<div class='secr'></div>\n");

        sb.append("<table class='bx'>\n");
        sb.append("<colgroup><col style='width:38%'/><col style='width:12%'/>"
                + "<col style='width:38%'/><col style='width:12%'/></colgroup>\n");
        sb.append("<tr><th>Diagnosa (ICD-10)</th><th>Kode</th>"
                + "<th>Tindakan (ICD-9)</th><th>Kode</th></tr>\n");
        appendDiagTindRow(sb, "Primer", txtNmDiagPrimer.getText(), txtKdDiagPrimer.getText(),
                txtNmTindakanPrimer.getText(), txtKdTindakanPrimer.getText());
        appendDiagTindRow(sb, "Sekunder 1", txtNmDiagSekunder1.getText(), txtKdDiagSekunder1.getText(),
                txtNmTindakanSekunder1.getText(), txtKdTindakanSekunder1.getText());
        appendDiagTindRow(sb, "Sekunder 2", txtNmDiagSekunder2.getText(), txtKdDiagSekunder2.getText(),
                txtNmTindakanSekunder2.getText(), txtKdTindakanSekunder2.getText());
        appendDiagTindRow(sb, "Sekunder 3", txtNmDiagSekunder3.getText(), txtKdDiagSekunder3.getText(),
                txtNmTindakanSekunder3.getText(), txtKdTindakanSekunder3.getText());
        appendDiagTindRow(sb, "Sekunder 4", txtNmDiagSekunder4.getText(), txtKdDiagSekunder4.getText(),
                txtNmTindakanSekunder4.getText(), txtKdTindakanSekunder4.getText());
        appendDiagTindRow(sb, "Sekunder 5", txtNmDiagSekunder5.getText(), txtKdDiagSekunder5.getText(),
                txtNmTindakanSekunder5.getText(), txtKdTindakanSekunder5.getText());
        sb.append("</table>\n");

        // ==================== PEMERIKSAAN PENUNJANG ====================
        sb.append("<div class='sec'>PEMERIKSAAN PENUNJANG</div>\n");
        sb.append("<div class='secr'></div>\n");

        sb.append("<table class='bx'>\n");
        sb.append("<colgroup><col style='width:72%'/><col style='width:28%'/></colgroup>\n");
        sb.append("<tr><th>Pemeriksaan Penunjang</th><th>Kode Penunjang</th></tr>\n");
        appendPenunjangRow(sb, "Primer", txtNmPenunjangPrimer.getText(), txtKdPenunjangPrimer.getText());
        appendPenunjangRow(sb, "Sekunder 1", txtNmPenunjangSekunder1.getText(), txtKdPenunjangSekunder1.getText());
        appendPenunjangRow(sb, "Sekunder 2", txtNmPenunjangSekunder2.getText(), txtKdPenunjangSekunder2.getText());
        appendPenunjangRow(sb, "Sekunder 3", txtNmPenunjangSekunder3.getText(), txtKdPenunjangSekunder3.getText());
        appendPenunjangRow(sb, "Sekunder 4", txtNmPenunjangSekunder4.getText(), txtKdPenunjangSekunder4.getText());
        appendPenunjangRow(sb, "Sekunder 5", txtNmPenunjangSekunder5.getText(), txtKdPenunjangSekunder5.getText());
        sb.append("</table>\n");

        // ==================== TANDA TANGAN ====================
        String dokterTtd = txtNmDokter.getText().trim().isEmpty() ? "dr. ................................" : txtNmDokter.getText().trim();
        String sipStr = txtSIPDokter.getText().trim().isEmpty() ? "SIP. -"
                : (txtSIPDokter.getText().trim().startsWith("SIP") ? txtSIPDokter.getText().trim()
                : "SIP. " + txtSIPDokter.getText().trim());
        String pasienTtd = txtPenandatanganPasien.getText().trim().isEmpty() ? "(……………)"
                : txtPenandatanganPasien.getText().trim();

        sb.append("<table style='margin-top:14pt'>\n");
        sb.append("<colgroup><col style='width:50%'/><col style='width:50%'/></colgroup>\n");
        sb.append("<tr><td class='c'>Dokter Penanggung Jawab</td>");
        sb.append("<td class='c'>Pasien/Keluarga Pasien</td></tr>\n");
        sb.append("<tr><td style='height:38pt'></td><td style='height:38pt'></td></tr>\n");
        sb.append("<tr><td class='c'>").append(dokterTtd).append("<br/>").append(sipStr).append("</td>");
        sb.append("<td class='c'>").append(pasienTtd).append("</td></tr>\n");
        sb.append("</table>\n");

        sb.append("</body>\n</html>\n");
        return sb.toString();
    }

    /**
     * Baris kop: label | : | nilai.
     * Anti-overflow berlapis:
     *   1) label selalu nowrap (tidak pernah pecah baris);
     *   2) nilai nowrap selama masih muat;
     *   3) font diperkecil bertahap 14pt -> 12pt -> 10pt;
     *   4) bila tetap tidak muat, nilai diizinkan wrap DI DALAM selnya sendiri
     *      (tinggi baris bertambah, tetapi tidak pernah menembus margin halaman).
     * Kapasitas kolom nilai ~ 0.55 x 0.66 x 523pt = 190pt.
     * Rata-rata lebar karakter Times New Roman: 14pt ~ 6.4pt, 12pt ~ 5.5pt, 10pt ~ 4.6pt.
     */
    private void appendKopRow(StringBuilder sb, String label, String value) {
        String v = value == null ? "" : value.trim();
        int n = v.length();
        String cls;
        if (n <= 29) {
            cls = "kop-v";                 // 14pt nowrap
        } else if (n <= 34) {
            cls = "kop-v2";                // 12pt nowrap
        } else if (n <= 41) {
            cls = "kop-v3";                // 10pt nowrap
        } else {
            cls = "kop-vw";                // 10pt, wrap di dalam sel
        }
        sb.append("<tr><td class='kop-l'>").append(label).append("</td>");
        sb.append("<td class='kop-l'>:</td>");
        sb.append("<td class='").append(cls).append("'>").append(v).append("</td></tr>\n");
    }

    /** Baris admin: label | : | nilai | label | : | nilai. */
    private void appendAdmPair(StringBuilder sb, String l1, String v1, String l2, String v2) {
        sb.append("<tr><td class='b'>").append(l1).append("</td><td>:</td>");
        sb.append("<td>").append(v1 == null ? "" : v1.trim()).append("</td>");
        sb.append("<td class='b'>").append(l2).append("</td><td>:</td>");
        sb.append("<td>").append(v2 == null ? "" : v2.trim()).append("</td></tr>\n");
    }

    /** Baris admin: label | : | nilai (colspan 4). */
    private void appendAdmSpan(StringBuilder sb, String label, String value) {
        sb.append("<tr><td class='b'>").append(label).append("</td><td>:</td>");
        sb.append("<td colspan='4'>").append(value == null ? "" : value.trim()).append("</td></tr>\n");
    }

    /** Baris CMG 3 kolom (Model 1: tabel tanpa garis). */
    private void appendCmg1Row(StringBuilder sb, String t1, boolean v1, String t2, boolean v2, String t3, boolean v3) {
        sb.append("<tr><td>").append(cb1(v1)).append(t1).append("</td>");
        sb.append("<td>").append(cb1(v2)).append(t2).append("</td>");
        sb.append("<td>").append(cb1(v3)).append(t3).append("</td></tr>\n");
    }

    /** Baris CMG 2 kolom (Model 1). */
    private void appendCmg2Row(StringBuilder sb, String t1, boolean v1, String t2, boolean v2) {
        sb.append("<tr><td>").append(cb1(v1)).append(t1).append("</td>");
        sb.append("<td>").append(t2 == null ? "&nbsp;" : cb1(v2) + t2).append("</td></tr>\n");
    }

    /** Baris Diagnosa+Tindakan berdampingan; sel kosong diisi "-". */
    private void appendDiagTindRow(StringBuilder sb, String prefix, String dNama, String dKode,
                                   String tNama, String tKode) {
        String dn = dNama == null ? "" : dNama.trim();
        String dk = dKode == null ? "" : dKode.trim();
        String tn = tNama == null ? "" : tNama.trim();
        String tk = tKode == null ? "" : tKode.trim();

        sb.append("<tr>");
        sb.append("<td>").append(prefix).append(": ").append(dn.isEmpty() ? "-" : dn).append("</td>");
        sb.append("<td>").append(dk.isEmpty() ? "-" : dk).append("</td>");
        sb.append("<td>").append(prefix).append(": ").append(tn.isEmpty() ? "-" : tn).append("</td>");
        sb.append("<td>").append(tk.isEmpty() ? "-" : tk).append("</td>");
        sb.append("</tr>\n");
    }

    /** Baris penunjang; sel kosong diisi "-". */
    private void appendPenunjangRow(StringBuilder sb, String prefix, String nama, String kode) {
        String nm = nama == null ? "" : nama.trim();
        String kd = kode == null ? "" : kode.trim();
        sb.append("<tr><td>").append(prefix).append(": ").append(nm.isEmpty() ? "-" : nm).append("</td>");
        sb.append("<td>").append(kd.isEmpty() ? "-" : kd).append("</td></tr>\n");
    }

    /** Kotak checkbox Model 1: identik dengan Model 2 (tercentang berisi huruf X). */
    private String cb1(boolean selected) {
        return "<span class='cb'>" + (selected ? "X" : "") + "</span>";
    }

    /**
     * MODEL 2: Replika presisi dokumen acuan FORM INA.
     * A4 595.3 x 841.9 pt, margin 1cm, Times New Roman 10pt.
     * Tiap blok memakai tabel fixed-layout sendiri agar lebar kolom persis
     * mengikuti posisi acuan (47.38/15/37.62 - 37.85/25.53/12.66/23.95 -
     * 2.63/23.7/32/41.67 - 26.26/42.2/6.58/24.96).
     */
    private String buildHTMLContentModel2() {
        String tglMasukRaw = txtTglMasuk.getText().trim();
        String tahun = tglMasukRaw.length() >= 4 ? tglMasukRaw.substring(0, 4)
                : new SimpleDateFormat("yyyy").format(new Date());

        String kodeRS = akses.getkodeppkkemenkes() != null ? akses.getkodeppkkemenkes() : "";
        String namaRS = akses.getnamars() != null ? akses.getnamars() : "";
        String alamatRS = akses.getalamatrs() != null ? akses.getalamatrs() : "";
        String kabProp = (akses.getkabupatenrs() != null ? akses.getkabupatenrs() : "")
                + " / " + (akses.getpropinsirs() != null ? akses.getpropinsirs() : "");

        String caraPulangVal = cmbCaraPulang.getSelectedItem() == null ? ""
                : cmbCaraPulang.getSelectedItem().toString();

        StringBuilder sb = new StringBuilder(16384);
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset='utf-8'/>\n<style>\n");
        sb.append("@page { size: 595.3pt 841.9pt; margin: 1cm 1cm 1cm 1cm; }\n");
        sb.append("body { font-family: 'Times New Roman', 'Liberation Serif', serif;"
                + " font-size: 10pt; line-height: 1.12; margin: 0; padding: 0; color: #000; }\n");
        sb.append("table { border-collapse: collapse; width: 100%; table-layout: fixed; margin: 0; }\n");
        sb.append("td { padding: 0; vertical-align: top; font-size: 10pt; }\n");
        sb.append(".p { padding: 0 5.4pt; }\n");
        sb.append(".c { text-align: center; }\n");
        sb.append(".b { font-weight: bold; }\n");
        sb.append(".bx { border: 0.6pt solid #000; padding: 0 5.4pt; }\n");
        sb.append(".vr { border-right: 0.6pt solid #000; padding: 0 5.4pt; }\n");
        sb.append(".cb { display: inline-block; width: 7.2pt; height: 7.2pt;"
                + " border: 0.6pt solid #000; vertical-align: -1.1pt; margin-right: 2.2pt;"
                + " text-align: center; font-size: 6.6pt; font-weight: bold; line-height: 7.2pt; }\n");
        sb.append(".sp { height: 6pt; }\n");
        sb.append(".q { padding: 0 2pt; }\n");
        sb.append(".nw { white-space: nowrap; }\n");
        sb.append("tr.med td { line-height: 0.92; height: 11.5pt; }\n");
        sb.append("tr.gap td { height: 11.5pt; }\n");
        sb.append("</style>\n</head>\n<body>\n");

        // ==================== BLOK 1: KOP ====================
        sb.append("<table>\n");
        sb.append("<colgroup><col style='width:47.38%'/><col style='width:15%'/><col style='width:37.62%'/></colgroup>\n");
        String[] kopTitle = {"LEMBAR BUKTI PELAYANAN", "VARIABEL INACBG", "TAHUN " + tahun, ""};
        String[] kopLabel = {"KODE RS", "NAMA RS", "ALAMAT", "KAB.PROP"};
        String[] kopValue = {kodeRS, namaRS, alamatRS, kabProp};
        for (int i = 0; i < 4; i++) {
            sb.append("<tr>");
            sb.append("<td class='c b vr'>").append(kopTitle[i]).append("</td>");
            sb.append("<td class='bx'>").append(kopLabel[i]).append("</td>");
            sb.append("<td class='bx'>").append(kopValue[i]).append("</td>");
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");

        // ==================== BLOK 2: DATA ADMINISTRATIF ====================
        sb.append("<table style='margin-top:9pt;'>\n");
        sb.append("<colgroup><col style='width:36.86%'/><col style='width:25.5%'/>"
                + "<col style='width:12.7%'/><col style='width:24.94%'/></colgroup>\n");

        appendAdm4(sb, "NOMOR&nbsp; RM", txtNoRM.getText(), false, "Jenis Kelamin", txtJK.getText());
        appendAdmFull(sb, "NAMA LENGKAP", txtNmPasien.getText(), true);
        appendAdmFull(sb, "TANGGAL LAHIR", formatTglINA(txtTglLahir.getText()), false);
        appendAdmFull(sb, "NOMOR PESERTA", txtNoPeserta.getText(), true);
        appendAdmFull(sb, "NOMOR SEP", txtNoSEP.getText(), true);
        appendAdm4(sb, "JENIS PERAWATAN", txtJenisPerawatan.getText(), false, "Kelas", txtKelas.getText());
        appendAdmFull(sb, "TANGGAL MASUK", formatTglINA(txtTglMasuk.getText()), false);
        appendAdmFull(sb, "TANGGAL KELUAR", formatTglINA(txtTglKeluar.getText()), false);

        sb.append("<tr><td class='p'>CARA PULANG</td><td class='p' colspan='3'>: ");
        sb.append(cb("SEMBUH".equalsIgnoreCase(caraPulangVal))).append(" SEMBUH&nbsp; ");
        sb.append(cb("PULANG PAKSA".equalsIgnoreCase(caraPulangVal))).append(" PULANG PAKSA&nbsp; ");
        sb.append(cb("RUJUK".equalsIgnoreCase(caraPulangVal))).append(" RUJUK&nbsp; ");
        sb.append(cb("MENINGGAL DUNIA".equalsIgnoreCase(caraPulangVal))).append(" MENINGGAL DUNIA");
        sb.append("</td></tr>\n");

        appendAdmFull(sb, "DOKTER PENANGGUNG JAWAB", txtNmDokter.getText(), false);
        appendAdmFull(sb, "BERAT LAHIR", txtBeratLahir.getText(), false);
        appendAdmFull(sb, "TOTAL TARIF RS", txtTotalTarif.getText(), false);

        sb.append("<tr><td class='p b'>ADL (Activity Daily Living) Skor 12 s/d 60</td>");
        sb.append("<td class='p' colspan='3'>: ").append(txtSkorADL.getText()).append("</td></tr>\n");
        appendAdmFull(sb, "Special CMG", "", false);
        sb.append("</table>\n");

        // ==================== BLOK 3: SPECIAL CMG ====================
        // 3 kolom: 2.63 | 23.7 | 32 | 41.67
        sb.append("<table style='margin-top:2pt;'>\n");
        sb.append("<colgroup><col style='width:2.2%'/><col style='width:25.4%'/>"
                + "<col style='width:32.2%'/><col style='width:40.2%'/></colgroup>\n");

        appendSectionRow(sb, "1.&nbsp; Special Prosedur");
        appendCmg3(sb, "PCL", cbPCL.isSelected(), "Colangiography", cbColangiography.isSelected(),
                "Repair Of Septal Defect Of Heart With Protheis", cbRepairSeptal.isSelected());
        appendCmg3(sb, "Timekotomi", cbTimekotomy.isSelected(), "Keratoplasty", cbKeratoplasty.isSelected(),
                "Torakotomi", cbTorakotomi.isSelected());
        appendCmg3(sb, "Tumor Pineal Endoscopy", cbTumorPineal.isSelected(), "Vitrectomy", cbVitrectomy.isSelected(),
                "Phacoemulsification", cbPhacoemulsification.isSelected());
        appendCmg3(sb, "Microlaringoscopi", cbMicrolaringoscopi.isSelected(), "HIP Replacement/Knee Replacement", cbHipKnee.isSelected(),
                "Air Plumbage", cbAirPlumbage.isSelected());
        appendCmg3(sb, "Lobektomi/Bilobektomi", cbLobektomi.isSelected(), "Stereotactic Surgery Radiotherapy", cbStereotactic.isSelected(),
                "Pancreatectomy", cbPancreatectomy.isSelected());

        appendSpacerRow(sb);
        appendSectionRow(sb, "2.&nbsp; Spesial Drug");
        appendCmg2(sb, "Streptokinase", cbStreptokinase.isSelected(), "Deferifrone", cbDeferifrone.isSelected());
        appendCmg2(sb, "Deferoksamin", cbDeferoksamin.isSelected(), "Deferasirox", cbDeferasirox.isSelected());
        appendCmg2(sb, "Human Albumin", cbHumanAlbumin.isSelected(), null, false);

        appendSpacerRow(sb);
        appendSectionRow(sb, "3.&nbsp; Spesial Investigation");
        appendCmg2(sb, "Other CT Scan", cbOtherCTScan.isSelected(), "Nuclear Medicine", cbNuclearMedicine.isSelected());
        appendCmg2(sb, "MRI", cbMRI.isSelected(), "Diagnostic and Imaging Prosedure Of eye", cbDiagnosticEye.isSelected());

        appendSpacerRow(sb);
        appendSectionRow(sb, "4.&nbsp; Spesial Protheis");
        appendCmg2(sb, "Subdurat Grid Electrode", cbSubduratGrid.isSelected(), "Cote Graft", cbCoteGraft.isSelected());
        appendCmg2(sb, "Liquid Embolic (For AVM)", cbLiquidEmbolic.isSelected(), "HIP Implant/Knee Implant", cbHipImplant.isSelected());
        sb.append("</table>\n");

        // ==================== BLOK 4: DIAGNOSA / TINDAKAN / PENUNJANG ====================
        // 4 kolom: 26.26 | 42.2 | 6.58 | 24.96
        sb.append("<table style='margin-top:8pt;'>\n");
        sb.append("<colgroup><col style='width:26.26%'/><col style='width:42.2%'/>"
                + "<col style='width:6.58%'/><col style='width:24.96%'/></colgroup>\n");

        appendMedRow(sb, "Diagnosa Primer", txtNmDiagPrimer.getText(), txtKdDiagPrimer.getText(), "");
        appendMedRow(sb, "Diagnosa Sekunder", txtNmDiagSekunder1.getText(), txtKdDiagSekunder1.getText(), "1.");
        appendMedRow(sb, "", txtNmDiagSekunder2.getText(), txtKdDiagSekunder2.getText(), "2.");
        appendMedRow(sb, "", txtNmDiagSekunder3.getText(), txtKdDiagSekunder3.getText(), "3.");
        appendMedRow(sb, "", txtNmDiagSekunder4.getText(), txtKdDiagSekunder4.getText(), "4.");
        appendMedRow(sb, "", txtNmDiagSekunder5.getText(), txtKdDiagSekunder5.getText(), "5.");
        sb.append("<tr class='gap'><td colspan='4'></td></tr>\n");

        appendMedRow(sb, "Tindakan Primer", txtNmTindakanPrimer.getText(), txtKdTindakanPrimer.getText(), "");
        appendMedRow(sb, "Tindakan Sekunder", txtNmTindakanSekunder1.getText(), txtKdTindakanSekunder1.getText(), "1.");
        appendMedRow(sb, "", txtNmTindakanSekunder2.getText(), txtKdTindakanSekunder2.getText(), "2.");
        appendMedRow(sb, "", txtNmTindakanSekunder3.getText(), txtKdTindakanSekunder3.getText(), "3.");
        appendMedRow(sb, "", txtNmTindakanSekunder4.getText(), txtKdTindakanSekunder4.getText(), "4.");
        appendMedRow(sb, "", txtNmTindakanSekunder5.getText(), txtKdTindakanSekunder5.getText(), "5.");
        sb.append("<tr class='gap'><td colspan='4'></td></tr>\n");

        appendMedRow(sb, "Penunjang Primer", txtNmPenunjangPrimer.getText(), txtKdPenunjangPrimer.getText(), "");
        appendMedRow(sb, "Penunjang Sekunder", txtNmPenunjangSekunder1.getText(), txtKdPenunjangSekunder1.getText(), "1.");
        appendMedRow(sb, "", txtNmPenunjangSekunder2.getText(), txtKdPenunjangSekunder2.getText(), "2.");
        appendMedRow(sb, "", txtNmPenunjangSekunder3.getText(), txtKdPenunjangSekunder3.getText(), "3.");
        appendMedRow(sb, "", txtNmPenunjangSekunder4.getText(), txtKdPenunjangSekunder4.getText(), "4.");
        appendMedRow(sb, "", txtNmPenunjangSekunder5.getText(), txtKdPenunjangSekunder5.getText(), "5.");
        sb.append("</table>\n");

        // ==================== BLOK 5: TANDA TANGAN ====================
        String dokterTtd = txtNmDokter.getText().trim().isEmpty() ? "dr. ................................" : txtNmDokter.getText().trim();
        String sipStr = txtSIPDokter.getText().trim().isEmpty() ? "SIP. -"
                : (txtSIPDokter.getText().trim().startsWith("SIP") ? txtSIPDokter.getText().trim()
                : "SIP. " + txtSIPDokter.getText().trim());
        String pasienTtd = txtPenandatanganPasien.getText().trim().isEmpty() ? "(……………)"
                : "(" + txtPenandatanganPasien.getText().trim() + ")";

        sb.append("<table style='margin-top:10pt;'>\n");
        sb.append("<colgroup><col style='width:50%'/><col style='width:50%'/></colgroup>\n");
        sb.append("<tr><td class='c'>Dokter Penanggung Jawab</td>");
        sb.append("<td class='c'>Pasien/Keluarga Pasien</td></tr>\n");
        sb.append("<tr><td style='height:55pt;'></td><td style='height:55pt;'></td></tr>\n");
        sb.append("<tr><td class='c'>").append(dokterTtd).append("<br/>").append(sipStr).append("</td>");
        sb.append("<td class='c'>").append(pasienTtd).append("</td></tr>\n");
        sb.append("</table>\n");

        sb.append("</body>\n</html>\n");
        return sb.toString();
    }

    /** Baris admin 4 kolom: label | : nilai | label | : nilai. */
    private void appendAdm4(StringBuilder sb, String l1, String v1, boolean b1, String l2, String v2) {
        sb.append("<tr>");
        sb.append("<td class='p'>").append(l1).append("</td>");
        sb.append("<td class='p'>: ").append(b1 && v1 != null && !v1.trim().isEmpty()
                ? "<span class='b'>" + v1.trim() + "</span>" : (v1 == null ? "" : v1.trim())).append("</td>");
        sb.append("<td class='p'>").append(l2).append("</td>");
        sb.append("<td class='p'>: ").append(v2 == null ? "" : v2.trim()).append("</td>");
        sb.append("</tr>\n");
    }

    /** Baris admin label + nilai penuh (colspan 3). */
    private void appendAdmFull(StringBuilder sb, String label, String value, boolean boldValue) {
        sb.append("<tr><td class='p'>").append(label).append("</td>");
        sb.append("<td class='p' colspan='3'>: ");
        if (boldValue && value != null && !value.trim().isEmpty()) {
            sb.append("<span class='b'>").append(value.trim()).append("</span>");
        } else {
            sb.append(value == null ? "" : value.trim());
        }
        sb.append("</td></tr>\n");
    }

    private void appendSectionRow(StringBuilder sb, String title) {
        sb.append("<tr><td colspan='4' class='q'>").append(title).append("</td></tr>\n");
    }

    private void appendSpacerRow(StringBuilder sb) {
        sb.append("<tr><td colspan='4' class='sp'></td></tr>\n");
    }

    /** Baris CMG 3 kolom (kolom ke-3 boleh kosong). */
    private void appendCmg3(StringBuilder sb, String t1, boolean v1, String t2, boolean v2, String t3, boolean v3) {
        sb.append("<tr><td></td>");
        sb.append("<td class='q'>").append(cb(v1)).append(" ").append(t1).append("</td>");
        sb.append("<td class='q'>").append(t2 == null ? "&nbsp;" : cb(v2) + " " + t2).append("</td>");
        sb.append("<td class='q'>").append(t3 == null ? "&nbsp;" : cb(v3) + " " + t3).append("</td>");
        sb.append("</tr>\n");
    }

    /** Baris CMG 2 item: item kedua membentang kolom 3+4 (73% seperti dokumen acuan). */
    private void appendCmg2(StringBuilder sb, String t1, boolean v1, String t2, boolean v2) {
        sb.append("<tr><td></td>");
        sb.append("<td class='q'>").append(cb(v1)).append(" ").append(t1).append("</td>");
        sb.append("<td class='q' colspan='2'>")
                .append(t2 == null ? "&nbsp;" : cb(v2) + " " + t2).append("</td>");
        sb.append("</tr>\n");
    }

    /**
     * Baris Diagnosa/Tindakan/Penunjang mengikuti grid acuan:
     * label | : [isi + garis bawah] | Kode | : [isi + garis bawah].
     * Garis bawah dicetak sebagai karakter garis (seperti dokumen acuan) dan
     * panjangnya menyesuaikan isi sehingga ujung kanan tiap baris tetap sejajar.
     */
    private void appendMedRow(StringBuilder sb, String label, String nama, String kode, String nomor) {
        String namaVal = nama == null ? "" : nama.trim();
        String kodeVal = kode == null ? "" : kode.trim();
        String prefix = ": " + (nomor == null || nomor.isEmpty() ? "" : nomor + " ");

        sb.append("<tr class='med'>");
        sb.append("<td class='q b'>").append(label).append("</td>");
        sb.append("<td class='q nw'>").append(prefix).append(namaVal)
                .append(garis(namaVal, 38)).append("</td>");
        sb.append("<td class='q'>Kode</td>");
        sb.append("<td class='q nw'>: ").append(kodeVal).append(garis(kodeVal, 20)).append("</td>");
        sb.append("</tr>\n");
    }

    /** Garis bawah (underscore) yang panjangnya menyesuaikan panjang teks isian. */
    private String garis(String value, int slot) {
        int n = slot - (value == null ? 0 : value.length());
        if (n < 3) {
            n = 3;
        }
        StringBuilder s = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            s.append('_');
        }
        return s.toString();
    }

    /** Kotak checkbox: kosong atau berisi X. */
    private String cb(boolean selected) {
        return "<span class='cb'>" + (selected ? "X" : "") + "</span>";
    }

    /** Konversi tanggal database (yyyy-MM-dd) ke format acuan dd/MM/yyyy. */
    private String formatTglINA(String value) {
        if (value == null) {
            return "";
        }
        String v = value.trim();
        if (v.isEmpty()) {
            return "";
        }
        if (v.length() >= 10 && v.charAt(4) == '-' && v.charAt(7) == '-') {
            String y = v.substring(0, 4);
            if ("0000".equals(y)) {
                return "";
            }
            return v.substring(8, 10) + "/" + v.substring(5, 7) + "/" + y;
        }
        return v;
    }

    /**
     * Render preview langsung dari PDF yang dihasilkan, sehingga tampilan layar
     * 100% identik dengan hasil cetak (bukan perkiraan HTML viewer).
     */
    private void renderHTMLPreview() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            byte[] pdfBytes = buildPdfBytes();
            if (pdfBytes == null) {
                return;
            }
            renderPdfPreview(pdfBytes);
        } catch (Exception e) {
            System.out.println("Error renderHTMLPreview: " + e);
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }

    /** Bangun PDF ke memori (tanpa menyentuh disk). */
    private byte[] buildPdfBytes() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(bos);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(PageSize.A4);
        ConverterProperties props = buildConverterProperties();
        HtmlConverter.convertToPdf(buildHTMLContent(), pdfDoc, props);
        pdfDoc.close();
        return bos.toByteArray();
    }

    /** Cache FontProvider agar tidak memindai font sistem berulang kali (mahal). */
    private static com.itextpdf.layout.font.FontProvider cachedFontProvider = null;
    private static boolean fontProviderInitTried = false;

    /** Konfigurasi converter: daftarkan font fallback metrik-kompatibel Times New Roman. */
    private ConverterProperties buildConverterProperties() {
        ConverterProperties props = new ConverterProperties();
        try {
            props.setFontProvider(getFontProvider());
        } catch (Exception e) {
            System.out.println("FontProvider fallback dilewati: " + e);
        }
        return props;
    }

    /** Bangun FontProvider sekali saja, lalu dipakai ulang untuk preview & cetak. */
    private static synchronized com.itextpdf.layout.font.FontProvider getFontProvider() {
        if (fontProviderInitTried) {
            return cachedFontProvider;
        }
        fontProviderInitTried = true;
        try {
            com.itextpdf.layout.font.FontProvider fp = new com.itextpdf.layout.font.FontProvider();
            boolean adaTNR = false;
            // Prioritaskan Times New Roman dari sistem (identik dengan dokumen acuan)
            for (String p : new String[]{
                    "C:/Windows/Fonts/times.ttf",
                    "C:/Windows/Fonts/timesbd.ttf",
                    "C:/Windows/Fonts/timesi.ttf",
                    "C:/Windows/Fonts/timesbi.ttf",
                    "/usr/share/fonts/truetype/msttcorefonts/Times_New_Roman.ttf",
                    "/usr/share/fonts/truetype/msttcorefonts/Times_New_Roman_Bold.ttf",
                    "/usr/share/fonts/truetype/msttcorefonts/Times_New_Roman_Italic.ttf",
                    "/usr/share/fonts/truetype/msttcorefonts/Times_New_Roman_Bold_Italic.ttf"}) {
                try {
                    File f = new File(p);
                    if (f.exists()) {
                        fp.addFont(p);
                        adaTNR = true;
                    }
                } catch (Exception ignored) {
                }
            }
            // Fallback metrik-kompatibel (lebar karakter identik dengan Times New Roman)
            for (String res : new String[]{
                    "/font/LiberationSerif-Regular.ttf",
                    "/font/LiberationSerif-Bold.ttf"}) {
                try {
                    java.io.InputStream is = DlgLembarBuktiINACBG.class.getResourceAsStream(res);
                    if (is != null) {
                        byte[] data = readAll(is);
                        is.close();
                        fp.addFont(data);
                    }
                } catch (Exception ignored) {
                }
            }
            // Pemindaian font sistem hanya bila TNR & Liberation sama sekali tidak ada.
            if (!adaTNR) {
                try {
                    fp.addSystemFonts();
                } catch (Exception ignored) {
                }
            }
            cachedFontProvider = fp;
        } catch (Exception e) {
            System.out.println("FontProvider gagal disiapkan: " + e);
            cachedFontProvider = null;
        }
        return cachedFontProvider;
    }

    private static byte[] readAll(java.io.InputStream is) throws java.io.IOException {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = is.read(buf)) != -1) {
            bos.write(buf, 0, n);
        }
        return bos.toByteArray();
    }

    /** Render setiap halaman PDF menjadi gambar dan tampilkan di panel preview. */
    private void renderPdfPreview(byte[] pdfBytes) throws Exception {
        pdfPreviewPages.clear();
        try (org.apache.pdfbox.pdmodel.PDDocument doc = org.apache.pdfbox.pdmodel.PDDocument.load(pdfBytes)) {
            org.apache.pdfbox.rendering.PDFRenderer renderer = new org.apache.pdfbox.rendering.PDFRenderer(doc);
            renderer.setSubsamplingAllowed(true);
            int total = doc.getNumberOfPages();
            for (int i = 0; i < total; i++) {
                java.awt.image.BufferedImage img = renderer.renderImageWithDPI(i, 110);
                pdfPreviewPages.add(img);
            }
        }

        panelPdfPreview.removeAll();
        for (int i = 0; i < pdfPreviewPages.size(); i++) {
            JLabel page = new JLabel(new ImageIcon(pdfPreviewPages.get(i)));
            page.setAlignmentX(JPanel.CENTER_ALIGNMENT);
            page.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(8, 8, 8, 8),
                    BorderFactory.createLineBorder(new Color(150, 160, 150))));
            panelPdfPreview.add(page);
        }
        panelPdfPreview.add(Box.createVerticalStrut(12));
        panelPdfPreview.revalidate();
        panelPdfPreview.repaint();
        scrollPreview.getViewport().setViewPosition(new java.awt.Point(0, 0));
    }

    private void clearPdfPreview() {
        pdfPreviewPages.clear();
        if (panelPdfPreview != null) {
            panelPdfPreview.removeAll();
            panelPdfPreview.revalidate();
            panelPdfPreview.repaint();
        }
    }

    private void generatePDF() {
        if (txtNoRawat.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No. Rawat kosong, tidak dapat membuat PDF!");
            return;
        }

        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            // Simpan snapshot terlebih dahulu agar konsisten
            simpan();

            File folder = new File("berkasina");
            if (!folder.exists()) {
                folder.mkdirs();
            }

            int modelIdx = (cmbModelCetakTab1 != null) ? cmbModelCetakTab1.getSelectedIndex() : 0;
            String modelSuffix = (modelIdx == 1) ? "_Model1" : "_Model2";
            String fileName = "berkasina" + File.separator + "BerkasINA_" + txtNoRawat.getText().trim().replaceAll("[^a-zA-Z0-9]", "_") + modelSuffix + ".pdf";
            File pdfFile = new File(fileName);

            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                PdfWriter writer = new PdfWriter(fos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                pdfDoc.setDefaultPageSize(PageSize.A4);
                HtmlConverter.convertToPdf(buildHTMLContent(), pdfDoc, buildConverterProperties());
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                JOptionPane.showMessageDialog(null, "File PDF berhasil dibuat: " + pdfFile.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("Error generatePDF: " + e);
            JOptionPane.showMessageDialog(null, "Gagal membuat PDF: " + e.getMessage());
        } finally {
            setCursor(Cursor.getDefaultCursor());
        }
    }
}
