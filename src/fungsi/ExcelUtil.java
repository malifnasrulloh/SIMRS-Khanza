package fungsi;

import java.awt.Component;
import java.awt.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.TableModel;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public final class ExcelUtil {

    private static final int MAX_XLS_ROWS = 65535;
    private static final int MAX_XLS_COLS = 255;
    private static final int MAX_NAME = 40;
    private static final int MAX_WIDTH = 60;
    private static final long SAFE_LONG = 9007199254740992L;

    private ExcelUtil() {
    }

    public static void writeToFile(TableModel model, File file, String sheetName) throws IOException {
        boolean xlsx = file.getName().toLowerCase().endsWith(".xlsx");
        if (!xlsx) {
            if (model.getRowCount() > MAX_XLS_ROWS) {
                throw new IOException("Data terlalu banyak (" + (model.getRowCount() + 1)
                        + " baris) untuk format .xls. Maksimal " + (MAX_XLS_ROWS + 1) + " baris. Pilih format .xlsx...!!!!");
            }
            if (model.getColumnCount() > MAX_XLS_COLS) {
                throw new IOException("Data terlalu banyak kolom (" + model.getColumnCount()
                        + ") untuk format .xls. Maksimal " + (MAX_XLS_COLS + 1) + " kolom. Pilih format .xlsx...!!!!");
            }
        }
        try (Workbook workbook = xlsx ? new XSSFWorkbook() : new HSSFWorkbook();
                FileOutputStream out = new FileOutputStream(file)) {
            Sheet sheet = workbook.createSheet(sanitizeSheetName(sheetName));
            int columnCount = model.getColumnCount();
            int[] widths = new int[columnCount];
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Row headerRow = sheet.createRow(0);
            for (int c = 0; c < columnCount; c++) {
                Cell cell = headerRow.createCell(c);
                String name = model.getColumnName(c) == null ? "" : model.getColumnName(c).toString();
                cell.setCellValue(name);
                cell.setCellStyle(headerStyle);
                widths[c] = Math.min(name.length(), MAX_WIDTH);
            }
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd/MM/yyyy HH:mm"));
            for (int r = 0; r < model.getRowCount(); r++) {
                Row row = sheet.createRow(r + 1);
                for (int c = 0; c < columnCount; c++) {
                    Object value = model.getValueAt(r, c);
                    if (value == null) {
                        continue;
                    }
                    Cell cell = row.createCell(c);
                    if (value instanceof Number) {
                        if (value instanceof Double || value instanceof Float || value instanceof BigDecimal) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else {
                            long l = ((Number) value).longValue();
                            if (Math.abs(l) < SAFE_LONG) {
                                cell.setCellValue((double) l);
                            } else {
                                cell.setCellValue(String.valueOf(l));
                            }
                        }
                    } else if (value instanceof Date) {
                        cell.setCellValue((Date) value);
                        cell.setCellStyle(dateStyle);
                    } else if (value instanceof Boolean) {
                        cell.setCellValue(((Boolean) value));
                    } else {
                        cell.setCellValue(value.toString());
                    }
                    int len = cell.toString().length();
                    if (len > widths[c]) {
                        widths[c] = Math.min(len, MAX_WIDTH);
                    }
                }
            }
            for (int c = 0; c < columnCount; c++) {
                if (widths[c] > 0) {
                    sheet.setColumnWidth(c, (widths[c] + 2) * 256);
                }
            }
            workbook.write(out);
        }
    }

    public static void export(JTable table) {
        TableModel model = table.getModel();
        Window parent = SwingUtilities.getWindowAncestor(table);
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(parent, "Maaf, tidak ada data untuk diexport...!!!!");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter xlsxFilter = new FileNameExtensionFilter("Excel 2007+ (.xlsx)", "xlsx");
        FileNameExtensionFilter xlsFilter = new FileNameExtensionFilter("Excel 97-2003 (.xls)", "xls");
        chooser.addChoosableFileFilter(xlsFilter);
        chooser.setFileFilter(xlsxFilter);
        chooser.setSelectedFile(new File(defaultFileName(table) + ".xlsx"));
        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        String name = file.getName().toLowerCase();
        if (!name.endsWith(".xlsx") && !name.endsWith(".xls")) {
            String ext = chooser.getFileFilter() == xlsFilter ? ".xls" : ".xlsx";
            file = new File(file.getParentFile(), file.getName() + ext);
        }
        try {
            writeToFile(model, file, defaultFileName(table));
            JOptionPane.showMessageDialog(parent, "Data berhasil diexport ke:\n" + file.getPath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parent, "Gagal mengexport data...!!!!\n" + ex.getMessage());
        }
    }

    private static String defaultFileName(JTable table) {
        Component c = table;
        while (c != null) {
            if (c instanceof JComponent) {
                String title = findTitledBorderTitle(((JComponent) c).getBorder());
                if (title != null) {
                    return sanitizeFileName(title);
                }
            }
            c = c.getParent();
        }
        Component top = table.getTopLevelAncestor();
        String name = top == null ? "" : top.getClass().getSimpleName();
        return sanitizeFileName(name.length() > 0 ? name : "export");
    }

    private static String findTitledBorderTitle(javax.swing.border.Border border) {
        if (border instanceof TitledBorder) {
            String title = ((TitledBorder) border).getTitle();
            return title == null || title.trim().isEmpty() ? null : title.trim();
        }
        if (border instanceof CompoundBorder) {
            CompoundBorder compound = (CompoundBorder) border;
            String title = findTitledBorderTitle(compound.getOutsideBorder());
            if (title == null) {
                title = findTitledBorderTitle(compound.getInsideBorder());
            }
            return title;
        }
        return null;
    }

    private static String sanitizeFileName(String name) {
        String clean = name.replaceAll("[<>:\"/\\\\|?*\\[\\]]", " ").replaceAll("\\s+", " ").trim();
        if (clean.isEmpty()) {
            clean = "export";
        }
        return clean.length() > MAX_NAME ? clean.substring(0, MAX_NAME) : clean;
    }

    private static String sanitizeSheetName(String name) {
        String clean = name.replaceAll("[\\[\\]:*?/\\\\]", " ").replaceAll("\\s+", " ").trim();
        if (clean.isEmpty()) {
            clean = "Data";
        }
        return clean.length() > 31 ? clean.substring(0, 31) : clean;
    }
}
