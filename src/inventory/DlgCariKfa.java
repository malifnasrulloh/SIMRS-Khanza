/*
  Dilarang keras menggandakan/mengcopy/menyebarkan/membajak/mendecompile
  Software ini dalam bentuk apapun tanpa seijin pembuat software
  (Khanza.Soft Media). Bagi yang sengaja membajak softaware ini ta
  npa ijin, kami sumpahi sial 1000 turunan, miskin sampai 500 turu
  nan. Selalu mendapat kecelakaan sampai 400 turunan. Anak pertama
  nya cacat tidak punya kaki sampai 300 turunan. Susah cari jodoh
  sampai umur 50 tahun sampai 200 turunan. Ya Alloh maafkan kami
  karena telah berdoa buruk, semua ini kami lakukan karena kami ti
  dak pernah rela karya kami dibajak tanpa ijin.
 */
package inventory;

import bridging.ApiSatuSehat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

public class DlgCariKfa extends javax.swing.JDialog {

    private DefaultTableModel tabMode;
    private sekuel Sequel = new sekuel();
    private validasi Valid = new validasi();
    private Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private int i = 0;
    private ResultSet rs;
    private String link = "", json = "";
    private HttpHeaders headers;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    private JsonNode root;
    private JsonNode nameNode;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private volatile boolean ceksukses = false;
    private ApiSatuSehat api = new ApiSatuSehat();

    public DlgCariKfa(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        tabMode = new DefaultTableModel(null, new Object[]{
            "KFA Code", "KFA Display", "Form Code", "Form Display", "Numerator Code", "Denomina Code", "Route Code", "Route Display", "Registrar", "Nama Dagang", "HNA (Rp)", "HET (Rp)"
        }) {
            Class[] types = new Class[]{
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class,
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }
        };
        tbObat.setModel(tabMode);

        tbObat.setPreferredScrollableViewportSize(new Dimension(800, 800));
        tbObat.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        for (i = 0; i < 12; i++) {
            TableColumn column = tbObat.getColumnModel().getColumn(i);
            if (i == 0) {
                column.setPreferredWidth(80);
            } else if (i == 1) {
                column.setPreferredWidth(400);
            } else if (i == 2) {
                column.setPreferredWidth(80);
            } else if (i == 3) {
                column.setPreferredWidth(80);
            } else if (i == 4) {
                column.setPreferredWidth(80);
            } else if (i == 5) {
                column.setPreferredWidth(80);
            } else if (i == 6) {
                column.setPreferredWidth(80);
            } else if (i == 7) {
                column.setPreferredWidth(80);
            } else if (i == 8) {
                column.setPreferredWidth(200);
            } else if (i == 9) {
                column.setPreferredWidth(200);
            } else if (i == 10) {
                column.setPreferredWidth(110);
            } else if (i == 11) {
                column.setPreferredWidth(110);
            }
        }
        tbObat.setDefaultRenderer(Object.class, new WarnaTable());

        BtnMiningKFA = new widget.Button();
        java.net.URL iconMining = getClass().getResource("/picture/system-software-update.png");
        if (iconMining != null) {
            BtnMiningKFA.setIcon(new javax.swing.ImageIcon(iconMining));
        }
        BtnMiningKFA.setText("Mining Farmasi");
        BtnMiningKFA.setToolTipText("Download semua data KFA Farmasi");
        BtnMiningKFA.setPreferredSize(new java.awt.Dimension(140, 30));
        BtnMiningKFA.addActionListener(e -> {
            BtnMiningKFA.setEnabled(false);
            BtnMiningKFA.setText("Mining...");
            new javax.swing.SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    miningKFA("farmasi");
                    return null;
                }

                @Override
                protected void done() {
                    BtnMiningKFA.setEnabled(true);
                    BtnMiningKFA.setText("Mining Farmasi");
                    runBackground(() -> tampil());
                }
            }.execute();
        });
        panelisi2.add(BtnMiningKFA);

        BtnMiningAlkes = new widget.Button();
        java.net.URL iconAlkes = getClass().getResource("/picture/system-software-update.png");
        if (iconAlkes != null) {
            BtnMiningAlkes.setIcon(new javax.swing.ImageIcon(iconAlkes));
        }
        BtnMiningAlkes.setText("Mining Alkes");
        BtnMiningAlkes.setToolTipText("Download semua data KFA Alat Kesehatan");
        BtnMiningAlkes.setPreferredSize(new java.awt.Dimension(140, 30));
        BtnMiningAlkes.addActionListener(e -> {
            BtnMiningAlkes.setEnabled(false);
            BtnMiningAlkes.setText("Mining...");
            new javax.swing.SwingWorker<Void, String>() {
                @Override
                protected Void doInBackground() throws Exception {
                    miningKFA("alkes");
                    return null;
                }

                @Override
                protected void done() {
                    BtnMiningAlkes.setEnabled(true);
                    BtnMiningAlkes.setText("Mining Alkes");
                    runBackground(() -> tampil());
                }
            }.execute();
        });
        panelisi2.add(BtnMiningAlkes);

        BtnCariApi = new widget.Button();
        java.net.URL iconCariApi = getClass().getResource("/picture/network-wireless.png");
        if (iconCariApi != null) {
            BtnCariApi.setIcon(new javax.swing.ImageIcon(iconCariApi));
        }
        BtnCariApi.setText("Cari API");
        BtnCariApi.setToolTipText("Cari data KFA langsung dari API Satu Sehat");
        BtnCariApi.setPreferredSize(new java.awt.Dimension(120, 30));
        BtnCariApi.addActionListener(e -> {
            String keyword = TCari.getText().trim();
            if (keyword.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(null, "Isi keyword pencarian terlebih dahulu.", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
                TCari.requestFocus();
                return;
            }
            BtnCariApi.setEnabled(false);
            BtnCariApi.setText("Mencari...");
            new javax.swing.SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() throws Exception {
                    cariKfaApi(keyword);
                    return null;
                }

                @Override
                protected void done() {
                    BtnCariApi.setEnabled(true);
                    BtnCariApi.setText("Cari API");
                }
            }.execute();
        });
        panelisi2.add(BtnCariApi);

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
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        internalFrame1 = new widget.InternalFrame();
        jPanel2 = new javax.swing.JPanel();
        panelisi2 = new widget.panelisi();
        label9 = new widget.Label();
        TCari = new widget.TextBox();
        label13 = new widget.Label();
        TCari2 = new widget.TextBox();
        BtnCari = new widget.Button();
        label10 = new widget.Label();
        LCount = new widget.Label();
        jLabel6 = new widget.Label();
        cmbHlm = new widget.ComboBox();
        BtnKeluar = new widget.Button();
        BtnUpdateKFA = new widget.Button();
        scrollPane1 = new widget.ScrollPane();
        tbObat = new widget.Table();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)), "::[ Data Cari KFA ]::", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 0, 11), new java.awt.Color(50, 50, 50))); // NOI18N
        internalFrame1.setName("internalFrame1"); // NOI18N
        internalFrame1.setLayout(new java.awt.BorderLayout(1, 1));

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setOpaque(false);
        jPanel2.setPreferredSize(new java.awt.Dimension(816, 100));
        jPanel2.setLayout(new java.awt.BorderLayout(1, 1));

        panelisi2.setBackground(new java.awt.Color(255, 150, 255));
        panelisi2.setName("panelisi2"); // NOI18N
        panelisi2.setPreferredSize(new java.awt.Dimension(100, 44));
        panelisi2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 4, 9));

        label9.setText("Key Word :");
        label9.setName("label9"); // NOI18N
        label9.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi2.add(label9);

        TCari.setName("TCari"); // NOI18N
        TCari.setPreferredSize(new java.awt.Dimension(200, 23));
        TCari.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCariKeyPressed(evt);
            }
        });
        panelisi2.add(TCari);

        label13.setText("Registarr:");
        label13.setName("label13"); // NOI18N
        label13.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi2.add(label13);

        TCari2.setName("TCari2"); // NOI18N
        TCari2.setPreferredSize(new java.awt.Dimension(150, 23));
        TCari2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TCari2KeyPressed(evt);
            }
        });
        panelisi2.add(TCari2);

        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png"))); // NOI18N
        BtnCari.setMnemonic('3');
        BtnCari.setToolTipText("Alt+3");
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
        panelisi2.add(BtnCari);

        label10.setText("Record :");
        label10.setName("label10"); // NOI18N
        label10.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi2.add(label10);

        LCount.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        LCount.setText("0");
        LCount.setName("LCount"); // NOI18N
        LCount.setPreferredSize(new java.awt.Dimension(60, 23));
        panelisi2.add(LCount);

        jLabel6.setText("Limit Data :");
        jLabel6.setName("jLabel6"); // NOI18N
        jLabel6.setPreferredSize(new java.awt.Dimension(70, 23));
        panelisi2.add(jLabel6);

        cmbHlm.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "100", "200", "300", "400", "500", "1000", "Semua" }));
        cmbHlm.setName("cmbHlm"); // NOI18N
        cmbHlm.setPreferredSize(new java.awt.Dimension(90, 23));
        panelisi2.add(cmbHlm);

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
        panelisi2.add(BtnKeluar);

        BtnUpdateKFA.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/inventaris.png"))); // NOI18N
        BtnUpdateKFA.setMnemonic('K');
        BtnUpdateKFA.setText("Update KFA");
        BtnUpdateKFA.setToolTipText("Alt+K");
        BtnUpdateKFA.setName("BtnUpdateKFA"); // NOI18N
        BtnUpdateKFA.setPreferredSize(new java.awt.Dimension(150, 30));
        BtnUpdateKFA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BtnUpdateKFAActionPerformed(evt);
            }
        });
        BtnUpdateKFA.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                BtnUpdateKFAKeyPressed(evt);
            }
        });
        panelisi2.add(BtnUpdateKFA);

        jPanel2.add(panelisi2, java.awt.BorderLayout.PAGE_START);

        internalFrame1.add(jPanel2, java.awt.BorderLayout.PAGE_END);

        scrollPane1.setName("scrollPane1"); // NOI18N
        scrollPane1.setOpaque(true);

        tbObat.setAutoCreateRowSorter(true);
        tbObat.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {},
                {},
                {},
                {}
            },
            new String [] {

            }
        ));
        tbObat.setToolTipText("Silahkan klik untuk memilih data yang mau diedit ataupun dihapus");
        tbObat.setName("tbObat"); // NOI18N
        tbObat.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tbObatMouseClicked(evt);
            }
        });
        tbObat.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                tbObatKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tbObatKeyReleased(evt);
            }
        });
        scrollPane1.setViewportView(tbObat);

        internalFrame1.add(scrollPane1, java.awt.BorderLayout.CENTER);

        getContentPane().add(internalFrame1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void TCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCariKeyPressed
        runBackground(() -> tampil());
}//GEN-LAST:event_TCariKeyPressed

    private void BtnCariActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnCariActionPerformed
        runBackground(() -> tampil());

}//GEN-LAST:event_BtnCariActionPerformed

    private void BtnCariKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnCariKeyPressed
        runBackground(() -> tampil());
}//GEN-LAST:event_BtnCariKeyPressed

    private void tbObatMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tbObatMouseClicked

}//GEN-LAST:event_tbObatMouseClicked

    private void tbObatKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyPressed
        if (tabMode.getRowCount() != 0) {
            if (evt.getKeyCode() == KeyEvent.VK_SHIFT) {
                TCari.setText("");
                TCari.requestFocus();
            }
        }
}//GEN-LAST:event_tbObatKeyPressed

    private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
        runBackground(() -> tampil());
    }//GEN-LAST:event_formWindowOpened

    private void tbObatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tbObatKeyReleased

    }//GEN-LAST:event_tbObatKeyReleased

    private void TCari2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_TCari2KeyPressed
        runBackground(() -> tampil());
    }//GEN-LAST:event_TCari2KeyPressed

    private void BtnKeluarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnKeluarActionPerformed
        dispose();
    }//GEN-LAST:event_BtnKeluarActionPerformed

    private void BtnKeluarKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnKeluarKeyPressed
        dispose();
    }//GEN-LAST:event_BtnKeluarKeyPressed

    private void BtnUpdateKFAActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnUpdateKFAActionPerformed
        // Ambil keyword dan registrar dari textbox
        String keyword = TCari.getText().trim();
        String registrar = TCari2.getText().trim();

        // Validasi input
        if (keyword.isEmpty()) {
            javax.swing.JOptionPane.showMessageDialog(null, "Keyword tidak boleh kosong!", "Peringatan", javax.swing.JOptionPane.WARNING_MESSAGE);
            TCari.requestFocus();
            return;
        }

        link = "https://api-satusehat.kemkes.go.id/kfa-v2/";

        try {
            // Setup headers untuk API request
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
            requestEntity = new HttpEntity(headers);

            int limit = 100;
            int totalSaved = 0;
            int totalFailed = 0;

            System.out.println("=== Mulai pencarian KFA ===");
            System.out.println("Keyword: " + keyword);
            System.out.println("Registrar: " + (registrar.isEmpty() ? "Semua" : registrar));

            // Loop untuk pagination (max 10 halaman untuk safety)
            for (int page = 1; page <= 10; page++) {
                System.out.println("Mengambil halaman ke-" + page + "...");

                // Build URL dengan parameter search
                String url = link + "products/all?page=" + page + "&size=" + limit + "&product_type=farmasi";

                // Tambahkan parameter keyword (search by name)
                if (!keyword.isEmpty()) {
                    url += "&name=" + java.net.URLEncoder.encode(keyword, "UTF-8");
                }

                // Tambahkan parameter registrar jika diisi
                if (!registrar.isEmpty()) {
                    url += "&registrar=" + java.net.URLEncoder.encode(registrar, "UTF-8");
                }

                System.out.println("URL: " + url);

                // Call API
                json = api.getRest().exchange(url, HttpMethod.GET, requestEntity, String.class).getBody();
                root = mapper.readTree(json);

                // Parse response
                JsonNode dataArray = root.path("items").path("data");

                // Jika tidak ada data, hentikan loop
                if (dataArray.size() == 0) {
                    System.out.println("Tidak ada data lagi di halaman " + page);
                    break;
                }

                System.out.println("Ditemukan " + dataArray.size() + " produk di halaman " + page);

                // Process each product
                for (JsonNode data : dataArray) {
                    try {
                        // Extract all fields
                        String name = data.path("name").asText();
                        String kfaCode = data.path("kfa_code").asText();
                        String active = data.path("active").asText();
                        String state = data.path("state").asText();
                        String image = data.path("image").asText();
                        String updatedAt = data.path("updated_at").asText();
                        String produksiBuatan = data.path("produksi_buatan").asText();
                        String nie = data.path("nie").asText();
                        String namaDagang = data.path("nama_dagang").asText();
                        String manufacturer = data.path("manufacturer").asText();
                        String registrarData = data.path("registrar").asText();
                        String generik = data.path("generik").asText();
                        String rxterm = data.path("rxterm").asText();
                        String dosePerUnit = data.path("dose_per_unit").asText();
                        String fixPrice = data.path("fix_price").asText();
                        String hetPrice = data.path("het_price").asText();
                        String farmalkesHscode = data.path("farmalkes_hscode").asText();
                        String tayangLkpp = data.path("tayang_lkpp").asText();
                        String kodeLkpp = data.path("kode_lkpp").asText();
                        String netWeight = data.path("net_weight").asText();
                        String netWeightUomName = data.path("net_weight_uom_name").asText();
                        String volume = data.path("volume").asText();
                        String volumeUomName = data.path("volume_uom_name").asText();
                        String dosageFormCode = data.path("dosage_form").path("code").asText();
                        String dosageFormName = data.path("dosage_form").path("name").asText();
                        String productTemplateKfaCode = data.path("product_template").path("kfa_code").asText();
                        String productTemplateName = data.path("product_template").path("name").asText();
                        String productTemplateState = data.path("product_template").path("state").asText();
                        String productTemplateActive = data.path("product_template").path("active").asText();
                        String productTemplateDisplayName = data.path("product_template").path("display_name").asText();
                        String productTemplateUpdatedAt = data.path("product_template").path("updated_at").asText();

                        // Store data into MySQL table
                        if (Sequel.menyimpantf2("satu_sehat_kfa_master", "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?",
                                "Kfa Master",
                                31, // 31 columns
                                new String[]{name, kfaCode, active, state, image, updatedAt, produksiBuatan, nie, namaDagang, manufacturer, registrarData, generik, rxterm, dosePerUnit, fixPrice, hetPrice, farmalkesHscode, tayangLkpp, kodeLkpp, netWeight, netWeightUomName, volume, volumeUomName, dosageFormCode, dosageFormName, productTemplateKfaCode, productTemplateName, productTemplateState, productTemplateActive, productTemplateDisplayName, productTemplateUpdatedAt}
                        ) == true) {
                            totalSaved++;
                            System.out.println("✓ Tersimpan: " + name + " (" + kfaCode + ")");
                        } else {
                            totalFailed++;
                        }
                    } catch (Exception e) {
                        totalFailed++;
                        System.out.println("✗ Error menyimpan produk: " + e.getMessage());
                    }
                }

                // Delay untuk menghindari rate limit
                Thread.sleep(2000);

                // Jika data kurang dari limit, berarti sudah halaman terakhir
                if (dataArray.size() < limit) {
                    System.out.println("Halaman terakhir tercapai");
                    break;
                }
            }

            // Tampilkan summary
            System.out.println("\n=== SELESAI ===");
            System.out.println("Total tersimpan: " + totalSaved);
            System.out.println("Total gagal/duplikat: " + totalFailed);

            javax.swing.JOptionPane.showMessageDialog(null,
                    "Pencarian selesai!\n"
                    + "Total tersimpan: " + totalSaved + "\n"
                    + "Total gagal/duplikat: " + totalFailed,
                    "Informasi",
                    javax.swing.JOptionPane.INFORMATION_MESSAGE);

            // Refresh tampilan
            runBackground(() -> tampil());

        } catch (Exception ea) {
            System.out.println("Error Bridging KFA: " + ea);
            ea.printStackTrace();
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Error: " + ea.getMessage(),
                    "Error",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_BtnUpdateKFAActionPerformed

    private void BtnUpdateKFAKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_BtnUpdateKFAKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnUpdateKFAKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            DlgCariKfa dialog = new DlgCariKfa(new javax.swing.JFrame(), true);
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
    private widget.Button BtnCari;
    private widget.Button BtnKeluar;
    private widget.Button BtnUpdateKFA;
    private widget.Label LCount;
    private widget.TextBox TCari;
    private widget.TextBox TCari2;
    private widget.ComboBox cmbHlm;
    private widget.InternalFrame internalFrame1;
    private widget.Label jLabel6;
    private javax.swing.JPanel jPanel2;
    private widget.Label label10;
    private widget.Label label13;
    private widget.Label label9;
    private widget.panelisi panelisi2;
    private widget.ScrollPane scrollPane1;
    private widget.Table tbObat;
    // End of variables declaration//GEN-END:variables
    private widget.Button BtnMiningKFA;
    private widget.Button BtnMiningAlkes;
    private widget.Button BtnCariApi;

    private void cariKfaApi(String keyword) {
        try {
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
            requestEntity = new HttpEntity(headers);

            String encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
            String url = "https://api-satusehat.kemkes.go.id/kfa-v2/products/all"
                    + "?page=1&size=200&product_type=farmasi&keyword=" + encodedKeyword;

            System.out.println("Cari KFA API: " + url);
            json = api.getRest().exchange(url, HttpMethod.GET, requestEntity, String.class).getBody();
            root = mapper.readTree(json);

            JsonNode dataArray = root.path("items").path("data");
            int total = root.path("total").asInt();
            System.out.println("Total ditemukan: " + total);

            javax.swing.SwingUtilities.invokeLater(() -> {
                Valid.tabelKosong(tabMode);
                for (JsonNode data : dataArray) {
                    String kfaCode = data.path("kfa_code").asText();
                    String name = data.path("name").asText();
                    String dfCode = data.path("dosage_form").path("code").asText();
                    String dfName = data.path("dosage_form").path("name").asText();
                    String ucumCode = "";
                    JsonNode paketObat = data.path("paket_obat");
                    if (paketObat.isArray() && paketObat.size() > 0) {
                        ucumCode = paketObat.get(0).path("ucum_cs_code").asText();
                    }
                    String ftCode = data.path("farmalkes_type").path("code").asText();
                    String ftName = data.path("farmalkes_type").path("name").asText();
                    String registrar = data.path("registrar").asText();
                    String namaDagang = data.path("nama_dagang").asText();
                    String fixPrice = data.path("fix_price").asText();
                    String hetPrice = data.path("het_price").asText();

                    tabMode.addRow(new Object[]{
                        kfaCode, name, dfCode, dfName,
                        ucumCode, "", ftCode, ftName,
                        registrar, namaDagang, fixPrice, hetPrice
                    });
                }
                LCount.setText(tabMode.getRowCount() + " (API)");
                if (total > 200) {
                    javax.swing.JOptionPane.showMessageDialog(null,
                            "Total ditemukan: " + total + " data.\n"
                            + "Ditampilkan 200 teratas. Gunakan keyword lebih spesifik untuk hasil lebih tepat.",
                            "Info", javax.swing.JOptionPane.INFORMATION_MESSAGE);
                }
            });
        } catch (Exception ea) {
            System.out.println("Error Cari KFA API: " + ea);
            javax.swing.SwingUtilities.invokeLater(()
                    -> javax.swing.JOptionPane.showMessageDialog(null,
                            "Error: " + ea.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE));
        }
    }

    private void miningKFA(String productType) {
        link = "https://api-satusehat.kemkes.go.id/kfa-v2/";
        try {
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("Authorization", "Bearer " + api.TokenSatuSehat());
            requestEntity = new HttpEntity(headers);

            // Pastikan kedua tabel ada (dengan kolom product_type)
            koneksi.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS satu_sehat_kfa_terbaru ("
                    + "kfa_code VARCHAR(50) NOT NULL PRIMARY KEY, product_type VARCHAR(20) DEFAULT 'farmasi', "
                    + "name TEXT, active VARCHAR(10), state VARCHAR(50), "
                    + "image VARCHAR(500), updated_at VARCHAR(50), produksi_buatan VARCHAR(100), nie VARCHAR(100), "
                    + "nama_dagang VARCHAR(255), manufacturer VARCHAR(255), registrar VARCHAR(255), generik VARCHAR(100), "
                    + "rxterm VARCHAR(255), dose_per_unit VARCHAR(50), fix_price VARCHAR(50), het_price VARCHAR(50), "
                    + "farmalkes_hscode VARCHAR(100), tayang_lkpp VARCHAR(10), kode_lkpp VARCHAR(100), "
                    + "net_weight VARCHAR(50), net_weight_uom_name VARCHAR(100), volume VARCHAR(50), volume_uom_name VARCHAR(100), "
                    + "fornas_is_fornas VARCHAR(10), product_template_kfa_code VARCHAR(50), product_template_name VARCHAR(255), "
                    + "product_template_state VARCHAR(50), product_template_active VARCHAR(10), product_template_bmhp VARCHAR(10), "
                    + "product_template_display_name VARCHAR(255), product_template_updated_at VARCHAR(50)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            // Tambah kolom product_type jika belum ada (migrasi tabel lama)
            try {
                koneksi.createStatement().execute(
                        "ALTER TABLE satu_sehat_kfa_terbaru ADD COLUMN product_type VARCHAR(20) DEFAULT 'farmasi' AFTER kfa_code");
            } catch (Exception ignored) {
            }
            koneksi.createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS satu_sehat_kfa_terbaru_detail ("
                    + "kfa_code VARCHAR(50) NOT NULL PRIMARY KEY, "
                    + "farmalkes_type_code VARCHAR(50), farmalkes_type_name VARCHAR(255), farmalkes_type_group VARCHAR(100), "
                    + "dosage_form_code VARCHAR(50), dosage_form_name VARCHAR(255), uom_name VARCHAR(100), "
                    + "med_dev_jenis VARCHAR(255), med_dev_subkategori VARCHAR(255), med_dev_kategori VARCHAR(255), "
                    + "med_dev_kelas_risiko VARCHAR(100), klasifikasi_izin VARCHAR(100), "
                    + "active_ingredients_kfa_code VARCHAR(50), active_ingredients_zat_aktif VARCHAR(255), active_ingredients_kekuatan VARCHAR(100), "
                    + "replacement_product_kfa_code VARCHAR(50), replacement_product_name VARCHAR(255), replacement_product_reason VARCHAR(255), "
                    + "replacement_template_kfa_code VARCHAR(50), replacement_template_name VARCHAR(255), "
                    + "paket_obat_kfa_code VARCHAR(50), paket_obat_qty VARCHAR(50), paket_obat_uom_name VARCHAR(100), "
                    + "paket_obat_ucum_cs_code VARCHAR(50), paket_obat_ucum_name VARCHAR(100), "
                    + "CONSTRAINT fk_kfa_terbaru_detail FOREIGN KEY (kfa_code) REFERENCES satu_sehat_kfa_terbaru(kfa_code) ON DELETE CASCADE"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            PreparedStatement psMaster = koneksi.prepareStatement(
                    "INSERT IGNORE INTO satu_sehat_kfa_terbaru "
                    + "(kfa_code,product_type,name,active,state,image,updated_at,produksi_buatan,nie,nama_dagang,manufacturer,registrar,"
                    + "generik,rxterm,dose_per_unit,fix_price,het_price,farmalkes_hscode,tayang_lkpp,kode_lkpp,"
                    + "net_weight,net_weight_uom_name,volume,volume_uom_name,fornas_is_fornas,"
                    + "product_template_kfa_code,product_template_name,product_template_state,"
                    + "product_template_active,product_template_bmhp,product_template_display_name,product_template_updated_at) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            PreparedStatement psDetail = koneksi.prepareStatement(
                    "INSERT IGNORE INTO satu_sehat_kfa_terbaru_detail "
                    + "(kfa_code,farmalkes_type_code,farmalkes_type_name,farmalkes_type_group,"
                    + "dosage_form_code,dosage_form_name,uom_name,"
                    + "med_dev_jenis,med_dev_subkategori,med_dev_kategori,med_dev_kelas_risiko,klasifikasi_izin,"
                    + "active_ingredients_kfa_code,active_ingredients_zat_aktif,active_ingredients_kekuatan,"
                    + "replacement_product_kfa_code,replacement_product_name,replacement_product_reason,"
                    + "replacement_template_kfa_code,replacement_template_name,"
                    + "paket_obat_kfa_code,paket_obat_qty,paket_obat_uom_name,paket_obat_ucum_cs_code,paket_obat_ucum_name) "
                    + "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            int totalSaved = 0, totalFailed = 0;
            int limit = 100;

            for (int page = 1; page <= 300; page++) {
                System.out.println("Mining KFA " + productType + " halaman ke-" + page);
                json = api.getRest().exchange(
                        link + "products/all?page=" + page + "&size=" + limit + "&product_type=" + productType,
                        HttpMethod.GET, requestEntity, String.class).getBody();
                root = mapper.readTree(json);
                JsonNode dataArray = root.path("items").path("data");

                if (dataArray.size() == 0) {
                    System.out.println("Tidak ada data lagi di halaman " + page);
                    break;
                }

                for (JsonNode d : dataArray) {
                    try {
                        String kfaCode = d.path("kfa_code").asText();
                        // aktif ingredient pertama
                        JsonNode ai = d.path("active_ingredients");
                        String aiKfa = "", aiZat = "", aiKuat = "";
                        if (ai.isArray() && ai.size() > 0) {
                            aiKfa = ai.get(0).path("kfa_code").asText();
                            aiZat = ai.get(0).path("zat_aktif").asText();
                            aiKuat = ai.get(0).path("kekuatan_zat_aktif").asText();
                        }
                        // paket obat pertama
                        JsonNode po = d.path("paket_obat");
                        String poKfa = "", poQty = "", poUom = "", poUcumCode = "", poUcumName = "";
                        if (po.isArray() && po.size() > 0) {
                            poKfa = po.get(0).path("kfa_code").asText();
                            poQty = po.get(0).path("qty").asText();
                            poUom = po.get(0).path("uom_name").asText();
                            poUcumCode = po.get(0).path("ucum_cs_code").asText();
                            poUcumName = po.get(0).path("ucum_name").asText();
                        }

                        // Insert master (param 1=kfa_code, 2=product_type, 3..32=data)
                        psMaster.setString(1, kfaCode);
                        psMaster.setString(2, productType);
                        psMaster.setString(3, d.path("name").asText());
                        psMaster.setString(4, d.path("active").asText());
                        psMaster.setString(5, d.path("state").asText());
                        psMaster.setString(6, d.path("image").asText());
                        psMaster.setString(7, d.path("updated_at").asText());
                        psMaster.setString(8, d.path("produksi_buatan").asText());
                        psMaster.setString(9, d.path("nie").asText());
                        psMaster.setString(10, d.path("nama_dagang").asText());
                        psMaster.setString(11, d.path("manufacturer").asText());
                        psMaster.setString(12, d.path("registrar").asText());
                        psMaster.setString(13, d.path("generik").asText());
                        psMaster.setString(14, d.path("rxterm").asText());
                        psMaster.setString(15, d.path("dose_per_unit").asText());
                        psMaster.setString(16, d.path("fix_price").asText());
                        psMaster.setString(17, d.path("het_price").asText());
                        psMaster.setString(18, d.path("farmalkes_hscode").asText());
                        psMaster.setString(19, d.path("tayang_lkpp").asText());
                        psMaster.setString(20, d.path("kode_lkpp").asText());
                        psMaster.setString(21, d.path("net_weight").asText());
                        psMaster.setString(22, d.path("net_weight_uom_name").asText());
                        psMaster.setString(23, d.path("volume").asText());
                        psMaster.setString(24, d.path("volume_uom_name").asText());
                        psMaster.setString(25, d.path("fornas").path("is_fornas").asText());
                        psMaster.setString(26, d.path("product_template").path("kfa_code").asText());
                        psMaster.setString(27, d.path("product_template").path("name").asText());
                        psMaster.setString(28, d.path("product_template").path("state").asText());
                        psMaster.setString(29, d.path("product_template").path("active").asText());
                        psMaster.setString(30, d.path("product_template").path("bmhp").asText());
                        psMaster.setString(31, d.path("product_template").path("display_name").asText());
                        psMaster.setString(32, d.path("product_template").path("updated_at").asText());
                        psMaster.executeUpdate();

                        // Insert detail
                        psDetail.setString(1, kfaCode);
                        psDetail.setString(2, d.path("farmalkes_type").path("code").asText());
                        psDetail.setString(3, d.path("farmalkes_type").path("name").asText());
                        psDetail.setString(4, d.path("farmalkes_type").path("group").asText());
                        psDetail.setString(5, d.path("dosage_form").path("code").asText());
                        psDetail.setString(6, d.path("dosage_form").path("name").asText());
                        psDetail.setString(7, d.path("uom").path("name").asText());
                        psDetail.setString(8, d.path("med_dev_jenis").asText());
                        psDetail.setString(9, d.path("med_dev_subkategori").asText());
                        psDetail.setString(10, d.path("med_dev_kategori").asText());
                        psDetail.setString(11, d.path("med_dev_kelas_risiko").asText());
                        psDetail.setString(12, d.path("klasifikasi_izin").asText());
                        psDetail.setString(13, aiKfa);
                        psDetail.setString(14, aiZat);
                        psDetail.setString(15, aiKuat);
                        psDetail.setString(16, d.path("replacement").path("product").path("kfa_code").asText());
                        psDetail.setString(17, d.path("replacement").path("product").path("name").asText());
                        psDetail.setString(18, d.path("replacement").path("product").path("reason").asText());
                        psDetail.setString(19, d.path("replacement").path("template").path("kfa_code").asText());
                        psDetail.setString(20, d.path("replacement").path("template").path("name").asText());
                        psDetail.setString(21, poKfa);
                        psDetail.setString(22, poQty);
                        psDetail.setString(23, poUom);
                        psDetail.setString(24, poUcumCode);
                        psDetail.setString(25, poUcumName);
                        psDetail.executeUpdate();

                        totalSaved++;
                        System.out.println("Tersimpan: " + d.path("name").asText());
                    } catch (Exception ex) {
                        totalFailed++;
                        System.out.println("Gagal: " + ex.getMessage());
                    }
                }

                if (dataArray.size() < limit) {
                    System.out.println("Halaman terakhir tercapai.");
                    break;
                }
                Thread.sleep(5000);
            }

            psMaster.close();
            psDetail.close();

            final int saved = totalSaved, failed = totalFailed;
            final String pt = productType;
            javax.swing.SwingUtilities.invokeLater(()
                    -> javax.swing.JOptionPane.showMessageDialog(null,
                            "Mining " + pt + " selesai!\nTersimpan : " + saved + "\nGagal/duplikat: " + failed,
                            "Informasi", javax.swing.JOptionPane.INFORMATION_MESSAGE));

        } catch (Exception ea) {
            System.out.println("Notifikasi Bridging : " + ea);
            miningKFA(productType);
        }
    }

    private void tampil() {
        Valid.tabelKosong(tabMode);
        try {
            String limitStr = cmbHlm.getSelectedItem().toString();
            String limitClause = "Semua".equalsIgnoreCase(limitStr) ? "" : " LIMIT " + limitStr;
            String baseSelect
                    = "select m.kfa_code, m.name, d.dosage_form_code, d.dosage_form_name, "
                    + "d.paket_obat_ucum_cs_code, '', d.farmalkes_type_code, d.farmalkes_type_name, "
                    + "m.registrar, m.nama_dagang, m.fix_price, m.het_price "
                    + "from satu_sehat_kfa_terbaru m "
                    + "left join satu_sehat_kfa_terbaru_detail d on m.kfa_code = d.kfa_code "
                    + "where m.registrar like ? and m.name like ? "
                    + "order by m.kfa_code, m.name asc" + limitClause;
            ps = koneksi.prepareStatement(baseSelect);
            ps.setString(1, "%" + TCari2.getText().trim() + "%");
            ps.setString(2, "%" + TCari.getText().trim() + "%");

            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4),
                        rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8),
                        rs.getString(9), rs.getString(10), rs.getString(11), rs.getString(12)
                    });
                }
                LCount.setText("" + tabMode.getRowCount());
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    public JTable getTable() {
        return tbObat;
    }

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
            System.out.println("Notif : " + ex);
            ceksukses = false;
        }
    }

    public void isCek() {
        TCari.requestFocus();
    }
}
