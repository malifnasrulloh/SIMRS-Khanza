/*
 * SatuSehatRekonsiliasiStudyRadiologi.java
 *
 * Dedicated modal reconciliation dialog for matching SIMRS Khanza Radiology
 * Orders with ambiguous candidate studies in Orthanc PACS for Satu Sehat
 * ServiceRequest & ImagingStudy bridging.
 */
package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import fungsi.WarnaTable;
import fungsi.koneksiDB;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import widget.Button;
import widget.InternalFrame;
import widget.Label;
import widget.PanelBiasa;
import widget.ScrollPane;
import widget.Table;
import widget.panelisi;

/**
 * Modern modal dialog for side-by-side reconciliation of SIMRS Radiology
 * requests vs multiple ambiguous Orthanc PACS candidate studies.
 *
 * @author malifnasruloh
 */
public class SatuSehatRekonsiliasiStudyRadiologi extends JDialog {

    private final DefaultTableModel tabMode;
    private final Table tbCandidates;
    private final Label lblPatient;
    private final Label lblOrder;
    private final Label lblTime;
    private final Label lblDoc;
    private final Label lblDiag;

    private final Button BtnPilih;
    private final Button BtnViewer;
    private final Button BtnKeluar;

    private final List<CandidateItem> candidateList = new ArrayList<>();
    private String selectedStudyId = "";
    private final ApiOrthanc orthanc = new ApiOrthanc();

    public static class CandidateItem {
        public final String studyId;
        public final int score;
        public final String matchLabel;
        public final String modality;
        public final String studyDate;
        public final String studyTime;
        public final String studyDesc;
        public final String studyAcsn;
        public final int seriesCount;
        public final JsonNode studyNode;

        public CandidateItem(String studyId, int score, String matchLabel, String modality,
                             String studyDate, String studyTime, String studyDesc,
                             String studyAcsn, int seriesCount, JsonNode studyNode) {
            this.studyId = studyId;
            this.score = score;
            this.matchLabel = matchLabel;
            this.modality = modality;
            this.studyDate = studyDate;
            this.studyTime = studyTime;
            this.studyDesc = studyDesc;
            this.studyAcsn = studyAcsn;
            this.seriesCount = seriesCount;
            this.studyNode = studyNode;
        }
    }

    public SatuSehatRekonsiliasiStudyRadiologi(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        setTitle("Rekonsiliasi Study PACS (Orthanc) — Satu Sehat");
        setUndecorated(true);
        setSize(960, 480);
        setLocationRelativeTo(parent);

        InternalFrame internalFrame = new InternalFrame();
        internalFrame.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(240, 245, 235)),
                "::[ Rekonsiliasi & Pencocokan Study PACS Orthanc ]::",
                javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                javax.swing.border.TitledBorder.DEFAULT_POSITION,
                new Font("Tahoma", 0, 11),
                new Color(50, 50, 50)
        ));
        internalFrame.setLayout(new BorderLayout(1, 1));

        // ---------------------------------------------------------------------
        // 1. Header Card (SIMRS Request Information)
        // ---------------------------------------------------------------------
        PanelBiasa headerPanel = new PanelBiasa();
        headerPanel.setLayout(new GridLayout(3, 2, 8, 4));
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(6, 10, 6, 10),
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(220, 225, 215)),
                        " Detail Permintaan SIMRS (Target) ",
                        javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                        javax.swing.border.TitledBorder.DEFAULT_POSITION,
                        new Font("Tahoma", Font.BOLD, 10),
                        new Color(70, 70, 70)
                )
        ));
        headerPanel.setPreferredSize(new Dimension(940, 105));

        lblPatient = new Label();
        lblPatient.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblPatient.setText("Pasien : -");

        lblOrder = new Label();
        lblOrder.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblOrder.setText("Permintaan : -");

        lblTime = new Label();
        lblTime.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblTime.setText("Tgl/Jam Permintaan : -");

        lblDoc = new Label();
        lblDoc.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblDoc.setText("Dokter Perujuk : -");

        lblDiag = new Label();
        lblDiag.setFont(new Font("Tahoma", Font.PLAIN, 11));
        lblDiag.setText("Diagnosa Klinis : -");

        Label lblHelp = new Label();
        lblHelp.setFont(new Font("Tahoma", Font.ITALIC, 10));
        lblHelp.setForeground(new Color(120, 120, 120));
        lblHelp.setText("★ = Sangat Cocok  • = Cukup Cocok  (Pilih study PACS yang sesuai)");

        headerPanel.add(lblPatient);
        headerPanel.add(lblOrder);
        headerPanel.add(lblTime);
        headerPanel.add(lblDoc);
        headerPanel.add(lblDiag);
        headerPanel.add(lblHelp);

        internalFrame.add(headerPanel, BorderLayout.NORTH);

        // ---------------------------------------------------------------------
        // 2. Center Table (Orthanc PACS Candidates)
        // ---------------------------------------------------------------------
        Object[] columnNames = {
            "Kecocokan", "Modality", "Tgl & Jam PACS", "Deskripsi Study (PACS)",
            "Accession Number", "Series", "Orthanc Study ID"
        };

        tabMode = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tbCandidates = new Table();
        tbCandidates.setModel(tabMode);
        tbCandidates.setPreferredScrollableViewportSize(new Dimension(920, 240));
        tbCandidates.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tbCandidates.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tbCandidates.setDefaultRenderer(Object.class, new WarnaTable());

        int[] colWidths = {130, 70, 130, 260, 130, 60, 180};
        for (int i = 0; i < colWidths.length; i++) {
            TableColumn col = tbCandidates.getColumnModel().getColumn(i);
            col.setPreferredWidth(colWidths[i]);
        }

        tbCandidates.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tbCandidates.getSelectedRow() != -1) {
                    confirmSelection();
                }
            }
        });

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setViewportView(tbCandidates);
        internalFrame.add(scrollPane, BorderLayout.CENTER);

        // ---------------------------------------------------------------------
        // 3. Bottom Action Bar
        // ---------------------------------------------------------------------
        panelisi bottomPanel = new panelisi();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 6, 8));
        bottomPanel.setPreferredSize(new Dimension(940, 44));

        BtnPilih = new Button();
        BtnPilih.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/accept.png")));
        BtnPilih.setMnemonic('P');
        BtnPilih.setText("Pilih Study Ini");
        BtnPilih.setToolTipText("Alt+P / Enter");
        BtnPilih.setPreferredSize(new Dimension(140, 26));
        BtnPilih.addActionListener((ActionEvent e) -> confirmSelection());

        BtnViewer = new Button();
        BtnViewer.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Search-16x16.png")));
        BtnViewer.setMnemonic('V');
        BtnViewer.setText("Buka di Viewer");
        BtnViewer.setToolTipText("Alt+V");
        BtnViewer.setPreferredSize(new Dimension(135, 26));
        BtnViewer.addActionListener((ActionEvent e) -> openInViewer());

        BtnKeluar = new Button();
        BtnKeluar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/exit.png")));
        BtnKeluar.setMnemonic('K');
        BtnKeluar.setText("Lewati / Batal");
        BtnKeluar.setToolTipText("Alt+K / Esc");
        BtnKeluar.setPreferredSize(new Dimension(130, 26));
        BtnKeluar.addActionListener((ActionEvent e) -> cancelSelection());

        bottomPanel.add(BtnPilih);
        bottomPanel.add(BtnViewer);
        bottomPanel.add(BtnKeluar);

        internalFrame.add(bottomPanel, BorderLayout.SOUTH);
        getContentPane().add(internalFrame, BorderLayout.CENTER);

        // ---------------------------------------------------------------------
        // Keyboard Shortcuts
        // ---------------------------------------------------------------------
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE"
        );
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cancelSelection();
            }
        });

        tbCandidates.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "ENTER"
        );
        tbCandidates.getActionMap().put("ENTER", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                confirmSelection();
            }
        });
    }

    /**
     * Sets the SIMRS order request context for header card.
     */
    public void setRequestContext(String noRM, String namaPasien, String jk, String tglLahir,
                                  String noorder, String nmPerawatan, String expectedModality,
                                  String tglPermintaan, String jamPermintaan,
                                  String nmDokter, String nmPoli, String diagnosa) {
        lblPatient.setText("<html><b>Pasien:</b> " + noRM + " - " + namaPasien
                + " (" + (jk == null || jk.isEmpty() ? "-" : jk) + ", " + tglLahir + ")</html>");
        lblOrder.setText("<html><b>Permintaan:</b> " + nmPerawatan
                + " [Modality: " + expectedModality + "] (Order: " + noorder + ")</html>");
        lblTime.setText("<html><b>Tgl/Jam:</b> " + tglPermintaan + " " + jamPermintaan
                + " (" + (nmPoli == null || nmPoli.isEmpty() ? "Radiologi" : nmPoli) + ")</html>");
        lblDoc.setText("<html><b>Dokter Perujuk:</b> " + (nmDokter == null || nmDokter.isEmpty() ? "-" : nmDokter) + "</html>");
        lblDiag.setText("<html><b>Diagnosa:</b> " + (diagnosa == null || diagnosa.isEmpty() ? "-" : diagnosa) + "</html>");
    }

    /**
     * Populates the table with scored candidate studies from Orthanc.
     */
    public void setCandidates(List<CandidateItem> candidates) {
        this.candidateList.clear();
        this.candidateList.addAll(candidates);
        this.selectedStudyId = "";

        while (tabMode.getRowCount() > 0) {
            tabMode.removeRow(0);
        }

        for (CandidateItem item : candidateList) {
            tabMode.addRow(new Object[]{
                item.matchLabel,
                item.modality,
                item.studyDate + " " + item.studyTime,
                item.studyDesc.isEmpty() ? "(Tanpa Deskripsi)" : item.studyDesc,
                item.studyAcsn.isEmpty() ? "-" : item.studyAcsn,
                item.seriesCount,
                item.studyId
            });
        }

        if (tbCandidates.getRowCount() > 0) {
            tbCandidates.setRowSelectionInterval(0, 0);
            tbCandidates.requestFocus();
        }
    }

    private void confirmSelection() {
        int row = tbCandidates.getSelectedRow();
        if (row != -1 && row < candidateList.size()) {
            this.selectedStudyId = candidateList.get(row).studyId;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Silahkan pilih salah satu study PACS pada tabel!",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openInViewer() {
        int row = tbCandidates.getSelectedRow();
        if (row == -1 || row >= candidateList.size()) {
            JOptionPane.showMessageDialog(this, "Silahkan pilih study PACS terlebih dahulu untuk melihat preview!",
                    "Informasi", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String studyId = candidateList.get(row).studyId;
        String viewerUrl = orthanc.getOrthancStudyWebUrl(studyId);
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(viewerUrl));
            } else {
                JOptionPane.showMessageDialog(this, "Browser viewer URL:\n" + viewerUrl,
                        "Viewer URL", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            System.out.println("Gagal membuka web viewer: " + ex);
            JOptionPane.showMessageDialog(this, "Gagal membuka web viewer: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cancelSelection() {
        this.selectedStudyId = "";
        dispose();
    }

    /**
     * Returns the selected Orthanc Study ID or empty string if cancelled.
     */
    public String getSelectedStudyId() {
        return selectedStudyId;
    }
}
