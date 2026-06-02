<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){ exit(header("Location:../index.php")); }
?>
<div class="block-header">
    <h2><center>DAFTAR ANTRIAN PASIEN</center></h2>
</div>

<!-- Filter Bar -->
<div class="row clearfix" style="margin-bottom:0;">
    <div class="col-xs-12">
        <div class="card" style="padding:15px 20px; margin-bottom:10px;">
            <form method="GET" action="index.php" id="formFilterPasien" style="display:flex; flex-wrap:wrap; align-items:center; gap:10px;">
                <input type="hidden" name="act" value="Pasien">
                <div style="display:flex; align-items:center; gap:8px; flex-wrap:wrap;">
                    <label style="margin:0; font-weight:600; color:#555; white-space:nowrap;">Tanggal:</label>
                    <input type="date" name="tgl" id="tglFilter" class="form-control" style="width:160px;"
                           value="<?php echo isset($_GET['tgl']) ? htmlspecialchars($_GET['tgl']) : date('Y-m-d'); ?>"
                           onchange="this.form.submit()">
                    <div class="btn-group" role="group">
                        <button type="button" class="btn btn-xs btn-default waves-effect" onclick="setTgl('<?=date('Y-m-d')?>','today')">Hari Ini</button>
                        <button type="button" class="btn btn-xs btn-default waves-effect" onclick="setTgl('<?=date('Y-m-d',strtotime('-1 day'))?>','yesterday')">Kemarin</button>
                    </div>
                </div>
                <div class="btn-group" role="group" id="statusFilter">
                    <button type="button" class="btn btn-sm waves-effect <?= (!isset($_GET['stts']) || $_GET['stts']=='')?'btn-primary':'btn-default' ?>" onclick="setStatus('')">Semua</button>
                    <button type="button" class="btn btn-sm waves-effect <?= (isset($_GET['stts'])&&$_GET['stts']=='Ralan')?'btn-primary':'btn-default' ?>" onclick="setStatus('Ralan')">Rawat Jalan</button>
                    <button type="button" class="btn btn-sm waves-effect <?= (isset($_GET['stts'])&&$_GET['stts']=='Ranap')?'btn-primary':'btn-default' ?>" onclick="setStatus('Ranap')">Rawat Inap</button>
                </div>
                <input type="hidden" name="stts" id="sttsInput" value="<?= htmlspecialchars(isset($_GET['stts'])?$_GET['stts']:'') ?>">
            </form>
        </div>
    </div>
</div>

<div class="row clearfix">
    <div class="col-xs-12">
        <div class="card">
            <div class="body">
                <div class="table-responsive">
                    <table class="table table-bordered table-striped table-hover js-basic-example dataTable" id="tblPasien" style="width:100%;">
                        <thead>
                            <tr>
                                <th style="width:70px;"><center>No.Poli</center></th>
                                <th>Data Pasien</th>
                                <th style="width:80px;"><center>Tipe</center></th>
                                <th style="width:260px;"><center>Aksi</center></th>
                            </tr>
                        </thead>
                        <tbody>
                        <?php
                            $tgl  = isset($_GET['tgl'])  && preg_match('/^\d{4}-\d{2}-\d{2}$/',$_GET['tgl'])  ? $_GET['tgl']  : date('Y-m-d');
                            $stts = isset($_GET['stts']) && in_array($_GET['stts'],['Ralan','Ranap']) ? $_GET['stts'] : '';
                            $kd_dokter = validTeks4(encrypt_decrypt($_SESSION["ses_dokter"],"d"),20);
                            $whereStts = $stts ? "AND reg_periksa.status_lanjut='$stts'" : '';
                            $q = bukaquery("
                                SELECT reg_periksa.no_reg, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                                       pasien.nm_pasien, pasien.jk,
                                       CONCAT(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,
                                       reg_periksa.stts, reg_periksa.status_lanjut
                                FROM reg_periksa
                                INNER JOIN pasien ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis
                                WHERE reg_periksa.kd_dokter='$kd_dokter'
                                  AND reg_periksa.tgl_registrasi='$tgl'
                                  $whereStts
                                ORDER BY reg_periksa.no_reg ASC
                            ");
                            while($r = mysqli_fetch_array($q)) {
                                $norawat = htmlspecialchars($r['no_rawat']);
                                $norm    = htmlspecialchars($r['no_rkm_medis']);
                                $nama    = htmlspecialchars($r['nm_pasien']);
                                $tipe    = $r['status_lanjut'];
                                $badge   = ($tipe=='Ranap') ? 'bg-red' : 'bg-teal';
                                $sttsVal = htmlspecialchars($r['stts']);
                                echo "
                                <tr>
                                    <td align='center' style='vertical-align:middle; font-weight:bold; font-size:14px; color:#555;'>{$r['no_reg']}</td>
                                    <td style='vertical-align:middle;'>
                                        <div style='font-weight:700; color:#1f87e6; font-size:14px; margin-bottom:4px;'>$nama</div>
                                        <div style='font-size:11px; color:#666; display:flex; flex-wrap:wrap; gap:8px; line-height:1.4;'>
                                            <span>RM: <b style='color:#333;'>$norm</b></span>
                                            <span style='color:#ccc;'>|</span>
                                            <span>Reg: <b style='color:#333;'>$norawat</b></span>
                                            <span style='color:#ccc;'>|</span>
                                            <span>JK: <b style='color:#333;'>{$r['jk']}</b></span>
                                            <span style='color:#ccc;'>|</span>
                                            <span>Umur: <b style='color:#333;'>{$r['umur']}</b></span>
                                            <span style='color:#ccc;'>|</span>
                                            <span>Status: <span class='label label-info' style='font-size:9px; padding:1px 4px; border-radius:3px; font-weight:600;'>$sttsVal</span></span>
                                        </div>
                                    </td>
                                    <td align='center' style='vertical-align:middle;'><span class='badge $badge' style='padding:4px 8px; font-size:11px;'>$tipe</span></td>
                                    <td align='center' style='vertical-align:middle;'>
                                        <div style='display:flex; gap:6px; flex-wrap:wrap; justify-content:center;'>
                                            <button class='btn btn-xs btn-info waves-effect' onclick='openEMR(\"$norm\",\"$nama\")' title='Lihat Riwayat EMR' style='padding:4px 8px; font-weight:600; border-radius:4px;'>
                                                <i class='material-icons' style='font-size:14px; vertical-align:middle; margin-right:4px;'>notes</i>Riwayat EMR
                                            </button>
                                            <button class='btn btn-xs btn-warning waves-effect' onclick='openRadiologi(\"$norm\",\"$nama\")' title='Lihat Radiologi' style='padding:4px 8px; font-weight:600; border-radius:4px;'>
                                                <i class='material-icons' style='font-size:14px; vertical-align:middle; margin-right:4px;'>settings_overscan</i>Radiologi
                                            </button>
                                            <button class='btn btn-xs btn-success waves-effect' onclick='openLaborat(\"$norawat\",\"$norm\",\"$nama\")' title='Lihat Laborat' style='padding:4px 8px; font-weight:600; border-radius:4px;'>
                                                <i class='material-icons' style='font-size:14px; vertical-align:middle; margin-right:4px;'>biotech</i>Laborat
                                            </button>
                                        </div>
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

<!-- MODAL EMR -->
<div class="modal fade" id="modalEMR" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document" style="width:92%; max-width:1000px;">
        <div class="modal-content">
            <div class="modal-header bg-blue-grey" style="color:white;">
                <button type="button" class="close" data-dismiss="modal" style="color:white; opacity:1;">&times;</button>
                <h4 class="modal-title"><i class="material-icons" style="vertical-align:middle;margin-right:6px;">notes</i> Riwayat EMR: <span id="emrNama"></span></h4>
            </div>
            <div class="modal-body" id="emrBody" style="max-height:75vh; overflow-y:auto; padding:20px;">
                <div class="text-center"><div class="preloader pl-size-xl"><div class="spinner-layer pl-blue-grey"><div class="circle-clipper left"><div class="circle"></div></div><div class="circle-clipper right"><div class="circle"></div></div></div></div></div>
            </div>
        </div>
    </div>
</div>

<!-- MODAL RADIOLOGI -->
<div class="modal fade" id="modalRadiologi" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document" style="width:92%; max-width:1000px;">
        <div class="modal-content">
            <div class="modal-header bg-orange" style="color:white;">
                <button type="button" class="close" data-dismiss="modal" style="color:white; opacity:1;">&times;</button>
                <h4 class="modal-title"><i class="material-icons" style="vertical-align:middle;margin-right:6px;">settings_overscan</i> Riwayat Radiologi: <span id="radNama"></span></h4>
            </div>
            <div class="modal-body" id="radBody" style="max-height:75vh; overflow-y:auto; padding:20px;">
                <div class="text-center"><div class="preloader pl-size-xl"><div class="spinner-layer pl-orange"><div class="circle-clipper left"><div class="circle"></div></div><div class="circle-clipper right"><div class="circle"></div></div></div></div></div>
            </div>
        </div>
    </div>
</div>

<!-- MODAL LABORAT -->
<div class="modal fade" id="modalLaborat" tabindex="-1" role="dialog">
    <div class="modal-dialog modal-lg" role="document" style="width:92%; max-width:1000px;">
        <div class="modal-content">
            <div class="modal-header bg-teal" style="color:white;">
                <button type="button" class="close" data-dismiss="modal" style="color:white; opacity:1;">&times;</button>
                <h4 class="modal-title"><i class="material-icons" style="vertical-align:middle;margin-right:6px;">biotech</i> Hasil Laborat: <span id="labNama"></span></h4>
            </div>
            <div class="modal-body" id="labBody" style="max-height:75vh; overflow-y:auto; padding:20px;">
                <div class="text-center"><div class="preloader pl-size-xl"><div class="spinner-layer pl-teal"><div class="circle-clipper left"><div class="circle"></div></div><div class="circle-clipper right"><div class="circle"></div></div></div></div></div>
            </div>
        </div>
    </div>
</div>

<style>
#tblPasien tbody tr:hover { background: rgba(33,150,243,.07) !important; }
.btn-xs .material-icons { font-size:15px; vertical-align:middle; }
.lab-H { color:#c62828; font-weight:700; }
.lab-L { color:#1565c0; font-weight:700; }
.soap-card { border-radius:8px; border:1px solid #e0e0e0; margin-bottom:12px; overflow:hidden; }
.soap-card .soap-head { background:#f9f9f9; padding:10px 14px; border-bottom:1px solid #eee; font-size:12px; color:#666; }
.soap-card .soap-body { padding:12px 14px; font-size:13px; line-height:1.7; }

.emr-visit-header {
    margin-top: 20px;
    margin-bottom: 8px;
    font-size: 12px;
    color: #455a64;
    font-weight: 700;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-bottom: 1px dashed #cfd8dc;
    padding-bottom: 6px;
    flex-wrap: wrap;
    gap: 8px;
}

@media (min-width: 992px) {
    .emr-flex-row {
        display: flex !important;
        align-items: stretch;
    }
    .emr-flex-col-left {
        display: flex;
        flex-direction: column;
        border-right: 1px solid #eee;
        padding-right: 15px !important;
    }
    .emr-flex-col-right {
        display: flex;
        flex-direction: column;
        padding-left: 15px !important;
    }
}
@media (max-width: 991px) {
    .emr-flex-row {
        display: block !important;
    }
    .emr-flex-col-left {
        margin-bottom: 15px;
        padding-right: 15px !important;
        border-right: none !important;
    }
    .emr-flex-col-right {
        padding-left: 15px !important;
    }
}
</style>

<script>
function setTgl(val){ document.getElementById('tglFilter').value=val; document.getElementById('formFilterPasien').submit(); }
function setStatus(val){ document.getElementById('sttsInput').value=val; document.getElementById('formFilterPasien').submit(); }

var SPINNER = '<div class="text-center" style="padding:40px 0;"><div class="preloader pl-size-xl"><div class="spinner-layer pl-blue-grey"><div class="circle-clipper left"><div class="circle"></div></div><div class="circle-clipper right"><div class="circle"></div></div></div></div></div>';

// -------- EMR MODAL --------
function openEMR(norm, nama) {
    document.getElementById('emrNama').innerText = nama;
    document.getElementById('emrBody').innerHTML = SPINNER;
    $('#modalEMR').modal('show');
    $.post('pages/ajax_emr.php', {no_rkm_medis: norm}, function(data) {
        var html = '';
        var visits = data.visits || [];
        
        if (visits.length === 0) {
            html += '<div class="alert alert-info">Belum ada riwayat medis historis (SOAP / Resep) untuk pasien ini.</div>';
        } else {
            // Header titles for columns
            html += '<div class="row hidden-xs hidden-sm" style="margin-bottom:10px; border-bottom:1px solid #ddd; padding-bottom:10px;">';
            html += '<div class="col-xs-6"><h5 style="font-weight:700; border-left:4px solid #607d8b; padding-left:10px; color:#333; margin:0;">Catatan Klinis (SOAP/SBAR)</h5></div>';
            html += '<div class="col-xs-6" style="padding-left:25px;"><h5 style="font-weight:700; border-left:4px solid #4caf50; padding-left:10px; color:#333; margin:0;">Riwayat Resep Obat</h5></div>';
            html += '</div>';

            visits.forEach(function(visit) {
                var s = visit.soap;
                var r = visit.resep;
                
                // Render Visit Meta Header
                html += '<div class="emr-visit-header">';
                html += '<span><i class="material-icons" style="font-size:15px; vertical-align:middle; margin-right:4px;">local_hospital</i> Kunjungan: <b>' + visit.no_rawat + '</b> <span class="badge bg-blue-grey" style="font-size:10px; margin-left:6px; padding:2px 6px;">' + visit.status_lanjut + '</span></span>';
                html += '<span style="color:#78909c;"><i class="material-icons" style="font-size:15px; vertical-align:middle;">event</i> ' + visit.tgl_registrasi + ' ' + visit.jam_reg + ' &nbsp;|&nbsp; dr. ' + (visit.nm_dokter || '-') + '</span>';
                html += '</div>';

                html += '<div class="row emr-flex-row" style="margin-bottom:10px;">';
                
                // SOAP Column (Left)
                html += '<div class="col-xs-12 col-md-6 emr-flex-col-left">';
                if (s) {
                    var badge = s.tipe=='Ranap'?'bg-red':'bg-teal';
                    html += '<div class="soap-card" style="display:flex; flex-direction:column; flex:1; margin:0; border:1px solid #e0e0e0; border-radius:6px; overflow:hidden;">' +
                        '<div class="soap-head" style="background:#f9f9f9; padding:8px 12px; border-bottom:1px solid #eee; font-size:11px; color:#666;">' +
                        '<span class="badge '+badge+'" style="font-size:9px; padding:2px 4px;">Pemeriksaan '+s.tipe+'</span> &nbsp; '+
                        '<i class="material-icons" style="font-size:12px;vertical-align:middle;">event</i> Periksa: '+s.tgl_perawatan+' '+s.jam_rawat+
                        '</div><div class="soap-body" style="padding:10px 12px; font-size:12px; flex:1; display:flex; flex-direction:column; justify-content:space-between;">'+
                        '<div>'+
                        '<div style="margin-bottom:6px; font-size:11px; color:#777;">Suhu: <b>'+s.suhu_tubuh+'°C</b> &nbsp;|&nbsp; Tensi: <b>'+s.tensi+' mmHg</b> &nbsp;|&nbsp; Nadi: <b>'+s.nadi+'</b> &nbsp;|&nbsp; SpO2: <b>'+s.spo2+'%</b></div>'+
                        '<p style="margin-bottom:6px;"><b>S / Keluhan:</b> '+(s.keluhan||'-')+'</p>'+
                        '<p style="margin-bottom:6px;"><b>O / Pemeriksaan:</b> '+(s.pemeriksaan||'-')+'</p>'+
                        '<p style="margin-bottom:6px;"><b>A / Asesmen:</b> '+(s.penilaian||'-')+'</p>'+
                        '<p style="margin:0;"><b>P / RTL:</b> '+(s.rtl||'-')+'</p>'+
                        '</div>'+
                        (s.instruksi?'<p style="margin-top:6px; margin-bottom:0; color:#c62828; font-size:11px;"><b>Instruksi:</b> '+s.instruksi+'</p>':'')+
                        '</div></div>';
                } else {
                    html += '<div class="soap-card" style="display:flex; flex-direction:column; flex:1; margin:0; border:1px dashed #ccc; border-radius:6px; background:#fafafa; justify-content:center; align-items:center; min-height:100px; padding:15px;">' +
                            '<span style="color:#aaa; font-style:italic; font-size:12px;">Tidak ada SOAP untuk kunjungan ini</span>' +
                            '</div>';
                }
                html += '</div>';

                // Resep Column (Right)
                html += '<div class="col-xs-12 col-md-6 emr-flex-col-right">';
                if (r) {
                    html += '<div class="soap-card" style="display:flex; flex-direction:column; flex:1; margin:0; border:1px solid #e0e0e0; border-radius:6px; overflow:hidden;">' +
                        '<div class="soap-head" style="background:#f9f9f9; padding:8px 12px; border-bottom:1px solid #eee; font-size:11px; color:#666;">' +
                        '<i class="material-icons" style="font-size:12px;vertical-align:middle;margin-right:2px;">receipt</i> Resep: <b>'+r.no_resep+'</b> &nbsp;|&nbsp; '+r.tgl_peresepan+' '+r.jam_peresepan+
                        '</div><div class="soap-body" style="padding:10px 12px; font-size:12px; flex:1;">'+
                        '<ul style="margin:0; padding-left:16px;">';
                    if(r.items && r.items.length) {
                        r.items.forEach(function(item){ html += '<li><b>'+item.nama_brng+'</b> ('+item.jml+') &mdash; <small style="color:#666;">'+item.aturan_pakai+'</small></li>'; });
                    } else {
                        html += '<li style="color:#999;">Tidak ada item obat</li>';
                    }
                    html += '</ul></div></div>';
                } else {
                    html += '<div class="soap-card" style="display:flex; flex-direction:column; flex:1; margin:0; border:1px dashed #ccc; border-radius:6px; background:#fafafa; justify-content:center; align-items:center; min-height:100px; padding:15px;">' +
                            '<span style="color:#aaa; font-style:italic; font-size:12px;">Tidak ada resep untuk kunjungan ini</span>' +
                            '</div>';
                }
                html += '</div>';

                html += '</div>'; // close row
            });
        }
        document.getElementById('emrBody').innerHTML = html;
    }, 'json').fail(function(){ document.getElementById('emrBody').innerHTML='<div class="alert alert-danger">Gagal memuat data EMR.</div>'; });
}

// -------- RADIOLOGI MODAL --------
function openRadiologi(norm, nama) {
    document.getElementById('radNama').innerText = nama;
    document.getElementById('radBody').innerHTML = SPINNER;
    $('#modalRadiologi').modal('show');
    $.post('pages/ajax_radiologi.php', {no_rkm_medis: norm}, function(data) {
        if(!data || !data.length){ document.getElementById('radBody').innerHTML='<div class="alert alert-info">Belum ada data pemeriksaan radiologi.</div>'; return; }
        var html='';
        data.forEach(function(r){
            var badge = r.sudah_dibaca=='1' ? '<span class="badge bg-teal" style="font-size:10px; padding:2px 6px;">&#10003; Sudah Dibaca</span>' : '<span class="badge bg-orange" style="font-size:10px; padding:2px 6px;">Belum Dibaca</span>';
            html += '<div class="soap-card" style="margin-bottom:12px;">';
            html += '<div class="soap-head" style="cursor:pointer; display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:8px;" onclick="toggleRadDetail(this)" data-norawat="'+r.no_rawat+'" data-tgl="'+r.tgl_periksa+'" data-jam="'+r.jam+'">';
            html += '<span><i class="material-icons" style="font-size:16px; vertical-align:middle; margin-right:4px;">expand_more</i> <b>'+r.nm_perawatan+'</b> <small style="color:#666; margin-left:8px;">('+r.tgl_fmt+' '+r.jam+')</small></span>';
            html += '<span>'+badge+'</span>';
            html += '</div><div class="rad-detail-body" style="display:none; padding:0; background:#fdfdfd;"></div></div>';
        });
        document.getElementById('radBody').innerHTML = html;
    }, 'json').fail(function(){ document.getElementById('radBody').innerHTML='<div class="alert alert-danger">Gagal memuat data radiologi.</div>'; });
}

function toggleRadDetail(header) {
    var body = header.nextElementSibling;
    if(body.style.display!='none'){ body.style.display='none'; return; }
    if(body.innerHTML.trim()!='') { body.style.display='block'; return; }
    body.style.display='block';
    body.innerHTML='<div style="padding:20px; text-align:center;"><div class="preloader pl-size-md"><div class="spinner-layer pl-orange"><div class="circle-clipper left"><div class="circle"></div></div><div class="circle-clipper right"><div class="circle"></div></div></div></div></div>';
    $.post('pages/ajax_radiologi_detail.php', {
        no_rawat:     header.dataset.norawat,
        tgl_periksa:  header.dataset.tgl,
        jam:          header.dataset.jam
    }, function(d){
        $(body).html(d);
    }, 'html').fail(function(){ $(body).html('<div class="alert alert-danger" style="margin:10px;">Gagal memuat citra/bacaan radiologi.</div>'); });
}

// -------- LABORAT MODAL --------
function openLaborat(norawat, norm, nama) {
    document.getElementById('labNama').innerText = nama;
    document.getElementById('labBody').innerHTML = SPINNER;
    $('#modalLaborat').modal('show');
    // First fetch the list of lab exams for this patient
    $.post('pages/ajax_laborat_list.php', {no_rkm_medis: norm}, function(data) {
        if(!data || !data.length){ document.getElementById('labBody').innerHTML='<div class="alert alert-info">Belum ada data pemeriksaan laborat.</div>'; return; }
        var html='';
        data.forEach(function(exam){
            html+='<div class="soap-card" style="margin-bottom:16px;">';
            html+='<div class="soap-head" style="cursor:pointer;" onclick="toggleLabDetail(this)" data-norawat="'+exam.no_rawat+'" data-kd="'+exam.kd_jenis_prw+'" data-tgl="'+exam.tgl_periksa+'" data-jam="'+exam.jam+'">';
            html+='<i class="material-icons" style="font-size:16px;vertical-align:middle;">expand_more</i> ';
            html+='<b>'+exam.nm_perawatan+'</b> &nbsp;<span class="badge bg-indigo">'+exam.kategori+'</span>&nbsp; '+exam.tgl_fmt+' '+exam.jam;
            html+='</div><div class="soap-body lab-detail-body" style="display:none; padding:0;"></div></div>';
        });
        document.getElementById('labBody').innerHTML=html;
    }, 'json').fail(function(){ document.getElementById('labBody').innerHTML='<div class="alert alert-danger">Gagal memuat data laborat.</div>'; });
}

function toggleLabDetail(header) {
    var body = header.nextElementSibling;
    if(body.style.display!='none'){ body.style.display='none'; return; }
    if(body.innerHTML.trim()!='') { body.style.display='block'; return; }
    body.style.display='block';
    body.innerHTML='<div style="padding:20px; text-align:center;">Memuat...</div>';
    $.post('pages/ajax_laborat.php', {
        no_rawat:      header.dataset.norawat,
        kd_jenis_prw:  header.dataset.kd,
        tgl_periksa:   header.dataset.tgl,
        jam:           header.dataset.jam
    }, function(d){
        var html='<div style="padding:10px;"><table class="table table-bordered" style="font-size:12px; margin:0;">';
        html+='<thead><tr><th>Pemeriksaan</th><th>Nilai</th><th>Satuan</th><th>Rujukan</th><th>Ket</th></tr></thead><tbody>';
        if(d.items && d.items.length){
            d.items.forEach(function(i){
                var cls=''; var ket=i.keterangan||'';
                if(ket=='H') cls='lab-H';
                else if(ket=='L') cls='lab-L';
                html+='<tr class="'+cls+'"><td>'+i.Pemeriksaan+'</td><td>'+i.nilai+'</td><td>'+i.satuan+'</td><td>'+i.nilai_rujukan+'</td><td>'+ket+'</td></tr>';
            });
        } else { html+='<tr><td colspan="5" class="text-center">Tidak ada detail item.</td></tr>'; }
        html+='</tbody></table></div>';
        body.innerHTML=html;
    },'json').fail(function(){ body.innerHTML='<div class="alert alert-danger" style="margin:10px;">Gagal memuat detail.</div>'; });
}
</script>