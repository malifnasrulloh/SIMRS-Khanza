<?php
require_once("../conf/conf.php");
session_start();
if(!isset($_SESSION["ses_dokter"])) { header("Content-Type: application/json"); echo json_encode([]); exit; }

header("Content-Type: application/json");

$norm      = validTeks(isset($_POST['no_rkm_medis'])   ? $_POST['no_rkm_medis']   : '');
$no_rawat  = validTeks(isset($_POST['no_rawat'])        ? $_POST['no_rawat']        : '');
$kd_jenis  = validTeks(isset($_POST['kd_jenis_prw'])   ? $_POST['kd_jenis_prw']   : '');
$tgl       = validTeks(isset($_POST['tgl_periksa'])     ? $_POST['tgl_periksa']     : '');
$jam       = validTeks(isset($_POST['jam'])              ? $_POST['jam']              : '');

if(empty($no_rawat) || empty($kd_jenis) || empty($tgl) || empty($jam)) {
    echo json_encode(['error'=>'Parameter tidak lengkap']); exit;
}

// Header
$header = [];
$qh = bukaquery("
    SELECT j.nm_perawatan, p.kategori
    FROM periksa_lab p
    INNER JOIN jns_perawatan_lab j ON p.kd_jenis_prw = j.kd_jenis_prw
    WHERE p.no_rawat='$no_rawat' AND p.kd_jenis_prw='$kd_jenis' AND p.tgl_periksa='$tgl' AND p.jam='$jam'
    LIMIT 1
");
if($r = mysqli_fetch_assoc($qh)) { $header = $r; }

// Detail items
$items = [];
$qi = bukaquery("
    SELECT t.Pemeriksaan, t.satuan, d.nilai, d.nilai_rujukan, d.keterangan
    FROM detail_periksa_lab d
    INNER JOIN template_laboratorium t ON d.id_template = t.id_template
    WHERE d.no_rawat='$no_rawat' AND d.kd_jenis_prw='$kd_jenis' AND d.tgl_periksa='$tgl' AND d.jam='$jam'
    ORDER BY t.urut ASC
");
while($r = mysqli_fetch_assoc($qi)) { $items[] = $r; }

echo json_encode(['header'=>$header, 'items'=>$items]);
