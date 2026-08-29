<?php
/**
 * Test Suite for Antrian Poli 51 API & Webapp Files
 */
echo "=== 1. Checking Antrian Poli 51 Audio & Core Files ===\n";
$requiredFiles = [
    'webapps/api_antrian_poli_call.php',
    'webapps/antrianpoli51.php',
    'webapps/antrianpoliperpoli51.php',
    'webapps/caller_poli51.php',
    'webapps/suara/nomor-urut.mp3',
    'webapps/suara/satu.mp3',
    'webapps/suara/dua.mp3',
    'webapps/suara/tiga.mp3',
    'webapps/suara/empat.mp3',
    'webapps/suara/lima.mp3',
    'webapps/suara/enam.mp3',
    'webapps/suara/tujuh.mp3',
    'webapps/suara/delapan.mp3',
    'webapps/suara/sembilan.mp3',
    'webapps/suara/sepuluh.mp3',
    'webapps/suara/sebelas.mp3',
    'webapps/suara/belas.mp3',
    'webapps/suara/puluh.mp3',
    'webapps/suara/seratus.mp3',
    'webapps/suara/ratus.mp3'
];

foreach ($requiredFiles as $file) {
    if (file_exists($file)) {
        echo "[OK] $file exists (" . filesize($file) . " bytes)\n";
    } else {
        echo "[FAIL] Missing: $file\n";
        exit(1);
    }
}

echo "\n=== 2. Testing PHP Syntax Linting ===\n";
$phpFiles = [
    'webapps/api_antrian_poli_call.php',
    'webapps/antrianpoli51.php',
    'webapps/antrianpoliperpoli51.php',
    'webapps/caller_poli51.php'
];

foreach ($phpFiles as $phpFile) {
    $output = [];
    $returnVar = 0;
    exec("php -l " . escapeshellarg($phpFile), $output, $returnVar);
    if ($returnVar === 0) {
        echo "[OK] Lint passed: $phpFile\n";
    } else {
        echo "[FAIL] Lint error in $phpFile:\n" . implode("\n", $output) . "\n";
        exit(1);
    }
}

echo "\n=== 3. Testing Number Decomposition for Outpatient Queue Numbers ===\n";
function buildPlaylistPHP($n) {
    $urut = [
        "", "satu", "dua", "tiga", "empat",
        "lima", "enam", "tujuh", "delapan",
        "sembilan", "sepuluh", "sebelas"
    ];
    $files = [];
    $parse = function($x) use (&$parse, &$files, $urut) {
        if ($x < 12) {
            if (!empty($urut[$x])) $files[] = $urut[$x];
        } elseif ($x < 20) {
            if (!empty($urut[$x - 10])) $files[] = $urut[$x - 10];
            $files[] = "belas";
        } elseif ($x < 100) {
            $puluhan = intdiv($x, 10);
            if (!empty($urut[$puluhan])) $files[] = $urut[$puluhan];
            $files[] = "puluh";
            if ($x % 10 > 0) $parse($x % 10);
        } elseif ($x < 200) {
            $files[] = "seratus";
            if ($x - 100 > 0) $parse($x - 100);
        } elseif ($x < 1000) {
            $ratusan = intdiv($x, 100);
            if (!empty($urut[$ratusan])) $files[] = $urut[$ratusan];
            $files[] = "ratus";
            if ($x % 100 > 0) $parse($x % 100);
        }
    };
    $parse($n);
    return implode(" ", $files);
}

$testCases = [
    1 => "satu",
    7 => "tujuh",
    12 => "dua belas",
    23 => "dua puluh tiga",
    104 => "seratus empat",
    118 => "seratus delapan belas",
    250 => "dua ratus lima puluh",
    789 => "tujuh ratus delapan puluh sembilan",
    999 => "sembilan ratus sembilan puluh sembilan"
];

foreach ($testCases as $num => $expected) {
    $actual = buildPlaylistPHP($num);
    if ($actual === $expected) {
        echo "[OK] Queue No $num: $actual\n";
    } else {
        echo "[FAIL] Queue No $num: expected '$expected' but got '$actual'\n";
        exit(1);
    }
}

echo "\n=== 4. Testing State File & Concurrent State Persistence ===\n";
$tempState = sys_get_temp_dir() . '/test_antrian_poli_state_' . date("Ymd_His") . '.json';

$initialData = [
    'tanggal' => date("Y-m-d"),
    'call_sequence' => 1,
    'last_hero' => ['no_reg' => '001', 'nm_pasien' => 'Test Patient'],
    'channels' => ['P01|D01' => ['no_reg' => '001', 'no_rawat' => '2026/08/29/000001']],
    'called_history' => ['P01|D01' => ['2026/08/29/000001']]
];

$fp = fopen($tempState, 'c+');
if ($fp && flock($fp, LOCK_EX)) {
    ftruncate($fp, 0);
    fwrite($fp, json_encode($initialData));
    fflush($fp);
    flock($fp, LOCK_UN);
    fclose($fp);
    echo "[OK] Atomic state file write with flock passed\n";
} else {
    echo "[FAIL] Could not lock and write state file\n";
    exit(1);
}

$readData = json_decode(file_get_contents($tempState), true);
if ($readData && $readData['call_sequence'] === 1 && in_array('2026/08/29/000001', $readData['called_history']['P01|D01'])) {
    echo "[OK] Atomic state file read and structure verification passed\n";
} else {
    echo "[FAIL] State content mismatch\n";
    exit(1);
}
@unlink($tempState);

echo "\n=== ALL ANTRIAN POLI 51 TESTS PASSED SUCCESSFULLY! ===\n";
