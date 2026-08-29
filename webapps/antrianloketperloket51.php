<?php
session_start();
require_once('conf/conf.php');
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");

$loket = isset($_GET['loket']) ? intval($_GET['loket']) : 1;
if ($loket < 1) $loket = 1;

$setting = mysqli_fetch_array(bukaquery("select setting.nama_instansi,setting.alamat_instansi,setting.kabupaten,setting.propinsi,setting.kontak,setting.email,setting.logo from setting"));
$runtext = "";
$resRun = bukaquery("select teks from runtext where aktifkan='Yes' limit 1");
if ($resRun && $rowRun = mysqli_fetch_array($resRun)) {
    $runtext = $rowRun['teks'];
}
if (empty($runtext)) {
    $runtext = "Selamat Datang di " . $setting['nama_instansi'] . " - Loket " . $loket . " siap melayani pendaftaran Anda dengan sepenuh hati.";
}
?>
<!doctype html>
<html lang="id">

<head>
    <title>Display Loket <?php echo $loket; ?> – <?php echo htmlspecialchars($setting['nama_instansi']); ?></title>
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
            background: linear-gradient(150deg, #0d47a1 0%, #1565c0 50%, #0d2b5e 100%);
            color: #fff;
        }

        .screen {
            display: flex;
            flex-direction: column;
            height: 100vh;
            padding: 1vw 1.2vw;
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
            padding: 0.7vw 1.5vw;
        }

        .topbar-left {
            display: flex;
            align-items: center;
            gap: 1vw;
        }

        .topbar-logo {
            width: 4.5vw;
            height: 4.5vw;
            border-radius: 50%;
            border: 0.2vw solid rgba(255, 255, 255, 0.8);
            background: #fff;
            padding: 0.25vw;
            object-fit: contain;
        }

        .topbar-name {
            font-size: 1.8vw;
            font-weight: 900;
            line-height: 1.15;
            text-shadow: 0 0.1vw 0.4vw rgba(0, 0, 0, 0.4);
        }

        .topbar-sub {
            font-size: 0.95vw;
            opacity: 0.9;
        }

        .clock-time {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 2.8vw;
            font-weight: 800;
            line-height: 1;
            text-align: right;
        }

        /* Main Giant Card */
        .giant-stage {
            flex: 1;
            background: var(--card-bg);
            border-radius: 1.5vw;
            border: 0.4vw solid #fff;
            box-shadow: 0 1vw 3vw rgba(0, 0, 0, 0.35);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: space-between;
            padding: 2vw;
            color: var(--td);
            position: relative;
            overflow: hidden;
            transition: all 0.3s ease;
        }

        .giant-stage.calling {
            background: linear-gradient(180deg, #f0fdf4 0%, #dcfce7 100%);
            border-color: var(--grn);
            animation: giant-pulse 1.2s infinite alternate;
        }

        @keyframes giant-pulse {
            from { transform: scale(1); box-shadow: 0 0 2vw rgba(0, 200, 83, 0.4); }
            to { transform: scale(1.015); box-shadow: 0 0 4vw rgba(0, 200, 83, 0.75); }
        }

        /* Stage Header: Loket Title */
        .stage-badge {
            background: linear-gradient(135deg, var(--bd), var(--bm));
            color: #fff;
            padding: 0.8vw 3.5vw;
            border-radius: 1vw;
            font-size: 3.2vw;
            font-weight: 900;
            letter-spacing: 0.06em;
            text-transform: uppercase;
            box-shadow: 0 0.5vw 1.5vw rgba(13, 71, 161, 0.3);
            display: flex;
            align-items: center;
            gap: 0.8vw;
        }

        .stage-badge .material-icons {
            font-size: 3.2vw;
        }

        .giant-stage.calling .stage-badge {
            background: linear-gradient(135deg, #059669, #10b981);
            box-shadow: 0 0.5vw 2vw rgba(5, 150, 105, 0.4);
        }

        /* Stage Center: Gigantic Number */
        .stage-center {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            margin: auto 0;
        }

        .stage-label {
            font-size: 1.8vw;
            font-weight: 800;
            color: var(--tm);
            text-transform: uppercase;
            letter-spacing: 0.15em;
        }

        .stage-number {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 20vw;
            font-weight: 900;
            line-height: 0.88;
            color: var(--bd);
            text-shadow: 0 0.4vw 1vw rgba(13, 71, 161, 0.2);
            letter-spacing: 0.02em;
        }

        .giant-stage.calling .stage-number {
            color: #047857;
            text-shadow: 0 0.4vw 1.5vw rgba(4, 120, 87, 0.4);
        }

        /* Stage Status Footer */
        .stage-status {
            font-size: 1.6vw;
            font-weight: 800;
            padding: 0.6vw 2vw;
            border-radius: 2vw;
            background: var(--bp);
            color: var(--bm);
            display: flex;
            align-items: center;
            gap: 0.6vw;
        }

        .giant-stage.calling .stage-status {
            background: #bbf7d0;
            color: #065f46;
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
    </style>
</head>

<body onclick="unlockAudio()">
    <!-- Non-blocking Audio Notification Toast -->
    <div id="audioToast" onclick="unlockAudio()">
        <div class="toast-icon">
            <span class="material-icons">volume_up</span>
        </div>
        <div class="toast-text">Klik layar untuk mengaktifkan audio Loket <?php echo $loket; ?></div>
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
                    <div class="topbar-sub">Sistem Informasi Rawat Jalan & Pendaftaran</div>
                </div>
            </div>
            <div class="clock-time" id="clockTime">--:--:--</div>
        </header>

        <!-- Main Giant Stage -->
        <main class="giant-stage" id="stageCard">
            <div class="stage-badge">
                <span class="material-icons">meeting_room</span>
                <span>LOKET <?php echo $loket; ?></span>
            </div>

            <div class="stage-center">
                <div class="stage-label">Nomor Antrean</div>
                <div class="stage-number" id="stageNumber">-</div>
            </div>

            <div class="stage-status" id="stageStatus">
                <span class="material-icons">hourglass_empty</span>
                <span id="stageStatusText">Standby Menunggu Antrean</span>
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
        var currentLoket = <?php echo $loket; ?>;
        var audioEnabled = false;
        var isPlayingAudio = false;
        var lastCallId = -1;
        var isInitialLoad = true;

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
                var snd = new Audio('suara/nomor-urut.mp3');
                snd.volume = 0.01;
                snd.play().catch(function(){});
            }
        }

        // Chained Audio Playlist Generator for Khanza Audio Files
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

        function announceCall(antrian, loket) {
            if (!audioEnabled || isPlayingAudio) return;
            isPlayingAudio = true;

            var playlist = ["suara/nomor-urut.mp3"];
            playlist = playlist.concat(buildNumberPlaylist(antrian));
            playlist.push("suara/loket.mp3");
            playlist = playlist.concat(buildNumberPlaylist(loket));

            var stageCard = document.getElementById('stageCard');
            stageCard.classList.add('calling');

            playAudioChain(playlist, function() {
                isPlayingAudio = false;
                setTimeout(function() {
                    stageCard.classList.remove('calling');
                }, 3000);
            });
        }

        function pollLoket() {
            fetch('api_antrian_loket_call.php?loket=' + currentLoket)
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data.status !== 'success') return;

                    var activeNum = (data.lokets && data.lokets[currentLoket]) ? data.lokets[currentLoket] : 0;
                    var stageNumEl = document.getElementById('stageNumber');
                    var stageStatEl = document.getElementById('stageStatusText');
                    var stageCard = document.getElementById('stageCard');

                    if (activeNum > 0) {
                        stageNumEl.textContent = String(activeNum).padStart(3, '0');
                        stageStatEl.textContent = 'Sedang Dilayani di Loket ' + currentLoket;

                        var callId = (data.hero && data.hero.call_id) ? data.hero.call_id : activeNum;
                        if (!isInitialLoad && callId !== lastCallId && lastCallId >= 0) {
                            announceCall(activeNum, currentLoket);
                        }
                        lastCallId = callId;
                    } else {
                        stageNumEl.textContent = '-';
                        stageStatEl.textContent = 'Standby Menunggu Antrean';
                        stageCard.classList.remove('calling');
                    }

                    if (isInitialLoad) {
                        lastCallId = (data.hero && data.hero.call_id) ? data.hero.call_id : activeNum;
                        isInitialLoad = false;
                    }
                })
                .catch(function(e) {
                    console.error('Error fetching loket state:', e);
                });
        }

        setInterval(pollLoket, 3000);
        pollLoket();
    </script>
</body>

</html>
