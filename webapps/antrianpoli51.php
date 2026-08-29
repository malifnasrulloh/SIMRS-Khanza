<?php
session_start();
require_once('conf/conf.php');
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");

$setting = mysqli_fetch_array(bukaquery("select setting.nama_instansi,setting.alamat_instansi,setting.kabupaten,setting.propinsi,setting.kontak,setting.email,setting.logo from setting"));
$runtext = "";
$resRun = bukaquery("select teks from runtext where aktifkan='Yes' limit 1");
if ($resRun && $rowRun = mysqli_fetch_array($resRun)) {
    $runtext = $rowRun['teks'];
}
if (empty($runtext)) {
    $runtext = "Selamat Datang di " . $setting['nama_instansi'] . " - Antrean Poliklinik Rawat Jalan. Harap memperhatikan nomor urut dan poliklinik tujuan Anda.";
}
?>
<!doctype html>
<html lang="id">

<head>
    <title>Display Antrean Poliklinik – <?php echo htmlspecialchars($setting['nama_instansi']); ?></title>
    <link rel="icon" href="conf/assets/img/rs.png" type="image/x-icon">
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no" />
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700;800;900&family=Barlow+Condensed:wght@600;700;800;900&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <style>
        :root {
            --bd: #0d47a1;
            --bm: #1976d2;
            --bl: #42a5f5;
            --bp: #e3f2fd;
            --bh: #bbdefb;
            --td: #0d2b5e;
            --tm: #546e9a;
            --grn: #00c853;
            --red: #e53935;
            --yel: #f9a825;
            --card-bg: rgba(255, 255, 255, 0.96);
        }

        *, *::before, *::after {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        html, body {
            height: 100%;
            overflow: hidden;
            font-family: 'Nunito', sans-serif;
            background: linear-gradient(135deg, #0d47a1 0%, #1565c0 40%, #0d2b5e 100%);
            color: #fff;
        }

        .screen {
            display: flex;
            flex-direction: column;
            height: 100vh;
            padding: 0.8vw 1.2vw;
            gap: 0.8vw;
        }

        /* Topbar Header */
        .topbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: rgba(255, 255, 255, 0.12);
            backdrop-filter: blur(10px);
            border: 0.15vw solid rgba(255, 255, 255, 0.25);
            border-radius: 1vw;
            padding: 0.6vw 1.4vw;
        }

        .topbar-left {
            display: flex;
            align-items: center;
            gap: 1vw;
        }

        .topbar-logo {
            width: 4.2vw;
            height: 4.2vw;
            border-radius: 50%;
            border: 0.2vw solid rgba(255, 255, 255, 0.8);
            background: #fff;
            padding: 0.2vw;
            object-fit: contain;
        }

        .topbar-name {
            font-size: 1.6vw;
            font-weight: 900;
            line-height: 1.15;
            text-shadow: 0 0.1vw 0.4vw rgba(0, 0, 0, 0.4);
        }

        .topbar-sub {
            font-size: 0.9vw;
            opacity: 0.9;
        }

        .topbar-right {
            display: flex;
            align-items: center;
            gap: 1.5vw;
        }

        .btn-config {
            background: rgba(255, 255, 255, 0.2);
            border: 1px solid rgba(255, 255, 255, 0.4);
            color: #fff;
            padding: 0.4vw 0.8vw;
            border-radius: 0.5vw;
            cursor: pointer;
            display: flex;
            align-items: center;
            gap: 0.4vw;
            font-size: 0.85vw;
            font-weight: 700;
            transition: all 0.2s ease;
        }

        .btn-config:hover {
            background: rgba(255, 255, 255, 0.35);
        }

        .clock-time {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 2.6vw;
            font-weight: 800;
            line-height: 1;
            text-align: right;
        }

        /* 3-Column Clinic Grid */
        .clinic-grid {
            flex: 1;
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 1vw;
            min-height: 0;
        }

        .clinic-card {
            background: var(--card-bg);
            border-radius: 1.2vw;
            border: 0.25vw solid #fff;
            box-shadow: 0 0.8vw 2.5vw rgba(0, 0, 0, 0.28);
            display: flex;
            flex-direction: column;
            color: var(--td);
            overflow: hidden;
            position: relative;
            transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
        }

        .clinic-card.calling {
            border-color: var(--grn);
            box-shadow: 0 0 3vw rgba(0, 200, 83, 0.6);
            animation: card-pulse 1.2s infinite alternate;
        }

        @keyframes card-pulse {
            from { transform: scale(1); }
            to { transform: scale(1.015); }
        }

        /* Card Header */
        .card-header {
            background: linear-gradient(135deg, var(--bd), var(--bm));
            color: #fff;
            padding: 0.7vw 1vw;
            display: flex;
            flex-direction: column;
            gap: 0.2vw;
            position: relative;
        }

        .clinic-card.calling .card-header {
            background: linear-gradient(135deg, #059669, #10b981);
        }

        .channel-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.3vw;
            font-size: 0.8vw;
            font-weight: 800;
            background: rgba(255, 255, 255, 0.2);
            padding: 0.15vw 0.5vw;
            border-radius: 0.3vw;
            width: fit-content;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        .clinic-title {
            font-size: 1.3vw;
            font-weight: 900;
            line-height: 1.2;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .doctor-title {
            font-size: 0.95vw;
            font-weight: 700;
            opacity: 0.92;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        /* Card Serving Stage */
        .card-stage {
            padding: 1vw;
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
            border-bottom: 0.15vw solid #e2e8f0;
            position: relative;
        }

        .clinic-card.calling .card-stage {
            background: linear-gradient(180deg, #f0fdf4 0%, #dcfce7 100%);
        }

        .stage-label {
            font-size: 0.9vw;
            font-weight: 800;
            color: var(--tm);
            text-transform: uppercase;
            letter-spacing: 0.08em;
        }

        .stage-number {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 7vw;
            font-weight: 900;
            line-height: 0.95;
            color: var(--bd);
            text-shadow: 0 0.2vw 0.6vw rgba(13, 71, 161, 0.15);
            margin: 0.2vw 0;
        }

        .clinic-card.calling .stage-number {
            color: #047857;
            text-shadow: 0 0.2vw 1vw rgba(4, 120, 87, 0.35);
        }

        .stage-patient {
            font-size: 1.25vw;
            font-weight: 900;
            color: var(--td);
            text-align: center;
            max-width: 95%;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .clinic-card.calling .stage-patient {
            color: #065f46;
        }

        .stage-badge-status {
            font-size: 0.75vw;
            font-weight: 800;
            padding: 0.2vw 0.8vw;
            border-radius: 1vw;
            margin-top: 0.4vw;
            background: #e2e8f0;
            color: var(--tm);
            display: inline-flex;
            align-items: center;
            gap: 0.3vw;
        }

        .clinic-card.calling .stage-badge-status {
            background: #bbf7d0;
            color: #065f46;
        }

        /* Upcoming Queue Stream Table */
        .card-upcoming {
            flex: 1;
            padding: 0.6vw 0.8vw;
            display: flex;
            flex-direction: column;
            overflow: hidden;
            background: #fff;
        }

        .upcoming-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 0.85vw;
            font-weight: 800;
            color: var(--tm);
            padding-bottom: 0.4vw;
            border-bottom: 0.1vw solid #e2e8f0;
            margin-bottom: 0.4vw;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }

        .upcoming-list {
            list-style: none;
            display: flex;
            flex-direction: column;
            gap: 0.35vw;
            overflow-y: auto;
            flex: 1;
        }

        .upcoming-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: #f8fafc;
            padding: 0.4vw 0.6vw;
            border-radius: 0.4vw;
            border: 1px solid #e2e8f0;
            font-size: 0.9vw;
            transition: all 0.2s ease;
        }

        .upcoming-item:hover {
            background: var(--bp);
            border-color: var(--bl);
        }

        .item-reg {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 1.15vw;
            font-weight: 800;
            color: var(--bd);
            background: var(--bp);
            padding: 0.1vw 0.4vw;
            border-radius: 0.3vw;
            min-width: 2.4vw;
            text-align: center;
        }

        .item-name {
            font-weight: 700;
            color: var(--td);
            flex: 1;
            margin: 0 0.6vw;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .item-time {
            font-size: 0.8vw;
            font-weight: 700;
            color: var(--tm);
        }

        .upcoming-empty {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100%;
            font-size: 0.9vw;
            font-weight: 700;
            color: #94a3b8;
            font-style: italic;
        }

        /* Footer Marquee */
        .footer-ticker {
            background: rgba(255, 255, 255, 0.12);
            backdrop-filter: blur(10px);
            border-radius: 0.8vw;
            padding: 0.4vw 1vw;
            overflow: hidden;
            display: flex;
            align-items: center;
        }

        .ticker-label {
            background: rgba(255, 255, 255, 0.25);
            padding: 0.2vw 0.7vw;
            border-radius: 0.4vw;
            font-size: 0.9vw;
            font-weight: 800;
            margin-right: 0.8vw;
            flex-shrink: 0;
            text-transform: uppercase;
        }

        .ticker-text {
            font-size: 1.05vw;
            font-weight: 700;
            white-space: nowrap;
            color: #fff;
        }

        /* Non-blocking Audio Toast */
        #audioToast {
            position: fixed;
            bottom: 4vw;
            right: 2vw;
            background: linear-gradient(135deg, #1e293b, #0f172a);
            color: #fff;
            border: 0.15vw solid var(--bl);
            border-radius: 0.8vw;
            padding: 0.8vw 1.4vw;
            display: flex;
            align-items: center;
            gap: 0.8vw;
            box-shadow: 0 0.5vw 1.5vw rgba(0, 0, 0, 0.4);
            cursor: pointer;
            z-index: 9999;
        }

        #audioToast .toast-icon {
            background: var(--bm);
            width: 2.4vw;
            height: 2.4vw;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.4vw;
        }

        #audioToast .toast-text {
            font-size: 0.95vw;
            font-weight: 700;
        }

        #audioToast .toast-btn {
            background: var(--grn);
            color: #065f46;
            font-weight: 800;
            padding: 0.3vw 0.8vw;
            border-radius: 0.4vw;
            font-size: 0.85vw;
        }

        /* Setup Modal */
        .modal-backdrop {
            display: none;
            position: fixed;
            inset: 0;
            background: rgba(13, 43, 94, 0.8);
            backdrop-filter: blur(8px);
            z-index: 10000;
            align-items: center;
            justify-content: center;
        }

        .modal-content {
            background: #fff;
            color: var(--td);
            border-radius: 1.2rem;
            width: 90%;
            max-width: 600px;
            padding: 2rem;
            box-shadow: 0 1rem 3rem rgba(0, 0, 0, 0.4);
        }

        .modal-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 1.5rem;
            border-bottom: 2px solid var(--bp);
            padding-bottom: 0.8rem;
        }

        .modal-header h2 {
            font-size: 1.3rem;
            font-weight: 900;
            color: var(--bd);
        }

        .setup-row {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 0.8rem;
            padding: 1rem;
            margin-bottom: 1rem;
        }

        .setup-row h4 {
            font-size: 1rem;
            font-weight: 800;
            color: var(--bm);
            margin-bottom: 0.6rem;
        }

        .setup-row select {
            width: 100%;
            padding: 0.7rem 0.9rem;
            border: 2px solid #cbd5e1;
            border-radius: 0.5rem;
            font-family: 'Nunito', sans-serif;
            font-size: 1rem;
            font-weight: 700;
            color: var(--td);
            outline: none;
            background: #fff;
        }

        .setup-row select:focus {
            border-color: var(--bm);
        }

        .modal-actions {
            display: flex;
            justify-content: flex-end;
            gap: 1rem;
            margin-top: 1.5rem;
        }

        .modal-actions button {
            padding: 0.7rem 1.5rem;
            border-radius: 0.6rem;
            font-family: 'Nunito', sans-serif;
            font-size: 1rem;
            font-weight: 800;
            cursor: pointer;
            border: none;
        }

        .btn-save {
            background: linear-gradient(135deg, #059669, #10b981);
            color: #fff;
        }

        .btn-cancel {
            background: #e2e8f0;
            color: var(--td);
        }
    </style>
</head>

<body onclick="unlockAudio()">
    <!-- Audio Toast -->
    <div id="audioToast" onclick="unlockAudio()">
        <div class="toast-icon">
            <span class="material-icons">volume_up</span>
        </div>
        <div class="toast-text">Klik layar untuk mengaktifkan suara antrean poliklinik</div>
        <div class="toast-btn">AKTIFKAN</div>
    </div>

    <!-- Quick Setup Modal (Poli-Centric Only) -->
    <div class="modal-backdrop" id="setupModal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>Konfigurasi Channel Poliklinik</h2>
                <span class="material-icons" style="cursor:pointer;" onclick="closeSetupModal()">close</span>
            </div>
            <p style="font-size:0.9rem; color:var(--tm); margin-bottom:1rem;">
                Pilih poliklinik untuk setiap channel display. Nama dokter akan terdeteksi dan diperbarui secara otomatis saat ada panggilan aktif.
            </p>
            <div id="setupRowsContainer">
                <!-- Injected via JavaScript -->
            </div>
            <div class="modal-actions">
                <button class="btn-cancel" onclick="closeSetupModal()">Batal</button>
                <button class="btn-save" onclick="saveConfiguration()">Simpan & Terapkan</button>
            </div>
        </div>
    </div>

    <div class="screen">
        <!-- Topbar Header -->
        <header class="topbar">
            <div class="topbar-left">
                <?php if (!empty($setting['logo'])) { ?>
                    <img class="topbar-logo" src="data:image/jpeg;base64,<?php echo base64_encode($setting['logo']); ?>" alt="Logo RS">
                <?php } ?>
                <div>
                    <div class="topbar-name"><?php echo htmlspecialchars($setting['nama_instansi']); ?></div>
                    <div class="topbar-sub">Sistem Antrean Poliklinik Rawat Jalan</div>
                </div>
            </div>
            <div class="topbar-right">
                <button class="btn-config" onclick="openSetupModal()">
                    <span class="material-icons" style="font-size:1.1vw;">settings</span>
                    <span>Set Poliklinik</span>
                </button>
                <div class="clock-time" id="clockTime">--:--:--</div>
            </div>
        </header>

        <!-- 3-Column Clinic Grid -->
        <main class="clinic-grid" id="clinicGrid">
            <!-- Channel 1 -->
            <div class="clinic-card" id="cardCh0">
                <div class="card-header">
                    <div class="channel-badge">
                        <span class="material-icons" style="font-size:0.9vw;">meeting_room</span>
                        <span>Channel 1</span>
                    </div>
                    <div class="clinic-title" id="titlePoli0">Memuat Poli...</div>
                    <div class="doctor-title" id="titleDokter0">-</div>
                </div>
                <div class="card-stage">
                    <div class="stage-label">Nomor Antrean</div>
                    <div class="stage-number" id="numCh0">-</div>
                    <div class="stage-patient" id="patientCh0">Standby</div>
                    <div class="stage-badge-status" id="badgeCh0">
                        <span class="material-icons" style="font-size:0.9vw;">hourglass_empty</span>
                        <span id="textBadge0">Menunggu Panggilan</span>
                    </div>
                </div>
                <div class="card-upcoming">
                    <div class="upcoming-header">
                        <span>Antrean Berikutnya</span>
                        <span id="countUpcoming0">0 Pasien</span>
                    </div>
                    <ul class="upcoming-list" id="listUpcoming0">
                        <li class="upcoming-empty">Tidak ada antrean menunggu</li>
                    </ul>
                </div>
            </div>

            <!-- Channel 2 -->
            <div class="clinic-card" id="cardCh1">
                <div class="card-header">
                    <div class="channel-badge">
                        <span class="material-icons" style="font-size:0.9vw;">meeting_room</span>
                        <span>Channel 2</span>
                    </div>
                    <div class="clinic-title" id="titlePoli1">Memuat Poli...</div>
                    <div class="doctor-title" id="titleDokter1">-</div>
                </div>
                <div class="card-stage">
                    <div class="stage-label">Nomor Antrean</div>
                    <div class="stage-number" id="numCh1">-</div>
                    <div class="stage-patient" id="patientCh1">Standby</div>
                    <div class="stage-badge-status" id="badgeCh1">
                        <span class="material-icons" style="font-size:0.9vw;">hourglass_empty</span>
                        <span id="textBadge1">Menunggu Panggilan</span>
                    </div>
                </div>
                <div class="card-upcoming">
                    <div class="upcoming-header">
                        <span>Antrean Berikutnya</span>
                        <span id="countUpcoming1">0 Pasien</span>
                    </div>
                    <ul class="upcoming-list" id="listUpcoming1">
                        <li class="upcoming-empty">Tidak ada antrean menunggu</li>
                    </ul>
                </div>
            </div>

            <!-- Channel 3 -->
            <div class="clinic-card" id="cardCh2">
                <div class="card-header">
                    <div class="channel-badge">
                        <span class="material-icons" style="font-size:0.9vw;">meeting_room</span>
                        <span>Channel 3</span>
                    </div>
                    <div class="clinic-title" id="titlePoli2">Memuat Poli...</div>
                    <div class="doctor-title" id="titleDokter2">-</div>
                </div>
                <div class="card-stage">
                    <div class="stage-label">Nomor Antrean</div>
                    <div class="stage-number" id="numCh2">-</div>
                    <div class="stage-patient" id="patientCh2">Standby</div>
                    <div class="stage-badge-status" id="badgeCh2">
                        <span class="material-icons" style="font-size:0.9vw;">hourglass_empty</span>
                        <span id="textBadge2">Menunggu Panggilan</span>
                    </div>
                </div>
                <div class="card-upcoming">
                    <div class="upcoming-header">
                        <span>Antrean Berikutnya</span>
                        <span id="countUpcoming2">0 Pasien</span>
                    </div>
                    <ul class="upcoming-list" id="listUpcoming2">
                        <li class="upcoming-empty">Tidak ada antrean menunggu</li>
                    </ul>
                </div>
            </div>
        </main>

        <!-- Footer Running Text -->
        <footer class="footer-ticker">
            <div class="ticker-label">
                <span class="material-icons" style="font-size:1.1vw; vertical-align:middle;">campaign</span>
                <span>Info</span>
            </div>
            <marquee class="ticker-text" behavior="scroll" direction="left" scrollamount="6">
                <?php echo htmlspecialchars($runtext); ?>
            </marquee>
        </footer>
    </div>

    <script>
        var audioEnabled = false;
        var isPlayingAudio = false;
        var audioQueue = [];
        var lastCallSequence = -1;
        var isInitialLoad = true;
        var isModalOpen = false;
        window.activeUtterance = null;

        // Default channels configuration (3 Polikliniks)
        var channelsConfig = ['', '', ''];

        // Master data cache for modal setup
        var allPoliList = [];

        function escapeHtml(str) {
            if (!str) return '';
            var div = document.createElement('div');
            div.textContent = str;
            return div.innerHTML;
        }

        function updateClock() {
            var now = new Date();
            var h = String(now.getHours()).padStart(2, '0');
            var m = String(now.getMinutes()).padStart(2, '0');
            var s = String(now.getSeconds()).padStart(2, '0');
            document.getElementById('clockTime').textContent = h + ':' + m + ':' + s;
        }
        setInterval(updateClock, 1000);
        updateClock();

        function unlockAudio() {
            if (!audioEnabled) {
                audioEnabled = true;
                var toast = document.getElementById('audioToast');
                if (toast) toast.style.display = 'none';
                if (audioQueue.length > 1) {
                    audioQueue = [audioQueue[audioQueue.length - 1]];
                }
                try {
                    window.speechSynthesis.resume();
                } catch(e) {}
                processAudioQueue();
            }
        }

        // Convert number (1 - 999999) to natural Indonesian speech words
        function numberToIndonesianWords(n) {
            var num = parseInt(n, 10);
            if (isNaN(num) || num <= 0) return String(n || '');

            var urut = [
                "", "satu", "dua", "tiga", "empat", "lima", "enam", "tujuh", "delapan", "sembilan", "sepuluh", "sebelas"
            ];

            function parse(x) {
                if (x < 12) {
                    return urut[x];
                } else if (x < 20) {
                    return urut[x - 10] + " belas";
                } else if (x < 100) {
                    var puluhan = Math.floor(x / 10);
                    var sisa = x % 10;
                    return urut[puluhan] + " puluh" + (sisa > 0 ? " " + parse(sisa) : "");
                } else if (x < 200) {
                    var sisa = x - 100;
                    return "seratus" + (sisa > 0 ? " " + parse(sisa) : "");
                } else if (x < 1000) {
                    var ratusan = Math.floor(x / 100);
                    var sisa = x % 100;
                    return urut[ratusan] + " ratus" + (sisa > 0 ? " " + parse(sisa) : "");
                } else if (x < 2000) {
                    var sisa = x - 1000;
                    return "seribu" + (sisa > 0 ? " " + parse(sisa) : "");
                } else if (x < 1000000) {
                    var ribuan = Math.floor(x / 1000);
                    var sisa = x % 1000;
                    return parse(ribuan) + " ribu" + (sisa > 0 ? " " + parse(sisa) : "");
                }
                return String(x);
            }

            return parse(num);
        }

        // Parse query string parameters or retrieve from localStorage
        function loadChannelsConfiguration() {
            var params = new URLSearchParams(window.location.search);
            var loadedFromUrl = false;

            for (var i = 1; i <= 3; i++) {
                var p = params.get('p' + i) || params.get('poli' + i);
                if (p) {
                    channelsConfig[i - 1] = p;
                    loadedFromUrl = true;
                }
            }

            if (!loadedFromUrl) {
                try {
                    var stored = localStorage.getItem('khanza_antrian_poli_channels_v2');
                    if (stored) {
                        var parsed = JSON.parse(stored);
                        if (Array.isArray(parsed) && parsed.length >= 3) {
                            channelsConfig = parsed;
                        }
                    }
                } catch(e) {}
            }
        }

        // Pure Web Speech API Voice Calling: Unified Tone & Seamless Delivery
        function speakCompleteCall(item, onFinished) {
            if (!('speechSynthesis' in window)) {
                if (onFinished) onFinished();
                return;
            }

            try {
                window.speechSynthesis.resume();
            } catch(e) {}

            var isCompleted = false;
            function finish() {
                if (!isCompleted) {
                    isCompleted = true;
                    window.activeUtterance = null;
                    if (onFinished) onFinished();
                }
            }

            // Watchdog timer: prevent freeze if onend is not fired
            var watchdog = setTimeout(finish, 9000);

            var spokenNumber = numberToIndonesianWords(item.no_reg);
            var textToSpeak = "Nomor urut, " + spokenNumber + ". Atas nama pasien, " + item.nm_pasien + ", silakan menuju ke " + item.nm_poli;

            var utterance = new SpeechSynthesisUtterance(textToSpeak);
            utterance.lang = 'id-ID';
            utterance.rate = 0.92;
            utterance.pitch = 1.0;

            var voices = window.speechSynthesis.getVoices();
            for (var v = 0; v < voices.length; v++) {
                if (voices[v].lang.indexOf('id') !== -1 || voices[v].name.indexOf('Indonesia') !== -1) {
                    utterance.voice = voices[v];
                    break;
                }
            }

            utterance.onend = function() {
                clearTimeout(watchdog);
                finish();
            };
            utterance.onerror = function() {
                clearTimeout(watchdog);
                finish();
            };

            window.activeUtterance = utterance;
            window.speechSynthesis.speak(utterance);
        }

        function processAudioQueue() {
            if (!audioEnabled || isPlayingAudio || audioQueue.length === 0) return;
            isPlayingAudio = true;
            var item = audioQueue.shift();

            // Find channel card element to highlight
            var highlightCard = null;
            for (var c = 0; c < 3; c++) {
                if (channelsConfig[c] === item.kd_poli) {
                    highlightCard = document.getElementById('cardCh' + c);
                    break;
                }
            }

            if (highlightCard) highlightCard.classList.add('calling');

            speakCompleteCall(item, function() {
                setTimeout(function() {
                    if (highlightCard) highlightCard.classList.remove('calling');
                    isPlayingAudio = false;
                    processAudioQueue();
                }, 2000);
            });
        }

        function enqueueCall(patient) {
            if (!patient || !patient.no_reg) return;
            audioQueue.push(patient);
            if (audioEnabled) {
                processAudioQueue();
            }
        }

        // Fetch master list of clinics
        function fetchMasterData(callback) {
            fetch('api_antrian_poli_call.php?action=get_poli_list')
                .then(function(r){ return r.json(); })
                .then(function(res){
                    if (res.status === 'success') allPoliList = res.data;
                    if (callback) callback();
                });
        }

        function openSetupModal() {
            isModalOpen = true;
            var container = document.getElementById('setupRowsContainer');
            container.innerHTML = '';

            for (var i = 0; i < 3; i++) {
                var rowHtml = '<div class="setup-row">' +
                    '<h4>Channel ' + (i + 1) + '</h4>' +
                    '<select id="modalPoli' + i + '">' +
                    '<option value="">-- Pilih Poliklinik --</option>';
                for (var p = 0; p < allPoliList.length; p++) {
                    var selP = (channelsConfig[i] === allPoliList[p].kd_poli) ? 'selected' : '';
                    rowHtml += '<option value="' + allPoliList[p].kd_poli + '" ' + selP + '>' + escapeHtml(allPoliList[p].nm_poli) + '</option>';
                }
                rowHtml += '</select></div>';
                container.innerHTML += rowHtml;
            }

            document.getElementById('setupModal').style.display = 'flex';
        }

        function closeSetupModal() {
            isModalOpen = false;
            document.getElementById('setupModal').style.display = 'none';
        }

        function saveConfiguration() {
            for (var i = 0; i < 3; i++) {
                channelsConfig[i] = document.getElementById('modalPoli' + i).value;
            }
            try {
                localStorage.setItem('khanza_antrian_poli_channels_v2', JSON.stringify(channelsConfig));
            } catch(e) {}
            closeSetupModal();
            pollPoliStatus();
        }

        function pollPoliStatus() {
            if (isModalOpen) return;

            var validPolis = channelsConfig.filter(function(p){ return p && p.trim() !== ''; });

            if (validPolis.length === 0) {
                if (isInitialLoad) {
                    fetchMasterData(function() {
                        if (!isModalOpen) openSetupModal();
                    });
                }
                return;
            }

            var queryUrl = 'api_antrian_poli_call.php?polis=' + encodeURIComponent(validPolis.join(','));

            fetch(queryUrl)
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data.status !== 'success') return;

                    var channels = data.channels || [];
                    for (var c = 0; c < 3; c++) {
                        var targetPoliCode = channelsConfig[c];
                        var chData = null;
                        for (var k = 0; k < channels.length; k++) {
                            if (channels[k].kd_poli === targetPoliCode) {
                                chData = channels[k];
                                break;
                            }
                        }

                        if (chData) {
                            document.getElementById('titlePoli' + c).textContent = chData.nm_poli;

                            var act = chData.active_patient;
                            var numEl = document.getElementById('numCh' + c);
                            var patEl = document.getElementById('patientCh' + c);
                            var docEl = document.getElementById('titleDokter' + c);
                            var textBadgeEl = document.getElementById('textBadge' + c);

                            if (act && act.no_reg) {
                                numEl.textContent = String(act.no_reg).padStart(3, '0');
                                patEl.textContent = act.nm_pasien;
                                docEl.textContent = act.nm_dokter || chData.nm_dokter || '-';
                                textBadgeEl.textContent = 'Sedang Diperiksa';
                            } else {
                                numEl.textContent = '-';
                                patEl.textContent = 'Standby';
                                docEl.textContent = '-'; // Standby: show '-' instead of cached/old doctor
                                textBadgeEl.textContent = 'Menunggu Panggilan';
                            }

                            // Render upcoming list safely with HTML escaping
                            var upListEl = document.getElementById('listUpcoming' + c);
                            var countEl = document.getElementById('countUpcoming' + c);
                            var upcoming = chData.upcoming || [];
                            countEl.textContent = upcoming.length + ' Pasien';

                            if (upcoming.length > 0) {
                                var listHtml = '';
                                for (var u = 0; u < upcoming.length; u++) {
                                    listHtml += '<li class="upcoming-item">' +
                                        '<span class="item-reg">' + String(upcoming[u].no_reg).padStart(3, '0') + '</span>' +
                                        '<span class="item-name">' + escapeHtml(upcoming[u].nm_pasien) + '</span>' +
                                        '<span class="item-time">' + escapeHtml(upcoming[u].jam_reg) + '</span>' +
                                        '</li>';
                                }
                                upListEl.innerHTML = listHtml;
                            } else {
                                upListEl.innerHTML = '<li class="upcoming-empty">Tidak ada antrean menunggu</li>';
                            }
                        } else if (!targetPoliCode) {
                            document.getElementById('titlePoli' + c).textContent = 'Belum Dikonfigurasi';
                            document.getElementById('titleDokter' + c).textContent = '-';
                            document.getElementById('numCh' + c).textContent = '-';
                            document.getElementById('patientCh' + c).textContent = 'Standby';
                            document.getElementById('textBadge' + c).textContent = 'Menunggu Panggilan';
                            document.getElementById('listUpcoming' + c).innerHTML = '<li class="upcoming-empty">Channel belum disetel</li>';
                        }
                    }

                    // Check for new calling events specifically relevant to clinics displayed on this screen
                    var seq = data.call_sequence || 0;
                    if (!isInitialLoad && seq !== lastCallSequence && seq > 0 && data.hero) {
                        var isRelevant = channelsConfig.indexOf(data.hero.kd_poli) !== -1;
                        if (isRelevant) {
                            enqueueCall(data.hero);
                        }
                    }
                    lastCallSequence = seq;
                    isInitialLoad = false;
                })
                .catch(function(e) {
                    console.error('Error polling poli status:', e);
                });
        }

        // Initialize
        loadChannelsConfiguration();
        fetchMasterData();
        setInterval(pollPoliStatus, 3000);
        setTimeout(pollPoliStatus, 500);
    </script>
</body>

</html>
