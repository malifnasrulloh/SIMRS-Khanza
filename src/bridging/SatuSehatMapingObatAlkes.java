/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package bridging;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import inventory.DlgBarang;
import inventory.DlgCariKfa;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

/**
 *
 * @author dosen
 */
public final class SatuSehatMapingObatAlkes extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel=new sekuel();
    private validasi Valid=new validasi();
    private Connection koneksi=koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;    
    private int i=0;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private DlgBarang barang=new DlgBarang(null,false);
    private DlgCariKfa kfa=new DlgCariKfa(null,false);

    /** Creates new form DlgJnsPerawatanRalan
     * @param parent
     * @param modal */
    public SatuSehatMapingObatAlkes(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocation(8,1);
        setSize(628,674);

        tabMode=new DefaultTableModel(null,new Object[]{
                "KFA Code","KFA System","Kode Barang","Nama Obat/Alkes/BHP","KFA Display","Form Code",
                "Form System","Form Display","Numerator Code","Numerator System","Denominator Code",
                "Denominator System","Route Code","Route System","Route Display"
            }){
             @Override public boolean isCellEditable(int rowIndex, int colIndex){return false;}
        };
        tbJnsPerawatan.setModel(tabMode);

        tbJnsPerawatan.setPreferredScrollableViewportSize(new Dimension(500,500));
        tbJnsPerawatan.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 15; i++) {
            TableColumn column = tbJnsPerawatan.getColumnModel().getColumn(i);
            if(i==0){
                column.setPreferredWidth(80);
            }else if(i==1){
                column.setPreferredWidth(200);
            }else if(i==2){
                column.setPreferredWidth(85);
            }else if(i==3){
                column.setPreferredWidth(200);
            }else if(i==4){
                column.setPreferredWidth(200);
            }else if(i==5){
                column.setPreferredWidth(80);
            }else if(i==6){
                column.setPreferredWidth(200);
            }else if(i==7){
                column.setPreferredWidth(170);
            }else if(i==8){
                column.setPreferredWidth(90);
            }else if(i==9){
                column.setPreferredWidth(200);
            }else if(i==10){
                column.setPreferredWidth(100);
            }else if(i==11){
                column.setPreferredWidth(200);
            }else if(i==12){
                column.setPreferredWidth(90);
            }else if(i==13){
                column.setPreferredWidth(170);
            }else if(i==14){
                column.setPreferredWidth(170);
            }
        }
        tbJnsPerawatan.setDefaultRenderer(Object.class, new WarnaTable());

        KodeBarang.setDocument(new batasInput((byte)15).getKata(KodeBarang)); 
        KFACode.setDocument(new batasInput((byte)15).getKata(KFACode)); 
        KFASystem.setDocument(new batasInput((byte)100).getKata(KFASystem)); 
        KFADisplay.setDocument(new batasInput((byte)80).getKata(KFADisplay)); 
        FormCode.setDocument(new batasInput((byte)30).getKata(FormCode)); 
        FormSystem.setDocument(new batasInput((byte)100).getKata(FormSystem)); 
        FormDisplay.setDocument(new batasInput((byte)80).getKata(FormDisplay)); 
        NumoratorCode.setDocument(new batasInput((byte)15).getKata(NumoratorCode)); 
        NemeratorSystem.setDocument(new batasInput((byte)80).getKata(NemeratorSystem)); 
        DenominatorCode.setDocument(new batasInput((byte)15).getKata(DenominatorCode)); 
        DenominatorSystem.setDocument(new batasInput((byte)80).getKata(DenominatorSystem)); 
        RouteCode.setDocument(new batasInput((byte)30).getKata(RouteCode)); 
        RouteSystem.setDocument(new batasInput((byte)100).getKata(RouteSystem)); 
        RouteDisplay.setDocument(new batasInput((byte)80).getKata(RouteDisplay)); 
        TCari.setDocument(new batasInput((byte)100).getKata(TCari));                  
        
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        tampil();
                    }
                }
            });
        }  
        
        barang.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(barang.getTable().getSelectedRow()!= -1){                    
                    KodeBarang.setText(barang.getTable().getValueAt(barang.getTable().getSelectedRow(),1).toString());
                    NamaBarang.setText(barang.getTable().getValueAt(barang.getTable().getSelectedRow(),2).toString());
                    DenominatorCode.setText(barang.getTable().getValueAt(barang.getTable().getSelectedRow(),6).toString());
                }
                btnBarang.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        }); 
        
        barang.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    barang.dispose();
                }  
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        kfa.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(kfa.getTable().getSelectedRow()!= -1){
                    int row = kfa.getTable().getSelectedRow();
                    String kfaCodeVal = safeCell(kfa.getTable().getValueAt(row,0));
                    KFACode.setText(kfaCodeVal);
                    KFADisplay.setText(safeCell(kfa.getTable().getValueAt(row,1)));
                    String formCodeVal = safeCell(kfa.getTable().getValueAt(row,2));
                    String formDisplayVal = safeCell(kfa.getTable().getValueAt(row,3));
                    FormCode.setText(formCodeVal);
                    FormDisplay.setText(formDisplayVal);

                    String numCode = safeCell(kfa.getTable().getValueAt(row,4));
                    if (numCode.isEmpty()) {
                        numCode = lookupNumeratorByKfa(kfaCodeVal);
                    }
                    NumoratorCode.setText(numCode);

                    DenominatorCode.setText(deriveDenominator(formCodeVal, formDisplayVal));

                    String[] route = deriveRoute(formCodeVal, formDisplayVal);
                    RouteCode.setText(route[0]);
                    RouteDisplay.setText(route[1]);

                    KFASystem.setText("http://sys-ids.kemkes.go.id/kfa");
                    FormSystem.setText("http://terminology.kemkes.go.id/CodeSystem/medication-form");
                    NemeratorSystem.setText("http://unitsofmeasure.org");
                    DenominatorSystem.setText("http://terminology.hl7.org/CodeSystem/v3-orderableDrugForm");
                    RouteSystem.setText("http://www.whocc.no/atc");
                }
                kfa.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        }); 
        
        kfa.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    kfa.dispose();
                }  
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        
        
        
        BtnAutoMapping = new widget.Button();
        java.net.URL iconAuto = getClass().getResource("/picture/swap.png");
        if (iconAuto != null) BtnAutoMapping.setIcon(new javax.swing.ImageIcon(iconAuto));
        BtnAutoMapping.setMnemonic('A');
        BtnAutoMapping.setText("Auto Mapping");
        BtnAutoMapping.setToolTipText("Otomatis mapping seluruh databarang ke KFA (Alt+A)");
        BtnAutoMapping.setPreferredSize(new java.awt.Dimension(140, 30));
        BtnAutoMapping.addActionListener(new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int konfirm = JOptionPane.showConfirmDialog(null,
                    "Proses auto mapping akan memindai seluruh databarang dan\n" +
                    "mencocokkan dengan data KFA terbaru. Lanjutkan?",
                    "Konfirmasi", JOptionPane.YES_NO_OPTION);
                if (konfirm != JOptionPane.YES_OPTION) return;
                BtnAutoMapping.setEnabled(false);
                BtnAutoMapping.setText("Memproses...");
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                new javax.swing.SwingWorker<int[], Void>() {
                    @Override
                    protected int[] doInBackground() {
                        return autoMapping();
                    }
                    @Override
                    protected void done() {
                        BtnAutoMapping.setEnabled(true);
                        BtnAutoMapping.setText("Auto Mapping");
                        setCursor(Cursor.getDefaultCursor());
                        try {
                            int[] r = get();
                            JOptionPane.showMessageDialog(null,
                                "Auto mapping selesai.\n" +
                                "Berhasil    : " + r[0] + "\n" +
                                "Dilewati    : " + r[1] + "\n" +
                                "Tidak cocok : " + r[2],
                                "Informasi", JOptionPane.INFORMATION_MESSAGE);
                            tampil();
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(null,
                                "Error: " + ex.getMessage(),
                                "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }.execute();
            }
        });
        panelGlass8.add(BtnAutoMapping, panelGlass8.getComponentCount() - 1);
        panelGlass8.revalidate();

        ChkInput.setSelected(false);
        isForm();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        NamaBarang = new widget.TextBox();
        internalFrame1 = new widget.InternalFrame();
        Scroll = new widget.ScrollPane();
        tbJnsPerawatan = new widget.Table();
        jPanel3 = new javax.swing.JPanel();
        panelGlass8 = new widget.panelisi();
        BtnSimpan = new widget.Button();
        BtnBatal = new widget.Button();
        BtnHapus = new widget.Button();
        BtnEdit = new widget.Button();
        BtnPrint = new widget.Button();
        BtnAll = new widget.Button();
        BtnKeluar = new widget.Button();
        panelGlass9 = new widget.panelisi();
        jLabel6 = new widget.Label();
        TCari = new widget.TextBox();
        BtnCari = new widget.Button();
        jLabel7 = new widget.Label();
        LCount = new widget.Label();
        PanelInput = new javax.swing.JPanel();
        ChkInput = new widget.CekBox();
        FormInput = new widget.PanelBiasa();
        jLabel4 = new widget.Label();
        KodeBarang = new widget.TextBox();
        btnBarang = new widget.Button();
        jLabel5 = new widget.Label();
        FormCode = new widget.TextBox();
        jLabel8 = new widget.Label();
        NumoratorCode = new widget.TextBox();
        KFACode = new widget.TextBox();
        jLabel9 = new widget.Label();
        jLabel10 = new widget.Label();
        KFADisplay = new widget.TextBox();
        jLabel11 = new widget.Label();
        FormSystem = new widget.TextBox();
        FormDisplay = new widget.TextBox();
        jLabel12 = new widget.Label();
        jLabel13 = new widget.Label();
        jLabel14 = new widget.Label();
        NemeratorSystem = new widget.TextBox();
        DenominatorCode = new widget.TextBox();
        KFASystem = new widget.TextBox();
        jLabel15 = new widget.Label();
        DenominatorSystem = new widget.TextBox();
        jLabel16 = new widget.Label();
        RouteCode = new widget.TextBox();
        jLabel17 = new widget.Label();
        RouteSystem = new widget.TextBox();
        jLabel18 = new widget.Label();
        RouteDisplay = new widget.TextBox();
        btnKfa = new widget.Button();

        NamaBarang.setEditable(false);
        NamaBarang.setHighlighter(null);
        NamaBarang.setName("NamaBarang"); // NOI18N

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Mapping Obat/Alkes/BHP Satu Sehat ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        Scroll.setName("Scroll"); // NOI18N
        Scroll.setOpaque(true);

        tbJnsPerawatan.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbJnsPerawatan.setName("tbJnsPerawatan"); // NOI18N
        tbJnsPerawatan.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbJnsPerawatanMouseClicked(evt);
            }
        });
        tbJnsPerawatan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbJnsPerawatanKeyReleased(evt);
            }
        });
        Scroll.setViewportView(tbJnsPerawatan);

        internalFrame1.add(Scroll, java.awt.BorderLayout.CENTER);

        jPanel3.setName("jPanel3"); // NOI18N
        jPanel3.setOpaque(false);
        jPanel3.setPreferredSize(new java.awt.Dimension(44, 100));
        jPanel3.setLayout(new java.awt.BorderLayout(1, 1));

        panelGlass8.setName("panelGlass8"); // NOI18N
        panelGlass8.setPreferredSize(new java.awt.Dimension(44, 44));
        panelGlass8.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 9));

        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png"))); // NOI18N
        BtnSimpan.setMnemonic('S');
        BtnSimpan.setText("Simpan");
        BtnSimpan.setToolTipText("Alt+S");
        BtnSimpan.setName("BtnSimpan"); // NOI18N
        BtnSimpan.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnSimpan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnSimpanActionPerformed(evt);
            }
        });
        BtnSimpan.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnSimpanKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnSimpan);

        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png"))); // NOI18N
        BtnBatal.setMnemonic('B');
        BtnBatal.setText("Baru");
        BtnBatal.setToolTipText("Alt+B");
        BtnBatal.setName("BtnBatal"); // NOI18N
        BtnBatal.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnBatal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnBatalActionPerformed(evt);
            }
        });
        BtnBatal.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnBatalKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnBatal);

        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png"))); // NOI18N
        BtnHapus.setMnemonic('H');
        BtnHapus.setText("Hapus");
        BtnHapus.setToolTipText("Alt+H");
        BtnHapus.setName("BtnHapus"); // NOI18N
        BtnHapus.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnHapus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnHapusActionPerformed(evt);
            }
        });
        BtnHapus.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnHapusKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnHapus);

        BtnEdit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnEdit.setMnemonic('G');
        BtnEdit.setText("Ganti");
        BtnEdit.setToolTipText("Alt+G");
        BtnEdit.setName("BtnEdit"); // NOI18N
        BtnEdit.setPreferredSize(new java.awt.Dimension(100, 30));
        BtnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnEditActionPerformed(evt);
            }
        });
        BtnEdit.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnEditKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnEdit);

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
        BtnPrint.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnPrintKeyPressed(evt);
            }
        });
        panelGlass8.add(BtnPrint);

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

        jLabel6.setText("Key Word :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelGlass9.add(jLabel6);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(450, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelGlass9.add(TCari);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('2');
        BtnCari.setToolTipText("Alt+2");
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

        jLabel7.setText("Record :");
        jLabel7.setName("jLabel7"); // NOI18N
        jLabel7.setPreferredSize(new java.awt.Dimension(75, 23));
        panelGlass9.add(jLabel7);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(80, 23));
        panelGlass9.add(LCount);

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        PanelInput.setName("PanelInput"); // NOI18N
        PanelInput.setOpaque(false);
        PanelInput.setPreferredSize(new java.awt.Dimension(660, 245));
        PanelInput.setLayout(new java.awt.BorderLayout(1, 1));

        ChkInput.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setMnemonic('I');
        ChkInput.setText(".: Input Data");
        ChkInput.setToolTipText("Alt+I");
        ChkInput.setBorderPainted(true);
        ChkInput.setBorderPaintedFlat(true);
        ChkInput.setFocusable(false);
        ChkInput.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ChkInput.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        ChkInput.setName("ChkInput"); // NOI18N
        ChkInput.setPreferredSize(new java.awt.Dimension(192, 20));
        ChkInput.setRolloverIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/143.png"))); // NOI18N
        ChkInput.setRolloverSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/145.png"))); // NOI18N
        ChkInput.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ChkInputActionPerformed(evt);
            }
        });
        PanelInput.add(ChkInput, java.awt.BorderLayout.PAGE_END);

        FormInput.setName("FormInput"); // NOI18N
        FormInput.setPreferredSize(new java.awt.Dimension(100, 104));
        FormInput.setLayout(null);

        jLabel4.setText("KFA System :");
        jLabel4.setName("jLabel4"); // NOI18N
        FormInput.add(jLabel4);
        jLabel4.setBounds(345, 10, 80, 23);

        KodeBarang.setEditable(false);
        KodeBarang.setHighlighter(null);
        KodeBarang.setName("KodeBarang"); // NOI18N
        FormInput.add(KodeBarang);
        KodeBarang.setBounds(240, 10, 90, 23);

        btnBarang.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnBarang.setMnemonic('1');
        btnBarang.setToolTipText("Alt+1");
        btnBarang.setName("btnBarang"); // NOI18N
        btnBarang.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBarangActionPerformed(evt);
            }
        });
        btnBarang.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnBarangKeyPressed(evt);
            }
        });
        FormInput.add(btnBarang);
        btnBarang.setBounds(330, 10, 28, 23);

        jLabel5.setText("Form Code :");
        jLabel5.setName("jLabel5"); // NOI18N
        FormInput.add(jLabel5);
        jLabel5.setBounds(0, 70, 105, 23);

        FormCode.setHighlighter(null);
        FormCode.setName("FormCode"); // NOI18N
        FormCode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FormCodeKeyPressed(evt);
            }
        });
        FormInput.add(FormCode);
        FormCode.setBounds(109, 70, 80, 23);

        jLabel8.setText("Numerator Code :");
        jLabel8.setName("jLabel8"); // NOI18N
        FormInput.add(jLabel8);
        jLabel8.setBounds(0, 130, 105, 23);

        NumoratorCode.setHighlighter(null);
        NumoratorCode.setName("NumoratorCode"); // NOI18N
        NumoratorCode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NumoratorCodeKeyPressed(evt);
            }
        });
        FormInput.add(NumoratorCode);
        NumoratorCode.setBounds(109, 130, 70, 23);

        KFACode.setHighlighter(null);
        KFACode.setName("KFACode"); // NOI18N
        KFACode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KFACodeKeyPressed(evt);
            }
        });
        FormInput.add(KFACode);
        KFACode.setBounds(109, 10, 100, 23);

        jLabel9.setText("KFA Code :");
        jLabel9.setName("jLabel9"); // NOI18N
        FormInput.add(jLabel9);
        jLabel9.setBounds(0, 10, 105, 23);

        jLabel10.setText("KFA Display :");
        jLabel10.setName("jLabel10"); // NOI18N
        FormInput.add(jLabel10);
        jLabel10.setBounds(0, 40, 105, 23);

        KFADisplay.setHighlighter(null);
        KFADisplay.setName("KFADisplay"); // NOI18N
        KFADisplay.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KFADisplayKeyPressed(evt);
            }
        });
        FormInput.add(KFADisplay);
        KFADisplay.setBounds(109, 40, 615, 23);

        jLabel11.setText("Form System :");
        jLabel11.setName("jLabel11"); // NOI18N
        FormInput.add(jLabel11);
        jLabel11.setBounds(192, 70, 90, 23);

        FormSystem.setHighlighter(null);
        FormSystem.setName("FormSystem"); // NOI18N
        FormSystem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FormSystemKeyPressed(evt);
            }
        });
        FormInput.add(FormSystem);
        FormSystem.setBounds(286, 70, 438, 23);

        FormDisplay.setHighlighter(null);
        FormDisplay.setName("FormDisplay"); // NOI18N
        FormDisplay.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                FormDisplayKeyPressed(evt);
            }
        });
        FormInput.add(FormDisplay);
        FormDisplay.setBounds(109, 100, 615, 23);

        jLabel12.setText("Form Display :");
        jLabel12.setName("jLabel12"); // NOI18N
        FormInput.add(jLabel12);
        jLabel12.setBounds(0, 100, 105, 23);

        jLabel13.setText("Denomina Code :");
        jLabel13.setName("jLabel13"); // NOI18N
        FormInput.add(jLabel13);
        jLabel13.setBounds(0, 160, 105, 23);

        jLabel14.setText("Numerator System :");
        jLabel14.setName("jLabel14"); // NOI18N
        FormInput.add(jLabel14);
        jLabel14.setBounds(180, 130, 130, 23);

        NemeratorSystem.setHighlighter(null);
        NemeratorSystem.setName("NemeratorSystem"); // NOI18N
        NemeratorSystem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                NemeratorSystemKeyPressed(evt);
            }
        });
        FormInput.add(NemeratorSystem);
        NemeratorSystem.setBounds(314, 130, 410, 23);

        DenominatorCode.setHighlighter(null);
        DenominatorCode.setName("DenominatorCode"); // NOI18N
        DenominatorCode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DenominatorCodeKeyPressed(evt);
            }
        });
        FormInput.add(DenominatorCode);
        DenominatorCode.setBounds(109, 160, 70, 23);

        KFASystem.setHighlighter(null);
        KFASystem.setName("KFASystem"); // NOI18N
        KFASystem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                KFASystemKeyPressed(evt);
            }
        });
        FormInput.add(KFASystem);
        KFASystem.setBounds(429, 10, 295, 23);

        jLabel15.setText("Denominator System :");
        jLabel15.setName("jLabel15"); // NOI18N
        FormInput.add(jLabel15);
        jLabel15.setBounds(180, 160, 130, 23);

        DenominatorSystem.setHighlighter(null);
        DenominatorSystem.setName("DenominatorSystem"); // NOI18N
        DenominatorSystem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                DenominatorSystemKeyPressed(evt);
            }
        });
        FormInput.add(DenominatorSystem);
        DenominatorSystem.setBounds(314, 160, 410, 23);

        jLabel16.setText("Route Code :");
        jLabel16.setName("jLabel16"); // NOI18N
        FormInput.add(jLabel16);
        jLabel16.setBounds(0, 190, 105, 23);

        RouteCode.setHighlighter(null);
        RouteCode.setName("RouteCode"); // NOI18N
        RouteCode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RouteCodeKeyPressed(evt);
            }
        });
        FormInput.add(RouteCode);
        RouteCode.setBounds(109, 190, 70, 23);

        jLabel17.setText("Route System :");
        jLabel17.setName("jLabel17"); // NOI18N
        FormInput.add(jLabel17);
        jLabel17.setBounds(185, 190, 90, 23);

        RouteSystem.setHighlighter(null);
        RouteSystem.setName("RouteSystem"); // NOI18N
        RouteSystem.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RouteSystemKeyPressed(evt);
            }
        });
        FormInput.add(RouteSystem);
        RouteSystem.setBounds(279, 190, 187, 23);

        jLabel18.setText("Route Display :");
        jLabel18.setName("jLabel18"); // NOI18N
        FormInput.add(jLabel18);
        jLabel18.setBounds(469, 190, 91, 23);

        RouteDisplay.setHighlighter(null);
        RouteDisplay.setName("RouteDisplay"); // NOI18N
        RouteDisplay.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                RouteDisplayKeyPressed(evt);
            }
        });
        FormInput.add(RouteDisplay);
        RouteDisplay.setBounds(564, 190, 160, 23);

        btnKfa.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/190.png"))); // NOI18N
        btnKfa.setMnemonic('1');
        btnKfa.setToolTipText("Alt+1");
        btnKfa.setName("btnKfa"); // NOI18N
        btnKfa.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnKfaActionPerformed(evt);
            }
        });
        btnKfa.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                btnKfaKeyPressed(evt);
            }
        });
        FormInput.add(btnKfa);
        btnKfa.setBounds(210, 10, 28, 23);

        PanelInput.add(FormInput, java.awt.BorderLayout.CENTER);

        internalFrame1.add(PanelInput, java.awt.BorderLayout.PAGE_START);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnBarangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBarangActionPerformed
        DlgBarang barang=new DlgBarang(null,false);
        barang.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {}
            @Override
            public void windowClosing(WindowEvent e) {}
            @Override
            public void windowClosed(WindowEvent e) {
                if(barang.getTable().getSelectedRow()!= -1){                    
                    KodeBarang.setText(barang.getTable().getValueAt(barang.getTable().getSelectedRow(),1).toString());
                    NamaBarang.setText(barang.getTable().getValueAt(barang.getTable().getSelectedRow(),2).toString());
                }
                btnBarang.requestFocus();
            }
            @Override
            public void windowIconified(WindowEvent e) {}
            @Override
            public void windowDeiconified(WindowEvent e) {}
            @Override
            public void windowActivated(WindowEvent e) {}
            @Override
            public void windowDeactivated(WindowEvent e) {}
        }); 
        
        barang.getTable().addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode()==KeyEvent.VK_SPACE){
                    barang.dispose();
                }  
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        barang.isCek();
        barang.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        barang.setLocationRelativeTo(internalFrame1);
        barang.setVisible(true);
}//GEN-LAST:event_btnBarangActionPerformed

    private void btnBarangKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnBarangKeyPressed
        Valid.pindah(evt, KFASystem, KFADisplay);
}//GEN-LAST:event_btnBarangKeyPressed

    private void BtnSimpanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnSimpanActionPerformed
        if(KFACode.getText().trim().equals("")){
            Valid.textKosong(KFACode,"KFA Code");
        }else if(KFASystem.getText().trim().equals("")){
            Valid.textKosong(KFASystem,"KFA System");
        }else if(NamaBarang.getText().trim().equals("")){
            Valid.textKosong(NamaBarang,"Obat/Alkes/BHP");
        }else if(KFADisplay.getText().trim().equals("")){
            Valid.textKosong(KFADisplay,"KFA Display");
        }else if(FormCode.getText().trim().equals("")){
            Valid.textKosong(FormCode,"Form Code");
        }else if(FormSystem.getText().trim().equals("")){
            Valid.textKosong(FormSystem,"Form System");
        }else if(FormDisplay.getText().trim().equals("")){
            Valid.textKosong(FormDisplay,"Form Display");
        }else if(NumoratorCode.getText().trim().equals("")){
            Valid.textKosong(NumoratorCode,"Numorator Code");
        }else if(NemeratorSystem.getText().trim().equals("")){
            Valid.textKosong(NemeratorSystem,"Nemerator System");
        }else if(DenominatorCode.getText().trim().equals("")){
            Valid.textKosong(DenominatorCode,"Denominator Code");
        }else if(DenominatorSystem.getText().trim().equals("")){
            Valid.textKosong(DenominatorSystem,"Denominator System");
        }else if(RouteCode.getText().trim().equals("")){
            Valid.textKosong(RouteCode,"Route Code");
        }else if(RouteSystem.getText().trim().equals("")){
            Valid.textKosong(RouteSystem,"Route System");
        }else if(RouteDisplay.getText().trim().equals("")){
            Valid.textKosong(RouteDisplay,"Route Display");
        }else{
            if(Sequel.menyimpantf("satu_sehat_mapping_obat","?,?,?,?,?,?,?,?,?,?,?,?,?,?","Mapping KFA",14,new String[]{
                KodeBarang.getText(),KFACode.getText(),KFASystem.getText(),KFADisplay.getText(),FormCode.getText(),
                FormSystem.getText(),FormDisplay.getText(),NumoratorCode.getText(),NemeratorSystem.getText(),DenominatorCode.getText(),
                DenominatorSystem.getText(),RouteCode.getText(),RouteSystem.getText(),RouteDisplay.getText()
            })==true){
                tabMode.addRow(new String[]{
                    KFACode.getText(),KFASystem.getText(),KodeBarang.getText(),NamaBarang.getText(),KFADisplay.getText(),FormCode.getText(),
                    FormSystem.getText(),FormDisplay.getText(),NumoratorCode.getText(),NemeratorSystem.getText(),DenominatorCode.getText(),
                    DenominatorSystem.getText(),RouteCode.getText(),RouteSystem.getText(),RouteDisplay.getText()
                });
                emptTeks();
                LCount.setText(""+tabMode.getRowCount());
            }                
        }
}//GEN-LAST:event_BtnSimpanActionPerformed

    private void BtnSimpanKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnSimpanKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnSimpanActionPerformed(null);
        }else{Valid.pindah(evt,RouteDisplay, BtnBatal);}
}//GEN-LAST:event_BtnSimpanKeyPressed

    private void BtnBatalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnBatalActionPerformed
        emptTeks();
}//GEN-LAST:event_BtnBatalActionPerformed

    private void BtnBatalKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnBatalKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            emptTeks();
        }else{Valid.pindah(evt, BtnSimpan, BtnHapus);}
}//GEN-LAST:event_BtnBatalKeyPressed

    private void BtnHapusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnHapusActionPerformed
        if(Valid.hapusTabletf(tabMode,KodeBarang,"satu_sehat_mapping_obat","kode_brng")==true){
            tabMode.removeRow(tbJnsPerawatan.getSelectedRow());
            emptTeks();
            LCount.setText(""+tabMode.getRowCount());
        }
}//GEN-LAST:event_BtnHapusActionPerformed

    private void BtnHapusKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnHapusKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnHapusActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnBatal, BtnEdit);
        }
}//GEN-LAST:event_BtnHapusKeyPressed

    private void BtnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnEditActionPerformed
        if(KFACode.getText().trim().equals("")){
            Valid.textKosong(KFACode,"KFA Code");
        }else if(KFASystem.getText().trim().equals("")){
            Valid.textKosong(KFASystem,"KFA System");
        }else if(KodeBarang.getText().trim().equals("")){
            Valid.textKosong(KodeBarang,"Obat/Alkes/BHP");
        }else if(KFADisplay.getText().trim().equals("")){
            Valid.textKosong(KFADisplay,"KFA Display");
        }else if(FormCode.getText().trim().equals("")){
            Valid.textKosong(FormCode,"Form Code");
        }else if(FormSystem.getText().trim().equals("")){
            Valid.textKosong(FormSystem,"Form System");
        }else if(FormDisplay.getText().trim().equals("")){
            Valid.textKosong(FormDisplay,"Form Display");
        }else if(NumoratorCode.getText().trim().equals("")){
            Valid.textKosong(NumoratorCode,"Numorator Code");
        }else if(NemeratorSystem.getText().trim().equals("")){
            Valid.textKosong(NemeratorSystem,"Nemerator System");
        }else if(DenominatorCode.getText().trim().equals("")){
            Valid.textKosong(DenominatorCode,"Denominator Code");
        }else if(DenominatorSystem.getText().trim().equals("")){
            Valid.textKosong(DenominatorSystem,"Denominator System");
        }else if(RouteCode.getText().trim().equals("")){
            Valid.textKosong(RouteCode,"Route Code");
        }else if(RouteSystem.getText().trim().equals("")){
            Valid.textKosong(RouteSystem,"Route System");
        }else if(RouteDisplay.getText().trim().equals("")){
            Valid.textKosong(RouteDisplay,"Route Display");
        }else{
            if(tbJnsPerawatan.getSelectedRow()>-1){
                if(Sequel.mengedittf("satu_sehat_mapping_obat","kode_brng=?","kode_brng=?,obat_code=?,obat_system=?,obat_display=?,"+
                        "form_code=?,form_system=?,form_display=?,numerator_code=?,numerator_system=?,denominator_code=?,denominator_system=?,"+
                        "route_code=?,route_system=?,route_display=?",15,new String[]{
                        KodeBarang.getText(),KFACode.getText(),KFASystem.getText(),KFADisplay.getText(),FormCode.getText(),
                        FormSystem.getText(),FormDisplay.getText(),NumoratorCode.getText(),NemeratorSystem.getText(),DenominatorCode.getText(),
                        DenominatorSystem.getText(),RouteCode.getText(),RouteSystem.getText(),RouteDisplay.getText(),
                        tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),2).toString()
                    })==true){
                    tabMode.setValueAt(KFACode.getText(),tbJnsPerawatan.getSelectedRow(),0);
                    tabMode.setValueAt(KFASystem.getText(),tbJnsPerawatan.getSelectedRow(),1);
                    tabMode.setValueAt(KodeBarang.getText(),tbJnsPerawatan.getSelectedRow(),2);
                    tabMode.setValueAt(NamaBarang.getText(),tbJnsPerawatan.getSelectedRow(),3);
                    tabMode.setValueAt(KFADisplay.getText(),tbJnsPerawatan.getSelectedRow(),4);
                    tabMode.setValueAt(FormCode.getText(),tbJnsPerawatan.getSelectedRow(),5);
                    tabMode.setValueAt(FormSystem.getText(),tbJnsPerawatan.getSelectedRow(),6);
                    tabMode.setValueAt(FormDisplay.getText(),tbJnsPerawatan.getSelectedRow(),7);
                    tabMode.setValueAt(NumoratorCode.getText(),tbJnsPerawatan.getSelectedRow(),8);
                    tabMode.setValueAt(NemeratorSystem.getText(),tbJnsPerawatan.getSelectedRow(),9);
                    tabMode.setValueAt(DenominatorCode.getText(),tbJnsPerawatan.getSelectedRow(),10);
                    tabMode.setValueAt(DenominatorSystem.getText(),tbJnsPerawatan.getSelectedRow(),11);
                    tabMode.setValueAt(RouteCode.getText(),tbJnsPerawatan.getSelectedRow(),12);
                    tabMode.setValueAt(RouteSystem.getText(),tbJnsPerawatan.getSelectedRow(),13);
                    tabMode.setValueAt(RouteDisplay.getText(),tbJnsPerawatan.getSelectedRow(),14);
                    emptTeks();
                }
            }                
        }
}//GEN-LAST:event_BtnEditActionPerformed

    private void BtnEditKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnEditKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnEditActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnHapus, BtnPrint);
        }
}//GEN-LAST:event_BtnEditKeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
}//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            dispose();
        }else{Valid.pindah(evt,BtnEdit,TCari);}
}//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if(tabMode.getRowCount()==0){
            JOptionPane.showMessageDialog(null,"Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            BtnBatal.requestFocus();
        }else if(tabMode.getRowCount()!=0){            
                Map<String, Object> param = new HashMap<>();    
                param.put("namars",akses.getnamars());
                param.put("alamatrs",akses.getalamatrs());
                param.put("kotars",akses.getkabupatenrs());
                param.put("propinsirs",akses.getpropinsirs());
                param.put("kontakrs",akses.getkontakrs());
                param.put("emailrs",akses.getemailrs());   
                param.put("logo",Sequel.cariGambar("select setting.logo from setting")); 
                param.put("parameter","%"+TCari.getText().trim()+"%");
                Valid.MyReport("rptMapingKFASatuSehat.jasper","report","::[ Mapping Obat/Alkes/BHP Satu Sehat Kemenkes ]::",param);            
        }
        this.setCursor(Cursor.getDefaultCursor());
}//GEN-LAST:event_BtnPrintActionPerformed

    private void BtnPrintKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnPrintKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnPrintActionPerformed(null);
        }else{
            Valid.pindah(evt, BtnEdit, BtnKeluar);
        }
}//GEN-LAST:event_BtnPrintKeyPressed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            BtnCariActionPerformed(null);
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_DOWN){
            BtnCari.requestFocus();
        }else if(evt.getKeyCode()==KeyEvent.VK_PAGE_UP){
            BtnKeluar.requestFocus();
        }
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        runBackground(() ->tampil());
}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            BtnCariActionPerformed(null);
        }else{
            Valid.pindah(evt, TCari, BtnAll);
        }
}//GEN-LAST:event_BtnCariKeyPressed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() ->tampil());
}//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if(evt.getKeyCode()==KeyEvent.VK_SPACE){
            TCari.setText("");
            runBackground(() ->tampil());
        }else{
            Valid.pindah(evt, BtnPrint, BtnKeluar);
        }
}//GEN-LAST:event_BtnAllKeyPressed

    private void tbJnsPerawatanMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbJnsPerawatanMouseClicked
        if(tabMode.getRowCount()!=0){
            try {
                getData();
            } catch (java.lang.NullPointerException e) {
            }
        }
}//GEN-LAST:event_tbJnsPerawatanMouseClicked

    private void tbJnsPerawatanKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbJnsPerawatanKeyReleased
        if(tabMode.getRowCount()!=0){
            if((evt.getKeyCode()==KeyEvent.VK_ENTER)||(evt.getKeyCode()==KeyEvent.VK_UP)||(evt.getKeyCode()==KeyEvent.VK_DOWN)){
                try {
                    getData();
                } catch (java.lang.NullPointerException e) {
                }
            }
        }
    }//GEN-LAST:event_tbJnsPerawatanKeyReleased

    private void ChkInputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ChkInputActionPerformed
        isForm();
    }//GEN-LAST:event_ChkInputActionPerformed

    private void KFACodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KFACodeKeyPressed
        Valid.pindah(evt, TCari, KFASystem);
    }//GEN-LAST:event_KFACodeKeyPressed

    private void KFASystemKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KFASystemKeyPressed
        Valid.pindah(evt, btnBarang, KFADisplay);
    }//GEN-LAST:event_KFASystemKeyPressed

    private void KFADisplayKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_KFADisplayKeyPressed
        Valid.pindah(evt, KFASystem, FormCode);
    }//GEN-LAST:event_KFADisplayKeyPressed

    private void FormCodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_FormCodeKeyPressed
        Valid.pindah(evt, KFADisplay, FormSystem);
    }//GEN-LAST:event_FormCodeKeyPressed

    private void FormSystemKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_FormSystemKeyPressed
        Valid.pindah(evt, FormCode, FormDisplay);
    }//GEN-LAST:event_FormSystemKeyPressed

    private void FormDisplayKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_FormDisplayKeyPressed
        Valid.pindah(evt, FormSystem, NumoratorCode);
    }//GEN-LAST:event_FormDisplayKeyPressed

    private void NumoratorCodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NumoratorCodeKeyPressed
        Valid.pindah(evt, FormDisplay, NemeratorSystem);
    }//GEN-LAST:event_NumoratorCodeKeyPressed

    private void NemeratorSystemKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_NemeratorSystemKeyPressed
        Valid.pindah(evt, NumoratorCode, DenominatorCode);
    }//GEN-LAST:event_NemeratorSystemKeyPressed

    private void DenominatorCodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DenominatorCodeKeyPressed
        Valid.pindah(evt, NemeratorSystem, DenominatorSystem);
    }//GEN-LAST:event_DenominatorCodeKeyPressed

    private void DenominatorSystemKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_DenominatorSystemKeyPressed
        Valid.pindah(evt, DenominatorCode, RouteCode);
    }//GEN-LAST:event_DenominatorSystemKeyPressed

    private void RouteCodeKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RouteCodeKeyPressed
        Valid.pindah(evt, DenominatorSystem, RouteSystem);
    }//GEN-LAST:event_RouteCodeKeyPressed

    private void RouteSystemKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RouteSystemKeyPressed
        Valid.pindah(evt, RouteCode, RouteDisplay);
    }//GEN-LAST:event_RouteSystemKeyPressed

    private void RouteDisplayKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_RouteDisplayKeyPressed
        Valid.pindah(evt, RouteSystem, BtnSimpan);
    }//GEN-LAST:event_RouteDisplayKeyPressed

    private void btnKfaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnKfaActionPerformed
        kfa.isCek();
        kfa.setSize(internalFrame1.getWidth()-20,internalFrame1.getHeight()-20);
        kfa.setLocationRelativeTo(internalFrame1);
        kfa.setVisible(true);
    }//GEN-LAST:event_btnKfaActionPerformed

    private void btnKfaKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_btnKfaKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnKfaKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        if(koneksiDB.CARICEPAT().equals("aktif")){
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener(){
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void removeUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
                @Override
                public void changedUpdate(DocumentEvent e) {
                    if(TCari.getText().length()>2){
                        runBackground(() ->tampil());
                    }
                }
            });
        }
    }//GEN-LAST:event_formWindowOpened

    /**
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            SatuSehatMapingObatAlkes dialog = new SatuSehatMapingObatAlkes(new javax.swing.JFrame(), true);
            dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    System.exit(0);
                }
            });
            dialog.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnAll;
    private widget.Button BtnBatal;
    private widget.Button BtnCari;
    private widget.Button BtnEdit;
    private widget.Button BtnHapus;
    private widget.Button BtnKeluar;
    private widget.Button BtnPrint;
    private widget.Button BtnSimpan;
    private widget.CekBox ChkInput;
    private widget.TextBox DenominatorCode;
    private widget.TextBox DenominatorSystem;
    private widget.TextBox FormCode;
    private widget.TextBox FormDisplay;
    private widget.PanelBiasa FormInput;
    private widget.TextBox FormSystem;
    private widget.TextBox KFACode;
    private widget.TextBox KFADisplay;
    private widget.TextBox KFASystem;
    private widget.TextBox KodeBarang;
    private widget.Label LCount;
    private widget.TextBox NamaBarang;
    private widget.TextBox NemeratorSystem;
    private widget.TextBox NumoratorCode;
    private javax.swing.JPanel PanelInput;
    private widget.TextBox RouteCode;
    private widget.TextBox RouteDisplay;
    private widget.TextBox RouteSystem;
    private widget.ScrollPane Scroll;
    private widget.TextBox TCari;
    private widget.Button btnBarang;
    private widget.Button btnKfa;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel10;
    private widget.Label jLabel11;
    private widget.Label jLabel12;
    private widget.Label jLabel13;
    private widget.Label jLabel14;
    private widget.Label jLabel15;
    private widget.Label jLabel16;
    private widget.Label jLabel17;
    private widget.Label jLabel18;
    private widget.Label jLabel4;
    private widget.Label jLabel5;
    private widget.Label jLabel6;
    private widget.Label jLabel7;
    private widget.Label jLabel8;
    private widget.Label jLabel9;
    private javax.swing.JPanel jPanel3;
    private widget.panelisi panelGlass8;
    private widget.panelisi panelGlass9;
    private widget.Table tbJnsPerawatan;
    // End of variables declaration//GEN-END:variables
    private widget.Button BtnAutoMapping;

    /** Ekstrak unit ukur dari kekuatan zat aktif, mis: "125 mg" → "mg", "10 mg/1 mL" → "mg" */
    private String safeCell(Object v) {
        if (v == null) return "";
        String s = v.toString().trim();
        return "null".equalsIgnoreCase(s) ? "" : s;
    }

    private String lookupNumeratorByKfa(String kfaCode) {
        if (kfaCode == null || kfaCode.isEmpty()) return "";
        PreparedStatement p = null;
        ResultSet r = null;
        try {
            p = koneksi.prepareStatement(
                "SELECT paket_obat_ucum_cs_code, active_ingredients_kekuatan " +
                "FROM satu_sehat_kfa_terbaru_detail WHERE kfa_code=?");
            p.setString(1, kfaCode);
            r = p.executeQuery();
            if (r.next()) {
                String ucum = r.getString(1);
                if (ucum != null && !ucum.trim().isEmpty()) return ucum.trim();
                String derived = extractUnit(r.getString(2));
                return "1".equals(derived) ? "" : derived;
            }
        } catch (Exception ex) {
            System.out.println("lookupNumeratorByKfa: " + ex);
        } finally {
            try { if (r != null) r.close(); } catch (Exception ignored) {}
            try { if (p != null) p.close(); } catch (Exception ignored) {}
        }
        return "";
    }

    private String extractUnit(String kekuatan) {
        if (kekuatan == null || kekuatan.isEmpty()) return "1";
        java.util.regex.Matcher m = java.util.regex.Pattern
            .compile("[\\d,\\.]+\\s*([a-zA-Z]+)").matcher(kekuatan);
        if (m.find()) {
            String unit = m.group(1).toLowerCase();
            // Normalisasi unit umum
            if (unit.equals("mcg") || unit.equals("μg") || unit.equals("ug")) return "mcg";
            if (unit.equals("mg"))  return "mg";
            if (unit.equals("ml") || unit.equals("ml")) return "mL";
            if (unit.equals("g"))   return "g";
            if (unit.equals("iu"))  return "IU";
            if (unit.equals("meq")) return "mEq";
            return m.group(1);
        }
        return "1";
    }

    /** Map kode BS (dosage form KFA) → kode HL7 v3 OrderableDrugForm */
    private static final java.util.Map<String,String> BS_TO_HL7;
    /** Map kode BS → {route_code, route_display} sesuai WHO ATC */
    private static final java.util.Map<String,String[]> BS_TO_ROUTE;
    static {
        BS_TO_HL7 = new HashMap<>();
        // Tablet & sejenisnya
        BS_TO_HL7.put("BS066","TAB");   // Tablet
        BS_TO_HL7.put("BS074","TAB");   // Tablet Cepat Larut
        BS_TO_HL7.put("BS072","TAB");   // Tablet Disintegrasi Oral
        BS_TO_HL7.put("BS073","TAB");   // Tablet Dispersibel
        BS_TO_HL7.put("BS067","TAB");   // Tablet Effervescent
        BS_TO_HL7.put("BS068","TAB");   // Tablet Hisap
        BS_TO_HL7.put("BS069","CHEWBAR");// Tablet Kunyah
        BS_TO_HL7.put("BS071","ENTAB"); // Tablet Pelepasan Lambat
        BS_TO_HL7.put("BS076","ENTAB"); // Tablet Salut Enterik
        BS_TO_HL7.put("BS075","TAB");   // Tablet Salut Gula
        BS_TO_HL7.put("BS077","TAB");   // Tablet Salut Selaput
        BS_TO_HL7.put("BS078","TAB");   // Tablet Sublingual
        BS_TO_HL7.put("BS080","TAB");   // Tablet Vaginal
        // Kaplet
        BS_TO_HL7.put("BS022","TAB");   // Kaplet
        BS_TO_HL7.put("BS028","CHEWBAR");// Kaplet Kunyah
        BS_TO_HL7.put("BS026","ENTAB"); // Kaplet Pelepasan Lambat
        BS_TO_HL7.put("BS025","TAB");   // Kaplet Salut Gula
        BS_TO_HL7.put("BS023","TAB");   // Kaplet Salut Selaput
        BS_TO_HL7.put("BS098","TAB");   // Orodispersible Film
        // Kapsul
        BS_TO_HL7.put("BS019","CAP");   // Kapsul
        BS_TO_HL7.put("BS020","CAP");   // Kapsul Lunak
        BS_TO_HL7.put("BS021","CAP");   // Kapsul Pelepasan Lambat
        // Sirup & cair oral
        BS_TO_HL7.put("BS055","SYRUP"); // Sirup
        BS_TO_HL7.put("BS056","SYRUP"); // Sirup Kering
        BS_TO_HL7.put("BS009","ELIXIR");// Eliksir
        BS_TO_HL7.put("BS010","SOLN");  // Emulsi
        BS_TO_HL7.put("BS032","SOLN");  // Larutan
        BS_TO_HL7.put("BS060","SUSP");  // Suspensi
        BS_TO_HL7.put("BS099","SOLN");  // Cairan Obat Dalam
        BS_TO_HL7.put("BS087","SOLN");  // Tetes Oral (Oral Drops)
        BS_TO_HL7.put("BS004","SOLN");  // Oral Spray
        BS_TO_HL7.put("BS036","SOLN");  // Obat Kumur
        BS_TO_HL7.put("BS083","CHEWBAR");// Chewing Gum
        // Serbuk/Granul oral
        BS_TO_HL7.put("BS047","POWD");  // Serbuk Oral
        BS_TO_HL7.put("BS016","GRAN");  // Granula
        BS_TO_HL7.put("BS054","POWD");  // Serbuk Effervescent
        // Injeksi/Infus (parenteral)
        BS_TO_HL7.put("BS035","IVSOLN");// Infus
        BS_TO_HL7.put("BS034","INJ");   // Larutan Injeksi
        BS_TO_HL7.put("BS061","SUSP");  // Suspensi Injeksi
        BS_TO_HL7.put("BS049","PWDP");  // Serbuk Injeksi
        BS_TO_HL7.put("BS050","PWDP");  // Serbuk Injeksi Liofilisasi
        BS_TO_HL7.put("BS051","PWDP");  // Serbuk Infus
        BS_TO_HL7.put("BS063","IVSOLN");// Cairan Steril
        BS_TO_HL7.put("BS018","INJ");   // Implant
        BS_TO_HL7.put("BS058","INJ");   // Subdermal Implants
        // Inhalasi
        BS_TO_HL7.put("BS002","INHL");  // Aerosol Metered Dose
        BS_TO_HL7.put("BS003","INHL");  // Aerosol Spray
        BS_TO_HL7.put("BS033","SOLN");  // Larutan Inhalasi
        BS_TO_HL7.put("BS097","SUSP");  // Suspensi Inhalasi
        BS_TO_HL7.put("BS048","INHL");  // Serbuk Inhaler
        BS_TO_HL7.put("BS012","INHL");  // Gas
        // Topikal
        BS_TO_HL7.put("BS042","OINT");  // Salep
        BS_TO_HL7.put("BS030","CREAM"); // Krim
        BS_TO_HL7.put("BS013","GEL");   // Gel
        BS_TO_HL7.put("BS038","PASTE"); // Pasta
        BS_TO_HL7.put("BS040","PATCH"); // Patch
        BS_TO_HL7.put("BS091","PATCH"); // Tulle/Plester Obat
        BS_TO_HL7.put("BS100","SOLN");  // Cairan Obat Luar
        BS_TO_HL7.put("BS062","SUSP");  // Suspensi/Cairan Obat Luar
        BS_TO_HL7.put("BS052","POWD");  // Serbuk Obat Luar
        BS_TO_HL7.put("BS044","SOLN");  // Sampo
        BS_TO_HL7.put("BS007","SOLN");  // Topical Spray
        // Mata & Telinga
        BS_TO_HL7.put("BS064","SOLN");  // Cairan Mata
        BS_TO_HL7.put("BS084","SOLN");  // Tetes Mata
        BS_TO_HL7.put("BS043","OINT");  // Salep Mata
        BS_TO_HL7.put("BS014","GEL");   // Gel Mata
        BS_TO_HL7.put("BS086","SOLN");  // Tetes Telinga
        BS_TO_HL7.put("BS088","SOLN");  // Tetes Mata Dan Telinga
        BS_TO_HL7.put("BS085","NASLSPRY");// Tetes Hidung
        BS_TO_HL7.put("BS045","NASLSPRY");// Semprot Hidung
        // Rektal/Vaginal
        BS_TO_HL7.put("BS059","SUPP"); // Supositoria
        BS_TO_HL7.put("BS011","ENEMA");// Enema
        BS_TO_HL7.put("BS037","SUPP"); // Ovula
        BS_TO_HL7.put("BS041","SUPP"); // Pessary
        BS_TO_HL7.put("BS080","TAB");  // Tablet Vaginal
        BS_TO_HL7.put("BS092","CREAM");// Vaginal Cream
        BS_TO_HL7.put("BS093","GEL");  // Vaginal Gel
        BS_TO_HL7.put("BS095","SUPP"); // Vaginal Ring

        BS_TO_ROUTE = new HashMap<>();
        // Oral
        for (String c : new String[]{"BS066","BS074","BS072","BS073","BS067","BS068","BS069",
            "BS071","BS076","BS075","BS077","BS078","BS022","BS028","BS026","BS025","BS023",
            "BS098","BS019","BS020","BS021","BS055","BS056","BS009","BS010","BS032","BS060",
            "BS099","BS087","BS004","BS036","BS083","BS047","BS016","BS054"})
            BS_TO_ROUTE.put(c, new String[]{"O","Oral"});
        // Sublingual
        BS_TO_ROUTE.put("BS078", new String[]{"SL","Sublingual"});
        // Parenteral
        for (String c : new String[]{"BS035","BS034","BS061","BS049","BS050","BS051","BS063","BS018","BS058"})
            BS_TO_ROUTE.put(c, new String[]{"P","Parenteral"});
        // Inhalasi
        for (String c : new String[]{"BS002","BS003","BS033","BS097","BS048","BS012"})
            BS_TO_ROUTE.put(c, new String[]{"Inhal","Inhalasi (dihirup)"});
        // Topikal/Dermal
        for (String c : new String[]{"BS042","BS030","BS013","BS038","BS040","BS091",
            "BS100","BS062","BS052","BS044","BS007"})
            BS_TO_ROUTE.put(c, new String[]{"D","Dermal"});
        // Mata & Telinga
        for (String c : new String[]{"BS064","BS084","BS043","BS014","BS086","BS088"})
            BS_TO_ROUTE.put(c, new String[]{"T","Topikal"});
        // Nasal
        BS_TO_ROUTE.put("BS085", new String[]{"N","Nasal"});
        BS_TO_ROUTE.put("BS045", new String[]{"N","Nasal"});
        // Rektal
        BS_TO_ROUTE.put("BS059", new String[]{"R","Rektal"});
        BS_TO_ROUTE.put("BS011", new String[]{"R","Rektal"});
        // Vaginal
        for (String c : new String[]{"BS037","BS041","BS080","BS092","BS093","BS095"})
            BS_TO_ROUTE.put(c, new String[]{"V","Vaginal"});
    }

    /** Ambil kode HL7 v3 OrderableDrugForm berdasarkan kode BS, fallback ke nama */
    private String deriveDenominator(String bsCode, String dosageForm) {
        if (bsCode != null && BS_TO_HL7.containsKey(bsCode)) return BS_TO_HL7.get(bsCode);
        // Fallback: derive dari nama
        if (dosageForm == null) return "1";
        String f = dosageForm.toLowerCase();
        if (f.contains("tablet") || f.contains("kaplet")) return "TAB";
        if (f.contains("kapsul")) return "CAP";
        if (f.contains("sirup"))  return "SYRUP";
        if (f.contains("suspensi")) return "SUSP";
        if (f.contains("infus"))  return "IVSOLN";
        if (f.contains("injeksi") || f.contains("larutan injeksi")) return "INJ";
        if (f.contains("serbuk injeksi")) return "PWDP";
        if (f.contains("larutan")) return "SOLN";
        if (f.contains("salep"))  return "OINT";
        if (f.contains("krim"))   return "CREAM";
        if (f.contains("gel"))    return "GEL";
        if (f.contains("patch") || f.contains("plester")) return "PATCH";
        if (f.contains("suppositoria") || f.contains("supositoria")) return "SUPP";
        if (f.contains("aerosol") || f.contains("inhaler") || f.contains("inhalasi")) return "INHL";
        if (f.contains("serbuk") || f.contains("granul")) return "POWD";
        return "1";
    }

    /** Ambil route code & display (WHO ATC) berdasarkan kode BS */
    private String[] deriveRoute(String bsCode, String dosageForm) {
        if (bsCode != null && BS_TO_ROUTE.containsKey(bsCode)) return BS_TO_ROUTE.get(bsCode);
        // Fallback: derive dari nama
        if (dosageForm == null) return new String[]{"O","Oral"};
        String f = dosageForm.toLowerCase();
        if (f.contains("infus") || f.contains("injeksi") || f.contains("intravena")) return new String[]{"P","Parenteral"};
        if (f.contains("inhalasi") || f.contains("aerosol") || f.contains("inhaler"))  return new String[]{"Inhal","Inhalasi (dihirup)"};
        if (f.contains("suppositoria") || f.contains("supositoria") || f.contains("enema")) return new String[]{"R","Rektal"};
        if (f.contains("vaginal")) return new String[]{"V","Vaginal"};
        if (f.contains("hidung")) return new String[]{"N","Nasal"};
        if (f.contains("mata") || f.contains("telinga")) return new String[]{"T","Topikal"};
        if (f.contains("salep") || f.contains("krim") || f.contains("gel") || f.contains("patch")) return new String[]{"D","Dermal"};
        return new String[]{"O","Oral"};
    }

    private int[] autoMapping() {
        int berhasil = 0, skip = 0, tidakDitemukan = 0;
        String obatSystem       = "http://sys-ids.kemkes.go.id/kfa";
        String formSystem       = "http://terminology.kemkes.go.id/CodeSystem/medication-form";
        String numeratorSystem  = "http://unitsofmeasure.org";
        String denominatorSystem= "http://terminology.hl7.org/CodeSystem/v3-orderableDrugForm";
        String routeSystem      = "http://www.whocc.no/atc";

        try {
            PreparedStatement psBarang = koneksi.prepareStatement(
                "SELECT d.kode_brng, d.nama_brng, COALESCE(i.nama_industri,'') as manufaktur " +
                "FROM databarang d " +
                "LEFT JOIN industrifarmasi i ON d.kode_industri = i.kode_industri " +
                "WHERE d.status='1'");
            ResultSet rsBarang = psBarang.executeQuery();

            PreparedStatement psCariKfa = koneksi.prepareStatement(
                "SELECT k.kfa_code, k.name, k.manufacturer, k.registrar, " +
                "COALESCE(d.dosage_form_code,'') as form_code, " +
                "COALESCE(d.dosage_form_name,'') as form_display, " +
                "COALESCE(d.active_ingredients_kekuatan,'') as kekuatan " +
                "FROM satu_sehat_kfa_terbaru k " +
                "LEFT JOIN satu_sehat_kfa_terbaru_detail d ON k.kfa_code = d.kfa_code " +
                "WHERE k.name LIKE ? " +
                "ORDER BY CASE WHEN (k.manufacturer LIKE ? OR k.registrar LIKE ?) THEN 0 ELSE 1 END, k.kfa_code " +
                "LIMIT 1");

            PreparedStatement psInsert = koneksi.prepareStatement(
                "INSERT INTO satu_sehat_mapping_obat " +
                "(kode_brng,obat_code,obat_system,obat_display,form_code,form_system,form_display," +
                "numerator_code,numerator_system,denominator_code,denominator_system," +
                "route_code,route_system,route_display) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
                "ON DUPLICATE KEY UPDATE " +
                "obat_code=VALUES(obat_code),obat_system=VALUES(obat_system),obat_display=VALUES(obat_display)," +
                "form_code=VALUES(form_code),form_system=VALUES(form_system),form_display=VALUES(form_display)," +
                "numerator_code=VALUES(numerator_code),numerator_system=VALUES(numerator_system)," +
                "denominator_code=VALUES(denominator_code),denominator_system=VALUES(denominator_system)," +
                "route_code=VALUES(route_code),route_system=VALUES(route_system),route_display=VALUES(route_display)");

            while (rsBarang.next()) {
                String kodeBarang = rsBarang.getString("kode_brng");
                String namaBarang = rsBarang.getString("nama_brng");
                String manufaktur = rsBarang.getString("manufaktur");

                // Ambil kata-kata alfabetik saja (skip token yang ada angka seperti "500MG", "10mg/5ml")
                // agar cocok dengan KFA yang tulis "500 mg" terpisah
                String[] tokens = namaBarang.trim().split("\\s+");
                StringBuilder keyword = new StringBuilder();
                int count = 0;
                for (String t : tokens) {
                    if (t.matches(".*\\d.*")) continue; // skip token berisi angka
                    String clean = t.replaceAll("[^a-zA-Z]", "");
                    if (clean.length() > 2 && count < 2) { keyword.append(clean).append("%"); count++; }
                }
                // fallback: pakai kata pertama apapun jika semua token ada angka
                if (keyword.length() == 0) {
                    String firstClean = tokens[0].replaceAll("[^a-zA-Z]", "");
                    if (firstClean.length() > 2) keyword.append(firstClean).append("%");
                }
                if (keyword.length() == 0) { tidakDitemukan++; continue; }

                String likeNama = "%" + keyword.toString();
                String firstWordManu = manufaktur.length() > 3 ? manufaktur.split("\\s+")[0] : manufaktur;
                String likeManu = "%" + firstWordManu + "%";

                psCariKfa.setString(1, likeNama);
                psCariKfa.setString(2, likeManu);
                psCariKfa.setString(3, likeManu);
                ResultSet rsKfa = psCariKfa.executeQuery();

                if (rsKfa.next()) {
                    String kfaCode     = rsKfa.getString("kfa_code");
                    String kfaDisplay  = rsKfa.getString("name");
                    String formCode    = rsKfa.getString("form_code");
                    String formDisplay = rsKfa.getString("form_display");
                    String kekuatan    = rsKfa.getString("kekuatan");

                    // Numerator: unit dari kekuatan zat aktif (mg, mL, mcg, g, IU, dll)
                    String numCode = extractUnit(kekuatan);

                    // Denominator: kode satuan kemasan dari bentuk sediaan
                    String denCode = deriveDenominator(formCode, formDisplay);

                    // Route: dari bentuk sediaan
                    String[] route = deriveRoute(formCode, formDisplay);
                    String routeCode    = route[0];
                    String routeDisplay = route[1];

                    if (formCode.isEmpty())    formCode    = "-";
                    if (formDisplay.isEmpty()) formDisplay = "-";

                    psInsert.setString(1,  kodeBarang);
                    psInsert.setString(2,  kfaCode);
                    psInsert.setString(3,  obatSystem);
                    psInsert.setString(4,  kfaDisplay);
                    psInsert.setString(5,  formCode);
                    psInsert.setString(6,  formSystem);
                    psInsert.setString(7,  formDisplay);
                    psInsert.setString(8,  numCode);
                    psInsert.setString(9,  numeratorSystem);
                    psInsert.setString(10, denCode);
                    psInsert.setString(11, denominatorSystem);
                    psInsert.setString(12, routeCode);
                    psInsert.setString(13, routeSystem);
                    psInsert.setString(14, routeDisplay);
                    int rows = psInsert.executeUpdate();
                    if (rows > 0) berhasil++; else skip++;
                } else {
                    tidakDitemukan++;
                }
                rsKfa.close();
            }

            rsBarang.close();
            psBarang.close();
            psCariKfa.close();
            psInsert.close();

        } catch (Exception e) {
            System.out.println("autoMapping error: " + e);
        }
        return new int[]{berhasil, skip, tidakDitemukan};
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try{
           ps=koneksi.prepareStatement(
                   "select satu_sehat_mapping_obat.kode_brng,databarang.nama_brng,satu_sehat_mapping_obat.obat_code,satu_sehat_mapping_obat.obat_system,"+
                   "satu_sehat_mapping_obat.obat_display,satu_sehat_mapping_obat.form_code,satu_sehat_mapping_obat.form_system,"+
                   "satu_sehat_mapping_obat.form_display,satu_sehat_mapping_obat.numerator_code,satu_sehat_mapping_obat.numerator_system,"+
                   "satu_sehat_mapping_obat.denominator_code,satu_sehat_mapping_obat.denominator_system,satu_sehat_mapping_obat.route_code,"+
                   "satu_sehat_mapping_obat.route_system,satu_sehat_mapping_obat.route_display from satu_sehat_mapping_obat inner join databarang "+
                   "on satu_sehat_mapping_obat.kode_brng=databarang.kode_brng "+
                   (TCari.getText().equals("")?"":"where satu_sehat_mapping_obat.kode_brng like ? or databarang.nama_brng like ? or "+
                   "satu_sehat_mapping_obat.obat_code like ? or satu_sehat_mapping_obat.obat_display like ? or satu_sehat_mapping_obat.form_display like ? ")+
                   " order by satu_sehat_mapping_obat.obat_code");
            try {
                if(!TCari.getText().equals("")){
                    ps.setString(1,"%"+TCari.getText()+"%");
                    ps.setString(2,"%"+TCari.getText()+"%");
                    ps.setString(3,"%"+TCari.getText()+"%");
                    ps.setString(4,"%"+TCari.getText()+"%");
                    ps.setString(5,"%"+TCari.getText()+"%");
                }
                rs=ps.executeQuery();
                while(rs.next()){
                    tabMode.addRow(new Object[]{
                        rs.getString("obat_code"),rs.getString("obat_system"),rs.getString("kode_brng"),rs.getString("nama_brng"),rs.getString("obat_display"),
                        rs.getString("form_code"),rs.getString("form_system"),rs.getString("form_display"),rs.getString("numerator_code"),rs.getString("numerator_system"),
                        rs.getString("denominator_code"),rs.getString("denominator_system"),rs.getString("route_code"),rs.getString("route_system"),rs.getString("route_display")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif Ketersediaan : "+e);
            } finally{
                if(rs!=null){
                    rs.close();
                }
                if(ps!=null){
                    ps.close();
                }
            }
        }catch(Exception e){
            System.out.println("Notifikasi : "+e);
        }
        LCount.setText(""+tabMode.getRowCount());
    }

    public void emptTeks() {
        KFACode.setText("");
      //  KFASystem.setText("");
        KodeBarang.setText("");
        NamaBarang.setText("");
        KFADisplay.setText("");
        FormCode.setText("");
       // FormSystem.setText("");
        FormDisplay.setText("");
        NumoratorCode.setText("");
       // NemeratorSystem.setText("");
        DenominatorCode.setText("");
      //  DenominatorSystem.setText("");
        RouteCode.setText("");
      //  RouteSystem.setText("");
        RouteDisplay.setText("");
        ChkInput.setSelected(true);
        isForm();
        KFACode.requestFocus();
    }

    private void getData() {
       if(tbJnsPerawatan.getSelectedRow()!= -1){
           KFACode.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),0).toString());
           KFASystem.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),1).toString());
           KodeBarang.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),2).toString());
           NamaBarang.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),3).toString());
           KFADisplay.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),4).toString());
           FormCode.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),5).toString());
           FormSystem.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),6).toString());
           FormDisplay.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),7).toString());
           NumoratorCode.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),8).toString());
           NemeratorSystem.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),9).toString());
           DenominatorCode.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),10).toString());
           DenominatorSystem.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),11).toString());
           RouteCode.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),12).toString());
           RouteSystem.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),13).toString());
           RouteDisplay.setText(tbJnsPerawatan.getValueAt(tbJnsPerawatan.getSelectedRow(),14).toString());
        }
    }
    
    public void isCek(){
        BtnSimpan.setEnabled(akses.getsatu_sehat_mapping_obat());
        BtnHapus.setEnabled(akses.getsatu_sehat_mapping_obat());
        BtnEdit.setEnabled(akses.getsatu_sehat_mapping_obat());
        BtnPrint.setEnabled(akses.getsatu_sehat_mapping_obat());
    }
    
    public JTable getTable(){
        return tbJnsPerawatan;
    }  
    
    private void isForm() {
        if (ChkInput.isSelected() == true) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 245));
            FormInput.setVisible(true);
            ChkInput.setVisible(true);
        } else if (ChkInput.isSelected() == false) {
            ChkInput.setVisible(false);
            PanelInput.setPreferredSize(new Dimension(WIDTH, 20));
            FormInput.setVisible(false);
            ChkInput.setVisible(true);
        }
    }

    private void runBackground(Runnable task) {
        if (ceksukses) return;
        if (executor.isShutdown() || executor.isTerminated()) return;
        if (!isDisplayable()) return;

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
    
    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }
}
