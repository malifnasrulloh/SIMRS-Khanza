/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fungsi;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author malifnasrulloh
 */
public class LogTableModel extends AbstractTableModel {

    private boolean use_as_table_model;
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy HH-mm-ss.SSSSSS");
    private final Path logPath;
    private String logName, prefixLogName;
            
    public LogTableModel(Path logPath, String prefixLogName, boolean use_as_table_model) {
        this.use_as_table_model = use_as_table_model;
        this.logPath = logPath;
        this.prefixLogName = prefixLogName;
        this.logName = prefixLogName + "-" + LocalDateTime.now(ZoneId.of("Asia/Jakarta")).format(formatter) + ".txt";

        try {
            Files.createDirectories(logPath);
        } catch (IOException e) {
            System.err.println("Failed to create directories.: "+ e);
            e.printStackTrace();
        }

    }

    private String[] columnNames = {"Tanggal", "Log",};
    private ArrayList<Object[]> logsData = new ArrayList<Object[]>();

    public int getColumnCount() {
        return this.columnNames.length;
    }

    public int getRowCount() {
        return this.logsData.size();
    }

    public String getColumnName(int col) {
        return this.columnNames[col];
    }

    public Object getValueAt(int row, int col) {
        return this.logsData.get(row)[col];
    }

    /*
        * JTable uses this method to determine the default renderer/
        * editor for each cell. If we didn't implement this method,
        * then the last column would contain text ("true"/"false"),
        * rather than a check box.
     */
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
        * Don't need to implement this method unless your table's
        * editable.
     */
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

    public void tambahData(Object log, String logType) {
        Object[] temp = {LocalDateTime.now(ZoneId.of("Asia/Jakarta")).format(formatter).replace("-", ":"), logType, log.toString().replaceAll("\n", "")};
        this.logsData.add(temp);
        if (this.use_as_table_model) {
            fireTableDataChanged();
        }
        exportData();
    }

    public void exportData() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(this.logPath + "/" + this.logName))) {
            for (Object[] i : this.logsData) {
                bw.write(String.format("[%s] [%s] %s\n", i[0], i[1], i[2]));
            }
        } catch (Exception e) {
            System.out.println("Error export log");
            e.printStackTrace();
        }
    }

    public void resetData() {
        this.logsData.clear();
        if (this.use_as_table_model) {
            fireTableDataChanged();
        }
        this.logName = this.prefixLogName + "-" + LocalDateTime.now(ZoneId.of("Asia/Jakarta")).format(formatter) + ".txt";;
    }

    /*
        * Don't need to implement this method unless your table's
        * logsData can change.
     */
    public void setValueAt(Object value, int row, int col) {

        System.out.println("Setting value at " + row + "," + col
                + " to " + value
                + " (an instance of "
                + value.getClass() + ")");

        this.logsData.get(row)[col] = value;
        if (this.use_as_table_model) {
            fireTableCellUpdated(row, col);
        }

        System.out.println("New value of data:");
        printDebugData();

    }

    private void printDebugData() {
        int numRows = getRowCount();
        int numCols = getColumnCount();

        for (int i = 0; i < numRows; i++) {
            System.out.print(" row " + i + ":");
            for (int j = 0; j < numCols; j++) {
                System.out.print(" " + this.getValueAt(i, j));
            }
            System.out.println();
        }
        System.out.println("--------------------------");
    }
}
