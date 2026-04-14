package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ValidasiCuti {

    private final Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private String pesanError = "";

    public ValidasiCuti() {
    }

    /**
     * Checks if the requested leave dates intersect with any active blackout
     * dates.
     *
     * @param tglAwal Requested start date (yyyy-MM-dd)
     * @param tglAkhir Requested end date (yyyy-MM-dd)
     * @return true if dates are valid (no blackout dates), false otherwise.
     */
    public boolean cekValidasi(String tglAwal, String tglAkhir) {
        pesanError = "";
        try {
            ps = koneksi.prepareStatement(
                    "SELECT event_name, "
                    + "DATE_ADD(tanggal_akhir, INTERVAL 1 DAY) as tanggal_awal_buffer, "
                    + "DATE_ADD(tanggal_akhir, INTERVAL buffer_days_after DAY) as tanggal_akhir_buffer "
                    + "FROM set_cuti "
                    + "WHERE status = 'Active' and buffer_days_after > 0 "
                    + "AND ? <= DATE_ADD(tanggal_akhir, INTERVAL buffer_days_after DAY) "
                    + "AND ? >= DATE_ADD(tanggal_akhir, INTERVAL 1 DAY) limit 1"
            );
            try {
                ps.setString(1, tglAwal);
                ps.setString(2, tglAkhir);
                rs = ps.executeQuery();
                if (rs.next()) {
                    String eventName = rs.getString("event_name");
                    String startBuffer = rs.getString("tanggal_awal_buffer");
                    String endBuffer = rs.getString("tanggal_akhir_buffer");
                    pesanError = "Pengajuan gagal: Anda tidak dapat mengambil cuti pada masa buffer (" + convertDate(startBuffer) + " s/d " + convertDate(endBuffer) + ") untuk periode setelah " + eventName + ".";
                    return false;
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notif Validasi Cuti: " + e);
        }
        return true;
    }

    private String convertDate(String strDate) {
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate date = LocalDate.parse(strDate, inputFormatter);

        return date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.of("id", "ID")));
    }

    public String getPesanError() {
        return pesanError;
    }
}
