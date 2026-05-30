<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){ exit(header("Location:../index.php")); }
    $range = isset($_GET['range']) && is_numeric($_GET['range']) ? (int)$_GET['range'] : 30;
    $kd_dokter = validTeks4(encrypt_decrypt($_SESSION["ses_dokter"],"d"),20);
?>
<div class="block-header"><h2><center>HASIL LABORAT PASIEN</center></h2></div>

<div class="row clearfix">
    <div class="col-xs-12">
        <div class="card" style="padding:12px 20px; margin-bottom:10px; display:flex; align-items:center; gap:10px; flex-wrap:wrap;">
            <b style="color:#555;">Tampilkan:</b>
            <?php foreach([7=>'7 Hari', 30=>'30 Hari', 90=>'90 Hari', 365=>'1 Tahun'] as $n=>$label): ?>
            <a href="?act=HasilLaborat&range=<?=$n?>" class="btn btn-sm waves-effect <?=($range==$n)?'btn-primary':'btn-default'?>"><?=$label?></a>
            <?php endforeach; ?>
        </div>
    </div>
</div>

<div class="row clearfix">
    <div class="col-xs-12">
        <div class="card">
            <div class="body">
                <div class="table-responsive">
                    <table class="table table-bordered table-striped table-hover js-basic-example dataTable" id="tblLaborat">
                        <thead>
                            <tr>
                                <th>Tanggal</th><th>No.Rawat</th><th>No.RM</th><th>Nama Pasien</th>
                                <th>Jenis Pemeriksaan</th><th><center>Kategori</center></th><th><center>Aksi</center></th>
                            </tr>
                        </thead>
                        <tbody>
                        <?php
                            $q = bukaquery("
                                SELECT pl.no_rawat, pl.kd_jenis_prw, pl.tgl_periksa, pl.jam, pl.kategori,
                                       DATE_FORMAT(pl.tgl_periksa,'%d/%m/%Y') as tgl_fmt,
                                       rp.no_rkm_medis, p.nm_pasien, j.nm_perawatan
                                FROM periksa_lab pl
                                INNER JOIN reg_periksa rp ON pl.no_rawat = rp.no_rawat
                                INNER JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis
                                INNER JOIN jns_perawatan_lab j ON pl.kd_jenis_prw = j.kd_jenis_prw
                                WHERE (pl.kd_dokter = '$kd_dokter' OR pl.dokter_perujuk = '$kd_dokter' OR rp.kd_dokter = '$kd_dokter')
                                  AND pl.tgl_periksa >= DATE_SUB(CURRENT_DATE(), INTERVAL $range DAY)
                                ORDER BY pl.tgl_periksa DESC, pl.jam DESC
                            ");
                            while($r = mysqli_fetch_array($q)){
                                $norawat  = htmlspecialchars($r['no_rawat']);
                                $kdjenis  = htmlspecialchars($r['kd_jenis_prw']);
                                $tgl      = htmlspecialchars($r['tgl_periksa']);
                                $jam      = htmlspecialchars($r['jam']);
                                $norm     = htmlspecialchars($r['no_rkm_medis']);
                                $nama     = htmlspecialchars($r['nm_pasien']);
                                $katBadge = ($r['kategori']=='PA')?'bg-orange':(($r['kategori']=='MB')?'bg-purple':'bg-blue');
                                echo "<tr>
                                    <td>{$r['tgl_fmt']} {$r['jam']}</td>
                                    <td>$norawat</td>
                                    <td>$norm</td>
                                    <td><b>$nama</b></td>
                                    <td>{$r['nm_perawatan']}</td>
                                    <td align='center'><span class='badge $katBadge'>{$r['kategori']}</span></td>
                                    <td align='center'>
                                        <button class='btn btn-sm btn-teal waves-effect'
                                            onclick='openLabDetail(\"$norawat\",\"$kdjenis\",\"$tgl\",\"$jam\",\"$nama\")'>
                                            <i class='material-icons' style='font-size:16px;vertical-align:middle;'>biotech</i> Lihat Hasil
                                        </button>
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

<!-- MODAL DETAIL LAB -->
<div class="modal fade" id="modalLabDetail" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document" style="width:92%; max-width:900px;">
        <div class="modal-content">
            <div class="modal-header bg-teal" style="color:white;">
                <button type="button" class="close" data-dismiss="modal" style="color:white; opacity:1;">&times;</button>
                <h4 class="modal-title"><i class="material-icons" style="vertical-align:middle;margin-right:6px;">biotech</i> Detail Hasil Laborat: <span id="labDetailNama"></span></h4>
            </div>
            <div class="modal-body" id="labDetailBody" style="max-height:75vh; overflow-y:auto;">
                <div class="text-center" style="padding:40px;"><div class="preloader pl-size-xl"><div class="spinner-layer pl-teal"><div class="circle-clipper left"><div class="circle"></div></div><div class="circle-clipper right"><div class="circle"></div></div></div></div></div>
            </div>
        </div>
    </div>
</div>

<style>
.lab-H { color:#c62828; font-weight:700; }
.lab-L { color:#1565c0; font-weight:700; }
.btn-teal { background-color:#009688; color:white !important; }
.btn-teal:hover { background-color:#00796b; }
</style>

<script>
function openLabDetail(norawat, kdjenis, tgl, jam, nama){
    document.getElementById('labDetailNama').innerText = nama;
    document.getElementById('labDetailBody').innerHTML = '<div class="text-center" style="padding:40px;"><div class="preloader pl-size-xl"><div class="spinner-layer pl-teal"><div class="circle-clipper left"><div class="circle"></div></div><div class="circle-clipper right"><div class="circle"></div></div></div></div></div>';
    $('#modalLabDetail').modal('show');
    $.post('pages/ajax_laborat.php', {no_rawat:norawat, kd_jenis_prw:kdjenis, tgl_periksa:tgl, jam:jam}, function(d){
        var html='<table class="table table-bordered table-striped" style="font-size:13px;">';
        html+='<thead><tr><th>Pemeriksaan</th><th>Nilai</th><th>Satuan</th><th>Nilai Rujukan</th><th>Ket</th></tr></thead><tbody>';
        if(d.items && d.items.length){
            d.items.forEach(function(i){
                var cls=''; var ket=i.keterangan||'';
                if(ket=='H') cls='lab-H'; else if(ket=='L') cls='lab-L';
                html+='<tr class="'+cls+'"><td>'+i.Pemeriksaan+'</td><td><b>'+i.nilai+'</b></td><td>'+i.satuan+'</td><td>'+i.nilai_rujukan+'</td><td>'+ket+'</td></tr>';
            });
        } else { html+='<tr><td colspan="5" class="text-center">Tidak ada item detail ditemukan.</td></tr>'; }
        html+='</tbody></table>';
        document.getElementById('labDetailBody').innerHTML=html;
    },'json').fail(function(){ document.getElementById('labDetailBody').innerHTML='<div class="alert alert-danger m-20">Gagal memuat data.</div>'; });
}
</script>
