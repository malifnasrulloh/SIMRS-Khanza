package widget;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import fungsi.ExcelUtil;

/**
 *
 * @author usu
 */
public class Table extends JTable {

    /*
     * Serial version UID
     */
    private static final long serialVersionUID = 1L;
    private final JPopupMenu exportMenu = new JPopupMenu();
    private final JMenuItem exportItem = new JMenuItem("Export ke Excel");

    public Table() {
        super();
        //setBackground(new Color(255,235,255));
        //setGridColor(new Color(245,170,245));
        //setForeground(new Color(90,90,90));
        setBackground(new Color(255,255,255));
        setGridColor(new Color(226,231,221));
        setForeground(new Color(50,50,50));
        setFont(new java.awt.Font("Tahoma", 0, 11));
        setRowHeight(22);
        setSelectionBackground(new Color(255,255,255));
        setSelectionForeground(new Color(255,0,0));
        getTableHeader().setForeground(new Color(50,50,50));
        getTableHeader().setBackground(new Color(255,250,250));
        getTableHeader().setBorder(javax.swing.BorderFactory.createLineBorder(new Color(255,250,250)));
        getTableHeader().setFont(new java.awt.Font("Tahoma", 0, 11));
        exportItem.setFont(new java.awt.Font("Tahoma", 0, 11));
        exportItem.setForeground(new java.awt.Color(50, 50, 50));
        exportItem.setIcon(new javax.swing.ImageIcon(getClass().getResource("/picture/Export16.png")));
        exportItem.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        exportItem.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        exportItem.setPreferredSize(new java.awt.Dimension(200, 28));
        exportItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExcelUtil.export(Table.this);
            }
        });
        exportMenu.add(exportItem);
        MouseAdapter popupListener = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handlePopupEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handlePopupEvent(e);
            }

            private void handlePopupEvent(MouseEvent e) {
                JPopupMenu formMenu = getComponentPopupMenu();
                if (formMenu != null) {
                    attachExportItem(formMenu);
                    return;
                }
                if (e.isPopupTrigger()) {
                    exportMenu.show(Table.this, e.getX(), e.getY());
                }
            }
        };
        addMouseListener(popupListener);
        getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleHeaderEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                handleHeaderEvent(e);
            }

            private void handleHeaderEvent(MouseEvent e) {
                JPopupMenu formMenu = getTableHeader().getComponentPopupMenu();
                if (formMenu == null) {
                    formMenu = getComponentPopupMenu();
                }
                if (formMenu != null) {
                    attachExportItem(formMenu);
                    return;
                }
                if (e.isPopupTrigger()) {
                    exportMenu.show(getTableHeader(), e.getX(), e.getY());
                }
            }
        });
    }

    private void attachExportItem(JPopupMenu formMenu) {
        if (exportItem.getParent() != formMenu) {
            if (exportItem.getParent() != null) {
                exportItem.getParent().remove(exportItem);
            }
            formMenu.add(exportItem);
        }
    }
}
