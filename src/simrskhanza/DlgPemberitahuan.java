package simrskhanza;

import fungsi.NotifikasiEngine;
import fungsi.NotifikasiModel;
import fungsi.WarnaTable;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.BiConsumer;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import widget.Button;
import widget.Label;
import widget.PanelBiasa;
import widget.ScrollPane;
import widget.Table;

public class DlgPemberitahuan extends JDialog {
    private final DefaultTableModel tabMode;
    private widget.InternalFrame internalFrame1;
    private Table tbNotifikasi;
    private ScrollPane scrollPane;
    private PanelBiasa panelBawah;
    private PanelBiasa panelAtas;
    private Label lblJudul;
    private Button BtnBuka;
    private Button BtnAbaikan;
    private Button BtnAbaikanSemua;
    private Button BtnKeluar;
    private final BiConsumer<String, String> onOpenTarget;

    public DlgPemberitahuan(java.awt.Frame parent, boolean modal) {
        this(parent, modal, null);
    }

    public DlgPemberitahuan(java.awt.Frame parent, boolean modal, BiConsumer<String, String> onOpenTarget) {
        super(parent, modal);
        this.onOpenTarget = onOpenTarget;
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        setResizable(false);
        setTitle("::[ Pemberitahuan Masuk ]::");
        setSize(780, 420);
        setLocationRelativeTo(parent);
        getContentPane().setLayout(new BorderLayout());

        internalFrame1 = new widget.InternalFrame();
        internalFrame1.setBorder(javax.swing.BorderFactory.createTitledBorder(
                javax.swing.BorderFactory.createLineBorder(new java.awt.Color(240, 245, 235)),
                "::[ Pemberitahuan Masuk ]::",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new java.awt.Font("Tahoma", 0, 11),
                new java.awt.Color(50, 50, 50)));
        internalFrame1.setLayout(new BorderLayout(1, 1));

        // Header Panel
        panelAtas = new PanelBiasa();
        panelAtas.setPreferredSize(new Dimension(100, 35));
        panelAtas.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 8));
        lblJudul = new Label();
        lblJudul.setFont(new Font("Tahoma", Font.BOLD, 12));
        lblJudul.setText("Daftar Permintaan & Resep Menunggu Respon");
        panelAtas.add(lblJudul);
        internalFrame1.add(panelAtas, BorderLayout.NORTH);

        // Table
        tabMode = new DefaultTableModel(null, new Object[]{
            "ID", "Waktu", "Jenis", "Pesan", "Keterangan", "Sumber", "No. Referensi"
        }) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tbNotifikasi = new Table();
        tbNotifikasi.setModel(tabMode);
        tbNotifikasi.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        // Column widths
        TableColumn colId = tbNotifikasi.getColumnModel().getColumn(0);
        colId.setMinWidth(0);
        colId.setMaxWidth(0);
        colId.setPreferredWidth(0);

        tbNotifikasi.getColumnModel().getColumn(1).setPreferredWidth(130); // Waktu
        tbNotifikasi.getColumnModel().getColumn(2).setPreferredWidth(140); // Jenis
        tbNotifikasi.getColumnModel().getColumn(3).setPreferredWidth(220); // Pesan
        tbNotifikasi.getColumnModel().getColumn(4).setPreferredWidth(150); // Keterangan
        tbNotifikasi.getColumnModel().getColumn(5).setPreferredWidth(110); // Sumber
        tbNotifikasi.getColumnModel().getColumn(6).setPreferredWidth(130); // No. Ref

        tbNotifikasi.setDefaultRenderer(Object.class, new WarnaTable());
        tbNotifikasi.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    bukaTerpilih();
                }
            }
        });

        scrollPane = new ScrollPane();
        scrollPane.setViewportView(tbNotifikasi);
        internalFrame1.add(scrollPane, BorderLayout.CENTER);

        // Bottom Actions Panel
        panelBawah = new PanelBiasa();
        panelBawah.setPreferredSize(new Dimension(100, 48));
        panelBawah.setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 8));

        BtnBuka = new Button();
        BtnBuka.setText("Buka Form");
        BtnBuka.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/136.png")));
        BtnBuka.setPreferredSize(new Dimension(120, 32));
        BtnBuka.addActionListener((ActionEvent e) -> bukaTerpilih());
        panelBawah.add(BtnBuka);

        BtnAbaikan = new Button();
        BtnAbaikan.setText("Abaikan");
        BtnAbaikan.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/cancel.png")));
        BtnAbaikan.setPreferredSize(new Dimension(100, 32));
        BtnAbaikan.addActionListener((ActionEvent e) -> abaikanTerpilih());
        panelBawah.add(BtnAbaikan);

        BtnAbaikanSemua = new Button();
        BtnAbaikanSemua.setText("Abaikan Semua");
        BtnAbaikanSemua.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Delete.png")));
        BtnAbaikanSemua.setPreferredSize(new Dimension(135, 32));
        BtnAbaikanSemua.addActionListener((ActionEvent e) -> abaikanSemua());
        panelBawah.add(BtnAbaikanSemua);

        BtnKeluar = new Button();
        BtnKeluar.setText("Tutup");
        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setPreferredSize(new Dimension(90, 32));
        BtnKeluar.addActionListener((ActionEvent e) -> dispose());
        panelBawah.add(BtnKeluar);

        internalFrame1.add(panelBawah, BorderLayout.SOUTH);
        getContentPane().add(internalFrame1, BorderLayout.CENTER);
    }

    public void tampil() {
        tabMode.setRowCount(0);
        List<NotifikasiModel> list = NotifikasiEngine.getActiveNotifications();
        for (NotifikasiModel item : list) {
            tabMode.addRow(new Object[]{
                item.getId(),
                item.getCreatedAt(),
                getEventLabel(item.getEventType()),
                item.getTitle(),
                item.getBody(),
                item.getSourceTable(),
                item.getSourcePk()
            });
        }
        lblJudul.setText("Daftar Pemberitahuan (" + list.size() + " Menunggu Respon)");
    }

    private String getEventLabel(String eventType) {
        if ("lab_request".equalsIgnoreCase(eventType)) return "Permintaan Lab";
        if ("prescription_request".equalsIgnoreCase(eventType)) return "Resep Belum Validasi";
        if ("radiology_request".equalsIgnoreCase(eventType)) return "Permintaan Radiologi";
        return eventType;
    }

    private void bukaTerpilih() {
        int row = tbNotifikasi.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silahkan pilih pemberitahuan terlebih dahulu!");
            return;
        }
        String sourceTable = java.util.Objects.toString(tabMode.getValueAt(row, 5), "");
        String sourcePk = java.util.Objects.toString(tabMode.getValueAt(row, 6), "");
        long id = Long.parseLong(java.util.Objects.toString(tabMode.getValueAt(row, 0), "0"));

        // Dismiss this notification item from active badge
        NotifikasiEngine.dismiss(id);
        dispose();

        if (onOpenTarget != null) {
            onOpenTarget.accept(sourceTable, sourcePk);
        }
    }

    private void abaikanTerpilih() {
        int row = tbNotifikasi.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Silahkan pilih pemberitahuan yang ingin diabaikan!");
            return;
        }
        long id = Long.parseLong(tabMode.getValueAt(row, 0).toString());
        NotifikasiEngine.dismiss(id);
        tabMode.removeRow(row);
        lblJudul.setText("Daftar Pemberitahuan (" + tabMode.getRowCount() + " Menunggu Respon)");
    }

    private void abaikanSemua() {
        if (tabMode.getRowCount() == 0) return;
        NotifikasiEngine.dismissAll();
        tabMode.setRowCount(0);
        lblJudul.setText("Daftar Pemberitahuan (0 Menunggu Respon)");
    }
}
