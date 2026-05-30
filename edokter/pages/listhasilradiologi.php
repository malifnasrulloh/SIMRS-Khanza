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
                    <table class="table table-bordered table-striped table-hover js-basic-example dataTable">
                        <thead>
                            <tr>
                                <th>Tanggal</th><th>No.Rawat</th><th>No.RM</th><th>Nama Pasien</th>
                                <th>Dokter Perujuk</th><th>Jenis Pemeriksaan</th><th><center>Status</center></th><th><center>Aksi</center></th>
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
                                $badge = $r['sudah_dibaca'] ? '<span class="badge bg-teal">&#10003; Sudah Dibaca</span>' : '<span class="badge bg-orange">Belum Dibaca</span>';
                                $btnClass = $r['sudah_dibaca'] ? 'btn-default' : 'btn-warning';
                                $btnLabel = $r['sudah_dibaca'] ? 'Lihat Bacaan' : 'Lihat & Isi';
                                $iyem = encrypt_decrypt('{"norawat":"'.$r['no_rawat'].'","tglperiksa":"'.$r['tgl_periksa'].'","jam":"'.$r['jam'].'"}','e');
                                echo "<tr>
                                    <td>{$r['tgl_fmt']} {$r['jam']}</td>
                                    <td>{$r['no_rawat']}</td>
                                    <td>{$r['no_rkm_medis']}</td>
                                    <td><b>{$r['nm_pasien']}</b></td>
                                    <td>{$r['nm_perujuk']}</td>
                                    <td>{$r['nm_perawatan']}</td>
                                    <td align='center'>$badge</td>
                                    <td align='center'><a href='index.php?act=BacaanRadiologi&iyem=$iyem' class='btn btn-sm $btnClass waves-effect'>$btnLabel</a></td>
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