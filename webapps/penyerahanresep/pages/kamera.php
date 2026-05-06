<?php
$noresep  = "";
$norawat  = "";
$_sql     = "select * from antriapotek3";
$hasil    = bukaquery2($_sql);
while ($data = mysqli_fetch_array($hasil)) {
	$noresep  = $data['no_resep'];
	$norawat  = $data['no_rawat'];
}

$no_rkm_medis = "";
$nm_pasien    = "";
$jk           = "";
$umur         = "";
$tgl_lahir    = "";
$alamat       = "";
$no_tlp       = "";
$_sql2  = "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','LAKI-LAKI','PEREMPUAN') as jk,
               pasien.umur,pasien.tgl_lahir,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat, 
               pasien.no_tlp from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis 
               inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel
               inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec 
               inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab
               where reg_periksa.no_rawat='" . $norawat . "'";
$hasil2 = bukaquery2($_sql2);
while ($data2  = mysqli_fetch_array($hasil2)) {
	$no_rkm_medis = $data2['no_rkm_medis'];
	$nm_pasien    = $data2['nm_pasien'];
	$jk           = $data2['jk'];
	$umur         = $data2['umur'];
	$tgl_lahir    = $data2['tgl_lahir'];
	$alamat       = $data2['alamat'];
	$no_tlp       = $data2['no_tlp'];
}
?>

<!DOCTYPE html>
<html>

<head>
	<title>SIMKES Khanza - Validasi Penyerahan Resep</title>
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<script src="js/jquery.min.js"></script>
	<script src="js/webcam.min.js"></script>
	<script src="js/signature_pad.umd.min.js"></script>
	<link rel="stylesheet" href="css/bootstrap.min.css" />
	<style type="text/css">
		:root {
			--primary: #6B9BD1;
			--primary-light: #7BA5C7;
			--primary-dark: #5A8ABF;
			--accent: #A8D5BA;
			--accent-light: #BFE5CE;
			--success: #065F46;
			--success-light: #10B981;
			--warning: #B45309;
			--danger: #C2410C;
			--gray-50: #F5F3F0;
			--gray-100: #F0EBE5;
			--gray-200: #E8E0D8;
			--gray-300: #D9CCC0;
			--gray-400: #C4B5A0;
			--gray-500: #8B8E91;
			--gray-600: #6B7077;
			--gray-700: #475569;
			--gray-800: #2C3E50;
			--gray-900: #1A2A3A;
			--radius: 12px;
			--shadow-sm: 0 1px 2px 0 rgba(0, 0, 0, 0.05);
			--shadow: 0 1px 3px 0 rgba(0, 0, 0, 0.1), 0 1px 2px -1px rgba(0, 0, 0, 0.1);
			--shadow-md: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -2px rgba(0, 0, 0, 0.1);
			--shadow-lg: 0 10px 15px -3px rgba(0, 0, 0, 0.1), 0 4px 6px -4px rgba(0, 0, 0, 0.1);
			--shadow-xl: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
		}

		* {
			box-sizing: border-box;
			margin: 0;
			padding: 0;
		}

		body {
			font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
			background: linear-gradient(180deg, #F5F3F0 0%, #F0EBE5 100%);
			min-height: 100vh;
			color: var(--gray-800);
			font-size: 16px;
			line-height: 1.6;
		}

		.page-wrapper {
			max-width: 900px;
			margin: 0 auto;
			padding: 32px 24px 60px;
		}

		/* Header */
		.page-header {
			text-align: center;
			margin-bottom: 28px;
		}

		.page-header h1 {
			font-size: 28px;
			font-weight: 700;
			color: #fff;
			text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
			margin-bottom: 8px;
			line-height: 1.3;
		}

		.page-header .resep-badge {
			display: inline-block;
			background: rgba(255, 255, 255, 0.2);
			backdrop-filter: blur(10px);
			color: #fff;
			padding: 4px 16px;
			border-radius: 20px;
			font-size: 13px;
			font-weight: 500;
		}

		/* Card base */
		.card {
			background: #fff;
			border-radius: 16px;
			box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
			overflow: hidden;
			margin-bottom: 24px;
			border: 1px solid var(--gray-100);
		}

		.card-header-bar {
			background: linear-gradient(135deg, var(--primary) 0%, var(--primary-light) 100%);
			padding: 16px 24px;
			display: flex;
			align-items: center;
			gap: 10px;
		}

		.card-header-bar .icon {
			font-size: 20px;
		}

		.card-header-bar h2 {
			color: #fff;
			font-size: 16px;
			font-weight: 700;
			margin: 0;
		}

		.card-body {
			padding: 24px 28px;
		}

		/* Patient Info */
		.info-grid {
			display: grid;
			grid-template-columns: 1fr 1fr;
			gap: 16px 32px;
		}

		.info-item {
			display: flex;
			gap: 8px;
		}

		.info-label {
			font-size: 13px;
			color: var(--gray-600);
			font-weight: 600;
			min-width: 100px;
			flex-shrink: 0;
		}

		.info-value {
			font-size: 14px;
			color: var(--gray-900);
			font-weight: 700;
		}

		/* Prescription Table */
		.rx-table {
			width: 100%;
			border-collapse: collapse;
			font-size: 12px;
		}

		.rx-table thead th {
			background: var(--gray-50);
			color: var(--gray-600);
			font-weight: 600;
			padding: 10px 12px;
			text-align: center;
			border-bottom: 2px solid var(--gray-200);
			font-size: 11px;
			text-transform: uppercase;
			letter-spacing: 0.5px;
		}

		.rx-table tbody td {
			padding: 10px 12px;
			text-align: center;
			border-bottom: 1px solid var(--gray-100);
			color: var(--gray-700);
		}

		.rx-table tbody tr:hover {
			background: var(--gray-50);
		}

		.rx-table tbody tr:last-child td {
			border-bottom: none;
		}

		/* Method Selector */
		.method-selector {
			display: flex;
			gap: 14px;
			padding: 16px 24px;
			flex-wrap: wrap;
		}

		.method-card {
			flex: 1;
			min-width: 220px;
			border: 2px solid var(--gray-200);
			border-radius: 14px;
			padding: 20px 16px;
			text-align: center;
			cursor: pointer;
			transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
			position: relative;
			overflow: hidden;
			background: #fff;
			opacity: 0.75;
			transform: scale(0.98);
			min-height: 160px;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
		}

		.method-card::before {
			content: '';
			position: absolute;
			top: 0;
			left: 0;
			right: 0;
			height: 3px;
			background: transparent;
			transition: background 0.3s ease;
		}

		.method-card:hover {
			border-color: var(--primary-light);
			box-shadow: 0 4px 12px rgba(107, 155, 209, 0.15);
			transform: translateY(-3px) scale(1.01);
		}

		.method-card.active {
			opacity: 1;
			transform: scale(1);
			border-color: var(--primary);
			background: linear-gradient(to bottom, rgba(107, 155, 209, 0.06), #fff);
			box-shadow: 0 0 0 3px rgba(107, 155, 209, 0.1), 0 4px 12px rgba(107, 155, 209, 0.15);
		}

		.method-card.active::before {
			background: linear-gradient(90deg, var(--primary), var(--accent));
		}

		.method-card.primary-method {
			opacity: 1;
			transform: scale(1);
			flex: 1.15;
		}

		.method-icon {
			font-size: 36px;
			margin-bottom: 10px;
			display: block;
		}

		.recommendation-badge {
			position: absolute;
			top: 12px;
			right: 12px;
			background: var(--accent);
			color: #2C5F47;
			padding: 5px 12px;
			border-radius: 20px;
			font-size: 11px;
			font-weight: 700;
			text-transform: uppercase;
			letter-spacing: 0.5px;
		}

		.method-title {
			font-size: 16px;
			font-weight: 700;
			color: var(--gray-900);
			margin-bottom: 6px;
		}

		.method-desc {
			font-size: 13px;
			color: var(--gray-600);
			line-height: 1.5;
		}

		.method-card.primary-method .method-desc small {
			display: block;
			font-size: 11px;
			color: var(--gray-500);
			margin-top: 4px;
		}

		.method-check {
			display: none;
			position: absolute;
			top: 8px;
			left: 8px;
			width: 28px;
			height: 28px;
			background: var(--primary);
			border-radius: 50%;
			align-items: center;
			justify-content: center;
			color: #fff;
			font-size: 14px;
			font-weight: 700;
			box-shadow: 0 2px 6px rgba(107, 155, 209, 0.4);
		}

		.method-card.active .method-check {
			display: flex;
		}

		/* Capture Areas */
		.capture-area {
			padding: 20px;
		}

		.capture-panel {
			display: none;
		}

		.capture-panel.active {
			display: block;
		}

		/* Webcam Panel */
		#webcam-panel .webcam-wrapper {
			display: flex;
			gap: 16px;
			align-items: flex-start;
		}

		#webcam-panel .webcam-feed {
			flex: 1;
			border-radius: 8px;
			overflow: hidden;
			border: 2px solid var(--gray-200);
			background: var(--gray-900);
		}

		#webcam-panel .webcam-preview {
			flex: 1;
			min-height: 280px;
			border-radius: 8px;
			border: 2px solid var(--gray-200);
			background: var(--gray-50);
			display: flex;
			align-items: center;
			justify-content: center;
			overflow: hidden;
		}

		#webcam-panel .webcam-preview img {
			max-width: 100%;
			max-height: 100%;
		}

		.preview-placeholder {
			text-align: center;
			color: var(--gray-400);
		}

		.preview-placeholder .icon {
			font-size: 40px;
			margin-bottom: 8px;
		}

		.preview-placeholder p {
			font-size: 12px;
			font-weight: 500;
		}

		/* Signature Panel */
		.signature-container {
			position: relative;
		}

		.signature-canvas-wrapper {
			border: 2px solid var(--gray-200);
			border-radius: 8px;
			overflow: hidden;
			background: #fff;
			position: relative;
		}

		.signature-canvas-wrapper.signing {
			border-color: var(--primary);
			box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.1);
		}

		#signature-canvas {
			width: 100%;
			height: 300px;
			display: block;
			cursor: crosshair;
			touch-action: none;
		}

		.signature-guide {
			position: absolute;
			bottom: 50px;
			left: 40px;
			right: 40px;
			border-bottom: 2px dashed var(--gray-300);
			pointer-events: none;
		}

		.signature-guide-label {
			position: absolute;
			bottom: 54px;
			left: 40px;
			font-size: 10px;
			color: var(--gray-400);
			font-weight: 500;
			text-transform: uppercase;
			letter-spacing: 1px;
			pointer-events: none;
		}

		.signature-toolbar {
			display: flex;
			gap: 8px;
			margin-top: 10px;
			justify-content: flex-end;
		}

		/* Buttons */
		.btn-custom {
			display: inline-flex;
			align-items: center;
			gap: 8px;
			padding: 12px 22px;
			font-size: 14px;
			font-weight: 700;
			border: none;
			border-radius: 10px;
			cursor: pointer;
			transition: all 0.25s ease;
			font-family: inherit;
			min-height: 48px;
			min-width: 140px;
		}

		.btn-custom:active {
			transform: scale(0.97);
		}

		.btn-custom:focus {
			outline: 3px solid var(--primary);
			outline-offset: 2px;
		}

		.btn-primary {
			background: linear-gradient(135deg, var(--primary) 0%, var(--primary-dark) 100%);
			color: #fff;
			box-shadow: 0 4px 12px rgba(107, 155, 209, 0.3);
		}

		.btn-primary:hover {
			box-shadow: 0 6px 16px rgba(107, 155, 209, 0.4);
			transform: translateY(-2px);
		}

		.btn-success {
			background: linear-gradient(135deg, var(--success) 0%, #047857 100%);
			color: #fff;
			box-shadow: 0 4px 12px rgba(6, 95, 70, 0.3);
		}

		.btn-success:hover {
			box-shadow: 0 6px 16px rgba(6, 95, 70, 0.4);
			transform: translateY(-2px);
		}

		.btn-warning {
			background: linear-gradient(135deg, #D97706 0%, var(--warning) 100%);
			color: #fff;
			box-shadow: 0 4px 12px rgba(180, 83, 9, 0.3);
		}

		.btn-warning:hover {
			box-shadow: 0 6px 16px rgba(180, 83, 9, 0.4);
			transform: translateY(-2px);
		}

		.btn-outline {
			background: #fff;
			color: var(--gray-700);
			border: 2px solid var(--gray-300);
		}

		.btn-outline:hover {
			background: var(--gray-50);
			border-color: var(--gray-400);
		}

		.btn-danger-outline {
			background: #fff;
			color: var(--danger);
			border: 2px solid #FBCFE8;
		}

		.btn-danger-outline:hover {
			background: #FEF2F2;
			border-color: var(--danger);
		}

		/* Action Bar */
		.action-bar {
			display: flex;
			justify-content: center;
			gap: 12px;
			padding: 24px 28px;
			background: var(--gray-50);
			border-top: 1px solid var(--gray-200);
			flex-wrap: wrap;
		}

		/* Validation Msg */
		.validation-msg {
			color: var(--danger);
			background: #FEF3C7;
			border-left: 4px solid var(--warning);
			padding: 12px 16px;
			border-radius: 8px;
			font-size: 13px;
			font-weight: 500;
			text-align: center;
			min-height: 20px;
			margin-top: 12px;
			line-height: 1.5;
		}

		/* Status indicator */
		.status-badge {
			display: inline-flex;
			align-items: center;
			gap: 8px;
			padding: 8px 16px;
			border-radius: 20px;
			font-size: 13px;
			font-weight: 600;
			margin-bottom: 16px;
		}

		.status-badge.ready {
			background: #ECFDF5;
			color: #065F46;
		}

		.status-badge.waiting {
			background: #FEF3C7;
			color: #92400E;
		}

		.status-dot {
			width: 8px;
			height: 8px;
			border-radius: 50%;
			animation: pulse 2s infinite;
		}

		.status-badge.ready .status-dot {
			background: var(--success);
		}

		.status-badge.waiting .status-dot {
			background: var(--warning);
		}

		@keyframes pulse {

			0%,
			100% {
				opacity: 1;
			}

			50% {
				opacity: 0.5;
			}
		}

		/* Reassurance Banner */
		.reassurance-banner {
			text-align: center;
			gap: 12px;
			background: linear-gradient(135deg, #ECFDF5 0%, #E0F7EC 100%);
			border: 1px solid #BFEBE5;
			border-radius: 12px;
			padding: 14px 18px;
			margin-bottom: 24px;
			font-size: 13px;
		}

		.reassurance-banner h1 {
			font-size: 28px;
			font-weight: 700;
			color: #065F46;
			text-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
			margin-bottom: 8px;
			line-height: 1.3;
		}

		.reassurance-banner .resep-badge {
			display: inline-block;
			background: rgba(255, 255, 255, 0.2);
			backdrop-filter: blur(10px);
			color: #0D6F56;
			padding: 4px 16px;
			border-radius: 20px;
			font-size: 16px;
			font-weight: 500;
		}

		/* Responsive */
		@media (max-width: 768px) {
			.page-wrapper {
				padding: 20px 14px 40px;
			}

			.page-header h1 {
				font-size: 24px;
			}

			.info-grid {
				grid-template-columns: 1fr;
				gap: 12px 16px;
			}

			#webcam-panel .webcam-wrapper {
				flex-direction: column;
			}

			.method-selector {
				flex-direction: column;
				gap: 12px;
			}

			.method-card {
				min-width: 100%;
				flex: 1 !important;
				transform: scale(1) !important;
				min-height: 130px;
			}

			.action-bar {
				flex-wrap: wrap;
				gap: 10px;
			}

			.btn-custom {
				flex: 1 1 calc(50% - 5px);
				min-width: unset;
				min-height: 44px;
			}

			.reassurance-banner {
				gap: 10px;
				padding: 12px 14px;
			}

			.reassurance-banner .icon {
				font-size: 16px;
			}

			.reassurance-banner p {
				font-size: 12px;
			}
		}

		/* Accessibility: Reduced Motion */
		@media (prefers-reduced-motion: reduce) {
			* {
				animation-duration: 0.01ms !important;
				animation-iteration-count: 1 !important;
				transition-duration: 0.01ms !important;
			}

			.status-dot {
				animation: none !important;
			}

			.method-card {
				transform: none !important;
			}

			.btn-custom:hover {
				transform: none !important;
			}
		}

		/* Accessibility: Focus Visible */
		:focus-visible {
			outline: 3px solid var(--primary);
			outline-offset: 2px;
		}
	</style>
</head>

<body>
	<div class="page-wrapper">
		<!-- Header -->
		<div class="reassurance-banner">
			<div>
				<h1>Konfirmasi Penerimaan Obat Anda</h1>
				<span class="resep-badge">📋 Resep: <?= $noresep; ?></span>
			</div>
		</div>

		<!-- Patient Info Card -->
		<div class="card">
			<div class="card-header-bar">
				<span class="icon">👤</span>
				<h2>Biodata Anda</h2>
			</div>
			<div class="card-body">
				<div class="info-grid">
					<div class="info-item">
						<span class="info-label">Nomor Rawat</span>
						<span class="info-value"><?= $norawat; ?></span>
					</div>
					<div class="info-item">
						<span class="info-label">Umur Pasien</span>
						<span class="info-value"><?= $umur; ?></span>
					</div>
					<div class="info-item">
						<span class="info-label">Nomor R.M.</span>
						<span class="info-value"><?= $no_rkm_medis; ?></span>
					</div>
					<div class="info-item">
						<span class="info-label">Tanggal Lahir</span>
						<span class="info-value"><?= $tgl_lahir; ?></span>
					</div>
					<div class="info-item">
						<span class="info-label">Nama Pasien</span>
						<span class="info-value"><?= $nm_pasien; ?></span>
					</div>
					<div class="info-item">
						<span class="info-label">Alamat</span>
						<span class="info-value"><?= $alamat; ?></span>
					</div>
					<div class="info-item">
						<span class="info-label">Jenis Kelamin</span>
						<span class="info-value"><?= $jk; ?></span>
					</div>
					<div class="info-item">
						<span class="info-label">No.HP/Telp</span>
						<span class="info-value"><?= $no_tlp; ?></span>
					</div>
				</div>
			</div>
		</div>

		<!-- Prescription Card -->
		<div class="card">
			<div class="card-header-bar">
				<span class="icon">💊</span>
				<h2>Daftar Obat</h2>
			</div>
			<div class="card-body" style="padding: 0;">
				<table class="rx-table">
					<thead>
						<tr>
							<th style="width:5%">No</th>
							<th style="width:40%">Nama Obat</th>
							<th style="width:17%">Jumlah</th>
							<th style="width:38%">Aturan Pakai</th>
						</tr>
					</thead>
					<tbody>
						<?php
						$i = 1;
						$resepnonracikan = bukaquery("select databarang.nama_brng,aturan_pakai.aturan,detail_pemberian_obat.jml,kodesatuan.satuan
                                    from resep_obat inner join reg_periksa inner join aturan_pakai inner join databarang inner join detail_pemberian_obat 
                                    inner join kodesatuan on resep_obat.no_rawat=reg_periksa.no_rawat and databarang.kode_brng=aturan_pakai.kode_brng and 
                                    detail_pemberian_obat.kode_brng=databarang.kode_brng and resep_obat.no_rawat=aturan_pakai.no_rawat and 
                                    resep_obat.tgl_perawatan=aturan_pakai.tgl_perawatan and resep_obat.jam=aturan_pakai.jam and 
                                    resep_obat.no_rawat=detail_pemberian_obat.no_rawat and resep_obat.tgl_perawatan=detail_pemberian_obat.tgl_perawatan and
                                    resep_obat.jam=detail_pemberian_obat.jam and kodesatuan.kode_sat=databarang.kode_sat where resep_obat.no_resep='$noresep'");
						while ($barisresepnonracikan = mysqli_fetch_array($resepnonracikan)) {
							echo "<tr>
                                        <td>$i</td>
                                        <td style='text-align:left;padding-left:16px'>$barisresepnonracikan[nama_brng]</td>
                                        <td>$barisresepnonracikan[jml] $barisresepnonracikan[satuan]</td>
                                        <td>$barisresepnonracikan[aturan]</td>
                                      </tr>";
							$i++;
						}
						$resepracikan = bukaquery("select obat_racikan.no_racik,obat_racikan.nama_racik,obat_racikan.tgl_perawatan,obat_racikan.jam,
                                    obat_racikan.no_rawat,obat_racikan.aturan_pakai,obat_racikan.jml_dr,metode_racik.nm_racik from resep_obat inner join 
                                    reg_periksa inner join obat_racikan inner join metode_racik on resep_obat.no_rawat=reg_periksa.no_rawat 
                                    and obat_racikan.kd_racik=metode_racik.kd_racik and resep_obat.no_rawat=obat_racikan.no_rawat and 
                                    resep_obat.tgl_perawatan=obat_racikan.tgl_perawatan and resep_obat.jam=obat_racikan.jam and 
                                    resep_obat.no_rawat=obat_racikan.no_rawat where resep_obat.no_resep='$noresep'");
						while ($barisresepracikan = mysqli_fetch_array($resepracikan)) {
							echo "<tr>
                                        <td>$i</td>
                                        <td style='text-align:left;padding-left:16px'>$barisresepracikan[no_racik] $barisresepracikan[nama_racik] (";
							$resepdetailracikan = bukaquery("select databarang.nama_brng,detail_pemberian_obat.jml from
                                        detail_pemberian_obat inner join databarang inner join detail_obat_racikan 
                                        on detail_pemberian_obat.kode_brng=databarang.kode_brng and 
                                        detail_pemberian_obat.kode_brng=detail_obat_racikan.kode_brng and 
                                        detail_pemberian_obat.tgl_perawatan=detail_obat_racikan.tgl_perawatan and 
                                        detail_pemberian_obat.jam=detail_obat_racikan.jam and 
                                        detail_pemberian_obat.no_rawat=detail_obat_racikan.no_rawat 
                                        where detail_pemberian_obat.tgl_perawatan='$barisresepracikan[tgl_perawatan]' 
                                        and detail_pemberian_obat.jam='$barisresepracikan[jam]' and 
                                        detail_pemberian_obat.no_rawat='$barisresepracikan[no_rawat]' and 
                                        detail_obat_racikan.no_racik='$barisresepracikan[no_racik]' order by databarang.kode_brng");
							while ($barisresepdetailracikan = mysqli_fetch_array($resepdetailracikan)) {
								echo "$barisresepdetailracikan[nama_brng] $barisresepdetailracikan[jml], ";
							}
							echo "  )
                                        </td>
                                        <td>$barisresepracikan[jml_dr] $barisresepracikan[nm_racik]</td>
                                        <td>$barisresepracikan[aturan_pakai]</td>
                                      </tr>";
							$i++;
						}

						if ($i == 0) {
							$resepnonracikan = bukaquery("select databarang.nama_brng,detailpiutang.aturan_pakai,detailpiutang.jml,kodesatuan.satuan
                            from detailpiutang inner join bridging_resep_apotek_bpjs on bridging_resep_apotek_bpjs.no_sep_apotek=detailpiutang.nota_piutang  
                            where bridging_resep_apotek_bpjs.no_resep='$noresep'");
							while ($barisresepnonracikan = mysqli_fetch_array($resepnonracikan)) {
								echo "<tr class='text-dark'>
                                <td align='center'>$i</td>
                                <td align='center'>$barisresepnonracikan[nama_brng]</td>
                                <td align='center'>$barisresepnonracikan[jml] $barisresepnonracikan[satuan]</td>
                                <td align='center'>$barisresepnonracikan[aturan_pakai]</td>
                              </tr>";
								$i++;
							}
						}
						?>
					</tbody>
				</table>
			</div>
		</div>

		<!-- Validation Card -->
		<div class="card">
			<div class="card-header-bar">
				<span class="icon">👤</span>
				<h2>Mari Kita Pastikan Identitas Anda</h2>
			</div>

			<!-- Method Selector -->
			<div class="method-selector">
				<div class="method-card primary-method active" id="method-webcam" onclick="switchMethod('webcam')" role="radio" aria-checked="true" aria-label="Pilih metode ambil foto" tabindex="0">
					<div class="method-check">✓</div>
					<div class="recommendation-badge">Direkomendasikan</div>
					<span class="method-icon">📷</span>
					<div class="method-title">Ambil Foto Saya</div>
					<div class="method-desc">Cara paling sederhana dan cepat<br><small>Ambil foto menggunakan kamera perangkat</small></div>
				</div>
				<div class="method-card" id="method-signature" onclick="switchMethod('signature')" role="radio" aria-checked="false" aria-label="Pilih metode tanda tangan" tabindex="0">
					<div class="method-check">✓</div>
					<span class="method-icon">✍️</span>
					<div class="method-title">Tanda Tangan</div>
					<div class="method-desc">Lebih familiar untuk beberapa pengguna<br><small>Tanda tangani langsung di layar</small></div>
				</div>
			</div>

			<form method="POST" action="pages/storeImage.php" onsubmit="return validasiSubmit();" enctype="multipart/form-data" id="validation-form">
				<input type="hidden" name="noresep" value="<?= $noresep; ?>">
				<input type="hidden" name="image" class="image-tag" id="TxtIsi1">

				<div class="capture-area">
					<!-- Webcam Panel -->
					<div class="capture-panel active" id="webcam-panel">
						<div style="text-align:center">
							<span class="status-badge waiting" id="webcam-status">
								<span class="status-dot"></span>
								Siap untuk ambil foto
							</span>
						</div>
						<div class="webcam-wrapper">
							<div class="webcam-feed">
								<div id="my_camera"></div>
							</div>
							<div class="webcam-preview" id="results">
								<div class="preview-placeholder">
									<div class="icon">📸</div>
									<p>Foto akan muncul di sini<br>setelah Anda menekan "Ambil Foto Saya"</p>
								</div>
							</div>
						</div>
						<div class="validation-msg" id="webcam-validation-msg"></div>
					</div>

					<!-- Signature Panel -->
					<div class="capture-panel" id="signature-panel">
						<div style="text-align:center">
							<span class="status-badge waiting" id="signature-status">
								<span class="status-dot"></span>
								Siap untuk tanda tangan
							</span>
						</div>
						<div class="signature-container">
							<div class="signature-canvas-wrapper" id="sig-wrapper">
								<canvas id="signature-canvas"></canvas>
								<div class="signature-guide"></div>
								<div class="signature-guide-label">Tanda tangani di atas garis ini</div>
							</div>
							<div class="signature-toolbar">
								<button type="button" class="btn-custom btn-outline" onclick="undoSignature()" title="Undo goresan terakhir">
									↩ Undo
								</button>
								<button type="button" class="btn-custom btn-danger-outline" onclick="clearSignature()" title="Hapus semua">
									✕ Hapus
								</button>
							</div>
						</div>
						<div class="validation-msg" id="signature-validation-msg"></div>
					</div>
				</div>

				<!-- Action Bar -->
				<div class="action-bar">
					<button type="button" class="btn-custom btn-warning" id="btn-capture" onclick="take_snapshot()">
						📸 Ambil Foto Saya
					</button>
					<button type="button" class="btn-custom btn-primary" id="btn-sign-confirm" onclick="confirmSignature()" style="display:none">
						✍️ Konfirmasi Tanda Tangan
					</button>
					<button type="submit" class="btn-custom btn-success" id="btn-save">
						💾 Selesai
					</button>
					<button type="button" class="btn-custom btn-outline" onclick="window.location.reload();">
						🔄 Mulai Ulang
					</button>
				</div>
			</form>
		</div>
	</div>

	<script>
		// ==========================================
		// State Management
		// ==========================================
		var currentMethod = 'webcam';
		var signaturePad = null;
		var webcamReady = false;
		var signatureConfirmed = false;
		var webcamCaptured = false;

		// ==========================================
		// Webcam Setup
		// ==========================================
		function initWebcam() {
			try {
				Webcam.set({
					width: 430,
					height: 320,
					image_format: 'jpeg',
					jpeg_quality: 90
				});
				Webcam.attach('#my_camera');
				webcamReady = true;
			} catch (e) {
				console.warn('Webcam initialization failed:', e);
			}
		}

		initWebcam();

		function take_snapshot() {
			if (!webcamReady) {
				document.getElementById('webcam-validation-msg').textContent = 'Kamera sedang memuat... Ini normal, silakan tunggu beberapa detik.';
				return;
			}
			Webcam.snap(function(data_uri) {
				$(".image-tag").val(data_uri);
				document.getElementById('results').innerHTML = '<img src="' + data_uri + '" style="width:100%;height:100%;object-fit:cover"/>';
				webcamCaptured = true;

				// Update status
				var status = document.getElementById('webcam-status');
				status.className = 'status-badge ready';
				status.innerHTML = '<span class="status-dot"></span> Foto berhasil! Siap untuk disimpan';
				document.getElementById('webcam-validation-msg').textContent = '';
			});
		}

		// ==========================================
		// Signature Pad Setup
		// ==========================================
		function initSignaturePad() {
			var canvas = document.getElementById('signature-canvas');
			var wrapper = document.getElementById('sig-wrapper');

			// Set canvas size to match container
			function resizeCanvas() {
				var ratio = Math.max(window.devicePixelRatio || 1, 1);
				canvas.width = canvas.offsetWidth * ratio;
				canvas.height = canvas.offsetHeight * ratio;
				canvas.getContext('2d').scale(ratio, ratio);
				if (signaturePad) {
					signaturePad.clear();
				}
			}

			signaturePad = new SignaturePad(canvas, {
				backgroundColor: 'rgb(255, 255, 255)',
				penColor: 'rgb(0, 0, 0)',
				minWidth: 1.5,
				maxWidth: 3.5,
				velocityFilterWeight: 0.7
			});

			// Visual feedback when signing
			signaturePad.addEventListener('beginStroke', function() {
				wrapper.classList.add('signing');
				signatureConfirmed = false;
				var status = document.getElementById('signature-status');
				status.className = 'status-badge waiting';
				status.innerHTML = '<span class="status-dot"></span> Sedang menandatangani...';
			});

			signaturePad.addEventListener('endStroke', function() {
				wrapper.classList.remove('signing');
			});

			resizeCanvas();
			window.addEventListener('resize', resizeCanvas);
		}

		function clearSignature() {
			if (signaturePad) {
				signaturePad.clear();
				signatureConfirmed = false;
				$(".image-tag").val('');
				var status = document.getElementById('signature-status');
				status.className = 'status-badge waiting';
				status.innerHTML = '<span class="status-dot"></span> Siap untuk tanda tangan baru';
				document.getElementById('signature-validation-msg').textContent = '';
			}
		}

		function undoSignature() {
			if (signaturePad) {
				var data = signaturePad.toData();
				if (data.length > 0) {
					data.pop();
					signaturePad.fromData(data);
					signatureConfirmed = false;
					$(".image-tag").val('');
				}
			}
		}

		function confirmSignature() {
			if (!signaturePad || signaturePad.isEmpty()) {
				document.getElementById('signature-validation-msg').textContent = 'Tolong buat tanda tangan terlebih dahulu di area yang disediakan.';
				return;
			}

			// Security: check that signature has minimum complexity
			var data = signaturePad.toData();
			var totalPoints = 0;
			for (var i = 0; i < data.length; i++) {
				totalPoints += data[i].points.length;
			}

			if (data.length < 2 || totalPoints < 10) {
				document.getElementById('signature-validation-msg').textContent = 'Tanda tangan terlalu sederhana. Coba lagi dengan gerakan yang lebih panjang dan jelas?';
				return;
			}

			// Generate image with timestamp watermark
			var dataUrl = signaturePad.toDataURL('image/jpeg');
			$(".image-tag").val(dataUrl);
			signatureConfirmed = true;

			// Update status
			var status = document.getElementById('signature-status');
			status.className = 'status-badge ready';
			status.innerHTML = '<span class="status-dot"></span> Tanda tangan dikonfirmasi. Siap untuk disimpan';
			document.getElementById('signature-validation-msg').textContent = '';
		}

		function addTimestamp(originalDataUrl) {
			// Create a canvas to add timestamp watermark
			var img = new Image();
			var canvas = document.createElement('canvas');
			var ctx = canvas.getContext('2d');

			img.src = originalDataUrl;
			canvas.width = img.width || 800;
			canvas.height = img.height || 600;

			ctx.drawImage(img, 0, 0);

			// Add semi-transparent timestamp
			var now = new Date();
			var timestamp = now.getFullYear() + '-' +
				String(now.getMonth() + 1).padStart(2, '0') + '-' +
				String(now.getDate()).padStart(2, '0') + ' ' +
				String(now.getHours()).padStart(2, '0') + ':' +
				String(now.getMinutes()).padStart(2, '0') + ':' +
				String(now.getSeconds()).padStart(2, '0');

			ctx.font = '11px sans-serif';
			ctx.fillStyle = 'rgba(148, 163, 184, 0.6)';
			ctx.textAlign = 'right';
			ctx.fillText(timestamp, canvas.width - 10, canvas.height - 10);

			return canvas.toDataURL('image/jpeg');
		}

		// ==========================================
		// Method Switching
		// ==========================================
		function switchMethod(method) {
			currentMethod = method;

			// Update cards
			document.getElementById('method-webcam').classList.toggle('active', method === 'webcam');
			document.getElementById('method-signature').classList.toggle('active', method === 'signature');

			// Update aria-checked for accessibility
			document.getElementById('method-webcam').setAttribute('aria-checked', method === 'webcam');
			document.getElementById('method-signature').setAttribute('aria-checked', method === 'signature');

			// Update panels
			document.getElementById('webcam-panel').classList.toggle('active', method === 'webcam');
			document.getElementById('signature-panel').classList.toggle('active', method === 'signature');

			// Update buttons
			document.getElementById('btn-capture').style.display = method === 'webcam' ? '' : 'none';
			document.getElementById('btn-sign-confirm').style.display = method === 'signature' ? '' : 'none';

			// Clear the image field when switching
			$(".image-tag").val('');

			// Initialize signature pad on first switch
			if (method === 'signature' && !signaturePad) {
				setTimeout(initSignaturePad, 100);
			}

			// Reset validation messages
			document.getElementById('webcam-validation-msg').textContent = '';
			document.getElementById('signature-validation-msg').textContent = '';

			// Handle webcam
			if (method === 'webcam') {
				if (!webcamReady) {
					initWebcam();
				}
			}
		}

		// ==========================================
		// Form Validation
		// ==========================================
		function validasiSubmit() {
			var imageData = document.getElementById('TxtIsi1').value;

			if (!imageData || imageData.length === 0) {
				if (currentMethod === 'webcam') {
					document.getElementById('webcam-validation-msg').textContent = 'Tolong ambil foto terlebih dahulu dengan menekan tombol "Ambil Foto Saya".';
				} else {
					document.getElementById('signature-validation-msg').textContent = 'Tolong buat dan konfirmasi tanda tangan terlebih dahulu.';
				}
				return false;
			}

			if (currentMethod === 'signature' && !signatureConfirmed) {
				document.getElementById('signature-validation-msg').textContent = 'Tolong konfirmasi tanda tangan dengan menekan tombol "Konfirmasi Tanda Tangan".';
				return false;
			}

			// Final size check - ensure the image data is not trivially small
			if (imageData.length < 1000) {
				if (currentMethod === 'webcam') {
					document.getElementById('webcam-validation-msg').textContent = 'Foto kurang jelas. Coba ulangi dengan pencahayaan yang lebih baik?';
				} else {
					document.getElementById('signature-validation-msg').textContent = 'Tanda tangan kurang jelas. Silakan buat tanda tangan ulang dengan gerakan yang lebih tegas.';
				}
				return false;
			}

			return true;
		}

		// ==========================================
		// Keyboard Navigation
		// ==========================================
		document.getElementById('method-webcam').addEventListener('keydown', function(e) {
			if (e.key === 'Enter' || e.key === ' ') {
				e.preventDefault();
				switchMethod('webcam');
			}
		});

		document.getElementById('method-signature').addEventListener('keydown', function(e) {
			if (e.key === 'Enter' || e.key === ' ') {
				e.preventDefault();
				switchMethod('signature');
			}
		});
	</script>
</body>

</html>