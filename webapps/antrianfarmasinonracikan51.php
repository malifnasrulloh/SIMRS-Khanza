<?php
session_start();
require_once('conf/conf.php');
header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
header("Cache-Control: no-store, no-cache, must-revalidate");
header("Cache-Control: post-check=0, pre-check=0", false);
header("Pragma: no-cache");
date_default_timezone_set("Asia/Bangkok");
?>
<!doctype html>
<html lang="id">

<head>
    <title>Layar Informasi – Antrean Farmasi Non Racikan</title>
    <link rel="icon" href="conf/assets/img/rs.png" type="image/x-icon">
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no" />
    <link href="https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700;800;900&family=Barlow+Condensed:wght@500;600;700;800&display=swap" rel="stylesheet">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">

    <style>
        @import url('https://fonts.googleapis.com/css2?family=Nunito:wght@400;600;700;800;900&family=Barlow+Condensed:wght@500;600;700;800&display=swap');

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
        }

        *,
        *::before,
        *::after {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        html,
        body {
            height: 100%;
            overflow: hidden;
            font-family: 'Nunito', sans-serif;
            background: linear-gradient(150deg, #daeeff 0%, #c2dff8 50%, #e6f4ff 100%);
            color: var(--td);
        }

        .screen {
            display: flex;
            flex-direction: column;
            height: 100vh;
            padding: 0.7vw 0.8vw 0;
            gap: 0.5vw;
        }

        .topbar {
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: linear-gradient(90deg, var(--bd) 0%, var(--bm) 55%, var(--bl) 100%);
            border-radius: 0.8vw;
            padding: 0.7vw 1.6vw;
            box-shadow: 0 0.2vw 1.2vw rgba(13, 71, 161, 0.18);
        }

        .topbar-brand {
            display: flex;
            align-items: center;
            gap: 1vw;
        }

        .topbar-logo {
            width: 4.5vw;
            height: 4.5vw;
            border-radius: 50%;
            border: 0.2vw solid rgba(255, 255, 255, 0.55);
            background: #fff;
            padding: 0.25vw;
            object-fit: contain;
        }

        .hospital-name {
            color: #fff;
            font-size: 1.6vw;
            font-weight: 900;
            line-height: 1.1;
            letter-spacing: 0.01em;
        }

        .hospital-addr {
            color: rgba(255, 255, 255, 0.75);
            font-size: 0.8vw;
            font-weight: 400;
            margin-top: 0.15vw;
        }

        .topbar-right {
            display: flex;
            align-items: center;
            gap: 1.5vw;
        }

        .live-pill {
            display: flex;
            align-items: center;
            gap: 0.4vw;
            background: rgba(0, 200, 83, 0.22);
            border: 0.1vw solid rgba(0, 200, 83, 0.55);
            border-radius: 2vw;
            padding: 0.25vw 0.9vw;
            font-size: 0.75vw;
            font-weight: 800;
            color: #00e676;
            letter-spacing: 0.1em;
            text-transform: uppercase;
        }

        .live-dot {
            width: 0.5vw;
            height: 0.5vw;
            border-radius: 50%;
            background: #00e676;
            animation: lp 1.4s ease-in-out infinite;
        }

        @keyframes lp {
            0%, 100% { opacity: 1; }
            50% { opacity: 0.25; }
        }

        .clock-wrap {
            text-align: right;
        }

        .clock-time {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 3.2vw;
            font-weight: 800;
            color: #fff;
            letter-spacing: 0.04em;
            line-height: 1;
        }

        .clock-date {
            color: rgba(255, 255, 255, 0.72);
            font-size: 0.78vw;
            margin-top: 0.1vw;
            font-weight: 600;
        }

        #data {
            flex: 1;
            min-height: 0;
            display: flex;
            width: 100%;
        }

        .ticker-footer {
            flex-shrink: 0;
            background: linear-gradient(90deg, var(--bd), var(--bm) 60%, var(--bl));
            border-radius: 0.8vw 0.8vw 0 0;
            padding: 0.5vw 1.2vw;
            display: flex;
            align-items: center;
            gap: 0.9vw;
            overflow: hidden;
        }

        .ticker-label {
            white-space: nowrap;
            background: rgba(255, 255, 255, 0.2);
            border-radius: 2vw;
            padding: 0.2vw 0.8vw;
            font-size: 0.78vw;
            font-weight: 800;
            color: #fff;
            letter-spacing: 0.07em;
            text-transform: uppercase;
        }

        .ticker-track {
            overflow: hidden;
            flex: 1;
        }

        .ticker-inner {
            display: inline-flex;
            white-space: nowrap;
            align-items: center;
        }

        .ticker-item {
            font-size: 0.88vw;
            font-weight: 700;
            color: rgba(255, 255, 255, 0.92);
            padding: 0 0.6vw;
        }

        .ticker-item b {
            color: #fff;
        }

        .card {
            background: #fff;
            border-radius: 0.8vw;
            box-shadow: 0 0.15vw 0.8vw rgba(13, 71, 161, 0.11);
            display: flex;
            flex-direction: column;
            overflow: hidden;
        }

        .card-head {
            flex-shrink: 0;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0.6vw 1.1vw;
            background: var(--bp);
            border-bottom: 0.12vw solid #c5dcf5;
        }

        .card-head-left {
            display: flex;
            align-items: center;
            gap: 0.6vw;
        }

        .card-icon {
            background: var(--bm);
            color: #fff;
            border-radius: 0.4vw;
            width: 2.2vw;
            height: 2.2vw;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .card-icon.call {
            background: var(--grn);
        }

        .card-icon.valid {
            background: var(--yel);
        }

        .card-icon .material-icons {
            font-size: 1.2vw !important;
        }

        .card-title {
            font-size: 1.05vw;
            font-weight: 900;
            color: var(--bd);
            text-transform: uppercase;
            letter-spacing: 0.07em;
        }

        .card-sub {
            font-size: 0.75vw;
            color: var(--tm);
            margin-top: 0.1vw;
        }

        .pill {
            background: var(--bm);
            color: #fff;
            border-radius: 2vw;
            padding: 0.2vw 0.8vw;
            font-size: 0.75vw;
            font-weight: 800;
        }

        .table-wrap {
            flex: 1;
            overflow: hidden;
            position: relative;
        }

        .scroll-inner {
            position: absolute;
            width: 100%;
        }

        .scroll-inner.scrolling {
            animation: vScroll linear infinite;
        }

        @keyframes vScroll {
            0% { transform: translateY(0); }
            100% { transform: translateY(var(--sd)); }
        }

        table.t {
            width: 100%;
            border-collapse: collapse;
        }

        table.t thead {
            background: #f0f8ff;
            position: sticky;
            top: 0;
            z-index: 2;
        }

        table.t thead th {
            padding: 0.6vw 0.9vw;
            font-size: 0.85vw;
            font-weight: 900;
            color: var(--bm);
            text-transform: uppercase;
            letter-spacing: 0.08em;
            border-bottom: 0.15vw solid #c5dcf5;
            white-space: nowrap;
        }

        table.t tbody tr {
            border-bottom: 0.06vw solid #dceefa;
        }

        table.t tbody tr:nth-child(even) td {
            background: #f5fafe;
        }

        table.t tbody tr:hover td {
            background: var(--bh);
        }

        table.t td {
            padding: 0.8vw 0.9vw;
            font-size: 1.25vw;
            font-weight: 700;
            color: var(--td);
            white-space: nowrap;
        }

        table.t td.med {
            font-size: 1.1vw;
            font-weight: 600;
        }

        table.t td.time {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 1.5vw;
            font-weight: 700;
            color: var(--bm);
            text-align: center;
            letter-spacing: 0.04em;
        }

        table.t td.c {
            text-align: center;
        }

        table.t td.num {
            font-size: 0.95vw;
            color: #8fa7c9;
            text-align: center;
            width: 2vw;
        }

        .badge {
            display: inline-block;
            padding: 0.25vw 0.75vw;
            border-radius: 0.4vw;
            font-size: 0.85vw;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.05em;
            white-space: nowrap;
        }

        .b-tunggu {
            background: #fff8e1;
            color: #e65100;
            border: 0.06vw solid #ffe082;
        }

        .b-jalan {
            background: #e8f5e9;
            color: #1b5e20;
            border: 0.06vw solid #a5d6a7;
        }

        .b-selesai {
            background: #e3f2fd;
            color: #0d47a1;
            border: 0.06vw solid #90caf9;
        }

        .b-nonracik {
            background: #e0f2f1;
            color: #004d40;
            border: 0.06vw solid #80cbc4;
        }

        .empty {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            padding: 4vw;
            gap: 0.6vw;
            color: var(--tm);
        }

        .empty .material-icons {
            font-size: 3.5vw !important;
            opacity: 0.2;
        }

        .empty p {
            font-size: 1vw;
            font-weight: 600;
        }

        /* Pharmacy Split Layout */
        .pharmacy-layout {
            display: flex;
            width: 100%;
            height: 100%;
            gap: 0.6vw;
        }

        .pharmacy-left {
            width: 35%;
            display: flex;
            flex-direction: column;
            gap: 0.5vw;
        }

        .pharmacy-right {
            width: 65%;
            display: flex;
            flex-direction: column;
        }

        .hero-call-card {
            background: #fff;
            border-radius: 0.8vw;
            box-shadow: 0 0.15vw 0.8vw rgba(13, 71, 161, 0.11);
            overflow: hidden;
            display: flex;
            flex-direction: column;
            border: 0.12vw solid #c5dcf5;
            position: relative;
        }

        .hero-call-card.calling {
            animation: pulseCard 1.2s ease-in-out infinite;
            border-color: var(--grn);
        }

        .hero-call-card.calling-valid {
            animation: pulseCardValid 1.2s ease-in-out infinite;
            border-color: var(--yel);
        }

        @keyframes pulseCard {
            0%, 100% { box-shadow: 0 0.15vw 0.8vw rgba(13, 71, 161, 0.11); }
            50% { box-shadow: 0 0 1.5vw rgba(0, 200, 83, 0.6); }
        }

        @keyframes pulseCardValid {
            0%, 100% { box-shadow: 0 0.15vw 0.8vw rgba(13, 71, 161, 0.11); }
            50% { box-shadow: 0 0 1.5vw rgba(249, 168, 37, 0.6); }
        }

        .hero-call-body {
            padding: 0.8vw 1.2vw;
            display: flex;
            flex-direction: column;
            align-items: center;
            text-align: center;
            justify-content: center;
        }

        .hero-call-num {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 3.8vw;
            font-weight: 800;
            color: var(--bd);
            line-height: 1;
            letter-spacing: 0.03em;
        }

        .hero-call-num.grn {
            color: #00a844;
        }

        .hero-call-num.yel {
            color: #e65100;
        }

        .hero-call-patient {
            font-size: 1.55vw;
            font-weight: 800;
            color: var(--td);
            margin-top: 0.3vw;
            line-height: 1.2;
            word-break: break-word;
        }

        .hero-call-meta {
            font-size: 0.9vw;
            color: var(--tm);
            margin-top: 0.2vw;
            font-weight: 600;
        }

        .kpi-grid {
            display: grid;
            grid-template-columns: repeat(2, 1fr);
            gap: 0.5vw;
            flex: 1;
        }

        .kpi-box {
            background: #fff;
            border-radius: 0.7vw;
            border-top: 0.35vw solid var(--bm);
            padding: 0.6vw 0.8vw;
            box-shadow: 0 0.1vw 0.6vw rgba(13, 71, 161, 0.09);
            display: flex;
            flex-direction: column;
            justify-content: center;
        }

        .kpi-box.yel {
            border-top-color: var(--yel);
        }

        .kpi-box.bl {
            border-top-color: var(--bl);
        }

        .kpi-box.grn {
            border-top-color: var(--grn);
        }

        .kpi-num {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 2.6vw;
            font-weight: 800;
            line-height: 1;
            color: var(--bd);
        }

        .kpi-box.yel .kpi-num { color: #e65100; }
        .kpi-box.bl .kpi-num { color: var(--bm); }
        .kpi-box.grn .kpi-num { color: var(--grn); }

        .kpi-lbl {
            font-size: 0.8vw;
            font-weight: 800;
            text-transform: uppercase;
            letter-spacing: 0.07em;
            color: var(--tm);
            margin-top: 0.2vw;
        }
    </style>
</head>

<body>
    <div class="screen">
        <div class="topbar">
            <?php $s = mysqli_fetch_array(bukaquery("select nama_instansi,alamat_instansi,kabupaten,propinsi,logo from setting")); ?>
            <div class="topbar-brand">
                <img class="topbar-logo" src="data:image/jpeg;base64,<?php echo base64_encode($s['logo']); ?>">
                <div>
                    <div class="hospital-name"><?php echo $s['nama_instansi']; ?></div>
                    <div class="hospital-addr"><?php echo $s['alamat_instansi'] . ', ' . $s['kabupaten'] . ', ' . $s['propinsi']; ?></div>
                </div>
            </div>
            <div class="topbar-right">
                <div class="live-pill">
                    <div class="live-dot"></div>LIVE
                </div>
                <div class="clock-wrap">
                    <div class="clock-time" id="ct"></div>
                    <div class="clock-date" id="cd"></div>
                </div>
            </div>
        </div>

        <div id="data"></div>

        <div class="ticker-footer">
            <div class="ticker-label">&#128138; Informasi Pelayanan Farmasi</div>
            <div class="ticker-track">
                <div class="ticker-inner">
                    <span class="ticker-item">Pastikan mencocokkan <b>Nomor Resep & Nama Pasien</b> saat penyerahan obat di loket apotek</span>
                    <span style="opacity:0.4;padding:0 2vw;color:#fff;">◆</span>
                    <span class="ticker-item">Estimasi waktu pelayanan <b>Obat Non-Racikan: 15 - 30 Menit</b></span>
                    <span style="opacity:0.4;padding:0 2vw;color:#fff;">◆</span>
                    <span class="ticker-item">Konsultasikan aturan pakai dan penyimpanan obat kepada <b>Apoteker / Petugas Farmasi</b></span>
                    <span style="opacity:0.4;padding:0 2vw;color:#fff;">◆</span>
                    <span class="ticker-item">Terima kasih atas kesabaran Anda menunggu antrean pelayanan farmasi</span>
                </div>
            </div>
        </div>
    </div>

    <script src="assets/js/jquery-2.1.1.min.js"></script>
    <script src="assets/js/materialize.min.js"></script>
    <script>
        (function tick() {
            var now = new Date();
            var te = document.getElementById('ct'),
                de = document.getElementById('cd');
            if (te) te.textContent = now.toLocaleTimeString('id-ID', {
                hour12: false,
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit'
            });
            if (de) de.textContent = now.toLocaleDateString('id-ID', {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
            setTimeout(tick, 1000);
        })();

        (function() {
            var track = document.querySelector('.ticker-track');
            var orig = document.querySelector('.ticker-inner');
            if (!track || !orig) return;
            var runner = document.createElement('div');
            runner.style.cssText = 'display:inline-flex;white-space:nowrap;will-change:transform;flex-shrink:0;';

            function mkClone() {
                var w = document.createElement('div');
                w.style.cssText = 'display:inline-flex;align-items:center;flex-shrink:0;';
                w.appendChild(orig.cloneNode(true));
                var s = document.createElement('span');
                s.style.cssText = 'opacity:0.4;padding:0 2vw;color:#fff;font-size:0.7vw;';
                s.textContent = '◆';
                w.appendChild(s);
                return w;
            }
            runner.appendChild(mkClone());
            runner.appendChild(mkClone());
            runner.appendChild(mkClone());
            track.innerHTML = '';
            track.style.overflow = 'hidden';
            track.appendChild(runner);
            var pos = 0,
                lw = 0;
            var vwPx = function() {
                return document.documentElement.clientWidth / 100;
            };

            function step() {
                pos -= 0.06 * vwPx();
                if (!lw) lw = runner.scrollWidth / 3;
                while (lw > 0 && pos <= -lw) pos += lw;
                runner.style.transform = 'translateX(' + pos + 'px)';
                requestAnimationFrame(step);
            }
            setTimeout(function() {
                lw = runner.scrollWidth / 3;
                requestAnimationFrame(step);
            }, 900);
        })();

        function setupScroll() {
            document.querySelectorAll('.table-wrap').forEach(function(w) {
                var i = w.querySelector('.scroll-inner');
                if (!i) return;
                i.classList.remove('scrolling');
                void i.offsetWidth;
                if (i.scrollHeight <= w.clientHeight + 2) return;
                var spd = Math.round((i.scrollHeight / 50) * 1000);
                i.style.setProperty('--sd', '-' + i.scrollHeight + 'px');
                i.style.animationDuration = spd + 'ms';
                i.classList.add('scrolling');
            });
        }

        function load() {
            $('#data').load('data_antrianfarmasinonracikan51.php', function() {
                setupScroll();
            });
        }
        load();
        setInterval(load, 4000);
    </script>
</body>

</html>