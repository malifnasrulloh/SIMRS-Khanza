<?php
require_once("../conf/conf.php");
session_start();
if(!isset($_SESSION["ses_dokter"])) { header("Content-Type: application/json"); echo json_encode([]); exit; }
header("Content-Type: application/json");

$norm = validTeks(isset($_POST['no_rkm_medis']) ? $_POST['no_rkm_medis'] : '');
if(empty($norm)) { echo json_encode(['error'=>'No RM wajib diisi']); exit; }

$kd_dokter = validTeks4(encrypt_decrypt($_SESSION["ses_dokter"],"d"),20);
$rows = [];
$q = bukaquery("
    SELECT pl.no_rawat, pl.kd_jenis_prw, pl.tgl_periksa, pl.jam, pl.kategori,
           DATE_FORMAT(pl.tgl_periksa,'%d-%m-%Y') as tgl_fmt,
           j.nm_perawatan
    FROM periksa_lab pl
    INNER JOIN reg_periksa rp ON pl.no_rawat = rp.no_rawat
    INNER JOIN jns_perawatan_lab j ON pl.kd_jenis_prw = j.kd_jenis_prw
    WHERE rp.no_rkm_medis = '$norm'
    ORDER BY pl.tgl_periksa DESC, pl.jam DESC
    LIMIT 30
");
while($r = mysqli_fetch_assoc($q)) { $rows[] = $r; }
echo json_encode($rows);
