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

$hari = getOne("select DAYNAME(current_date())");
$namahari = "";
if ($hari == "Sunday")        $namahari = "AKHAD";
elseif ($hari == "Monday")    $namahari = "SENIN";
elseif ($hari == "Tuesday")   $namahari = "SELASA";
elseif ($hari == "Wednesday") $namahari = "RABU";
elseif ($hari == "Thursday")  $namahari = "KAMIS";
elseif ($hari == "Friday")    $namahari = "JUMAT";
elseif ($hari == "Saturday")  $namahari = "SABTU";
?>

<div class="table-card" style="width:100%; height:100%;">
    <div class="table-card-header">
        <span class="material-icons">assignment_ind</span>
        Jadwal Praktek Dokter &nbsp;
        <span style="font-size:11px; opacity:0.8; font-weight:600;">(<?= $namahari ?>)</span>
    </div>
    <div class="table-scroll-wrap">
        <div class="auto-scroll-inner">
            <table class="rs-table">
                <thead>
                    <tr>
                        <th style="text-align:left;">Nama Dokter</th>
                        <th style="text-align:left;">Poliklinik</th>
                        <th style="text-align:center;">Mulai</th>
                        <th style="text-align:center;">Selesai</th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    $_sql = "Select dokter.nm_dokter, poliklinik.nm_poli, jadwal.jam_mulai, jadwal.jam_selesai
                             from jadwal inner join dokter inner join poliklinik
                             on dokter.kd_dokter=jadwal.kd_dokter
                             and jadwal.kd_poli=poliklinik.kd_poli
                             where jadwal.hari_kerja='$namahari'";
                    $hasil = bukaquery($_sql);
                    $cek   = mysqli_num_rows($hasil);

                    if ($cek > 0) {
                        while ($data = mysqli_fetch_array($hasil)) {
                            echo "<tr>
                                <td style='text-align:left; font-weight:700;'>" . htmlspecialchars($data['nm_dokter']) . "</td>
                                <td style='text-align:left;'>"                  . htmlspecialchars($data['nm_poli'])   . "</td>
                                <td style='text-align:center; font-weight:800; color:#1565c0;'>" . $data['jam_mulai']   . "</td>
                                <td style='text-align:center; font-weight:800; color:#1565c0;'>" . $data['jam_selesai'] . "</td>
                            </tr>";
                        }
                    } else {
                        echo "<tr><td colspan='4'>
                                <div class='empty-state'>
                                    <span class='material-icons'>event_busy</span>
                                    Jadwal praktek hari ini belum tersedia
                                </div>
                              </td></tr>";
                    }
                    ?>
                </tbody>
            </table>
        </div>
    </div>
</div>