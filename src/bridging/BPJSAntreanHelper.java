package bridging;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

/**
 * Helper class for BPJS Antrean API v2 integration in SIMRS Khanza.
 * Ensures unified nobooking generation (YYYYMMDD00000X matching index.php),
 * polyclinic/doctor BPJS code mapping, and standardized JSON payloads.
 */
public class BPJSAntreanHelper {

    /**
     * Fetch existing nobooking from referensi_mobilejkn_bpjs, or generate a unified MAX+1
     * sequence matching index.php (YYYYMMDD + 6-digit sequence).
     */
    public static String fetchOrGenerateNobooking(Connection conn, String noRawat, String tglPeriksa) {
        if (noRawat == null || noRawat.trim().isEmpty()) {
            return "";
        }

        // 1. Check existing referensi_mobilejkn_bpjs record
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT nobooking FROM referensi_mobilejkn_bpjs WHERE no_rawat = ? LIMIT 1");
            ps.setString(1, noRawat);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String existing = rs.getString("nobooking");
                rs.close();
                ps.close();
                if (existing != null && !existing.trim().isEmpty()) {
                    return existing;
                }
            } else {
                rs.close();
                ps.close();
            }
        } catch (Exception e) {
            System.out.println("BPJSAntreanHelper Error checking nobooking: " + e);
        }

        // 2. Compute MAX(nobooking) + 1 matching index.php (lines 523–524)
        if (tglPeriksa == null || tglPeriksa.trim().isEmpty()) {
            tglPeriksa = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
        }

        int maxNum = 1;
        try {
            PreparedStatement psMax = conn.prepareStatement(
                "SELECT IFNULL(MAX(CONVERT(RIGHT(nobooking, 6), SIGNED)), 0) + 1 AS maxb FROM referensi_mobilejkn_bpjs WHERE tanggalperiksa = ?"
            );
            psMax.setString(1, tglPeriksa);
            ResultSet rsMax = psMax.executeQuery();
            if (rsMax.next()) {
                maxNum = rsMax.getInt("maxb");
            }
            rsMax.close();
            psMax.close();
        } catch (Exception e) {
            System.out.println("BPJSAntreanHelper Error computing max nobooking: " + e);
        }

        String dateStr = tglPeriksa.replace("-", "");
        return dateStr + String.format("%06d", maxNum);
    }

    /**
     * Get mapped BPJS polyclinic code from maping_poli_bpjs.
     */
    public static String getBpjsPoliCode(Connection conn, String kdPoliRs) {
        if (kdPoliRs == null || kdPoliRs.trim().isEmpty()) return "";
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT kd_poli_bpjs FROM maping_poli_bpjs WHERE kd_poli_rs = ? LIMIT 1");
            ps.setString(1, kdPoliRs);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String code = rs.getString("kd_poli_bpjs");
                rs.close(); ps.close();
                if (code != null && !code.trim().isEmpty()) return code;
            } else {
                rs.close(); ps.close();
            }
        } catch (Exception e) {
            System.out.println("BPJSAntreanHelper Error getBpjsPoliCode: " + e);
        }
        return kdPoliRs;
    }

    /**
     * Get mapped BPJS polyclinic name from maping_poli_bpjs.
     */
    public static String getBpjsPoliName(Connection conn, String kdPoliRs) {
        if (kdPoliRs == null || kdPoliRs.trim().isEmpty()) return "";
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT nm_poli_bpjs FROM maping_poli_bpjs WHERE kd_poli_rs = ? LIMIT 1");
            ps.setString(1, kdPoliRs);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String name = rs.getString("nm_poli_bpjs");
                rs.close(); ps.close();
                if (name != null && !name.trim().isEmpty()) return name;
            } else {
                rs.close(); ps.close();
            }
        } catch (Exception e) {
            System.out.println("BPJSAntreanHelper Error getBpjsPoliName: " + e);
        }
        return "";
    }

    /**
     * Get mapped BPJS doctor DPJP code from maping_dokter_dpjpvclaim.
     */
    public static String getBpjsDokterCode(Connection conn, String kdDokterRs) {
        if (kdDokterRs == null || kdDokterRs.trim().isEmpty()) return "";
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT kd_dokter_bpjs FROM maping_dokter_dpjpvclaim WHERE kd_dokter = ? LIMIT 1");
            ps.setString(1, kdDokterRs);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String code = rs.getString("kd_dokter_bpjs");
                rs.close(); ps.close();
                if (code != null && !code.trim().isEmpty()) return code;
            } else {
                rs.close(); ps.close();
            }
        } catch (Exception e) {
            System.out.println("BPJSAntreanHelper Error getBpjsDokterCode: " + e);
        }
        return kdDokterRs;
    }

    /**
     * Save/sync booking record to referensi_mobilejkn_bpjs table.
     */
    public static boolean saveToReferensiMobileJKN(
        Connection conn,
        String nobooking,
        String noRawat,
        String noKartu,
        String nik,
        String noHp,
        String kdPoliBpjs,
        String pasienBaru,
        String noRm,
        String tglPeriksa,
        String kdDokterBpjs,
        String jamPraktek,
        String nomorReferensi,
        String noReg,
        int kuota
    ) {
        try {
            int regNum = 1;
            try { regNum = Integer.parseInt(noReg); } catch (Exception ignored) {}

            String sql = "INSERT IGNORE INTO referensi_mobilejkn_bpjs " +
                "(nobooking, no_rawat, nomorkartu, nik, nohp, kodepoli, pasienbaru, norm, tanggalperiksa, kodedokter, jampraktek, jeniskunjungan, nomorreferensi, nomorantrean, angkaantrean, estimasidilayani, sisakuotajkn, kuotajkn, sisakuotanonjkn, kuotanonjkn, status, validasi, statuskirim) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, '3 (Kontrol)', ?, ?, ?, ?, ?, ?, ?, ?, 'Checkin', NOW(), 'Sudah')";

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nobooking);
            ps.setString(2, noRawat);
            ps.setString(3, noKartu != null && !noKartu.trim().isEmpty() ? noKartu : "-");
            ps.setString(4, nik != null && !nik.trim().isEmpty() ? nik : "-");
            ps.setString(5, noHp != null && !noHp.trim().isEmpty() ? noHp : "-");
            ps.setString(6, kdPoliBpjs);
            ps.setString(7, pasienBaru);
            ps.setString(8, noRm);
            ps.setString(9, tglPeriksa);
            ps.setString(10, kdDokterBpjs);
            ps.setString(11, jamPraktek);
            ps.setString(12, nomorReferensi != null ? nomorReferensi : "");
            ps.setString(13, kdPoliBpjs + "-" + noReg);
            ps.setInt(14, regNum);
            ps.setLong(15, System.currentTimeMillis());
            ps.setInt(16, Math.max(0, kuota - regNum));
            ps.setInt(17, kuota);
            ps.setInt(18, Math.max(0, kuota - regNum));
            ps.setInt(19, kuota);

            int result = ps.executeUpdate();
            ps.close();
            return result > 0;
        } catch (Exception e) {
            System.out.println("BPJSAntreanHelper Error saveToReferensiMobileJKN: " + e);
            return false;
        }
    }
}
