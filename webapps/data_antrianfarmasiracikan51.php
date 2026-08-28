<?php
require_once('conf/conf.php');
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");
$tanggal = mktime(date("m"), date("d"), date("Y"));
?>
<div class="pharmacy-layout">
    <!-- LEFT COLUMN: Active Calls + KPI Summary -->
    <div class="pharmacy-left">
        <!-- Hero Call: Penyerahan Obat (Priority Top) -->
        <div class="hero-call-card" id="cardPenyerahan">
            <div class="card-head">
                <div class="card-head-left">
                    <div class="card-icon call">
                        <span class="material-icons">local_pharmacy</span>
                    </div>
                    <div>
                        <div class="card-title">Panggilan Penyerahan Obat</div>
                        <div class="card-sub">Obat racikan siap di loket apotek</div>
                    </div>
                </div>
            </div>
            <div class="hero-call-body">
                <div class="hero-call-num grn" id="penyerahanNum">-</div>
                <div class="hero-call-patient" id="penyerahanPatient">Menunggu panggilan</div>
                <div class="hero-call-meta" id="penyerahanMeta">No. Resep &bull; Nama Pasien</div>
            </div>
        </div>

        <!-- Hero Call: Validasi Resep -->
        <div class="hero-call-card" id="cardValidasi">
            <div class="card-head">
                <div class="card-head-left">
                    <div class="card-icon valid">
                        <span class="material-icons">assignment</span>
                    </div>
                    <div>
                        <div class="card-title">Panggilan Validasi Resep</div>
                        <div class="card-sub">Resep divalidasi, masuk antrean racik</div>
                    </div>
                </div>
            </div>
            <div class="hero-call-body">
                <div class="hero-call-num yel" id="validasiNum">-</div>
                <div class="hero-call-patient" id="validasiPatient">Menunggu panggilan</div>
                <div class="hero-call-meta" id="validasiMeta">No. Resep &bull; Nama Pasien</div>
            </div>
        </div>

        <!-- KPI Summary Grid -->
        <div class="kpi-grid">
            <div class="kpi-box">
                <div class="kpi-num" id="kpiTotal">0</div>
                <div class="kpi-lbl">Total Resep Racik</div>
            </div>
            <div class="kpi-box yel">
                <div class="kpi-num" id="kpiMenunggu">0</div>
                <div class="kpi-lbl">Menunggu Validasi</div>
            </div>
            <div class="kpi-box bl">
                <div class="kpi-num" id="kpiProses">0</div>
                <div class="kpi-lbl">Sedang Diracik</div>
            </div>
            <div class="kpi-box grn">
                <div class="kpi-num" id="kpiSelesai">0</div>
                <div class="kpi-lbl">Selesai / Diserahkan</div>
            </div>
        </div>
    </div>

    <!-- RIGHT COLUMN: Queue Table -->
    <div class="pharmacy-right">
        <div class="card" style="height: 100%;">
            <div class="card-head">
                <div class="card-head-left">
                    <div class="card-icon">
                        <span class="material-icons">science</span>
                    </div>
                    <div>
                        <div class="card-title">Antrean Resep Racikan</div>
                        <div class="card-sub">Update otomatis setiap 4 detik</div>
                    </div>
                </div>
                <div class="pill" id="queueCount">0</div>
            </div>
            <div class="table-wrap">
                <div class="scroll-inner" id="scrollInner">
                    <table class="t">
                        <thead>
                            <tr>
                                <th class="num">No</th>
                                <th>No. Resep</th>
                                <th>Nama Pasien</th>
                                <th>Dokter Peresep</th>
                                <th class="time">Jam Masuk</th>
                                <th class="c">Status</th>
                            </tr>
                        </thead>
                        <tbody id="queueBody">
                            <?php
                            $_sql = "select resep_obat.no_resep,resep_obat.no_rawat,pasien.nm_pasien,resep_obat.jam_peresepan,
                                    if(resep_obat.jam='00:00:00','',resep_obat.jam) as jam_validasi,
                                    if(resep_obat.jam_penyerahan='00:00:00','',resep_obat.jam_penyerahan) as jam_penyerahan,dokter.nm_dokter
                                    from resep_obat inner join reg_periksa on resep_obat.no_rawat=reg_periksa.no_rawat
                                    inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis
                                    inner join dokter on resep_obat.kd_dokter=dokter.kd_dokter
                                    where resep_obat.no_resep in(select distinct resep_dokter_racikan.no_resep from resep_dokter_racikan)
                                    and resep_obat.jam_peresepan<>'00:00:00' and resep_obat.status='ralan'
                                    and resep_obat.tgl_peresepan='" . date("Y-m-d", $tanggal) . "'
                                    order by resep_obat.jam_peresepan desc";
                            $hasil = bukaquery($_sql);
                            $no = 1;
                            $total = 0;
                            $menunggu = 0;
                            $proses = 0;
                            $selesai = 0;
                            while ($data = mysqli_fetch_array($hasil)) {
                                $total++;
                                $status = '';
                                $statusClass = '';
                                if ($data['jam_penyerahan'] != '') {
                                    $status = 'Selesai';
                                    $statusClass = 'b-selesai';
                                    $selesai++;
                                } elseif ($data['jam_validasi'] != '') {
                                    $status = 'Proses';
                                    $statusClass = 'b-jalan';
                                    $proses++;
                                } else {
                                    $status = 'Menunggu';
                                    $statusClass = 'b-tunggu';
                                    $menunggu++;
                                }
                            ?>
                            <tr>
                                <td class="num"><?php echo $no++; ?></td>
                                <td><?php echo $data['no_resep']; ?></td>
                                <td><?php echo $data['nm_pasien']; ?></td>
                                <td class="med"><?php echo $data['nm_dokter']; ?></td>
                                <td class="time"><?php echo $data['jam_peresepan']; ?></td>
                                <td class="c"><span class="badge <?php echo $statusClass; ?>"><?php echo $status; ?></span></td>
                            </tr>
                            <?php
                            }
                            ?>
                        </tbody>
                    </table>
                    <?php if ($no == 1) { ?>
                    <div class="empty">
                        <span class="material-icons">science</span>
                        <p>Belum ada resep racikan hari ini</p>
                    </div>
                    <?php } ?>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    // Update KPI counts from rendered table
    document.addEventListener('DOMContentLoaded', function() {
        var total = <?php echo $total; ?>;
        var menunggu = <?php echo $menunggu; ?>;
        var proses = <?php echo $proses; ?>;
        var selesai = <?php echo $selesai; ?>;

        document.getElementById('kpiTotal').textContent = total;
        document.getElementById('kpiMenunggu').textContent = menunggu;
        document.getElementById('kpiProses').textContent = proses;
        document.getElementById('kpiSelesai').textContent = selesai;
        document.getElementById('queueCount').textContent = total + ' Resep';
    });

    // Poll active calls every 4 seconds
    (function pollCalls() {
        fetch('api_antrian_call.php?type=racikan')
            .then(r => r.json())
            .then(d => {
                // Penyerahan Obat
                var pCard = document.getElementById('cardPenyerahan');
                if (d.penyerahan && d.penyerahan.no_resep) {
                    document.getElementById('penyerahanNum').textContent = d.penyerahan.no_resep;
                    document.getElementById('penyerahanPatient').textContent = d.penyerahan.nama;
                    document.getElementById('penyerahanMeta').textContent = d.penyerahan.no_rawat;
                    pCard.classList.add('calling');
                    if (!pCard.dataset.played) {
                        new Audio('bell2.wav').play();
                        pCard.dataset.played = '1';
                    }
                } else {
                    pCard.classList.remove('calling');
                    pCard.dataset.played = '0';
                }

                // Validasi Resep
                var vCard = document.getElementById('cardValidasi');
                if (d.validasi && d.validasi.no_resep) {
                    document.getElementById('validasiNum').textContent = d.validasi.no_resep;
                    document.getElementById('validasiPatient').textContent = d.validasi.nama;
                    document.getElementById('validasiMeta').textContent = d.validasi.no_rawat;
                    vCard.classList.add('calling-valid');
                    if (!vCard.dataset.played) {
                        new Audio('bell.wav').play();
                        vCard.dataset.played = '1';
                    }
                } else {
                    vCard.classList.remove('calling-valid');
                    vCard.dataset.played = '0';
                }
            })
            .catch(() => {});
        setTimeout(pollCalls, 4000);
    })();
</script>
