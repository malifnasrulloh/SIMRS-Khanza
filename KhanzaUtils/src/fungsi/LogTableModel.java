/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fungsi;

import fungsi.logger.FileLogger;
import fungsi.logger.LogType;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author malifnasrulloh
 */
public class LogTableModel extends AbstractTableModel {

    private FileLogger logger;
    private final String[] columnNames = {"Tanggal", "Log"};
    private final ArrayList<Object[]> logsData = new ArrayList<Object[]>();
    private Path logPath;
    private String prefixLogName;

    public LogTableModel(Path logPath, String prefixLogName) {
        this.logPath = logPath;
        this.prefixLogName = prefixLogName;
        logger = new FileLogger(this.logPath, this.prefixLogName);
    }

    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    @Override
    public int getRowCount() {
        return this.logsData.size();
    }

    @Override
    public String getColumnName(int col) {
        return this.columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= logsData.size() || col < 0 || col >= columnNames.length) {
            return null;
        }
        return logsData.get(row)[col];
    }

    /*
        * JTable uses this method to determine the default renderer/
        * editor for each cell. If we didn't implement this method,
        * then the last column would contain text ("true"/"false"),
        * rather than a check box.
     */
    @Override
    public Class<?> getColumnClass(int col) {
        if (logsData.isEmpty()) {
            return Object.class;
        }
        Object value = getValueAt(0, col);
        return (value != null) ? value.getClass() : Object.class;
    }

    /*
        * Don't need to implement this method unless your table's
        * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the logsData/cell address is constant,
        //no matter where the cell appears onscreen.
        return false;
//            if (col < 2) {
//                return false;
//            } else {
//                return true;
//            }
    }

    public void tambahData(Object log, LogType type) {
        String waktu = LocalDateTime.now(ZoneId.of("Asia/Jakarta")).format(logger.getDateTimeFormatter());
        String pesan = (log != null) ? log.toString().replace("\n", " ") : "null";
        Object[] row = {waktu, pesan, type};
        logsData.add(row);
        fireTableDataChanged();
        logger.log(log, type);
    }

    public void tambahData(Object log) {
        tambahData(log, LogType.LOG);
    }

    public void resetData() {
        this.logsData.clear();
        fireTableDataChanged();
        logger = new FileLogger(this.logPath, this.prefixLogName);
    }

    /*
        * Don't need to implement this method unless your table's
        * logsData can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {
        System.out.println("Setting value at " + row + "," + col
                + " to " + value
                + " (an instance of "
                + value.getClass() + ")");

        this.logsData.get(row)[col] = value;
        fireTableCellUpdated(row, col);
    }
}
