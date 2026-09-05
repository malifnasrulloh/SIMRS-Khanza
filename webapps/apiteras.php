<?php
require_once('conf/conf.php');
date_default_timezone_set('Asia/Jayapura');

// === 1. Ambil Token ===
$curl = curl_init();
curl_setopt_array($curl, [
    CURLOPT_URL => "https://demo2.terassekawanbersama.co.id/ws/",
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_ENCODING => "",
    CURLOPT_MAXREDIRS => 10,
    CURLOPT_TIMEOUT => 5,
    CURLOPT_FOLLOWLOCATION => true,
    CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
    CURLOPT_CUSTOMREQUEST => "POST",
    CURLOPT_HTTPHEADER => [
        "x-user: demo",
        "x-secret: 123qwe",
        "x-mod: auth",
        "x-cid: 4301202030080005"
    ]
]);
$token_resp = curl_exec($curl);
$token_err = curl_error($curl);
curl_close($curl);

$token = json_decode($token_resp, true);
if (empty($token['token'])) {
    $pesan = urlencode("Gagal autentikasi ke TERAS LIS: " . ($token_err ?: ($token['pesan'] ?? 'Token tidak valid')));
    echo "<meta http-equiv='refresh' content='1;URL=?aksi=GagalKirim&pesan=$pesan'>";
    exit;
}

// === 2. Ambil No Permintaan & Query Order ===
$nopermintaan = validTeks4((isset($_GET["nopermintaan"]) ? $_GET["nopermintaan"] : NULL), 25);
$json = "";
$json2 = "";

$qrypermintaan = bukaquery("
    SELECT 
        permintaan_lab.noorder,
        permintaan_lab.no_rawat,
        reg_periksa.no_rkm_medis,
        pasien.nm_pasien,
        permintaan_lab.tgl_permintaan,
        IF(permintaan_lab.jam_permintaan='00:00:00','',permintaan_lab.jam_permintaan) AS jam_permintaan,
        pasien.tgl_lahir,
        pasien.jk,
        pasien.alamat,
        pasien.no_ktp,
        IF(permintaan_lab.tgl_sampel='0000-00-00','',permintaan_lab.tgl_sampel) AS tgl_sampel,
        IF(permintaan_lab.jam_sampel='00:00:00','',permintaan_lab.jam_sampel) AS jam_sampel,
        IF(permintaan_lab.tgl_hasil='0000-00-00','',permintaan_lab.tgl_hasil) AS tgl_hasil,
        IF(permintaan_lab.jam_hasil='00:00:00','',permintaan_lab.jam_hasil) AS jam_hasil,
        permintaan_lab.dokter_perujuk,
        IFNULL(dokter.nm_dokter,'-') AS nm_dokter,
        IFNULL(poliklinik.nm_poli,'-') AS nm_poli,
        pasien.no_tlp,
        IFNULL(penjab.png_jawab,'-') AS png_jawab,
        pasien.no_peserta,
        YEAR(FROM_DAYS(DATEDIFF(NOW(), pasien.tgl_lahir))) AS tahun,
        MONTH(FROM_DAYS(DATEDIFF(NOW(), pasien.tgl_lahir))) AS bulan,
        DAY(FROM_DAYS(DATEDIFF(NOW(), pasien.tgl_lahir))) AS hari,
        permintaan_lab.status,
        IFNULL(reg_periksa.kd_poli,'-') AS kd_poli,
        reg_periksa.kd_pj,
        permintaan_lab.diagnosa_klinis,
        permintaan_lab.informasi_tambahan
    FROM permintaan_lab
    INNER JOIN reg_periksa ON permintaan_lab.no_rawat = reg_periksa.no_rawat
    INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis
    LEFT JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj
    LEFT JOIN dokter ON permintaan_lab.dokter_perujuk = dokter.kd_dokter
    LEFT JOIN poliklinik ON reg_periksa.kd_poli = poliklinik.kd_poli
    WHERE permintaan_lab.noorder = '$nopermintaan'
");

while ($rsqrypermintaan = mysqli_fetch_array($qrypermintaan)) {
    $qrydetailpermintaan = bukaquery("
        SELECT permintaan_detail_permintaan_lab.id_template, template_laboratorium.Pemeriksaan,
        template_laboratorium.urut FROM permintaan_detail_permintaan_lab 
        INNER JOIN template_laboratorium ON permintaan_detail_permintaan_lab.id_template=template_laboratorium.id_template 
        WHERE permintaan_detail_permintaan_lab.noorder='$nopermintaan' 
        ORDER BY template_laboratorium.kd_jenis_prw, template_laboratorium.urut DESC
    ");
    while ($rsqrydetailpermintaan = mysqli_fetch_array($qrydetailpermintaan)) {
        $json2 = '{"id_pemeriksaan": ' . $rsqrydetailpermintaan['id_template'] . ',"status": "add"},' . $json2;
    }

    $noRawat = $rsqrypermintaan['no_rawat'];
    $kamar = "";
    if ($rsqrypermintaan['status'] == "ralan") {
        $kamar = $rsqrypermintaan['kd_poli'];
    } else {
        $qbed = bukaquery("SELECT kamar_inap.kd_kamar FROM kamar_inap WHERE no_rawat='$noRawat' AND tgl_keluar='0000-00-00'");
        $rsqbed = mysqli_fetch_array($qbed);
        $bed = $rsqbed['kd_kamar'] ?? '';
        $qkamar = bukaquery("SELECT kd_bangsal FROM kamar WHERE kd_kamar='$bed'");
        $rsqkamar = mysqli_fetch_array($qkamar);
        $kamar = $rsqkamar['kd_bangsal'] ?? $rsqrypermintaan['kd_poli'];
    }

    $json2 = substr_replace($json2, "", -1);
    $json  = '{
                "data_pasien": {
                    "no_rekam": "' . $rsqrypermintaan['no_rkm_medis'] . '",
                    "no_ref": "' . $rsqrypermintaan['no_ktp'] . '",
                    "no_bpjs": "' . $rsqrypermintaan['no_peserta'] . '",
                    "sebutan": "' . ($rsqrypermintaan['jk'] == "L" ? "Tn" : "Ny") . '",
                    "nama_pasien": "' . addslashes($rsqrypermintaan['nm_pasien']) . '",
                    "jenis_kelamin": "' . ($rsqrypermintaan['jk'] == "L" ? "0" : "1") . '",
                    "tgl_lahir": "' . $rsqrypermintaan['tgl_lahir'] . '",
                    "y": ' . (int)$rsqrypermintaan['tahun'] . ',
                    "m": ' . (int)$rsqrypermintaan['bulan'] . ',
                    "d": ' . (int)$rsqrypermintaan['hari'] . ',
                    "jam": 0,
                    "alamat": "' . addslashes($rsqrypermintaan['alamat']) . '",
                    "telp": "' . $rsqrypermintaan['no_tlp'] . '"
                },
                "data_order": {
                    "status_pasien": "' . $rsqrypermintaan['kd_pj'] . '",
                    "ruang": "' . $kamar . '",
                    "dokter_pengirim": "' . $rsqrypermintaan['dokter_perujuk'] . '",
                    "dokter_pk": "' . getOne("SELECT kd_dokterlab FROM set_pjlab") . '",
                    "bahasa": "id",
                    "diagnosa": "' . addslashes($rsqrypermintaan['diagnosa_klinis']) . '",
                    "cito": ' . (strpos(strtolower($rsqrypermintaan['informasi_tambahan']), 'cito') === false ? "0" : "1") . ',
                    "golongan": "' . ($rsqrypermintaan['status'] == "ralan" ? "RJ" : "RI") . '"
                },
                "pemeriksaan":[' . $json2 . '],
                "no_lab": "' . $rsqrypermintaan['noorder'] . '"
            }';
}

if (empty($json)) {
    $pesan = urlencode("Data permintaan $nopermintaan tidak ditemukan di database.");
    echo "<meta http-equiv='refresh' content='1;URL=?aksi=GagalKirim&pesan=$pesan'>";
    exit;
}

// === 3. Kirim Order ke TERAS ===
$curl2 = curl_init();
curl_setopt_array($curl2, [
    CURLOPT_URL => "https://demo2.terassekawanbersama.co.id/ws/",
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_ENCODING => "",
    CURLOPT_MAXREDIRS => 10,
    CURLOPT_TIMEOUT => 10,
    CURLOPT_FOLLOWLOCATION => true,
    CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
    CURLOPT_CUSTOMREQUEST => "POST",
    CURLOPT_POSTFIELDS => $json,
    CURLOPT_HTTPHEADER => [
        "x-token:" . $token['token'],
        "x-mod: order"
    ]
]);

$response_raw = curl_exec($curl2);
$curl_err2 = curl_error($curl2);
curl_close($curl2);

$response = json_decode($response_raw, true);

bukaquery("INSERT INTO test(data) VALUES('Send Order $nopermintaan to TERAS: " . addslashes(substr($response_raw, 0, 500)) . "')");

$pesan_resp = strtolower($response["pesan"] ?? "");
if ((isset($response['status']) && $response['status'] == 1) || strpos($pesan_resp, 'created') !== false || strpos($pesan_resp, 'berhasil') !== false) {
    echo "<meta http-equiv='refresh' content='1;URL=?aksi=SuksesKirim'>";
} else {
    $err_msg = $response["pesan"] ?? $curl_err2 ?? "Unknown error dari TERAS";
    $pesan = urlencode("Gagal kirim order: " . $err_msg);
    echo "<meta http-equiv='refresh' content='1;URL=?aksi=GagalKirim&pesan=$pesan'>";
}
?>
