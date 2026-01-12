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

    /**
     * @return the kode
     */
    public String getKode() {
        return kode;
    }

    /**
     * @param aKode the kode to set
     */
    public void setKode(String aKode) {
        kode = aKode;
    }

    /**
     * @return the kdbangsal
     */
    public String getKdbangsal() {
        return kdbangsal;
    }

    /**
     * @param aKdbangsal the kdbangsal to set
     */
    public void setKdbangsal(String aKdbangsal) {
        kdbangsal = aKdbangsal;
    }

    /**
     * @return the alamatip
     */
    public String getAlamatip() {
        return alamatip;
    }

    /**
     * @param aAlamatip the alamatip to set
     */
    public void setAlamatip(String aAlamatip) {
        alamatip = aAlamatip;
    }

    /**
     * @return the namars
     */
    public String getNamars() {
        return namars;
    }

    /**
     * @param aNamars the namars to set
     */
    public void setNamars(String aNamars) {
        namars = aNamars;
    }

    /**
     * @return the alamatrs
     */
    public String getAlamatrs() {
        return alamatrs;
    }

    /**
     * @param aAlamatrs the alamatrs to set
     */
    public void setAlamatrs(String aAlamatrs) {
        alamatrs = aAlamatrs;
    }

    /**
     * @return the kabupatenrs
     */
    public String getKabupatenrs() {
        return kabupatenrs;
    }

    /**
     * @param aKabupatenrs the kabupatenrs to set
     */
    public void setKabupatenrs(String aKabupatenrs) {
        kabupatenrs = aKabupatenrs;
    }

    /**
     * @return the propinsirs
     */
    public String getPropinsirs() {
        return propinsirs;
    }

    /**
     * @param aPropinsirs the propinsirs to set
     */
    public void setPropinsirs(String aPropinsirs) {
        propinsirs = aPropinsirs;
    }

    /**
     * @return the kontakrs
     */
    public String getKontakrs() {
        return kontakrs;
    }

    /**
     * @param aKontakrs the kontakrs to set
     */
    public void setKontakrs(String aKontakrs) {
        kontakrs = aKontakrs;
    }

    /**
     * @return the emailrs
     */
    public String getEmailrs() {
        return emailrs;
    }

    /**
     * @param aEmailrs the emailrs to set
     */
    public void setEmailrs(String aEmailrs) {
        emailrs = aEmailrs;
    }

    /**
     * @return the form
     */
    public String getForm() {
        return form;
    }

    /**
     * @param aForm the form to set
     */
    public void setForm(String aForm) {
        form = aForm;
    }

    /**
     * @return the namauser
     */
    public String getNamauser() {
        return namauser;
    }

    /**
     * @param aNamauser the namauser to set
     */
    public void setNamauser(String aNamauser) {
        namauser = aNamauser;
    }

    /**
     * @return the kode_ppk
     */
    public String getKode_ppk() {
        return kode_ppk;
    }

    /**
     * @param aKode_ppk the kode_ppk to set
     */
    public void setKode_ppk(String aKode_ppk) {
        kode_ppk = aKode_ppk;
    }

    /**
     * @return the jml1
     */
    public int getJml1() {
        return jml1;
    }

    /**
     * @param aJml1 the jml1 to set
     */
    public void setJml1(int aJml1) {
        jml1 = aJml1;
    }

    /**
     * @return the jml2
     */
    public int getJml2() {
        return jml2;
    }

    /**
     * @param aJml2 the jml2 to set
     */
    public void setJml2(int aJml2) {
        jml2 = aJml2;
    }

    /**
     * @return the lebar
     */
    public int getLebar() {
        return lebar;
    }

    /**
     * @param aLebar the lebar to set
     */
    public void setLebar(int aLebar) {
        lebar = aLebar;
    }

    /**
     * @return the tinggi
     */
    public int getTinggi() {
        return tinggi;
    }

    /**
     * @param aTinggi the tinggi to set
     */
    public void setTinggi(int aTinggi) {
        tinggi = aTinggi;
    }

    /**
     * @return the admin
     */
    public boolean isAdmin() {
        return admin;
    }

    /**
     * @param aAdmin the admin to set
     */
    public void setAdmin(boolean aAdmin) {
        admin = aAdmin;
    }

    /**
     * @return the aktif
     */
    public boolean isAktif() {
        return aktif;
    }

    /**
     * @param aAktif the aktif to set
     */
    public void setAktif(boolean aAktif) {
        aktif = aAktif;
    }

    /**
     * @return the aplikasi
     */
    public boolean isAplikasi() {
        return aplikasi;
    }

    /**
     * @param aAplikasi the aplikasi to set
     */
    public void setAplikasi(boolean aAplikasi) {
        aplikasi = aAplikasi;
    }

    /**
     * @return the bulanan_klasifikasi_pasien_ranap
     */
    public boolean isBulanan_klasifikasi_pasien_ranap() {
        return bulanan_klasifikasi_pasien_ranap;
    }

    /**
     * @param aBulanan_klasifikasi_pasien_ranap the
     * bulanan_klasifikasi_pasien_ranap to set
     */
    public void setBulanan_klasifikasi_pasien_ranap(boolean aBulanan_klasifikasi_pasien_ranap) {
        bulanan_klasifikasi_pasien_ranap = aBulanan_klasifikasi_pasien_ranap;
    }

    /**
     * @return the e_eksekutif
     */
    public boolean isE_eksekutif() {
        return e_eksekutif;
    }

    /**
     * @param aE_eksekutif the e_eksekutif to set
     */
    public void setE_eksekutif(boolean aE_eksekutif) {
        e_eksekutif = aE_eksekutif;
    }

    /**
     * @return the obat10_terbanyak_poli
     */
    public boolean isObat10_terbanyak_poli() {
        return obat10_terbanyak_poli;
    }

    /**
     * @param aObat10_terbanyak_poli the obat10_terbanyak_poli to set
     */
    public void setObat10_terbanyak_poli(boolean aObat10_terbanyak_poli) {
        obat10_terbanyak_poli = aObat10_terbanyak_poli;
    }

    /**
     * @return the status
     */
    public boolean isStatus() {
        return status;
    }

    /**
     * @param aStatus the status to set
     */
    public void setStatus(boolean aStatus) {
        status = aStatus;
    }

    /**
     * @return the user
     */
    public boolean isUser() {
        return user;
    }

    /**
     * @param aUser the user to set
     */
    public void setUser(boolean aUser) {
        user = aUser;
    }

    /**
     * @return the vakum
     */
    public boolean isVakum() {
        return vakum;
    }

    /**
     * @param aVakum the vakum to set
     */
    public void setVakum(boolean aVakum) {
        vakum = aVakum;
    }

    private final Connection koneksi = koneksiDB.condb();
    private PreparedStatement ps, ps2;
    private ResultSet rs, rs2;

    private HashMap<String, Boolean> dataAccess = new HashMap<>();

    public akses() {
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

    private String kode = "", kdbangsal = "", alamatip = "", namars = "", alamatrs = "", kabupatenrs = "", propinsirs = "", kontakrs = "", emailrs = "", form = "", namauser = "", kode_ppk = "";
    private int jml1 = 0, jml2 = 0, lebar = 0, tinggi = 0;
    private boolean admin = false, aktif = false, aplikasi = false, bulanan_klasifikasi_pasien_ranap = false, e_eksekutif = false, obat10_terbanyak_poli = false, status = false, user = false, vakum = false;

    public void setData(String user, String pass) {
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

                setJml1(rs.getRow());
                setJml2(rs2.getRow());
                if (rs.getRow() >= 1) {
                    setKode("Admin Utama");
                    for (String tableName : dataAccess.keySet()) {
                        dataAccess.replace(tableName, true);
                    }
                    this.admin = true;
                    this.user = true;
                    this.e_eksekutif = true;
                    this.vakum = true;
                    this.aplikasi = true;
                } else if (rs2.getRow() >= 1) {
                    rs2.beforeFirst();
                    rs2.next();
                    setKode(user);

                    for (String tableName : dataAccess.keySet()) {
                        dataAccess.replace(tableName, rs2.getBoolean(tableName));
                    }
                    this.admin = false;
                    this.user = false;
                    this.e_eksekutif = false;
                    this.vakum = false;
                    this.aplikasi = false;
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

    public void setLogOut() {
        setKode("");
        for (String tableName : dataAccess.keySet()) {
            dataAccess.replace(tableName, false);
        }
        this.admin = false;
        this.user = false;
        this.e_eksekutif = false;
        this.vakum = false;
        this.aplikasi = false;
    }

    public boolean getPermission(String tableName) {
        try {
            return dataAccess.get(tableName);
        } catch (Exception e) {
            System.out.println("Notifikasi: Permission pada table tersebut tidak ditemukan.");
        }
        return false;
    }

    public void setPermissionToFalse(String tableName) {
        try {
            dataAccess.replace(tableName, false);
        } catch (Exception e) {
            System.out.println("Notifikasi: " + e);
        }
    }

}
