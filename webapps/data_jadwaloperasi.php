<?php
require_once('conf/conf.php');
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");
$tgl = date("Y-m-d");
?>
<div class="card" style="width:100%;height:100%;">
    <div class="card-head">
        <div class="card-head-left">
            <div class="card-icon"><span class="material-icons">favorite</span></div>
            <div>
                <div class="card-title">Jadwal Operasi</div>
                <div class="card-sub"><?= date('l, d F Y') ?></div>
            </div>
        </div>
        <?php
        $cnt = mysqli_num_rows(bukaquery("SELECT booking_operasi.no_rawat FROM booking_operasi
            INNER JOIN reg_periksa   ON booking_operasi.no_rawat=reg_periksa.no_rawat
            INNER JOIN pasien        ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis
            INNER JOIN paket_operasi ON booking_operasi.kode_paket=paket_operasi.kode_paket
            INNER JOIN dokter        ON booking_operasi.kd_dokter=dokter.kd_dokter
            INNER JOIN ruang_ok      ON booking_operasi.kd_ruang_ok=ruang_ok.kd_ruang_ok
            WHERE tanggal='$tgl'"));
        if ($cnt > 0) echo "<span class='pill'>$cnt Tindakan</span>";
        ?>
    </div>
    <div class="table-wrap">
        <div class="scroll-inner">
            <table class="t">
                <thead>
                    <tr>
                        <th style="width:2vw;">#</th>
                        <th style="width:9vw;">No. Rawat</th>
                        <th style="width:5vw;text-align:center;">Umur</th>
                        <th style="width:3vw;text-align:center;">JK</th>
                        <th style="text-align:center;width:6vw;">Mulai</th>
                        <th style="text-align:center;width:6vw;">Selesai</th>
                        <th style="text-align:center;width:8vw;">Status</th>
                        <th>Jenis Operasi</th>
                        <th>Operator</th>
                        <th style="text-align:center;width:8vw;">Ruang OK</th>
                    </tr>
                </thead>
                <tbody>
                    <?php
                    $res = bukaquery("SELECT booking_operasi.no_rawat, booking_operasi.jam_mulai, booking_operasi.jam_selesai,
                    booking_operasi.status, dokter.nm_dokter, paket_operasi.nm_perawatan,
                    concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,
                    pasien.jk, ruang_ok.nm_ruang_ok
                    FROM booking_operasi
                    INNER JOIN reg_periksa   ON booking_operasi.no_rawat=reg_periksa.no_rawat
                    INNER JOIN pasien        ON reg_periksa.no_rkm_medis=pasien.no_rkm_medis
                    INNER JOIN paket_operasi ON booking_operasi.kode_paket=paket_operasi.kode_paket
                    INNER JOIN dokter        ON booking_operasi.kd_dokter=dokter.kd_dokter
                    INNER JOIN ruang_ok      ON booking_operasi.kd_ruang_ok=ruang_ok.kd_ruang_ok
                    WHERE tanggal='$tgl' ORDER BY booking_operasi.jam_mulai");
                    if (mysqli_num_rows($res) > 0) {
                        $n = 1;
                        while ($d = mysqli_fetch_array($res)) {
                            $st = strtolower(trim($d['status']));
                            if (str_contains($st, 'tunggu') || str_contains($st, 'menunggu')) $cls = 'b-tunggu';
                            elseif (str_contains($st, 'jalan') || str_contains($st, 'proses'))   $cls = 'b-jalan';
                            elseif (str_contains($st, 'selesai') || str_contains($st, 'done'))    $cls = 'b-selesai';
                            elseif (str_contains($st, 'batal') || str_contains($st, 'cancel'))   $cls = 'b-batal';
                            else                                                               $cls = 'b-default';
                            echo "<tr>
                            <td class='num'>$n</td>
                            <td class='sm'>" . htmlspecialchars($d['no_rawat']) . "</td>
                            <td class='c med'>" . htmlspecialchars($d['umur']) . "</td>
                            <td class='c'>" . htmlspecialchars($d['jk']) . "</td>
                            <td class='time'>" . $d['jam_mulai'] . "</td>
                            <td class='time'>" . $d['jam_selesai'] . "</td>
                            <td class='c'><span class='badge $cls'>" . htmlspecialchars($d['status']) . "</span></td>
                            <td style='white-space:normal;max-width:14vw;line-height:1.3;font-size:1vw;'>" . htmlspecialchars($d['nm_perawatan']) . "</td>
                            <td class='med'>" . htmlspecialchars($d['nm_dokter']) . "</td>
                            <td class='c med'>" . htmlspecialchars($d['nm_ruang_ok']) . "</td>
                        </tr>";
                            $n++;
                        }
                    } else {
                        echo "<tr><td colspan='10'><div class='empty'>
                        <span class='material-icons'>event_available</span>
                        <p>Tidak ada jadwal operasi hari ini</p></div></td></tr>";
                    }
                    ?>
                </tbody>
            </table>
        </div>
    </div>
</div>