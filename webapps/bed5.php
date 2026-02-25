<?php
//fitur update kamar aplicare ini adalah penyempurnaan dari kontribusi Mas Tirta dari RSUK Ciracas Jakarta Timur
session_start();
require_once('conf/conf.php');
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache"); // HTTP/1.0
date_default_timezone_set("Asia/Bangkok");
$tanggal = mktime(date("m"), date("d"), date("Y"));
$jam = date("H:i");
?>
<!doctype html>
<html lang="en">

<head>
    <title>Layar Informasi</title>
    <link rel="icon" href="assets/img/rs.png" type="image/x-icon">
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" />
    <link type="text/css" rel="stylesheet" href="assets/css/materialize.min.css" media="screen,projection" />
    <link rel="stylesheet" href="assets/css/marquee.css" />
    <link rel="stylesheet" href="assets/css/ok.css" />
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700;800&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <style>
        :root {
            --blue-dark: #0d47a1;
            --blue-mid: #1976d2;
            --blue-light: #42a5f5;
            --blue-pale: #e3f2fd;
            --blue-hover: #bbdefb;
            --white: #ffffff;
            --text-dark: #0d2b5e;
            --text-muted: #546e9a;
            --accent: #00b0ff;
            --green: #00c853;
            --red: #f44336;
            --yellow: #ffc107;
            --radius: 14px;
            --shadow: 0 4px 20px rgba(13, 71, 161, 0.13);
        }

        * {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        html,
        body {
            height: 100%;
            overflow: hidden;
            font-family: 'Nunito', sans-serif;
            background: linear-gradient(160deg, #deeeff 0%, #c5e0fb 40%, #e8f4ff 100%);
            color: var(--text-dark);
        }

        /* ── LAYOUT ─────────────────────────────────── */
        .screen {
            display: flex;
            flex-direction: column;
            height: 100vh;
            padding: 10px 14px 0;
            gap: 8px;
        }

        /* ── TOPBAR ─────────────────────────────────── */
        .topbar {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: linear-gradient(90deg, var(--blue-dark), var(--blue-mid));
            border-radius: var(--radius);
            padding: 8px 18px;
            box-shadow: var(--shadow);
            flex-shrink: 0;
        }

        .topbar-brand {
            display: flex;
            align-items: center;
            gap: 12px;
        }

        .topbar-logo {
            width: 54px;
            height: 54px;
            border-radius: 50%;
            border: 3px solid rgba(255, 255, 255, 0.5);
            object-fit: cover;
        }

        .topbar-name {
            color: white;
        }

        .topbar-name .ins {
            font-size: 18px;
            font-weight: 800;
            letter-spacing: 0.5px;
            line-height: 1.2;
        }

        .topbar-name .almt {
            font-size: 11px;
            opacity: 0.85;
            font-weight: 400;
        }

        .topbar-clock {
            text-align: right;
            color: white;
        }

        .clock-time {
            font-size: 28px;
            font-weight: 800;
            letter-spacing: 2px;
            line-height: 1;
        }

        .clock-date {
            font-size: 12px;
            opacity: 0.85;
            margin-top: 2px;
        }

        /* ── DATA AREA ──────────────────────────────── */
        #data {
            flex: 1;
            min-height: 0;
            display: flex;
            width: 100%;
        }

        /* ensure loaded partial fills the space */
        #data>div {
            display: flex;
            gap: 10px;
            width: 100%;
            height: 100%;
        }

        /* ── TICKER FOOTER ──────────────────────────── */
        .ticker-footer {
            background: linear-gradient(90deg, var(--blue-dark), var(--blue-mid));
            border-radius: var(--radius) var(--radius) 0 0;
            color: white;
            padding: 7px 14px;
            display: flex;
            align-items: center;
            gap: 10px;
            font-size: 13px;
            font-weight: 700;
            letter-spacing: 0.5px;
            flex-shrink: 0;
            overflow: hidden;
        }

        .ticker-label {
            white-space: nowrap;
            background: rgba(255, 255, 255, 0.2);
            padding: 3px 10px;
            border-radius: 20px;
            font-size: 12px;
        }

        .ticker-track {
            overflow: hidden;
            flex: 1;
        }

        .ticker-inner {
            display: inline-flex;
            white-space: nowrap;
        }

        .ticker-item {
            color: rgba(255, 255, 255, 0.92);
            font-weight: 600;
        }

        /* ── TABLE CARD ─────────────────────────────── */
        .table-card {
            background: white;
            border-radius: var(--radius);
            box-shadow: var(--shadow);
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }

        .table-card-header {
            background: linear-gradient(90deg, var(--blue-mid), var(--blue-light));
            color: white;
            padding: 8px 14px;
            display: flex;
            align-items: center;
            gap: 8px;
            font-size: 14px;
            font-weight: 800;
            letter-spacing: 0.5px;
            flex-shrink: 0;
        }

        .table-card-header .material-icons {
            font-size: 18px;
        }

        .table-scroll-wrap {
            flex: 1;
            overflow: hidden;
            position: relative;
        }

        /* Auto-scroll container */
        .auto-scroll-inner {
            position: absolute;
            width: 100%;
        }

        .auto-scroll-inner.scrolling {
            animation: vertScroll linear infinite;
        }

        @keyframes vertScroll {
            0% {
                transform: translateY(0);
            }

            100% {
                transform: translateY(var(--scroll-dist));
            }
        }

        /* ── TABLES ─────────────────────────────────── */
        table.rs-table {
            width: 100%;
            border-collapse: collapse;
        }

        table.rs-table thead tr {
            background: var(--blue-pale);
        }

        table.rs-table th {
            padding: 7px 10px;
            font-size: 12px;
            font-weight: 800;
            color: var(--blue-dark);
            text-transform: uppercase;
            letter-spacing: 0.5px;
            border-bottom: 2px solid #c5dcf5;
            white-space: nowrap;
        }

        table.rs-table td {
            padding: 7px 10px;
            font-size: 13px;
            font-weight: 600;
            border-bottom: 1px solid #e8f2fb;
            white-space: nowrap;
        }

        table.rs-table tbody tr:nth-child(even) td {
            background: #f0f7ff;
        }

        table.rs-table tbody tr:hover td {
            background: var(--blue-hover);
        }

        /* ── BED BADGES ─────────────────────────────── */
        .badge-bed {
            display: inline-block;
            min-width: 36px;
            padding: 3px 10px;
            border-radius: 20px;
            font-size: 14px;
            font-weight: 800;
            text-align: center;
        }

        .badge-total {
            background: #e3f2fd;
            color: #1565c0;
            border: 1.5px solid #90caf9;
        }

        .badge-isi {
            background: #ffebee;
            color: #c62828;
            border: 1.5px solid #ef9a9a;
        }

        .badge-kosong {
            background: #e8f5e9;
            color: #2e7d32;
            border: 1.5px solid #a5d6a7;
        }

        /* ── EMPTY STATE ────────────────────────────── */
        .empty-state {
            text-align: center;
            padding: 30px;
            color: var(--text-muted);
            font-size: 13px;
        }

        .empty-state .material-icons {
            font-size: 36px;
            opacity: 0.3;
            display: block;
            margin-bottom: 6px;
        }
    </style>
</head>

<body>
    <div class="screen">

        <!-- TOPBAR -->
        <div class="topbar">
            <?php $setting = mysqli_fetch_array(bukaquery("select nama_instansi,alamat_instansi,kabupaten,propinsi,kontak,email,logo from setting")); ?>
            <div class="topbar-brand">
                <img class="topbar-logo" src="data:image/jpeg;base64,<?php echo base64_encode($setting['logo']); ?>" />
                <div class="topbar-name">
                    <div class="ins"><?php echo $setting["nama_instansi"]; ?></div>
                    <div class="almt"><?php echo $setting["alamat_instansi"] . ", " . $setting["kabupaten"] . ", " . $setting["propinsi"]; ?></div>
                </div>
            </div>
            <div class="topbar-clock">
                <div class="clock-time" id="header-time"></div>
                <div class="clock-date" id="header-date"></div>
            </div>
        </div>

        <!-- DATA (loaded via AJAX) -->
        <div id="data"></div>

        <!-- TICKER FOOTER -->
        <div class="ticker-footer">
            <div class="ticker-label">💰 Tarif Kamar</div>
            <div class="ticker-track">
                <div class="ticker-inner" id="ticker-inner">
                    <?php
                    $sql = "SELECT kelas, trf_kamar FROM kamar WHERE statusdata='1' GROUP BY kelas";
                    $hasil = bukaquery($sql);
                    $items = [];
                    while ($data = mysqli_fetch_array($hasil)) {
                        $items[] = '<span class="ticker-item">🛏 ' . htmlspecialchars($data['kelas']) . ' &nbsp; <b>Rp ' . number_format($data['trf_kamar'], 0, ".", ",") . '</b></span>';
                    }
                    $all = implode('<span style="opacity:0.35; padding:0 28px;">·</span>', $items);
                    echo $all;
                    ?>
                </div>
            </div>
        </div>

    </div>

    <script src="assets/js/jquery-2.1.1.min.js"></script>
    <script src="assets/js/materialize.min.js"></script>
    <script>
        // ── Clock ────────────────────────────────────────
        (function tick() {
            var now = new Date();
            document.getElementById('header-time').textContent = now.toLocaleTimeString('id-ID', {
                hour12: false,
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
            document.getElementById('header-date').textContent = now.toLocaleDateString('id-ID', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
            setTimeout(tick, 1000);
        })();

        // ── Infinite Ticker (no reset glitch) ────────────
        (function initTicker() {
            var track = document.querySelector('.ticker-track');
            var orig = document.querySelector('.ticker-inner');
            if (!track || !orig) return;

            // Build a runner with 3 clones so the window never runs out of content
            var runner = document.createElement('div');
            runner.style.cssText = 'display:inline-flex; white-space:nowrap; will-change:transform; flex-shrink:0;';

            function makeClone() {
                var wrap = document.createElement('div');
                wrap.style.cssText = 'display:inline-flex; align-items:center; flex-shrink:0;';
                var clone = orig.cloneNode(true);
                var sep = document.createElement('span');
                sep.style.cssText = 'opacity:0.35; padding:0 28px; flex-shrink:0;';
                sep.textContent = '·';
                wrap.appendChild(clone);
                wrap.appendChild(sep);
                return wrap;
            }

            runner.appendChild(makeClone());
            runner.appendChild(makeClone());
            runner.appendChild(makeClone());

            track.innerHTML = '';
            track.style.overflow = 'hidden';
            track.appendChild(runner);

            var pos = 0;
            var speed = 0.8; // px per rAF tick (~48px/s @ 60fps)
            var loopW = 0; // measured ONCE after layout

            function step() {
                pos -= speed;
                // while-loop handles any overshoot (slow frame, tab switch, etc.)
                while (loopW > 0 && pos <= -loopW) {
                    pos += loopW;
                }
                runner.style.transform = 'translateX(' + pos + 'px)';
                requestAnimationFrame(step);
            }

            // Measure after fonts + layout settle, then start — never re-measure inside rAF
            setTimeout(function() {
                loopW = runner.scrollWidth / 3; // one third = one clone unit
                requestAnimationFrame(step);
            }, 800);
        })();

        // ── Auto-scroll for tables ────────────────────────
        function setupAutoScroll() {
            document.querySelectorAll('.table-scroll-wrap').forEach(function(wrap) {
                var inner = wrap.querySelector('.auto-scroll-inner');
                if (!inner) return;
                inner.classList.remove('scrolling');
                void inner.offsetWidth;
                if (inner.scrollHeight <= wrap.clientHeight) return;
                var spd = Math.round((inner.scrollHeight / 60) * 1000);
                inner.style.setProperty('--scroll-dist', '-' + inner.scrollHeight + 'px');
                inner.style.animationDuration = spd + 'ms';
                inner.classList.add('scrolling');
            });
        }

        // ── Load data ─────────────────────────────────────
        function loadData() {
            $('#data').load('data_jadwal_kamar.php', function() {
                setupAutoScroll();
            });
        }
        loadData();
        setInterval(loadData, 10000);
    </script>
</body>

</html>