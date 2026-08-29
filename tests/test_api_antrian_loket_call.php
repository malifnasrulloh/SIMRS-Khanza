<?php
/**
 * Test Suite for Antrian Loket 51 API & Files
 */
echo "=== 1. Checking File Existence ===\n";
$requiredFiles = [
    'webapps/api_antrian_loket_call.php',
    'webapps/antrianloket51.php',
    'webapps/antrianloketperloket51.php',
    'webapps/caller_loket51.php',
    'webapps/suara/nomor-urut.mp3',
    'webapps/suara/loket.mp3',
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

echo "\n=== 2. Testing Number Playlist Decomposition Logic ===\n";
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
    10 => "sepuluh",
    11 => "sebelas",
    15 => "lima belas",
    20 => "dua puluh",
    25 => "dua puluh lima",
    100 => "seratus",
    105 => "seratus lima",
    115 => "seratus lima belas",
    125 => "seratus dua puluh lima",
    200 => "dua ratus",
    345 => "tiga ratus empat puluh lima",
    999 => "sembilan ratus sembilan puluh sembilan"
];

foreach ($testCases as $num => $expected) {
    $actual = buildPlaylistPHP($num);
    if ($actual === $expected) {
        echo "[OK] Num $num: $actual\n";
    } else {
        echo "[FAIL] Num $num: expected '$expected' but got '$actual'\n";
        exit(1);
    }
}

echo "\n=== ALL TESTS PASSED SUCCESSFULLY! ===\n";
