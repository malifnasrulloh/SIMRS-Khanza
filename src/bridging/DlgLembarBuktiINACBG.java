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
import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import widget.Button;
import widget.CekBox;
import widget.ComboBox;
import widget.InternalFrame;
import widget.Label;
import widget.PanelBiasa;
import widget.ScrollPane;
import widget.TextBox;
import widget.editorpane;
import widget.panelisi;

/**
 * Dialog Form Lembar Bukti Pelayanan Variabel INACBG
 */
public class DlgLembarBuktiINACBG extends javax.swing.JDialog {
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();
    private final DecimalFormat df = new DecimalFormat("#,##0");
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    // UI Components
    private InternalFrame internalFrame;
    private JTabbedPane tabPane;
    private JPanel panelInput, panelPreview;
    private ScrollPane scrollInput, scrollPreview;
    private editorpane loadHTML;

    // Buttons
    private Button btnSimpan, btnHapus, btnBaru, btnPreview, btnKeluar;
    private Button btnGeneratePDF, btnKembali, btnRefresh;

    // Administrative Inputs
    private TextBox txtNoRawat, txtNoRM, txtNmPasien, txtJK, txtTglLahir;
    private TextBox txtNoPeserta, txtNoSEP, txtJenisPerawatan, txtKelas;
    private TextBox txtTglMasuk, txtTglKeluar;
    private ComboBox cmbCaraPulang;
    private TextBox txtKdDokter, txtNmDokter, txtSIPDokter;
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

    // Diagnosa Fields
    private TextBox txtKdDiagPrimer, txtNmDiagPrimer;
    private TextBox txtKdDiagSekunder1, txtNmDiagSekunder1;
    private TextBox txtKdDiagSekunder2, txtNmDiagSekunder2;
    private TextBox txtKdDiagSekunder3, txtNmDiagSekunder3;
    private TextBox txtKdDiagSekunder4, txtNmDiagSekunder4;
    private TextBox txtKdDiagSekunder5, txtNmDiagSekunder5;

    // Tindakan Fields
    private TextBox txtKdTindakanPrimer, txtNmTindakanPrimer;
    private TextBox txtKdTindakanSekunder1, txtNmTindakanSekunder1;
    private TextBox txtKdTindakanSekunder2, txtNmTindakanSekunder2;
    private TextBox txtKdTindakanSekunder3, txtNmTindakanSekunder3;
    private TextBox txtKdTindakanSekunder4, txtNmTindakanSekunder4;
    private TextBox txtKdTindakanSekunder5, txtNmTindakanSekunder5;

    // Penunjang Fields
    private TextBox txtKdPenunjangPrimer, txtNmPenunjangPrimer;
    private TextBox txtKdPenunjangSekunder1, txtNmPenunjangSekunder1;
    private TextBox txtKdPenunjangSekunder2, txtNmPenunjangSekunder2;
    private TextBox txtKdPenunjangSekunder3, txtNmPenunjangSekunder3;

    private static final Font FONT_REGULAR = new Font("Tahoma", Font.PLAIN, 11);
    private static final Font FONT_BOLD = new Font("Tahoma", Font.BOLD, 11);
    private static final Font FONT_TITLE = new Font("Tahoma", Font.BOLD, 12);

    public DlgLembarBuktiINACBG(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    private void initComponents() {
        setTitle("::[ Lembar Bukti Pelayanan Variabel INACBG ]::");
        setSize(980, 680);
        setLocationRelativeTo(null);

        internalFrame = new InternalFrame();
        internalFrame.setLayout(new BorderLayout());
        getContentPane().add(internalFrame);

        tabPane = new JTabbedPane();
        tabPane.setFont(FONT_BOLD);

        // TAB 1: INPUT VARIABEL INA
        panelInput = new JPanel();
        panelInput.setLayout(new BoxLayout(panelInput, BoxLayout.Y_AXIS));
        panelInput.setBackground(new Color(250, 255, 250));

        initInputFields();

        scrollInput = new ScrollPane();
        scrollInput.setViewportView(panelInput);
        scrollInput.getVerticalScrollBar().setUnitIncrement(16);

        JPanel panelTab1 = new JPanel(new BorderLayout());
        panelTab1.add(scrollInput, BorderLayout.CENTER);

        // Bottom Action Bar Tab 1
        panelisi panelTombolTab1 = new panelisi();
        panelTombolTab1.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));

        btnSimpan = new Button();
        btnSimpan.setIcon(getSafeIcon("/picture/save-16x16.png"));
        btnSimpan.setText("Simpan");
        btnSimpan.setFont(FONT_REGULAR);
        btnSimpan.setPreferredSize(new Dimension(100, 30));
        btnSimpan.addActionListener(e -> simpan());

        btnHapus = new Button();
        btnHapus.setIcon(getSafeIcon("/picture/stop_f2.png"));
        btnHapus.setText("Hapus");
        btnHapus.setFont(FONT_REGULAR);
        btnHapus.setPreferredSize(new Dimension(100, 30));
        btnHapus.addActionListener(e -> hapus());

        btnBaru = new Button();
        btnBaru.setIcon(getSafeIcon("/picture/Cancel-2-16x16.png"));
        btnBaru.setText("Reset Auto-Fill");
        btnBaru.setFont(FONT_REGULAR);
        btnBaru.setPreferredSize(new Dimension(130, 30));
        btnBaru.addActionListener(e -> autoFill(txtNoRawat.getText().trim()));

        btnPreview = new Button();
        btnPreview.setIcon(getSafeIcon("/picture/Search-16x16.png"));
        btnPreview.setText("Preview Cetak");
        btnPreview.setFont(FONT_REGULAR);
        btnPreview.setPreferredSize(new Dimension(120, 30));
        btnPreview.addActionListener(e -> {
            renderHTMLPreview();
            tabPane.setSelectedIndex(1);
        });

        btnKeluar = new Button();
        btnKeluar.setIcon(getSafeIcon("/picture/exit.png"));
        btnKeluar.setText("Keluar");
        btnKeluar.setFont(FONT_REGULAR);
        btnKeluar.setPreferredSize(new Dimension(100, 30));
        btnKeluar.addActionListener(e -> dispose());

        panelTombolTab1.add(btnSimpan);
        panelTombolTab1.add(btnHapus);
        panelTombolTab1.add(btnBaru);
        panelTombolTab1.add(btnPreview);
        panelTombolTab1.add(btnKeluar);
        panelTab1.add(panelTombolTab1, BorderLayout.SOUTH);

        tabPane.addTab("  1. Input Variabel INA  ", panelTab1);

        // TAB 2: PREVIEW & CETAK BERKAS
        panelPreview = new JPanel(new BorderLayout());

        panelisi panelTombolTab2 = new panelisi();
        panelTombolTab2.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));

        btnKembali = new Button();
        btnKembali.setIcon(getSafeIcon("/picture/011.png"));
        btnKembali.setText("Kembali ke Form");
        btnKembali.setFont(FONT_REGULAR);
        btnKembali.setPreferredSize(new Dimension(135, 30));
        btnKembali.addActionListener(e -> tabPane.setSelectedIndex(0));

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
        btnGeneratePDF.setPreferredSize(new Dimension(160, 30));
        btnGeneratePDF.addActionListener(e -> generatePDF());

        panelTombolTab2.add(btnKembali);
        panelTombolTab2.add(btnRefresh);
        panelTombolTab2.add(btnGeneratePDF);
        panelPreview.add(panelTombolTab2, BorderLayout.NORTH);

        loadHTML = new editorpane();
        loadHTML.setEditable(false);
        HTMLEditorKit kit = new HTMLEditorKit();
        loadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { font-family: 'Times New Roman', Times, serif; font-size: 12pt; margin: 15px; color: #000; }");
        styleSheet.addRule(".box { border: 1px solid #000; font-family: Arial, sans-serif; font-size: 10pt; font-weight: bold; }");

        scrollPreview = new ScrollPane();
        scrollPreview.setViewportView(loadHTML);
        scrollPreview.getVerticalScrollBar().setUnitIncrement(16);
        panelPreview.add(scrollPreview, BorderLayout.CENTER);

        tabPane.addTab("  2. Preview & Cetak Berkas  ", panelPreview);

        internalFrame.add(tabPane, BorderLayout.CENTER);
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
        txtNmPasien = createTextBox(200);
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

        txtKdDokter = createTextBox(80);
        txtNmDokter = createTextBox(200);
        txtSIPDokter = createTextBox(160);

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

        // Baris 4: DPJP, SIP, Total Tarif, ADL Skor
        addGrid(pAdm, g, createLabel("DPJP (Dokter) :"), 0, 3, 1);
        addGrid(pAdm, g, txtNmDokter, 1, 3, 3);
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
        cbRepairSeptal = createCheckBox("Repair Of Septal Defect Of Heart With Protheis");
        cbTimekotomy = createCheckBox("TIMEKOTOMY");
        cbKeratoplasty = createCheckBox("Keratoplasty");
        cbTorakotomi = createCheckBox("Torakotomi");
        cbTumorPineal = createCheckBox("Tumor Pineal Endoscopy");
        cbVitrectomy = createCheckBox("Vitrectomy");
        cbPhacoemulsification = createCheckBox("Phacoemulsification");
        cbMicrolaringoscopi = createCheckBox("Microlaringoscopi");
        cbHipKnee = createCheckBox("HIP Replacement / Knee Replacement");
        cbAirPlumbage = createCheckBox("Air Plumbage");
        cbLobektomi = createCheckBox("Lobektomi / Bilobektomi");
        cbStereotactic = createCheckBox("Stereotactic Surgery Radioteraphy");
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

        // Sub 2: Drug & Sub 3: Investigation
        JPanel pDrugInvest = new JPanel(new GridLayout(1, 2, 8, 4));
        pDrugInvest.setOpaque(false);

        JPanel pDrug = new JPanel(new GridLayout(0, 2, 4, 2));
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
        pDrugInvest.add(pDrug);

        JPanel pInvest = new JPanel(new GridLayout(0, 2, 4, 2));
        pInvest.setBorder(BorderFactory.createTitledBorder(null, "3. Spesial Investigation", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, new Color(50, 50, 50)));
        pInvest.setOpaque(false);
        cbOtherCTScan = createCheckBox("Other CT Scan");
        cbNuclearMedicine = createCheckBox("Nuclear Medicine");
        cbMRI = createCheckBox("MRI");
        cbDiagnosticEye = createCheckBox("Diagnostic & Imaging Procedure Of Eye");
        pInvest.add(cbOtherCTScan);
        pInvest.add(cbNuclearMedicine);
        pInvest.add(cbMRI);
        pInvest.add(cbDiagnosticEye);
        pDrugInvest.add(pInvest);
        pCMG.add(pDrugInvest);

        // Sub 4: Protheis
        JPanel pProtheis = new JPanel(new GridLayout(0, 2, 4, 2));
        pProtheis.setBorder(BorderFactory.createTitledBorder(null, "4. Spesial Protheis", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, new Color(50, 50, 50)));
        pProtheis.setOpaque(false);
        cbSubduratGrid = createCheckBox("Subdurat Grid Electrode");
        cbCoteGraft = createCheckBox("Cote Graft");
        cbLiquidEmbolic = createCheckBox("Liquid Embolic (For AVM)");
        cbHipImplant = createCheckBox("HIP Implant / Knee Implant");
        pProtheis.add(cbSubduratGrid);
        pProtheis.add(cbCoteGraft);
        pProtheis.add(cbLiquidEmbolic);
        pProtheis.add(cbHipImplant);
        pCMG.add(pProtheis);

        panelInput.add(pCMG);

        // 3. DIAGNOSA & TINDAKAN
        JPanel pDiagTind = createSectionPanel(" Diagnosa (ICD-10) & Tindakan (ICD-9) ");
        pDiagTind.setLayout(new GridLayout(1, 2, 8, 4));

        // Kolom Kiri: Diagnosa
        JPanel pDiagnosa = new JPanel(new GridBagLayout());
        pDiagnosa.setBorder(BorderFactory.createTitledBorder(null, "Diagnosa (ICD-10)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, new Color(50, 50, 50)));
        pDiagnosa.setOpaque(false);
        GridBagConstraints gd = new GridBagConstraints();
        gd.insets = new Insets(2, 3, 2, 3);
        gd.fill = GridBagConstraints.HORIZONTAL;

        txtKdDiagPrimer = createTextBox(65);
        txtNmDiagPrimer = createTextBox(230);
        txtKdDiagSekunder1 = createTextBox(65);
        txtNmDiagSekunder1 = createTextBox(230);
        txtKdDiagSekunder2 = createTextBox(65);
        txtNmDiagSekunder2 = createTextBox(230);
        txtKdDiagSekunder3 = createTextBox(65);
        txtNmDiagSekunder3 = createTextBox(230);
        txtKdDiagSekunder4 = createTextBox(65);
        txtNmDiagSekunder4 = createTextBox(230);
        txtKdDiagSekunder5 = createTextBox(65);
        txtNmDiagSekunder5 = createTextBox(230);

        addGrid(pDiagnosa, gd, createLabel("Primer :"), 0, 0, 1);
        addGrid(pDiagnosa, gd, txtKdDiagPrimer, 1, 0, 1);
        addGrid(pDiagnosa, gd, txtNmDiagPrimer, 2, 0, 1);

        addGrid(pDiagnosa, gd, createLabel("Sekunder 1 :"), 0, 1, 1);
        addGrid(pDiagnosa, gd, txtKdDiagSekunder1, 1, 1, 1);
        addGrid(pDiagnosa, gd, txtNmDiagSekunder1, 2, 1, 1);

        addGrid(pDiagnosa, gd, createLabel("Sekunder 2 :"), 0, 2, 1);
        addGrid(pDiagnosa, gd, txtKdDiagSekunder2, 1, 2, 1);
        addGrid(pDiagnosa, gd, txtNmDiagSekunder2, 2, 2, 1);

        addGrid(pDiagnosa, gd, createLabel("Sekunder 3 :"), 0, 3, 1);
        addGrid(pDiagnosa, gd, txtKdDiagSekunder3, 1, 3, 1);
        addGrid(pDiagnosa, gd, txtNmDiagSekunder3, 2, 3, 1);

        addGrid(pDiagnosa, gd, createLabel("Sekunder 4 :"), 0, 4, 1);
        addGrid(pDiagnosa, gd, txtKdDiagSekunder4, 1, 4, 1);
        addGrid(pDiagnosa, gd, txtNmDiagSekunder4, 2, 4, 1);

        addGrid(pDiagnosa, gd, createLabel("Sekunder 5 :"), 0, 5, 1);
        addGrid(pDiagnosa, gd, txtKdDiagSekunder5, 1, 5, 1);
        addGrid(pDiagnosa, gd, txtNmDiagSekunder5, 2, 5, 1);
        pDiagTind.add(pDiagnosa);

        // Kolom Kanan: Tindakan
        JPanel pTindakan = new JPanel(new GridBagLayout());
        pTindakan.setBorder(BorderFactory.createTitledBorder(null, "Tindakan / Prosedur (ICD-9)", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_BOLD, new Color(50, 50, 50)));
        pTindakan.setOpaque(false);
        GridBagConstraints gt = new GridBagConstraints();
        gt.insets = new Insets(2, 3, 2, 3);
        gt.fill = GridBagConstraints.HORIZONTAL;

        txtKdTindakanPrimer = createTextBox(65);
        txtNmTindakanPrimer = createTextBox(230);
        txtKdTindakanSekunder1 = createTextBox(65);
        txtNmTindakanSekunder1 = createTextBox(230);
        txtKdTindakanSekunder2 = createTextBox(65);
        txtNmTindakanSekunder2 = createTextBox(230);
        txtKdTindakanSekunder3 = createTextBox(65);
        txtNmTindakanSekunder3 = createTextBox(230);
        txtKdTindakanSekunder4 = createTextBox(65);
        txtNmTindakanSekunder4 = createTextBox(230);
        txtKdTindakanSekunder5 = createTextBox(65);
        txtNmTindakanSekunder5 = createTextBox(230);

        addGrid(pTindakan, gt, createLabel("Primer :"), 0, 0, 1);
        addGrid(pTindakan, gt, txtKdTindakanPrimer, 1, 0, 1);
        addGrid(pTindakan, gt, txtNmTindakanPrimer, 2, 0, 1);

        addGrid(pTindakan, gt, createLabel("Sekunder 1 :"), 0, 1, 1);
        addGrid(pTindakan, gt, txtKdTindakanSekunder1, 1, 1, 1);
        addGrid(pTindakan, gt, txtNmTindakanSekunder1, 2, 1, 1);

        addGrid(pTindakan, gt, createLabel("Sekunder 2 :"), 0, 2, 1);
        addGrid(pTindakan, gt, txtKdTindakanSekunder2, 1, 2, 1);
        addGrid(pTindakan, gt, txtNmTindakanSekunder2, 2, 2, 1);

        addGrid(pTindakan, gt, createLabel("Sekunder 3 :"), 0, 3, 1);
        addGrid(pTindakan, gt, txtKdTindakanSekunder3, 1, 3, 1);
        addGrid(pTindakan, gt, txtNmTindakanSekunder3, 2, 3, 1);

        addGrid(pTindakan, gt, createLabel("Sekunder 4 :"), 0, 4, 1);
        addGrid(pTindakan, gt, txtKdTindakanSekunder4, 1, 4, 1);
        addGrid(pTindakan, gt, txtNmTindakanSekunder4, 2, 4, 1);

        addGrid(pTindakan, gt, createLabel("Sekunder 5 :"), 0, 5, 1);
        addGrid(pTindakan, gt, txtKdTindakanSekunder5, 1, 5, 1);
        addGrid(pTindakan, gt, txtNmTindakanSekunder5, 2, 5, 1);
        pDiagTind.add(pTindakan);

        panelInput.add(pDiagTind);

        // 4. PENUNJANG
        JPanel pPenunjang = createSectionPanel(" Pemeriksaan Penunjang ");
        pPenunjang.setLayout(new GridBagLayout());
        GridBagConstraints gp = new GridBagConstraints();
        gp.insets = new Insets(2, 4, 2, 4);
        gp.fill = GridBagConstraints.HORIZONTAL;

        txtKdPenunjangPrimer = createTextBox(80);
        txtNmPenunjangPrimer = createTextBox(250);
        txtKdPenunjangSekunder1 = createTextBox(80);
        txtNmPenunjangSekunder1 = createTextBox(250);
        txtKdPenunjangSekunder2 = createTextBox(80);
        txtNmPenunjangSekunder2 = createTextBox(250);
        txtKdPenunjangSekunder3 = createTextBox(80);
        txtNmPenunjangSekunder3 = createTextBox(250);

        addGrid(pPenunjang, gp, createLabel("Primer :"), 0, 0, 1);
        addGrid(pPenunjang, gp, txtKdPenunjangPrimer, 1, 0, 1);
        addGrid(pPenunjang, gp, txtNmPenunjangPrimer, 2, 0, 1);

        addGrid(pPenunjang, gp, createLabel("Sekunder 1 :"), 3, 0, 1);
        addGrid(pPenunjang, gp, txtKdPenunjangSekunder1, 4, 0, 1);
        addGrid(pPenunjang, gp, txtNmPenunjangSekunder1, 5, 0, 1);

        addGrid(pPenunjang, gp, createLabel("Sekunder 2 :"), 0, 1, 1);
        addGrid(pPenunjang, gp, txtKdPenunjangSekunder2, 1, 1, 1);
        addGrid(pPenunjang, gp, txtNmPenunjangSekunder2, 2, 1, 1);

        addGrid(pPenunjang, gp, createLabel("Sekunder 3 :"), 3, 1, 1);
        addGrid(pPenunjang, gp, txtKdPenunjangSekunder3, 4, 1, 1);
        addGrid(pPenunjang, gp, txtNmPenunjangSekunder3, 5, 1, 1);

        panelInput.add(pPenunjang);
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel();
        p.setBackground(new Color(255, 255, 255));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(210, 220, 210), 1),
            title, TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, FONT_TITLE, new Color(0, 100, 50)
        ));
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

        tabPane.setSelectedIndex(0);
        loadHTML.setText("");
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
                cbTimekotomy.setSelected(sp.contains("TIMEKOTOMY"));
                cbKeratoplasty.setSelected(sp.contains("Keratoplasty"));
                cbTorakotomi.setSelected(sp.contains("Torakotomi"));
                cbTumorPineal.setSelected(sp.contains("Tumor Pineal"));
                cbVitrectomy.setSelected(sp.contains("Vitrectomy"));
                cbPhacoemulsification.setSelected(sp.contains("Phacoemulsification"));
                cbMicrolaringoscopi.setSelected(sp.contains("Microlaringoscopi"));
                cbHipKnee.setSelected(sp.contains("HIP Replacement"));
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

                // Penunjang
                txtKdPenunjangPrimer.setText(rs.getString("kd_penunjang_primer") == null ? "" : rs.getString("kd_penunjang_primer"));
                txtNmPenunjangPrimer.setText(rs.getString("penunjang_primer") == null ? "" : rs.getString("penunjang_primer"));
                txtKdPenunjangSekunder1.setText(rs.getString("kd_penunjang_sekunder1") == null ? "" : rs.getString("kd_penunjang_sekunder1"));
                txtNmPenunjangSekunder1.setText(rs.getString("penunjang_sekunder1") == null ? "" : rs.getString("penunjang_sekunder1"));
                txtKdPenunjangSekunder2.setText(rs.getString("kd_penunjang_sekunder2") == null ? "" : rs.getString("kd_penunjang_sekunder2"));
                txtNmPenunjangSekunder2.setText(rs.getString("penunjang_sekunder2") == null ? "" : rs.getString("penunjang_sekunder2"));
                txtKdPenunjangSekunder3.setText(rs.getString("kd_penunjang_sekunder3") == null ? "" : rs.getString("kd_penunjang_sekunder3"));
                txtNmPenunjangSekunder3.setText(rs.getString("penunjang_sekunder3") == null ? "" : rs.getString("penunjang_sekunder3"));

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

        } catch (Exception e) {
            System.out.println("Error autoFill Lembar INA: " + e);
        }
    }

    private void simpan() {
        if (txtNoRawat.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "No. Rawat tidak boleh kosong!");
            return;
        }

        try {
            // Hitung string checklist
            StringBuilder sp = new StringBuilder();
            if (cbPCL.isSelected()) sp.append("PCL, ");
            if (cbColangiography.isSelected()) sp.append("Colangiography, ");
            if (cbRepairSeptal.isSelected()) sp.append("Repair Of Septal Defect Of Heart With Protheis, ");
            if (cbTimekotomy.isSelected()) sp.append("TIMEKOTOMY, ");
            if (cbKeratoplasty.isSelected()) sp.append("Keratoplasty, ");
            if (cbTorakotomi.isSelected()) sp.append("Torakotomi, ");
            if (cbTumorPineal.isSelected()) sp.append("Tumor Pineal Endoscopy, ");
            if (cbVitrectomy.isSelected()) sp.append("Vitrectomy, ");
            if (cbPhacoemulsification.isSelected()) sp.append("Phacoemulsification, ");
            if (cbMicrolaringoscopi.isSelected()) sp.append("Microlaringoscopi, ");
            if (cbHipKnee.isSelected()) sp.append("HIP Replacement / Knee Replacement, ");
            if (cbAirPlumbage.isSelected()) sp.append("Air Plumbage, ");
            if (cbLobektomi.isSelected()) sp.append("Lobektomi / Bilobektomi, ");
            if (cbStereotactic.isSelected()) sp.append("Stereotactic Surgery Radioteraphy, ");
            if (cbPancreatectomy.isSelected()) sp.append("Pancreatectomy, ");

            StringBuilder sd = new StringBuilder();
            if (cbStreptokinase.isSelected()) sd.append("Streptokinase, ");
            if (cbDeferifrone.isSelected()) sd.append("Deferifrone, ");
            if (cbDeferoksamin.isSelected()) sd.append("Deferoksamin, ");
            if (cbDeferasirox.isSelected()) sd.append("Deferasirox, ");
            if (cbHumanAlbumin.isSelected()) sd.append("Human Albumin, ");

            StringBuilder si = new StringBuilder();
            if (cbOtherCTScan.isSelected()) si.append("Other CT Scan, ");
            if (cbNuclearMedicine.isSelected()) si.append("Nuclear Medicine, ");
            if (cbMRI.isSelected()) si.append("MRI, ");
            if (cbDiagnosticEye.isSelected()) si.append("Diagnostic & Imaging Procedure Of Eye, ");

            StringBuilder sprot = new StringBuilder();
            if (cbSubduratGrid.isSelected()) sprot.append("Subdurat Grid Electrode, ");
            if (cbCoteGraft.isSelected()) sprot.append("Cote Graft, ");
            if (cbLiquidEmbolic.isSelected()) sprot.append("Liquid Embolic (For AVM), ");
            if (cbHipImplant.isSelected()) sprot.append("HIP Implant / Knee Implant, ");

            double totalTarif = 0;
            try {
                totalTarif = Double.parseDouble(txtTotalTarif.getText().replaceAll("[^0-9.]", ""));
            } catch (Exception ignored) {}

            PreparedStatement ps = koneksi.prepareStatement(
                "REPLACE INTO lembar_bukti_pelayanan_inacbg (" +
                "no_rawat, tgl_registrasi, no_sep, no_peserta, tgl_masuk, tgl_keluar, jenis_perawatan, kelas, " +
                "cara_pulang, kd_dokter, dokter_penanggung_jawab, sip_dokter, berat_lahir, total_tarif, adl_skor, " +
                "spesial_prosedur, spesial_drug, spesial_investigation, spesial_protheis, " +
                "diagnosa_primer, kd_diagnosa_primer, diagnosa_sekunder1, kd_diagnosa_sekunder1, " +
                "diagnosa_sekunder2, kd_diagnosa_sekunder2, diagnosa_sekunder3, kd_diagnosa_sekunder3, " +
                "diagnosa_sekunder4, kd_diagnosa_sekunder4, diagnosa_sekunder5, kd_diagnosa_sekunder5, " +
                "tindakan_primer, kd_tindakan_primer, tindakan_sekunder1, kd_tindakan_sekunder1, " +
                "tindakan_sekunder2, kd_tindakan_sekunder2, tindakan_sekunder3, kd_tindakan_sekunder3, " +
                "tindakan_sekunder4, kd_tindakan_sekunder4, tindakan_sekunder5, kd_tindakan_sekunder5, " +
                "penunjang_primer, kd_penunjang_primer, penunjang_sekunder1, kd_penunjang_sekunder1, " +
                "penunjang_sekunder2, kd_penunjang_sekunder2, penunjang_sekunder3, kd_penunjang_sekunder3, " +
                "nama_pasien_keluarga" +
                ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
            );

            ps.setString(1, txtNoRawat.getText().trim());
            ps.setString(2, txtTglMasuk.getText().trim().isEmpty() ? null : txtTglMasuk.getText().trim());
            ps.setString(3, txtNoSEP.getText().trim());
            ps.setString(4, txtNoPeserta.getText().trim());
            ps.setString(5, txtTglMasuk.getText().trim().isEmpty() ? null : txtTglMasuk.getText().trim());
            ps.setString(6, txtTglKeluar.getText().trim().isEmpty() ? null : txtTglKeluar.getText().trim());
            ps.setString(7, txtJenisPerawatan.getText().trim());
            ps.setString(8, txtKelas.getText().trim());
            ps.setString(9, cmbCaraPulang.getSelectedItem().toString());
            ps.setString(10, txtKdDokter.getText().trim());
            ps.setString(11, txtNmDokter.getText().trim());
            ps.setString(12, txtSIPDokter.getText().trim());
            ps.setString(13, txtBeratLahir.getText().trim());
            ps.setDouble(14, totalTarif);
            ps.setString(15, txtSkorADL.getText().trim());
            ps.setString(16, sp.toString());
            ps.setString(17, sd.toString());
            ps.setString(18, si.toString());
            ps.setString(19, sprot.toString());

            ps.setString(20, txtNmDiagPrimer.getText().trim());
            ps.setString(21, txtKdDiagPrimer.getText().trim());
            ps.setString(22, txtNmDiagSekunder1.getText().trim());
            ps.setString(23, txtKdDiagSekunder1.getText().trim());
            ps.setString(24, txtNmDiagSekunder2.getText().trim());
            ps.setString(25, txtKdDiagSekunder2.getText().trim());
            ps.setString(26, txtNmDiagSekunder3.getText().trim());
            ps.setString(27, txtKdDiagSekunder3.getText().trim());
            ps.setString(28, txtNmDiagSekunder4.getText().trim());
            ps.setString(29, txtKdDiagSekunder4.getText().trim());
            ps.setString(30, txtNmDiagSekunder5.getText().trim());
            ps.setString(31, txtKdDiagSekunder5.getText().trim());

            ps.setString(32, txtNmTindakanPrimer.getText().trim());
            ps.setString(33, txtKdTindakanPrimer.getText().trim());
            ps.setString(34, txtNmTindakanSekunder1.getText().trim());
            ps.setString(35, txtKdTindakanSekunder1.getText().trim());
            ps.setString(36, txtNmTindakanSekunder2.getText().trim());
            ps.setString(37, txtKdTindakanSekunder2.getText().trim());
            ps.setString(38, txtNmTindakanSekunder3.getText().trim());
            ps.setString(39, txtKdTindakanSekunder3.getText().trim());
            ps.setString(40, txtNmTindakanSekunder4.getText().trim());
            ps.setString(41, txtKdTindakanSekunder4.getText().trim());
            ps.setString(42, txtNmTindakanSekunder5.getText().trim());
            ps.setString(43, txtKdTindakanSekunder5.getText().trim());

            ps.setString(44, txtNmPenunjangPrimer.getText().trim());
            ps.setString(45, txtKdPenunjangPrimer.getText().trim());
            ps.setString(46, txtNmPenunjangSekunder1.getText().trim());
            ps.setString(47, txtKdPenunjangSekunder1.getText().trim());
            ps.setString(48, txtNmPenunjangSekunder2.getText().trim());
            ps.setString(49, txtKdPenunjangSekunder2.getText().trim());
            ps.setString(50, txtNmPenunjangSekunder3.getText().trim());
            ps.setString(51, txtKdPenunjangSekunder3.getText().trim());
            ps.setString(52, txtPenandatanganPasien.getText().trim());

            ps.executeUpdate();
            ps.close();

            JOptionPane.showMessageDialog(null, "Data Berkas INA berhasil disimpan ke database!");
        } catch (Exception e) {
            System.out.println("Error simpan Lembar INA: " + e);
            JOptionPane.showMessageDialog(null, "Gagal menyimpan data: " + e.getMessage());
        }
    }

    private void hapus() {
        if (txtNoRawat.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Pilih data yang akan dihapus!");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(null, "Apakah Anda yakin ingin menghapus Berkas INA untuk No. Rawat " + txtNoRawat.getText() + "?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                PreparedStatement ps = koneksi.prepareStatement(
                    "DELETE FROM lembar_bukti_pelayanan_inacbg WHERE no_rawat=?"
                );
                ps.setString(1, txtNoRawat.getText().trim());
                ps.executeUpdate();
                ps.close();
                JOptionPane.showMessageDialog(null, "Data Berkas INA berhasil dihapus!");
                emptTeks();
            } catch (Exception e) {
                System.out.println("Error hapus Lembar INA: " + e);
                JOptionPane.showMessageDialog(null, "Gagal menghapus data: " + e.getMessage());
            }
        }
    }

    private String chkBox(boolean checked, String label) {
        String boxMark = checked ? "v" : "&nbsp;";
        return "<table cellpadding='0' cellspacing='0' style='border-collapse:collapse; display:inline; margin: 1px 0;'>" +
               "<tr>" +
               "<td style='border:1px solid #000; width:12px; height:12px; min-width:12px; max-width:12px; text-align:center; vertical-align:middle; line-height:10px; font-size:9pt; font-family:sans-serif; font-weight:bold;'>" + boxMark + "</td>" +
               "<td style='border:none; padding-left:5px; font-size:10.5pt; vertical-align:middle; font-family:Arial,sans-serif;'>" + label + "</td>" +
               "</tr>" +
               "</table>";
    }

    private String chkCara(boolean checked, String label) {
        String boxMark = checked ? "v" : "&nbsp;";
        return "<table cellpadding='0' cellspacing='0' style='border-collapse:collapse; display:inline; margin-right:15px;'>" +
               "<tr>" +
               "<td style='border:1px solid #000; width:12px; height:12px; min-width:12px; max-width:12px; text-align:center; vertical-align:middle; line-height:10px; font-size:9pt; font-family:sans-serif; font-weight:bold;'>" + boxMark + "</td>" +
               "<td style='border:none; padding-left:4px; font-size:12pt; vertical-align:middle; font-family:\"Times New Roman\",Times,serif;'>" + label + "</td>" +
               "</tr>" +
               "</table>";
    }

    private String buildHTMLContent() {
        String tahun = "2026";
        if (txtTglMasuk.getText().length() >= 4) {
            tahun = txtTglMasuk.getText().substring(0, 4);
        } else {
            tahun = new SimpleDateFormat("yyyy").format(new Date());
        }

        String caraPulangVal = cmbCaraPulang.getSelectedItem().toString();
        String cSembuh = chkCara("SEMBUH".equalsIgnoreCase(caraPulangVal), "Sembuh");
        String cPaksa = chkCara("PULANG PAKSA".equalsIgnoreCase(caraPulangVal), "Pulang Paksa");
        String cRujuk = chkCara("RUJUK".equalsIgnoreCase(caraPulangVal), "Rujuk");
        String cMati = chkCara("MENINGGAL DUNIA".equalsIgnoreCase(caraPulangVal), "Meninggal Dunia");

        String jkText = "L".equalsIgnoreCase(txtJK.getText()) ? "Laki-laki" : ("P".equalsIgnoreCase(txtJK.getText()) ? "Perempuan" : txtJK.getText());
        String totalTarifStr = txtTotalTarif.getText().trim().isEmpty() ? "Rp 0" : (txtTotalTarif.getText().startsWith("Rp") ? txtTotalTarif.getText() : "Rp " + txtTotalTarif.getText());
        String beratLahirStr = txtBeratLahir.getText().trim().isEmpty() ? "-" : txtBeratLahir.getText().trim();
        String adlSkorStr = txtSkorADL.getText().trim().isEmpty() ? "-" : txtSkorADL.getText().trim();

        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n<html>\n<head>\n<meta charset='utf-8'/>\n<style>\n");
        sb.append("@page { size: A4 portrait; margin-top: 12.7mm; margin-bottom: 12.7mm; margin-left: 12.7mm; margin-right: 12.7mm; }\n");
        sb.append("body { font-family: 'Times New Roman', Times, serif; font-size: 12pt; margin: 0; padding: 0; color: #000; line-height: 1.25; }\n");
        sb.append(".tbl-hdr { width: 100%; border-collapse: collapse; margin-bottom: 4px; }\n");
        sb.append(".tbl-hdr td { font-size: 14pt; font-weight: bold; vertical-align: top; border: none; }\n");
        sb.append(".tbl-adm { width: 100%; border-collapse: collapse; margin-bottom: 8px; }\n");
        sb.append(".tbl-adm td { border: none; font-size: 12pt; padding: 2px 2px; vertical-align: top; }\n");
        sb.append(".lbl-bold { font-weight: bold; }\n");
        sb.append(".sec-title { font-size: 13pt; font-weight: bold; margin-top: 6px; }\n");
        sb.append(".sub-title { font-size: 12pt; font-weight: bold; margin-top: 4px; margin-bottom: 4px; }\n");
        sb.append(".tbl-grid { width: 100%; border-collapse: collapse; margin-bottom: 12px; }\n");
        sb.append(".tbl-grid th { border: 1px solid #000; font-size: 12pt; font-weight: bold; padding: 4px 6px; text-align: left; vertical-align: top; }\n");
        sb.append(".tbl-grid td { border: 1px solid #000; font-size: 12pt; padding: 3px 6px; vertical-align: top; }\n");
        sb.append(".ttd-tbl { width: 100%; border-collapse: collapse; margin-top: 35px; }\n");
        sb.append(".ttd-tbl td { text-align: center; font-size: 12pt; vertical-align: top; border: none; }\n");
        sb.append(".page-break { page-break-after: always; break-after: page; }\n");
        sb.append("</style>\n</head>\n<body>\n");

        // PAGE 1: Header RS & Title
        sb.append("<table class='tbl-hdr'>\n");
        sb.append("<tr>\n");
        sb.append("  <td width='46%'>\n");
        sb.append("    LEMBAR BUKTI PELAYANAN<br/>\n");
        sb.append("    VARIABEL INACBG<br/>\n");
        sb.append("    TAHUN ").append(tahun).append("\n");
        sb.append("  </td>\n");
        sb.append("  <td width='54%'>\n");
        sb.append("    <table style='width:100%; font-size:14pt; font-weight:bold; border-collapse:collapse;'>\n");
        sb.append("      <tr><td width='32%'>Kode RS</td><td width='4%'>:</td><td>").append(akses.getkodeppkkemenkes() != null ? akses.getkodeppkkemenkes() : "-").append("</td></tr>\n");
        sb.append("      <tr><td>Nama RS</td><td>:</td><td>").append(akses.getnamars()).append("</td></tr>\n");
        sb.append("      <tr><td>Alamat</td><td>:</td><td>").append(akses.getalamatrs()).append("</td></tr>\n");
        sb.append("      <tr><td style='vertical-align:top;'>Kab /<br/>Prop</td><td style='vertical-align:top;'>:<br/>&nbsp;</td><td style='vertical-align:top;'>").append(akses.getkabupatenrs()).append(" / ").append(akses.getpropinsirs()).append("</td></tr>\n");
        sb.append("    </table>\n");
        sb.append("  </td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");

        // Garis Pembatas Ganda (Double Rule Kop RS)
        sb.append("<div style='border-top: 1px solid #000; border-bottom: 2.5px solid #000; height: 2px; margin: 4px 0 10px 0;'></div>\n");

        // Data Administratif Pasien (Table 2)
        sb.append("<table class='tbl-adm'>\n");
        sb.append("<tr>\n");
        sb.append("  <td width='20%' class='lbl-bold'>Nomor RM</td><td width='2%'>:</td><td width='28%'>").append(txtNoRM.getText()).append("</td>\n");
        sb.append("  <td width='22%' class='lbl-bold'>Jenis kelamin</td><td width='2%'>:</td><td width='26%'>").append(jkText).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td class='lbl-bold'>Nama Lengkap</td><td>:</td><td colspan='4'>").append(txtNmPasien.getText()).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td class='lbl-bold'>Tanggal Lahir</td><td>:</td><td>").append(txtTglLahir.getText()).append("</td>\n");
        sb.append("  <td class='lbl-bold'>Nomor Peserta</td><td>:</td><td colspan='2'>").append(txtNoPeserta.getText()).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td class='lbl-bold'>Nomor SEP</td><td>:</td><td colspan='4'>").append(txtNoSEP.getText()).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td class='lbl-bold'>Jenis Perawatan</td><td>:</td><td>").append(txtJenisPerawatan.getText()).append("</td>\n");
        sb.append("  <td class='lbl-bold'>Kelas</td><td>:</td><td colspan='2'>").append(txtKelas.getText().isEmpty() ? "-" : txtKelas.getText()).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr><td colspan='6' style='height: 4px;'></td></tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td class='lbl-bold'>Tanggal Masuk</td><td>:</td><td>").append(txtTglMasuk.getText()).append("</td>\n");
        sb.append("  <td class='lbl-bold'>Tanggal Keluar</td><td>:</td><td colspan='2'>").append(txtTglKeluar.getText()).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr><td colspan='6' style='height: 4px;'></td></tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td class='lbl-bold'>Cara Pulang</td><td>:</td><td colspan='4'>\n");
        sb.append("    <table cellpadding='0' cellspacing='0' style='border-collapse:collapse;'><tr>\n");
        sb.append("      <td>").append(cSembuh).append("</td>\n");
        sb.append("      <td>").append(cPaksa).append("</td>\n");
        sb.append("      <td>").append(cRujuk).append("</td>\n");
        sb.append("      <td>").append(cMati).append("</td>\n");
        sb.append("    </tr></table>\n");
        sb.append("  </td>\n");
        sb.append("</tr>\n");
        sb.append("<tr><td colspan='6' style='height: 4px;'></td></tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td class='lbl-bold'>DPJP</td><td>:</td><td colspan='4'>").append(txtNmDokter.getText()).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td class='lbl-bold'>Berat Lahir</td><td>:</td><td>").append(beratLahirStr).append("</td>\n");
        sb.append("  <td class='lbl-bold'>Total Tarif RS</td><td>:</td><td colspan='2'>").append(totalTarifStr).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td class='lbl-bold' style='vertical-align:top;'>ADL Skor<br/>(12 - 60)</td><td style='vertical-align:top;'>:<br/>&nbsp;</td><td colspan='4' style='vertical-align:top;'>").append(adlSkorStr).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");

        // SPESIAL CMG
        sb.append("<div class='sec-title'>Spesial CMG</div>\n");
        sb.append("<div style='border-top: 1px solid #000; margin: 2px 0 6px 0;'></div>\n");

        // 1. Spesial Prosedur (3 Kolom: 32%, 35%, 33%)
        sb.append("<div class='sub-title'>1. Spesial Prosedur</div>\n");
        sb.append("<table style='width:100%; border-collapse:collapse; margin-bottom: 6px;'>\n");
        sb.append("<tr>\n");
        sb.append("  <td width='32%'>").append(chkBox(cbPCL.isSelected(), "PCL")).append("</td>\n");
        sb.append("  <td width='35%'>").append(chkBox(cbColangiography.isSelected(), "Colangiography")).append("</td>\n");
        sb.append("  <td width='33%'>").append(chkBox(cbRepairSeptal.isSelected(), "Repair Septal Defect Heart")).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td>").append(chkBox(cbTimekotomy.isSelected(), "TIMEKOTOMY")).append("</td>\n");
        sb.append("  <td>").append(chkBox(cbKeratoplasty.isSelected(), "Keratoplasty")).append("</td>\n");
        sb.append("  <td>").append(chkBox(cbTorakotomi.isSelected(), "Torakotomi")).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td>").append(chkBox(cbTumorPineal.isSelected(), "Tumor Pineal Endoscopy")).append("</td>\n");
        sb.append("  <td>").append(chkBox(cbVitrectomy.isSelected(), "Vitrectomy")).append("</td>\n");
        sb.append("  <td>").append(chkBox(cbPhacoemulsification.isSelected(), "Phacoemulsification")).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td>").append(chkBox(cbMicrolaringoscopi.isSelected(), "Microlaringoscopi")).append("</td>\n");
        sb.append("  <td>").append(chkBox(cbHipKnee.isSelected(), "HIP / Knee Replacement")).append("</td>\n");
        sb.append("  <td>").append(chkBox(cbAirPlumbage.isSelected(), "Air Plumbage")).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td>").append(chkBox(cbLobektomi.isSelected(), "Lobektomi / Bilobektomi")).append("</td>\n");
        sb.append("  <td>").append(chkBox(cbStereotactic.isSelected(), "Stereotactic Surgery Radiotherapy")).append("</td>\n");
        sb.append("  <td>").append(chkBox(cbPancreatectomy.isSelected(), "Pancreatectomy")).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");

        // 2. Spesial Drug & 3. Spesial Investigation (50% - 50%)
        sb.append("<table style='width:100%; border-collapse:collapse; margin-bottom: 4px;'>\n");
        sb.append("<tr>\n");
        sb.append("  <td width='50%' style='vertical-align:top; padding-right:10px;'>\n");
        sb.append("    <div class='sub-title'>2. Spesial Drug</div>\n");
        sb.append("    <table style='width:100%; border-collapse:collapse;'>\n");
        sb.append("      <tr><td width='50%'>").append(chkBox(cbStreptokinase.isSelected(), "Streptokinase")).append("</td><td width='50%'>").append(chkBox(cbDeferifrone.isSelected(), "Deferifrone")).append("</td></tr>\n");
        sb.append("      <tr><td>").append(chkBox(cbDeferoksamin.isSelected(), "Deferoksamin")).append("</td><td>").append(chkBox(cbDeferasirox.isSelected(), "Deferasirox")).append("</td></tr>\n");
        sb.append("      <tr><td colspan='2'>").append(chkBox(cbHumanAlbumin.isSelected(), "Human Albumin")).append("</td></tr>\n");
        sb.append("    </table>\n");
        sb.append("  </td>\n");
        sb.append("  <td width='50%' style='vertical-align:top; padding-left:10px;'>\n");
        sb.append("    <div class='sub-title'>3. Spesial Investigation</div>\n");
        sb.append("    <table style='width:100%; border-collapse:collapse;'>\n");
        sb.append("      <tr><td width='42%'>").append(chkBox(cbOtherCTScan.isSelected(), "Other CT Scan")).append("</td><td width='58%'>").append(chkBox(cbNuclearMedicine.isSelected(), "Nuclear Medicine")).append("</td></tr>\n");
        sb.append("      <tr><td>").append(chkBox(cbMRI.isSelected(), "MRI")).append("</td><td>").append(chkBox(cbDiagnosticEye.isSelected(), "Diagnostic & Imaging Eye")).append("</td></tr>\n");
        sb.append("    </table>\n");
        sb.append("  </td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");

        // 4. Spesial Protheis
        sb.append("<div class='sub-title' style='margin-top:6px;'>4. Spesial Protheis</div>\n");
        sb.append("<table style='width:100%; border-collapse:collapse;'>\n");
        sb.append("<tr>\n");
        sb.append("  <td width='50%'>").append(chkBox(cbSubduratGrid.isSelected(), "Subdurat Grid Electrode")).append("</td>\n");
        sb.append("  <td width='50%'>").append(chkBox(cbCoteGraft.isSelected(), "Cote Graft")).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td>").append(chkBox(cbLiquidEmbolic.isSelected(), "Liquid Embolic (For AVM)")).append("</td>\n");
        sb.append("  <td>").append(chkBox(cbHipImplant.isSelected(), "HIP Implant / Knee Implant")).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");

        // PAGE BREAK
        sb.append("<div class='page-break'></div>\n");

        // PAGE 2: Diagnosa & Tindakan
        sb.append("<div class='sec-title' style='margin-top:0;'>Diagnosa & Tindakan</div>\n");
        sb.append("<div style='border-top: 1px solid #000; margin: 2px 0 6px 0;'></div>\n");

        sb.append("<table class='tbl-grid'>\n");
        sb.append("<tr>\n");
        sb.append("  <th width='38%'>Diagnosa (ICD-10)</th><th width='12%'>Kode</th>\n");
        sb.append("  <th width='38%'>Tindakan (ICD-9)</th><th width='12%'>Kode</th>\n");
        sb.append("</tr>\n");

        // Row 1 (Primer)
        String dPrimer = txtNmDiagPrimer.getText().trim().isEmpty() ? "Primer: -" : "Primer: " + txtNmDiagPrimer.getText().trim();
        String kdDPrimer = txtKdDiagPrimer.getText().trim().isEmpty() ? "-" : txtKdDiagPrimer.getText().trim();
        String tPrimer = txtNmTindakanPrimer.getText().trim().isEmpty() ? "Primer: -" : "Primer: " + txtNmTindakanPrimer.getText().trim();
        String kdTPrimer = txtKdTindakanPrimer.getText().trim().isEmpty() ? "-" : txtKdTindakanPrimer.getText().trim();
        sb.append("<tr>\n");
        sb.append("  <td>").append(dPrimer).append("</td><td>").append(kdDPrimer).append("</td>\n");
        sb.append("  <td>").append(tPrimer).append("</td><td>").append(kdTPrimer).append("</td>\n");
        sb.append("</tr>\n");

        // Row 2 (Sekunder 1)
        String dSek1 = txtNmDiagSekunder1.getText().trim().isEmpty() ? "Sekunder 1: -" : "Sekunder 1: " + txtNmDiagSekunder1.getText().trim();
        String kdDSek1 = txtKdDiagSekunder1.getText().trim().isEmpty() ? "-" : txtKdDiagSekunder1.getText().trim();
        String tSek1 = txtNmTindakanSekunder1.getText().trim().isEmpty() ? "Sekunder 1: -" : "Sekunder 1: " + txtNmTindakanSekunder1.getText().trim();
        String kdTSek1 = txtKdTindakanSekunder1.getText().trim().isEmpty() ? "-" : txtKdTindakanSekunder1.getText().trim();
        sb.append("<tr>\n");
        sb.append("  <td>").append(dSek1).append("</td><td>").append(kdDSek1).append("</td>\n");
        sb.append("  <td>").append(tSek1).append("</td><td>").append(kdTSek1).append("</td>\n");
        sb.append("</tr>\n");

        // Row 3 (Sekunder 2)
        String dSek2 = txtNmDiagSekunder2.getText().trim().isEmpty() ? "Sekunder 2: -" : "Sekunder 2: " + txtNmDiagSekunder2.getText().trim();
        String kdDSek2 = txtKdDiagSekunder2.getText().trim().isEmpty() ? "-" : txtKdDiagSekunder2.getText().trim();
        String tSek2 = txtNmTindakanSekunder2.getText().trim().isEmpty() ? "Sekunder 2: -" : "Sekunder 2: " + txtNmTindakanSekunder2.getText().trim();
        String kdTSek2 = txtKdTindakanSekunder2.getText().trim().isEmpty() ? "-" : txtKdTindakanSekunder2.getText().trim();
        sb.append("<tr>\n");
        sb.append("  <td>").append(dSek2).append("</td><td>").append(kdDSek2).append("</td>\n");
        sb.append("  <td>").append(tSek2).append("</td><td>").append(kdTSek2).append("</td>\n");
        sb.append("</tr>\n");

        // Row 4 (Sekunder 3)
        String dSek3 = txtNmDiagSekunder3.getText().trim().isEmpty() ? "Sekunder 3: -" : "Sekunder 3: " + txtNmDiagSekunder3.getText().trim();
        String kdDSek3 = txtKdDiagSekunder3.getText().trim().isEmpty() ? "-" : txtKdDiagSekunder3.getText().trim();
        String tSek3 = txtNmTindakanSekunder3.getText().trim().isEmpty() ? "Sekunder 3: -" : "Sekunder 3: " + txtNmTindakanSekunder3.getText().trim();
        String kdTSek3 = txtKdTindakanSekunder3.getText().trim().isEmpty() ? "-" : txtKdTindakanSekunder3.getText().trim();
        sb.append("<tr>\n");
        sb.append("  <td>").append(dSek3).append("</td><td>").append(kdDSek3).append("</td>\n");
        sb.append("  <td>").append(tSek3).append("</td><td>").append(kdTSek3).append("</td>\n");
        sb.append("</tr>\n");

        // Row 5 (Sekunder 4)
        String dSek4 = txtNmDiagSekunder4.getText().trim().isEmpty() ? "Sekunder 4: -" : "Sekunder 4: " + txtNmDiagSekunder4.getText().trim();
        String kdDSek4 = txtKdDiagSekunder4.getText().trim().isEmpty() ? "-" : txtKdDiagSekunder4.getText().trim();
        String tSek4 = txtNmTindakanSekunder4.getText().trim().isEmpty() ? "Sekunder 4: -" : "Sekunder 4: " + txtNmTindakanSekunder4.getText().trim();
        String kdTSek4 = txtKdTindakanSekunder4.getText().trim().isEmpty() ? "-" : txtKdTindakanSekunder4.getText().trim();
        sb.append("<tr>\n");
        sb.append("  <td>").append(dSek4).append("</td><td>").append(kdDSek4).append("</td>\n");
        sb.append("  <td>").append(tSek4).append("</td><td>").append(kdTSek4).append("</td>\n");
        sb.append("</tr>\n");

        // Row 6 (Sekunder 5)
        String dSek5 = txtNmDiagSekunder5.getText().trim().isEmpty() ? "Sekunder 5: -" : "Sekunder 5: " + txtNmDiagSekunder5.getText().trim();
        String kdDSek5 = txtKdDiagSekunder5.getText().trim().isEmpty() ? "-" : txtKdDiagSekunder5.getText().trim();
        String tSek5 = txtNmTindakanSekunder5.getText().trim().isEmpty() ? "Sekunder 5: -" : "Sekunder 5: " + txtNmTindakanSekunder5.getText().trim();
        String kdTSek5 = txtKdTindakanSekunder5.getText().trim().isEmpty() ? "-" : txtKdTindakanSekunder5.getText().trim();
        sb.append("<tr>\n");
        sb.append("  <td>").append(dSek5).append("</td><td>").append(kdDSek5).append("</td>\n");
        sb.append("  <td>").append(tSek5).append("</td><td>").append(kdTSek5).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");

        // PEMERIKSAAN PENUNJANG
        sb.append("<div class='sec-title' style='margin-top:10px;'>PEMERIKSAAN PENUNJANG</div>\n");
        sb.append("<div style='border-top: 1px solid #000; margin: 2px 0 6px 0;'></div>\n");

        sb.append("<table class='tbl-grid'>\n");
        sb.append("<tr>\n");
        sb.append("  <th width='72%'>Pemeriksaan Penunjang</th><th width='28%'>Kode Penunjang</th>\n");
        sb.append("</tr>\n");

        // Penunjang Primer
        String pPrimer = txtNmPenunjangPrimer.getText().trim().isEmpty() ? "Primer: -" : "Primer: " + txtNmPenunjangPrimer.getText().trim();
        String kdPPrimer = txtKdPenunjangPrimer.getText().trim().isEmpty() ? "-" : txtKdPenunjangPrimer.getText().trim();
        sb.append("<tr>\n");
        sb.append("  <td>").append(pPrimer).append("</td><td>").append(kdPPrimer).append("</td>\n");
        sb.append("</tr>\n");

        // Penunjang Sekunder 1
        String pSek1 = txtNmPenunjangSekunder1.getText().trim().isEmpty() ? "Sekunder 1: -" : "Sekunder 1: " + txtNmPenunjangSekunder1.getText().trim();
        String kdPSek1 = txtKdPenunjangSekunder1.getText().trim().isEmpty() ? "-" : txtKdPenunjangSekunder1.getText().trim();
        sb.append("<tr>\n");
        sb.append("  <td>").append(pSek1).append("</td><td>").append(kdPSek1).append("</td>\n");
        sb.append("</tr>\n");

        // Penunjang Sekunder 2
        String pSek2 = txtNmPenunjangSekunder2.getText().trim().isEmpty() ? "Sekunder 2: -" : "Sekunder 2: " + txtNmPenunjangSekunder2.getText().trim();
        String kdPSek2 = txtKdPenunjangSekunder2.getText().trim().isEmpty() ? "-" : txtKdPenunjangSekunder2.getText().trim();
        sb.append("<tr>\n");
        sb.append("  <td>").append(pSek2).append("</td><td>").append(kdPSek2).append("</td>\n");
        sb.append("</tr>\n");

        // Penunjang Sekunder 3
        String pSek3 = txtNmPenunjangSekunder3.getText().trim().isEmpty() ? "Sekunder 3: -" : "Sekunder 3: " + txtNmPenunjangSekunder3.getText().trim();
        String kdPSek3 = txtKdPenunjangSekunder3.getText().trim().isEmpty() ? "-" : txtKdPenunjangSekunder3.getText().trim();
        sb.append("<tr>\n");
        sb.append("  <td>").append(pSek3).append("</td><td>").append(kdPSek3).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");

        // Tanda Tangan DPJP & Pasien / Keluarga
        String dokterTtd = txtNmDokter.getText().trim().isEmpty() ? "dr. ................................" : txtNmDokter.getText().trim();
        String sipStr = txtSIPDokter.getText().trim().isEmpty() ? "SIP. -" : (txtSIPDokter.getText().startsWith("SIP") ? txtSIPDokter.getText() : "SIP. " + txtSIPDokter.getText());
        String pasienTtd = txtPenandatanganPasien.getText().trim().isEmpty() ? "................................" : txtPenandatanganPasien.getText().trim();

        sb.append("<table class='ttd-tbl'>\n");
        sb.append("<tr>\n");
        sb.append("  <td width='50%'>Dokter Penanggung Jawab</td>\n");
        sb.append("  <td width='50%'>Pasien/Keluarga Pasien</td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td style='height: 75px;'></td>\n");
        sb.append("  <td style='height: 75px;'></td>\n");
        sb.append("</tr>\n");
        sb.append("<tr>\n");
        sb.append("  <td>").append(dokterTtd).append("<br/>").append(sipStr).append("</td>\n");
        sb.append("  <td>").append(pasienTtd).append("</td>\n");
        sb.append("</tr>\n");
        sb.append("</table>\n");

        sb.append("</body>\n</html>\n");
        return sb.toString();
    }

    private void renderHTMLPreview() {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        try {
            loadHTML.setText(buildHTMLContent());
            loadHTML.setCaretPosition(0);
        } catch (Exception e) {
            System.out.println("Error renderHTMLPreview: " + e);
        } finally {
            setCursor(Cursor.getDefaultCursor());
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

            String fileName = "BerkasINA_" + txtNoRawat.getText().trim().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf";
            File pdfFile = new File(fileName);

            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                PdfWriter writer = new PdfWriter(fos);
                PdfDocument pdfDoc = new PdfDocument(writer);
                pdfDoc.setDefaultPageSize(PageSize.A4);

                ConverterProperties props = new ConverterProperties();
                HtmlConverter.convertToPdf(buildHTMLContent(), pdfDoc, props);
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

