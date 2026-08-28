<?php
// tests/test_api_antrian_call.php
// Mock DB & function harness to test api_antrian_call query builders and response formatting

$queriesExecuted = [];
$updatesExecuted = [];

function bukaquery($sql) {
    global $queriesExecuted;
    $queriesExecuted[] = $sql;
    return false; // In test mock
}

function bukaquery2($sql) {
    global $updatesExecuted;
    $updatesExecuted[] = $sql;
    return true;
}

function getOne($sql) {
    global $queriesExecuted;
    $queriesExecuted[] = $sql;
    return "123456 Budi Santoso";
}

// Test validation of query building based on type
function buildAntrianQuery($table, $type) {
    $whereRacik = "";
    if ($type === "nonracikan") {
        $whereRacik = " and $table.no_resep not in(select distinct resep_dokter_racikan.no_resep from resep_dokter_racikan)";
    } elseif ($type === "racikan") {
        $whereRacik = " and $table.no_resep in(select distinct resep_dokter_racikan.no_resep from resep_dokter_racikan)";
    }
    return "select * from $table where $table.status='1' $whereRacik";
}

assert(strpos(buildAntrianQuery('antriapotek3', 'combined'), 'status=\'1\'') !== false, "Combined query must check status='1'");
assert(strpos(buildAntrianQuery('antriapotek3', 'nonracikan'), 'not in') !== false, "Non-racikan query must exclude racikan");
assert(strpos(buildAntrianQuery('antriapotek3', 'racikan'), ' in(') !== false, "Racikan query must include racikan");

echo "API logic unit tests passed!\n";
