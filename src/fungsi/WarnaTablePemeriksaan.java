package fungsi;

import java.awt.Color;
import java.awt.Component;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class WarnaTablePemeriksaan extends DefaultTableCellRenderer {
    private static Set<String> doctorIds = null;
    private int nipColumnIndex;

    public WarnaTablePemeriksaan(int nipColumnIndex) {
        this.nipColumnIndex = nipColumnIndex;
        if (doctorIds == null) {
            loadDoctorIds();
        }
    }

    private synchronized void loadDoctorIds() {
        if (doctorIds != null) return;
        doctorIds = new HashSet<>();
        try {
            Connection koneksi = koneksiDB.condb();
            PreparedStatement ps = koneksi.prepareStatement("select kd_dokter from dokter");
            ResultSet rs = null;
            try {
                rs = ps.executeQuery();
                while (rs.next()) {
                    doctorIds.add(rs.getString(1));
                }
            } finally {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        
        Object nipValue = table.getValueAt(row, nipColumnIndex);
        boolean isDoctor = false;
        if (nipValue != null && doctorIds.contains(nipValue.toString())) {
            isDoctor = true;
        }

        if (isDoctor) {
            component.setBackground(new Color(175, 255, 175));
        } else {
            component.setBackground(new Color(255, 255, 180));
        }

        component.setForeground(new Color(50, 50, 50));
        return component;
    }
}