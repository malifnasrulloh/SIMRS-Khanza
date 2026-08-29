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
    $runtext = "Selamat Datang di " . $setting['nama_instansi'] . " - Silakan menunggu nomor antrean Anda dipanggil menuju loket pendaftaran yang tersedia.";
}
?>
<!doctype html>
<html lang="id">

<head>
    <title>Layar Antrean Loket Pendaftaran – <?php echo htmlspecialchars($setting['nama_instansi']); ?></title>
    <link rel="icon" href="conf/assets/img/rs.png" type="image/x-icon">
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no" />
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700;800;900&family=Barlow+Condensed:wght@500;600;700;800;900&display=swap" rel="stylesheet">
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
            --card-bg: rgba(255, 255, 255, 0.94);
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
            background: linear-gradient(150deg, #dbeafe 0%, #bfdbfe 50%, #eff6ff 100%);
            color: var(--td);
        }

        .screen {
            display: flex;
            flex-direction: column;
            height: 100vh;
            padding: 0.7vw 0.8vw 0;
            gap: 0.5vw;
        }

        /* Topbar */
        .topbar {
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: linear-gradient(90deg, var(--bd) 0%, var(--bm) 55%, var(--bl) 100%);
            border-radius: 0.8vw;
            padding: 0.5vw 1.2vw;
            box-shadow: 0 0.4vw 1.2vw rgba(13, 71, 161, 0.35);
            color: #fff;
        }

        .topbar-left {
            display: flex;
            align-items: center;
            gap: 0.9vw;
        }

        .topbar-logo {
            width: 4.2vw;
            height: 4.2vw;
            border-radius: 50%;
            border: 0.2vw solid rgba(255, 255, 255, 0.6);
            background: #fff;
            padding: 0.2vw;
            object-fit: contain;
        }

        .topbar-name {
            font-size: 1.55vw;
            font-weight: 900;
            letter-spacing: 0.02em;
            line-height: 1.15;
            text-shadow: 0 0.1vw 0.3vw rgba(0, 0, 0, 0.3);
        }

        .topbar-sub {
            font-size: 0.85vw;
            opacity: 0.9;
            margin-top: 0.1vw;
        }

        .topbar-right {
            display: flex;
            align-items: center;
            gap: 1.2vw;
        }

        .live-badge {
            display: flex;
            align-items: center;
            gap: 0.4vw;
            background: rgba(239, 68, 68, 0.85);
            padding: 0.3vw 0.8vw;
            border-radius: 2vw;
            font-size: 0.9vw;
            font-weight: 800;
            letter-spacing: 0.08em;
            text-transform: uppercase;
            box-shadow: 0 0 0.8vw rgba(239, 68, 68, 0.5);
            animation: pulse-live 1.8s infinite;
        }

        .live-dot {
            width: 0.5vw;
            height: 0.5vw;
            background: #fff;
            border-radius: 50%;
        }

        @keyframes pulse-live {
            0%, 100% { opacity: 1; transform: scale(1); }
            50% { opacity: 0.82; transform: scale(0.96); }
        }

        .clock-block {
            text-align: right;
            border-left: 0.15vw solid rgba(255, 255, 255, 0.3);
            padding-left: 1.2vw;
        }

        .clock-time {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 2.3vw;
            font-weight: 800;
            letter-spacing: 0.05em;
            line-height: 1;
        }

        .clock-date {
            font-size: 0.75vw;
            opacity: 0.88;
            margin-top: 0.15vw;
        }

        /* Main Content Layout (38% Left Hero, 62% Right Loket Grid + Table) */
        .content-layout {
            flex: 1;
            display: grid;
            grid-template-columns: 38% calc(62% - 0.6vw);
            gap: 0.6vw;
            min-height: 0;
        }

        /* Left Column: Hero Calling Display */
        .left-col {
            display: flex;
            flex-direction: column;
            gap: 0.6vw;
            min-height: 0;
        }

        .hero-card {
            flex: 1;
            background: var(--card-bg);
            border-radius: 1vw;
            box-shadow: 0 0.5vw 1.5vw rgba(13, 71, 161, 0.18);
            border: 0.25vw solid #fff;
            display: flex;
            flex-direction: column;
            padding: 1.2vw;
            text-align: center;
            justify-content: space-between;
            position: relative;
            overflow: hidden;
            transition: all 0.3s ease;
        }

        .hero-card.calling {
            border-color: var(--grn);
            background: linear-gradient(180deg, #f0fdf4 0%, #dcfce7 100%);
            box-shadow: 0 0 2.5vw rgba(0, 200, 83, 0.45);
            animation: hero-glow 1.2s ease-in-out infinite alternate;
        }

        @keyframes hero-glow {
            from { transform: scale(1); box-shadow: 0 0 1.5vw rgba(0, 200, 83, 0.3); }
            to { transform: scale(1.01); box-shadow: 0 0 2.8vw rgba(0, 200, 83, 0.65); }
        }

        .hero-header {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.6vw;
        }

        .hero-header-icon {
            background: linear-gradient(135deg, var(--bm), var(--bd));
            color: #fff;
            width: 3vw;
            height: 3vw;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .hero-header-icon .material-icons {
            font-size: 1.8vw;
        }

        .hero-title {
            font-size: 1.5vw;
            font-weight: 900;
            color: var(--bd);
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }

        .hero-sub {
            font-size: 0.9vw;
            color: var(--tm);
            font-weight: 600;
        }

        .hero-number-wrap {
            margin: 0.8vw 0;
        }

        .hero-number-label {
            font-size: 1.1vw;
            font-weight: 800;
            color: var(--tm);
            text-transform: uppercase;
            letter-spacing: 0.1em;
        }

        .hero-number {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 8.5vw;
            font-weight: 900;
            line-height: 0.95;
            color: var(--bd);
            text-shadow: 0 0.2vw 0.5vw rgba(13, 71, 161, 0.2);
            letter-spacing: 0.02em;
        }

        .hero-card.calling .hero-number {
            color: #047857;
            text-shadow: 0 0.2vw 0.8vw rgba(4, 120, 87, 0.35);
        }

        .hero-loket-badge {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            gap: 0.5vw;
            background: linear-gradient(135deg, var(--bd), var(--bm));
            color: #fff;
            padding: 0.6vw 1.8vw;
            border-radius: 0.8vw;
            font-size: 2.2vw;
            font-weight: 900;
            letter-spacing: 0.04em;
            box-shadow: 0 0.4vw 1vw rgba(13, 71, 161, 0.3);
        }

        .hero-card.calling .hero-loket-badge {
            background: linear-gradient(135deg, #059669, #10b981);
            box-shadow: 0 0.4vw 1vw rgba(5, 150, 105, 0.4);
        }

        .hero-status-tag {
            font-size: 1vw;
            font-weight: 800;
            color: var(--tm);
            margin-top: 0.5vw;
        }

        /* Right Column: Active Lokets Grid + Recent Tickets */
        .right-col {
            display: flex;
            flex-direction: column;
            gap: 0.6vw;
            min-height: 0;
        }

        /* 6 Loket Cards Grid */
        .loket-grid {
            display: grid;
            grid-template-columns: repeat(3, 1fr);
            gap: 0.5vw;
            flex-shrink: 0;
        }

        .loket-box {
            background: var(--card-bg);
            border-radius: 0.8vw;
            border: 0.15vw solid #fff;
            padding: 0.6vw 0.4vw;
            text-align: center;
            box-shadow: 0 0.3vw 0.8vw rgba(13, 71, 161, 0.1);
            transition: all 0.2s ease;
        }

        .loket-box.active {
            border-color: var(--bl);
            background: #fff;
            box-shadow: 0 0.4vw 1.2vw rgba(25, 118, 210, 0.22);
        }

        .loket-box-title {
            font-size: 1vw;
            font-weight: 800;
            color: var(--td);
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        .loket-box-num {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 2.8vw;
            font-weight: 900;
            line-height: 1;
            color: var(--bm);
            margin: 0.2vw 0;
        }

        .loket-box-status {
            font-size: 0.75vw;
            font-weight: 800;
            padding: 0.15vw 0.5vw;
            border-radius: 1vw;
            display: inline-block;
            background: var(--bp);
            color: var(--bm);
        }

        .loket-box.active .loket-box-status {
            background: #dcfce7;
            color: #047857;
        }

        /* Recent Ticket Stream Card */
        .stream-card {
            flex: 1;
            background: var(--card-bg);
            border-radius: 0.8vw;
            border: 0.15vw solid #fff;
            box-shadow: 0 0.3vw 0.8vw rgba(13, 71, 161, 0.1);
            display: flex;
            flex-direction: column;
            min-height: 0;
            overflow: hidden;
        }

        .stream-card-head {
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0.5vw 1vw;
            background: linear-gradient(90deg, var(--bp) 0%, rgba(227, 242, 253, 0.3) 100%);
            border-bottom: 0.1vw solid rgba(13, 71, 161, 0.1);
        }

        .stream-title {
            font-size: 1vw;
            font-weight: 800;
            color: var(--td);
            display: flex;
            align-items: center;
            gap: 0.4vw;
        }

        .stream-title .material-icons {
            font-size: 1.2vw;
            color: var(--bm);
        }

        .stream-pill {
            font-size: 0.8vw;
            font-weight: 800;
            background: var(--bm);
            color: #fff;
            padding: 0.2vw 0.7vw;
            border-radius: 1vw;
        }

        .stream-table-wrap {
            flex: 1;
            overflow-y: auto;
            padding: 0.4vw;
        }

        table.t {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.95vw;
        }

        table.t thead th {
            position: sticky;
            top: 0;
            background: var(--bp);
            color: var(--td);
            font-weight: 800;
            text-align: left;
            padding: 0.4vw 0.8vw;
            font-size: 0.8vw;
            text-transform: uppercase;
            letter-spacing: 0.04em;
        }

        table.t tbody tr {
            border-bottom: 0.08vw solid rgba(0, 0, 0, 0.05);
        }

        table.t tbody tr:nth-child(even) {
            background: rgba(240, 249, 255, 0.5);
        }

        table.t td {
            padding: 0.4vw 0.8vw;
            font-weight: 700;
        }

        table.t td.num-cell {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 1.4vw;
            font-weight: 800;
            color: var(--bd);
        }

        table.t td.time-cell {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 1.1vw;
            color: var(--tm);
        }

        .badge-status {
            font-size: 0.75vw;
            font-weight: 800;
            padding: 0.15vw 0.5vw;
            border-radius: 0.4vw;
            display: inline-block;
        }

        .badge-wait {
            background: #fef3c7;
            color: #92400e;
        }

        .badge-called {
            background: #dcfce7;
            color: #065f46;
        }

        .badge-done {
            background: #e2e8f0;
            color: #475569;
        }

        /* Footer Marquee */
        .footer-ticker {
            flex-shrink: 0;
            display: flex;
            align-items: center;
            background: linear-gradient(90deg, var(--bd), var(--bm));
            border-radius: 0.6vw;
            padding: 0.35vw 0.8vw;
            color: #fff;
            overflow: hidden;
            box-shadow: 0 -0.2vw 0.8vw rgba(13, 71, 161, 0.15);
        }

        .ticker-label {
            display: flex;
            align-items: center;
            gap: 0.3vw;
            font-size: 0.85vw;
            font-weight: 900;
            background: rgba(255, 255, 255, 0.2);
            padding: 0.2vw 0.6vw;
            border-radius: 0.4vw;
            margin-right: 0.8vw;
            flex-shrink: 0;
            text-transform: uppercase;
        }

        .ticker-label .material-icons {
            font-size: 1.1vw;
        }

        .ticker-text {
            font-size: 0.95vw;
            font-weight: 700;
            white-space: nowrap;
        }

        /* Non-blocking Audio Notification Toast */
        #audioToast {
            position: fixed;
            bottom: 3.5vw;
            right: 1.5vw;
            background: linear-gradient(135deg, #1e293b, #0f172a);
            color: #fff;
            border: 0.15vw solid var(--bl);
            border-radius: 0.8vw;
            padding: 0.8vw 1.2vw;
            display: flex;
            align-items: center;
            gap: 0.8vw;
            box-shadow: 0 0.5vw 1.5vw rgba(0, 0, 0, 0.3);
            cursor: pointer;
            z-index: 9999;
            transition: opacity 0.3s ease;
        }

        #audioToast .toast-icon {
            background: var(--bm);
            width: 2.2vw;
            height: 2.2vw;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 1.2vw;
        }

        #audioToast .toast-text {
            font-size: 0.85vw;
            font-weight: 700;
        }

        #audioToast .toast-btn {
            background: var(--grn);
            color: #065f46;
            font-weight: 800;
            padding: 0.3vw 0.7vw;
            border-radius: 0.4vw;
            font-size: 0.8vw;
        }
    </style>
</head>

<body onclick="unlockAudio()">
    <!-- Non-blocking Audio Toast -->
    <div id="audioToast" onclick="unlockAudio()">
        <div class="toast-icon">
            <span class="material-icons">volume_up</span>
        </div>
        <div class="toast-text">Klik layar untuk mengaktifkan audio panggilan</div>
        <div class="toast-btn">AKTIFKAN</div>
    </div>

    <div class="screen">
        <!-- Topbar -->
        <header class="topbar">
            <div class="topbar-left">
                <?php if (!empty($setting['logo'])) { ?>
                    <img class="topbar-logo" src="data:image/jpeg;base64,<?php echo base64_encode($setting['logo']); ?>" alt="Logo RS">
                <?php } ?>
                <div>
                    <div class="topbar-name"><?php echo htmlspecialchars($setting['nama_instansi']); ?></div>
                    <div class="topbar-sub">Sistem Antrean Pendaftaran Loket Pasien</div>
                </div>
            </div>
            <div class="topbar-right">
                <div class="live-badge">
                    <div class="live-dot"></div>
                    <span>LIVE CALL</span>
                </div>
                <div class="clock-block">
                    <div class="clock-time" id="clockTime">--:--:--</div>
                    <div class="clock-date" id="clockDate"><?php echo date('d M Y'); ?></div>
                </div>
            </div>
        </header>

        <!-- Main Layout -->
        <main class="content-layout">
            <!-- Left Column: Hero Calling Card -->
            <section class="left-col">
                <div class="hero-card" id="heroCard">
                    <div class="hero-header">
                        <div class="hero-header-icon">
                            <span class="material-icons">campaign</span>
                        </div>
                        <div>
                            <div class="hero-title">Panggilan Antrean</div>
                            <div class="hero-sub">Silakan Menuju ke Loket Pendaftaran</div>
                        </div>
                    </div>

                    <div class="hero-number-wrap">
                        <div class="hero-number-label">Nomor Antrean</div>
                        <div class="hero-number" id="heroAntri">-</div>
                    </div>

                    <div>
                        <div class="hero-loket-badge" id="heroLoket">
                            <span class="material-icons">meeting_room</span>
                            <span id="heroLoketText">LOKET -</span>
                        </div>
                        <div class="hero-status-tag" id="heroStatusText">Menunggu Panggilan Berikutnya</div>
                    </div>
                </div>
            </section>

            <!-- Right Column: Active Lokets Grid (6 Lokets) + Recent Tickets -->
            <section class="right-col">
                <!-- 6 Loket Cards Grid (2x3) -->
                <div class="loket-grid" id="loketGrid">
                    <?php for ($i = 1; $i <= 6; $i++) { ?>
                    <div class="loket-box" id="loketBox<?php echo $i; ?>">
                        <div class="loket-box-title">Loket <?php echo $i; ?></div>
                        <div class="loket-box-num" id="loketNum<?php echo $i; ?>">-</div>
                        <div class="loket-box-status" id="loketStatus<?php echo $i; ?>">Standby</div>
                    </div>
                    <?php } ?>
                </div>

                <!-- Recent Ticket Stream -->
                <div class="stream-card">
                    <div class="stream-card-head">
                        <div class="stream-title">
                            <span class="material-icons">receipt_long</span>
                            <span>Daftar Antrean Tiket Terakhir</span>
                        </div>
                        <div class="stream-pill" id="totalCetakPill">0 Tiket Hari Ini</div>
                    </div>
                    <div class="stream-table-wrap">
                        <table class="t">
                            <thead>
                                <tr>
                                    <th>No. Antrean</th>
                                    <th>Jam Ambil</th>
                                    <th>Status Pelayanan</th>
                                </tr>
                            </thead>
                            <tbody id="ticketBody">
                                <tr>
                                    <td colspan="3" style="text-align: center; color: var(--tm); padding: 1.5vw;">Memuat data antrean...</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>
            </section>
        </main>

        <!-- Footer Running Text -->
        <footer class="footer-ticker">
            <div class="ticker-label">
                <span class="material-icons">info</span>
                <span>Info</span>
            </div>
            <marquee class="ticker-text" behavior="scroll" direction="left" scrollamount="6">
                <?php echo htmlspecialchars($runtext); ?>
            </marquee>
        </footer>
    </div>

    <!-- Audio Chaining & Queue Engine Script -->
    <script>
        // Real-time Clock
        function updateClock() {
            var now = new Date();
            var h = String(now.getHours()).padStart(2, '0');
            var m = String(now.getMinutes()).padStart(2, '0');
            var s = String(now.getSeconds()).padStart(2, '0');
            document.getElementById('clockTime').textContent = h + ':' + m + ':' + s;
        }
        setInterval(updateClock, 1000);
        updateClock();

        // Audio System
        var audioEnabled = false;
        var audioQueue = [];
        var isPlayingAudio = false;
        var lastCallSequence = -1;
        var isInitialLoad = true;

        function unlockAudio() {
            if (!audioEnabled) {
                audioEnabled = true;
                var toast = document.getElementById('audioToast');
                if (toast) toast.style.display = 'none';
                // Play dummy silent audio to unlock Web Audio context
                var snd = new Audio('suara/nomor-urut.mp3');
                snd.volume = 0.01;
                snd.play().catch(function(){});
            }
        }

        // Chained Audio Playlist Generator for Khanza Audio Files (1-999)
        function buildNumberPlaylist(num) {
            var files = [];
            var n = parseInt(num, 10);
            if (isNaN(n) || n <= 0) return files;

            var urut = [
                "", "suara/satu.mp3", "suara/dua.mp3", "suara/tiga.mp3", "suara/empat.mp3",
                "suara/lima.mp3", "suara/enam.mp3", "suara/tujuh.mp3", "suara/delapan.mp3",
                "suara/sembilan.mp3", "suara/sepuluh.mp3", "suara/sebelas.mp3"
            ];

            function parse(x) {
                if (x < 12) {
                    if (urut[x]) files.push(urut[x]);
                } else if (x < 20) {
                    if (urut[x - 10]) files.push(urut[x - 10]);
                    files.push("suara/belas.mp3");
                } else if (x < 100) {
                    var puluhan = Math.floor(x / 10);
                    if (urut[puluhan]) files.push(urut[puluhan]);
                    files.push("suara/puluh.mp3");
                    if (x % 10 > 0) parse(x % 10);
                } else if (x < 200) {
                    files.push("suara/seratus.mp3");
                    if (x - 100 > 0) parse(x - 100);
                } else if (x < 1000) {
                    var ratusan = Math.floor(x / 100);
                    if (urut[ratusan]) files.push(urut[ratusan]);
                    files.push("suara/ratus.mp3");
                    if (x % 100 > 0) parse(x % 100);
                }
            }

            parse(n);
            return files;
        }

        function playAudioChain(playlist, onComplete) {
            if (!playlist || playlist.length === 0) {
                if (onComplete) onComplete();
                return;
            }
            var idx = 0;
            function playNext() {
                if (idx >= playlist.length) {
                    if (onComplete) onComplete();
                    return;
                }
                var audio = new Audio(playlist[idx]);
                idx++;
                audio.onended = playNext;
                audio.onerror = playNext;
                audio.play().catch(function() {
                    playNext();
                });
            }
            playNext();
        }

        // Process buffered audio queue sequentially to prevent audio drop/collisions
        function processAudioQueue() {
            if (!audioEnabled || isPlayingAudio || audioQueue.length === 0) return;

            isPlayingAudio = true;
            var item = audioQueue.shift();

            var playlist = ["suara/nomor-urut.mp3"];
            playlist = playlist.concat(buildNumberPlaylist(item.antrian));
            playlist.push("suara/loket.mp3");
            playlist = playlist.concat(buildNumberPlaylist(item.loket));

            var heroCard = document.getElementById('heroCard');
            heroCard.classList.add('calling');

            playAudioChain(playlist, function() {
                setTimeout(function() {
                    heroCard.classList.remove('calling');
                    isPlayingAudio = false;
                    processAudioQueue();
                }, 1000);
            });
        }

        function enqueueCall(antrian, loket) {
            audioQueue.push({ antrian: antrian, loket: loket });
            processAudioQueue();
        }

        // Poll Loket Calls & Statistics
        function pollData() {
            fetch('api_antrian_loket_call.php')
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data.status !== 'success') return;

                    // 1. Update Hero Calling Card
                    var heroCard = document.getElementById('heroCard');
                    if (data.hero && data.hero.antrian) {
                        var antri = String(data.hero.antrian).padStart(3, '0');
                        var lok = data.hero.loket;
                        document.getElementById('heroAntri').textContent = antri;
                        document.getElementById('heroLoketText').textContent = 'LOKET ' + lok;
                        document.getElementById('heroStatusText').textContent = 'Sedang Dipanggil Menuju Loket ' + lok;

                        // Trigger Call Audio using call_sequence/call_id token
                        var currentSeq = data.call_sequence || 0;
                        if (!isInitialLoad && currentSeq > lastCallSequence && lastCallSequence >= 0) {
                            enqueueCall(data.hero.antrian, data.hero.loket);
                        }
                        lastCallSequence = currentSeq;
                    } else {
                        document.getElementById('heroAntri').textContent = '-';
                        document.getElementById('heroLoketText').textContent = 'LOKET -';
                        document.getElementById('heroStatusText').textContent = 'Menunggu Panggilan Berikutnya';
                        heroCard.classList.remove('calling');
                    }

                    if (isInitialLoad) {
                        lastCallSequence = data.call_sequence || 0;
                        isInitialLoad = false;
                    }

                    // 2. Update Loket Cards Grid (Loket 1..6)
                    for (var i = 1; i <= 6; i++) {
                        var box = document.getElementById('loketBox' + i);
                        if (!box) continue;
                        var numEl = document.getElementById('loketNum' + i);
                        var statEl = document.getElementById('loketStatus' + i);
                        if (data.lokets && data.lokets[i]) {
                            box.classList.add('active');
                            numEl.textContent = String(data.lokets[i]).padStart(3, '0');
                            statEl.textContent = 'Melayani';
                        } else {
                            box.classList.remove('active');
                            numEl.textContent = '-';
                            statEl.textContent = 'Standby';
                        }
                    }

                    // 3. Update Recent Tickets Stream
                    document.getElementById('totalCetakPill').textContent = data.total_cetak + ' Tiket Hari Ini';
                    var tbody = document.getElementById('ticketBody');
                    if (data.recent_tickets && data.recent_tickets.length > 0) {
                        var html = '';
                        for (var j = 0; j < data.recent_tickets.length; j++) {
                            var t = data.recent_tickets[j];
                            var badgeClass = 'badge-wait';
                            if (t.status.indexOf('Loket') !== -1) {
                                badgeClass = 'badge-called';
                            } else if (t.status === 'Selesai') {
                                badgeClass = 'badge-done';
                            }
                            html += '<tr>' +
                                '<td class="num-cell">' + t.nomor + '</td>' +
                                '<td class="time-cell">' + t.jam + '</td>' +
                                '<td><span class="badge-status ' + badgeClass + '">' + t.status + '</span></td>' +
                                '</tr>';
                        }
                        tbody.innerHTML = html;
                    } else {
                        tbody.innerHTML = '<tr><td colspan="3" style="text-align: center; color: var(--tm); padding: 1.5vw;">Belum ada tiket antrean hari ini</td></tr>';
                    }
                })
                .catch(function(e) {
                    console.error('Fetch error:', e);
                });
        }

        setInterval(pollData, 3000);
        pollData();
    </script>
</body>

</html>
