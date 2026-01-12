package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

/**
 *
 * @author malifnasrulloh
 */
public final class akses {

    private static final Connection koneksi = koneksiDB.condb();
    private static PreparedStatement ps, ps2;
    private static ResultSet rs, rs2;

    private static final HashMap<String, Boolean> dataAccess = new HashMap<>();

    private static String kode = "", kdbangsal = "", alamatip = "", namars = "", alamatrs = "", kabupatenrs = "", propinsirs = "", kontakrs = "", emailrs = "", form = "", namauser = "", kode_ppk = "";
    private static int jml1 = 0, jml2 = 0, lebar = 0, tinggi = 0;
    private static boolean admin = false, aktif = false, aplikasi = false, bulanan_klasifikasi_pasien_ranap = false, e_eksekutif = false, obat10_terbanyak_poli = false, status = false, user = false, vakum = false;

    public static void getInitialPermission() {
        try {
            ps2 = koneksi.prepareStatement("select c.COLUMN_NAME from information_schema.`COLUMNS` c where c.TABLE_NAME = 'user' and c.TABLE_SCHEMA = 'sik' and c.DATA_TYPE = 'enum'");
            rs2 = ps2.executeQuery();
            while (rs2.next()) {
                dataAccess.put(rs2.getString(1), false);
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }
    }

    public static void setData(String user, String pass) {
        try {
            ps = koneksi.prepareStatement("select * from admin where admin.usere=AES_ENCRYPT(?,'nur') and admin.passworde=AES_ENCRYPT(?,'windi')");
            ps2 = koneksi.prepareStatement("select * from user where user.id_user=AES_ENCRYPT(?,'nur') and user.password=AES_ENCRYPT(?,'windi')");
            try {
                ps.setString(1, user);
                ps.setString(2, pass);
                rs = ps.executeQuery();
                rs.last();

                ps2.setString(1, user);
                ps2.setString(2, pass);
                rs2 = ps2.executeQuery();
                rs2.last();

                akses.setJml1(rs.getRow());
                akses.setJml2(rs2.getRow());
                if (rs.getRow() >= 1) {
                    akses.setKode("Admin Utama");
                    for (String tableName : dataAccess.keySet()) {
                        dataAccess.replace(tableName, true);
                    }
                    akses.admin = true;
                    akses.user = true;
                    akses.e_eksekutif = true;
                    akses.vakum = true;
                    akses.aplikasi = true;
                } else if (rs2.getRow() >= 1) {
                    rs2.beforeFirst();
                    rs2.next();
                    akses.setKode(user);

                    for (String tableName : dataAccess.keySet()) {
                        dataAccess.replace(tableName, rs2.getBoolean(tableName));
                    }
                    akses.admin = false;
                    akses.user = false;
                    akses.e_eksekutif = false;
                    akses.vakum = false;
                    akses.aplikasi = false;
                } else if ((rs.getRow() == 0) && (rs2.getRow() == 0)) {
                    setLogOut();
                }
            } catch (Exception e) {
                System.out.println("Notifikasi : " + e);
            } finally {
                if (rs != null) {
                    rs.close();
                }
                if (rs2 != null) {
                    rs2.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (ps2 != null) {
                    ps2.close();
                }
            }
        } catch (Exception e) {
            System.out.println("Notifikasi : " + e);
        }

    }

    public static void setLogOut() {
        setKode("");
        for (String tableName : dataAccess.keySet()) {
            dataAccess.replace(tableName, false);
        }
        akses.admin = false;
        akses.user = false;
        akses.e_eksekutif = false;
        akses.vakum = false;
        akses.aplikasi = false;
    }

    public static boolean getPermission(String tableName) {
        try {
            return dataAccess.get(tableName);
        } catch (Exception e) {
            System.out.println("Notifikasi: Permission pada table tersebut tidak ditemukan.");
        }
        return false;
    }

    public static void setPermissionToFalse(String tableName) {
        try {
            dataAccess.replace(tableName, false);
        } catch (Exception e) {
            System.out.println("Notifikasi: " + e);
        }
    }

    /**
     * @return the kode
     */
    public static String getKode() {
        return kode;
    }

    /**
     * @return the kdbangsal
     */
    public static String getKdbangsal() {
        return kdbangsal;
    }

    /**
     * @return the alamatip
     */
    public static String getAlamatip() {
        return alamatip;
    }

    /**
     * @return the namars
     */
    public static String getNamars() {
        return namars;
    }

    /**
     * @return the alamatrs
     */
    public static String getAlamatrs() {
        return alamatrs;
    }

    /**
     * @return the kabupatenrs
     */
    public static String getKabupatenrs() {
        return kabupatenrs;
    }

    /**
     * @return the propinsirs
     */
    public static String getPropinsirs() {
        return propinsirs;
    }

    /**
     * @return the kontakrs
     */
    public static String getKontakrs() {
        return kontakrs;
    }

    /**
     * @return the emailrs
     */
    public static String getEmailrs() {
        return emailrs;
    }

    /**
     * @return the form
     */
    public static String getForm() {
        return form;
    }

    /**
     * @return the namauser
     */
    public static String getNamauser() {
        return namauser;
    }

    /**
     * @return the kode_ppk
     */
    public static String getKode_ppk() {
        return kode_ppk;
    }

    /**
     * @return the jml1
     */
    public static int getJml1() {
        return jml1;
    }

    /**
     * @return the jml2
     */
    public static int getJml2() {
        return jml2;
    }

    /**
     * @return the lebar
     */
    public static int getLebar() {
        return lebar;
    }

    /**
     * @return the tinggi
     */
    public static int getTinggi() {
        return tinggi;
    }

    /**
     * @return the admin
     */
    public static boolean isAdmin() {
        return admin;
    }

    /**
     * @return the aktif
     */
    public static boolean isAktif() {
        return aktif;
    }

    /**
     * @return the aplikasi
     */
    public static boolean isAplikasi() {
        return aplikasi;
    }

    /**
     * @return the bulanan_klasifikasi_pasien_ranap
     */
    public static boolean isBulanan_klasifikasi_pasien_ranap() {
        return bulanan_klasifikasi_pasien_ranap;
    }

    /**
     * @return the e_eksekutif
     */
    public static boolean isE_eksekutif() {
        return e_eksekutif;
    }

    /**
     * @return the obat10_terbanyak_poli
     */
    public static boolean isObat10_terbanyak_poli() {
        return obat10_terbanyak_poli;
    }

    /**
     * @return the status
     */
    public static boolean isStatus() {
        return status;
    }

    /**
     * @return the user
     */
    public static boolean isUser() {
        return user;
    }

    /**
     * @return the vakum
     */
    public static boolean isVakum() {
        return vakum;
    }

    /**
     * @param aKode the kode to set
     */
    public static void setKode(String aKode) {
        kode = aKode;
    }

    /**
     * @param aKdbangsal the kdbangsal to set
     */
    public static void setKdbangsal(String aKdbangsal) {
        kdbangsal = aKdbangsal;
    }

    /**
     * @param aAlamatip the alamatip to set
     */
    public static void setAlamatip(String aAlamatip) {
        alamatip = aAlamatip;
    }

    /**
     * @param aNamars the namars to set
     */
    public static void setNamars(String aNamars) {
        namars = aNamars;
    }

    /**
     * @param aAlamatrs the alamatrs to set
     */
    public static void setAlamatrs(String aAlamatrs) {
        alamatrs = aAlamatrs;
    }

    /**
     * @param aKabupatenrs the kabupatenrs to set
     */
    public static void setKabupatenrs(String aKabupatenrs) {
        kabupatenrs = aKabupatenrs;
    }

    /**
     * @param aPropinsirs the propinsirs to set
     */
    public static void setPropinsirs(String aPropinsirs) {
        propinsirs = aPropinsirs;
    }

    /**
     * @param aKontakrs the kontakrs to set
     */
    public static void setKontakrs(String aKontakrs) {
        kontakrs = aKontakrs;
    }

    /**
     * @param aEmailrs the emailrs to set
     */
    public static void setEmailrs(String aEmailrs) {
        emailrs = aEmailrs;
    }

    /**
     * @param aForm the form to set
     */
    public static void setForm(String aForm) {
        form = aForm;
    }

    /**
     * @param aNamauser the namauser to set
     */
    public static void setNamauser(String aNamauser) {
        namauser = aNamauser;
    }

    /**
     * @param aKode_ppk the kode_ppk to set
     */
    public static void setKode_ppk(String aKode_ppk) {
        kode_ppk = aKode_ppk;
    }

    /**
     * @param aJml1 the jml1 to set
     */
    public static void setJml1(int aJml1) {
        jml1 = aJml1;
    }

    /**
     * @param aJml2 the jml2 to set
     */
    public static void setJml2(int aJml2) {
        jml2 = aJml2;
    }

    /**
     * @param aLebar the lebar to set
     */
    public static void setLebar(int aLebar) {
        lebar = aLebar;
    }

    /**
     * @param aTinggi the tinggi to set
     */
    public static void setTinggi(int aTinggi) {
        tinggi = aTinggi;
    }
}
