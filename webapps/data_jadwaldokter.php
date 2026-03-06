<?php
require_once('conf/conf.php');
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");
$hari = getOne("select DAYNAME(current_date())");
$hMap = [
    "Sunday" => "AKHAD",
    "Monday" => "SENIN",
    "Tuesday" => "SELASA",
    "Wednesday" => "RABU",
    "Thursday" => "KAMIS",
    "Friday" => "JUMAT",
    "Saturday" => "SABTU"
];
$nh = $hMap[$hari] ?? $hari;
?>
<div class="card" style="width:100%;height:100%;">
    <div class="card-head">
        <div class="card-head-left">
            <div class="card-icon"><span class="material-icons">assignment_ind</span></div>
            <div>
                <div class="card-title">Jadwal Praktek Dokter</div>
                <div class="card-sub"><?= $nh ?> &middot; <?= date('d F Y') ?></div>
            </div>
        </div>
        <?php
        $cnt = mysqli_num_rows(bukaquery("SELECT jadwal.kd_dokter FROM jadwal
            INNER JOIN dokter INNER JOIN poliklinik
            ON dokter.kd_dokter=jadwal.kd_dokter AND jadwal.kd_poli=poliklinik.kd_poli
            WHERE jadwal.hari_kerja='$nh'"));
        if ($cnt > 0) echo "<span class='pill'>$cnt Dokter Bertugas</span>";
        ?>
    </div>
    <div class="table-wrap">
        <div class="scroll-inner">
            <table class="t">
                <thead>
                    <tr>
                        <th style="width:2.2vw;">#</th>
                        <th>Nama Dokter</th>
                        <th>Poliklinik / Spesialisasi</th>
                        <th style="text-align:center;width:8vw;">Jam Mulai</th>
                        <th style="text-align:center;width:8vw;">Jam Selesai</th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    $res = bukaquery("SELECT dokter.nm_dokter,poliklinik.nm_poli,jadwal.jam_mulai,jadwal.jam_selesai
                    FROM jadwal INNER JOIN dokter INNER JOIN poliklinik
                    ON dokter.kd_dokter=jadwal.kd_dokter AND jadwal.kd_poli=poliklinik.kd_poli
                    WHERE jadwal.hari_kerja='$nh' ORDER BY jadwal.jam_mulai");
                    if (mysqli_num_rows($res) > 0) {
                        $n = 1;
                        while ($d = mysqli_fetch_array($res)) {
                            echo "<tr>
                            <td class='num'>$n</td>
                            <td>" . htmlspecialchars($d['nm_dokter']) . "</td>
                            <td class='med'>" . htmlspecialchars($d['nm_poli']) . "</td>
                            <td class='time'>" . $d['jam_mulai'] . "</td>
                            <td class='time'>" . $d['jam_selesai'] . "</td>
                        </tr>";
                            $n++;
                        }
                    } else {
                        echo "<tr><td colspan='5'><div class='empty'>
                        <span class='material-icons'>event_busy</span>
                        <p>Tidak ada jadwal praktek hari ini</p></div></td></tr>";
                    }
                    ?>
                </tbody>
            </table>
        </div>
    </div>
</div>