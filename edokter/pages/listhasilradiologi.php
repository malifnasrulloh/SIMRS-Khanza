<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){ exit(header("Location:../index.php")); }
    $range = isset($_GET['range']) && is_numeric($_GET['range']) ? (int)$_GET['range'] : 30;
    $kd_dokter = validTeks4(encrypt_decrypt($_SESSION["ses_dokter"],"d"),20);
?>
<div class="block-header"><h2><center>DATA PEMERIKSAAN RADIOLOGI</center></h2></div>

<div class="row clearfix">
    <div class="col-xs-12">
        <div class="card" style="padding:12px 20px; margin-bottom:10px; display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
            <b style="color:#555;">Tampilkan:</b>
            <?php foreach([7=>'7 Hari', 30=>'30 Hari', 90=>'90 Hari', 365=>'1 Tahun'] as $n=>$label): ?>
            <a href="?act=HasilRadiologi&range=<?=$n?>" class="btn btn-sm waves-effect <?=($range==$n)?'btn-primary':'btn-default'?>"><?=$label?></a>
            <?php endforeach; ?>
        </div>
    </div>
</div>

<div class="row clearfix">
    <div class="col-xs-12">
        <div class="card">
            <div class="body">
                <div class="table-responsive">
                    <table class="table table-bordered table-striped table-hover js-basic-example dataTable" style="width:100%;">
                        <thead>
                            <tr>
                                <th style="width:140px;">Tanggal</th>
                                <th>Data Pasien</th>
                                <th>Pemeriksaan / Perujuk</th>
                                <th style="width:110px;"><center>Status</center></th>
                                <th style="width:110px;"><center>Aksi</center></th>
                            </tr>
                        </thead>
                        <tbody>
                        <?php
                            $q = bukaquery("
                                SELECT
                                    DATE_FORMAT(pr.tgl_periksa,'%d/%m/%Y') as tgl_fmt,
                                    pr.jam, pr.no_rawat, pr.tgl_periksa,
                                    rp.no_rkm_medis, p.nm_pasien,
                                    d.nm_dokter as nm_perujuk,
                                    j.nm_perawatan,
                                    CASE WHEN hr.no_rawat IS NOT NULL THEN 1 ELSE 0 END as sudah_dibaca
                                FROM periksa_radiologi pr
                                INNER JOIN reg_periksa rp ON pr.no_rawat=rp.no_rawat
                                INNER JOIN pasien p ON rp.no_rkm_medis=p.no_rkm_medis
                                LEFT JOIN dokter d ON pr.dokter_perujuk=d.kd_dokter
                                INNER JOIN jns_perawatan_radiologi j ON pr.kd_jenis_prw=j.kd_jenis_prw
                                LEFT JOIN hasil_radiologi hr ON pr.no_rawat=hr.no_rawat AND pr.tgl_periksa=hr.tgl_periksa AND pr.jam=hr.jam
                                WHERE (pr.kd_dokter='$kd_dokter' OR pr.dokter_perujuk='$kd_dokter' OR rp.kd_dokter='$kd_dokter')
                                  AND pr.tgl_periksa >= DATE_SUB(CURRENT_DATE(), INTERVAL $range DAY)
                                ORDER BY pr.tgl_periksa DESC, pr.jam DESC
                            ");
                            while($r = mysqli_fetch_array($q)) {
                                $badge = $r['sudah_dibaca'] ? '<span class="badge bg-teal" style="padding:4px 8px;">&#10003; Sudah Dibaca</span>' : '<span class="badge bg-orange" style="padding:4px 8px;">Belum Dibaca</span>';
                                $btnClass = $r['sudah_dibaca'] ? 'btn-default' : 'btn-warning';
                                $btnLabel = $r['sudah_dibaca'] ? 'Lihat Bacaan' : 'Lihat & Isi';
                                $iyem = encrypt_decrypt('{"norawat":"'.$r['no_rawat'].'","tglperiksa":"'.$r['tgl_periksa'].'","jam":"'.$r['jam'].'"}','e');
                                echo "<tr>
                                    <td style='vertical-align:middle; font-size:13px; color:#555;'>{$r['tgl_fmt']} <small style='color:#777;'>{$r['jam']}</small></td>
                                    <td style='vertical-align:middle;'>
                                        <div style='font-weight:700; color:#ff9800; font-size:14px; margin-bottom:4px;'>{$r['nm_pasien']}</div>
                                        <div style='font-size:11px; color:#666; display:flex; flex-wrap:wrap; gap:8px;'>
                                            <span>RM: <b style='color:#333;'>{$r['no_rkm_medis']}</b></span>
                                            <span style='color:#ccc;'>|</span>
                                            <span>Reg: <b style='color:#333;'>{$r['no_rawat']}</b></span>
                                        </div>
                                    </td>
                                    <td style='vertical-align:middle; font-size:13px;'>
                                        <div style='font-weight:600; color:#333;'>{$r['nm_perawatan']}</div>
                                        <div style='font-size:11px; color:#777; margin-top:2px;'>Perujuk: dr. ".(htmlspecialchars($r['nm_perujuk'])?:'-')."</div>
                                    </td>
                                    <td align='center' style='vertical-align:middle;'>$badge</td>
                                    <td align='center' style='vertical-align:middle;'>
                                        <a href='index.php?act=BacaanRadiologi&iyem=$iyem' class='btn btn-xs $btnClass waves-effect' style='padding:4px 8px; font-weight:600; border-radius:4px;'>$btnLabel</a>
                                    </td>
                                </tr>";
                            }
                        ?>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>