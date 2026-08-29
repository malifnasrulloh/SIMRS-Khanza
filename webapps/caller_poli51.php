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
    <title>Web Caller – Poliklinik</title>
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
            max-width: 1060px;
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

        /* Selectors Row */
        .selector-box {
            background: var(--bp);
            padding: 1.2rem;
            border-radius: 0.8rem;
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1rem;
        }

        .select-item {
            display: flex;
            flex-direction: column;
            gap: 0.4rem;
        }

        .select-item label {
            font-weight: 800;
            font-size: 0.95rem;
            color: var(--bd);
            text-transform: uppercase;
        }

        select.form-control {
            font-family: 'Nunito', sans-serif;
            font-size: 1rem;
            font-weight: 700;
            padding: 0.6rem 0.8rem;
            border-radius: 0.5rem;
            border: 2px solid var(--bl);
            background: #fff;
            color: var(--bd);
            cursor: pointer;
            outline: none;
        }

        /* Current Patient Card */
        .active-stage {
            background: linear-gradient(180deg, #f8fafc 0%, #f1f5f9 100%);
            border: 2px dashed #cbd5e1;
            border-radius: 1rem;
            padding: 1.5rem;
            text-align: center;
            transition: all 0.3s ease;
        }

        .active-stage.calling-active {
            background: #f0fdf4;
            border-color: var(--grn);
            box-shadow: 0 0 1.5rem rgba(0, 200, 83, 0.2);
        }

        .stage-title {
            font-size: 0.95rem;
            font-weight: 800;
            color: var(--tm);
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }

        .stage-reg-num {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 5rem;
            font-weight: 900;
            line-height: 1;
            color: var(--bd);
            margin: 0.3rem 0;
        }

        .stage-patient-fullname {
            font-size: 1.6rem;
            font-weight: 900;
            color: var(--td);
        }

        .stage-patient-sub {
            font-size: 0.95rem;
            font-weight: 700;
            color: var(--tm);
            margin-top: 0.2rem;
        }

        /* Action Buttons Grid */
        .btn-grid {
            display: grid;
            grid-template-columns: 2fr 1.2fr 1fr;
            gap: 0.8rem;
        }

        button.btn {
            font-family: 'Nunito', sans-serif;
            border: none;
            border-radius: 0.8rem;
            padding: 0.9rem 1.2rem;
            font-size: 1.1rem;
            font-weight: 800;
            cursor: pointer;
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.5rem;
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

        button.btn-reset {
            background: linear-gradient(135deg, #ef4444, #dc2626);
            color: #fff;
        }

        /* Upcoming List Table Panel */
        .table-panel {
            background: #f8fafc;
            border: 1px solid #e2e8f0;
            border-radius: 0.8rem;
            padding: 1.2rem;
        }

        .table-header {
            display: flex;
            align-items: center;
            justify-content: space-between;
            margin-bottom: 0.8rem;
            gap: 1rem;
            flex-wrap: wrap;
        }

        .table-header-left {
            display: flex;
            align-items: center;
            gap: 0.8rem;
        }

        .table-header-left h3 {
            font-size: 1.15rem;
            font-weight: 800;
            color: var(--bd);
        }

        .badge-count {
            background: var(--bp);
            color: var(--bm);
            font-size: 0.85rem;
            font-weight: 800;
            padding: 0.2rem 0.6rem;
            border-radius: 1rem;
        }

        /* Search Input */
        .table-search-box {
            position: relative;
            display: flex;
            align-items: center;
            flex: 1;
            max-width: 380px;
        }

        .table-search-box .material-icons {
            position: absolute;
            left: 0.7rem;
            color: var(--tm);
            font-size: 1.2rem;
            pointer-events: none;
        }

        .table-search-input {
            width: 100%;
            padding: 0.5rem 0.8rem 0.5rem 2.3rem;
            border: 2px solid #cbd5e1;
            border-radius: 0.5rem;
            font-family: 'Nunito', sans-serif;
            font-size: 0.95rem;
            font-weight: 700;
            color: var(--td);
            outline: none;
            background: #fff;
            transition: all 0.2s ease;
        }

        .table-search-input:focus {
            border-color: var(--bm);
            box-shadow: 0 0 0.5rem rgba(25, 118, 210, 0.15);
        }

        .table-scroll-container {
            max-height: 420px;
            overflow-y: auto;
            border-radius: 0.5rem;
            border: 1px solid #e2e8f0;
            background: #fff;
        }

        table.queue-table {
            width: 100%;
            border-collapse: collapse;
            font-size: 0.92rem;
        }

        table.queue-table thead {
            position: sticky;
            top: 0;
            z-index: 10;
        }

        table.queue-table th {
            background: var(--bp);
            color: var(--bd);
            font-weight: 800;
            text-align: left;
            padding: 0.7rem 0.8rem;
            border-bottom: 2px solid var(--bl);
            white-space: nowrap;
        }

        table.queue-table td {
            padding: 0.6rem 0.8rem;
            border-bottom: 1px solid #f1f5f9;
            font-weight: 700;
            vertical-align: middle;
        }

        table.queue-table tbody tr:hover td {
            background: #f8fafc;
        }

        .td-reg {
            font-family: 'Barlow Condensed', sans-serif;
            font-size: 1.3rem;
            font-weight: 800;
            color: var(--bd);
        }

        .td-time {
            color: var(--tm);
            font-size: 0.85rem;
            font-weight: 800;
        }

        .td-patient {
            color: var(--td);
            font-weight: 800;
        }

        .td-doctor {
            color: var(--bm);
            font-size: 0.9rem;
            font-weight: 700;
        }

        .btn-call-inline {
            background: var(--bm);
            color: #fff;
            border: none;
            border-radius: 0.4rem;
            padding: 0.35rem 0.75rem;
            font-size: 0.85rem;
            font-weight: 800;
            cursor: pointer;
            display: inline-flex;
            align-items: center;
            gap: 0.3rem;
            transition: all 0.2s ease;
        }

        .btn-call-inline:hover {
            background: var(--bd);
            transform: scale(1.03);
        }

        .links-row {
            display: flex;
            justify-content: center;
            gap: 1.5rem;
            margin-top: 0.5rem;
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
                        <h1>Web Caller Poliklinik</h1>
                        <p><?php echo htmlspecialchars($setting['nama_instansi']); ?></p>
                    </div>
                </div>
            </div>

            <div class="caller-body">
                <!-- Feedback Banner -->
                <div id="feedbackAlert"></div>

                <!-- Selectors Box -->
                <div class="selector-box">
                    <div class="select-item">
                        <label for="selectPoli">Pilih Poliklinik:</label>
                        <select id="selectPoli" class="form-control" onchange="onPoliChange()">
                            <option value="">Memuat Poliklinik...</option>
                        </select>
                    </div>
                    <div class="select-item">
                        <label for="selectDokter">Pilih Dokter:</label>
                        <select id="selectDokter" class="form-control" onchange="fetchClinicStatus()">
                            <option value="">Memuat Dokter...</option>
                        </select>
                    </div>
                </div>

                <!-- Active Patient Stage -->
                <div class="active-stage" id="activeStage">
                    <div class="stage-title">Pasien Sedang Dilayani di Ruang Poli</div>
                    <div class="stage-reg-num" id="activeReg">-</div>
                    <div class="stage-patient-fullname" id="activeName">Belum Ada Pasien Dipanggil</div>
                    <div class="stage-patient-sub" id="activeSub">-</div>
                </div>

                <!-- Action Buttons -->
                <div class="btn-grid">
                    <button class="btn btn-next" onclick="callNext()">
                        <span class="material-icons" style="font-size:1.6rem;">skip_next</span>
                        <span>Panggil Berikutnya</span>
                    </button>
                    <button class="btn btn-recall" onclick="callRecall()">
                        <span class="material-icons" style="font-size:1.6rem;">replay</span>
                        <span>Panggil Ulang</span>
                    </button>
                    <button class="btn btn-reset" onclick="resetClinicCall()">
                        <span class="material-icons" style="font-size:1.6rem;">done_all</span>
                        <span>Selesai / Clear</span>
                    </button>
                </div>

                <!-- Upcoming List Table (6 Columns + Real-Time Search) -->
                <div class="table-panel">
                    <div class="table-header">
                        <div class="table-header-left">
                            <h3>Daftar Antrean Pasien Menunggu</h3>
                            <span class="badge-count" id="statTotalWait">0 Pasien</span>
                        </div>
                        <div class="table-search-box">
                            <span class="material-icons">search</span>
                            <input type="text" id="queueSearchInput" class="table-search-input" placeholder="Cari nama pasien / no. rawat / no. reg / dokter..." oninput="filterQueueTable()">
                        </div>
                    </div>
                    <div class="table-scroll-container">
                        <table class="queue-table">
                            <thead>
                                <tr>
                                    <th style="width:8%;">No. Reg</th>
                                    <th style="width:10%;">Jam</th>
                                    <th style="width:20%;">No. Rawat</th>
                                    <th style="width:30%;">Nama Pasien</th>
                                    <th style="width:20%;">Dokter Dituju</th>
                                    <th style="width:12%; text-align:center;">Aksi</th>
                                </tr>
                            </thead>
                            <tbody id="queueTableBody">
                                <tr>
                                    <td colspan="6" style="text-align:center; color:#94a3b8; font-style:italic; padding:2rem;">Pilih poliklinik dan dokter untuk melihat antrean</td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Links to Displays -->
                <div class="links-row">
                    <a href="antrianpoli51.php" target="_blank">
                        <span class="material-icons">tv</span>
                        <span>Buka Display Hall Multi-Poli</span>
                    </a>
                    <a href="#" id="linkDoorDisplay" target="_blank">
                        <span class="material-icons">desktop_windows</span>
                        <span>Buka Display Pintu Poli Ini</span>
                    </a>
                </div>
            </div>
        </div>
    </div>

    <script>
        var currentPoli = '';
        var currentDokter = '';
        var isCalling = false;
        var knownDoctorList = [];
        var currentUpcomingList = []; // In-memory cache of upcoming queue for instant filtering

        function escapeHtml(str) {
            if (!str) return '';
            var div = document.createElement('div');
            div.textContent = str;
            return div.innerHTML;
        }

        function showFeedback(msg, isError) {
            var el = document.getElementById('feedbackAlert');
            el.className = isError ? 'error' : 'success';
            el.textContent = msg;
            setTimeout(function() {
                el.style.display = 'none';
                el.className = '';
            }, 3500);
        }

        function loadPoliList() {
            fetch('api_antrian_poli_call.php?action=get_poli_list')
                .then(function(r) { return r.json(); })
                .then(function(res) {
                    if (res.status === 'success') {
                        var el = document.getElementById('selectPoli');
                        el.innerHTML = '<option value="">-- Pilih Poliklinik --</option>';
                        res.data.forEach(function(item) {
                            el.innerHTML += '<option value="' + item.kd_poli + '">' + escapeHtml(item.nm_poli) + '</option>';
                        });

                        var params = new URLSearchParams(window.location.search);
                        var p = params.get('p') || params.get('kd_poli');
                        if (p) {
                            el.value = p;
                        }
                        onPoliChange(true);
                    }
                });
        }

        function syncDoctorDropdown(doctors, preserveSelection) {
            var docEl = document.getElementById('selectDokter');
            var previousVal = docEl.value;

            var newKeys = doctors.map(function(d){ return d.kd_dokter; }).join(',');
            var oldKeys = knownDoctorList.map(function(d){ return d.kd_dokter; }).join(',');

            if (newKeys === oldKeys && docEl.options.length > 1) {
                return;
            }

            knownDoctorList = doctors;
            var html = '<option value="">-- Semua Dokter di Poli Ini --</option>';
            doctors.forEach(function(item) {
                html += '<option value="' + item.kd_dokter + '">' + escapeHtml(item.nm_dokter) + '</option>';
            });
            docEl.innerHTML = html;

            if (preserveSelection && previousVal) {
                docEl.value = previousVal;
            } else if (doctors.length === 1) {
                docEl.value = doctors[0].kd_dokter;
            }
        }

        function onPoliChange(initialLoad) {
            currentPoli = document.getElementById('selectPoli').value;
            var docEl = document.getElementById('selectDokter');
            docEl.innerHTML = '<option value="">Memuat Dokter...</option>';
            knownDoctorList = [];

            if (!currentPoli) {
                docEl.innerHTML = '<option value="">-- Pilih Dokter --</option>';
                return;
            }

            fetch('api_antrian_poli_call.php?action=get_dokter_list&kd_poli=' + encodeURIComponent(currentPoli))
                .then(function(r) { return r.json(); })
                .then(function(res) {
                    if (res.status === 'success') {
                        syncDoctorDropdown(res.data, false);

                        if (initialLoad) {
                            var params = new URLSearchParams(window.location.search);
                            var d = params.get('d') || params.get('kd_dokter');
                            if (d) {
                                docEl.value = d;
                            }
                        }
                        fetchClinicStatus();
                    }
                });
        }

        function refreshDoctorsInBackground() {
            if (!currentPoli) return;
            fetch('api_antrian_poli_call.php?action=get_dokter_list&kd_poli=' + encodeURIComponent(currentPoli))
                .then(function(r) { return r.json(); })
                .then(function(res) {
                    if (res.status === 'success') {
                        syncDoctorDropdown(res.data, true);
                    }
                })
                .catch(function(){});
        }

        function renderQueueTable(list) {
            var tbody = document.getElementById('queueTableBody');
            if (!list || list.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:#94a3b8; font-style:italic; padding:2rem;">Tidak ada antrean pasien yang cocok</td></tr>';
                return;
            }

            var tbodyHtml = '';
            list.forEach(function(item) {
                tbodyHtml += '<tr>' +
                    '<td class="td-reg">' + String(item.no_reg).padStart(3, '0') + '</td>' +
                    '<td class="td-time">' + escapeHtml(item.jam_reg) + '</td>' +
                    '<td>' + escapeHtml(item.no_rawat) + '</td>' +
                    '<td class="td-patient">' + escapeHtml(item.nm_pasien) + '</td>' +
                    '<td class="td-doctor">' + escapeHtml(item.nm_dokter || '-') + '</td>' +
                    '<td style="text-align:center;"><button class="btn-call-inline" onclick="callManual(\'' + escapeHtml(item.no_rawat) + '\')"><span class="material-icons" style="font-size:1rem;">campaign</span> Panggil</button></td>' +
                    '</tr>';
            });
            tbody.innerHTML = tbodyHtml;
        }

        function filterQueueTable() {
            var query = document.getElementById('queueSearchInput').value.toLowerCase().trim();
            if (!query) {
                renderQueueTable(currentUpcomingList);
                return;
            }

            var filtered = currentUpcomingList.filter(function(item) {
                return String(item.no_reg).toLowerCase().indexOf(query) !== -1 ||
                       String(item.no_rawat).toLowerCase().indexOf(query) !== -1 ||
                       String(item.nm_pasien).toLowerCase().indexOf(query) !== -1 ||
                       String(item.nm_dokter || '').toLowerCase().indexOf(query) !== -1;
            });
            renderQueueTable(filtered);
        }

        function fetchClinicStatus() {
            currentPoli = document.getElementById('selectPoli').value;
            currentDokter = document.getElementById('selectDokter').value;

            if (!currentPoli) return;

            var linkDoor = 'antrianpoliperpoli51.php?p=' + encodeURIComponent(currentPoli);
            if (currentDokter) linkDoor += '&d=' + encodeURIComponent(currentDokter);
            document.getElementById('linkDoorDisplay').href = linkDoor;

            var queryUrl = 'api_antrian_poli_call.php?kd_poli=' + encodeURIComponent(currentPoli) + '&limit=50';
            if (currentDokter) {
                queryUrl += '&kd_dokter=' + encodeURIComponent(currentDokter);
            }

            fetch(queryUrl)
                .then(function(r) { return r.json(); })
                .then(function(data) {
                    if (data.status !== 'success' || !data.channels || data.channels.length === 0) return;

                    var ch = data.channels[0];
                    var stage = document.getElementById('activeStage');
                    var regEl = document.getElementById('activeReg');
                    var nameEl = document.getElementById('activeName');
                    var subEl = document.getElementById('activeSub');

                    var act = ch.active_patient;
                    if (act && act.no_reg) {
                        regEl.textContent = String(act.no_reg).padStart(3, '0');
                        nameEl.textContent = act.nm_pasien;
                        subEl.textContent = 'Dokter: ' + (act.nm_dokter || '-') + ' | No. Rawat: ' + act.no_rawat + ' | Jam: ' + act.jam_reg;
                        stage.classList.add('calling-active');
                    } else {
                        regEl.textContent = '-';
                        nameEl.textContent = 'Belum Ada Pasien Dipanggil';
                        subEl.textContent = 'Dokter: ' + (ch.nm_dokter || '-');
                        stage.classList.remove('calling-active');
                    }

                    // Update memory list and count
                    currentUpcomingList = ch.upcoming || [];
                    document.getElementById('statTotalWait').textContent = currentUpcomingList.length + ' Pasien';

                    // Re-render table respecting active search query
                    filterQueueTable();
                })
                .catch(function(err) {
                    console.error('Error fetching clinic status:', err);
                });
        }

        function callNext() {
            if (isCalling) return;
            currentPoli = document.getElementById('selectPoli').value;
            currentDokter = document.getElementById('selectDokter').value;
            if (!currentPoli) {
                alert('Silakan pilih Poliklinik terlebih dahulu');
                return;
            }

            isCalling = true;
            var fd = new FormData();
            fd.append('kd_poli', currentPoli);
            if (currentDokter) fd.append('kd_dokter', currentDokter);

            fetch('api_antrian_poli_call.php?action=next', {
                method: 'POST',
                body: fd
            })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                isCalling = false;
                if (res.status === 'success') {
                    showFeedback('Pasien ' + res.patient.nm_pasien + ' (No. ' + String(res.patient.no_reg).padStart(3, '0') + ') berhasil dipanggil', false);
                    fetchClinicStatus();
                } else {
                    showFeedback(res.message || 'Gagal memanggil antrean', true);
                }
            })
            .catch(function(err) {
                isCalling = false;
                showFeedback('Gagal koneksi API: ' + err, true);
            });
        }

        function callRecall() {
            if (isCalling) return;
            currentPoli = document.getElementById('selectPoli').value;
            currentDokter = document.getElementById('selectDokter').value;
            if (!currentPoli) {
                alert('Silakan pilih Poliklinik terlebih dahulu');
                return;
            }

            isCalling = true;
            var fd = new FormData();
            fd.append('kd_poli', currentPoli);
            if (currentDokter) fd.append('kd_dokter', currentDokter);

            fetch('api_antrian_poli_call.php?action=recall', {
                method: 'POST',
                body: fd
            })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                isCalling = false;
                if (res.status === 'success') {
                    showFeedback('Panggilan ulang untuk ' + res.patient.nm_pasien + ' dikirim ke layar display', false);
                    fetchClinicStatus();
                } else {
                    showFeedback(res.message || 'Gagal panggil ulang', true);
                }
            })
            .catch(function(err) {
                isCalling = false;
                showFeedback('Gagal panggil ulang: ' + err, true);
            });
        }

        function callManual(noRawat) {
            if (isCalling) return;
            currentPoli = document.getElementById('selectPoli').value;
            currentDokter = document.getElementById('selectDokter').value;

            isCalling = true;
            var fd = new FormData();
            fd.append('kd_poli', currentPoli);
            if (currentDokter) fd.append('kd_dokter', currentDokter);
            fd.append('no_rawat', noRawat);

            fetch('api_antrian_poli_call.php?action=call_manual', {
                method: 'POST',
                body: fd
            })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                isCalling = false;
                if (res.status === 'success') {
                    showFeedback('Pasien ' + res.patient.nm_pasien + ' berhasil dipanggil', false);
                    fetchClinicStatus();
                } else {
                    showFeedback(res.message || 'Gagal memanggil manual', true);
                }
            })
            .catch(function(err) {
                isCalling = false;
                showFeedback('Gagal memanggil manual: ' + err, true);
            });
        }

        function resetClinicCall() {
            currentPoli = document.getElementById('selectPoli').value;
            currentDokter = document.getElementById('selectDokter').value;
            if (!currentPoli) return;

            if (!confirm('Kosongkan antrean aktif di layar display?')) return;

            var fd = new FormData();
            fd.append('kd_poli', currentPoli);
            if (currentDokter) fd.append('kd_dokter', currentDokter);

            fetch('api_antrian_poli_call.php?action=reset', {
                method: 'POST',
                body: fd
            })
            .then(function(r) { return r.json(); })
            .then(function(res) {
                showFeedback('Antrean di layar display berhasil dikosongkan/selesai', false);
                fetchClinicStatus();
            })
            .catch(function(err) {
                showFeedback('Gagal reset: ' + err, true);
            });
        }

        // Keyboard Shortcuts: Space / Enter for Next Patient with Focus Protection
        document.addEventListener('keydown', function(e) {
            if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT' || e.target.tagName === 'TEXTAREA' || e.target.tagName === 'BUTTON') return;
            if (e.code === 'Space' || e.code === 'Enter') {
                e.preventDefault();
                callNext();
            }
        });

        loadPoliList();
        setInterval(fetchClinicStatus, 3500);
        setInterval(refreshDoctorsInBackground, 10000);
    </script>
</body>

</html>
