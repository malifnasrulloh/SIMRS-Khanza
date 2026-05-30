<?php
require_once("../conf/conf.php");
session_start();
if(!isset($_SESSION["ses_dokter"])) { header("Content-Type: application/json"); echo json_encode([]); exit; }

header("Content-Type: application/json");

$norm = validTeks(isset($_POST['no_rkm_medis']) ? $_POST['no_rkm_medis'] : '');
if(empty($norm)) { echo json_encode(['error'=>'No RM wajib diisi']); exit; }

// 1. Fetch the last 20 registration visits for this patient
$visits = [];
$qv = bukaquery("
    SELECT r.no_rawat, r.tgl_registrasi, r.jam_reg, r.status_lanjut, d.nm_dokter
    FROM reg_periksa r
    LEFT JOIN dokter d ON r.kd_dokter = d.kd_dokter
    WHERE r.no_rkm_medis = '$norm'
    ORDER BY r.tgl_registrasi DESC, r.jam_reg DESC
    LIMIT 20
");

while($v = mysqli_fetch_assoc($qv)) {
    $norawat = $v['no_rawat'];
    $tipe = $v['status_lanjut']; // 'Ralan' or 'Ranap'

    // 2. Fetch SOAP details matching this specific no_rawat
    $soap = null;
    $table_soap = ($tipe == 'Ranap') ? 'pemeriksaan_ranap' : 'pemeriksaan_ralan';
    
    // Check if SOAP table has record
    $qs = bukaquery("
        SELECT p.tgl_perawatan, p.jam_rawat, p.keluhan, p.pemeriksaan, p.penilaian, p.rtl, p.instruksi, p.evaluasi,
               p.suhu_tubuh, p.tensi, p.nadi, p.respirasi, p.berat, p.tinggi, p.spo2, p.kesadaran, d.nm_dokter
        FROM $table_soap p
        LEFT JOIN dokter d ON p.nip = d.kd_dokter
        WHERE p.no_rawat = '$norawat'
        LIMIT 1
    ");
    
    if($qs && mysqli_num_rows($qs) > 0) {
        $soap = mysqli_fetch_assoc($qs);
        $soap['tipe'] = $tipe;
    }

    // 3. Fetch Resep details matching this specific no_rawat
    $resep = null;
    $qr = bukaquery("
        SELECT ro.no_resep, ro.tgl_peresepan, ro.jam_peresepan
        FROM resep_obat ro
        WHERE ro.no_rawat = '$norawat'
        LIMIT 1
    ");
    
    if($qr && mysqli_num_rows($qr) > 0) {
        $resep = mysqli_fetch_assoc($qr);
        $no_resep = $resep['no_resep'];
        $items = [];
        $qi = bukaquery("
            SELECT rd.jml, rd.aturan_pakai, db.nama_brng
            FROM resep_dokter rd
            INNER JOIN databarang db ON rd.kode_brng = db.kode_brng
            WHERE rd.no_resep = '$no_resep'
        ");
        while($i = mysqli_fetch_assoc($qi)) { $items[] = $i; }
        $resep['items'] = $items;
    }

    // Only include this visit if it has *either* a SOAP entry *or* a Resep entry!
    if ($soap !== null || $resep !== null) {
        $visits[] = [
            'no_rawat'       => $norawat,
            'tgl_registrasi' => $v['tgl_registrasi'],
            'jam_reg'        => $v['jam_reg'],
            'status_lanjut'  => $tipe,
            'nm_dokter'      => $v['nm_dokter'],
            'soap'           => $soap,
            'resep'          => $resep
        ];
    }
}

echo json_encode(['visits' => $visits]);
