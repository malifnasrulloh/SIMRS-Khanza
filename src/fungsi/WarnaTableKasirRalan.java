/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fungsi;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 *
 * @author Owner
 */
public class WarnaTableKasirRalan extends DefaultTableCellRenderer {
    private int statusCol = 10;
    private int statusBayarCol = 15;

    public WarnaTableKasirRalan() {
    }

    public WarnaTableKasirRalan(int statusCol, int statusBayarCol) {
        this.statusCol = statusCol;
        this.statusBayarCol = statusBayarCol;
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (row % 2 == 1){
            component.setBackground(new Color(255,244,244));
            component.setForeground(new Color(50,50,50));
        }else{
            component.setBackground(new Color(255,255,255));
            component.setForeground(new Color(50,50,50));
        } 
        Object statusValue = table.getValueAt(row, statusCol);
        if (statusValue != null) {
            switch (statusValue.toString()) {
                case "Belum":
                    component.setBackground(new Color(173, 216, 230));
                    component.setForeground(new Color(50, 50, 50));
                    break;
                case "Sudah":
                    component.setBackground(new Color(200, 0, 0));
                    component.setForeground(new Color(255, 230, 230));
                    break;
                case "Batal":
                    component.setBackground(new Color(255, 243, 109));
                    component.setForeground(new Color(120, 110, 50));
                    break;
                case "Berkas Diterima":
                    component.setBackground(new Color(64, 224, 208));
                    component.setForeground(new Color(255, 255, 255));
                    break;
                case "Dirujuk":
                case "Meninggal":
                case "Pulang Paksa":
                    component.setBackground(new Color(152, 152, 156));
                    component.setForeground(new Color(245, 245, 255));
                    break;
                case "Dirawat":
                    component.setBackground(new Color(119, 221, 119));
                    component.setForeground(new Color(245, 255, 245));
                    break;
            }
        }
        
        if (statusBayarCol != -1) {
            Object statusBayarValue = table.getValueAt(row, statusBayarCol);
            if (statusBayarValue != null && statusBayarValue.toString().equals("Sudah Bayar")) {
                component.setBackground(new Color(50, 50, 50));
                component.setForeground(new Color(255, 255, 255));
            }
        }
        return component;
    }

}

