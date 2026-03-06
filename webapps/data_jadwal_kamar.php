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

$tot_bed = mysqli_fetch_array(bukaquery("SELECT count(*) FROM kamar WHERE statusdata='1'"));
$tot_isi = mysqli_fetch_array(bukaquery("SELECT count(*) FROM kamar WHERE statusdata='1' AND status='ISI'"));
$tot_kos = mysqli_fetch_array(bukaquery("SELECT count(*) FROM kamar WHERE statusdata='1' AND status='KOSONG'"));
?>

<div style="display:flex;flex-direction:column;width:100%;height:100%;gap:0.5vw;">
    <div style="display:flex;gap:0.5vw;flex:1;min-height:0;">
        <div class="card" style="flex:62;min-width:0;">
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
                if ($cnt > 0) echo "<span class='pill'>$cnt Dokter</span>";
                ?>
            </div>
            <div class="table-wrap">
                <div class="scroll-inner">
                    <table class="t">
                        <thead>
                            <tr>
                                <th style="width:2.2vw;">#</th>
                                <th>Nama Dokter</th>
                                <th>Poliklinik</th>
                                <th style="text-align:center;width:7vw;">Jam Mulai</th>
                                <th style="text-align:center;width:7vw;">Jam Selesai</th>
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

        <div class="card" style="flex:38;min-width:0;display:flex;flex-direction:column;">
            <div class="card-head">
                <div class="card-head-left">
                    <div class="card-icon"><span class="material-icons">hotel</span></div>
                    <div>
                        <div class="card-title">Rawat Inap</div>
                        <div class="card-sub">Ketersediaan kamar</div>
                    </div>
                </div>
            </div>
            <div style="flex:1;overflow-y:auto;scrollbar-width:none;">
                <div class="bed-grid" style="grid-template-columns:1fr;">
                    <?php
                    $ac = ['var(--bm)', 'var(--bd)', 'var(--bl)', '#0288d1', '#1565c0'];
                    $i = 0;
                    $res = bukaquery("SELECT kelas FROM kamar WHERE statusdata='1' GROUP BY kelas");
                    if (mysqli_num_rows($res) > 0) {
                        while ($d = mysqli_fetch_array($res)) {
                            $k = $d['kelas'];
                            $c = $ac[$i % count($ac)];
                            $tot = mysqli_fetch_array(bukaquery("SELECT count(*) FROM kamar WHERE statusdata='1' AND kelas='$k'"));
                            $isi = mysqli_fetch_array(bukaquery("SELECT count(*) FROM kamar WHERE statusdata='1' AND kelas='$k' AND status='ISI'"));
                            $kos = mysqli_fetch_array(bukaquery("SELECT count(*) FROM kamar WHERE statusdata='1' AND kelas='$k' AND status='KOSONG'"));
                            $pct = $tot[0] > 0 ? round(($isi[0] / $tot[0]) * 100) : 0;
                            $bar = $pct >= 90 ? 'var(--red)' : ($pct >= 65 ? 'var(--yel)' : 'var(--grn)');
                            echo "<div class='bed-tile' style='border-left-color:$c;'>
                                <div class='tile-label'>
                                    <div class='tile-name'>" . htmlspecialchars($k) . "</div>
                                    <div class='tile-bar'><div class='tile-bar-fill' style='width:{$pct}%;background:$bar;'></div></div>
                                    <div class='tile-pct'>{$pct}% terisi</div>
                                </div>
                                <div class='tile-nums'>
                                    <div class='tile-num'><div class='n n-tot'>{$tot[0]}</div><div class='l'>Total</div></div>
                                    <div class='tile-num'><div class='n n-isi'>{$isi[0]}</div><div class='l'>Terisi</div></div>
                                    <div class='tile-num'><div class='n n-kos'>{$kos[0]}</div><div class='l'>Kosong</div></div>
                                </div>
                            </div>";
                            $i++;
                        }
                    }
                    ?>
                </div>
            </div>
        </div>
    </div>
</div>