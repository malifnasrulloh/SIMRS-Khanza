<?php
require_once("../conf/conf.php");
session_start();

if (!isset($_SESSION["ses_dokter"])) {
    header("Content-Type: application/json");
    echo json_encode(["dokter" => 0, "perawat" => 0, "play" => false]);
    exit;
}

$kdDokter = validTeks4(encrypt_decrypt($_SESSION["ses_dokter"], "d"), 20);

// Unanswered medik consultations
$medikCount = (int) getOne("
    SELECT COUNT(konsultasi_medik.no_permintaan)
    FROM konsultasi_medik
    WHERE NOT EXISTS (
        SELECT 1 FROM jawaban_konsultasi_medik
        WHERE jawaban_konsultasi_medik.no_permintaan = konsultasi_medik.no_permintaan
    )
    AND konsultasi_medik.kd_dokter_dikonsuli = '$kdDokter'
");

// Unanswered perawat consultations
$perawatCount = (int) getOne("
    SELECT COUNT(konsultasi_perawat.no_permintaan)
    FROM konsultasi_perawat
    WHERE NOT EXISTS (
        SELECT 1 FROM jawaban_konsultasi_perawat
        WHERE jawaban_konsultasi_perawat.no_permintaan = konsultasi_perawat.no_permintaan
    )
    AND konsultasi_perawat.kd_dokter_dikonsuli = '$kdDokter'
");

// Detect if counts increased from the last poll to play alert sound
$prevDokter = isset($_SESSION['prev_konsul_dokter']) ? (int)$_SESSION['prev_konsul_dokter'] : 0;
$prevPerawat = isset($_SESSION['prev_konsul_perawat']) ? (int)$_SESSION['prev_konsul_perawat'] : 0;

$play = false;
if ($medikCount > $prevDokter || $perawatCount > $prevPerawat) {
    $play = true;
}

$_SESSION['prev_konsul_dokter'] = $medikCount;
$_SESSION['prev_konsul_perawat'] = $perawatCount;

header("Content-Type: application/json");
echo json_encode([
    "dokter"  => $medikCount,
    "perawat" => $perawatCount,
    "play"    => $play
]);
