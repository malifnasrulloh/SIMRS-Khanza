<?php
header("Content-Type: application/json; charset=UTF-8");
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");

require_once('conf/conf.php');

$action = isset($_GET['action']) ? validTeks4($_GET['action'], 20) : 'status';
$tanggal = date("Y-m-d");

// Helper: State file path untuk tracking high-water mark & call events lintas-loket
$stateFile = sys_get_temp_dir() . '/khanza_antrian_loket_' . $tanggal . '.json';

function getLoketState($stateFile, $tanggal) {
    if (file_exists($stateFile)) {
        $json = @file_get_contents($stateFile);
        $data = json_decode($json, true);
        if ($data && isset($data['tanggal']) && $data['tanggal'] === $tanggal) {
            return $data;
        }
    }
    return [
        'tanggal' => $tanggal,
        'max_called' => 0,
        'last_hero' => null,
        'call_sequence' => 0,
        'last_call_time' => date("Y-m-d H:i:s"),
        'loket_last_call' => []
    ];
}

function saveLoketState($stateFile, $data) {
    @file_put_contents($stateFile, json_encode($data), LOCK_EX);
}

// Action: Panggil nomor berikutnya (Next)
if ($action === 'next') {
    $loket = isset($_POST['loket']) ? intval($_POST['loket']) : (isset($_GET['loket']) ? intval($_GET['loket']) : 1);
    if ($loket < 1) $loket = 1;

    $state = getLoketState($stateFile, $tanggal);

    // Ambil nilai max dari active antriloket juga sebagai pertimbangan
    $maxActive = 0;
    $resMax = bukaquery("select max(antrian) as max_antri from antriloket");
    if ($resMax && $rowMax = mysqli_fetch_array($resMax)) {
        $maxActive = intval($rowMax['max_antri']);
    }

    $highWaterMark = max($state['max_called'], $maxActive);

    // Cari antrean cetak berikutnya dari antriloketcetak yang lebih besar dari highWaterMark
    $nextAntri = 0;
    $resNext = bukaquery("select nomor from antriloketcetak where tanggal='$tanggal' and cast(nomor as unsigned) > $highWaterMark order by cast(nomor as unsigned) asc limit 1");
    if ($resNext && $rowNext = mysqli_fetch_array($resNext)) {
        $nextAntri = intval($rowNext['nomor']);
    } else {
        // Fallback jika tidak ada data di antriloketcetak
        $nextAntri = $highWaterMark + 1;
    }

    // Update / Insert ke antriloket
    bukaquery2("delete from antriloket where loket='$loket'");
    bukaquery2("insert into antriloket values('$loket', '$nextAntri')");

    // Update state
    $state['max_called'] = max($highWaterMark, $nextAntri);
    $state['call_sequence']++;
    $state['last_call_time'] = date("Y-m-d H:i:s");
    $state['last_hero'] = [
        'loket' => $loket,
        'antrian' => $nextAntri,
        'call_id' => $state['call_sequence'],
        'time' => date("H:i:s")
    ];
    $state['loket_last_call'][$loket] = $state['last_hero'];
    saveLoketState($stateFile, $state);

    echo json_encode([
        'status' => 'success',
        'action' => 'next',
        'loket' => $loket,
        'antrian' => $nextAntri,
        'call_id' => $state['call_sequence']
    ]);
    exit();
}

// Action: Panggil Ulang (Recall)
if ($action === 'recall') {
    $loket = isset($_POST['loket']) ? intval($_POST['loket']) : (isset($_GET['loket']) ? intval($_GET['loket']) : 1);
    if ($loket < 1) $loket = 1;

    $currentAntri = 0;
    $resCurr = bukaquery("select antrian from antriloket where loket='$loket'");
    if ($resCurr && $rowCurr = mysqli_fetch_array($resCurr)) {
        $currentAntri = intval($rowCurr['antrian']);
    }

    if ($currentAntri > 0) {
        bukaquery2("delete from antriloket where loket='$loket'");
        bukaquery2("insert into antriloket values('$loket', '$currentAntri')");
    }

    $state = getLoketState($stateFile, $tanggal);
    $state['call_sequence']++;
    $state['last_call_time'] = date("Y-m-d H:i:s");
    $state['last_hero'] = [
        'loket' => $loket,
        'antrian' => $currentAntri,
        'call_id' => $state['call_sequence'],
        'time' => date("H:i:s"),
        'is_recall' => true
    ];
    $state['loket_last_call'][$loket] = $state['last_hero'];
    saveLoketState($stateFile, $state);

    echo json_encode([
        'status' => 'success',
        'action' => 'recall',
        'loket' => $loket,
        'antrian' => $currentAntri,
        'call_id' => $state['call_sequence']
    ]);
    exit();
}

// Action: Panggil Nomor Tertentu (Manual Call)
if ($action === 'call_manual') {
    $loket = isset($_POST['loket']) ? intval($_POST['loket']) : (isset($_GET['loket']) ? intval($_GET['loket']) : 1);
    $antrian = isset($_POST['antrian']) ? intval($_POST['antrian']) : (isset($_GET['antrian']) ? intval($_GET['antrian']) : 1);
    if ($loket < 1) $loket = 1;
    if ($antrian < 1) $antrian = 1;

    bukaquery2("delete from antriloket where loket='$loket'");
    bukaquery2("insert into antriloket values('$loket', '$antrian')");

    $state = getLoketState($stateFile, $tanggal);
    $state['max_called'] = max($state['max_called'], $antrian);
    $state['call_sequence']++;
    $state['last_call_time'] = date("Y-m-d H:i:s");
    $state['last_hero'] = [
        'loket' => $loket,
        'antrian' => $antrian,
        'call_id' => $state['call_sequence'],
        'time' => date("H:i:s")
    ];
    $state['loket_last_call'][$loket] = $state['last_hero'];
    saveLoketState($stateFile, $state);

    echo json_encode([
        'status' => 'success',
        'action' => 'call_manual',
        'loket' => $loket,
        'antrian' => $antrian,
        'call_id' => $state['call_sequence']
    ]);
    exit();
}

// Action: Reset / Hapus Antrean Loket
if ($action === 'reset') {
    $loket = isset($_POST['loket']) ? intval($_POST['loket']) : (isset($_GET['loket']) ? intval($_GET['loket']) : 0);
    if ($loket > 0) {
        bukaquery2("delete from antriloket where loket='$loket'");
    } else {
        bukaquery2("delete from antriloket");
    }

    $state = getLoketState($stateFile, $tanggal);
    if ($loket > 0) {
        unset($state['loket_last_call'][$loket]);
        if (isset($state['last_hero']['loket']) && $state['last_hero']['loket'] == $loket) {
            $state['last_hero'] = null;
        }
    } else {
        $state['loket_last_call'] = [];
        $state['last_hero'] = null;
    }
    saveLoketState($stateFile, $state);

    echo json_encode([
        'status' => 'success',
        'action' => 'reset',
        'loket' => $loket
    ]);
    exit();
}

// Default Action: Get Status
$targetLoket = isset($_GET['loket']) ? intval($_GET['loket']) : 0;
$state = getLoketState($stateFile, $tanggal);

$activeCalls = [];
$loketMap = [];

$resCalls = bukaquery("select loket, antrian from antriloket order by loket asc");
if ($resCalls) {
    while ($row = mysqli_fetch_array($resCalls)) {
        $l = intval($row['loket']);
        $a = intval($row['antrian']);
        $activeCalls[] = [
            'loket' => $l,
            'antrian' => $a
        ];
        $loketMap[$l] = $a;
    }
}

// Tentukan Hero Card
$latestHero = null;
if ($targetLoket > 0) {
    if (isset($loketMap[$targetLoket])) {
        $callId = isset($state['loket_last_call'][$targetLoket]['call_id']) ? $state['loket_last_call'][$targetLoket]['call_id'] : $state['call_sequence'];
        $latestHero = [
            'loket' => $targetLoket,
            'antrian' => $loketMap[$targetLoket],
            'call_id' => $callId
        ];
    }
} else {
    // Multi-loket display
    if (!empty($state['last_hero']) && isset($loketMap[$state['last_hero']['loket']])) {
        // Hero dari call event terakhir yang masih aktif
        $latestHero = $state['last_hero'];
    } elseif (!empty($activeCalls)) {
        $latestHero = [
            'loket' => $activeCalls[0]['loket'],
            'antrian' => $activeCalls[0]['antrian'],
            'call_id' => $state['call_sequence']
        ];
    }
}

// Statistik antriloketcetak
$totalCetak = 0;
$maxNomorCetak = 0;
$resTotal = bukaquery("select count(*) as total, max(cast(nomor as unsigned)) as max_nomor from antriloketcetak where tanggal='$tanggal'");
if ($resTotal && $rowTotal = mysqli_fetch_array($resTotal)) {
    $totalCetak = intval($rowTotal['total']);
    $maxNomorCetak = intval($rowTotal['max_nomor']);
}

// Recent tickets stream (10 tiket terbaru)
$recentTickets = [];
$resRecent = bukaquery("select nomor, jam from antriloketcetak where tanggal='$tanggal' order by cast(nomor as unsigned) desc limit 10");
if ($resRecent) {
    while ($rowR = mysqli_fetch_array($resRecent)) {
        $num = intval($rowR['nomor']);
        $statusTiket = 'Menunggu';

        // Cek apakah sedang aktif di loket
        $isServing = false;
        foreach ($activeCalls as $c) {
            if ($c['antrian'] == $num) {
                $statusTiket = 'Loket ' . $c['loket'];
                $isServing = true;
                break;
            }
        }

        if (!$isServing) {
            if ($num <= $state['max_called'] && $state['max_called'] > 0) {
                $statusTiket = 'Selesai';
            } else {
                $statusTiket = 'Menunggu';
            }
        }

        $recentTickets[] = [
            'nomor' => sprintf("%03d", $num),
            'jam' => $rowR['jam'],
            'status' => $statusTiket
        ];
    }
}

echo json_encode([
    'status' => 'success',
    'hero' => $latestHero,
    'lokets' => $loketMap,
    'total_cetak' => $totalCetak,
    'max_nomor' => $maxNomorCetak,
    'max_called' => $state['max_called'],
    'call_sequence' => $state['call_sequence'],
    'recent_tickets' => $recentTickets,
    'timestamp' => date("H:i:s")
]);
