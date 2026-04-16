package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class ValidasiCuti {

    private final Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps;
    private ResultSet rs;
    private String pesanError = "";

    public ValidasiCuti() {
    }

    /**
     * Checks if the requested leave dates intersect with any individual
     * blackout dates.
     *
     * @param tglAwal Requested start date (yyyy-MM-dd)
     * @param tglAkhir Requested end date (yyyy-MM-dd)
     * @return true if dates are valid (no blackout dates), false otherwise.
     */
    public boolean cekValidasi(String tglAwal, String tglAkhir) {
        pesanError = "";
        try {
            ps = koneksi.prepareStatement(
                    "SELECT tanggal FROM pembatasan_cuti "
                    + "WHERE tanggal between ? and ? limit 1"
            );
            try {
                ps.setString(1, tglAwal);
                ps.setString(2, tglAkhir);
                rs = ps.executeQuery();
                if (rs.next()) {
                    String tglBlackout = rs.getString("tanggal");
                    pesanError = "Pengajuan gagal: Anda tidak dapat mengambil cuti pada tanggal (" + convertDate(tglBlackout) + ") karena dibatasi oleh manajemen.";
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

    /**
     * Checks if the submission meets the buffer requirement and semester quota.
     */
    public boolean cekValidasiPengajuan(String tglPengajuan, String tglAwal, int jumlahCuti, String nik, String noPengajuan) {
        pesanError = "";
        int maksPengajuan = 0;
        int maksJatahSemester = 0;

        try {
            ps = koneksi.prepareStatement("SELECT maks_pengajuan, maks_jatah_semester FROM set_pengaturan_cuti LIMIT 1");
            rs = ps.executeQuery();
            if (rs.next()) {
                maksPengajuan = rs.getInt("maks_pengajuan");
                maksJatahSemester = rs.getInt("maks_jatah_semester");
            } else {
                return true;
            }
            rs.close();
            ps.close();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate pengajuan = LocalDate.parse(tglPengajuan, formatter);
            LocalDate awal = LocalDate.parse(tglAwal, formatter);
            long diffDays = ChronoUnit.DAYS.between(pengajuan, awal);

            if (diffDays < maksPengajuan) {
                pesanError = "Pengajuan gagal: Cuti harus diajukan minimal H-" + maksPengajuan + " hari sebelum pelaksanaan.";
                return false;
            }

            int bulan = awal.getMonthValue();
            int tahun = awal.getYear();
            boolean semesterPertama = (bulan <= 6);

            String queryQuota;

            if (semesterPertama) {
                queryQuota = "SELECT sum(jumlah) FROM pengajuan_cuti WHERE nik=? AND year(tanggal_awal)=? AND month(tanggal_awal) BETWEEN 1 AND 6 AND status<>'Ditolak' AND status_manajemen<>'Ditolak'";
            } else {
                queryQuota = "SELECT sum(jumlah) FROM pengajuan_cuti WHERE nik=? AND year(tanggal_awal)=? AND month(tanggal_awal) BETWEEN 7 AND 12 AND status<>'Ditolak' AND status_manajemen<>'Ditolak'";
            }

            if (noPengajuan != null && !noPengajuan.trim().isEmpty()) {
                queryQuota += " AND no_pengajuan<>?";
            }

            ps = koneksi.prepareStatement(queryQuota);
            ps.setString(1, nik);
            ps.setInt(2, tahun);
            if (noPengajuan != null && !noPengajuan.trim().isEmpty()) {
                ps.setString(3, noPengajuan);
            }
            rs = ps.executeQuery();
            int jatahTerpakai = 0;
            if (rs.next()) {
                jatahTerpakai = rs.getInt(1);
            }
            rs.close();
            ps.close();

            if (jatahTerpakai + jumlahCuti > maksJatahSemester) {
                pesanError = "Pengajuan gagal: Jatah cuti semester " + (semesterPertama ? "1 " : "2 ") + String.valueOf(tahun) + " (" + maksJatahSemester + " hari) tidak mencukupi (Sisa: " + (maksJatahSemester - jatahTerpakai) + " hari).";
                return false;
            }

        } catch (Exception e) {
            System.out.println("Notif Validasi Pengajuan: " + e);
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
