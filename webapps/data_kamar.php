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

<div class="table-card" style="width:100%; height:100%;">
    <div class="table-card-header">
        <span class="material-icons">hotel</span>
        Daftar Ruang Rawat Inap
    </div>
    <div class="table-scroll-wrap">
        <div class="auto-scroll-inner">
            <table class="rs-table">
                <thead>
                    <tr>
                        <th style="text-align:left;">Kelas Kamar</th>
                        <th style="text-align:center;">Jumlah Bed</th>
                        <th style="text-align:center;">Bed Terisi</th>
                        <th style="text-align:center;">Bed Kosong</th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    $_sql = "Select kelas from kamar where statusdata='1' group by kelas";
                    $hasil = bukaquery($_sql);
                    $cek   = mysqli_num_rows($hasil);

                    if ($cek > 0) {
                        while ($data = mysqli_fetch_array($hasil)) {
                            $kelas  = $data['kelas'];
                            $total  = mysqli_fetch_array(bukaquery("select count(kelas) from kamar where statusdata='1' and kelas='$kelas'"));
                            $isi    = mysqli_fetch_array(bukaquery("select count(kelas) from kamar where statusdata='1' and kelas='$kelas' and status='ISI'"));
                            $kosong = mysqli_fetch_array(bukaquery("select count(kelas) from kamar where statusdata='1' and kelas='$kelas' and status='KOSONG'"));

                            echo "<tr>
                                <td style='text-align:left; font-weight:700; font-size:16px;'>" . htmlspecialchars($kelas) . "</td>
                                <td style='text-align:center;'><span class='badge-bed badge-total'>"  . $total[0]  . "</span></td>
                                <td style='text-align:center;'><span class='badge-bed badge-isi'>"    . $isi[0]    . "</span></td>
                                <td style='text-align:center;'><span class='badge-bed badge-kosong'>" . $kosong[0] . "</span></td>
                            </tr>";
                        }
                    } else {
                        echo "<tr><td colspan='4'>
                                <div class='empty-state'>
                                    <span class='material-icons'>bed</span>
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