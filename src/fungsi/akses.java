package fungsi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.EnumMap;
import java.util.List;

/**
 *
 * @author Owner
 */
public final class akses {

    private static final Connection koneksi = koneksiDB.condb();
    private static PreparedStatement ps, ps2;
    private static ResultSet rs, rs2;
    private static String kode = "", kdbangsal = "", alamatip = "", namars = "", alamatrs = "", kabupatenrs = "", propinsirs = "", kontakrs = "", emailrs = "", form = "", namauser = "", kode_ppk = "",kode_ppk_kemenkes="";
    private static final EnumMap<EnumAkses, Boolean> hakAkses = new EnumMap<>(EnumAkses.class);
    static {
        for (EnumAkses col : EnumAkses.values()) {
            hakAkses.put(col, false);
        }
    }
    private static int jml1 = 0, jml2 = 0;
    private static boolean aktif = false, admin = false, user = false, vakum = false, aplikasi = false, e_eksekutif = false, status = false;

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

                akses.jml1 = rs.getRow();
                akses.jml2 = rs2.getRow();
                if (rs.getRow() >= 1) {
                    akses.kode = "Admin Utama";
                    akses.admin = true;
                    akses.user = true;
                    akses.e_eksekutif = true;
                    akses.vakum = true;
                    akses.aplikasi = true;

                    for (EnumAkses col : EnumAkses.getNonUserColumns()) {
                        hakAkses.put(col, true);
                    }
                } else if (rs2.getRow() >= 1) {
                    rs2.beforeFirst();
                    rs2.next();
                    akses.kode = user;
                    akses.admin = false;
                    akses.user = false;
                    akses.e_eksekutif = false;
                    akses.vakum = false;
                    akses.aplikasi = false;

                    List<String> userTableColumns = new sekuel().getColumns("user");
                    for (EnumAkses col : EnumAkses.getNonUserColumns()) {
                        if (userTableColumns.contains(col.getDBTableColumn())) {
                            hakAkses.put(col, rs2.getBoolean(col.getDBTableColumn()));
                        } else {
                            hakAkses.put(col, false);
                        }
                    }
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
        akses.kode = "";
        akses.admin = false;
        akses.user = false;
        akses.e_eksekutif = false;
        akses.vakum = false;
        akses.aplikasi = false;

        for (EnumAkses col : EnumAkses.getNonUserColumns()) {
            hakAkses.put(col, false);
        }
    }

    public static int getjml1() {return akses.jml1;}
    public static int getjml2() {return akses.jml2;}
    public static boolean getadmin() {return akses.admin;}
    public static boolean getuser() {return akses.user;}
    public static boolean gete_eksekutif() {return akses.e_eksekutif;}
    public static boolean getvakum() {return akses.vakum;}
    public static boolean getaplikasi() {return akses.aplikasi;}
    public static String getkode() {return akses.kode;}
    public static void setkdbangsal(String kdbangsal) {akses.kdbangsal = kdbangsal;}
    public static String getkdbangsal() {return akses.kdbangsal;}
    public static void setalamatip(String alamatip) {akses.alamatip = alamatip;}
    public static String getalamatip() {return akses.alamatip;}
    public static void setform(String form) {akses.form = form;}
    public static String getform() {return akses.form;}
    public static void setnamauser(String namauser) {akses.namauser = namauser;}
    public static String getnamauser() {return akses.namauser;}
    public static void setstatus(boolean status) {akses.status = status;}
    public static boolean getstatus() {return akses.status;}
    public static void setnamars(String namars) {akses.namars = namars;}
    public static void setalamatrs(String alamatrs) {akses.alamatrs = alamatrs;}
    public static void setkabupatenrs(String kabupatenrs) {akses.kabupatenrs = kabupatenrs;}
    public static void setpropinsirs(String propinsirs) {akses.propinsirs = propinsirs;}
    public static void setkontakrs(String kontakrs) {akses.kontakrs = kontakrs;}
    public static void setemailrs(String emailrs) {akses.emailrs = emailrs;}
    public static void setkodeppkbpjs(String kode_ppk) {akses.kode_ppk = kode_ppk;}
    public static void setkodeppkkemenkes(String kode_ppk){akses.kode_ppk_kemenkes = kode_ppk;}
    public static String getnamars() {return akses.namars;}
    public static String getalamatrs() {return akses.alamatrs;}
    public static String getkabupatenrs() {return akses.kabupatenrs;}
    public static String getpropinsirs() {return akses.propinsirs;}
    public static String getkontakrs() {return akses.kontakrs;}
    public static String getemailrs() {return akses.emailrs;}
    public static String getkodeppkbpjs() {return akses.kode_ppk;}
    public static String getkodeppkkemenkes(){return akses.kode_ppk_kemenkes;}
    public static boolean getAktif() {return akses.aktif;}
    public static void setAktif(boolean status) {akses.aktif = status;}
    public static boolean getmanajemen() {return hakAkses.get(EnumAkses.manajemen);}
    public static boolean getpenyakit() {return hakAkses.get(EnumAkses.penyakit);}
    public static boolean getobat_penyakit() {return hakAkses.get(EnumAkses.obat_penyakit);}
    public static boolean getdokter() {return hakAkses.get(EnumAkses.dokter);}
    public static boolean getjadwal_praktek() {return hakAkses.get(EnumAkses.jadwal_praktek);}
    public static boolean getpetugas() {return hakAkses.get(EnumAkses.petugas);}
    public static boolean getpasien() {return hakAkses.get(EnumAkses.pasien);}
    public static boolean getregistrasi() {return hakAkses.get(EnumAkses.registrasi);}
    public static boolean gettindakan_ralan() {return hakAkses.get(EnumAkses.tindakan_ralan);}
    public static boolean getkamar_inap() {return hakAkses.get(EnumAkses.kamar_inap);}
    public static boolean gettindakan_ranap() {return hakAkses.get(EnumAkses.tindakan_ranap);}
    public static boolean getoperasi() {return hakAkses.get(EnumAkses.operasi);}
    public static boolean getrujukan_keluar() {return hakAkses.get(EnumAkses.rujukan_keluar);}
    public static boolean getrujukan_masuk() {return hakAkses.get(EnumAkses.rujukan_masuk);}
    public static boolean getberi_obat() {return hakAkses.get(EnumAkses.beri_obat);}
    public static boolean getresep_pulang() {return hakAkses.get(EnumAkses.resep_pulang);}
    public static boolean getpasien_meninggal() {return hakAkses.get(EnumAkses.pasien_meninggal);}
    public static boolean getdiet_pasien() {return hakAkses.get(EnumAkses.diet_pasien);}
    public static boolean getkelahiran_bayi() {return hakAkses.get(EnumAkses.kelahiran_bayi);}
    public static boolean getperiksa_lab() {return hakAkses.get(EnumAkses.periksa_lab);}
    public static boolean getperiksa_radiologi() {return hakAkses.get(EnumAkses.periksa_radiologi);}
    public static boolean getkasir_ralan() {return hakAkses.get(EnumAkses.kasir_ralan);}
    public static boolean getdeposit_pasien() {return hakAkses.get(EnumAkses.deposit_pasien);}
    public static boolean getpiutang_pasien() {return hakAkses.get(EnumAkses.piutang_pasien);}
    public static boolean getpeminjaman_berkas() {return hakAkses.get(EnumAkses.peminjaman_berkas);}
    public static boolean getbarcode() {return hakAkses.get(EnumAkses.barcode);}
    public static boolean getpresensi_harian() {return hakAkses.get(EnumAkses.presensi_harian);}
    public static boolean getpresensi_bulanan() {return hakAkses.get(EnumAkses.presensi_bulanan);}
    public static boolean getpegawai_admin() {return hakAkses.get(EnumAkses.pegawai_admin);}
    public static boolean getpegawai_user() {return hakAkses.get(EnumAkses.pegawai_user);}
    public static boolean getsuplier() {return hakAkses.get(EnumAkses.suplier);}
    public static boolean getsatuan_barang() {return hakAkses.get(EnumAkses.satuan_barang);}
    public static boolean getkonversi_satuan() {return hakAkses.get(EnumAkses.konversi_satuan);}
    public static boolean getjenis_barang() {return hakAkses.get(EnumAkses.jenis_barang);}
    public static boolean getobat() {return hakAkses.get(EnumAkses.obat);}
    public static boolean getstok_opname_obat() {return hakAkses.get(EnumAkses.stok_opname_obat);}
    public static boolean getstok_obat_pasien() {return hakAkses.get(EnumAkses.stok_obat_pasien);}
    public static boolean getpengadaan_obat() {return hakAkses.get(EnumAkses.pengadaan_obat);}
    public static boolean getpemesanan_obat() {return hakAkses.get(EnumAkses.pemesanan_obat);}
    public static boolean getpenjualan_obat() {return hakAkses.get(EnumAkses.penjualan_obat);}
    public static void setpenjualan_obatfalse() {hakAkses.put(EnumAkses.penjualan_obat, false);}
    public static boolean getpiutang_obat() {return hakAkses.get(EnumAkses.piutang_obat);}
    public static boolean getretur_ke_suplier() {return hakAkses.get(EnumAkses.retur_ke_suplier);}
    public static boolean getretur_dari_pembeli() {return hakAkses.get(EnumAkses.retur_dari_pembeli);}
    public static boolean getretur_obat_ranap() {return hakAkses.get(EnumAkses.retur_obat_ranap);}
    public static boolean getretur_piutang_pasien() {return hakAkses.get(EnumAkses.retur_piutang_pasien);}
    public static boolean getkeuntungan_penjualan() {return hakAkses.get(EnumAkses.keuntungan_penjualan);}
    public static boolean getkeuntungan_beri_obat() {return hakAkses.get(EnumAkses.keuntungan_beri_obat);}
    public static boolean getsirkulasi_obat() {return hakAkses.get(EnumAkses.sirkulasi_obat);}
    public static boolean getipsrs_barang() {return hakAkses.get(EnumAkses.ipsrs_barang);}
    public static boolean getipsrs_pengadaan_barang() {return hakAkses.get(EnumAkses.ipsrs_pengadaan_barang);}
    public static boolean getipsrs_stok_keluar() {return hakAkses.get(EnumAkses.ipsrs_stok_keluar);}
    public static boolean getipsrs_rekap_pengadaan() {return hakAkses.get(EnumAkses.ipsrs_rekap_pengadaan);}
    public static boolean getipsrs_rekap_stok_keluar() {return hakAkses.get(EnumAkses.ipsrs_rekap_stok_keluar);}
    public static boolean getipsrs_pengeluaran_harian() {return hakAkses.get(EnumAkses.ipsrs_pengeluaran_harian);}
    public static boolean getipsrs_jenis_barang() {return hakAkses.get(EnumAkses.ipsrs_jenis_barang);}
    public static boolean getinventaris_jenis() {return hakAkses.get(EnumAkses.inventaris_jenis);}
    public static boolean getinventaris_kategori() {return hakAkses.get(EnumAkses.inventaris_kategori);}
    public static boolean getinventaris_merk() {return hakAkses.get(EnumAkses.inventaris_merk);}
    public static boolean getinventaris_ruang() {return hakAkses.get(EnumAkses.inventaris_ruang);}
    public static boolean getinventaris_produsen() {return hakAkses.get(EnumAkses.inventaris_produsen);}
    public static boolean getinventaris_koleksi() {return hakAkses.get(EnumAkses.inventaris_koleksi);}
    public static boolean getinventaris_inventaris() {return hakAkses.get(EnumAkses.inventaris_inventaris);}
    public static boolean getinventaris_sirkulasi() {return hakAkses.get(EnumAkses.inventaris_sirkulasi);}
    public static boolean getparkir_jenis() {return hakAkses.get(EnumAkses.parkir_jenis);}
    public static boolean getparkir_in() {return hakAkses.get(EnumAkses.parkir_in);}
    public static boolean getparkir_out() {return hakAkses.get(EnumAkses.parkir_out);}
    public static boolean getparkir_rekap_harian() {return hakAkses.get(EnumAkses.parkir_rekap_harian);}
    public static boolean getparkir_rekap_bulanan() {return hakAkses.get(EnumAkses.parkir_rekap_bulanan);}
    public static boolean getinformasi_kamar() {return hakAkses.get(EnumAkses.informasi_kamar);}
    public static boolean getharian_tindakan_poli() {return hakAkses.get(EnumAkses.harian_tindakan_poli);}
    public static boolean getobat_per_poli() {return hakAkses.get(EnumAkses.obat_per_poli);}
    public static boolean getobat_per_kamar() {return hakAkses.get(EnumAkses.obat_per_kamar);}
    public static boolean getobat_per_dokter_ralan() {return hakAkses.get(EnumAkses.obat_per_dokter_ralan);}
    public static boolean getobat_per_dokter_ranap() {return hakAkses.get(EnumAkses.obat_per_dokter_ranap);}
    public static boolean getharian_dokter() {return hakAkses.get(EnumAkses.harian_dokter);}
    public static boolean getbulanan_dokter() {return hakAkses.get(EnumAkses.bulanan_dokter);}
    public static boolean getharian_paramedis() {return hakAkses.get(EnumAkses.harian_paramedis);}
    public static boolean getbulanan_paramedis() {return hakAkses.get(EnumAkses.bulanan_paramedis);}
    public static boolean getpembayaran_ralan() {return hakAkses.get(EnumAkses.pembayaran_ralan);}
    public static boolean getpembayaran_ranap() {return hakAkses.get(EnumAkses.pembayaran_ranap);}
    public static boolean getrekap_pembayaran_ralan() {return hakAkses.get(EnumAkses.rekap_pembayaran_ralan);}
    public static boolean getrekap_pembayaran_ranap() {return hakAkses.get(EnumAkses.rekap_pembayaran_ranap);}
    public static boolean gettagihan_masuk() {return hakAkses.get(EnumAkses.tagihan_masuk);}
    public static boolean gettambahan_biaya() {return hakAkses.get(EnumAkses.tambahan_biaya);}
    public static boolean getpotongan_biaya() {return hakAkses.get(EnumAkses.potongan_biaya);}
    public static boolean getresep_obat() {return hakAkses.get(EnumAkses.resep_obat);}
    public static boolean getresume_pasien() {return hakAkses.get(EnumAkses.resume_pasien);}
    public static boolean getpenyakit_ralan() {return hakAkses.get(EnumAkses.penyakit_ralan);}
    public static boolean getpenyakit_ranap() {return hakAkses.get(EnumAkses.penyakit_ranap);}
    public static boolean getkamar() {return hakAkses.get(EnumAkses.kamar);}
    public static boolean gettarif_ralan() {return hakAkses.get(EnumAkses.tarif_ralan);}
    public static boolean gettarif_ranap() {return hakAkses.get(EnumAkses.tarif_ranap);}
    public static boolean gettarif_lab() {return hakAkses.get(EnumAkses.tarif_lab);}
    public static boolean gettarif_radiologi() {return hakAkses.get(EnumAkses.tarif_radiologi);}
    public static boolean gettarif_operasi() {return hakAkses.get(EnumAkses.tarif_operasi);}
    public static boolean getakun_rekening() {return hakAkses.get(EnumAkses.akun_rekening);}
    public static boolean getrekening_tahun() {return hakAkses.get(EnumAkses.rekening_tahun);}
    public static boolean getposting_jurnal() {return hakAkses.get(EnumAkses.posting_jurnal);}
    public static boolean getbuku_besar() {return hakAkses.get(EnumAkses.buku_besar);}
    public static boolean getcashflow() {return hakAkses.get(EnumAkses.cashflow);}
    public static boolean getkeuangan() {return hakAkses.get(EnumAkses.keuangan);}
    public static boolean getpengeluaran() {return hakAkses.get(EnumAkses.pengeluaran);}
    public static boolean getsetup_pjlab() {return hakAkses.get(EnumAkses.setup_pjlab);}
    public static boolean getsetup_otolokasi() {return hakAkses.get(EnumAkses.setup_otolokasi);}
    public static boolean getsetup_jam_kamin() {return hakAkses.get(EnumAkses.setup_jam_kamin);}
    public static boolean getsetup_embalase() {return hakAkses.get(EnumAkses.setup_embalase);}
    public static boolean gettracer_login() {return hakAkses.get(EnumAkses.tracer_login);}
    public static boolean getdisplay() {return hakAkses.get(EnumAkses.display);}
    public static boolean getset_harga_obat() {return hakAkses.get(EnumAkses.set_harga_obat);}
    public static boolean getset_penggunaan_tarif() {return hakAkses.get(EnumAkses.set_penggunaan_tarif);}
    public static boolean getset_oto_ralan() {return hakAkses.get(EnumAkses.set_oto_ralan);}
    public static boolean getbiaya_harian() {return hakAkses.get(EnumAkses.biaya_harian);}
    public static boolean getbiaya_masuk_sekali() {return hakAkses.get(EnumAkses.biaya_masuk_sekali);}
    public static boolean getset_no_rm() {return hakAkses.get(EnumAkses.set_no_rm);}
    public static boolean getbilling_ralan() {return hakAkses.get(EnumAkses.billing_ralan);}
    public static boolean getbilling_ranap() {return hakAkses.get(EnumAkses.billing_ranap);}
    public static boolean getjm_ranap_dokter() {return hakAkses.get(EnumAkses.jm_ranap_dokter);}
    public static boolean getigd() {return hakAkses.get(EnumAkses.igd);}
    public static boolean getbarcoderalan() {return hakAkses.get(EnumAkses.barcoderalan);}
    public static boolean getbarcoderanap() {return hakAkses.get(EnumAkses.barcoderanap);}
    public static boolean getset_harga_obat_ralan() {return hakAkses.get(EnumAkses.set_harga_obat_ralan);}
    public static boolean getset_harga_obat_ranap() {return hakAkses.get(EnumAkses.set_harga_obat_ranap);}
    public static boolean getpenyakit_pd3i() {return hakAkses.get(EnumAkses.penyakit_pd3i);}
    public static boolean getsurveilans_pd3i() {return hakAkses.get(EnumAkses.surveilans_pd3i);}
    public static boolean getsurveilans_ralan() {return hakAkses.get(EnumAkses.surveilans_ralan);}
    public static boolean getdiagnosa_pasien() {return hakAkses.get(EnumAkses.diagnosa_pasien);}
    public static boolean getsurveilans_ranap() {return hakAkses.get(EnumAkses.surveilans_ranap);}
    public static boolean getpny_takmenular_ranap() {return hakAkses.get(EnumAkses.pny_takmenular_ranap);}
    public static boolean getpny_takmenular_ralan() {return hakAkses.get(EnumAkses.pny_takmenular_ralan);}
    public static boolean getkunjungan_ralan() {return hakAkses.get(EnumAkses.kunjungan_ralan);}
    public static boolean getrl32() {return hakAkses.get(EnumAkses.rl32);}
    public static boolean getrl33() {return hakAkses.get(EnumAkses.rl33);}
    public static boolean getrl37() {return hakAkses.get(EnumAkses.rl37);}
    public static boolean getrl38() {return hakAkses.get(EnumAkses.rl38);}
    public static boolean getharian_tindakan_dokter() {return hakAkses.get(EnumAkses.harian_tindakan_dokter);}
    public static boolean getsms() {return hakAkses.get(EnumAkses.sms);}
    public static boolean getsidikjari() {return hakAkses.get(EnumAkses.sidikjari);}
    public static boolean getjam_masuk() {return hakAkses.get(EnumAkses.jam_masuk);}
    public static boolean getjadwal_pegawai() {return hakAkses.get(EnumAkses.jadwal_pegawai);}
    public static boolean getparkir_barcode() {return hakAkses.get(EnumAkses.parkir_barcode);}
    public static boolean getset_nota() {return hakAkses.get(EnumAkses.set_nota);}
    public static boolean getdpjp_ranap() {return hakAkses.get(EnumAkses.dpjp_ranap);}
    public static boolean getmutasi_barang() {return hakAkses.get(EnumAkses.mutasi_barang);}
    public static boolean getrl34() {return hakAkses.get(EnumAkses.rl34);}
    public static boolean getrl36() {return hakAkses.get(EnumAkses.rl36);}
    public static boolean getfee_visit_dokter() {return hakAkses.get(EnumAkses.fee_visit_dokter);}
    public static boolean getfee_bacaan_ekg() {return hakAkses.get(EnumAkses.fee_bacaan_ekg);}
    public static boolean getfee_rujukan_rontgen() {return hakAkses.get(EnumAkses.fee_rujukan_rontgen);}
    public static boolean getfee_rujukan_ranap() {return hakAkses.get(EnumAkses.fee_rujukan_ranap);}
    public static boolean getfee_ralan() {return hakAkses.get(EnumAkses.fee_ralan);}
    public static boolean getakun_bayar() {return hakAkses.get(EnumAkses.akun_bayar);}
    public static boolean getbayar_pemesanan_obat() {return hakAkses.get(EnumAkses.bayar_pemesanan_obat);}
    public static boolean getobat_per_dokter_peresep() {return hakAkses.get(EnumAkses.obat_per_dokter_peresep);}
    public static boolean getpemasukan_lain() {return hakAkses.get(EnumAkses.pemasukan_lain);}
    public static boolean getpengaturan_rekening() {return hakAkses.get(EnumAkses.pengaturan_rekening);}
    public static boolean getclosing_kasir() {return hakAkses.get(EnumAkses.closing_kasir);}
    public static boolean getketerlambatan_presensi() {return hakAkses.get(EnumAkses.keterlambatan_presensi);}
    public static boolean getset_harga_kamar() {return hakAkses.get(EnumAkses.set_harga_kamar);}
    public static boolean getrekap_per_shift() {return hakAkses.get(EnumAkses.rekap_per_shift);}
    public static boolean getbpjs_cek_nik() {return hakAkses.get(EnumAkses.bpjs_cek_nik);}
    public static boolean getbpjs_cek_kartu() {return hakAkses.get(EnumAkses.bpjs_cek_kartu);}
    public static boolean getbpjs_cek_riwayat() {return hakAkses.get(EnumAkses.bpjs_cek_riwayat);}
    public static boolean getobat_per_cara_bayar() {return hakAkses.get(EnumAkses.obat_per_cara_bayar);}
    public static boolean getkunjungan_ranap() {return hakAkses.get(EnumAkses.kunjungan_ranap);}
    public static boolean getbayar_piutang() {return hakAkses.get(EnumAkses.bayar_piutang);}
    public static boolean getpayment_point() {return hakAkses.get(EnumAkses.payment_point);}
    public static boolean getbpjs_cek_nomor_rujukan() {return hakAkses.get(EnumAkses.bpjs_cek_nomor_rujukan);}
    public static boolean geticd9() {return hakAkses.get(EnumAkses.icd9);}
    public static boolean getdarurat_stok() {return hakAkses.get(EnumAkses.darurat_stok);}
    public static boolean getretensi_rm() {return hakAkses.get(EnumAkses.retensi_rm);}
    public static boolean gettemporary_presensi() {return hakAkses.get(EnumAkses.temporary_presensi);}
    public static boolean getjurnal_harian() {return hakAkses.get(EnumAkses.jurnal_harian);}
    public static boolean getsirkulasi_obat2() {return hakAkses.get(EnumAkses.sirkulasi_obat2);}
    public static boolean getedit_registrasi() {return hakAkses.get(EnumAkses.edit_registrasi);}
    public static boolean getbpjs_referensi_diagnosa() {return hakAkses.get(EnumAkses.bpjs_referensi_diagnosa);}
    public static boolean getbpjs_referensi_poli() {return hakAkses.get(EnumAkses.bpjs_referensi_poli);}
    public static boolean getindustrifarmasi() {return hakAkses.get(EnumAkses.industrifarmasi);}
    public static boolean getharian_js() {return hakAkses.get(EnumAkses.harian_js);}
    public static boolean getbulanan_js() {return hakAkses.get(EnumAkses.bulanan_js);}
    public static boolean getharian_paket_bhp() {return hakAkses.get(EnumAkses.harian_paket_bhp);}
    public static boolean getbulanan_paket_bhp() {return hakAkses.get(EnumAkses.bulanan_paket_bhp);}
    public static boolean getpiutang_pasien2() {return hakAkses.get(EnumAkses.piutang_pasien2);}
    public static boolean getbpjs_referensi_faskes() {return hakAkses.get(EnumAkses.bpjs_referensi_faskes);}
    public static boolean getbpjs_sep() {return hakAkses.get(EnumAkses.bpjs_sep);}
    public static boolean getpengambilan_utd() {return hakAkses.get(EnumAkses.pengambilan_utd);}
    public static boolean gettarif_utd() {return hakAkses.get(EnumAkses.tarif_utd);}
    public static boolean getpengambilan_utd2() {return hakAkses.get(EnumAkses.pengambilan_utd2);}
    public static boolean getutd_medis_rusak() {return hakAkses.get(EnumAkses.utd_medis_rusak);}
    public static boolean getpengambilan_penunjang_utd() {return hakAkses.get(EnumAkses.pengambilan_penunjang_utd);}
    public static boolean getpengambilan_penunjang_utd2() {return hakAkses.get(EnumAkses.pengambilan_penunjang_utd2);}
    public static boolean getutd_penunjang_rusak() {return hakAkses.get(EnumAkses.utd_penunjang_rusak);}
    public static boolean getsuplier_penunjang() {return hakAkses.get(EnumAkses.suplier_penunjang);}
    public static boolean getutd_donor() {return hakAkses.get(EnumAkses.utd_donor);}
    public static boolean getbpjs_monitoring_klaim() {return hakAkses.get(EnumAkses.bpjs_monitoring_klaim);}
    public static boolean getutd_cekal_darah() {return hakAkses.get(EnumAkses.utd_cekal_darah);}
    public static boolean getutd_komponen_darah() {return hakAkses.get(EnumAkses.utd_komponen_darah);}
    public static boolean getutd_stok_darah() {return hakAkses.get(EnumAkses.utd_stok_darah);}
    public static boolean getutd_pemisahan_darah() {return hakAkses.get(EnumAkses.utd_pemisahan_darah);}
    public static boolean getharian_kamar() {return hakAkses.get(EnumAkses.harian_kamar);}
    public static boolean getrincian_piutang_pasien() {return hakAkses.get(EnumAkses.rincian_piutang_pasien);}
    public static boolean getkeuntungan_beri_obat_nonpiutang() {return hakAkses.get(EnumAkses.keuntungan_beri_obat_nonpiutang);}
    public static boolean getreklasifikasi_ralan() {return hakAkses.get(EnumAkses.reklasifikasi_ralan);}
    public static boolean getreklasifikasi_ranap() {return hakAkses.get(EnumAkses.reklasifikasi_ranap);}
    public static boolean getutd_penyerahan_darah() {return hakAkses.get(EnumAkses.utd_penyerahan_darah);}
    public static void setutd_penyerahan_darahfalse() {hakAkses.put(EnumAkses.utd_penyerahan_darah, false);}
    public static boolean gethutang_obat() {return hakAkses.get(EnumAkses.hutang_obat);}
    public static boolean getriwayat_obat_alkes_bhp() {return hakAkses.get(EnumAkses.riwayat_obat_alkes_bhp);}
    public static boolean getsensus_harian_poli() {return hakAkses.get(EnumAkses.sensus_harian_poli);}
    public static boolean getrl4a() {return hakAkses.get(EnumAkses.rl4a);}
    public static boolean getaplicare_referensi_kamar() {return hakAkses.get(EnumAkses.aplicare_referensi_kamar);}
    public static boolean getaplicare_ketersediaan_kamar() {return hakAkses.get(EnumAkses.aplicare_ketersediaan_kamar);}
    public static boolean getinacbg_klaim_baru_otomatis() {return hakAkses.get(EnumAkses.inacbg_klaim_baru_otomatis);}
    public static boolean getinacbg_klaim_baru_manual() {return hakAkses.get(EnumAkses.inacbg_klaim_baru_manual);}
    public static boolean getinacbg_coder_nik() {return hakAkses.get(EnumAkses.inacbg_coder_nik);}
    public static boolean getmutasi_berkas() {return hakAkses.get(EnumAkses.mutasi_berkas);}
    public static boolean getakun_piutang() {return hakAkses.get(EnumAkses.akun_piutang);}
    public static boolean getharian_kso() {return hakAkses.get(EnumAkses.harian_kso);}
    public static boolean getbulanan_kso() {return hakAkses.get(EnumAkses.bulanan_kso);}
    public static boolean getharian_menejemen() {return hakAkses.get(EnumAkses.harian_menejemen);}
    public static boolean getbulanan_menejemen() {return hakAkses.get(EnumAkses.bulanan_menejemen);}
    public static boolean getinhealth_cek_eligibilitas() {return hakAkses.get(EnumAkses.inhealth_cek_eligibilitas);}
    public static boolean getinhealth_referensi_jenpel_ruang_rawat() {return hakAkses.get(EnumAkses.inhealth_referensi_jenpel_ruang_rawat);}
    public static boolean getinhealth_referensi_poli() {return hakAkses.get(EnumAkses.inhealth_referensi_poli);}
    public static boolean getinhealth_referensi_faskes() {return hakAkses.get(EnumAkses.inhealth_referensi_faskes);}
    public static boolean getinhealth_sjp() {return hakAkses.get(EnumAkses.inhealth_sjp);}
    public static boolean getpiutang_ralan() {return hakAkses.get(EnumAkses.piutang_ralan);}
    public static boolean getpiutang_ranap() {return hakAkses.get(EnumAkses.piutang_ranap);}
    public static boolean getdetail_piutang_penjab() {return hakAkses.get(EnumAkses.detail_piutang_penjab);}
    public static boolean getlama_pelayanan_ralan() {return hakAkses.get(EnumAkses.lama_pelayanan_ralan);}
    public static boolean getcatatan_pasien() {return hakAkses.get(EnumAkses.catatan_pasien);}
    public static boolean getrl4b() {return hakAkses.get(EnumAkses.rl4b);}
    public static boolean getrl4asebab() {return hakAkses.get(EnumAkses.rl4asebab);}
    public static boolean getrl4bsebab() {return hakAkses.get(EnumAkses.rl4bsebab);}
    public static boolean getdata_HAIs() {return hakAkses.get(EnumAkses.data_HAIs);}
    public static boolean getharian_HAIs() {return hakAkses.get(EnumAkses.harian_HAIs);}
    public static boolean getbulanan_HAIs() {return hakAkses.get(EnumAkses.bulanan_HAIs);}
    public static boolean gethitung_bor() {return hakAkses.get(EnumAkses.hitung_bor);}
    public static boolean getperusahaan_pasien() {return hakAkses.get(EnumAkses.perusahaan_pasien);}
    public static boolean getresep_dokter() {return hakAkses.get(EnumAkses.resep_dokter);}
    public static void setresep_dokterfalse() {hakAkses.put(EnumAkses.resep_dokter, false);}
    public static boolean getlama_pelayanan_apotek() {return hakAkses.get(EnumAkses.lama_pelayanan_apotek);}
    public static boolean gethitung_alos() {return hakAkses.get(EnumAkses.hitung_alos);}
    public static boolean getdetail_tindakan() {return hakAkses.get(EnumAkses.detail_tindakan);}
    public static boolean getrujukan_poli_internal() {return hakAkses.get(EnumAkses.rujukan_poli_internal);}
    public static boolean getrekap_poli_anak() {return hakAkses.get(EnumAkses.rekap_poli_anak);}
    public static boolean getgrafik_kunjungan_poli() {return hakAkses.get(EnumAkses.grafik_kunjungan_poli);}
    public static boolean getgrafik_kunjungan_perdokter() {return hakAkses.get(EnumAkses.grafik_kunjungan_perdokter);}
    public static boolean getgrafik_kunjungan_perpekerjaan() {return hakAkses.get(EnumAkses.grafik_kunjungan_perpekerjaan);}
    public static boolean getgrafik_kunjungan_perpendidikan() {return hakAkses.get(EnumAkses.grafik_kunjungan_perpendidikan);}
    public static boolean getgrafik_kunjungan_pertahun() {return hakAkses.get(EnumAkses.grafik_kunjungan_pertahun);}
    public static boolean getberkas_digital_perawatan() {return hakAkses.get(EnumAkses.berkas_digital_perawatan);}
    public static boolean getpenyakit_menular_ranap() {return hakAkses.get(EnumAkses.penyakit_menular_ranap);}
    public static boolean getpenyakit_menular_ralan() {return hakAkses.get(EnumAkses.penyakit_menular_ralan);}
    public static boolean getgrafik_kunjungan_perbulan() {return hakAkses.get(EnumAkses.grafik_kunjungan_perbulan);}
    public static boolean getgrafik_kunjungan_pertanggal() {return hakAkses.get(EnumAkses.grafik_kunjungan_pertanggal);}
    public static boolean getgrafik_kunjungan_demografi() {return hakAkses.get(EnumAkses.grafik_kunjungan_demografi);}
    public static boolean getgrafik_kunjungan_statusdaftartahun() {return hakAkses.get(EnumAkses.grafik_kunjungan_statusdaftartahun);}
    public static boolean getgrafik_kunjungan_statusdaftartahun2() {return hakAkses.get(EnumAkses.grafik_kunjungan_statusdaftartahun2);}
    public static boolean getgrafik_kunjungan_statusdaftarbulan() {return hakAkses.get(EnumAkses.grafik_kunjungan_statusdaftarbulan);}
    public static boolean getgrafik_kunjungan_statusdaftarbulan2() {return hakAkses.get(EnumAkses.grafik_kunjungan_statusdaftarbulan2);}
    public static boolean getgrafik_kunjungan_statusdaftartanggal() {return hakAkses.get(EnumAkses.grafik_kunjungan_statusdaftartanggal);}
    public static boolean getgrafik_kunjungan_statusdaftartanggal2() {return hakAkses.get(EnumAkses.grafik_kunjungan_statusdaftartanggal2);}
    public static boolean getgrafik_kunjungan_statusbataltahun() {return hakAkses.get(EnumAkses.grafik_kunjungan_statusbataltahun);}
    public static boolean getgrafik_kunjungan_statusbatalbulan() {return hakAkses.get(EnumAkses.grafik_kunjungan_statusbatalbulan);}
    public static boolean getpcare_cek_penyakit() {return hakAkses.get(EnumAkses.pcare_cek_penyakit);}
    public static boolean getgrafik_kunjungan_statusbataltanggal() {return hakAkses.get(EnumAkses.grafik_kunjungan_statusbataltanggal);}
    public static boolean getkategori_barang() {return hakAkses.get(EnumAkses.kategori_barang);}
    public static boolean getgolongan_barang() {return hakAkses.get(EnumAkses.golongan_barang);}
    public static boolean getpemberian_obat_pertanggal() {return hakAkses.get(EnumAkses.pemberian_obat_pertanggal);}
    public static boolean getpenjualan_obat_pertanggal() {return hakAkses.get(EnumAkses.penjualan_obat_pertanggal);}
    public static boolean getpcare_cek_kesadaran() {return hakAkses.get(EnumAkses.pcare_cek_kesadaran);}
    public static boolean getpembatalan_periksa_dokter() {return hakAkses.get(EnumAkses.pembatalan_periksa_dokter);}
    public static boolean getpembayaran_per_unit() {return hakAkses.get(EnumAkses.pembayaran_per_unit);}
    public static boolean getrekap_pembayaran_per_unit() {return hakAkses.get(EnumAkses.rekap_pembayaran_per_unit);}
    public static boolean getgrafik_kunjungan_percarabayar() {return hakAkses.get(EnumAkses.grafik_kunjungan_percarabayar);}
    public static boolean getipsrs_pengadaan_pertanggal() {return hakAkses.get(EnumAkses.ipsrs_pengadaan_pertanggal);}
    public static boolean getipsrs_stokkeluar_pertanggal() {return hakAkses.get(EnumAkses.ipsrs_stokkeluar_pertanggal);}
    public static boolean getgrafik_kunjungan_ranaptahun() {return hakAkses.get(EnumAkses.grafik_kunjungan_ranaptahun);}
    public static boolean getpcare_cek_rujukan() {return hakAkses.get(EnumAkses.pcare_cek_rujukan);}
    public static boolean getgrafik_lab_ralantahun() {return hakAkses.get(EnumAkses.grafik_lab_ralantahun);}
    public static boolean getgrafik_rad_ralantahun() {return hakAkses.get(EnumAkses.grafik_rad_ralantahun);}
    public static boolean getcek_entry_ralan() {return hakAkses.get(EnumAkses.cek_entry_ralan);}
    public static boolean getinacbg_klaim_baru_manual2() {return hakAkses.get(EnumAkses.inacbg_klaim_baru_manual2);}
    public static boolean getpermintaan_medis() {return hakAkses.get(EnumAkses.permintaan_medis);}
    public static boolean getrekap_permintaan_medis() {return hakAkses.get(EnumAkses.rekap_permintaan_medis);}
    public static boolean getsurat_pemesanan_medis() {return hakAkses.get(EnumAkses.surat_pemesanan_medis);}
    public static boolean getpermintaan_non_medis() {return hakAkses.get(EnumAkses.permintaan_non_medis);}
    public static boolean getrekap_permintaan_non_medis() {return hakAkses.get(EnumAkses.rekap_permintaan_non_medis);}
    public static boolean getsurat_pemesanan_non_medis() {return hakAkses.get(EnumAkses.surat_pemesanan_non_medis);}
    public static boolean getgrafik_per_perujuk() {return hakAkses.get(EnumAkses.grafik_per_perujuk);}
    public static boolean getbpjs_cek_prosedur() {return hakAkses.get(EnumAkses.bpjs_cek_prosedur);}
    public static boolean getbpjs_cek_kelas_rawat() {return hakAkses.get(EnumAkses.bpjs_cek_kelas_rawat);}
    public static boolean getbpjs_cek_dokter() {return hakAkses.get(EnumAkses.bpjs_cek_dokter);}
    public static boolean getbpjs_cek_spesialistik() {return hakAkses.get(EnumAkses.bpjs_cek_spesialistik);}
    public static boolean getbpjs_cek_ruangrawat() {return hakAkses.get(EnumAkses.bpjs_cek_ruangrawat);}
    public static boolean getbpjs_cek_carakeluar() {return hakAkses.get(EnumAkses.bpjs_cek_carakeluar);}
    public static boolean getbpjs_cek_pasca_pulang() {return hakAkses.get(EnumAkses.bpjs_cek_pasca_pulang);}
    public static boolean getdetail_tindakan_okvk() {return hakAkses.get(EnumAkses.detail_tindakan_okvk);}
    public static boolean getbilling_parsial() {return hakAkses.get(EnumAkses.billing_parsial);}
    public static boolean getbpjs_cek_nomor_rujukan_rs() {return hakAkses.get(EnumAkses.bpjs_cek_nomor_rujukan_rs);}
    public static boolean getbpjs_cek_rujukan_kartu_pcare() {return hakAkses.get(EnumAkses.bpjs_cek_rujukan_kartu_pcare);}
    public static boolean getbpjs_cek_rujukan_kartu_rs() {return hakAkses.get(EnumAkses.bpjs_cek_rujukan_kartu_rs);}
    public static boolean getakses_depo_obat() {return hakAkses.get(EnumAkses.akses_depo_obat);}
    public static boolean getbpjs_rujukan_keluar() {return hakAkses.get(EnumAkses.bpjs_rujukan_keluar);}
    public static boolean getgrafik_lab_ralanbulan() {return hakAkses.get(EnumAkses.grafik_lab_ralanbulan);}
    public static boolean getpengeluaran_stok_apotek() {return hakAkses.get(EnumAkses.pengeluaran_stok_apotek);}
    public static boolean getgrafik_rad_ralanbulan() {return hakAkses.get(EnumAkses.grafik_rad_ralanbulan);}
    public static boolean getdetailjmdokter2() {return hakAkses.get(EnumAkses.detailjmdokter2);}
    public static boolean getpengaduan_pasien() {return hakAkses.get(EnumAkses.pengaduan_pasien);}
    public static boolean getgrafik_lab_ralanhari() {return hakAkses.get(EnumAkses.grafik_lab_ralanhari);}
    public static boolean getgrafik_rad_ralanhari() {return hakAkses.get(EnumAkses.grafik_rad_ralanhari);}
    public static boolean getsensus_harian_ralan() {return hakAkses.get(EnumAkses.sensus_harian_ralan);}
    public static boolean getmetode_racik() {return hakAkses.get(EnumAkses.metode_racik);}
    public static boolean getpembayaran_akun_bayar() {return hakAkses.get(EnumAkses.pembayaran_akun_bayar);}
    public static boolean getpengguna_obat_resep() {return hakAkses.get(EnumAkses.pengguna_obat_resep);}
    public static boolean getrekap_pemesanan() {return hakAkses.get(EnumAkses.rekap_pemesanan);}
    public static boolean getmaster_berkas_pegawai() {return hakAkses.get(EnumAkses.master_berkas_pegawai);}
    public static boolean getberkas_kepegawaian() {return hakAkses.get(EnumAkses.berkas_kepegawaian);}
    public static boolean getriwayat_jabatan() {return hakAkses.get(EnumAkses.riwayat_jabatan);}
    public static boolean getriwayat_pendidikan() {return hakAkses.get(EnumAkses.riwayat_pendidikan);}
    public static boolean getriwayat_naik_gaji() {return hakAkses.get(EnumAkses.riwayat_naik_gaji);}
    public static boolean getkegiatan_ilmiah() {return hakAkses.get(EnumAkses.kegiatan_ilmiah);}
    public static boolean getriwayat_penghargaan() {return hakAkses.get(EnumAkses.riwayat_penghargaan);}
    public static boolean getriwayat_penelitian() {return hakAkses.get(EnumAkses.riwayat_penelitian);}
    public static boolean getpenerimaan_non_medis() {return hakAkses.get(EnumAkses.penerimaan_non_medis);}
    public static boolean getbayar_pesan_non_medis() {return hakAkses.get(EnumAkses.bayar_pesan_non_medis);}
    public static boolean gethutang_barang_non_medis() {return hakAkses.get(EnumAkses.hutang_barang_non_medis);}
    public static boolean getrekap_pemesanan_non_medis() {return hakAkses.get(EnumAkses.rekap_pemesanan_non_medis);}
    public static boolean getinsiden_keselamatan() {return hakAkses.get(EnumAkses.insiden_keselamatan);}
    public static boolean getinsiden_keselamatan_pasien() {return hakAkses.get(EnumAkses.insiden_keselamatan_pasien);}
    public static boolean getgrafik_ikp_pertahun() {return hakAkses.get(EnumAkses.grafik_ikp_pertahun);}
    public static boolean getgrafik_ikp_perbulan() {return hakAkses.get(EnumAkses.grafik_ikp_perbulan);}
    public static boolean getgrafik_ikp_pertanggal() {return hakAkses.get(EnumAkses.grafik_ikp_pertanggal);}
    public static boolean getriwayat_data_batch() {return hakAkses.get(EnumAkses.riwayat_data_batch);}
    public static boolean getgrafik_ikp_jenis() {return hakAkses.get(EnumAkses.grafik_ikp_jenis);}
    public static boolean getgrafik_ikp_dampak() {return hakAkses.get(EnumAkses.grafik_ikp_dampak);}
    public static boolean getpiutang_akun_piutang() {return hakAkses.get(EnumAkses.piutang_akun_piutang);}
    public static void setresep_obatfalse() {hakAkses.put(EnumAkses.resep_obat, false);}
    public static boolean getgrafik_kunjungan_per_agama() {return hakAkses.get(EnumAkses.grafik_kunjungan_per_agama);}
    public static boolean getgrafik_kunjungan_per_umur() {return hakAkses.get(EnumAkses.grafik_kunjungan_per_umur);}
    public static boolean getsuku_bangsa() {return hakAkses.get(EnumAkses.suku_bangsa);}
    public static boolean getbahasa_pasien() {return hakAkses.get(EnumAkses.bahasa_pasien);}
    public static boolean getgolongan_tni() {return hakAkses.get(EnumAkses.golongan_tni);}
    public static boolean getsatuan_tni() {return hakAkses.get(EnumAkses.satuan_tni);}
    public static boolean getjabatan_tni() {return hakAkses.get(EnumAkses.jabatan_tni);}
    public static boolean getpangkat_tni() {return hakAkses.get(EnumAkses.pangkat_tni);}
    public static boolean getgolongan_polri() {return hakAkses.get(EnumAkses.golongan_polri);}
    public static boolean getsatuan_polri() {return hakAkses.get(EnumAkses.satuan_polri);}
    public static boolean getjabatan_polri() {return hakAkses.get(EnumAkses.jabatan_polri);}
    public static boolean getpangkat_polri() {return hakAkses.get(EnumAkses.pangkat_polri);}
    public static boolean getcacat_fisik() {return hakAkses.get(EnumAkses.cacat_fisik);}
    public static boolean getgrafik_kunjungan_suku() {return hakAkses.get(EnumAkses.grafik_kunjungan_suku);}
    public static boolean getgrafik_kunjungan_bahasa() {return hakAkses.get(EnumAkses.grafik_kunjungan_bahasa);}
    public static boolean getbooking_operasi() {return hakAkses.get(EnumAkses.booking_operasi);}
    public static boolean getmapping_poli_bpjs() {return hakAkses.get(EnumAkses.mapping_poli_bpjs);}
    public static boolean getgrafik_kunjungan_per_cacat() {return hakAkses.get(EnumAkses.grafik_kunjungan_per_cacat);}
    public static boolean getbarang_cssd() {return hakAkses.get(EnumAkses.barang_cssd);}
    public static boolean getskdp_bpjs() {return hakAkses.get(EnumAkses.skdp_bpjs);}
    public static boolean getbooking_registrasi() {return hakAkses.get(EnumAkses.booking_registrasi);}
    public static boolean getbpjs_cek_propinsi() {return hakAkses.get(EnumAkses.bpjs_cek_propinsi);}
    public static boolean getbpjs_cek_kabupaten() {return hakAkses.get(EnumAkses.bpjs_cek_kabupaten);}
    public static boolean getbpjs_cek_kecamatan() {return hakAkses.get(EnumAkses.bpjs_cek_kecamatan);}
    public static boolean getbpjs_cek_dokterdpjp() {return hakAkses.get(EnumAkses.bpjs_cek_dokterdpjp);}
    public static boolean getbpjs_cek_riwayat_rujukanrs() {return hakAkses.get(EnumAkses.bpjs_cek_riwayat_rujukanrs);}
    public static boolean getbpjs_cek_tanggal_rujukan() {return hakAkses.get(EnumAkses.bpjs_cek_tanggal_rujukan);}
    public static boolean getpermintaan_lab() {return hakAkses.get(EnumAkses.permintaan_lab);}
    public static void setperiksalabfalse() {hakAkses.put(EnumAkses.periksa_lab, false);}
    public static void setperiksalabpafalse() {hakAkses.put(EnumAkses.pemeriksaan_lab_pa, false);}
    public static void setperiksalabmbfalse() {hakAkses.put(EnumAkses.pemeriksaan_lab_mb, false);}
    public static void setpermintaanlabfalse() {hakAkses.put(EnumAkses.permintaan_lab, false);}
    public static boolean getpermintaan_radiologi() {return hakAkses.get(EnumAkses.permintaan_radiologi);}
    public static void setperiksaradiologifalse() {hakAkses.put(EnumAkses.periksa_radiologi, false);}
    public static void setpermintaanradiologifalse() {hakAkses.put(EnumAkses.permintaan_radiologi, false);}
    public static boolean getsurat_indeks() {return hakAkses.get(EnumAkses.surat_indeks);}
    public static boolean getsurat_map() {return hakAkses.get(EnumAkses.surat_map);}
    public static boolean getsurat_almari() {return hakAkses.get(EnumAkses.surat_almari);}
    public static boolean getsurat_rak() {return hakAkses.get(EnumAkses.surat_rak);}
    public static boolean getsurat_ruang() {return hakAkses.get(EnumAkses.surat_ruang);}
    public static boolean getsurat_klasifikasi() {return hakAkses.get(EnumAkses.surat_klasifikasi);}
    public static boolean getsurat_status() {return hakAkses.get(EnumAkses.surat_status);}
    public static boolean getsurat_sifat() {return hakAkses.get(EnumAkses.surat_sifat);}
    public static boolean getsurat_balas() {return hakAkses.get(EnumAkses.surat_balas);}
    public static boolean getsurat_masuk() {return hakAkses.get(EnumAkses.surat_masuk);}
    public static boolean getpcare_cek_dokter() {return hakAkses.get(EnumAkses.pcare_cek_dokter);}
    public static boolean getpcare_cek_poli() {return hakAkses.get(EnumAkses.pcare_cek_poli);}
    public static boolean getpcare_cek_provider() {return hakAkses.get(EnumAkses.pcare_cek_provider);}
    public static boolean getpcare_cek_statuspulang() {return hakAkses.get(EnumAkses.pcare_cek_statuspulang);}
    public static boolean getpcare_cek_spesialis() {return hakAkses.get(EnumAkses.pcare_cek_spesialis);}
    public static boolean getpcare_cek_subspesialis() {return hakAkses.get(EnumAkses.pcare_cek_subspesialis);}
    public static boolean getpcare_cek_sarana() {return hakAkses.get(EnumAkses.pcare_cek_sarana);}
    public static boolean getpcare_cek_khusus() {return hakAkses.get(EnumAkses.pcare_cek_khusus);}
    public static boolean getpcare_cek_obat() {return hakAkses.get(EnumAkses.pcare_cek_obat);}
    public static boolean getpcare_cek_tindakan() {return hakAkses.get(EnumAkses.pcare_cek_tindakan);}
    public static boolean getpcare_cek_faskessubspesialis() {return hakAkses.get(EnumAkses.pcare_cek_faskessubspesialis);}
    public static boolean getpcare_cek_faskesalihrawat() {return hakAkses.get(EnumAkses.pcare_cek_faskesalihrawat);}
    public static boolean getpcare_cek_faskesthalasemia() {return hakAkses.get(EnumAkses.pcare_cek_faskesthalasemia);}
    public static boolean getpcare_mapping_obat() {return hakAkses.get(EnumAkses.pcare_mapping_obat);}
    public static boolean getpcare_mapping_tindakan() {return hakAkses.get(EnumAkses.pcare_mapping_tindakan);}
    public static boolean getpcare_club_prolanis() {return hakAkses.get(EnumAkses.pcare_club_prolanis);}
    public static boolean getpcare_mapping_poli() {return hakAkses.get(EnumAkses.pcare_mapping_poli);}
    public static boolean getpcare_kegiatan_kelompok() {return hakAkses.get(EnumAkses.pcare_kegiatan_kelompok);}
    public static boolean getpcare_mapping_tindakan_ranap() {return hakAkses.get(EnumAkses.pcare_mapping_tindakan_ranap);}
    public static boolean getpcare_peserta_kegiatan_kelompok() {return hakAkses.get(EnumAkses.pcare_peserta_kegiatan_kelompok);}
    public static boolean getsirkulasi_obat3() {return hakAkses.get(EnumAkses.sirkulasi_obat3);}
    public static boolean getbridging_pcare_daftar() {return hakAkses.get(EnumAkses.bridging_pcare_daftar);}
    public static boolean getpcare_mapping_dokter() {return hakAkses.get(EnumAkses.pcare_mapping_dokter);}
    public static boolean getranap_per_ruang() {return hakAkses.get(EnumAkses.ranap_per_ruang);}
    public static boolean getpenyakit_ranap_cara_bayar() {return hakAkses.get(EnumAkses.penyakit_ranap_cara_bayar);}
    public static boolean getanggota_militer_dirawat() {return hakAkses.get(EnumAkses.anggota_militer_dirawat);}
    public static boolean getset_input_parsial() {return hakAkses.get(EnumAkses.set_input_parsial);}
    public static boolean getlama_pelayanan_radiologi() {return hakAkses.get(EnumAkses.lama_pelayanan_radiologi);}
    public static boolean getlama_pelayanan_lab() {return hakAkses.get(EnumAkses.lama_pelayanan_lab);}
    public static boolean getbpjs_cek_sep() {return hakAkses.get(EnumAkses.bpjs_cek_sep);}
    public static boolean getcatatan_perawatan() {return hakAkses.get(EnumAkses.catatan_perawatan);}
    public static boolean getsurat_keluar() {return hakAkses.get(EnumAkses.surat_keluar);}
    public static boolean getkegiatan_farmasi() {return hakAkses.get(EnumAkses.kegiatan_farmasi);}
    public static boolean getstok_opname_logistik() {return hakAkses.get(EnumAkses.stok_opname_logistik);}
    public static boolean getsirkulasi_non_medis() {return hakAkses.get(EnumAkses.sirkulasi_non_medis);}
    public static boolean getrekap_lab_pertahun() {return hakAkses.get(EnumAkses.rekap_lab_pertahun);}
    public static boolean getperujuk_lab_pertahun() {return hakAkses.get(EnumAkses.perujuk_lab_pertahun);}
    public static boolean getrekap_radiologi_pertahun() {return hakAkses.get(EnumAkses.rekap_radiologi_pertahun);}
    public static boolean getperujuk_radiologi_pertahun() {return hakAkses.get(EnumAkses.perujuk_radiologi_pertahun);}
    public static boolean getjumlah_porsi_diet() {return hakAkses.get(EnumAkses.jumlah_porsi_diet);}
    public static boolean getjumlah_macam_diet() {return hakAkses.get(EnumAkses.jumlah_macam_diet);}
    public static boolean getpayment_point2() {return hakAkses.get(EnumAkses.payment_point2);}
    public static boolean getpembayaran_akun_bayar2() {return hakAkses.get(EnumAkses.pembayaran_akun_bayar2);}
    public static boolean gethapus_nota_salah() {return hakAkses.get(EnumAkses.hapus_nota_salah);}
    public static boolean gethais_perbangsal() {return hakAkses.get(EnumAkses.hais_perbangsal);}
    public static boolean getppn_obat() {return hakAkses.get(EnumAkses.ppn_obat);}
    public static boolean getsaldo_akun_perbulan() {return hakAkses.get(EnumAkses.saldo_akun_perbulan);}
    public static boolean getdisplay_apotek() {return hakAkses.get(EnumAkses.display_apotek);}
    public static boolean getsisrute_referensi_faskes() {return hakAkses.get(EnumAkses.sisrute_referensi_faskes);}
    public static boolean getsisrute_referensi_alasanrujuk() {return hakAkses.get(EnumAkses.sisrute_referensi_alasanrujuk);}
    public static boolean getsisrute_referensi_diagnosa() {return hakAkses.get(EnumAkses.sisrute_referensi_diagnosa);}
    public static boolean getsisrute_rujukan_masuk() {return hakAkses.get(EnumAkses.sisrute_rujukan_masuk);}
    public static boolean getsisrute_rujukan_keluar() {return hakAkses.get(EnumAkses.sisrute_rujukan_keluar);}
    public static boolean getbpjs_cek_skdp() {return hakAkses.get(EnumAkses.bpjs_cek_skdp);}
    public static boolean getdata_batch() {return hakAkses.get(EnumAkses.data_batch);}
    public static boolean getkunjungan_permintaan_lab() {return hakAkses.get(EnumAkses.kunjungan_permintaan_lab);}
    public static boolean getkunjungan_permintaan_lab2() {return hakAkses.get(EnumAkses.kunjungan_permintaan_lab2);}
    public static boolean getkunjungan_permintaan_radiologi() {return hakAkses.get(EnumAkses.kunjungan_permintaan_radiologi);}
    public static boolean getkunjungan_permintaan_radiologi2() {return hakAkses.get(EnumAkses.kunjungan_permintaan_radiologi2);}
    public static boolean getpcare_pemberian_obat() {return hakAkses.get(EnumAkses.pcare_pemberian_obat);}
    public static boolean getpcare_pemberian_tindakan() {return hakAkses.get(EnumAkses.pcare_pemberian_tindakan);}
    public static boolean getpembayaran_akun_bayar3() {return hakAkses.get(EnumAkses.pembayaran_akun_bayar3);}
    public static boolean getpassword_asuransi() {return hakAkses.get(EnumAkses.password_asuransi);}
    public static boolean getkemenkes_sitt() {return hakAkses.get(EnumAkses.kemenkes_sitt);}
    public static boolean getsiranap_ketersediaan_kamar() {return hakAkses.get(EnumAkses.siranap_ketersediaan_kamar);}
    public static boolean getgrafik_tb_periodelaporan() {return hakAkses.get(EnumAkses.grafik_tb_periodelaporan);}
    public static boolean getgrafik_tb_rujukan() {return hakAkses.get(EnumAkses.grafik_tb_rujukan);}
    public static boolean getgrafik_tb_riwayat() {return hakAkses.get(EnumAkses.grafik_tb_riwayat);}
    public static boolean getgrafik_tb_tipediagnosis() {return hakAkses.get(EnumAkses.grafik_tb_tipediagnosis);}
    public static boolean getgrafik_tb_statushiv() {return hakAkses.get(EnumAkses.grafik_tb_statushiv);}
    public static boolean getgrafik_tb_skoringanak() {return hakAkses.get(EnumAkses.grafik_tb_skoringanak);}
    public static boolean getgrafik_tb_konfirmasiskoring5() {return hakAkses.get(EnumAkses.grafik_tb_konfirmasiskoring5);}
    public static boolean getgrafik_tb_konfirmasiskoring6() {return hakAkses.get(EnumAkses.grafik_tb_konfirmasiskoring6);}
    public static boolean getgrafik_tb_sumberobat() {return hakAkses.get(EnumAkses.grafik_tb_sumberobat);}
    public static boolean getgrafik_tb_hasilakhirpengobatan() {return hakAkses.get(EnumAkses.grafik_tb_hasilakhirpengobatan);}
    public static boolean getgrafik_tb_hasilteshiv() {return hakAkses.get(EnumAkses.grafik_tb_hasilteshiv);}
    public static boolean getkadaluarsa_batch() {return hakAkses.get(EnumAkses.kadaluarsa_batch);}
    public static boolean getsisa_stok() {return hakAkses.get(EnumAkses.sisa_stok);}
    public static boolean getobat_per_resep() {return hakAkses.get(EnumAkses.obat_per_resep);}
    public static boolean getpemakaian_air_pdam() {return hakAkses.get(EnumAkses.pemakaian_air_pdam);}
    public static boolean getlimbah_b3_medis() {return hakAkses.get(EnumAkses.limbah_b3_medis);}
    public static boolean getgrafik_air_pdam_pertanggal() {return hakAkses.get(EnumAkses.grafik_air_pdam_pertanggal);}
    public static boolean getgrafik_air_pdam_perbulan() {return hakAkses.get(EnumAkses.grafik_air_pdam_perbulan);}
    public static boolean getgrafik_limbahb3_pertanggal() {return hakAkses.get(EnumAkses.grafik_limbahb3_pertanggal);}
    public static boolean getgrafik_limbahb3_perbulan() {return hakAkses.get(EnumAkses.grafik_limbahb3_perbulan);}
    public static boolean getlimbah_domestik() {return hakAkses.get(EnumAkses.limbah_domestik);}
    public static boolean getgrafik_limbahdomestik_pertanggal() {return hakAkses.get(EnumAkses.grafik_limbahdomestik_pertanggal);}
    public static boolean getgrafik_limbahdomestik_perbulan() {return hakAkses.get(EnumAkses.grafik_limbahdomestik_perbulan);}
    public static boolean getmutu_air_limbah() {return hakAkses.get(EnumAkses.mutu_air_limbah);}
    public static boolean getpest_control() {return hakAkses.get(EnumAkses.pest_control);}
    public static boolean getruang_perpustakaan() {return hakAkses.get(EnumAkses.ruang_perpustakaan);}
    public static boolean getkategori_perpustakaan() {return hakAkses.get(EnumAkses.kategori_perpustakaan);}
    public static boolean getjenis_perpustakaan() {return hakAkses.get(EnumAkses.jenis_perpustakaan);}
    public static boolean getpengarang_perpustakaan() {return hakAkses.get(EnumAkses.pengarang_perpustakaan);}
    public static boolean getpenerbit_perpustakaan() {return hakAkses.get(EnumAkses.penerbit_perpustakaan);}
    public static boolean getkoleksi_perpustakaan() {return hakAkses.get(EnumAkses.koleksi_perpustakaan);}
    public static boolean getinventaris_perpustakaan() {return hakAkses.get(EnumAkses.inventaris_perpustakaan);}
    public static boolean getset_peminjaman_perpustakaan() {return hakAkses.get(EnumAkses.set_peminjaman_perpustakaan);}
    public static boolean getdenda_perpustakaan() {return hakAkses.get(EnumAkses.denda_perpustakaan);}
    public static boolean getanggota_perpustakaan() {return hakAkses.get(EnumAkses.anggota_perpustakaan);}
    public static boolean getpeminjaman_perpustakaan() {return hakAkses.get(EnumAkses.peminjaman_perpustakaan);}
    public static boolean getbayar_denda_perpustakaan() {return hakAkses.get(EnumAkses.bayar_denda_perpustakaan);}
    public static boolean getebook_perpustakaan() {return hakAkses.get(EnumAkses.ebook_perpustakaan);}
    public static boolean getjenis_cidera_k3rs() {return hakAkses.get(EnumAkses.jenis_cidera_k3rs);}
    public static boolean getpenyebab_k3rs() {return hakAkses.get(EnumAkses.penyebab_k3rs);}
    public static boolean getjenis_luka_k3rs() {return hakAkses.get(EnumAkses.jenis_luka_k3rs);}
    public static boolean getlokasi_kejadian_k3rs() {return hakAkses.get(EnumAkses.lokasi_kejadian_k3rs);}
    public static boolean getdampak_cidera_k3rs() {return hakAkses.get(EnumAkses.dampak_cidera_k3rs);}
    public static boolean getjenis_pekerjaan_k3rs() {return hakAkses.get(EnumAkses.jenis_pekerjaan_k3rs);}
    public static boolean getbagian_tubuh_k3rs() {return hakAkses.get(EnumAkses.bagian_tubuh_k3rs);}
    public static boolean getperistiwa_k3rs() {return hakAkses.get(EnumAkses.peristiwa_k3rs);}
    public static boolean getgrafik_k3_pertahun() {return hakAkses.get(EnumAkses.grafik_k3_pertahun);}
    public static boolean getgrafik_k3_perbulan() {return hakAkses.get(EnumAkses.grafik_k3_perbulan);}
    public static boolean getgrafik_k3_pertanggal() {return hakAkses.get(EnumAkses.grafik_k3_pertanggal);}
    public static boolean getgrafik_k3_perjeniscidera() {return hakAkses.get(EnumAkses.grafik_k3_perjeniscidera);}
    public static boolean getgrafik_k3_perpenyebab() {return hakAkses.get(EnumAkses.grafik_k3_perpenyebab);}
    public static boolean getgrafik_k3_perjenisluka() {return hakAkses.get(EnumAkses.grafik_k3_perjenisluka);}
    public static boolean getgrafik_k3_lokasikejadian() {return hakAkses.get(EnumAkses.grafik_k3_lokasikejadian);}
    public static boolean getgrafik_k3_dampakcidera() {return hakAkses.get(EnumAkses.grafik_k3_dampakcidera);}
    public static boolean getgrafik_k3_perjenispekerjaan() {return hakAkses.get(EnumAkses.grafik_k3_perjenispekerjaan);}
    public static boolean getgrafik_k3_perbagiantubuh() {return hakAkses.get(EnumAkses.grafik_k3_perbagiantubuh);}
    public static boolean getjenis_cidera_k3rstahun() {return hakAkses.get(EnumAkses.jenis_cidera_k3rstahun);}
    public static boolean getpenyebab_k3rstahun() {return hakAkses.get(EnumAkses.penyebab_k3rstahun);}
    public static boolean getjenis_luka_k3rstahun() {return hakAkses.get(EnumAkses.jenis_luka_k3rstahun);}
    public static boolean getlokasi_kejadian_k3rstahun() {return hakAkses.get(EnumAkses.lokasi_kejadian_k3rstahun);}
    public static boolean getdampak_cidera_k3rstahun() {return hakAkses.get(EnumAkses.dampak_cidera_k3rstahun);}
    public static boolean getjenis_pekerjaan_k3rstahun() {return hakAkses.get(EnumAkses.jenis_pekerjaan_k3rstahun);}
    public static boolean getbagian_tubuh_k3rstahun() {return hakAkses.get(EnumAkses.bagian_tubuh_k3rstahun);}
    public static boolean getsekrining_rawat_jalan() {return hakAkses.get(EnumAkses.sekrining_rawat_jalan);}
    public static boolean getbpjs_histori_pelayanan() {return hakAkses.get(EnumAkses.bpjs_histori_pelayanan);}
    public static boolean getrekap_mutasi_berkas() {return hakAkses.get(EnumAkses.rekap_mutasi_berkas);}
    public static boolean getskrining_ralan_pernapasan_pertahun() {return hakAkses.get(EnumAkses.skrining_ralan_pernapasan_pertahun);}
    public static boolean getpengajuan_barang_medis() {return hakAkses.get(EnumAkses.pengajuan_barang_medis);}
    public static boolean getpengajuan_barang_nonmedis() {return hakAkses.get(EnumAkses.pengajuan_barang_nonmedis);}
    public static boolean getgrafik_kunjungan_ranapbulan() {return hakAkses.get(EnumAkses.grafik_kunjungan_ranapbulan);}
    public static boolean getgrafik_kunjungan_ranaptanggal() {return hakAkses.get(EnumAkses.grafik_kunjungan_ranaptanggal);}
    public static boolean getgrafik_kunjungan_ranap_peruang() {return hakAkses.get(EnumAkses.grafik_kunjungan_ranap_peruang);}
    public static boolean getkunjungan_bangsal_pertahun() {return hakAkses.get(EnumAkses.kunjungan_bangsal_pertahun);}
    public static boolean getgrafik_jenjang_jabatanpegawai() {return hakAkses.get(EnumAkses.grafik_jenjang_jabatanpegawai);}
    public static boolean getgrafik_bidangpegawai() {return hakAkses.get(EnumAkses.grafik_bidangpegawai);}
    public static boolean getgrafik_departemenpegawai() {return hakAkses.get(EnumAkses.grafik_departemenpegawai);}
    public static boolean getgrafik_pendidikanpegawai() {return hakAkses.get(EnumAkses.grafik_pendidikanpegawai);}
    public static boolean getgrafik_sttswppegawai() {return hakAkses.get(EnumAkses.grafik_sttswppegawai);}
    public static boolean getgrafik_sttskerjapegawai() {return hakAkses.get(EnumAkses.grafik_sttskerjapegawai);}
    public static boolean getgrafik_sttspulangranap() {return hakAkses.get(EnumAkses.grafik_sttspulangranap);}
    public static boolean getkip_pasien_ranap() {return hakAkses.get(EnumAkses.kip_pasien_ranap);}
    public static boolean getkip_pasien_ralan() {return hakAkses.get(EnumAkses.kip_pasien_ralan);}
    public static boolean getbpjs_mapping_dokterdpjp() {return hakAkses.get(EnumAkses.bpjs_mapping_dokterdpjp);}
    public static boolean getdata_triase_igd() {return hakAkses.get(EnumAkses.data_triase_igd);}
    public static boolean getmaster_triase_skala1() {return hakAkses.get(EnumAkses.master_triase_skala1);}
    public static boolean getmaster_triase_skala2() {return hakAkses.get(EnumAkses.master_triase_skala2);}
    public static boolean getmaster_triase_skala3() {return hakAkses.get(EnumAkses.master_triase_skala3);}
    public static boolean getmaster_triase_skala4() {return hakAkses.get(EnumAkses.master_triase_skala4);}
    public static boolean getmaster_triase_skala5() {return hakAkses.get(EnumAkses.master_triase_skala5);}
    public static boolean getmaster_triase_pemeriksaan() {return hakAkses.get(EnumAkses.master_triase_pemeriksaan);}
    public static boolean getmaster_triase_macamkasus() {return hakAkses.get(EnumAkses.master_triase_macamkasus);}
    public static boolean getrekap_permintaan_diet() {return hakAkses.get(EnumAkses.rekap_permintaan_diet);}
    public static boolean getdaftar_pasien_ranap() {return hakAkses.get(EnumAkses.daftar_pasien_ranap);}
    public static boolean getdaftar_pasien_ranaptni() {return hakAkses.get(EnumAkses.daftar_pasien_ranaptni);}
    public static boolean getpengajuan_asetinventaris() {return hakAkses.get(EnumAkses.pengajuan_asetinventaris);}
    public static boolean getitem_apotek_jenis() {return hakAkses.get(EnumAkses.item_apotek_jenis);}
    public static boolean getitem_apotek_kategori() {return hakAkses.get(EnumAkses.item_apotek_kategori);}
    public static boolean getitem_apotek_golongan() {return hakAkses.get(EnumAkses.item_apotek_golongan);}
    public static boolean getitem_apotek_industrifarmasi() {return hakAkses.get(EnumAkses.item_apotek_industrifarmasi);}
    public static boolean getobat10_terbanyak_poli() {return hakAkses.get(EnumAkses.obat10_terbanyak_poli);}
    public static boolean getgrafik_pengajuan_aset_urgensi() {return hakAkses.get(EnumAkses.grafik_pengajuan_aset_urgensi);}
    public static boolean getgrafik_pengajuan_aset_status() {return hakAkses.get(EnumAkses.grafik_pengajuan_aset_status);}
    public static boolean getgrafik_pengajuan_aset_departemen() {return hakAkses.get(EnumAkses.grafik_pengajuan_aset_departemen);}
    public static boolean getrekap_pengajuan_aset_departemen() {return hakAkses.get(EnumAkses.rekap_pengajuan_aset_departemen);}
    public static boolean getgrafik_kelompok_jabatanpegawai() {return hakAkses.get(EnumAkses.grafik_kelompok_jabatanpegawai);}
    public static boolean getgrafik_resiko_kerjapegawai() {return hakAkses.get(EnumAkses.grafik_resiko_kerjapegawai);}
    public static boolean getgrafik_emergency_indexpegawai() {return hakAkses.get(EnumAkses.grafik_emergency_indexpegawai);}
    public static boolean getgrafik_inventaris_ruang() {return hakAkses.get(EnumAkses.grafik_inventaris_ruang);}
    public static boolean getharian_HAIs2() {return hakAkses.get(EnumAkses.harian_HAIs2);}
    public static boolean getgrafik_inventaris_jenis() {return hakAkses.get(EnumAkses.grafik_inventaris_jenis);}
    public static boolean getdata_resume_pasien() {return hakAkses.get(EnumAkses.data_resume_pasien);}
    public static boolean getperkiraan_biaya_ranap() {return hakAkses.get(EnumAkses.perkiraan_biaya_ranap);}
    public static boolean getrekap_obat_poli() {return hakAkses.get(EnumAkses.rekap_obat_poli);}
    public static boolean getrekap_obat_pasien() {return hakAkses.get(EnumAkses.rekap_obat_pasien);}
    public static boolean getgrafik_HAIs_pasienbangsal() {return hakAkses.get(EnumAkses.grafik_HAIs_pasienbangsal);}
    public static boolean getgrafik_HAIs_pasienbulan() {return hakAkses.get(EnumAkses.grafik_HAIs_pasienbulan);}
    public static boolean getpermintaan_perbaikan_inventaris() {return hakAkses.get(EnumAkses.permintaan_perbaikan_inventaris);}
    public static boolean getgrafik_HAIs_laju_vap() {return hakAkses.get(EnumAkses.grafik_HAIs_laju_vap);}
    public static boolean getgrafik_HAIs_laju_iad() {return hakAkses.get(EnumAkses.grafik_HAIs_laju_iad);}
    public static boolean getgrafik_HAIs_laju_pleb() {return hakAkses.get(EnumAkses.grafik_HAIs_laju_pleb);}
    public static boolean getgrafik_HAIs_laju_isk() {return hakAkses.get(EnumAkses.grafik_HAIs_laju_isk);}
    public static boolean getgrafik_HAIs_laju_ilo() {return hakAkses.get(EnumAkses.grafik_HAIs_laju_ilo);}
    public static boolean getgrafik_HAIs_laju_hap() {return hakAkses.get(EnumAkses.grafik_HAIs_laju_hap);}
    public static boolean getinhealth_mapping_poli() {return hakAkses.get(EnumAkses.inhealth_mapping_poli);}
    public static boolean getinhealth_mapping_dokter() {return hakAkses.get(EnumAkses.inhealth_mapping_dokter);}
    public static boolean getinhealth_mapping_tindakan_ralan() {return hakAkses.get(EnumAkses.inhealth_mapping_tindakan_ralan);}
    public static boolean getinhealth_mapping_tindakan_ranap() {return hakAkses.get(EnumAkses.inhealth_mapping_tindakan_ranap);}
    public static boolean getinhealth_mapping_tindakan_radiologi() {return hakAkses.get(EnumAkses.inhealth_mapping_tindakan_radiologi);}
    public static boolean getinhealth_mapping_tindakan_laborat() {return hakAkses.get(EnumAkses.inhealth_mapping_tindakan_laborat);}
    public static boolean getinhealth_mapping_tindakan_operasi() {return hakAkses.get(EnumAkses.inhealth_mapping_tindakan_operasi);}
    public static boolean gethibah_obat_bhp() {return hakAkses.get(EnumAkses.hibah_obat_bhp);}
    public static boolean getasal_hibah() {return hakAkses.get(EnumAkses.asal_hibah);}
    public static boolean getasuhan_gizi() {return hakAkses.get(EnumAkses.asuhan_gizi);}
    public static boolean getinhealth_kirim_tagihan() {return hakAkses.get(EnumAkses.inhealth_kirim_tagihan);}
    public static boolean getsirkulasi_obat4() {return hakAkses.get(EnumAkses.sirkulasi_obat4);}
    public static boolean getsirkulasi_obat5() {return hakAkses.get(EnumAkses.sirkulasi_obat5);}
    public static boolean getsirkulasi_non_medis2() {return hakAkses.get(EnumAkses.sirkulasi_non_medis2);}
    public static boolean getmonitoring_asuhan_gizi() {return hakAkses.get(EnumAkses.monitoring_asuhan_gizi);}
    public static boolean getpenerimaan_obat_perbulan() {return hakAkses.get(EnumAkses.penerimaan_obat_perbulan);}
    public static boolean getrekap_kunjungan() {return hakAkses.get(EnumAkses.rekap_kunjungan);}
    public static boolean getsurat_sakit() {return hakAkses.get(EnumAkses.surat_sakit);}
    public static boolean getpenilaian_awal_keperawatan_ralan() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_ralan);}
    public static boolean getpermintaan_diet() {return hakAkses.get(EnumAkses.permintaan_diet);}
    public static boolean getmaster_masalah_keperawatan() {return hakAkses.get(EnumAkses.master_masalah_keperawatan);}
    public static boolean getpengajuan_cuti() {return hakAkses.get(EnumAkses.pengajuan_cuti);}
    public static boolean getkedatangan_pasien() {return hakAkses.get(EnumAkses.kedatangan_pasien);}
    public static boolean getutd_pendonor() {return hakAkses.get(EnumAkses.utd_pendonor);}
    public static boolean gettoko_suplier() {return hakAkses.get(EnumAkses.toko_suplier);}
    public static boolean gettoko_jenis() {return hakAkses.get(EnumAkses.toko_jenis);}
    public static boolean gettoko_set_harga() {return hakAkses.get(EnumAkses.toko_set_harga);}
    public static boolean gettoko_barang() {return hakAkses.get(EnumAkses.toko_barang);}
    public static boolean getpenagihan_piutang_pasien() {return hakAkses.get(EnumAkses.penagihan_piutang_pasien);}
    public static boolean getakun_penagihan_piutang() {return hakAkses.get(EnumAkses.akun_penagihan_piutang);}
    public static boolean getstok_opname_toko() {return hakAkses.get(EnumAkses.stok_opname_toko);}
    public static boolean gettoko_riwayat_barang() {return hakAkses.get(EnumAkses.toko_riwayat_barang);}
    public static boolean gettoko_surat_pemesanan() {return hakAkses.get(EnumAkses.toko_surat_pemesanan);}
    public static boolean gettoko_pengajuan_barang() {return hakAkses.get(EnumAkses.toko_pengajuan_barang);}
    public static boolean gettoko_penerimaan_barang() {return hakAkses.get(EnumAkses.toko_penerimaan_barang);}
    public static boolean gettoko_pengadaan_barang() {return hakAkses.get(EnumAkses.toko_pengadaan_barang);}
    public static boolean gettoko_hutang() {return hakAkses.get(EnumAkses.toko_hutang);}
    public static boolean gettoko_bayar_pemesanan() {return hakAkses.get(EnumAkses.toko_bayar_pemesanan);}
    public static boolean gettoko_member() {return hakAkses.get(EnumAkses.toko_member);}
    public static boolean gettoko_penjualan() {return hakAkses.get(EnumAkses.toko_penjualan);}
    public static boolean getregistrasi_poli_per_tanggal() {return hakAkses.get(EnumAkses.registrasi_poli_per_tanggal);}
    public static boolean gettoko_piutang() {return hakAkses.get(EnumAkses.toko_piutang);}
    public static boolean gettoko_retur_beli() {return hakAkses.get(EnumAkses.toko_retur_beli);}
    public static boolean getipsrs_returbeli() {return hakAkses.get(EnumAkses.ipsrs_returbeli);}
    public static boolean getipsrs_riwayat_barang() {return hakAkses.get(EnumAkses.ipsrs_riwayat_barang);}
    public static boolean getpasien_corona() {return hakAkses.get(EnumAkses.pasien_corona);}
    public static boolean gettoko_pendapatan_harian() {return hakAkses.get(EnumAkses.toko_pendapatan_harian);}
    public static boolean getdiagnosa_pasien_corona() {return hakAkses.get(EnumAkses.diagnosa_pasien_corona);}
    public static boolean getperawatan_pasien_corona() {return hakAkses.get(EnumAkses.perawatan_pasien_corona);}
    public static boolean getpenilaian_awal_keperawatan_gigi() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_gigi);}
    public static boolean getmaster_masalah_keperawatan_gigi() {return hakAkses.get(EnumAkses.master_masalah_keperawatan_gigi);}
    public static boolean gettoko_bayar_piutang() {return hakAkses.get(EnumAkses.toko_bayar_piutang);}
    public static boolean gettoko_piutang_harian() {return hakAkses.get(EnumAkses.toko_piutang_harian);}
    public static boolean gettoko_penjualan_harian() {return hakAkses.get(EnumAkses.toko_penjualan_harian);}
    public static boolean getdeteksi_corona() {return hakAkses.get(EnumAkses.deteksi_corona);}
    public static boolean getpenilaian_awal_keperawatan_kebidanan() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_kebidanan);}
    public static boolean getpengumuman_epasien() {return hakAkses.get(EnumAkses.pengumuman_epasien);}
    public static boolean getsurat_hamil() {return hakAkses.get(EnumAkses.surat_hamil);}
    public static boolean getset_tarif_online() {return hakAkses.get(EnumAkses.set_tarif_online);}
    public static boolean getbooking_periksa() {return hakAkses.get(EnumAkses.booking_periksa);}
    public static boolean gettoko_sirkulasi() {return hakAkses.get(EnumAkses.toko_sirkulasi);}
    public static boolean gettoko_retur_jual() {return hakAkses.get(EnumAkses.toko_retur_jual);}
    public static boolean gettoko_retur_piutang() {return hakAkses.get(EnumAkses.toko_retur_piutang);}
    public static boolean gettoko_sirkulasi2() {return hakAkses.get(EnumAkses.toko_sirkulasi2);}
    public static boolean gettoko_keuntungan_barang() {return hakAkses.get(EnumAkses.toko_keuntungan_barang);}
    public static boolean getzis_pengeluaran_penerima_dankes() {return hakAkses.get(EnumAkses.zis_pengeluaran_penerima_dankes);}
    public static boolean getzis_penghasilan_penerima_dankes() {return hakAkses.get(EnumAkses.zis_penghasilan_penerima_dankes);}
    public static boolean getzis_ukuran_rumah_penerima_dankes() {return hakAkses.get(EnumAkses.zis_ukuran_rumah_penerima_dankes);}
    public static boolean getzis_dinding_rumah_penerima_dankes() {return hakAkses.get(EnumAkses.zis_dinding_rumah_penerima_dankes);}
    public static boolean getzis_lantai_rumah_penerima_dankes() {return hakAkses.get(EnumAkses.zis_lantai_rumah_penerima_dankes);}
    public static boolean getzis_atap_rumah_penerima_dankes() {return hakAkses.get(EnumAkses.zis_atap_rumah_penerima_dankes);}
    public static boolean getzis_kepemilikan_rumah_penerima_dankes() {return hakAkses.get(EnumAkses.zis_kepemilikan_rumah_penerima_dankes);}
    public static boolean getzis_kamar_mandi_penerima_dankes() {return hakAkses.get(EnumAkses.zis_kamar_mandi_penerima_dankes);}
    public static boolean getzis_dapur_rumah_penerima_dankes() {return hakAkses.get(EnumAkses.zis_dapur_rumah_penerima_dankes);}
    public static boolean getzis_kursi_rumah_penerima_dankes() {return hakAkses.get(EnumAkses.zis_kursi_rumah_penerima_dankes);}
    public static boolean getzis_kategori_phbs_penerima_dankes() {return hakAkses.get(EnumAkses.zis_kategori_phbs_penerima_dankes);}
    public static boolean getzis_elektronik_penerima_dankes() {return hakAkses.get(EnumAkses.zis_elektronik_penerima_dankes);}
    public static boolean getzis_ternak_penerima_dankes() {return hakAkses.get(EnumAkses.zis_ternak_penerima_dankes);}
    public static boolean getzis_jenis_simpanan_penerima_dankes() {return hakAkses.get(EnumAkses.zis_jenis_simpanan_penerima_dankes);}
    public static boolean getpenilaian_awal_keperawatan_anak() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_anak);}
    public static boolean getzis_kategori_asnaf_penerima_dankes() {return hakAkses.get(EnumAkses.zis_kategori_asnaf_penerima_dankes);}
    public static boolean getmaster_masalah_keperawatan_anak() {return hakAkses.get(EnumAkses.master_masalah_keperawatan_anak);}
    public static boolean getmaster_imunisasi() {return hakAkses.get(EnumAkses.master_imunisasi);}
    public static boolean getzis_patologis_penerima_dankes() {return hakAkses.get(EnumAkses.zis_patologis_penerima_dankes);}
    public static boolean getpcare_cek_kartu() {return hakAkses.get(EnumAkses.pcare_cek_kartu);}
    public static boolean getsurat_bebas_narkoba() {return hakAkses.get(EnumAkses.surat_bebas_narkoba);}
    public static boolean getsurat_keterangan_covid() {return hakAkses.get(EnumAkses.surat_keterangan_covid);}
    public static boolean getpemakaian_air_tanah() {return hakAkses.get(EnumAkses.pemakaian_air_tanah);}
    public static boolean getgrafik_air_tanah_pertanggal() {return hakAkses.get(EnumAkses.grafik_air_tanah_pertanggal);}
    public static boolean getgrafik_air_tanah_perbulan() {return hakAkses.get(EnumAkses.grafik_air_tanah_perbulan);}
    public static boolean getlama_pelayanan_poli() {return hakAkses.get(EnumAkses.lama_pelayanan_poli);}
    public static boolean gethemodialisa() {return hakAkses.get(EnumAkses.hemodialisa);}
    public static boolean getgrafik_harian_hemodialisa() {return hakAkses.get(EnumAkses.grafik_harian_hemodialisa);}
    public static boolean getgrafik_bulanan_hemodialisa() {return hakAkses.get(EnumAkses.grafik_bulanan_hemodialisa);}
    public static boolean getgrafik_tahunan_hemodialisa() {return hakAkses.get(EnumAkses.grafik_tahunan_hemodialisa);}
    public static boolean getgrafik_bulanan_meninggal() {return hakAkses.get(EnumAkses.grafik_bulanan_meninggal);}
    public static boolean getlaporan_tahunan_irj() {return hakAkses.get(EnumAkses.laporan_tahunan_irj);}
    public static boolean getperbaikan_inventaris() {return hakAkses.get(EnumAkses.perbaikan_inventaris);}
    public static boolean getsurat_cuti_hamil() {return hakAkses.get(EnumAkses.surat_cuti_hamil);}
    public static boolean getpermintaan_stok_obat_pasien() {return hakAkses.get(EnumAkses.permintaan_stok_obat_pasien);}
    public static boolean getpemeliharaan_inventaris() {return hakAkses.get(EnumAkses.pemeliharaan_inventaris);}
    public static boolean getklasifikasi_pasien_ranap() {return hakAkses.get(EnumAkses.klasifikasi_pasien_ranap);}
    public static boolean getbulanan_klasifikasi_pasien_ranap() {return hakAkses.get(EnumAkses.bulanan_klasifikasi_pasien_ranap);}
    public static boolean getharian_klasifikasi_pasien_ranap() {return hakAkses.get(EnumAkses.harian_klasifikasi_pasien_ranap);}
    public static boolean getklasifikasi_pasien_perbangsal() {return hakAkses.get(EnumAkses.klasifikasi_pasien_perbangsal);}
    public static boolean getsoap_perawatan() {return hakAkses.get(EnumAkses.soap_perawatan);}
    public static boolean getklaim_rawat_jalan() {return hakAkses.get(EnumAkses.klaim_rawat_jalan);}
    public static boolean getskrining_gizi() {return hakAkses.get(EnumAkses.skrining_gizi);}
    public static boolean getlama_penyiapan_rm() {return hakAkses.get(EnumAkses.lama_penyiapan_rm);}
    public static boolean getdosis_radiologi() {return hakAkses.get(EnumAkses.dosis_radiologi);}
    public static boolean getdemografi_umur_kunjungan() {return hakAkses.get(EnumAkses.demografi_umur_kunjungan);}
    public static boolean getjam_diet_pasien() {return hakAkses.get(EnumAkses.jam_diet_pasien);}
    public static boolean getrvu_bpjs() {return hakAkses.get(EnumAkses.rvu_bpjs);}
    public static boolean getverifikasi_penerimaan_farmasi() {return hakAkses.get(EnumAkses.verifikasi_penerimaan_farmasi);}
    public static boolean getverifikasi_penerimaan_logistik() {return hakAkses.get(EnumAkses.verifikasi_penerimaan_logistik);}
    public static boolean getpemeriksaan_lab_pa() {return hakAkses.get(EnumAkses.pemeriksaan_lab_pa);}
    public static boolean getringkasan_pengajuan_obat() {return hakAkses.get(EnumAkses.ringkasan_pengajuan_obat);}
    public static boolean getringkasan_pemesanan_obat() {return hakAkses.get(EnumAkses.ringkasan_pemesanan_obat);}
    public static boolean getringkasan_pengadaan_obat() {return hakAkses.get(EnumAkses.ringkasan_pengadaan_obat);}
    public static boolean getringkasan_penerimaan_obat() {return hakAkses.get(EnumAkses.ringkasan_penerimaan_obat);}
    public static boolean getringkasan_hibah_obat() {return hakAkses.get(EnumAkses.ringkasan_hibah_obat);}
    public static boolean getringkasan_penjualan_obat() {return hakAkses.get(EnumAkses.ringkasan_penjualan_obat);}
    public static boolean getringkasan_beri_obat() {return hakAkses.get(EnumAkses.ringkasan_beri_obat);}
    public static boolean getringkasan_piutang_obat() {return hakAkses.get(EnumAkses.ringkasan_piutang_obat);}
    public static boolean getringkasan_stok_keluar_obat() {return hakAkses.get(EnumAkses.ringkasan_stok_keluar_obat);}
    public static boolean getringkasan_retur_suplier_obat() {return hakAkses.get(EnumAkses.ringkasan_retur_suplier_obat);}
    public static boolean getringkasan_retur_pembeli_obat() {return hakAkses.get(EnumAkses.ringkasan_retur_pembeli_obat);}
    public static boolean getpenilaian_awal_keperawatan_ranapkebidanan() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_ranapkebidanan);}
    public static boolean getringkasan_pengajuan_nonmedis() {return hakAkses.get(EnumAkses.ringkasan_pengajuan_nonmedis);}
    public static boolean getringkasan_pemesanan_nonmedis() {return hakAkses.get(EnumAkses.ringkasan_pemesanan_nonmedis);}
    public static boolean getringkasan_pengadaan_nonmedis() {return hakAkses.get(EnumAkses.ringkasan_pengadaan_nonmedis);}
    public static boolean getringkasan_penerimaan_nonmedis() {return hakAkses.get(EnumAkses.ringkasan_penerimaan_nonmedis);}
    public static boolean getringkasan_stokkeluar_nonmedis() {return hakAkses.get(EnumAkses.ringkasan_stokkeluar_nonmedis);}
    public static boolean getringkasan_returbeli_nonmedis() {return hakAkses.get(EnumAkses.ringkasan_returbeli_nonmedis);}
    public static boolean getomset_penerimaan() {return hakAkses.get(EnumAkses.omset_penerimaan);}
    public static boolean getvalidasi_penagihan_piutang() {return hakAkses.get(EnumAkses.validasi_penagihan_piutang);}
    public static boolean getpermintaan_ranap() {return hakAkses.get(EnumAkses.permintaan_ranap);}
    public static boolean getbpjs_diagnosa_prb() {return hakAkses.get(EnumAkses.bpjs_diagnosa_prb);}
    public static boolean getbpjs_obat_prb() {return hakAkses.get(EnumAkses.bpjs_obat_prb);}
    public static boolean getbpjs_surat_kontrol() {return hakAkses.get(EnumAkses.bpjs_surat_kontrol);}
    public static boolean getpenggunaan_bhp_ok() {return hakAkses.get(EnumAkses.penggunaan_bhp_ok);}
    public static boolean getsurat_keterangan_rawat_inap() {return hakAkses.get(EnumAkses.surat_keterangan_rawat_inap);}
    public static boolean getsurat_keterangan_sehat() {return hakAkses.get(EnumAkses.surat_keterangan_sehat);}
    public static boolean getpendapatan_per_carabayar() {return hakAkses.get(EnumAkses.pendapatan_per_carabayar);}
    public static boolean getakun_host_to_host_bank_jateng() {return hakAkses.get(EnumAkses.akun_host_to_host_bank_jateng);}
    public static boolean getpembayaran_bank_jateng() {return hakAkses.get(EnumAkses.pembayaran_bank_jateng);}
    public static boolean getbpjs_surat_pri() {return hakAkses.get(EnumAkses.bpjs_surat_pri);}
    public static boolean getringkasan_tindakan() {return hakAkses.get(EnumAkses.ringkasan_tindakan);}
    public static boolean getlama_pelayanan_pasien() {return hakAkses.get(EnumAkses.lama_pelayanan_pasien);}
    public static boolean getsurat_sakit_pihak_2() {return hakAkses.get(EnumAkses.surat_sakit_pihak_2);}
    public static boolean gettagihan_hutang_obat() {return hakAkses.get(EnumAkses.tagihan_hutang_obat);}
    public static boolean getreferensi_mobilejkn_bpjs() {return hakAkses.get(EnumAkses.referensi_mobilejkn_bpjs);}
    public static boolean getbatal_pendaftaran_mobilejkn_bpjs() {return hakAkses.get(EnumAkses.batal_pendaftaran_mobilejkn_bpjs);}
    public static boolean getlama_operasi() {return hakAkses.get(EnumAkses.lama_operasi);}
    public static boolean getgrafik_inventaris_kategori() {return hakAkses.get(EnumAkses.grafik_inventaris_kategori);}
    public static boolean getgrafik_inventaris_merk() {return hakAkses.get(EnumAkses.grafik_inventaris_merk);}
    public static boolean getgrafik_inventaris_produsen() {return hakAkses.get(EnumAkses.grafik_inventaris_produsen);}
    public static boolean getpengembalian_deposit_pasien() {return hakAkses.get(EnumAkses.pengembalian_deposit_pasien);}
    public static boolean getvalidasi_tagihan_hutang_obat() {return hakAkses.get(EnumAkses.validasi_tagihan_hutang_obat);}
    public static boolean getpiutang_obat_belum_lunas() {return hakAkses.get(EnumAkses.piutang_obat_belum_lunas);}
    public static boolean getintegrasi_briapi() {return hakAkses.get(EnumAkses.integrasi_briapi);}
    public static boolean getpengadaan_aset_inventaris() {return hakAkses.get(EnumAkses.pengadaan_aset_inventaris);}
    public static boolean getakun_aset_inventaris() {return hakAkses.get(EnumAkses.akun_aset_inventaris);}
    public static boolean getsuplier_inventaris() {return hakAkses.get(EnumAkses.suplier_inventaris);}
    public static boolean getpenerimaan_aset_inventaris() {return hakAkses.get(EnumAkses.penerimaan_aset_inventaris);}
    public static boolean getbayar_pemesanan_iventaris() {return hakAkses.get(EnumAkses.bayar_pemesanan_iventaris);}
    public static boolean gethutang_aset_inventaris() {return hakAkses.get(EnumAkses.hutang_aset_inventaris);}
    public static boolean gethibah_aset_inventaris() {return hakAkses.get(EnumAkses.hibah_aset_inventaris);}
    public static boolean gettitip_faktur_non_medis() {return hakAkses.get(EnumAkses.titip_faktur_non_medis);}
    public static boolean getvalidasi_tagihan_non_medis() {return hakAkses.get(EnumAkses.validasi_tagihan_non_medis);}
    public static boolean gettitip_faktur_aset() {return hakAkses.get(EnumAkses.titip_faktur_aset);}
    public static boolean getvalidasi_tagihan_aset() {return hakAkses.get(EnumAkses.validasi_tagihan_aset);}
    public static boolean gethibah_non_medis() {return hakAkses.get(EnumAkses.hibah_non_medis);}
    public static boolean getpcare_alasan_tacc() {return hakAkses.get(EnumAkses.pcare_alasan_tacc);}
    public static boolean getresep_luar() {return hakAkses.get(EnumAkses.resep_luar);}
    public static boolean getsurat_bebas_tbc() {return hakAkses.get(EnumAkses.surat_bebas_tbc);}
    public static boolean getsurat_buta_warna() {return hakAkses.get(EnumAkses.surat_buta_warna);}
    public static boolean getsurat_bebas_tato() {return hakAkses.get(EnumAkses.surat_bebas_tato);}
    public static boolean getsurat_kewaspadaan_kesehatan() {return hakAkses.get(EnumAkses.surat_kewaspadaan_kesehatan);}
    public static boolean getgrafik_porsidiet_pertanggal() {return hakAkses.get(EnumAkses.grafik_porsidiet_pertanggal);}
    public static boolean getgrafik_porsidiet_perbulan() {return hakAkses.get(EnumAkses.grafik_porsidiet_perbulan);}
    public static boolean getgrafik_porsidiet_pertahun() {return hakAkses.get(EnumAkses.grafik_porsidiet_pertahun);}
    public static boolean getgrafik_porsidiet_perbangsal() {return hakAkses.get(EnumAkses.grafik_porsidiet_perbangsal);}
    public static boolean getpenilaian_awal_medis_ralan() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan);}
    public static boolean getmaster_masalah_keperawatan_mata() {return hakAkses.get(EnumAkses.master_masalah_keperawatan_mata);}
    public static boolean getpenilaian_awal_keperawatan_mata() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_mata);}
    public static boolean getpenilaian_awal_medis_ranap() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ranap);}
    public static boolean getpenilaian_awal_medis_ranap_kebidanan() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ranap_kebidanan);}
    public static boolean getpenilaian_awal_medis_ralan_kebidanan() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_kebidanan);}
    public static boolean getpenilaian_awal_medis_igd() {return hakAkses.get(EnumAkses.penilaian_awal_medis_igd);}
    public static boolean getpenilaian_awal_medis_ralan_anak() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_anak);}
    public static boolean getbpjs_referensi_poli_hfis() {return hakAkses.get(EnumAkses.bpjs_referensi_poli_hfis);}
    public static boolean getbpjs_referensi_dokter_hfis() {return hakAkses.get(EnumAkses.bpjs_referensi_dokter_hfis);}
    public static boolean getbpjs_referensi_jadwal_hfis() {return hakAkses.get(EnumAkses.bpjs_referensi_jadwal_hfis);}
    public static boolean getpenilaian_fisioterapi() {return hakAkses.get(EnumAkses.penilaian_fisioterapi);}
    public static boolean getbpjs_program_prb() {return hakAkses.get(EnumAkses.bpjs_program_prb);}
    public static boolean getbpjs_suplesi_jasaraharja() {return hakAkses.get(EnumAkses.bpjs_suplesi_jasaraharja);}
    public static boolean getbpjs_data_induk_kecelakaan() {return hakAkses.get(EnumAkses.bpjs_data_induk_kecelakaan);}
    public static boolean getbpjs_sep_internal() {return hakAkses.get(EnumAkses.bpjs_sep_internal);}
    public static boolean getbpjs_klaim_jasa_raharja() {return hakAkses.get(EnumAkses.bpjs_klaim_jasa_raharja);}
    public static boolean getbpjs_daftar_finger_print() {return hakAkses.get(EnumAkses.bpjs_daftar_finger_print);}
    public static boolean getbpjs_rujukan_khusus() {return hakAkses.get(EnumAkses.bpjs_rujukan_khusus);}
    public static boolean getpemeliharaan_gedung() {return hakAkses.get(EnumAkses.pemeliharaan_gedung);}
    public static boolean getgrafik_perbaikan_inventaris_pertanggal() {return hakAkses.get(EnumAkses.grafik_perbaikan_inventaris_pertanggal);}
    public static boolean getgrafik_perbaikan_inventaris_perbulan() {return hakAkses.get(EnumAkses.grafik_perbaikan_inventaris_perbulan);}
    public static boolean getgrafik_perbaikan_inventaris_pertahun() {return hakAkses.get(EnumAkses.grafik_perbaikan_inventaris_pertahun);}
    public static boolean getgrafik_perbaikan_inventaris_perpelaksana_status() {return hakAkses.get(EnumAkses.grafik_perbaikan_inventaris_perpelaksana_status);}
    public static boolean getpenilaian_mcu() {return hakAkses.get(EnumAkses.penilaian_mcu);}
    public static boolean getpeminjam_piutang() {return hakAkses.get(EnumAkses.peminjam_piutang);}
    public static boolean getpiutang_lainlain() {return hakAkses.get(EnumAkses.piutang_lainlain);}
    public static boolean getcara_bayar() {return hakAkses.get(EnumAkses.cara_bayar);}
    public static boolean getaudit_kepatuhan_apd() {return hakAkses.get(EnumAkses.audit_kepatuhan_apd);}
    public static boolean getbpjs_task_id() {return hakAkses.get(EnumAkses.bpjs_task_id);}
    public static boolean getbayar_piutang_lain() {return hakAkses.get(EnumAkses.bayar_piutang_lain);}
    public static boolean getpembayaran_akun_bayar4() {return hakAkses.get(EnumAkses.pembayaran_akun_bayar4);}
    public static boolean getstok_akhir_farmasi_pertanggal() {return hakAkses.get(EnumAkses.stok_akhir_farmasi_pertanggal);}
    public static boolean getriwayat_kamar_pasien() {return hakAkses.get(EnumAkses.riwayat_kamar_pasien);}
    public static boolean getuji_fungsi_kfr() {return hakAkses.get(EnumAkses.uji_fungsi_kfr);}
    public static boolean gethapus_berkas_digital_perawatan() {return hakAkses.get(EnumAkses.hapus_berkas_digital_perawatan);}
    public static boolean getkategori_pengeluaran_harian() {return hakAkses.get(EnumAkses.kategori_pengeluaran_harian);}
    public static boolean getkategori_pemasukan_lain() {return hakAkses.get(EnumAkses.kategori_pemasukan_lain);}
    public static boolean getpembayaran_akun_bayar5() {return hakAkses.get(EnumAkses.pembayaran_akun_bayar5);}
    public static boolean getruang_ok() {return hakAkses.get(EnumAkses.ruang_ok);}
    public static boolean getjasa_tindakan_pasien() {return hakAkses.get(EnumAkses.jasa_tindakan_pasien);}
    public static boolean gettelaah_resep() {return hakAkses.get(EnumAkses.telaah_resep);}
    public static boolean getpermintaan_resep_pulang() {return hakAkses.get(EnumAkses.permintaan_resep_pulang);}
    public static boolean getrekap_jm_dokter() {return hakAkses.get(EnumAkses.rekap_jm_dokter);}
    public static boolean getstatus_data_rm() {return hakAkses.get(EnumAkses.status_data_rm);}
    public static boolean getubah_petugas_lab_pk() {return hakAkses.get(EnumAkses.ubah_petugas_lab_pk);}
    public static boolean getubah_petugas_lab_pa() {return hakAkses.get(EnumAkses.ubah_petugas_lab_pa);}
    public static boolean getubah_petugas_radiologi() {return hakAkses.get(EnumAkses.ubah_petugas_radiologi);}
    public static boolean getgabung_norawat() {return hakAkses.get(EnumAkses.gabung_norawat);}
    public static boolean getgabung_rm() {return hakAkses.get(EnumAkses.gabung_rm);}
    public static boolean getringkasan_biaya_obat_pasien_pertanggal() {return hakAkses.get(EnumAkses.ringkasan_biaya_obat_pasien_pertanggal);}
    public static boolean getmaster_masalah_keperawatan_igd() {return hakAkses.get(EnumAkses.master_masalah_keperawatan_igd);}
    public static boolean getpenilaian_awal_keperawatan_igd() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_igd);}
    public static boolean getbpjs_referensi_dpho_apotek() {return hakAkses.get(EnumAkses.bpjs_referensi_dpho_apotek);}
    public static boolean getbpjs_referensi_poli_apotek() {return hakAkses.get(EnumAkses.bpjs_referensi_poli_apotek);}
    public static boolean getbayar_jm_dokter() {return hakAkses.get(EnumAkses.bayar_jm_dokter);}
    public static boolean getbpjs_referensi_faskes_apotek() {return hakAkses.get(EnumAkses.bpjs_referensi_faskes_apotek);}
    public static boolean getbpjs_referensi_spesialistik_apotek() {return hakAkses.get(EnumAkses.bpjs_referensi_spesialistik_apotek);}
    public static boolean getpembayaran_briva() {return hakAkses.get(EnumAkses.pembayaran_briva);}
    public static boolean getpenilaian_awal_keperawatan_ranap() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_ranap);}
    public static boolean getnilai_penerimaan_vendor_farmasi_perbulan() {return hakAkses.get(EnumAkses.nilai_penerimaan_vendor_farmasi_perbulan);}
    public static boolean getakun_bayar_hutang() {return hakAkses.get(EnumAkses.akun_bayar_hutang);}
    public static boolean getmaster_rencana_keperawatan() {return hakAkses.get(EnumAkses.master_rencana_keperawatan);}
    public static boolean getlaporan_tahunan_igd() {return hakAkses.get(EnumAkses.laporan_tahunan_igd);}
    public static boolean getobat_bhp_tidakbergerak() {return hakAkses.get(EnumAkses.obat_bhp_tidakbergerak);}
    public static boolean getringkasan_hutang_vendor_farmasi() {return hakAkses.get(EnumAkses.ringkasan_hutang_vendor_farmasi);}
    public static boolean getnilai_penerimaan_vendor_nonmedis_perbulan() {return hakAkses.get(EnumAkses.nilai_penerimaan_vendor_nonmedis_perbulan);}
    public static boolean getringkasan_hutang_vendor_nonmedis() {return hakAkses.get(EnumAkses.ringkasan_hutang_vendor_nonmedis);}
    public static boolean getanggota_polri_dirawat() {return hakAkses.get(EnumAkses.anggota_polri_dirawat);}
    public static boolean getdaftar_pasien_ranap_polri() {return hakAkses.get(EnumAkses.daftar_pasien_ranap_polri);}
    public static boolean getsoap_ralan_polri() {return hakAkses.get(EnumAkses.soap_ralan_polri);}
    public static boolean getsoap_ranap_polri() {return hakAkses.get(EnumAkses.soap_ranap_polri);}
    public static boolean getlaporan_penyakit_polri() {return hakAkses.get(EnumAkses.laporan_penyakit_polri);}
    public static boolean getmaster_rencana_keperawatan_anak() {return hakAkses.get(EnumAkses.master_rencana_keperawatan_anak);}
    public static boolean getjumlah_pengunjung_ralan_polri() {return hakAkses.get(EnumAkses.jumlah_pengunjung_ralan_polri);}
    public static boolean getcatatan_observasi_igd() {return hakAkses.get(EnumAkses.catatan_observasi_igd);}
    public static boolean getcatatan_observasi_ranap() {return hakAkses.get(EnumAkses.catatan_observasi_ranap);}
    public static boolean getcatatan_observasi_ranap_kebidanan() {return hakAkses.get(EnumAkses.catatan_observasi_ranap_kebidanan);}
    public static boolean getcatatan_observasi_ranap_postpartum() {return hakAkses.get(EnumAkses.catatan_observasi_ranap_postpartum);}
    public static boolean getpenilaian_awal_medis_ralan_tht() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_tht);}
    public static boolean getpenilaian_psikologi() {return hakAkses.get(EnumAkses.penilaian_psikologi);}
    public static boolean getaudit_cuci_tangan_medis() {return hakAkses.get(EnumAkses.audit_cuci_tangan_medis);}
    public static boolean getaudit_pembuangan_limbah() {return hakAkses.get(EnumAkses.audit_pembuangan_limbah);}
    public static boolean getruang_audit_kepatuhan() {return hakAkses.get(EnumAkses.ruang_audit_kepatuhan);}
    public static boolean getaudit_pembuangan_benda_tajam() {return hakAkses.get(EnumAkses.audit_pembuangan_benda_tajam);}
    public static boolean getaudit_penanganan_darah() {return hakAkses.get(EnumAkses.audit_penanganan_darah);}
    public static boolean getaudit_pengelolaan_linen_kotor() {return hakAkses.get(EnumAkses.audit_pengelolaan_linen_kotor);}
    public static boolean getaudit_penempatan_pasien() {return hakAkses.get(EnumAkses.audit_penempatan_pasien);}
    public static boolean getaudit_kamar_jenazah() {return hakAkses.get(EnumAkses.audit_kamar_jenazah);}
    public static boolean getaudit_bundle_iadp() {return hakAkses.get(EnumAkses.audit_bundle_iadp);}
    public static boolean getaudit_bundle_ido() {return hakAkses.get(EnumAkses.audit_bundle_ido);}
    public static boolean getaudit_fasilitas_kebersihan_tangan() {return hakAkses.get(EnumAkses.audit_fasilitas_kebersihan_tangan);}
    public static boolean getaudit_fasilitas_apd() {return hakAkses.get(EnumAkses.audit_fasilitas_apd);}
    public static boolean getaudit_pembuangan_limbah_cair_infeksius() {return hakAkses.get(EnumAkses.audit_pembuangan_limbah_cair_infeksius);}
    public static boolean getaudit_sterilisasi_alat() {return hakAkses.get(EnumAkses.audit_sterilisasi_alat);}
    public static boolean getpenilaian_awal_medis_ralan_psikiatri() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_psikiatri);}
    public static boolean getpersetujuan_penolakan_tindakan() {return hakAkses.get(EnumAkses.persetujuan_penolakan_tindakan);}
    public static boolean getaudit_bundle_isk() {return hakAkses.get(EnumAkses.audit_bundle_isk);}
    public static boolean getaudit_bundle_plabsi() {return hakAkses.get(EnumAkses.audit_bundle_plabsi);}
    public static boolean getaudit_bundle_vap() {return hakAkses.get(EnumAkses.audit_bundle_vap);}
    public static boolean getakun_host_to_host_bank_papua() {return hakAkses.get(EnumAkses.akun_host_to_host_bank_papua);}
    public static boolean getpembayaran_bank_papua() {return hakAkses.get(EnumAkses.pembayaran_bank_papua);}
    public static boolean getpenilaian_awal_medis_ralan_penyakit_dalam() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_penyakit_dalam);}
    public static boolean getpenilaian_awal_medis_ralan_mata() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_mata);}
    public static boolean getpenilaian_awal_medis_ralan_neurologi() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_neurologi);}
    public static boolean getsirkulasi_obat6() {return hakAkses.get(EnumAkses.sirkulasi_obat6);}
    public static boolean getpenilaian_awal_medis_ralan_orthopedi() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_orthopedi);}
    public static boolean getpenilaian_awal_medis_ralan_bedah() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_bedah);}
    public static boolean getintegrasi_khanza_health_services() {return hakAkses.get(EnumAkses.integrasi_khanza_health_services);}
    public static boolean getsoap_ralan_tni() {return hakAkses.get(EnumAkses.soap_ralan_tni);}
    public static boolean getsoap_ranap_tni() {return hakAkses.get(EnumAkses.soap_ranap_tni);}
    public static boolean getjumlah_pengunjung_ralan_tni() {return hakAkses.get(EnumAkses.jumlah_pengunjung_ralan_tni);}
    public static boolean getlaporan_penyakit_tni() {return hakAkses.get(EnumAkses.laporan_penyakit_tni);}
    public static boolean getcatatan_keperawatan_ranap() {return hakAkses.get(EnumAkses.catatan_keperawatan_ranap);}
    public static boolean getmaster_rencana_keperawatan_gigi() {return hakAkses.get(EnumAkses.master_rencana_keperawatan_gigi);}
    public static boolean getmaster_rencana_keperawatan_mata() {return hakAkses.get(EnumAkses.master_rencana_keperawatan_mata);}
    public static boolean getmaster_rencana_keperawatan_igd() {return hakAkses.get(EnumAkses.master_rencana_keperawatan_igd);}
    public static boolean getmaster_masalah_keperawatan_psikiatri() {return hakAkses.get(EnumAkses.master_masalah_keperawatan_psikiatri);}
    public static boolean getmaster_rencana_keperawatan_psikiatri() {return hakAkses.get(EnumAkses.master_rencana_keperawatan_psikiatri);}
    public static boolean getpenilaian_awal_keperawatan_psikiatri() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_psikiatri);}
    public static boolean getpemantauan_pews_anak() {return hakAkses.get(EnumAkses.pemantauan_pews_anak);}
    public static boolean getsurat_pulang_atas_permintaan_sendiri() {return hakAkses.get(EnumAkses.surat_pulang_atas_permintaan_sendiri);}
    public static boolean gettemplate_hasil_radiologi() {return hakAkses.get(EnumAkses.template_hasil_radiologi);}
    public static boolean getlaporan_bulanan_irj() {return hakAkses.get(EnumAkses.laporan_bulanan_irj);}
    public static boolean gettemplate_pemeriksaan() {return hakAkses.get(EnumAkses.template_pemeriksaan);}
    public static boolean getpemeriksaan_lab_mb() {return hakAkses.get(EnumAkses.pemeriksaan_lab_mb);}
    public static boolean getubah_petugas_lab_mb() {return hakAkses.get(EnumAkses.ubah_petugas_lab_mb);}
    public static boolean getpenilaian_pre_operasi() {return hakAkses.get(EnumAkses.penilaian_pre_operasi);}
    public static boolean getpenilaian_pre_anestesi() {return hakAkses.get(EnumAkses.penilaian_pre_anestesi);}
    public static boolean getperencanaan_pemulangan() {return hakAkses.get(EnumAkses.perencanaan_pemulangan);}
    public static boolean getpenilaian_lanjutan_resiko_jatuh_dewasa() {return hakAkses.get(EnumAkses.penilaian_lanjutan_resiko_jatuh_dewasa);}
    public static boolean getpenilaian_lanjutan_resiko_jatuh_anak() {return hakAkses.get(EnumAkses.penilaian_lanjutan_resiko_jatuh_anak);}
    public static boolean getpenilaian_awal_medis_ralan_geriatri() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_geriatri);}
    public static boolean getpenilaian_tambahan_pasien_geriatri() {return hakAkses.get(EnumAkses.penilaian_tambahan_pasien_geriatri);}
    public static boolean getskrining_nutrisi_dewasa() {return hakAkses.get(EnumAkses.skrining_nutrisi_dewasa);}
    public static boolean getskrining_nutrisi_lansia() {return hakAkses.get(EnumAkses.skrining_nutrisi_lansia);}
    public static boolean gethasil_pemeriksaan_usg() {return hakAkses.get(EnumAkses.hasil_pemeriksaan_usg);}
    public static boolean getskrining_nutrisi_anak() {return hakAkses.get(EnumAkses.skrining_nutrisi_anak);}
    public static boolean getakun_host_to_host_bank_jabar() {return hakAkses.get(EnumAkses.akun_host_to_host_bank_jabar);}
    public static boolean getpembayaran_bank_jabar() {return hakAkses.get(EnumAkses.pembayaran_bank_jabar);}
    public static boolean getsurat_pernyataan_pasien_umum() {return hakAkses.get(EnumAkses.surat_pernyataan_pasien_umum);}
    public static boolean getkonseling_farmasi() {return hakAkses.get(EnumAkses.konseling_farmasi);}
    public static boolean getpelayanan_informasi_obat() {return hakAkses.get(EnumAkses.pelayanan_informasi_obat);}
    public static boolean getjawaban_pio_apoteker() {return hakAkses.get(EnumAkses.jawaban_pio_apoteker);}
    public static boolean getsurat_persetujuan_umum() {return hakAkses.get(EnumAkses.surat_persetujuan_umum);}
    public static boolean gettransfer_pasien_antar_ruang() {return hakAkses.get(EnumAkses.transfer_pasien_antar_ruang);}
    public static boolean getsatu_sehat_referensi_dokter() {return hakAkses.get(EnumAkses.satu_sehat_referensi_dokter);}
    public static boolean getsatu_sehat_referensi_pasien() {return hakAkses.get(EnumAkses.satu_sehat_referensi_pasien);}
    public static boolean getsatu_sehat_mapping_departemen() {return hakAkses.get(EnumAkses.satu_sehat_mapping_departemen);}
    public static boolean getsatu_sehat_mapping_lokasi() {return hakAkses.get(EnumAkses.satu_sehat_mapping_lokasi);}
    public static boolean getsatu_sehat_kirim_encounter() {return hakAkses.get(EnumAkses.satu_sehat_kirim_encounter);}
    public static boolean getsatu_sehat_kirim_episodeofcare() {return hakAkses.get(EnumAkses.satu_sehat_kirim_episodeofcare);}
    public static boolean getcatatan_cek_gds() {return hakAkses.get(EnumAkses.catatan_cek_gds);}
    public static boolean getsatu_sehat_kirim_condition() {return hakAkses.get(EnumAkses.satu_sehat_kirim_condition);}
    public static boolean getchecklist_pre_operasi() {return hakAkses.get(EnumAkses.checklist_pre_operasi);}
    public static boolean getsatu_sehat_kirim_observationttv() {return hakAkses.get(EnumAkses.satu_sehat_kirim_observationttv);}
    public static boolean getsignin_sebelum_anestesi() {return hakAkses.get(EnumAkses.signin_sebelum_anestesi);}
    public static boolean getsatu_sehat_kirim_procedure() {return hakAkses.get(EnumAkses.satu_sehat_kirim_procedure);}
    public static boolean getoperasi_per_bulan() {return hakAkses.get(EnumAkses.operasi_per_bulan);}
    public static boolean gettimeout_sebelum_insisi() {return hakAkses.get(EnumAkses.timeout_sebelum_insisi);}
    public static boolean getsignout_sebelum_menutup_luka() {return hakAkses.get(EnumAkses.signout_sebelum_menutup_luka);}
    public static boolean getdapur_barang() {return hakAkses.get(EnumAkses.dapur_barang);}
    public static boolean getdapur_opname() {return hakAkses.get(EnumAkses.dapur_opname);}
    public static boolean getsatu_sehat_mapping_vaksin() {return hakAkses.get(EnumAkses.satu_sehat_mapping_vaksin);}
    public static boolean getdapur_suplier() {return hakAkses.get(EnumAkses.dapur_suplier);}
    public static boolean getsatu_sehat_kirim_Immunization() {return hakAkses.get(EnumAkses.satu_sehat_kirim_Immunization);}
    public static boolean getchecklist_post_operasi() {return hakAkses.get(EnumAkses.checklist_post_operasi);}
    public static boolean getdapur_pembelian() {return hakAkses.get(EnumAkses.dapur_pembelian);}
    public static boolean getdapur_stok_keluar() {return hakAkses.get(EnumAkses.dapur_stok_keluar);}
    public static boolean getdapur_riwayat_barang() {return hakAkses.get(EnumAkses.dapur_riwayat_barang);}
    public static boolean getpermintaan_dapur() {return hakAkses.get(EnumAkses.permintaan_dapur);}
    public static boolean getrekonsiliasi_obat() {return hakAkses.get(EnumAkses.rekonsiliasi_obat);}
    public static boolean getbiaya_pengadaan_dapur() {return hakAkses.get(EnumAkses.biaya_pengadaan_dapur);}
    public static boolean getrekap_pengadaan_dapur() {return hakAkses.get(EnumAkses.rekap_pengadaan_dapur);}
    public static boolean getkesling_limbah_b3medis_cair() {return hakAkses.get(EnumAkses.kesling_limbah_b3medis_cair);}
    public static boolean getgrafik_limbahb3cair_pertanggal() {return hakAkses.get(EnumAkses.grafik_limbahb3cair_pertanggal);}
    public static boolean getgrafik_limbahb3cair_perbulan() {return hakAkses.get(EnumAkses.grafik_limbahb3cair_perbulan);}
    public static boolean getrekap_biaya_registrasi() {return hakAkses.get(EnumAkses.rekap_biaya_registrasi);}
    public static boolean getkonfirmasi_rekonsiliasi_obat() {return hakAkses.get(EnumAkses.konfirmasi_rekonsiliasi_obat);}
    public static boolean getsatu_sehat_kirim_clinicalimpression() {return hakAkses.get(EnumAkses.satu_sehat_kirim_clinicalimpression);}
    public static boolean getpenilaian_pasien_terminal() {return hakAkses.get(EnumAkses.penilaian_pasien_terminal);}
    public static boolean getsurat_persetujuan_rawat_inap() {return hakAkses.get(EnumAkses.surat_persetujuan_rawat_inap);}
    public static boolean getmonitoring_reaksi_tranfusi() {return hakAkses.get(EnumAkses.monitoring_reaksi_tranfusi);}
    public static boolean getpenilaian_korban_kekerasan() {return hakAkses.get(EnumAkses.penilaian_korban_kekerasan);}
    public static boolean getpenilaian_lanjutan_resiko_jatuh_lansia() {return hakAkses.get(EnumAkses.penilaian_lanjutan_resiko_jatuh_lansia);}
    public static boolean getmpp_skrining() {return hakAkses.get(EnumAkses.mpp_skrining);}
    public static boolean getpenilaian_pasien_penyakit_menular() {return hakAkses.get(EnumAkses.penilaian_pasien_penyakit_menular);}
    public static boolean getedukasi_pasien_keluarga_rj() {return hakAkses.get(EnumAkses.edukasi_pasien_keluarga_rj);}
    public static boolean getpemantauan_pews_dewasa() {return hakAkses.get(EnumAkses.pemantauan_pews_dewasa);}
    public static boolean getpenilaian_tambahan_bunuh_diri() {return hakAkses.get(EnumAkses.penilaian_tambahan_bunuh_diri);}
    public static boolean getbpjs_antrean_pertanggal() {return hakAkses.get(EnumAkses.bpjs_antrean_pertanggal);}
    public static boolean getpenilaian_tambahan_perilaku_kekerasan() {return hakAkses.get(EnumAkses.penilaian_tambahan_perilaku_kekerasan);}
    public static boolean getpenilaian_tambahan_beresiko_melarikan_diri() {return hakAkses.get(EnumAkses.penilaian_tambahan_beresiko_melarikan_diri);}
    public static boolean getpersetujuan_penundaan_pelayanan() {return hakAkses.get(EnumAkses.persetujuan_penundaan_pelayanan);}
    public static boolean getsisa_diet_pasien() {return hakAkses.get(EnumAkses.sisa_diet_pasien);}
    public static boolean getpenilaian_awal_medis_ralan_bedah_mulut() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_bedah_mulut);}
    public static boolean getpenilaian_pasien_keracunan() {return hakAkses.get(EnumAkses.penilaian_pasien_keracunan);}
    public static boolean getpemantauan_meows_obstetri() {return hakAkses.get(EnumAkses.pemantauan_meows_obstetri);}
    public static boolean getcatatan_adime_gizi() {return hakAkses.get(EnumAkses.catatan_adime_gizi);}
    public static boolean getpengajuan_biaya() {return hakAkses.get(EnumAkses.pengajuan_biaya);}
    public static boolean getpenilaian_awal_keperawatan_ralan_geriatri() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_ralan_geriatri);}
    public static boolean getmaster_masalah_keperawatan_geriatri() {return hakAkses.get(EnumAkses.master_masalah_keperawatan_geriatri);}
    public static boolean getmaster_rencana_keperawatan_geriatri() {return hakAkses.get(EnumAkses.master_rencana_keperawatan_geriatri);}
    public static boolean getchecklist_kriteria_masuk_hcu() {return hakAkses.get(EnumAkses.checklist_kriteria_masuk_hcu);}
    public static boolean getchecklist_kriteria_keluar_hcu() {return hakAkses.get(EnumAkses.checklist_kriteria_keluar_hcu);}
    public static boolean getpenilaian_risiko_dekubitus() {return hakAkses.get(EnumAkses.penilaian_risiko_dekubitus);}
    public static boolean getmaster_menolak_anjuran_medis() {return hakAkses.get(EnumAkses.master_menolak_anjuran_medis);}
    public static boolean getpenolakan_anjuran_medis() {return hakAkses.get(EnumAkses.penolakan_anjuran_medis);}
    public static boolean getlaporan_tahunan_penolakan_anjuran_medis() {return hakAkses.get(EnumAkses.laporan_tahunan_penolakan_anjuran_medis);}
    public static boolean gettemplate_laporan_operasi() {return hakAkses.get(EnumAkses.template_laporan_operasi);}
    public static boolean gethasil_tindakan_eswl() {return hakAkses.get(EnumAkses.hasil_tindakan_eswl);}
    public static boolean getchecklist_kriteria_masuk_icu() {return hakAkses.get(EnumAkses.checklist_kriteria_masuk_icu);}
    public static boolean getchecklist_kriteria_keluar_icu() {return hakAkses.get(EnumAkses.checklist_kriteria_keluar_icu);}
    public static boolean getakses_dokter_lain_rawat_jalan() {return hakAkses.get(EnumAkses.akses_dokter_lain_rawat_jalan);}
    public static boolean getfollow_up_dbd() {return hakAkses.get(EnumAkses.follow_up_dbd);}
    public static boolean getpenilaian_risiko_jatuh_neonatus() {return hakAkses.get(EnumAkses.penilaian_risiko_jatuh_neonatus);}
    public static boolean getpersetujuan_pengajuan_biaya() {return hakAkses.get(EnumAkses.persetujuan_pengajuan_biaya);}
    public static boolean getpemeriksaan_fisik_ralan_per_penyakit() {return hakAkses.get(EnumAkses.pemeriksaan_fisik_ralan_per_penyakit);}
    public static boolean getpenilaian_lanjutan_resiko_jatuh_geriatri() {return hakAkses.get(EnumAkses.penilaian_lanjutan_resiko_jatuh_geriatri);}
    public static boolean getpemantauan_ews_neonatus() {return hakAkses.get(EnumAkses.pemantauan_ews_neonatus);}
    public static boolean getvalidasi_persetujuan_pengajuan_biaya() {return hakAkses.get(EnumAkses.validasi_persetujuan_pengajuan_biaya);}
    public static boolean getriwayat_perawatan_icare_bpjs() {return hakAkses.get(EnumAkses.riwayat_perawatan_icare_bpjs);}
    public static boolean getrekap_pengajuan_biaya() {return hakAkses.get(EnumAkses.rekap_pengajuan_biaya);}
    public static boolean getpenilaian_awal_medis_ralan_kulit_kelamin() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_kulit_kelamin);}
    public static boolean getakun_host_to_host_bank_mandiri() {return hakAkses.get(EnumAkses.akun_host_to_host_bank_mandiri);}
    public static boolean getpenilaian_medis_ralan_hemodialisa() {return hakAkses.get(EnumAkses.penilaian_medis_hemodialisa);}
    public static boolean getpenilaian_level_kecemasan_ranap_anak() {return hakAkses.get(EnumAkses.penilaian_level_kecemasan_ranap_anak);}
    public static boolean getpenilaian_lanjutan_resiko_jatuh_psikiatri() {return hakAkses.get(EnumAkses.penilaian_lanjutan_resiko_jatuh_psikiatri);}
    public static boolean getpenilaian_lanjutan_skrining_fungsional() {return hakAkses.get(EnumAkses.penilaian_lanjutan_skrining_fungsional);}
    public static boolean getpenilaian_medis_ralan_rehab_medik() {return hakAkses.get(EnumAkses.penilaian_medis_ralan_rehab_medik);}
    public static boolean getlaporan_anastesi() {return hakAkses.get(EnumAkses.laporan_anestesi);}
    public static boolean gettemplate_persetujuan_penolakan_tindakan() {return hakAkses.get(EnumAkses.template_persetujuan_penolakan_tindakan);}
    public static boolean getpenilaian_medis_ralan_gawat_darurat_psikiatri() {return hakAkses.get(EnumAkses.penilaian_medis_ralan_gawat_darurat_psikiatri);}
    public static boolean getbpjs_referensi_setting_apotek() {return hakAkses.get(EnumAkses.bpjs_referensi_setting_apotek);}
    public static boolean getbpjs_referensi_obat_apotek() {return hakAkses.get(EnumAkses.bpjs_referensi_obat_apotek);}
    public static boolean getbpjs_mapping_obat_apotek() {return hakAkses.get(EnumAkses.bpjs_mapping_obat_apotek);}
    public static boolean getpembayaran_bank_mandiri() {return hakAkses.get(EnumAkses.pembayaran_bank_mandiri);}
    public static boolean getpenilaian_ulang_nyeri() {return hakAkses.get(EnumAkses.penilaian_ulang_nyeri);}
    public static boolean getpenilaian_terapi_wicara() {return hakAkses.get(EnumAkses.penilaian_terapi_wicara);}
    public static boolean getbpjs_obat_23hari_apotek() {return hakAkses.get(EnumAkses.bpjs_obat_23hari_apotek);}
    public static boolean getpengkajian_restrain() {return hakAkses.get(EnumAkses.pengkajian_restrain);}
    public static boolean getbpjs_kunjungan_sep_apotek() {return hakAkses.get(EnumAkses.bpjs_kunjungan_sep_apotek);}
    public static boolean getbpjs_monitoring_klaim_apotek() {return hakAkses.get(EnumAkses.bpjs_monitoring_klaim_apotek);}
    public static boolean getbpjs_daftar_pelayanan_obat_apotek() {return hakAkses.get(EnumAkses.bpjs_daftar_pelayanan_obat_apotek);}
    public static boolean getpenilaian_awal_medis_ralan_paru() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_paru);}
    public static boolean getcatatan_keperawatan_ralan() {return hakAkses.get(EnumAkses.catatan_keperawatan_ralan);}
    public static boolean getcatatan_persalinan() {return hakAkses.get(EnumAkses.catatan_persalinan);}
    public static boolean getskor_aldrette_pasca_anestesi() {return hakAkses.get(EnumAkses.skor_aldrette_pasca_anestesi);}
    public static boolean getskor_steward_pasca_anestesi() {return hakAkses.get(EnumAkses.skor_steward_pasca_anestesi);}
    public static boolean getskor_bromage_pasca_anestesi() {return hakAkses.get(EnumAkses.skor_bromage_pasca_anestesi);}
    public static boolean getpenilaian_pre_induksi() {return hakAkses.get(EnumAkses.penilaian_pre_induksi);}
    public static boolean gethasil_usg_urologi() {return hakAkses.get(EnumAkses.hasil_usg_urologi);}
    public static boolean gethasil_usg_gynecologi() {return hakAkses.get(EnumAkses.hasil_usg_gynecologi);}
    public static boolean gethasil_pemeriksaan_ekg() {return hakAkses.get(EnumAkses.hasil_pemeriksaan_ekg);}
    public static boolean gethapus_edit_sep_bpjs() {return hakAkses.get(EnumAkses.hapus_edit_sep_bpjs);}
    public static boolean getsatu_sehat_kirim_diet() {return hakAkses.get(EnumAkses.satu_sehat_kirim_diet);}
    public static boolean getsatu_sehat_mapping_obat() {return hakAkses.get(EnumAkses.satu_sehat_mapping_obat);}
    public static boolean getdapur_ringkasan_pembelian() {return hakAkses.get(EnumAkses.dapur_ringkasan_pembelian);}
    public static boolean getsatu_sehat_kirim_medication() {return hakAkses.get(EnumAkses.satu_sehat_kirim_medication);}
    public static boolean getsatu_sehat_kirim_medicationrequest() {return hakAkses.get(EnumAkses.satu_sehat_kirim_medicationrequest);}
    public static boolean getpenatalaksanaan_terapi_okupasi() {return hakAkses.get(EnumAkses.penatalaksanaan_terapi_okupasi);}
    public static boolean getsatu_sehat_kirim_medicationdispense() {return hakAkses.get(EnumAkses.satu_sehat_kirim_medicationdispense);}
    public static boolean gethasil_usg_neonatus() {return hakAkses.get(EnumAkses.hasil_usg_neonatus);}
    public static boolean gethasil_endoskopi_faring_laring() {return hakAkses.get(EnumAkses.hasil_endoskopi_faring_laring);}
    public static boolean getsatu_sehat_mapping_radiologi() {return hakAkses.get(EnumAkses.satu_sehat_mapping_radiologi);}
    public static boolean getsatu_sehat_kirim_servicerequest_radiologi() {return hakAkses.get(EnumAkses.satu_sehat_kirim_servicerequest_radiologi);}
    public static boolean gethasil_endoskopi_hidung() {return hakAkses.get(EnumAkses.hasil_endoskopi_hidung);}
    public static boolean getsatu_sehat_kirim_specimen_radiologi() {return hakAkses.get(EnumAkses.satu_sehat_kirim_specimen_radiologi);}
    public static boolean getmaster_masalah_keperawatan_neonatus() {return hakAkses.get(EnumAkses.master_masalah_keperawatan_neonatus);}
    public static boolean getmaster_rencana_keperawatan_neonatus() {return hakAkses.get(EnumAkses.master_rencana_keperawatan_neonatus);}
    public static boolean getpenilaian_awal_keperawatan_ranap_neonatus() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_ranap_neonatus);}
    public static boolean getsatu_sehat_kirim_observation_radiologi() {return hakAkses.get(EnumAkses.satu_sehat_kirim_observation_radiologi);}
    public static boolean getsatu_sehat_kirim_diagnosticreport_radiologi() {return hakAkses.get(EnumAkses.satu_sehat_kirim_diagnosticreport_radiologi);}
    public static boolean gethasil_endoskopi_telinga() {return hakAkses.get(EnumAkses.hasil_endoskopi_telinga);}
    public static boolean getsatu_sehat_mapping_lab() {return hakAkses.get(EnumAkses.satu_sehat_mapping_lab);}
    public static boolean getsatu_sehat_kirim_servicerequest_lab() {return hakAkses.get(EnumAkses.satu_sehat_kirim_servicerequest_lab);}
    public static boolean getsatu_sehat_kirim_servicerequest_labmb() {return hakAkses.get(EnumAkses.satu_sehat_kirim_servicerequest_labmb);}
    public static boolean getsatu_sehat_kirim_specimen_lab() {return hakAkses.get(EnumAkses.satu_sehat_kirim_specimen_lab);}
    public static boolean getsatu_sehat_kirim_specimen_labmb() {return hakAkses.get(EnumAkses.satu_sehat_kirim_specimen_labmb);}
    public static boolean getsatu_sehat_kirim_observation_lab() {return hakAkses.get(EnumAkses.satu_sehat_kirim_observation_lab);}
    public static boolean getsatu_sehat_kirim_observation_labmb() {return hakAkses.get(EnumAkses.satu_sehat_kirim_observation_labmb);}
    public static boolean getsatu_sehat_kirim_diagnosticreport_lab() {return hakAkses.get(EnumAkses.satu_sehat_kirim_diagnosticreport_lab);}
    public static boolean getsatu_sehat_kirim_diagnosticreport_labmb() {return hakAkses.get(EnumAkses.satu_sehat_kirim_diagnosticreport_labmb);}
    public static boolean getkepatuhan_kelengkapan_keselamatan_bedah() {return hakAkses.get(EnumAkses.kepatuhan_kelengkapan_keselamatan_bedah);}
    public static boolean getnilai_piutang_perjenis_bayar_per_bulan() {return hakAkses.get(EnumAkses.nilai_piutang_perjenis_bayar_per_bulan);}
    public static boolean getringkasan_piutang_jenis_bayar() {return hakAkses.get(EnumAkses.ringkasan_piutang_jenis_bayar);}
    public static boolean getpenilaian_pasien_imunitas_rendah() {return hakAkses.get(EnumAkses.penilaian_pasien_imunitas_rendah);}
    public static boolean getbalance_cairan() {return hakAkses.get(EnumAkses.balance_cairan);}
    public static boolean getcatatan_observasi_chbp() {return hakAkses.get(EnumAkses.catatan_observasi_chbp);}
    public static boolean getcatatan_observasi_induksi_persalinan() {return hakAkses.get(EnumAkses.catatan_observasi_induksi_persalinan);}
    public static boolean getskp_kategori_penilaian() {return hakAkses.get(EnumAkses.skp_kategori_penilaian);}
    public static boolean getskp_kriteria_penilaian() {return hakAkses.get(EnumAkses.skp_kriteria_penilaian);}
    public static boolean getskp_penilaian() {return hakAkses.get(EnumAkses.skp_penilaian);}
    public static boolean getreferensi_poli_mobilejknfktp() {return hakAkses.get(EnumAkses.referensi_poli_mobilejknfktp);}
    public static boolean getreferensi_dokter_mobilejknfktp() {return hakAkses.get(EnumAkses.referensi_dokter_mobilejknfktp);}
    public static boolean getskp_rekapitulasi_penilaian() {return hakAkses.get(EnumAkses.skp_rekapitulasi_penilaian);}
    public static boolean getpembayaran_pihak_ke3_bankmandiri() {return hakAkses.get(EnumAkses.pembayaran_pihak_ke3_bankmandiri);}
    public static boolean getmetode_pembayaran_bankmandiri() {return hakAkses.get(EnumAkses.metode_pembayaran_bankmandiri);}
    public static boolean getbank_tujuan_transfer_bankmandiri() {return hakAkses.get(EnumAkses.bank_tujuan_transfer_bankmandiri);}
    public static boolean getkodetransaksi_tujuan_transfer_bankmandiri() {return hakAkses.get(EnumAkses.kodetransaksi_tujuan_transfer_bankmandiri);}
    public static boolean getkonsultasi_medik() {return hakAkses.get(EnumAkses.konsultasi_medik);}
    public static boolean getjawaban_konsultasi_medik() {return hakAkses.get(EnumAkses.jawaban_konsultasi_medik);}
    public static boolean getpcare_cek_alergi() {return hakAkses.get(EnumAkses.pcare_cek_alergi);}
    public static boolean getpcare_cek_prognosa() {return hakAkses.get(EnumAkses.pcare_cek_prognosa);}
    public static boolean getdata_sasaran_usiaproduktif() {return hakAkses.get(EnumAkses.data_sasaran_usiaproduktif);}
    public static boolean getdata_sasaran_usialansia() {return hakAkses.get(EnumAkses.data_sasaran_usialansia);}
    public static boolean getskrining_perilaku_merokok_sekolah_remaja() {return hakAkses.get(EnumAkses.skrining_perilaku_merokok_sekolah_remaja);}
    public static boolean getskrining_kekerasan_pada_perempuan() {return hakAkses.get(EnumAkses.skrining_kekerasan_pada_perempuan);}
    public static boolean getskrining_obesitas() {return hakAkses.get(EnumAkses.skrining_obesitas);}
    public static boolean getskrining_risiko_kanker_payudara() {return hakAkses.get(EnumAkses.skrining_risiko_kanker_payudara);}
    public static boolean getskrining_risiko_kanker_paru() {return hakAkses.get(EnumAkses.skrining_risiko_kanker_paru);}
    public static boolean getskrining_tbc() {return hakAkses.get(EnumAkses.skrining_tbc);}
    public static boolean getskrining_kesehatan_gigi_mulut_remaja() {return hakAkses.get(EnumAkses.skrining_kesehatan_gigi_mulut_remaja);}
    public static boolean getpenilaian_awal_keperawatan_ranap_bayi() {return hakAkses.get(EnumAkses.penilaian_awal_keperawatan_ranap_bayi);}
    public static boolean getbooking_mcu_perusahaan() {return hakAkses.get(EnumAkses.booking_mcu_perusahaan);}
    public static boolean getcatatan_observasi_restrain_nonfarma() {return hakAkses.get(EnumAkses.catatan_observasi_restrain_nonfarma);}
    public static boolean getcatatan_observasi_ventilator() {return hakAkses.get(EnumAkses.catatan_observasi_ventilator);}
    public static boolean getcatatan_anestesi_sedasi() {return hakAkses.get(EnumAkses.catatan_anestesi_sedasi);}
    public static boolean getskrining_puma() {return hakAkses.get(EnumAkses.skrining_puma);}
    public static boolean getsatu_sehat_kirim_careplan() {return hakAkses.get(EnumAkses.satu_sehat_kirim_careplan);}
    public static boolean getsatu_sehat_kirim_medicationstatement() {return hakAkses.get(EnumAkses.satu_sehat_kirim_medicationstatement);}
    public static boolean getskrining_adiksi_nikotin() {return hakAkses.get(EnumAkses.skrining_adiksi_nikotin);}
    public static boolean getskrining_thalassemia() {return hakAkses.get(EnumAkses.skrining_thalassemia);}
    public static boolean getskrining_instrumen_sdq() {return hakAkses.get(EnumAkses.skrining_instrumen_sdq);}
    public static boolean getskrining_instrumen_srq() {return hakAkses.get(EnumAkses.skrining_instrumen_srq);}
    public static boolean getchecklist_pemberian_fibrinolitik() {return hakAkses.get(EnumAkses.checklist_pemberian_fibrinolitik);}
    public static boolean getskrining_kanker_kolorektal() {return hakAkses.get(EnumAkses.skrining_kanker_kolorektal);}
    public static boolean getdapur_pemesanan() {return hakAkses.get(EnumAkses.dapur_pemesanan);}
    public static boolean getbayar_pesan_dapur() {return hakAkses.get(EnumAkses.bayar_pesan_dapur);}
    public static boolean gethutang_dapur() {return hakAkses.get(EnumAkses.hutang_dapur);}
    public static boolean gettitip_faktur_dapur() {return hakAkses.get(EnumAkses.titip_faktur_dapur);}
    public static boolean getvalidasi_tagihan_dapur() {return hakAkses.get(EnumAkses.validasi_tagihan_dapur);}
    public static boolean getsurat_pemesanan_dapur() {return hakAkses.get(EnumAkses.surat_pemesanan_dapur);}
    public static boolean getpengajuan_barang_dapur() {return hakAkses.get(EnumAkses.pengajuan_barang_dapur);}
    public static boolean getdapur_returbeli() {return hakAkses.get(EnumAkses.dapur_returbeli);}
    public static boolean gethibah_dapur() {return hakAkses.get(EnumAkses.hibah_dapur);}
    public static boolean getringkasan_penerimaan_dapur() {return hakAkses.get(EnumAkses.ringkasan_penerimaan_dapur);}
    public static boolean getringkasan_pengajuan_dapur() {return hakAkses.get(EnumAkses.ringkasan_pengajuan_dapur);}
    public static boolean getringkasan_pemesanan_dapur() {return hakAkses.get(EnumAkses.ringkasan_pemesanan_dapur);}
    public static boolean getringkasan_returbeli_dapur() {return hakAkses.get(EnumAkses.ringkasan_returbeli_dapur);}
    public static boolean getringkasan_stokkeluar_dapur() {return hakAkses.get(EnumAkses.ringkasan_stokkeluar_dapur);}
    public static boolean getdapur_stokkeluar_pertanggal() {return hakAkses.get(EnumAkses.dapur_stokkeluar_pertanggal);}
    public static boolean getsirkulasi_dapur() {return hakAkses.get(EnumAkses.sirkulasi_dapur);}
    public static boolean getsirkulasi_dapur2() {return hakAkses.get(EnumAkses.sirkulasi_dapur2);}
    public static boolean getverifikasi_penerimaan_dapur() {return hakAkses.get(EnumAkses.verifikasi_penerimaan_dapur);}
    public static boolean getnilai_penerimaan_vendor_dapur_perbulan() {return hakAkses.get(EnumAkses.nilai_penerimaan_vendor_dapur_perbulan);}
    public static boolean getringkasan_hutang_vendor_dapur() {return hakAkses.get(EnumAkses.ringkasan_hutang_vendor_dapur);}
    public static boolean getpenilaian_psikologi_klinis() {return hakAkses.get(EnumAkses.penilaian_psikologi_klinis);}
    public static boolean getpenilaian_awal_medis_ranap_neonatus() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ranap_neonatus);}
    public static boolean getpenilaian_derajat_dehidrasi() {return hakAkses.get(EnumAkses.penilaian_derajat_dehidrasi);}
    public static boolean getringkasan_jasa_tindakan_medis() {return hakAkses.get(EnumAkses.ringkasan_jasa_tindakan_medis);}
    public static boolean getpendapatan_per_akun() {return hakAkses.get(EnumAkses.pendapatan_per_akun);}
    public static boolean gethasil_pemeriksaan_echo() {return hakAkses.get(EnumAkses.hasil_pemeriksaan_echo);}
    public static boolean getpenilaian_bayi_baru_lahir() {return hakAkses.get(EnumAkses.penilaian_bayi_baru_lahir);}
    public static boolean getrl1_3_ketersediaan_kamar() {return hakAkses.get(EnumAkses.rl1_3_ketersediaan_kamar);}
    public static boolean getpendapatan_per_akun_closing() {return hakAkses.get(EnumAkses.pendapatan_per_akun_closing);}
    public static boolean getpengeluaran_pengeluaran() {return hakAkses.get(EnumAkses.pengeluaran_pengeluaran);}
    public static boolean getskrining_diabetes_melitus() {return hakAkses.get(EnumAkses.skrining_diabetes_melitus);}
    public static boolean getlaporan_tindakan() {return hakAkses.get(EnumAkses.laporan_tindakan);}
    public static boolean getpelaksanaan_informasi_edukasi() {return hakAkses.get(EnumAkses.pelaksanaan_informasi_edukasi);}
    public static boolean getlayanan_kedokteran_fisik_rehabilitasi() {return hakAkses.get(EnumAkses.layanan_kedokteran_fisik_rehabilitasi);}
    public static boolean getskrining_kesehatan_gigi_mulut_balita() {return hakAkses.get(EnumAkses.skrining_kesehatan_gigi_mulut_balita);}
    public static boolean getskrining_anemia() {return hakAkses.get(EnumAkses.skrining_anemia);}
    public static boolean getlayanan_program_kfr() {return hakAkses.get(EnumAkses.layanan_program_kfr);}
    public static boolean getskrining_hipertensi() {return hakAkses.get(EnumAkses.skrining_hipertensi);}
    public static boolean getskrining_kesehatan_penglihatan() {return hakAkses.get(EnumAkses.skrining_kesehatan_penglihatan);}
    public static boolean getcatatan_observasi_hemodialisa() {return hakAkses.get(EnumAkses.catatan_observasi_hemodialisa);}
    public static boolean getskrining_kesehatan_gigi_mulut_dewasa() {return hakAkses.get(EnumAkses.skrining_kesehatan_gigi_mulut_dewasa);}
    public static boolean getskrining_risiko_kanker_serviks() {return hakAkses.get(EnumAkses.skrining_risiko_kanker_serviks);}
    public static boolean getcatatan_cairan_hemodialisa() {return hakAkses.get(EnumAkses.catatan_cairan_hemodialisa);}
    public static boolean getskrining_kesehatan_gigi_mulut_lansia() {return hakAkses.get(EnumAkses.skrining_kesehatan_gigi_mulut_lansia);}
    public static boolean getskrining_indra_pendengaran() {return hakAkses.get(EnumAkses.skrining_indra_pendengaran);}
    public static boolean getcatatan_pengkajian_paska_operasi() {return hakAkses.get(EnumAkses.catatan_pengkajian_paska_operasi);}
    public static boolean getskrining_frailty_syndrome() {return hakAkses.get(EnumAkses.skrining_frailty_syndrome);}
    public static boolean getsirkulasi_cssd() {return hakAkses.get(EnumAkses.sirkulasi_cssd);}
    public static boolean getlama_pelayanan_cssd() {return hakAkses.get(EnumAkses.lama_pelayanan_cssd);}
    public static boolean getcatatan_observasi_bayi() {return hakAkses.get(EnumAkses.catatan_observasi_bayi);}
    public static boolean getriwayat_surat_peringatan() {return hakAkses.get(EnumAkses.riwayat_surat_peringatan);}
    public static boolean getmaster_kesimpulan_anjuran_mcu() {return hakAkses.get(EnumAkses.master_kesimpulan_anjuran_mcu);}
    public static boolean getkategori_piutang_jasa_perusahaan() {return hakAkses.get(EnumAkses.kategori_piutang_jasa_perusahaan);}
    public static boolean getpiutang_jasa_perusahaan() {return hakAkses.get(EnumAkses.piutang_jasa_perusahaan);}
    public static boolean getbayar_piutang_jasa_perusahaan() {return hakAkses.get(EnumAkses.bayar_piutang_jasa_perusahaan);}
    public static boolean getpiutang_jasa_perusahaan_belum_lunas() {return hakAkses.get(EnumAkses.piutang_jasa_perusahaan_belum_lunas);}
    public static boolean getchecklist_kesiapan_anestesi() {return hakAkses.get(EnumAkses.checklist_kesiapan_anestesi);}
    public static boolean getpiutang_peminjaman_uang_belum_lunas() {return hakAkses.get(EnumAkses.piutang_peminjaman_uang_belum_lunas);}
    public static boolean gethasil_pemeriksaan_slit_lamp() {return hakAkses.get(EnumAkses.hasil_pemeriksaan_slit_lamp);}
    public static boolean gethasil_pemeriksaan_oct() {return hakAkses.get(EnumAkses.hasil_pemeriksaan_oct);}
    public static boolean getbeban_hutang_lain() {return hakAkses.get(EnumAkses.beban_hutang_lain);}
    public static boolean getpoli_asal_pasien_ranap() {return hakAkses.get(EnumAkses.poli_asal_pasien_ranap);}
    public static boolean getpemberi_hutang_lain() {return hakAkses.get(EnumAkses.pemberi_hutang_lain);}
    public static boolean getdokter_asal_pasien_ranap() {return hakAkses.get(EnumAkses.dokter_asal_pasien_ranap);}
    public static boolean getduta_parkir_rekap_keluar() {return hakAkses.get(EnumAkses.duta_parkir_rekap_keluar);}
    public static boolean getsurat_keterangan_layak_terbang() {return hakAkses.get(EnumAkses.surat_keterangan_layak_terbang);}
    public static boolean getbayar_beban_hutang_lain() {return hakAkses.get(EnumAkses.bayar_beban_hutang_lain);}
    public static boolean getsurat_persetujuan_pemeriksaan_hiv() {return hakAkses.get(EnumAkses.surat_persetujuan_pemeriksaan_hiv);}
    public static boolean getskrining_instrumen_acrs() {return hakAkses.get(EnumAkses.skrining_instrumen_acrs);}
    public static boolean getsurat_pernyataan_memilih_dpjp() {return hakAkses.get(EnumAkses.surat_pernyataan_memilih_dpjp);}
    public static boolean getskrining_instrumen_mental_emosional() {return hakAkses.get(EnumAkses.skrining_instrumen_mental_emosional);}
    public static boolean getpelanggan_lab_kesehatan_lingkungan() {return hakAkses.get(EnumAkses.pelanggan_lab_kesehatan_lingkungan);}
    public static boolean getkriteria_masuk_nicu() {return hakAkses.get(EnumAkses.kriteria_masuk_nicu);}
    public static boolean getkriteria_keluar_nicu() {return hakAkses.get(EnumAkses.kriteria_keluar_nicu);}
    public static boolean getpenilaian_medis_ranap_psikiatrik() {return hakAkses.get(EnumAkses.penilaian_medis_ranap_psikiatrik);}
    public static boolean getkriteria_masuk_picu() {return hakAkses.get(EnumAkses.kriteria_masuk_picu);}
    public static boolean getkriteria_keluar_picu() {return hakAkses.get(EnumAkses.kriteria_keluar_picu);}
    public static boolean getmaster_sampel_bakumutu() {return hakAkses.get(EnumAkses.master_sampel_bakumutu);}
    public static boolean getskrining_instrumen_amt() {return hakAkses.get(EnumAkses.skrining_instrumen_amt);}
    public static boolean getparameter_pengujian_lab_kesehatan_lingkungan() {return hakAkses.get(EnumAkses.parameter_pengujian_lab_kesehatan_lingkungan);}
    public static boolean getnilai_normal_baku_mutu_lab_kesehatan_lingkungan() {return hakAkses.get(EnumAkses.nilai_normal_baku_mutu_lab_kesehatan_lingkungan);}
    public static boolean getskrining_pneumonia_severity_index() {return hakAkses.get(EnumAkses.skrining_pneumonia_severity_index);}
    public static boolean getpermintaan_pengujian_sampel_lab_kesehatan_lingkungan() {return hakAkses.get(EnumAkses.permintaan_pengujian_sampel_lab_kesehatan_lingkungan);}
    public static boolean getpenilaian_awal_medis_ralan_jantung() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_jantung);}
    public static boolean getpenilaian_awal_medis_ralan_urologi() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ralan_urologi);}
    public static boolean gethasil_pemeriksaan_treadmill() {return hakAkses.get(EnumAkses.hasil_pemeriksaan_treadmill);}
    public static boolean gethasil_pemeriksaan_echo_pediatrik() {return hakAkses.get(EnumAkses.hasil_pemeriksaan_echo_pediatrik);}
    public static boolean gettemplate_pelaksanaan_informasi_edukasi() {return hakAkses.get(EnumAkses.template_pelaksanaan_informasi_edukasi);}
    public static boolean getskrining_instrumen_esat() {return hakAkses.get(EnumAkses.skrining_instrumen_esat);}
    public static boolean getpenilaian_awal_medis_ranap_jantung() {return hakAkses.get(EnumAkses.penilaian_awal_medis_ranap_jantung);}
    public static boolean getpenugasan_pengujian_sampel_lab_kesehatan_lingkungan() {return hakAkses.get(EnumAkses.penugasan_pengujian_sampel_lab_kesehatan_lingkungan);}
    public static boolean gethasil_pengujian_sampel_lab_kesehatan_lingkungan() {return hakAkses.get(EnumAkses.hasil_pengujian_sampel_lab_kesehatan_lingkungan);}
    public static boolean getverifikasi_pengujian_sampel_lab_kesehatan_lingkungan() {return hakAkses.get(EnumAkses.verifikasi_pengujian_sampel_lab_kesehatan_lingkungan);}
    public static boolean getvalidasi_pengujian_sampel_lab_kesehatan_lingkungan() {return hakAkses.get(EnumAkses.validasi_pengujian_sampel_lab_kesehatan_lingkungan);}
    public static boolean getrekap_pelayanan_lab_kesehatan_lingkungan() {return hakAkses.get(EnumAkses.rekap_pelayanan_lab_kesehatan_lingkungan);}
    public static boolean getpembayaran_pengujian_sampel_lab_kesehatan_lingkungan() {return hakAkses.get(EnumAkses.pembayaran_pengujian_sampel_lab_kesehatan_lingkungan);}
    public static boolean getskrining_curb65() {return hakAkses.get(EnumAkses.skrining_curb65);}
    public static boolean getbpjs_potensi_prb() {return hakAkses.get(EnumAkses.bpjs_potensi_prb);}
    public static boolean getbpjs_riwayat_pelayanan_obat() {return hakAkses.get(EnumAkses.bpjs_riwayat_pelayanan_obat);}
    public static boolean getskrining_gizi_kehamilan() {return hakAkses.get(EnumAkses.skrining_gizi_kehamilan);}
    public static boolean getbpjs_rekap_peserta_prb_apotek() {return hakAkses.get(EnumAkses.bpjs_rekap_peserta_prb_apotek);}
    public static boolean getserah_terima_anggota_tubuh_barang() {return hakAkses.get(EnumAkses.serah_terima_anggota_tubuh_barang);}
    public static boolean getpcra_icra_jenis_aktivitas_proyek() {return hakAkses.get(EnumAkses.pcra_icra_jenis_aktivitas_proyek);}
    public static boolean getpcra_icra_lokasi_kelompok_risiko_area() {return hakAkses.get(EnumAkses.pcra_icra_lokasi_kelompok_risiko_area);}
    public static boolean getpcra_icra_kelas_risiko_pencegahan() {return hakAkses.get(EnumAkses.pcra_icra_kelas_risiko_pencegahan);}
    public static boolean getpcra_icra_tindakan_pengendalian() {return hakAkses.get(EnumAkses.pcra_icra_tindakan_pengendalian);}
    public static boolean getpcra_icra_identifkasi_risiko_infeksi() {return hakAkses.get(EnumAkses.pcra_icra_identifkasi_risiko_infeksi);}
    public static boolean getpcra_icra_identifkasi_risiko_keselamatan() {return hakAkses.get(EnumAkses.pcra_icra_identifkasi_risiko_keselamatan);}
    public static boolean getpcra_icra_identifkasi_risiko_kebakaran() {return hakAkses.get(EnumAkses.pcra_icra_identifkasi_risiko_kebakaran);}
    public static boolean getpcra_icra_identifkasi_risiko_utilitas() {return hakAkses.get(EnumAkses.pcra_icra_identifkasi_risiko_utilitas);}
    public static boolean getbpjs_daftar_resep_apotek() {return hakAkses.get(EnumAkses.bpjs_daftar_resep_apotek);}
    public static boolean getdaftar_permintaan_resep_iterasi_bpjs() {return hakAkses.get(EnumAkses.daftar_permintaan_resep_iterasi_bpjs);}
    public static boolean getpcra_icra_pengkajian_risiko_prakonstruksi() {return hakAkses.get(EnumAkses.pcra_icra_pengkajian_risiko_prakonstruksi);}
    public static boolean getpcra_icra_persyaratan_harus_dipenuhi() {return hakAkses.get(EnumAkses.pcra_icra_persyaratan_harus_dipenuhi);}
    public static boolean getsatu_sehat_kirim_questionresponse_telaah_farmasi() {return hakAkses.get(EnumAkses.satu_sehat_kirim_questionresponse_telaah_farmasi);}
    public static boolean getsatu_sehat_kirim_allergy_intolerance() {return hakAkses.get(EnumAkses.satu_sehat_kirim_allergy_intolerance);}
    public static boolean getkonsultasi_perawat() {return hakAkses.get(EnumAkses.konsultasi_perawat);}
    public static boolean getjawaban_konsultasi_perawat(){return hakAkses.get(EnumAkses.jawaban_konsultasi_perawat);}
    public static boolean getbridging_smart_klaim_bpjs(){return hakAkses.get(EnumAkses.bridging_smart_klaim_bpjs);}
    public static boolean getmapping_prosedur_smart_klaim_bpjs(){return hakAkses.get(EnumAkses.mapping_prosedur_smart_klaim_bpjs);}
    public static boolean getmapping_penyakit_smart_klaim_bpjs(){return hakAkses.get(EnumAkses.mapping_penyakit_smart_klaim_bpjs);}
    public static boolean getpermintaan_binrohtal(){return hakAkses.get(EnumAkses.permintaan_binrohtal);}
    public static boolean getsurat_permintaan_perlindungan_dari_kekerasan(){return hakAkses.get(EnumAkses.surat_permintaan_perlindungan_dari_kekerasan);}
    public static boolean getsurat_permohonan_privasi(){return hakAkses.get(EnumAkses.surat_permohonan_privasi);}
    public static boolean getsurat_permintaan_second_opinion(){return hakAkses.get(EnumAkses.surat_permintaan_second_opinion);}
    public static boolean getsurat_keterangan_berobat(){return hakAkses.get(EnumAkses.surat_keterangan_berobat);}
    public static boolean getsurat_penolakan_resusitasi(){return hakAkses.get(EnumAkses.surat_penolakan_resusitasi);}
    public static boolean getcatatan_observasi_ruang_ok(){return hakAkses.get(EnumAkses.catatan_observasi_ruang_ok);}
    public static boolean gethasil_pemeriksaan_usg_abdomen(){return hakAkses.get(EnumAkses.hasil_pemeriksaan_usg_abdomen);}
    public static boolean getintervensi_nyeri_farmakologi(){return hakAkses.get(EnumAkses.intervensi_nyeri_farmakologi);}
    public static boolean getintervensi_nyeri_nonfarmakologi(){return hakAkses.get(EnumAkses.intervensi_nyeri_nonfarmakologi);}
    public static boolean getsurat_pengajuan_cuti_pasien(){return hakAkses.get(EnumAkses.surat_pengajuan_cuti_pasien);}
    public static boolean getchecklist_kriteria_masuk_isolasi(){return hakAkses.get(EnumAkses.checklist_kriteria_masuk_isolasi);}
    public static boolean getsatu_sehat_mapping_kptl_tindakan_ralan(){return hakAkses.get(EnumAkses.satu_sehat_mapping_kptl_tindakan_ralan);}
    public static boolean getsatu_sehat_mapping_kptl_tindakan_ranap(){return hakAkses.get(EnumAkses.satu_sehat_mapping_kptl_tindakan_ranap);}
    public static boolean getsatu_sehat_mapping_kptl_tindakan_radiologi(){return hakAkses.get(EnumAkses.satu_sehat_mapping_kptl_tindakan_radiologi);}
    public static boolean getsatu_sehat_mapping_kptl_tindakan_laborat(){return hakAkses.get(EnumAkses.satu_sehat_mapping_kptl_tindakan_laborat);}
    public static boolean getsatu_sehat_mapping_kptl_tindakan_operasi(){return hakAkses.get(EnumAkses.satu_sehat_mapping_kptl_tindakan_operasi);}
    public static boolean getsatu_sehat_mapping_kptl_tarif_kamar(){return hakAkses.get(EnumAkses.satu_sehat_mapping_kptl_tarif_kamar);}
}
