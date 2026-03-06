<?php
require_once('conf/conf.php');
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");
?>
<div class="card" style="width:100%;height:100%;">
    <div class="card-head">
        <div class="card-head-left">
            <div class="card-icon"><span class="material-icons">hotel</span></div>
            <div>
                <div class="card-title">Daftar Ruang Rawat Inap</div>
                <div class="card-sub">Ketersediaan kamar &middot; <?= date('d F Y') ?></div>
            </div>
        </div>
        <?php
        $kos = mysqli_fetch_array(bukaquery("SELECT count(*) FROM kamar WHERE statusdata='1' AND status='KOSONG'"));
        echo "<span class='pill'>{$kos[0]} Bed Tersedia</span>";
        ?>
    </div>
    <div style="flex:1;overflow-y:auto;scrollbar-width:none;">
        <div style="display:flex;flex-direction:column;gap:0.7vw;padding:0.7vw;">
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
                            <div class='tile-pct'>{$pct}% kapasitas terpakai</div>
                        </div>
                        <div class='tile-nums'>
                            <div class='tile-num'><div class='n n-tot'>{$tot[0]}</div><div class='l'>Total</div></div>
                            <div class='tile-num'><div class='n n-isi'>{$isi[0]}</div><div class='l'>Terisi</div></div>
                            <div class='tile-num'><div class='n n-kos'>{$kos[0]}</div><div class='l'>Kosong</div></div>
                        </div>
                    </div>";
                    $i++;
                }
            } else {
                echo "<div class='empty'><span class='material-icons'>bed</span><p>Data belum tersedia</p></div>";
            }
            ?>
        </div>
    </div>
</div>