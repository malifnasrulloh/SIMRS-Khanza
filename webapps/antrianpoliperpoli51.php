<?php
session_start();
require_once('conf/conf.php');
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");

$kd_poli = isset($_GET['p']) ? validTeks4($_GET['p'], 20) : (isset($_GET['kd_poli']) ? validTeks4($_GET['kd_poli'], 20) : '');
$kd_dokter = isset($_GET['d']) ? validTeks4($_GET['d'], 20) : (isset($_GET['kd_dokter']) ? validTeks4($_GET['kd_dokter'], 20) : '');

$setting = mysqli_fetch_array(bukaquery("select setting.nama_instansi,setting.alamat_instansi,setting.kabupaten,setting.propinsi,setting.kontak,setting.email,setting.logo from setting"));
$runtext = "";
$resRun = bukaquery("select teks from runtext where aktifkan='Yes' limit 1");
if ($resRun && $rowRun = mysqli_fetch_array($resRun)) {
    $runtext = $rowRun['teks'];
}
if (empty($runtext)) {
    $runtext = "Selamat Datang di " . $setting['nama_instansi'] . " - Antrean Poliklinik. Silakan menunggu hingga nomor urut Anda dipanggil.";
}
?>
<!doctype html>
<html lang="id">

<head>
    <title>Display Pintu Poliklinik – <?php echo htmlspecialchars($setting['nama_instansi']); ?></title>
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
            background: linear-gradient(145deg, #0d47a1 0%, #1565c0 50%, #0d2b5e 100%);
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

        .clock-time {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 2.6vw;
            font-weight: 800;
            line-height: 1;
            text-align: right;
        }

        /* Main Content Layout (70% Giant Stage + 30% Upcoming List) */
        .main-layout {
            flex: 1;
            display: grid;
            grid-template-columns: 2fr 1fr;
            gap: 1vw;
            min-height: 0;
        }

        /* Giant Stage Card */
        .giant-stage {
            background: var(--card-bg);
            border-radius: 1.5vw;
            border: 0.35vw solid #fff;
            box-shadow: 0 1vw 3vw rgba(0, 0, 0, 0.35);
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: space-between;
            padding: 1.5vw 2vw;
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

        .stage-badge-header {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 0.3vw;
            width: 100%;
        }

        .badge-room {
            background: linear-gradient(135deg, var(--bd), var(--bm));
            color: #fff;
            padding: 0.6vw 2.5vw;
            border-radius: 0.8vw;
            font-size: 2vw;
            font-weight: 900;
            letter-spacing: 0.05em;
            text-transform: uppercase;
            box-shadow: 0 0.4vw 1.2vw rgba(13, 71, 161, 0.3);
            display: flex;
            align-items: center;
            gap: 0.6vw;
        }

        .giant-stage.calling .badge-room {
            background: linear-gradient(135deg, #059669, #10b981);
        }

        .doctor-banner {
            font-size: 1.3vw;
            font-weight: 800;
            color: var(--bm);
            text-align: center;
        }

        .stage-center {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            margin: auto 0;
            width: 100%;
        }

        .stage-label {
            font-size: 1.3vw;
            font-weight: 800;
            color: var(--tm);
            text-transform: uppercase;
            letter-spacing: 0.15em;
        }

        .stage-number {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 13vw;
            font-weight: 900;
            line-height: 0.9;
            color: var(--bd);
            text-shadow: 0 0.4vw 1vw rgba(13, 71, 161, 0.2);
            letter-spacing: 0.02em;
        }

        .giant-stage.calling .stage-number {
            color: #047857;
            text-shadow: 0 0.4vw 1.5vw rgba(4, 120, 87, 0.4);
        }

        /* Full Patient Name: Multi-line Auto-wrapping to prevent cropping */
        .stage-patient-name {
            font-size: clamp(1.6rem, 2.3vw, 3.2rem);
            font-weight: 900;
            line-height: 1.15;
            color: var(--td);
            text-align: center;
            max-width: 98%;
            word-wrap: break-word;
            overflow-wrap: break-word;
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            padding: 0.2vw 0.5vw;
        }

        .giant-stage.calling .stage-patient-name {
            color: #065f46;
        }

        .stage-status-bar {
            font-size: 1.2vw;
            font-weight: 800;
            padding: 0.4vw 1.8vw;
            border-radius: 1.5vw;
            background: var(--bp);
            color: var(--bm);
            display: flex;
            align-items: center;
            gap: 0.5vw;
        }

        .giant-stage.calling .stage-status-bar {
            background: #bbf7d0;
            color: #065f46;
        }

        /* Right Panel: Upcoming Queue */
        .side-upcoming {
            background: var(--card-bg);
            border-radius: 1.5vw;
            border: 0.25vw solid #fff;
            box-shadow: 0 0.8vw 2.5vw rgba(0, 0, 0, 0.25);
            display: flex;
            flex-direction: column;
            color: var(--td);
            padding: 1.2vw;
            overflow: hidden;
        }

        .side-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            font-size: 1.1vw;
            font-weight: 900;
            color: var(--bd);
            padding-bottom: 0.6vw;
            border-bottom: 0.15vw solid #e2e8f0;
            margin-bottom: 0.8vw;
            text-transform: uppercase;
        }

        .side-list {
            list-style: none;
            display: flex;
            flex-direction: column;
            gap: 0.6vw;
            overflow-y: auto;
            flex: 1;
        }

        .side-item {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: #f8fafc;
            padding: 0.7vw 0.9vw;
            border-radius: 0.6vw;
            border: 1px solid #e2e8f0;
            font-size: 1.1vw;
            transition: all 0.2s ease;
        }

        .side-item:hover {
            background: var(--bp);
            border-color: var(--bl);
        }

        .side-reg {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 1.6vw;
            font-weight: 900;
            color: var(--bd);
            background: var(--bp);
            padding: 0.1vw 0.6vw;
            border-radius: 0.4vw;
            min-width: 3.2vw;
            text-align: center;
        }

        .side-name {
            font-weight: 800;
            color: var(--td);
            flex: 1;
            margin: 0 0.8vw;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .side-time {
            font-size: 0.9vw;
            font-weight: 700;
            color: var(--tm);
        }

        .side-empty {
            display: flex;
            align-items: center;
            justify-content: center;
            height: 100%;
            font-size: 1.1vw;
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
    </style>
</head>

<body onclick="unlockAudio()">
    <!-- Audio Toast -->
    <div id="audioToast" onclick="unlockAudio()">
        <div class="toast-icon">
            <span class="material-icons">volume_up</span>
        </div>
        <div class="toast-text">Klik layar untuk mengaktifkan audio panggilan poliklinik ini</div>
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
                    <div class="topbar-sub">Sistem Antrean Poliklinik Rawat Jalan</div>
                </div>
            </div>
            <div class="clock-time" id="clockTime">--:--:--</div>
        </header>

        <!-- Main Content -->
        <main class="main-layout">
            <!-- Left 70% Stage -->
            <div class="giant-stage" id="stageCard">
                <div class="stage-badge-header">
                    <div class="badge-room">
                        <span class="material-icons" style="font-size:2vw;">meeting_room</span>
                        <span id="labelPoli">Memuat Poliklinik...</span>
                    </div>
                    <div class="doctor-banner" id="labelDokter">-</div>
                </div>

                <div class="stage-center">
                    <div class="stage-label">Nomor Antrean Pasien</div>
                    <div class="stage-number" id="stageNumber">-</div>
                    <div class="stage-patient-name" id="stagePatient">Standby Menunggu Panggilan</div>
                </div>

                <div class="stage-status-bar" id="stageStatus">
                    <span class="material-icons">hourglass_empty</span>
                    <span id="stageStatusText">Standby</span>
                </div>
            </div>

            <!-- Right 30% Upcoming List -->
            <div class="side-upcoming">
                <div class="side-header">
                    <span>Antrean Berikutnya</span>
                    <span id="countUpcoming">0 Pasien</span>
                </div>
                <ul class="side-list" id="sideList">
                    <li class="side-empty">Tidak ada antrean menunggu</li>
                </ul>
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
        var currentPoli = "<?php echo $kd_poli; ?>";
        var currentDokter = "<?php echo $kd_dokter; ?>";
        var audioEnabled = false;
        var isPlayingAudio = false;
        var audioQueue = [];
        var lastCallId = -1;
        var isInitialLoad = true;
        window.activeUtterance = null;

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

            var watchdog = setTimeout(finish, 9000);

            var spokenNumber = numberToIndonesianWords(item.no_reg);
            var textToSpeak = "Nomor urut, " + spokenNumber + ". Atas nama pasien, " + item.nm_pasien + ", silakan masuk ke " + item.nm_poli;

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

            var stageCard = document.getElementById('stageCard');
            stageCard.classList.add('calling');

            speakCompleteCall(item, function() {
                setTimeout(function() {
                    stageCard.classList.remove('calling');
                    isPlayingAudio = false;
                    processAudioQueue();
                }, 2000);
            });
        }

        function enqueueCall(item) {
            if (!item || !item.no_reg) return;
            audioQueue.push(item);
            if (audioEnabled) {
                processAudioQueue();
            }
        }

        function pollDoorStatus() {
            if (!currentPoli) return;

            var queryUrl = 'api_antrian_poli_call.php?kd_poli=' + encodeURIComponent(currentPoli);
            if (currentDokter) {
                queryUrl += '&kd_dokter=' + encodeURIComponent(currentDokter);
            }

            fetch(queryUrl)
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data.status !== 'success' || !data.channels || data.channels.length === 0) return;

                    var ch = data.channels[0];
                    document.getElementById('labelPoli').textContent = ch.nm_poli;

                    var act = ch.active_patient;
                    var numEl = document.getElementById('stageNumber');
                    var patEl = document.getElementById('stagePatient');
                    var docEl = document.getElementById('labelDokter');
                    var statEl = document.getElementById('stageStatusText');

                    if (act && act.no_reg) {
                        numEl.textContent = String(act.no_reg).padStart(3, '0');
                        patEl.textContent = act.nm_pasien;
                        docEl.textContent = act.nm_dokter || ch.nm_dokter || '-';
                        statEl.textContent = 'Sedang Diperiksa';

                        var callId = act.call_id ? act.call_id : parseInt(act.no_reg, 10);
                        if (!isInitialLoad && callId !== lastCallId && lastCallId >= 0) {
                            enqueueCall(act);
                        }
                        lastCallId = callId;
                    } else {
                        numEl.textContent = '-';
                        patEl.textContent = 'Standby Menunggu Panggilan';
                        docEl.textContent = '-'; // Standby: show '-' instead of old/cached doctor
                        statEl.textContent = 'Standby';
                    }

                    // Render upcoming list safely with HTML escaping
                    var upListEl = document.getElementById('sideList');
                    var countEl = document.getElementById('countUpcoming');
                    var upcoming = ch.upcoming || [];
                    countEl.textContent = upcoming.length + ' Pasien';

                    if (upcoming.length > 0) {
                        var listHtml = '';
                        for (var u = 0; u < upcoming.length; u++) {
                            listHtml += '<li class="side-item">' +
                                '<span class="side-reg">' + String(upcoming[u].no_reg).padStart(3, '0') + '</span>' +
                                '<span class="side-name">' + escapeHtml(upcoming[u].nm_pasien) + '</span>' +
                                '<span class="side-time">' + escapeHtml(upcoming[u].jam_reg) + '</span>' +
                                '</li>';
                        }
                        upListEl.innerHTML = listHtml;
                    } else {
                        upListEl.innerHTML = '<li class="side-empty">Tidak ada antrean menunggu</li>';
                    }

                    if (isInitialLoad) {
                        lastCallId = (act && act.call_id) ? act.call_id : 0;
                        isInitialLoad = false;
                    }
                })
                .catch(function(e) {
                    console.error('Error polling door display:', e);
                });
        }

        setInterval(pollDoorStatus, 3000);
        pollDoorStatus();
    </script>
</body>

</html>
