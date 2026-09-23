package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.batasInput;
import fungsi.koneksiDB;
import java.awt.Dimension;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import fungsi.sekuel;
import fungsi.validasi;
import fungsi.akses;
import java.awt.Cursor;
import java.awt.Desktop;
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
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

/**
 * SatuSehatKirimNutritionOrder
 *
 * Bridging dialog for SatuSehat FHIR R4 NutritionOrder resource.
 * Synchronizes clinical dietary orders (catatan_adime_gizi + detail_beri_diet)
 * with Kemenkes SATUSEHAT platform.
 *
 * @author malifnasrulloh
 */
public final class SatuSehatKirimNutritionOrder extends javax.swing.JDialog {
    private final DefaultTableModel tabMode;
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private int i = 0;
    private String link = "", json = "", idpasien = "", idpraktisi = "";
    private ApiSatuSehat api = new ApiSatuSehat();
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode response;
    private SatuSehatCekNIK cekViaSatuSehat = new SatuSehatCekNIK();
    private StringBuilder htmlContent;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;

    /** Creates new form SatuSehatKirimNutritionOrder
     * @param parent
     * @param modal */
    public SatuSehatKirimNutritionOrder(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        this.setLocation(10, 2);
        setSize(628, 674);

        tabMode = new DefaultTableModel(null, new String[]{
            "P", "Tanggal Registrasi", "No.Rawat", "No.RM", "Nama Pasien", "No.KTP Pasien", "ID Encounter",
            "Nama Diet", "Intervensi Gizi", "Instruksi", "Petugas/Praktisi", "No.KTP Praktisi",
            "Tanggal ADIME", "ID Nutrition Order", "Status", "KTP Dokter DPJP", "Diagnosis Gizi"
        }) {
            @Override
            public boolean isCellEditable(int rowIndex, int colIndex) {
                return colIndex == 0;
            }

            Class[] types = new Class[]{
                java.lang.Boolean.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
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

        for (i = 0; i < 17; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(20);
            } else if (i == 1) {
                column.setPreferredWidth(150);
            } else if (i == 2) {
                column.setPreferredWidth(105);
            } else if (i == 3) {
                column.setPreferredWidth(70);
            } else if (i == 4) {
                column.setPreferredWidth(150);
            } else if (i == 5) {
                column.setPreferredWidth(110);
            } else if (i == 6) {
                column.setPreferredWidth(210);
            } else if (i == 7) {
                column.setPreferredWidth(120);
            } else if (i == 8) {
                column.setPreferredWidth(200);
            } else if (i == 9) {
                column.setPreferredWidth(150);
            } else if (i == 10) {
                column.setPreferredWidth(150);
            } else if (i == 11) {
                column.setPreferredWidth(110);
            } else if (i == 12) {
                column.setPreferredWidth(130);
            } else if (i == 13) {
                column.setPreferredWidth(210);
            } else if (i == 14) {
                column.setPreferredWidth(80);
            } else if (i == 15) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            } else if (i == 16) {
                column.setMinWidth(0);
                column.setMaxWidth(0);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        TCari.setDocument(new batasInput((byte) 100).getKata(TCari));

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    if (TCari.getText().length() > 2) {
                        runBackground(() -> tampil());
                    }
                }
            });
        }

        try {
            link = koneksiDB.URLFHIRSATUSEHAT();
        } catch (Exception e) {
            System.out.println("Notif : " + e);
        }

        HTMLEditorKit kit = new HTMLEditorKit();
        LoadHTML.setEditable(true);
        LoadHTML.setEditorKit(kit);
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule(".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;font-weight:normal;line-height:11px;}");
        styleSheet.addRule(".isi2 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#323232;font-weight:normal;line-height:11px;}");
        styleSheet.addRule(".isi3 td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;font-weight:normal;line-height:11px;}");
        styleSheet.addRule(".isi4 td{font: 11px tahoma;height:12px;border-top: 1px solid #e2e7dd;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;font-weight:normal;line-height:11px;}");
        styleSheet.addRule(".isi10 td{font: 9.5px tahoma;height:12px;border-top: 1px solid #e2e7dd;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;font-weight:normal;line-height:11px;}");
        styleSheet.addRule(".head td{border-right: 1px solid #777777;font: 9.5px tahoma;height:10px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;font-weight:normal;line-height:10px;}");
        styleSheet.addRule(".head2 td{font: 10px tahoma;height:10px;background: #ffffff;color:#323232;font-weight:normal;line-height:10px;}");
        styleSheet.addRule(".isi2 td{border:none;font: 9.5px tahoma;height:12px;background: #ffffff;color:#323232;font-weight:normal;line-height:11px;}");
        styleSheet.addRule(".isi5 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#AA0000;font-weight:normal;line-height:11px;}");
        styleSheet.addRule(".isi6 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#FF0000;font-weight:normal;line-height:11px;}");
        styleSheet.addRule(".isi7 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#C80000;font-weight:normal;line-height:11px;}");
        styleSheet.addRule(".isi8 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#005b00;font-weight:normal;line-height:11px;}");
        styleSheet.addRule(".isi9 td{font: 8.5px tahoma;border:none;height:12px;background: #ffffff;color:#960000;font-weight:normal;line-height:11px;}");
        Document doc = kit.createDefaultDocument();
        LoadHTML.setDocument(doc);
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
                } catch (Throwable t) {
                    t.printStackTrace();
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

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        ppPilihSemua = new javax.swing.JMenuItem();
        ppBersihkan = new javax.swing.JMenuItem();
        ppPilihBelumTerkirim = new javax.swing.JMenuItem();
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

        jPopupMenu1.setName("jPopupMenu1"); // NOI18N

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

        ppPilihBelumTerkirim.setBackground(new java.awt.Color(255, 255, 254));
        ppPilihBelumTerkirim.setFont(new java.awt.Font("Tahoma", 0, 11)); // NOI18N
        ppPilihBelumTerkirim.setForeground(new java.awt.Color(50, 50, 50));
        ppPilihBelumTerkirim.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/category.png"))); // NOI18N
        ppPilihBelumTerkirim.setText("Pilih Belum Terkirim");
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

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Pengiriman Data Nutrition Order Satu Sehat ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
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

        jPanel3.add(panelGlass9, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel3, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

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

    private void BtnPrintActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnPrintActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (tabMode.getRowCount() == 0) {
            javax.swing.JOptionPane.showMessageDialog(null, "Maaf, data sudah habis. Tidak ada data yang bisa anda print...!!!!");
            TCari.requestFocus();
        } else if (tabMode.getRowCount() != 0) {
            try {
                htmlContent = new StringBuilder();
                htmlContent.append(
                    "<tr class='head'>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='25px'><b>P</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='100px'><b>Tanggal Registrasi</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='100px'><b>No.Rawat</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='60px'><b>No.RM</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='120px'><b>Nama Pasien</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='100px'><b>No.KTP Pasien</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'><b>ID Encounter</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='100px'><b>Nama Diet</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'><b>Intervensi Gizi</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='120px'><b>Instruksi</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='120px'><b>Petugas/Praktisi</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='100px'><b>No.KTP Praktisi</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='110px'><b>Tanggal ADIME</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='150px'><b>ID Nutrition Order</b></td>" +
                    "<td valign='middle' bgcolor='#FFFAFA' align='center' width='60px'><b>Status</b></td>" +
                    "</tr>"
                );
                for (i = 0; i < tabMode.getRowCount(); i++) {
                    htmlContent.append(
                        "<tr class='isi'>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 0) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 1) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 2) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 3) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 4) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 5) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 6) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 7) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 8) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 9) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 10) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 11) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 12) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 13) + "</td>" +
                        "<td valign='top'>" + tbObat.getValueAt(i, 14) + "</td>" +
                        "</tr>"
                    );
                }
                LoadHTML.setText(
                    "<html><body>" +
                    "<table width='100%' border='0' align='center' cellpadding='1px' cellspacing='0' class='tbl_form'>" +
                    htmlContent.toString() +
                    "</table>" +
                    "</body></html>"
                );
                File g = new File("file2.css");
                BufferedWriter bg = new BufferedWriter(new FileWriter(g));
                bg.write(
                    ".isi td{border-right: 1px solid #e2e7dd;font: 8.5px tahoma;height:12px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}" +
                    ".head td{border-right: 1px solid #777777;font: 9.5px tahoma;height:10px;border-bottom: 1px solid #e2e7dd;background: #ffffff;color:#323232;}"
                );
                bg.close();
                File f = new File("DataSatuSehatNutritionOrder.html");
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                bw.write(LoadHTML.getText().replaceAll("<head>", "<head>" +
                    "<link href=\"file2.css\" rel=\"stylesheet\" type=\"text/css\" />" +
                    "<table width='1700px' border='0' align='center' cellpadding='3px' cellspacing='0' class='tbl_form'>" +
                        "<tr class='isi2'>" +
                            "<td valign='top' align='center'>" +
                                "<font size='4' face='Tahoma'>" + akses.getnamars() + "</font><br>" +
                                akses.getalamatrs() + ", " + akses.getkabupatenrs() + ", " + akses.getpropinsirs() + "<br>" +
                                akses.getkontakrs() + ", E-mail : " + akses.getemailrs() + "<br><br>" +
                                "<font size='2' face='Tahoma'>DATA PENGIRIMAN SATU SEHAT NUTRITION ORDER<br><br></font>" +
                            "</td>" +
                        "</tr>" +
                    "</table>")
                );
                bw.close();
                Desktop.getDesktop().browse(f.toURI());
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            }
        }
        this.setCursor(Cursor.getDefaultCursor());
    }//GEN-LAST:event_BtnPrintActionPerformed

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            BtnCariActionPerformed(null);
        } else if (evt.getKeyCode() == KeyEvent.VK_PAGE_DOWN) {
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

    private void BtnKirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKirimActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        runBackground(() -> {
            for (int r = 0; r < tbObat.getRowCount(); r++) {
                if (tbObat.getValueAt(r, 0).toString().equals("true") &&
                    !tbObat.getValueAt(r, 5).toString().equals("") &&
                    !tbObat.getValueAt(r, 6).toString().equals("") &&
                    tbObat.getValueAt(r, 13).toString().equals("")) {

                    final int row = r;
                    try {
                        String noktpPasien = tbObat.getValueAt(row, 5).toString();
                        String idEncounter = tbObat.getValueAt(row, 6).toString();
                        String noRawat = tbObat.getValueAt(row, 2).toString();
                        String nmPasien = tbObat.getValueAt(row, 4).toString();
                        String namaDiet = tbObat.getValueAt(row, 7).toString();
                        String intervensi = tbObat.getValueAt(row, 8).toString();
                        String instruksi = tbObat.getValueAt(row, 9).toString();
                        String namaPetugas = tbObat.getValueAt(row, 10).toString();
                        String ktpPraktisi = tbObat.getValueAt(row, 11).toString();
                        String tanggalAdime = tbObat.getValueAt(row, 12).toString();
                        String ktpDpjp = tbObat.getValueAt(row, 15).toString();
                        String diagnosis = tbObat.getValueAt(row, 16).toString();

                        idpasien = cekViaSatuSehat.tampilIDPasien(noktpPasien);
                        if (idpasien.equals("")) {
                            System.out.println("SatuSehat NutritionOrder: Missing IHS Patient for " + noRawat);
                            continue;
                        }

                        // Dietitian -> DPJP doctor fallback
                        idpraktisi = "";
                        if (!ktpPraktisi.equals("")) {
                            idpraktisi = cekViaSatuSehat.tampilIDParktisi(ktpPraktisi);
                        }
                        if (idpraktisi.equals("") && !ktpDpjp.equals("")) {
                            idpraktisi = cekViaSatuSehat.tampilIDParktisi(ktpDpjp);
                        }
                        if (idpraktisi.equals("")) {
                            System.out.println("SatuSehat NutritionOrder: Missing IHS Practitioner for " + noRawat);
                            continue;
                        }

                        // Pre-flight Duplicate Prevention check
                        String dupId = resolveDuplicateNutritionOrder(idpasien, idEncounter);
                        if (dupId != null && !dupId.isEmpty()) {
                            if (Sequel.menyimpantf2("satu_sehat_nutritionorder", "?,?,?,?", "NutritionOrder", 4, new String[]{
                                noRawat, tanggalAdime, dupId, "active"
                            })) {
                                SwingUtilities.invokeLater(() -> {
                                    tbObat.setValueAt(dupId, row, 13);
                                    tbObat.setValueAt("active", row, 14);
                                    tbObat.setValueAt(false, row, 0);
                                });
                                System.out.println("SatuSehat NutritionOrder: Recovered duplicate " + dupId);
                            }
                            continue;
                        }

                        try {
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());

                            json = SatuSehatNutritionOrderBuilder.buildPayload(
                                koneksiDB.IDSATUSEHAT(), noRawat, tanggalAdime, idpasien, nmPasien,
                                idEncounter, idpraktisi, namaPetugas, namaDiet, intervensi, instruksi, diagnosis, ""
                            );

                            System.out.println("URL : " + link + "/NutritionOrder");
                            System.out.println("Request JSON : " + json);
                            requestEntity = new HttpEntity(json, headers);
                            json = api.getRest().exchange(link + "/NutritionOrder", HttpMethod.POST, requestEntity, String.class).getBody();
                            System.out.println("Result JSON : " + json);

                            root = mapper.readTree(json);
                            response = root.path("id");
                            if (!response.asText().equals("")) {
                                String responseId = response.asText();
                                if (Sequel.menyimpantf2("satu_sehat_nutritionorder", "?,?,?,?", "NutritionOrder", 4, new String[]{
                                    noRawat, tanggalAdime, responseId, "active"
                                })) {
                                    SwingUtilities.invokeLater(() -> {
                                        tbObat.setValueAt(responseId, row, 13);
                                        tbObat.setValueAt("active", row, 14);
                                        tbObat.setValueAt(false, row, 0);
                                    });
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Notifikasi Bridging : " + e);
                            // Conflict / Duplicate auto-recovery
                            String recId = resolveDuplicateNutritionOrder(idpasien, idEncounter);
                            if (recId != null && !recId.isEmpty()) {
                                if (Sequel.menyimpantf2("satu_sehat_nutritionorder", "?,?,?,?", "NutritionOrder", 4, new String[]{
                                    noRawat, tanggalAdime, recId, "active"
                                })) {
                                    SwingUtilities.invokeLater(() -> {
                                        tbObat.setValueAt(recId, row, 13);
                                        tbObat.setValueAt("active", row, 14);
                                        tbObat.setValueAt(false, row, 0);
                                    });
                                }
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    }
                }
            }
        });
    }//GEN-LAST:event_BtnKirimActionPerformed

    private void ppPilihSemuaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihSemuaActionPerformed
        for (i = 0; i < tbObat.getRowCount(); i++) {
            tbObat.setValueAt(true, i, 0);
        }
    }//GEN-LAST:event_ppPilihSemuaActionPerformed

    private void ppBersihkanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppBersihkanActionPerformed
        for (i = 0; i < tbObat.getRowCount(); i++) {
            tbObat.setValueAt(false, i, 0);
        }
    }//GEN-LAST:event_ppBersihkanActionPerformed

    private void ppPilihBelumTerkirimActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ppPilihBelumTerkirimActionPerformed
        for (i = 0; i < tbObat.getRowCount(); i++) {
            Object idVal = tbObat.getValueAt(i, 13);
            tbObat.setValueAt(
                idVal == null || idVal.toString().trim().equals("") || idVal.toString().equals("-"),
                i, 0
            );
        }
    }//GEN-LAST:event_ppPilihBelumTerkirimActionPerformed

    private void BtnUpdateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateActionPerformed
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        runBackground(() -> {
            for (int r = 0; r < tbObat.getRowCount(); r++) {
                if (tbObat.getValueAt(r, 0).toString().equals("true") &&
                    !tbObat.getValueAt(r, 5).toString().equals("") &&
                    !tbObat.getValueAt(r, 6).toString().equals("") &&
                    !tbObat.getValueAt(r, 13).toString().equals("")) {

                    final int row = r;
                    try {
                        String noktpPasien = tbObat.getValueAt(row, 5).toString();
                        String idEncounter = tbObat.getValueAt(row, 6).toString();
                        String noRawat = tbObat.getValueAt(row, 2).toString();
                        String nmPasien = tbObat.getValueAt(row, 4).toString();
                        String namaDiet = tbObat.getValueAt(row, 7).toString();
                        String intervensi = tbObat.getValueAt(row, 8).toString();
                        String instruksi = tbObat.getValueAt(row, 9).toString();
                        String namaPetugas = tbObat.getValueAt(row, 10).toString();
                        String ktpPraktisi = tbObat.getValueAt(row, 11).toString();
                        String tanggalAdime = tbObat.getValueAt(row, 12).toString();
                        String idNutritionOrder = tbObat.getValueAt(row, 13).toString();
                        String ktpDpjp = tbObat.getValueAt(row, 15).toString();
                        String diagnosis = tbObat.getValueAt(row, 16).toString();

                        idpasien = cekViaSatuSehat.tampilIDPasien(noktpPasien);
                        if (idpasien.equals("")) {
                            continue;
                        }

                        idpraktisi = "";
                        if (!ktpPraktisi.equals("")) {
                            idpraktisi = cekViaSatuSehat.tampilIDParktisi(ktpPraktisi);
                        }
                        if (idpraktisi.equals("") && !ktpDpjp.equals("")) {
                            idpraktisi = cekViaSatuSehat.tampilIDParktisi(ktpDpjp);
                        }
                        if (idpraktisi.equals("")) {
                            continue;
                        }

                        try {
                            headers = new HttpHeaders();
                            headers.setContentType(MediaType.APPLICATION_JSON);
                            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());

                            json = SatuSehatNutritionOrderBuilder.buildPayload(
                                koneksiDB.IDSATUSEHAT(), noRawat, tanggalAdime, idpasien, nmPasien,
                                idEncounter, idpraktisi, namaPetugas, namaDiet, intervensi, instruksi, diagnosis, idNutritionOrder
                            );

                            System.out.println("URL : " + link + "/NutritionOrder/" + idNutritionOrder);
                            System.out.println("Request JSON : " + json);
                            requestEntity = new HttpEntity(json, headers);
                            json = api.getRest().exchange(link + "/NutritionOrder/" + idNutritionOrder, HttpMethod.PUT, requestEntity, String.class).getBody();
                            System.out.println("Result JSON : " + json);

                            Sequel.mengedit("satu_sehat_nutritionorder", "no_rawat='" + noRawat + "' and tanggal='" + tanggalAdime + "'", "status='updated'");
                            SwingUtilities.invokeLater(() -> {
                                tbObat.setValueAt("updated", row, 14);
                                tbObat.setValueAt(false, row, 0);
                            });
                        } catch (Exception e) {
                            System.out.println("Notifikasi Bridging : " + e);
                        }
                    } catch (Exception e) {
                        System.out.println("Notifikasi : " + e);
                    }
                }
            }
        });
    }//GEN-LAST:event_BtnUpdateActionPerformed

    private void BtnAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnAllActionPerformed
        TCari.setText("");
        runBackground(() -> tampil());
    }//GEN-LAST:event_BtnAllActionPerformed

    private void BtnAllKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnAllKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_SPACE) {
            BtnAllActionPerformed(null);
        } else {
            Valid.pindah(evt, TCari, BtnPrint);
        }
    }//GEN-LAST:event_BtnAllKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        runBackground(() -> tampil());
    }//GEN-LAST:event_formWindowOpened

    private String resolveDuplicateNutritionOrder(String idPasien, String idEncounter) {
        try {
            HttpHeaders h = new HttpHeaders();
            h.setContentType(MediaType.APPLICATION_JSON);
            h.add("Authorization", "Bearer " + api.TokenSatuSehat());
            HttpEntity req = new HttpEntity(h);
            String url = link + "/NutritionOrder?patient=" + idPasien + "&encounter=" + idEncounter;
            String res = api.getRest().exchange(url, HttpMethod.GET, req, String.class).getBody();
            JsonNode rootNode = mapper.readTree(res);
            JsonNode entryNode = rootNode.path("entry");
            if (entryNode.isArray() && entryNode.size() > 0) {
                String existingId = entryNode.get(0).path("resource").path("id").asText();
                if (!existingId.isEmpty()) {
                    return existingId;
                }
            }
        } catch (Exception e) {
            System.out.println("resolveDuplicateNutritionOrder error: " + e.getMessage());
        }
        return null;
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            String sql =
                "SELECT rp.tgl_registrasi, rp.jam_reg, rp.no_rawat, rp.no_rkm_medis, " +
                "pasien.nm_pasien, pasien.no_ktp AS noktp_pasien, sse.id_encounter, " +
                "ca.tanggal AS tanggal_adime, ca.intervensi, ca.instruksi, ca.diagnosis, " +
                "COALESCE(pg_gizi.nama, pg_dok.nama, '') AS nama_petugas, " +
                "COALESCE(pg_gizi.no_ktp, '') AS ktppraktisi, " +
                "COALESCE(pg_dok.no_ktp, '') AS ktpdokter_dpjp, " +
                "COALESCE(ssn.id_nutritionorder, '') AS satu_sehat_nutritionorder, " +
                "COALESCE(ssn.status, '') AS status_nutritionorder, " +
                "COALESCE(d.nama_diet, '') AS nama_diet, COALESCE(d.kd_diet, '') AS kd_diet " +
                "FROM catatan_adime_gizi ca " +
                "INNER JOIN reg_periksa rp ON ca.no_rawat = rp.no_rawat " +
                "INNER JOIN pasien ON rp.no_rkm_medis = pasien.no_rkm_medis " +
                "INNER JOIN satu_sehat_encounter sse ON sse.no_rawat = rp.no_rawat " +
                "LEFT JOIN pegawai pg_gizi ON ca.nip = pg_gizi.nik " +
                "LEFT JOIN pegawai pg_dok ON rp.kd_dokter = pg_dok.nik " +
                "LEFT JOIN satu_sehat_nutritionorder ssn ON ssn.no_rawat = ca.no_rawat AND ssn.tanggal = ca.tanggal " +
                "LEFT JOIN detail_beri_diet dbd ON dbd.no_rawat = ca.no_rawat AND dbd.tanggal = DATE(ca.tanggal) " +
                "LEFT JOIN diet d ON dbd.kd_diet = d.kd_diet " +
                "WHERE rp.tgl_registrasi BETWEEN ? AND ? " +
                (TCari.getText().trim().isEmpty() ? "" :
                    "AND (rp.no_rawat LIKE ? OR rp.no_rkm_medis LIKE ? OR pasien.nm_pasien LIKE ? OR " +
                    "pasien.no_ktp LIKE ? OR pg_gizi.nama LIKE ? OR d.nama_diet LIKE ?) ") +
                "GROUP BY ca.no_rawat, ca.tanggal " +
                "ORDER BY rp.tgl_registrasi ASC, rp.no_rawat ASC";

            ps = koneksi.prepareStatement(sql);
            try {
                ps.setString(1, Valid.SetTgl(DTPCari1.getSelectedItem() + ""));
                ps.setString(2, Valid.SetTgl(DTPCari2.getSelectedItem() + ""));
                if (!TCari.getText().trim().isEmpty()) {
                    String param = "%" + TCari.getText().trim() + "%";
                    ps.setString(3, param);
                    ps.setString(4, param);
                    ps.setString(5, param);
                    ps.setString(6, param);
                    ps.setString(7, param);
                    ps.setString(8, param);
                }
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        false,
                        rs.getString("tgl_registrasi") + " " + rs.getString("jam_reg"),
                        rs.getString("no_rawat"),
                        rs.getString("no_rkm_medis"),
                        rs.getString("nm_pasien"),
                        rs.getString("noktp_pasien"),
                        rs.getString("id_encounter"),
                        rs.getString("nama_diet"),
                        rs.getString("intervensi"),
                        rs.getString("instruksi"),
                        rs.getString("nama_petugas"),
                        rs.getString("ktppraktisi"),
                        rs.getString("tanggal_adime"),
                        rs.getString("satu_sehat_nutritionorder"),
                        rs.getString("status_nutritionorder"),
                        rs.getString("ktpdokter_dpjp"),
                        rs.getString("diagnosis")
                    });
                }
            } catch (Exception e) {
                System.out.println("Notif : " + e);
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    public void isCek() {
        BtnKirim.setEnabled(akses.getsatu_sehat_kirim_nutritionorder());
        BtnUpdate.setEnabled(akses.getsatu_sehat_kirim_nutritionorder());
        BtnPrint.setEnabled(akses.getsatu_sehat_kirim_nutritionorder());
    }

    public JTable getTable() {
        return tbObat;
    }

    @Override
    public void dispose() {
        executor.shutdownNow();
        super.dispose();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private widget.Button BtnAll;
    private widget.Button BtnCari;
    private widget.Button BtnKeluar;
    private widget.Button BtnKirim;
    private widget.Button BtnPrint;
    private widget.Button BtnUpdate;
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
    private javax.swing.JMenuItem ppPilihSemua;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables
}
