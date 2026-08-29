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
?>
<!doctype html>
<html lang="id">

<head>
    <title>Web Caller – Loket Pendaftaran (51-Series)</title>
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
            --card-bg: #ffffff;
        }

        *, *::before, *::after {
            box-sizing: border-box;
            margin: 0;
            padding: 0;
        }

        body {
            font-family: 'Nunito', sans-serif;
            background: linear-gradient(135deg, #f0f4f8 0%, #d9e2ec 100%);
            color: var(--td);
            min-height: 100vh;
            display: flex;
            flex-direction: column;
            padding: 1.5rem;
        }

        .container {
            max-width: 900px;
            margin: 0 auto;
            width: 100%;
        }

        .caller-card {
            background: #fff;
            border-radius: 1.2rem;
            box-shadow: 0 0.8rem 2rem rgba(13, 71, 161, 0.12);
            border: 1px solid rgba(13, 71, 161, 0.08);
            overflow: hidden;
        }

        .caller-header {
            background: linear-gradient(90deg, var(--bd), var(--bm));
            padding: 1.2rem 1.8rem;
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: space-between;
        }

        .header-left {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .header-logo {
            width: 3.5rem;
            height: 3.5rem;
            border-radius: 50%;
            background: #fff;
            padding: 0.2rem;
            object-fit: contain;
            border: 2px solid rgba(255, 255, 255, 0.8);
        }

        .header-title h1 {
            font-size: 1.3rem;
            font-weight: 800;
            line-height: 1.2;
        }

        .header-title p {
            font-size: 0.85rem;
            opacity: 0.9;
        }

        .caller-body {
            padding: 1.8rem;
            display: flex;
            flex-direction: column;
            gap: 1.5rem;
        }

        /* Loket Selector */
        .loket-select-row {
            display: flex;
            align-items: center;
            justify-content: space-between;
            background: var(--bp);
            padding: 1rem 1.5rem;
            border-radius: 0.8rem;
        }

        .select-group {
            display: flex;
            align-items: center;
            gap: 0.8rem;
        }

        .select-group label {
            font-weight: 800;
            font-size: 1.1rem;
            color: var(--bd);
        }

        select.form-control {
            font-family: 'Nunito', sans-serif;
            font-size: 1.1rem;
            font-weight: 800;
            padding: 0.5rem 1rem;
            border-radius: 0.5rem;
            border: 2px solid var(--bl);
            background: #fff;
            color: var(--bd);
            cursor: pointer;
            outline: none;
        }

        /* Status Display */
        .status-box {
            text-align: center;
            background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
            border: 2px dashed #cbd5e1;
            border-radius: 1rem;
            padding: 1.5rem;
            transition: all 0.3s ease;
        }

        .status-box.calling-active {
            background: #f0fdf4;
            border-color: var(--grn);
            box-shadow: 0 0 1.5rem rgba(0, 200, 83, 0.2);
        }

        .status-label {
            font-size: 1rem;
            font-weight: 800;
            color: var(--tm);
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        .status-num {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 6rem;
            font-weight: 900;
            line-height: 1;
            color: var(--bd);
            margin: 0.5rem 0;
        }

        .status-sub {
            font-size: 1rem;
            font-weight: 700;
            color: var(--tm);
        }

        /* Action Buttons Grid */
        .btn-grid {
            display: grid;
            grid-template-columns: 2fr 1.5fr;
            gap: 1rem;
        }

        button.btn {
            font-family: 'Nunito', sans-serif;
            border: none;
            border-radius: 0.8rem;
            padding: 1rem 1.5rem;
            font-size: 1.2rem;
            font-weight: 800;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.6rem;
            transition: all 0.2s ease;
            box-shadow: 0 0.3rem 0.8rem rgba(0, 0, 0, 0.1);
        }

        button.btn:active {
            transform: translateY(2px);
            box-shadow: 0 0.1rem 0.3rem rgba(0, 0, 0, 0.15);
        }

        button.btn-next {
            background: linear-gradient(135deg, #059669, #10b981);
            color: #fff;
        }

        button.btn-recall {
            background: linear-gradient(135deg, #2563eb, #3b82f6);
            color: #fff;
        }

        /* Manual Input & Reset */
        .manual-row {
            display: flex;
            gap: 0.8rem;
            align-items: center;
            background: #f8fafc;
            padding: 1rem;
            border-radius: 0.8rem;
            border: 1px solid #e2e8f0;
        }

        .manual-row input {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 1.5rem;
            font-weight: 800;
            padding: 0.4rem 0.8rem;
            border-radius: 0.5rem;
            border: 2px solid #cbd5e1;
            width: 8rem;
            text-align: center;
            color: var(--bd);
            outline: none;
        }

        button.btn-manual {
            background: #475569;
            color: #fff;
            font-size: 1rem;
            padding: 0.6rem 1.2rem;
        }

        button.btn-reset {
            background: #ef4444;
            color: #fff;
            font-size: 1rem;
            padding: 0.6rem 1.2rem;
            margin-left: auto;
        }

        .links-row {
            display: flex;
            justify-content: center;
            gap: 1.5rem;
            margin-top: 1rem;
            font-size: 0.95rem;
        }

        .links-row a {
            color: var(--bm);
            text-decoration: none;
            font-weight: 700;
            display: flex;
            align-items: center;
            gap: 0.3rem;
        }

        .links-row a:hover {
            text-decoration: underline;
        }

        /* Feedback Alert Banner */
        #feedbackAlert {
            display: none;
            padding: 0.8rem 1.2rem;
            border-radius: 0.6rem;
            font-weight: 800;
            font-size: 0.95rem;
            text-align: center;
        }
        #feedbackAlert.success {
            display: block;
            background: #dcfce7;
            color: #065f46;
            border: 1px solid #86efac;
        }
        #feedbackAlert.error {
            display: block;
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fca5a5;
        }
    </style>
</head>

<body>
    <div class="container">
        <div class="caller-card">
            <div class="caller-header">
                <div class="header-left">
                    <?php if (!empty($setting['logo'])) { ?>
                        <img class="header-logo" src="data:image/jpeg;base64,<?php echo base64_encode($setting['logo']); ?>" alt="Logo">
                    <?php } ?>
                    <div class="header-title">
                        <h1>Web Caller Loket Pendaftaran</h1>
                        <p><?php echo htmlspecialchars($setting['nama_instansi']); ?></p>
                    </div>
                </div>
            </div>

            <div class="caller-body">
                <!-- Feedback Alert -->
                <div id="feedbackAlert"></div>

                <!-- Select Loket -->
                <div class="loket-select-row">
                    <div class="select-group">
                        <label for="selectLoket">PILIH LOKET ANDA:</label>
                        <select id="selectLoket" class="form-control" onchange="changeLoket()">
                            <option value="1">LOKET 1</option>
                            <option value="2">LOKET 2</option>
                            <option value="3">LOKET 3</option>
                            <option value="4">LOKET 4</option>
                            <option value="5">LOKET 5</option>
                            <option value="6">LOKET 6</option>
                        </select>
                    </div>
                    <div id="statTotalCetak" style="font-weight:800; color:var(--tm);">Memuat antrean...</div>
                </div>

                <!-- Current Call Display -->
                <div class="status-box" id="statusBox">
                    <div class="status-label">Nomor Antrean Sedang Dilayani</div>
                    <div class="status-num" id="currentNumDisplay">-</div>
                    <div class="status-sub" id="currentLoketLabel">Loket 1</div>
                </div>

                <!-- Action Buttons -->
                <div class="btn-grid">
                    <button class="btn btn-next" onclick="callNext()">
                        <span class="material-icons" style="font-size:1.8rem;">skip_next</span>
                        <span>Panggil Berikutnya</span>
                    </button>
                    <button class="btn btn-recall" onclick="callRecall()">
                        <span class="material-icons" style="font-size:1.8rem;">replay</span>
                        <span>Panggil Ulang</span>
                    </button>
                </div>

                <!-- Manual Call & Reset -->
                <div class="manual-row">
                    <span style="font-weight:700;">Panggil Manual:</span>
                    <input type="number" id="manualNum" min="1" max="999" placeholder="Nomor">
                    <button class="btn btn-manual" onclick="callManual()">Panggil</button>
                    <button class="btn btn-reset" onclick="resetLoket()">
                        <span class="material-icons" style="font-size:1.2rem;">cancel</span>
                        <span>Clear / Selesai</span>
                    </button>
                </div>

                <!-- Links to Displays -->
                <div class="links-row">
                    <a href="antrianloket51.php" target="_blank">
                        <span class="material-icons">tv</span>
                        <span>Buka Display Multi-Loket</span>
                    </a>
                    <a href="antrianloketperloket51.php?loket=1" target="_blank" id="linkPerLoket">
                        <span class="material-icons">desktop_windows</span>
                        <span>Buka Display Monitor Loket Ini</span>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <script>
        var currentLoket = 1;

        function showFeedback(msg, isError) {
            var el = document.getElementById('feedbackAlert');
            el.className = isError ? 'error' : 'success';
            el.textContent = msg;
            setTimeout(function() {
                el.style.display = 'none';
                el.className = '';
            }, 3500);
        }

        function changeLoket() {
            currentLoket = parseInt(document.getElementById('selectLoket').value, 10);
            document.getElementById('currentLoketLabel').textContent = 'Loket ' + currentLoket;
            document.getElementById('linkPerLoket').href = 'antrianloketperloket51.php?loket=' + currentLoket;
            fetchStatus();
        }

        function fetchStatus() {
            fetch('api_antrian_loket_call.php?loket=' + currentLoket)
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data.status !== 'success') return;

                    var activeNum = (data.lokets && data.lokets[currentLoket]) ? data.lokets[currentLoket] : 0;
                    var box = document.getElementById('statusBox');
                    var numDisplay = document.getElementById('currentNumDisplay');

                    if (activeNum > 0) {
                        numDisplay.textContent = String(activeNum).padStart(3, '0');
                        box.classList.add('calling-active');
                    } else {
                        numDisplay.textContent = '-';
                        box.classList.remove('calling-active');
                    }
                    document.getElementById('statTotalCetak').textContent = data.total_cetak + ' Tiket Dicetak Hari Ini';
                })
                .catch(function(err) {
                    console.error('Fetch error:', err);
                });
        }

        function callNext() {
            var fd = new FormData();
            fd.append('loket', currentLoket);
            fetch('api_antrian_loket_call.php?action=next', {
                method: 'POST',
                body: fd
            })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                showFeedback('Nomor ' + String(res.antrian).padStart(3, '0') + ' berhasil dipanggil ke Loket ' + currentLoket, false);
                fetchStatus();
            })
            .catch(function(err) {
                showFeedback('Gagal memanggil antrean berikutnya: ' + err, true);
            });
        }

        function callRecall() {
            var fd = new FormData();
            fd.append('loket', currentLoket);
            fetch('api_antrian_loket_call.php?action=recall', {
                method: 'POST',
                body: fd
            })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                showFeedback('Panggilan ulang nomor ' + String(res.antrian).padStart(3, '0') + ' dikirim ke display', false);
                fetchStatus();
            })
            .catch(function(err) {
                showFeedback('Gagal memanggil ulang antrean: ' + err, true);
            });
        }

        function callManual() {
            var num = parseInt(document.getElementById('manualNum').value, 10);
            if (isNaN(num) || num <= 0) {
                alert('Silakan masukkan nomor antrean yang valid');
                return;
            }
            var fd = new FormData();
            fd.append('loket', currentLoket);
            fd.append('antrian', num);
            fetch('api_antrian_loket_call.php?action=call_manual', {
                method: 'POST',
                body: fd
            })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                document.getElementById('manualNum').value = '';
                showFeedback('Nomor ' + String(res.antrian).padStart(3, '0') + ' berhasil dipanggil manual', false);
                fetchStatus();
            })
            .catch(function(err) {
                showFeedback('Gagal memanggil nomor manual: ' + err, true);
            });
        }

        function resetLoket() {
            if (!confirm('Kosongkan antrean aktif untuk Loket ' + currentLoket + '?')) return;
            var fd = new FormData();
            fd.append('loket', currentLoket);
            fetch('api_antrian_loket_call.php?action=reset', {
                method: 'POST',
                body: fd
            })
            .then(function(r) { return r.json(); })
            .then(function() {
                showFeedback('Loket ' + currentLoket + ' berhasil dikosongkan/standby', false);
                fetchStatus();
            })
            .catch(function(err) {
                showFeedback('Gagal reset loket: ' + err, true);
            });
        }

        setInterval(fetchStatus, 3000);
        fetchStatus();
    </script>
</body>

</html>
