<?php
header("Content-Type: application/json; charset=UTF-8");
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");

require_once('conf/conf.php');

$action = isset($_GET['action']) ? validTeks4($_GET['action'], 30) : 'status';
$tanggal = date("Y-m-d");

// Helper: State file path with directory hash to prevent collision across multi-tenant/multi-database instances
$dirHash = substr(md5(__DIR__), 0, 8);
$stateFile = sys_get_temp_dir() . '/khanza_antrian_poli_' . $dirHash . '_' . $tanggal . '.json';

function getPoliState($stateFile, $tanggal) {
    if (file_exists($stateFile)) {
        $json = @file_get_contents($stateFile);
        $data = json_decode($json, true);
        if ($data && isset($data['tanggal']) && $data['tanggal'] === $tanggal) {
            return $data;
        }
    }
    return [
        'tanggal' => $tanggal,
        'call_sequence' => 0,
        'last_call_time' => date("Y-m-d H:i:s"),
        'last_hero' => null, // Last called patient across all clinics
        'channels' => [],    // Active call state per "kd_poli|kd_dokter" or "kd_poli" key
        'called_history' => [] // Channel-specific history of called no_rawats
    ];
}

function savePoliState($stateFile, $data) {
    $fp = @fopen($stateFile, 'c+');
    if ($fp) {
        if (flock($fp, LOCK_EX)) {
            ftruncate($fp, 0);
            fwrite($fp, json_encode($data));
            fflush($fp);
            flock($fp, LOCK_UN);
        }
        fclose($fp);
    }
}

// Action: Get list of active clinics (Poliklinik)
if ($action === 'get_poli_list') {
    $list = [];
    $res = bukaquery("select kd_poli, nm_poli from poliklinik where status='1' order by nm_poli asc");
    if ($res) {
        while ($row = mysqli_fetch_array($res)) {
            $list[] = [
                'kd_poli' => $row['kd_poli'],
                'nm_poli' => $row['nm_poli']
            ];
        }
    }
    echo json_encode(['status' => 'success', 'data' => $list]);
    exit();
}

// Action: Get list of doctors by clinic or all active doctors
if ($action === 'get_dokter_list') {
    $kd_poli = isset($_GET['kd_poli']) ? validTeks4($_GET['kd_poli'], 20) : '';
    $list = [];

    if (!empty($kd_poli)) {
        $sql = "select distinct dokter.kd_dokter, dokter.nm_dokter
                from jadwal inner join dokter on jadwal.kd_dokter=dokter.kd_dokter
                where jadwal.kd_poli='$kd_poli' and dokter.status='1'
                union
                select distinct dokter.kd_dokter, dokter.nm_dokter
                from reg_periksa inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter
                where reg_periksa.kd_poli='$kd_poli' and reg_periksa.tgl_registrasi='$tanggal'
                order by nm_dokter asc";
        $res = bukaquery($sql);
    } else {
        $res = bukaquery("select kd_dokter, nm_dokter from dokter where status='1' order by nm_dokter asc");
    }

    if ($res) {
        while ($row = mysqli_fetch_array($res)) {
            $list[] = [
                'kd_dokter' => $row['kd_dokter'],
                'nm_dokter' => $row['nm_dokter']
            ];
        }
    }
    echo json_encode(['status' => 'success', 'data' => $list]);
    exit();
}

// Helper: resolve channel key
function makeChannelKey($kd_poli, $kd_dokter = '') {
    if (!empty($kd_dokter)) {
        return trim($kd_poli) . '|' . trim($kd_dokter);
    }
    return trim($kd_poli);
}

// Action: Call Next Patient (Panggil Berikutnya)
if ($action === 'next') {
    $kd_poli = isset($_POST['kd_poli']) ? validTeks4($_POST['kd_poli'], 20) : (isset($_GET['kd_poli']) ? validTeks4($_GET['kd_poli'], 20) : '');
    $kd_dokter = isset($_POST['kd_dokter']) ? validTeks4($_POST['kd_dokter'], 20) : (isset($_GET['kd_dokter']) ? validTeks4($_GET['kd_dokter'], 20) : '');

    if (empty($kd_poli)) {
        echo json_encode(['status' => 'error', 'message' => 'Poliklinik parameter is required']);
        exit();
    }

    $state = getPoliState($stateFile, $tanggal);
    $chKey = makeChannelKey($kd_poli, $kd_dokter);

    if (!isset($state['called_history'][$chKey])) {
        $state['called_history'][$chKey] = [];
    }

    // Build exclusion list of previously called no_rawats in this channel
    $excludedNoRawats = $state['called_history'][$chKey];
    if (isset($state['channels'][$chKey]['no_rawat']) && !in_array($state['channels'][$chKey]['no_rawat'], $excludedNoRawats)) {
        $excludedNoRawats[] = $state['channels'][$chKey]['no_rawat'];
    }

    // Find next waiting patient from reg_periksa joined with mutasi_berkas
    $sqlNext = "select reg_periksa.no_reg, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                       pasien.nm_pasien, poliklinik.nm_poli, dokter.nm_dokter, reg_periksa.jam_reg, reg_periksa.kd_dokter, reg_periksa.stts
                from reg_periksa
                inner join pasien on reg_periksa.no_rkm_medis = pasien.no_rkm_medis
                inner join poliklinik on reg_periksa.kd_poli = poliklinik.kd_poli
                inner join dokter on reg_periksa.kd_dokter = dokter.kd_dokter
                left join mutasi_berkas on reg_periksa.no_rawat = mutasi_berkas.no_rawat
                where reg_periksa.kd_poli='$kd_poli'
                  and reg_periksa.tgl_registrasi='$tanggal'
                  and reg_periksa.stts not in ('Batal', 'Sudah')
                  and (mutasi_berkas.status in ('Sudah Dikirim', 'Sudah Diterima') or mutasi_berkas.no_rawat is null) ";

    if (!empty($kd_dokter)) {
        $sqlNext .= " and reg_periksa.kd_dokter='$kd_dokter' ";
    }

    if (!empty($excludedNoRawats)) {
        $escapedList = "'" . implode("','", array_map('addslashes', $excludedNoRawats)) . "'";
        $sqlNext .= " and reg_periksa.no_rawat not in ($escapedList) ";
    }

    $sqlNext .= " order by cast(reg_periksa.no_reg as unsigned) asc, reg_periksa.jam_reg asc limit 1";

    $resNext = bukaquery($sqlNext);
    $nextPatient = null;
    if ($resNext && $rowN = mysqli_fetch_array($resNext)) {
        $actualDoctor = $rowN['kd_dokter'];
        $nextPatient = [
            'no_reg' => $rowN['no_reg'],
            'no_rawat' => $rowN['no_rawat'],
            'no_rkm_medis' => $rowN['no_rkm_medis'],
            'nm_pasien' => $rowN['nm_pasien'],
            'nm_poli' => $rowN['nm_poli'],
            'nm_dokter' => $rowN['nm_dokter'],
            'jam_reg' => $rowN['jam_reg'],
            'kd_poli' => $kd_poli,
            'kd_dokter' => $actualDoctor
        ];
    }

    if (!$nextPatient) {
        echo json_encode(['status' => 'empty', 'message' => 'Tidak ada antrean pasien tersisa untuk poli ini']);
        exit();
    }

    // Sync with SIMRS Khanza legacy antripoli table with status=0 so polling does not double-trigger
    $docCode = $nextPatient['kd_dokter'];
    bukaquery2("delete from antripoli where kd_dokter='$docCode' and kd_poli='$kd_poli'");
    bukaquery2("insert into antripoli values('$docCode', '$kd_poli', '0', '" . addslashes($nextPatient['no_rawat']) . "')");

    // Add to called history
    if (!in_array($nextPatient['no_rawat'], $state['called_history'][$chKey])) {
        $state['called_history'][$chKey][] = $nextPatient['no_rawat'];
    }
    // Also track in poli-wide history
    if (!isset($state['called_history'][$kd_poli])) {
        $state['called_history'][$kd_poli] = [];
    }
    if (!in_array($nextPatient['no_rawat'], $state['called_history'][$kd_poli])) {
        $state['called_history'][$kd_poli][] = $nextPatient['no_rawat'];
    }

    // Update state cache
    $state['call_sequence']++;
    $nextPatient['call_id'] = $state['call_sequence'];
    $nextPatient['call_time'] = date("H:i:s");
    $nextPatient['channel_key'] = $chKey;
    $nextPatient['is_recall'] = false;

    $state['channels'][$chKey] = $nextPatient;
    $state['channels'][$kd_poli] = $nextPatient; // Keep poli-level reference for display screens
    $state['last_hero'] = $nextPatient;
    $state['last_call_time'] = date("Y-m-d H:i:s");

    savePoliState($stateFile, $state);

    echo json_encode([
        'status' => 'success',
        'action' => 'next',
        'patient' => $nextPatient,
        'call_id' => $state['call_sequence']
    ]);
    exit();
}

// Action: Panggil Ulang (Recall)
if ($action === 'recall') {
    $kd_poli = isset($_POST['kd_poli']) ? validTeks4($_POST['kd_poli'], 20) : (isset($_GET['kd_poli']) ? validTeks4($_GET['kd_poli'], 20) : '');
    $kd_dokter = isset($_POST['kd_dokter']) ? validTeks4($_POST['kd_dokter'], 20) : (isset($_GET['kd_dokter']) ? validTeks4($_GET['kd_dokter'], 20) : '');

    if (empty($kd_poli)) {
        echo json_encode(['status' => 'error', 'message' => 'Poli parameter is required']);
        exit();
    }

    $state = getPoliState($stateFile, $tanggal);
    $chKey = makeChannelKey($kd_poli, $kd_dokter);

    $currentPatient = isset($state['channels'][$chKey]) ? $state['channels'][$chKey] : (isset($state['channels'][$kd_poli]) ? $state['channels'][$kd_poli] : null);

    // If not in cache, check antripoli table
    if (!$currentPatient) {
        $sqlA = "select kd_dokter, no_rawat from antripoli where kd_poli='$kd_poli'";
        if (!empty($kd_dokter)) $sqlA .= " and kd_dokter='$kd_dokter'";
        $sqlA .= " limit 1";

        $resA = bukaquery($sqlA);
        if ($resA && $rowA = mysqli_fetch_array($resA)) {
            $nr = addslashes($rowA['no_rawat']);
            $resP = bukaquery("select reg_periksa.no_reg, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                                      pasien.nm_pasien, poliklinik.nm_poli, dokter.nm_dokter, reg_periksa.jam_reg, reg_periksa.kd_dokter
                               from reg_periksa
                               inner join pasien on reg_periksa.no_rkm_medis = pasien.no_rkm_medis
                               inner join poliklinik on reg_periksa.kd_poli = poliklinik.kd_poli
                               inner join dokter on reg_periksa.kd_dokter = dokter.kd_dokter
                               where reg_periksa.no_rawat='$nr' limit 1");
            if ($resP && $rowP = mysqli_fetch_array($resP)) {
                $currentPatient = [
                    'no_reg' => $rowP['no_reg'],
                    'no_rawat' => $rowP['no_rawat'],
                    'no_rkm_medis' => $rowP['no_rkm_medis'],
                    'nm_pasien' => $rowP['nm_pasien'],
                    'nm_poli' => $rowP['nm_poli'],
                    'nm_dokter' => $rowP['nm_dokter'],
                    'jam_reg' => $rowP['jam_reg'],
                    'kd_poli' => $kd_poli,
                    'kd_dokter' => $rowP['kd_dokter']
                ];
            }
        }
    }

    // Self-healing: verify patient still exists in reg_periksa
    if ($currentPatient && !empty($currentPatient['no_rawat'])) {
        $checkNr = addslashes($currentPatient['no_rawat']);
        $stillExists = getOne("select count(*) from reg_periksa where no_rawat='$checkNr' and stts not in ('Batal')");
        if (!$stillExists || intval($stillExists) === 0) {
            unset($state['channels'][$chKey]);
            unset($state['channels'][$kd_poli]);
            if (isset($state['last_hero']['no_rawat']) && $state['last_hero']['no_rawat'] === $currentPatient['no_rawat']) {
                $state['last_hero'] = null;
            }
            bukaquery2("delete from antripoli where kd_poli='$kd_poli' and no_rawat='$checkNr'");
            savePoliState($stateFile, $state);
            $currentPatient = null;
        }
    }

    if (!$currentPatient) {
        echo json_encode(['status' => 'empty', 'message' => 'Belum ada pasien yang sedang dipanggil di poli ini']);
        exit();
    }

    $docCode = $currentPatient['kd_dokter'];
    bukaquery2("update antripoli set status='0' where kd_dokter='$docCode' and kd_poli='$kd_poli'");

    $state['call_sequence']++;
    $currentPatient['call_id'] = $state['call_sequence'];
    $currentPatient['call_time'] = date("H:i:s");
    $currentPatient['channel_key'] = $chKey;
    $currentPatient['is_recall'] = true;

    $state['channels'][$chKey] = $currentPatient;
    $state['channels'][$kd_poli] = $currentPatient;
    $state['last_hero'] = $currentPatient;
    $state['last_call_time'] = date("Y-m-d H:i:s");

    savePoliState($stateFile, $state);

    echo json_encode([
        'status' => 'success',
        'action' => 'recall',
        'patient' => $currentPatient,
        'call_id' => $state['call_sequence']
    ]);
    exit();
}

// Action: Panggil Pasien Tertentu (Manual Call by No.Rawat)
if ($action === 'call_manual') {
    $kd_poli = isset($_POST['kd_poli']) ? validTeks4($_POST['kd_poli'], 20) : (isset($_GET['kd_poli']) ? validTeks4($_GET['kd_poli'], 20) : '');
    $kd_dokter = isset($_POST['kd_dokter']) ? validTeks4($_POST['kd_dokter'], 20) : (isset($_GET['kd_dokter']) ? validTeks4($_GET['kd_dokter'], 20) : '');
    $no_rawat = isset($_POST['no_rawat']) ? validTeks4($_POST['no_rawat'], 25) : (isset($_GET['no_rawat']) ? validTeks4($_GET['no_rawat'], 25) : '');

    if (empty($kd_poli) || empty($no_rawat)) {
        echo json_encode(['status' => 'error', 'message' => 'Poli and No.Rawat parameters are required']);
        exit();
    }

    $escapedNoRawat = addslashes($no_rawat);
    $resM = bukaquery("select reg_periksa.no_reg, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                              pasien.nm_pasien, poliklinik.nm_poli, dokter.nm_dokter, reg_periksa.jam_reg, reg_periksa.kd_dokter
                       from reg_periksa
                       inner join pasien on reg_periksa.no_rkm_medis = pasien.no_rkm_medis
                       inner join poliklinik on reg_periksa.kd_poli = poliklinik.kd_poli
                       inner join dokter on reg_periksa.kd_dokter = dokter.kd_dokter
                       where reg_periksa.no_rawat='$escapedNoRawat' limit 1");

    if (!$resM || !($rowM = mysqli_fetch_array($resM))) {
        echo json_encode(['status' => 'error', 'message' => 'Data pasien tidak ditemukan']);
        exit();
    }

    $actualDoctor = $rowM['kd_dokter'];
    $patient = [
        'no_reg' => $rowM['no_reg'],
        'no_rawat' => $rowM['no_rawat'],
        'no_rkm_medis' => $rowM['no_rkm_medis'],
        'nm_pasien' => $rowM['nm_pasien'],
        'nm_poli' => $rowM['nm_poli'],
        'nm_dokter' => $rowM['nm_dokter'],
        'jam_reg' => $rowM['jam_reg'],
        'kd_poli' => $kd_poli,
        'kd_dokter' => $actualDoctor
    ];

    bukaquery2("delete from antripoli where kd_dokter='$actualDoctor' and kd_poli='$kd_poli'");
    bukaquery2("insert into antripoli values('$actualDoctor', '$kd_poli', '0', '$escapedNoRawat')");

    $state = getPoliState($stateFile, $tanggal);
    $chKey = makeChannelKey($kd_poli, $actualDoctor);

    if (!isset($state['called_history'][$chKey])) {
        $state['called_history'][$chKey] = [];
    }
    if (!in_array($escapedNoRawat, $state['called_history'][$chKey])) {
        $state['called_history'][$chKey][] = $escapedNoRawat;
    }
    if (!isset($state['called_history'][$kd_poli])) {
        $state['called_history'][$kd_poli] = [];
    }
    if (!in_array($escapedNoRawat, $state['called_history'][$kd_poli])) {
        $state['called_history'][$kd_poli][] = $escapedNoRawat;
    }

    $state['call_sequence']++;
    $patient['call_id'] = $state['call_sequence'];
    $patient['call_time'] = date("H:i:s");
    $patient['channel_key'] = $chKey;
    $patient['is_recall'] = false;

    $state['channels'][$chKey] = $patient;
    $state['channels'][$kd_poli] = $patient;
    $state['last_hero'] = $patient;
    $state['last_call_time'] = date("Y-m-d H:i:s");

    savePoliState($stateFile, $state);

    echo json_encode([
        'status' => 'success',
        'action' => 'call_manual',
        'patient' => $patient,
        'call_id' => $state['call_sequence']
    ]);
    exit();
}

// Action: Reset / Selesai
if ($action === 'reset') {
    $kd_poli = isset($_POST['kd_poli']) ? validTeks4($_POST['kd_poli'], 20) : (isset($_GET['kd_poli']) ? validTeks4($_GET['kd_poli'], 20) : '');
    $kd_dokter = isset($_POST['kd_dokter']) ? validTeks4($_POST['kd_dokter'], 20) : (isset($_GET['kd_dokter']) ? validTeks4($_GET['kd_dokter'], 20) : '');

    if (!empty($kd_poli)) {
        if (!empty($kd_dokter)) {
            bukaquery2("delete from antripoli where kd_dokter='$kd_dokter' and kd_poli='$kd_poli'");
        } else {
            bukaquery2("delete from antripoli where kd_poli='$kd_poli'");
        }
        $state = getPoliState($stateFile, $tanggal);
        $chKey = makeChannelKey($kd_poli, $kd_dokter);
        unset($state['channels'][$chKey]);
        unset($state['channels'][$kd_poli]);
        if (isset($state['last_hero']['kd_poli']) && $state['last_hero']['kd_poli'] === $kd_poli) {
            $state['last_hero'] = null;
        }
        savePoliState($stateFile, $state);
    }

    echo json_encode(['status' => 'success', 'action' => 'reset']);
    exit();
}

// Default Action: Status Query for Multi-Poli / Single-Poli Displays & Web Caller
$state = getPoliState($stateFile, $tanggal);

// Parse requested channels (supports 'polis=U0001,U0002,U0003' or 'channels=U0001:D0001,U0002:D0002' or 'kd_poli=U0001')
$channelPairs = [];
if (isset($_GET['polis']) && !empty($_GET['polis'])) {
    $rawPolis = explode(',', $_GET['polis']);
    foreach ($rawPolis as $rawP) {
        $p = validTeks4(trim($rawP), 20);
        if (!empty($p)) {
            $channelPairs[] = ['kd_poli' => $p, 'kd_dokter' => ''];
        }
    }
} elseif (isset($_GET['channels']) && !empty($_GET['channels'])) {
    $rawPairs = explode(',', $_GET['channels']);
    foreach ($rawPairs as $pair) {
        $parts = explode(':', $pair);
        $p = validTeks4(trim($parts[0]), 20);
        $d = isset($parts[1]) ? validTeks4(trim($parts[1]), 20) : '';
        if (!empty($p)) {
            $channelPairs[] = ['kd_poli' => $p, 'kd_dokter' => $d];
        }
    }
} elseif (isset($_GET['kd_poli'])) {
    $p = validTeks4(trim($_GET['kd_poli']), 20);
    $d = isset($_GET['kd_dokter']) ? validTeks4(trim($_GET['kd_dokter']), 20) : '';
    if (!empty($p)) {
        $channelPairs[] = ['kd_poli' => $p, 'kd_dokter' => $d];
    }
}

// Check desktop antripoli table for external triggers (from Java desktop app only when status = '1')
$resAntri = bukaquery("select kd_dokter, kd_poli, status, no_rawat from antripoli where status='1'");
if ($resAntri) {
    while ($rowA = mysqli_fetch_array($resAntri)) {
        $p = $rowA['kd_poli'];
        $d = $rowA['kd_dokter'];
        $nr = addslashes($rowA['no_rawat']);
        $chKey = makeChannelKey($p, $d);

        $resP = bukaquery("select reg_periksa.no_reg, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                                  pasien.nm_pasien, poliklinik.nm_poli, dokter.nm_dokter, reg_periksa.jam_reg
                           from reg_periksa
                           inner join pasien on reg_periksa.no_rkm_medis = pasien.no_rkm_medis
                           inner join poliklinik on reg_periksa.kd_poli = poliklinik.kd_poli
                           inner join dokter on reg_periksa.kd_dokter = dokter.kd_dokter
                           where reg_periksa.no_rawat='$nr' and reg_periksa.stts not in ('Batal') limit 1");
        if ($resP && $rowP = mysqli_fetch_array($resP)) {
            $state['call_sequence']++;
            $syncPatient = [
                'no_reg' => $rowP['no_reg'],
                'no_rawat' => $rowP['no_rawat'],
                'no_rkm_medis' => $rowP['no_rkm_medis'],
                'nm_pasien' => $rowP['nm_pasien'],
                'nm_poli' => $rowP['nm_poli'],
                'nm_dokter' => $rowP['nm_dokter'],
                'jam_reg' => $rowP['jam_reg'],
                'kd_poli' => $p,
                'kd_dokter' => $d,
                'call_id' => $state['call_sequence'],
                'call_time' => date("H:i:s"),
                'channel_key' => $chKey,
                'is_recall' => true
            ];
            $state['channels'][$chKey] = $syncPatient;
            $state['channels'][$p] = $syncPatient;
            $state['last_hero'] = $syncPatient;
            $state['last_call_time'] = date("Y-m-d H:i:s");

            if (!isset($state['called_history'][$chKey])) $state['called_history'][$chKey] = [];
            if (!in_array($nr, $state['called_history'][$chKey])) $state['called_history'][$chKey][] = $nr;
            if (!isset($state['called_history'][$p])) $state['called_history'][$p] = [];
            if (!in_array($nr, $state['called_history'][$p])) $state['called_history'][$p][] = $nr;

            bukaquery2("update antripoli set status='0' where kd_dokter='$d' and kd_poli='$p'");
            savePoliState($stateFile, $state);
        } else {
            bukaquery2("delete from antripoli where kd_dokter='$d' and kd_poli='$p' and no_rawat='$nr'");
        }
    }
}

// Build response data for requested channels
$limitUpcoming = isset($_GET['limit']) ? intval($_GET['limit']) : 50;
if ($limitUpcoming < 1) $limitUpcoming = 50;

$channelsData = [];
$stateModified = false;

foreach ($channelPairs as $cp) {
    $p = $cp['kd_poli'];
    $d = $cp['kd_dokter'];
    $chKey = makeChannelKey($p, $d);

    $nmPoli = getOne("select nm_poli from poliklinik where kd_poli='$p'");

    // Check active patient in this channel
    $activePatient = null;
    if (!empty($d) && isset($state['channels'][$chKey])) {
        $activePatient = $state['channels'][$chKey];
    } elseif (isset($state['channels'][$p])) {
        $activePatient = $state['channels'][$p];
    }

    // Real-Time Self-Healing: Verify active patient still exists in reg_periksa and is not 'Batal'
    if ($activePatient && !empty($activePatient['no_rawat'])) {
        $checkNr = addslashes($activePatient['no_rawat']);
        $stillExists = getOne("select count(*) from reg_periksa where no_rawat='$checkNr' and stts not in ('Batal')");
        if (!$stillExists || intval($stillExists) === 0) {
            unset($state['channels'][$chKey]);
            unset($state['channels'][$p]);
            if (isset($state['last_hero']['no_rawat']) && $state['last_hero']['no_rawat'] === $activePatient['no_rawat']) {
                $state['last_hero'] = null;
            }
            bukaquery2("delete from antripoli where kd_poli='$p' and no_rawat='$checkNr'");
            $activePatient = null;
            $stateModified = true;
        }
    }

    // Dynamically resolve Doctor name
    $resolvedDoctorName = '';
    $resolvedDoctorCode = $d;

    if ($activePatient && !empty($activePatient['nm_dokter'])) {
        $resolvedDoctorName = $activePatient['nm_dokter'];
        $resolvedDoctorCode = $activePatient['kd_dokter'];
    } elseif (!empty($d)) {
        $resolvedDoctorName = getOne("select nm_dokter from dokter where kd_dokter='$d'");
    } else {
        $sqlDoc = "select dokter.kd_dokter, dokter.nm_dokter
                   from reg_periksa inner join dokter on reg_periksa.kd_dokter=dokter.kd_dokter
                   where reg_periksa.kd_poli='$p' and reg_periksa.tgl_registrasi='$tanggal'
                   union
                   select dokter.kd_dokter, dokter.nm_dokter
                   from jadwal inner join dokter on jadwal.kd_dokter=dokter.kd_dokter
                   where jadwal.kd_poli='$p' and dokter.status='1'
                   limit 1";
        $resDoc = bukaquery($sqlDoc);
        if ($resDoc && $rowDoc = mysqli_fetch_array($resDoc)) {
            $resolvedDoctorName = $rowDoc['nm_dokter'];
            $resolvedDoctorCode = $rowDoc['kd_dokter'];
        } else {
            $resolvedDoctorName = "Dokter Praktik Hari Ini";
        }
    }

    // Build exclusion list of served patients for upcoming list
    $excludedList = [];
    if (!empty($d) && isset($state['called_history'][$chKey])) {
        $excludedList = $state['called_history'][$chKey];
    } elseif (isset($state['called_history'][$p])) {
        $excludedList = $state['called_history'][$p];
    }
    if ($activePatient && !in_array($activePatient['no_rawat'], $excludedList)) {
        $excludedList[] = $activePatient['no_rawat'];
    }

    // Outpatient queue query joined with mutasi_berkas (Option B: Hybrid/Flexible)
    $sqlUp = "select reg_periksa.no_reg, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                     pasien.nm_pasien, dokter.nm_dokter, reg_periksa.jam_reg, reg_periksa.stts
              from reg_periksa
              inner join pasien on reg_periksa.no_rkm_medis = pasien.no_rkm_medis
              inner join dokter on reg_periksa.kd_dokter = dokter.kd_dokter
              left join mutasi_berkas on reg_periksa.no_rawat = mutasi_berkas.no_rawat
              where reg_periksa.kd_poli='$p'
                and reg_periksa.tgl_registrasi='$tanggal'
                and reg_periksa.stts not in ('Batal', 'Sudah')
                and (mutasi_berkas.status in ('Sudah Dikirim', 'Sudah Diterima') or mutasi_berkas.no_rawat is null) ";

    if (!empty($d)) {
        $sqlUp .= " and reg_periksa.kd_dokter='$d' ";
    }

    if (!empty($excludedList)) {
        $escapedList = "'" . implode("','", array_map('addslashes', $excludedList)) . "'";
        $sqlUp .= " and reg_periksa.no_rawat not in ($escapedList) ";
    }

    $sqlUp .= " order by cast(reg_periksa.no_reg as unsigned) asc, reg_periksa.jam_reg asc limit $limitUpcoming";

    $upcoming = [];
    $resUp = bukaquery($sqlUp);
    if ($resUp) {
        while ($rowU = mysqli_fetch_array($resUp)) {
            $upcoming[] = [
                'no_reg' => $rowU['no_reg'],
                'no_rawat' => $rowU['no_rawat'],
                'nm_pasien' => $rowU['nm_pasien'],
                'nm_dokter' => $rowU['nm_dokter'],
                'jam_reg' => $rowU['jam_reg']
            ];
        }
    }

    $totalRegistered = 0;
    $sqlCount = "select count(*) as total
                 from reg_periksa
                 left join mutasi_berkas on reg_periksa.no_rawat = mutasi_berkas.no_rawat
                 where reg_periksa.kd_poli='$p'
                   and reg_periksa.tgl_registrasi='$tanggal'
                   and reg_periksa.stts not in ('Batal')
                   and (mutasi_berkas.status in ('Sudah Dikirim', 'Sudah Diterima') or mutasi_berkas.no_rawat is null)";
    if (!empty($d)) $sqlCount .= " and reg_periksa.kd_dokter='$d'";
    $resCount = bukaquery($sqlCount);
    if ($resCount && $rowCount = mysqli_fetch_array($resCount)) {
        $totalRegistered = intval($rowCount['total']);
    }

    $channelsData[] = [
        'kd_poli' => $p,
        'nm_poli' => $nmPoli ? $nmPoli : $p,
        'kd_dokter' => $resolvedDoctorCode,
        'nm_dokter' => $resolvedDoctorName,
        'active_patient' => $activePatient,
        'upcoming' => $upcoming,
        'total_registered' => $totalRegistered
    ];
}

if ($stateModified) {
    savePoliState($stateFile, $state);
}

echo json_encode([
    'status' => 'success',
    'hero' => $state['last_hero'],
    'call_sequence' => $state['call_sequence'],
    'channels' => $channelsData,
    'timestamp' => date("H:i:s")
]);
