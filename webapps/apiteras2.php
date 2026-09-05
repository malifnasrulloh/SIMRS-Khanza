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
    CURLOPT_TIMEOUT => 0,
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
$token = json_decode(curl_exec($curl), true);
curl_close($curl);

// === 2. Ambil No Permintaan ===
$nopermintaan = validTeks(isset($_GET["nopermintaan"]) ? $_GET["nopermintaan"] : NULL);
$nopermintaan = str_replace("PL", "", $nopermintaan);
echo "no_lab=$nopermintaan";

bukaquery("INSERT INTO test(data) VALUES('No Permintaan: $nopermintaan')");

// === 3. Ambil Hasil Pemeriksaan ===
$curl2 = curl_init();
curl_setopt_array($curl2, [
    CURLOPT_URL => "https://demo2.terassekawanbersama.co.id/ws/",
    CURLOPT_RETURNTRANSFER => true,
    CURLOPT_ENCODING => "",
    CURLOPT_MAXREDIRS => 10,
    CURLOPT_TIMEOUT => 0,
    CURLOPT_FOLLOWLOCATION => true,
    CURLOPT_HTTP_VERSION => CURL_HTTP_VERSION_1_1,
    CURLOPT_CUSTOMREQUEST => "POST",
    CURLOPT_HTTPHEADER => [
        "x-token:".($token['token'] ?? $token["token"] ?? ""),
        "x-mod: get_hasil",
        "x-noo:$nopermintaan"
    ]
]);
$response = curl_exec($curl2);
$curl_err = curl_error($curl2);
curl_close($curl2);
$data_json = json_decode($response, true);
$json_err = json_last_error_msg();

bukaquery("INSERT INTO test(data) VALUES('len:".strlen($response ?? '')." json_err:".validTeks($json_err)."')");
if ($curl_err) bukaquery("INSERT INTO test(data) VALUES('curl_err: ".validTeks($curl_err)."')");

// === 4. Proses Data Jika Ada Pemeriksaan ===
if (isset($data_json['pemeriksaan']) && is_array($data_json['pemeriksaan'])) {
    $noorder = validTeks($data_json['order']['HID'] ?? $nopermintaan);
    $data1 = fetch_assoc("SELECT * FROM permintaan_lab WHERE noorder = '$noorder'");
    
    if (!$data1) {
        bukaquery("INSERT INTO test(data) VALUES('No Order $noorder Tidak ditemukan dalam table permintaan_lab.')");
    } else {
        $norawat = $data1['no_rawat'];
        $tgl_validasi_full = $data_json['order']['tgl_validasi'] ?? $data_json['order']['tgl_selesai'] ?? "";
        $tgl = date("Y-m-d");
        $jam = date("H:i:s");
        if (!empty($tgl_validasi_full) && strpos($tgl_validasi_full, " ") !== false) {
            $parts = explode(" ", trim($tgl_validasi_full));
            if (count($parts)==2 && $parts[0]!="0000-00-00" && $parts[0]!="") { $tgl=$parts[0]; $jam=$parts[1]; }
        }
        
        bukaquery("INSERT INTO test(data) VALUES('No Order dari API: $noorder')");
        bukaquery("INSERT INTO test(data) VALUES('No Rawat: $norawat | No Order: $noorder | tgl_validasi: $tgl $jam')");

        // Build map: lokal id_template = TERAS idh/key (3630) atau idi (100105)
        $map = [];
        foreach ($data_json['pemeriksaan'] as $key => $val) {
            if ($key==="" || $key===null) continue;
            if (!is_array($val)) continue;
            if (($val['status'] ?? "") != "1") continue;
            $idh = trim($val['idh'] ?? "");
            $idi = trim($val['idi'] ?? "");
            $k = trim($key);
            if ($idi=="" && $idh=="" && $k=="") continue;
            if (trim($val['hasil'] ?? "")=="" && trim($val['has_ket'] ?? "")=="" && $idi=="100432") continue;
            if ($k!=="") $map[$k] = $val;
            if ($idh!=="") $map[$idh] = $val;
            if ($idi!=="" && $idi!=="0") $map[$idi] = $val;
        }
        bukaquery("INSERT INTO test(data) VALUES('Map size: ".count($map)." keys: ".implode(',', array_keys($map))."')");

        // === 5. Auto Update tgl_sampel & jam_sampel jika masih kosong ('0000-00-00') - Q2A / Q3A ===
        if (($data1['tgl_sampel']=="0000-00-00" || empty($data1['tgl_sampel'])) && ($data1['jam_sampel']=="00:00:00" || empty($data1['jam_sampel']))) {
            $tgl_sampel_src = trim($data_json['order']['tgl_cekin'] ?? "");
            if (empty($tgl_sampel_src) || $tgl_sampel_src=="0000-00-00" || $tgl_sampel_src=="0000-00-00 00:00:00") {
                bukaquery("INSERT INTO test(data) VALUES('Skip tgl_sampel: order.tgl_cekin empty')");
            } else if (strpos($tgl_sampel_src," ")!==false) {
                $p = explode(" ", $tgl_sampel_src);
                if (count($p)==2 && $p[0]!="0000-00-00" && $p[0]!="") {
                    $ts_tgl = validTeks($p[0]); $ts_jam = validTeks($p[1]);
                    bukaquery("UPDATE permintaan_lab SET tgl_sampel='$ts_tgl', jam_sampel='$ts_jam' WHERE noorder='$noorder' AND tgl_sampel='0000-00-00'");
                    bukaquery("INSERT INTO test(data) VALUES('Auto tgl_sampel: $ts_tgl $ts_jam dari order.tgl_cekin')");
                }
            }
        }

        // === 6. Upsert Saran & Kesan Lab (Q3B) ===
        $saran = validTeks($data_json['order']['catatan']['analis'] ?? '');
        $kesan = validTeks($data_json['order']['catatan']['pk'] ?? '');
        $cek = fetch_assoc("SELECT * FROM saran_kesan_lab WHERE no_rawat='$norawat' AND tgl_periksa='$tgl' AND jam='$jam'");
        if ($cek) {
            bukaquery("UPDATE saran_kesan_lab SET saran='$saran', kesan='$kesan' WHERE no_rawat='$norawat' AND tgl_periksa='$tgl' AND jam='$jam'");
            bukaquery("INSERT INTO test(data) VALUES('Update saran & kesan untuk No Rawat: $norawat')");
        } else {
            bukaquery("INSERT INTO saran_kesan_lab (no_rawat, tgl_periksa, jam, saran, kesan) VALUES ('$norawat', '$tgl', '$jam', '$saran', '$kesan')");
            bukaquery("INSERT INTO test(data) VALUES('Insert saran & kesan untuk No Rawat: $norawat')");
        }

        // === 7. Clean up temporary_permintaan_lab untuk order ini (Q1A) ===
        bukaquery("DELETE FROM temporary_permintaan_lab WHERE temp1='$noorder'");

        // === 8. Insert ke temporary_permintaan_lab untuk konfirmasi verifikasi user di DlgPeriksaLaboratorium ===
        // Schema DlgPeriksaLaboratorium.java:2182 (tampilLICA):
        // SELECT temp1,temp2,temp3,temp4,temp5,temp6,temp7 FROM temporary_permintaan_lab WHERE temp7=? (id_template) AND temp1=? (noorder)
        // temp1 = noorder
        // temp2 = nama test
        // temp3 = hasil
        // temp4 = nilai rujukan (nilnor)
        // temp5 = satuan (UnitTest)
        // temp6 = keterangan (kombinasi [flag] has_ket sesuai Q2A)
        // temp7 = id_template
        $qDetail = bukaquery("SELECT permintaan_detail_permintaan_lab.kd_jenis_prw, permintaan_detail_permintaan_lab.id_template FROM permintaan_detail_permintaan_lab WHERE permintaan_detail_permintaan_lab.noorder='$noorder'");
        $cnt_temp = 0;
        while ($row = mysqli_fetch_assoc($qDetail)) {
            $id_template = $row['id_template'];
            if (!isset($map[$id_template])) continue;
            $val = $map[$id_template];
            $hasilx = validTeks($val['hasil'] ?? '');
            $nilnorx = $val['nilnor'] ?? '';
            $nilnor = validTeks(str_replace("&lt;", "<", $nilnorx));
            $namatest = validTeks($val['NmTestInd'] ?? '');
            $satuan = validTeks($val['UnitTest'] ?? '');
            
            // Q2A: Gabungkan flag dan has_ket
            $flag = trim($val['flag'] ?? '');
            $has_ket = trim($val['has_ket'] ?? '');
            $keterangan = '';
            if (!empty($flag) && !empty($has_ket)) {
                $keterangan = "[$flag] $has_ket";
            } else if (!empty($flag)) {
                $keterangan = "[$flag]";
            } else if (!empty($has_ket)) {
                $keterangan = $has_ket;
            }
            $keterangan = validTeks($keterangan);

            bukaquery("INSERT INTO temporary_permintaan_lab VALUES ('$cnt_temp', '$noorder', '$namatest', '$hasilx', '$nilnor', '$satuan', '$keterangan', '$id_template', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '', '')");
            $cnt_temp++;
        }
        bukaquery("INSERT INTO test(data) VALUES('Insert temporary_permintaan_lab count: $cnt_temp untuk No Order $noorder')");
    }

} else {
    bukaquery("INSERT INTO test(data) VALUES('Data Hasil Pemeriksaan Tidak Ditemukan')");
    $pesan = urlencode("Data hasil pemeriksaan di TERAS LIS belum tersedia atau belum divalidasi.");
    echo "<meta http-equiv='refresh' content='1;URL=?aksi=GagalAmbilTeras&pesan=$pesan'>";
    exit;
}

bukaquery("INSERT INTO test(data) VALUES('[".date("Y-m-d H:i:s")."] Get Hasil Selesai untuk No Permintaan: $nopermintaan')");

// === 9. Redirect URL ke Java (Q3A) ===
// Tidak simpan langsung ke periksa_lab & detail_periksa_lab.
// Redirect trigger DlgCariPermintaanLab.java untuk buka DlgPeriksaLaboratorium.java
if (isset($data_json['status']) && $data_json['status']==1) {
    echo "<meta http-equiv='refresh' content='1;URL=?aksi=SuksesAmbilTeras&noorder=$noorder'>";
} else if (strpos(strtolower($response), '"status": 1')!==false || strpos(strtolower($response), '"status":1')!==false) {
    echo "<meta http-equiv='refresh' content='1;URL=?aksi=SuksesAmbilTeras&noorder=$noorder'>";
}
?>
