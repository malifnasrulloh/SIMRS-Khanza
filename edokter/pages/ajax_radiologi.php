<?php
require_once("../conf/conf.php");
session_start();
if(!isset($_SESSION["ses_dokter"])) { header("Content-Type: application/json"); echo json_encode([]); exit; }

header("Content-Type: application/json");

$norm = validTeks(isset($_POST['no_rkm_medis']) ? $_POST['no_rkm_medis'] : '');
if(empty($norm)) { echo json_encode(['error'=>'No RM wajib diisi']); exit; }

$rows = [];
$q = bukaquery("
    SELECT
        DATE_FORMAT(pr.tgl_periksa, '%d-%m-%Y') as tgl_fmt,
        pr.jam, pr.no_rawat, pr.kd_jenis_prw, pr.tgl_periksa,
        j.nm_perawatan,
        d.nm_dokter as nm_perujuk,
        CASE WHEN hr.no_rawat IS NOT NULL THEN 1 ELSE 0 END as sudah_dibaca,
        IFNULL(hr.hasil,'') as hasil_bacaan
    FROM periksa_radiologi pr
    INNER JOIN reg_periksa rp ON pr.no_rawat = rp.no_rawat
    INNER JOIN jns_perawatan_radiologi j ON pr.kd_jenis_prw = j.kd_jenis_prw
    LEFT JOIN dokter d ON pr.dokter_perujuk = d.kd_dokter
    LEFT JOIN hasil_radiologi hr ON pr.no_rawat=hr.no_rawat AND pr.tgl_periksa=hr.tgl_periksa AND pr.jam=hr.jam
    WHERE rp.no_rkm_medis = '$norm'
    ORDER BY pr.tgl_periksa DESC, pr.jam DESC
    LIMIT 20
");
while($r = mysqli_fetch_assoc($q)) { $rows[] = $r; }

echo json_encode($rows);
