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
if ($hari == "Sunday")    $namahari = "AKHAD";
elseif ($hari == "Monday")    $namahari = "SENIN";
elseif ($hari == "Tuesday")   $namahari = "SELASA";
elseif ($hari == "Wednesday") $namahari = "RABU";
elseif ($hari == "Thursday")  $namahari = "KAMIS";
elseif ($hari == "Friday")    $namahari = "JUMAT";
elseif ($hari == "Saturday")  $namahari = "SABTU";
?>

<style>
    /* Shared table styles – only injected once per load, browser ignores duplicates */
    .rs-panel {
        display: flex;
        flex-direction: column;
        flex: 1;
        min-width: 0;
        overflow: hidden;
    }

    .panel-cols {
        display: flex;
        gap: 10px;
        height: 100%;
    }
</style>

<div style="display:flex; gap:10px; width:100%; height:100%;">

    <!-- ── LEFT: Jadwal Dokter ─────────────────────── -->
    <div class="table-card" style="flex:6; min-width:0;">
        <div class="table-card-header">
            <span class="material-icons">assignment_ind</span>
            Jadwal Praktek Dokter &nbsp;
            <span style="font-size:11px; opacity:0.8; font-weight:600;">(<?= $namahari ?>)</span>
        </div>
        <div class="table-scroll-wrap">
            <div class="auto-scroll-inner" id="scroll-dokter">
                <table class="rs-table">
                    <thead>
                        <tr>
                            <th>Nama Dokter</th>
                            <th>Poliklinik</th>
                            <th>Mulai</th>
                            <th>Selesai</th>
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
                                    <td>" . htmlspecialchars($data['nm_dokter']) . "</td>
                                    <td>" . htmlspecialchars($data['nm_poli'])   . "</td>
                                    <td style='text-align:center;'>" . $data['jam_mulai']   . "</td>
                                    <td style='text-align:center;'>" . $data['jam_selesai'] . "</td>
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

    <!-- ── RIGHT: Daftar Kamar ────────────────────── -->
    <div class="table-card" style="flex:4; min-width:0;">
        <div class="table-card-header">
            <span class="material-icons">hotel</span>
            Daftar Ruang Rawat Inap
        </div>
        <div class="table-scroll-wrap">
            <div class="auto-scroll-inner" id="scroll-kamar">
                <table class="rs-table">
                    <thead>
                        <tr>
                            <th style="text-align:left;">Kelas Kamar</th>
                            <th>Total Bed</th>
                            <th>Terisi</th>
                            <th>Kosong</th>
                        </tr>
                    </thead>
                    <tbody>
                        <?php
                        $_sql = "Select kelas from kamar where statusdata='1' group by kelas";
                        $hasil = bukaquery($_sql);
                        $cek   = mysqli_num_rows($hasil);

                        if ($cek > 0) {
                            while ($data = mysqli_fetch_array($hasil)) {
                                $kelas   = $data['kelas'];
                                $total   = mysqli_fetch_array(bukaquery("select count(kelas) from kamar where statusdata='1' and kelas='$kelas'"));
                                $isi     = mysqli_fetch_array(bukaquery("select count(kelas) from kamar where statusdata='1' and kelas='$kelas' and status='ISI'"));
                                $kosong  = mysqli_fetch_array(bukaquery("select count(kelas) from kamar where statusdata='1' and kelas='$kelas' and status='KOSONG'"));

                                echo "<tr>
                                    <td style='text-align:left; font-weight:700;'>" . htmlspecialchars($kelas) . "</td>
                                    <td style='text-align:center;'><span class='badge-bed badge-total'>"  . $total[0]  . "</span></td>
                                    <td style='text-align:center;'><span class='badge-bed badge-isi'>"    . $isi[0]    . "</span></td>
                                    <td style='text-align:center;'><span class='badge-bed badge-kosong'>" . $kosong[0] . "</span></td>
                                </tr>";
                            }
                        } else {
                            echo "<tr><td colspan='4'>
                                    <div class='empty-state'>
                                        <span class='material-icons'>bed_outlined</span>
                                        Data ruang rawat inap belum tersedia
                                    </div>
                                  </td></tr>";
                        }
                        ?>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</div>