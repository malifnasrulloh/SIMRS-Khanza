<?php
header("Content-Type: application/json; charset=UTF-8");
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");

require_once('conf/conf.php');

$type = isset($_GET['type']) ? cleanKar($_GET['type']) : 'combined';
if (!in_array($type, ['combined', 'nonracikan', 'racikan'])) {
    $type = 'combined';
}

$response = [
    'penyerahan' => null,
    'validasi' => null
];

$racikFilter2 = "";
$racikFilter3 = "";

if ($type === 'nonracikan') {
    $racikFilter2 = " and antriapotek2.no_resep not in(select distinct resep_dokter_racikan.no_resep from resep_dokter_racikan)";
    $racikFilter3 = " and antriapotek3.no_resep not in(select distinct resep_dokter_racikan.no_resep from resep_dokter_racikan)";
} elseif ($type === 'racikan') {
    $racikFilter2 = " and antriapotek2.no_resep in(select distinct resep_dokter_racikan.no_resep from resep_dokter_racikan)";
    $racikFilter3 = " and antriapotek3.no_resep in(select distinct resep_dokter_racikan.no_resep from resep_dokter_racikan)";
}

// 1. Check Penyerahan (antriapotek3)
$sqlPenyerahan = "select antriapotek3.no_resep, antriapotek3.no_rawat, antriapotek3.status from antriapotek3 where antriapotek3.status='1'" . $racikFilter3;
$resPenyerahan = bukaquery($sqlPenyerahan);
if ($resPenyerahan && mysqli_num_rows($resPenyerahan) > 0) {
    while ($row = mysqli_fetch_array($resPenyerahan)) {
        $namaPasien = getOne("select pasien.nm_pasien from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat='" . $row['no_rawat'] . "'");
        $response['penyerahan'] = [
            'no_resep' => $row['no_resep'],
            'no_rawat' => $row['no_rawat'],
            'nama' => !empty($namaPasien) ? $namaPasien : 'Pasien'
        ];
        bukaquery2("update antriapotek3 set status='0' where no_resep='" . $row['no_resep'] . "' and no_rawat='" . $row['no_rawat'] . "'");
    }
}

// 2. Check Validasi (antriapotek2)
$sqlValidasi = "select antriapotek2.no_resep, antriapotek2.no_rawat, antriapotek2.status from antriapotek2 where antriapotek2.status='1'" . $racikFilter2;
$resValidasi = bukaquery($sqlValidasi);
if ($resValidasi && mysqli_num_rows($resValidasi) > 0) {
    while ($row = mysqli_fetch_array($resValidasi)) {
        $namaPasien = getOne("select pasien.nm_pasien from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis where reg_periksa.no_rawat='" . $row['no_rawat'] . "'");
        $response['validasi'] = [
            'no_resep' => $row['no_resep'],
            'no_rawat' => $row['no_rawat'],
            'nama' => !empty($namaPasien) ? $namaPasien : 'Pasien'
        ];
        bukaquery2("update antriapotek2 set status='0' where no_resep='" . $row['no_resep'] . "' and no_rawat='" . $row['no_rawat'] . "'");
    }
}

echo json_encode($response);
