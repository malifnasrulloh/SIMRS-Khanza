package setting;

import fungsi.NotifikasiEngine;
import fungsi.TableColumnAdjuster;
import fungsi.WarnaTable;
import fungsi.koneksiDB;
import fungsi.sekuel;
import fungsi.validasi;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import widget.Button;
import widget.InternalFrame;
import widget.Label;
import widget.ScrollPane;
import widget.Table;
import widget.TextBox;
import widget.panelisi;

public class DlgSetNotifikasi extends JDialog {
    private final DefaultTableModel tabMode;
    private final Connection koneksi = koneksiDB.condb();
    private final sekuel Sequel = new sekuel();
    private final validasi Valid = new validasi();

    private InternalFrame internalFrame1;
    private ScrollPane scrollPane;
    private Table tbUser;
    private panelisi panelTopInput;
    private JPanel panelBottom;
    private panelisi panelSearch;
    private panelisi panelButtons;

    private Label lblKd;
    private TextBox TKd;
    private Label lblNm;
    private TextBox TNmUser;
    private Label lblJbtn;
    private TextBox TJabatan;

    private JCheckBox chkLab;
    private JCheckBox chkResep;
    private JCheckBox chkRad;
    private JCheckBox chkSuara;

    private Label lblCari;
    private TextBox TCari;
    private Button BtnCari;
    private Button BtnAll;
    private Label lblRecord;
    private Label LCount;

    private Button BtnSimpan;
    private Button BtnBatal;
    private Button BtnHapus;
    private Button BtnKeluar;

    public DlgSetNotifikasi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        tabMode = new DefaultTableModel(null, new Object[]{
            "ID User", "Nama User", "Jabatan", "Permintaan Lab", "Resep Obat", "Permintaan Radiologi", "Suara Bell"
        }) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col >= 3;
            }

            @Override
            public Class<?> getColumnClass(int col) {
                switch (col) {
                    case 0:
                    case 1:
                    case 2:
                        return Object.class;
                    default:
                        return Boolean.class;
                }
            }
        };

        tbUser.setModel(tabMode);
        tbUser.setPreferredScrollableViewportSize(new Dimension(500, 500));
        tbUser.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        TableColumnAdjuster tca = new TableColumnAdjuster(tbUser);
        tca.setColumnHeaderIncluded(true);
        tca.setColumnDataIncluded(true);
        tca.setDynamicAdjustment(true);
        tca.setOnlyAdjustLarger(false);
        tca.adjustColumns();

        tbUser.setDefaultRenderer(Object.class, new WarnaTable());
        tbUser.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (tbUser.getSelectedRow() != -1) {
                    pilihUser();
                }
            }
        });

        if (koneksiDB.CARICEPAT().equals("aktif")) {
            TCari.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
                @Override
                public void insertUpdate(javax.swing.event.DocumentEvent e) {
                    cekCari();
                }

                @Override
                public void removeUpdate(javax.swing.event.DocumentEvent e) {
                    cekCari();
                }

                @Override
                public void changedUpdate(javax.swing.event.DocumentEvent e) {
                    cekCari();
                }

                private void cekCari() {
                    if (TCari.getText().length() > 2 || TCari.getText().trim().isEmpty()) {
                        tampil();
                    }
                }
            });
        }

        tampil();
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setTitle("::[ Pengaturan Notifikasi Pengguna ]::");
        getContentPane().setLayout(new BorderLayout());

        internalFrame1 = new InternalFrame();
        internalFrame1.setName("internalFrame1");
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Pengaturan Notifikasi Pengguna ]::",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Tahoma", Font.PLAIN, 11),
                new java.awt.Color(50, 50, 50)));
        internalFrame1.setLayout(new BorderLayout(1, 1));

        // Top input panel
        panelTopInput = new panelisi();
        panelTopInput.setPreferredSize(new Dimension(100, 72));
        panelTopInput.setLayout(null);

        lblKd = new Label();
        lblKd.setText("ID User :");
        lblKd.setBounds(10, 10, 60, 23);
        panelTopInput.add(lblKd);

        TKd = new TextBox();
        TKd.setBounds(75, 10, 120, 23);
        TKd.setEditable(false);
        panelTopInput.add(TKd);

        lblNm = new Label();
        lblNm.setText("Nama :");
        lblNm.setBounds(205, 10, 50, 23);
        panelTopInput.add(lblNm);

        TNmUser = new TextBox();
        TNmUser.setBounds(260, 10, 200, 23);
        TNmUser.setEditable(false);
        panelTopInput.add(TNmUser);

        lblJbtn = new Label();
        lblJbtn.setText("Jabatan :");
        lblJbtn.setBounds(470, 10, 60, 23);
        panelTopInput.add(lblJbtn);

        TJabatan = new TextBox();
        TJabatan.setBounds(535, 10, 190, 23);
        TJabatan.setEditable(false);
        panelTopInput.add(TJabatan);

        // Checkboxes row
        chkLab = new JCheckBox("Permintaan Lab");
        chkLab.setBounds(75, 40, 130, 23);
        chkLab.setOpaque(false);
        panelTopInput.add(chkLab);

        chkResep = new JCheckBox("Resep Obat");
        chkResep.setBounds(215, 40, 110, 23);
        chkResep.setOpaque(false);
        panelTopInput.add(chkResep);

        chkRad = new JCheckBox("Permintaan Radiologi");
        chkRad.setBounds(335, 40, 160, 23);
        chkRad.setOpaque(false);
        panelTopInput.add(chkRad);

        chkSuara = new JCheckBox("Suara Bell");
        chkSuara.setBounds(505, 40, 110, 23);
        chkSuara.setSelected(true);
        chkSuara.setOpaque(false);
        panelTopInput.add(chkSuara);

        internalFrame1.add(panelTopInput, BorderLayout.PAGE_START);

        // Table in center
        tbUser = new Table();
        scrollPane = new ScrollPane();
        scrollPane.setViewportView(tbUser);
        internalFrame1.add(scrollPane, BorderLayout.CENTER);

        // Bottom panel
        panelBottom = new JPanel();
        panelBottom.setOpaque(false);
        panelBottom.setLayout(new BorderLayout(0, 1));

        // Search panel
        panelSearch = new panelisi();
        panelSearch.setPreferredSize(new Dimension(44, 44));
        panelSearch.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        lblCari = new Label();
        lblCari.setText("Key Word :");
        lblCari.setPreferredSize(new Dimension(65, 23));
        panelSearch.add(lblCari);

        TCari = new TextBox();
        TCari.setPreferredSize(new Dimension(350, 23));
        TCari.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                    tampil();
                }
            }
        });
        panelSearch.add(TCari);

        BtnCari = new Button();
        BtnCari.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnCari.setText("Cari");
        BtnCari.setPreferredSize(new Dimension(75, 23));
        BtnCari.addActionListener((ActionEvent e) -> tampil());
        panelSearch.add(BtnCari);

        BtnAll = new Button();
        BtnAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnAll.setText("Semua");
        BtnAll.setPreferredSize(new Dimension(85, 23));
        BtnAll.addActionListener((ActionEvent e) -> {
            TCari.setText("");
            tampil();
        });
        panelSearch.add(BtnAll);

        lblRecord = new Label();
        lblRecord.setText("Record :");
        lblRecord.setPreferredSize(new Dimension(60, 23));
        panelSearch.add(lblRecord);

        LCount = new Label();
        LCount.setText("0");
        LCount.setPreferredSize(new Dimension(50, 23));
        panelSearch.add(LCount);

        panelBottom.add(panelSearch, BorderLayout.PAGE_START);

        // Buttons panel
        panelButtons = new panelisi();
        panelButtons.setPreferredSize(new Dimension(55, 50));
        panelButtons.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 9));

        BtnSimpan = new Button();
        BtnSimpan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/save-16x16.png")));
        BtnSimpan.setText("Simpan");
        BtnSimpan.setPreferredSize(new Dimension(100, 30));
        BtnSimpan.addActionListener((ActionEvent e) -> simpan());
        panelButtons.add(BtnSimpan);

        BtnBatal = new Button();
        BtnBatal.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Cancel-2-16x16.png")));
        BtnBatal.setText("Baru");
        BtnBatal.setPreferredSize(new Dimension(100, 30));
        BtnBatal.addActionListener((ActionEvent e) -> emptTeks());
        panelButtons.add(BtnBatal);

        BtnHapus = new Button();
        BtnHapus.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/stop_f2.png")));
        BtnHapus.setText("Hapus");
        BtnHapus.setPreferredSize(new Dimension(100, 30));
        BtnHapus.addActionListener((ActionEvent e) -> hapus());
        panelButtons.add(BtnHapus);

        BtnKeluar = new Button();
        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setText("Keluar");
        BtnKeluar.setPreferredSize(new Dimension(100, 30));
        BtnKeluar.addActionListener((ActionEvent e) -> dispose());
        panelButtons.add(BtnKeluar);

        panelBottom.add(panelButtons, BorderLayout.PAGE_END);

        internalFrame1.add(panelBottom, BorderLayout.PAGE_END);
        getContentPane().add(internalFrame1, BorderLayout.CENTER);
    }

    public void tampil() {
        tabMode.setRowCount(0);
        String rawKey = TCari.getText().trim();
        String keyword = "%" + rawKey + "%";
        String sql =
            "SELECT 'Admin Utama' AS id_user, 'Administrator Utama' AS nama_user, 'Admin Sistem' AS jabatan, " +
            "       COALESCE(nu.lab_request, 'false') AS lab_request, " +
            "       COALESCE(nu.prescription_request, 'false') AS prescription_request, " +
            "       COALESCE(nu.radiology_request, 'false') AS radiology_request, " +
            "       COALESCE(nu.suara_bell, 'true') AS suara_bell " +
            "FROM (SELECT 1) AS dummy " +
            "LEFT JOIN notifikasi_user nu ON nu.id_user = 'Admin Utama' " +
            "WHERE 'Admin Utama' LIKE ? OR 'Administrator Utama' LIKE ? " +
            "UNION " +
            "SELECT AES_DECRYPT(u.id_user, 'nur') AS id_user, " +
            "       COALESCE(p.nama, 'Petugas / User') AS nama_user, " +
            "       COALESCE(p.jbtn, '-') AS jabatan, " +
            "       COALESCE(nu.lab_request, 'false') AS lab_request, " +
            "       COALESCE(nu.prescription_request, 'false') AS prescription_request, " +
            "       COALESCE(nu.radiology_request, 'false') AS radiology_request, " +
            "       COALESCE(nu.suara_bell, 'true') AS suara_bell " +
            "FROM user u " +
            "LEFT JOIN pegawai p ON AES_DECRYPT(u.id_user, 'nur') = p.nik " +
            "LEFT JOIN notifikasi_user nu ON AES_DECRYPT(u.id_user, 'nur') = nu.id_user OR nu.id_user = u.id_user " +
            "WHERE AES_DECRYPT(u.id_user, 'nur') LIKE ? OR p.nama LIKE ? OR p.jbtn LIKE ? " +
            "ORDER BY id_user";

        try (PreparedStatement ps = koneksi.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, keyword);
            ps.setString(3, keyword);
            ps.setString(4, keyword);
            ps.setString(5, keyword);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tabMode.addRow(new Object[]{
                        rs.getString("id_user"),
                        rs.getString("nama_user"),
                        rs.getString("jabatan"),
                        "true".equalsIgnoreCase(rs.getString("lab_request")),
                        "true".equalsIgnoreCase(rs.getString("prescription_request")),
                        "true".equalsIgnoreCase(rs.getString("radiology_request")),
                        "true".equalsIgnoreCase(rs.getString("suara_bell"))
                    });
                }
            }
        } catch (Exception e) {
            System.out.println("DlgSetNotifikasi.tampil error: " + e.getMessage());
        }
        LCount.setText("" + tabMode.getRowCount());
    }

    private void pilihUser() {
        int row = tbUser.getSelectedRow();
        if (row != -1) {
            TKd.setText(tabMode.getValueAt(row, 0).toString());
            TNmUser.setText(tabMode.getValueAt(row, 1).toString());
            TJabatan.setText(tabMode.getValueAt(row, 2).toString());
            chkLab.setSelected((Boolean) tabMode.getValueAt(row, 3));
            chkResep.setSelected((Boolean) tabMode.getValueAt(row, 4));
            chkRad.setSelected((Boolean) tabMode.getValueAt(row, 5));
            chkSuara.setSelected((Boolean) tabMode.getValueAt(row, 6));
        }
    }

    public void emptTeks() {
        TKd.setText("");
        TNmUser.setText("");
        TJabatan.setText("");
        chkLab.setSelected(false);
        chkResep.setSelected(false);
        chkRad.setSelected(false);
        chkSuara.setSelected(true);
        tampil();
    }

    private void simpan() {
        int row = tbUser.getSelectedRow();
        String idUser = TKd.getText().trim();
        if (idUser.isEmpty() && row != -1) {
            idUser = tabMode.getValueAt(row, 0).toString();
        }
        if (idUser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silahkan pilih user terlebih dahulu dari tabel!");
            return;
        }

        boolean labVal = chkLab.isSelected();
        boolean resepVal = chkResep.isSelected();
        boolean radVal = chkRad.isSelected();
        boolean suaraVal = chkSuara.isSelected();

        // If table cell checkboxes were directly edited
        if (row != -1 && idUser.equals(tabMode.getValueAt(row, 0).toString())) {
            // Also take into account whatever is checked in the table row
            if (tbUser.isEditing()) {
                tbUser.getCellEditor().stopCellEditing();
            }
            labVal = (Boolean) tabMode.getValueAt(row, 3);
            resepVal = (Boolean) tabMode.getValueAt(row, 4);
            radVal = (Boolean) tabMode.getValueAt(row, 5);
            suaraVal = (Boolean) tabMode.getValueAt(row, 6);
        }

        String isisimpan = "'" + idUser + "','" + (labVal ? "true" : "false") + "','" + (resepVal ? "true" : "false") + "','" + (radVal ? "true" : "false") + "','" + (suaraVal ? "true" : "false") + "'";
        String isiedit = "lab_request='" + (labVal ? "true" : "false") + "',prescription_request='" + (resepVal ? "true" : "false") + "',radiology_request='" + (radVal ? "true" : "false") + "',suara_bell='" + (suaraVal ? "true" : "false") + "'";
        String acuanField = "id_user='" + idUser + "'";

        if (Sequel.menyimpantf("notifikasi_user", isisimpan, isiedit, acuanField)) {
            JOptionPane.showMessageDialog(this, "Pengaturan notifikasi untuk " + idUser + " berhasil disimpan!");
            NotifikasiEngine.refreshNow();
            tampil();
        }
    }

    private void hapus() {
        int row = tbUser.getSelectedRow();
        String idUser = TKd.getText().trim();
        if (idUser.isEmpty() && row != -1) {
            idUser = tabMode.getValueAt(row, 0).toString();
        }
        if (idUser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Silahkan pilih user terlebih dahulu dari tabel!");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "Reset / hapus pengaturan notifikasi untuk user '" + idUser + "'?", "Konfirmasi", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (Sequel.meghapustf("notifikasi_user", "id_user", idUser)) {
                JOptionPane.showMessageDialog(this, "Pengaturan notifikasi berhasil direset!");
                NotifikasiEngine.refreshNow();
                emptTeks();
            }
        }
    }
}
