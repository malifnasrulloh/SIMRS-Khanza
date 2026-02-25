<?php
require_once('conf/conf.php');
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");
$tanggal = mktime(date("m"), date("d"), date("Y"));
$jam = date("H:i");
?>

<div class="table-card" style="height:100%;">
    <div class="table-card-header">
        <span class="material-icons">favorite</span>
        Jadwal Operasi Hari Ini &nbsp;
        <span style="font-size:11px; opacity:0.8; font-weight:600;"><?= date("d F Y") ?></span>
    </div>
    <div class="table-scroll-wrap">
        <div class="auto-scroll-inner">
            <table class="rs-table">
                <thead>
                    <tr>
                        <th>#</th>
                        <th>No. Rawat</th>
                        <th>Umur</th>
                        <th>J.K.</th>
                        <th>Mulai</th>
                        <th>Selesai</th>
                        <th>Status</th>
                        <th>Jenis Operasi</th>
                        <th>Operator</th>
                        <th>Ruang OK</th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    $_sql = "select booking_operasi.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien,
                                    booking_operasi.tanggal, booking_operasi.jam_mulai, booking_operasi.jam_selesai,
                                    booking_operasi.status, booking_operasi.kd_dokter, dokter.nm_dokter,
                                    booking_operasi.kode_paket, paket_operasi.nm_perawatan,
                                    concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,
                                    pasien.jk, ruang_ok.nm_ruang_ok
                             from booking_operasi
                             inner join reg_periksa  on booking_operasi.no_rawat   = reg_periksa.no_rawat
                             inner join pasien       on reg_periksa.no_rkm_medis   = pasien.no_rkm_medis
                             inner join paket_operasi on booking_operasi.kode_paket = paket_operasi.kode_paket
                             inner join dokter       on booking_operasi.kd_dokter   = dokter.kd_dokter
                             inner join ruang_ok     on booking_operasi.kd_ruang_ok = ruang_ok.kd_ruang_ok
                             where tanggal='" . date("Y-m-d", $tanggal) . "'
                             order by booking_operasi.jam_mulai";

                    $hasil = bukaquery($_sql);
                    $cek   = mysqli_num_rows($hasil);

                    if ($cek > 0) {
                        $no = 1;
                        while ($data = mysqli_fetch_array($hasil)) {
                            // Map status to CSS class
                            $st  = strtolower(trim($data['status']));
                            $cls = 'status-default';
                            if (strpos($st, 'tunggu') !== false || strpos($st, 'menunggu') !== false) $cls = 'status-menunggu';
                            elseif (strpos($st, 'jalan') !== false || strpos($st, 'proses') !== false) $cls = 'status-berjalan';
                            elseif (strpos($st, 'selesai') !== false || strpos($st, 'done') !== false) $cls = 'status-selesai';
                            elseif (strpos($st, 'batal') !== false || strpos($st, 'cancel') !== false) $cls = 'status-batal';

                            echo "<tr>
                                <td style='color:#aaa; font-size:11px;'>" . $no++ . "</td>
                                <td style='font-size:12px;'>" . htmlspecialchars($data['no_rawat'])      . "</td>
                                <td>" . htmlspecialchars($data['umur'])          . "</td>
                                <td>" . htmlspecialchars($data['jk'])            . "</td>
                                <td style='font-weight:800; color:#1565c0;'>" . $data['jam_mulai']   . "</td>
                                <td style='font-weight:800; color:#1565c0;'>" . $data['jam_selesai'] . "</td>
                                <td><span class='status-badge $cls'>" . htmlspecialchars($data['status']) . "</span></td>
                                <td style='text-align:left; max-width:180px; white-space:normal; line-height:1.3;'>" . htmlspecialchars($data['nm_perawatan']) . "</td>
                                <td style='text-align:left;'>" . htmlspecialchars($data['nm_dokter'])    . "</td>
                                <td>" . htmlspecialchars($data['nm_ruang_ok'])  . "</td>
                            </tr>";
                        }
                    } else {
                        echo "<tr><td colspan='10'>
                                <div class='empty-state'>
                                    <span class='material-icons'>event_available</span>
                                    Tidak ada jadwal operasi pada hari ini
                                </div>
                              </td></tr>";
                    }
                    ?>
                </tbody>
            </table>
        </div>
    </div>
</div>