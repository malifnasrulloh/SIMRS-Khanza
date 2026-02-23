 <?php
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
 <div class="col s12 row">
 	<div class="col s12">
 		<h5 class="center"><i class="material-icons md-36">group</i> Jadwal Praktek Dokter</h5>
 		<table class="default">
 			<thead>
 				<tr>
 					<th><b>Nama Dokter</b></th>
 					<th><b>Poliklinik</b></th>
 					<th><b>Mulai</b></th>
 					<th><b>Selesai</b></th>
 				</tr>
 			</thead>
 			<tbody>
 				<?php
					$hari = getOne("select DAYNAME(current_date())");
					$namahari = "";
					if ($hari == "Sunday") {
						$namahari = "AKHAD";
					} else if ($hari == "Monday") {
						$namahari = "SENIN";
					} else if ($hari == "Tuesday") {
						$namahari = "SELASA";
					} else if ($hari == "Wednesday") {
						$namahari = "RABU";
					} else if ($hari == "Thursday") {
						$namahari = "KAMIS";
					} else if ($hari == "Friday") {
						$namahari = "JUMAT";
					} else if ($hari == "Saturday") {
						$namahari = "SABTU";
					}
					$_sql = "Select dokter.nm_dokter,poliklinik.nm_poli,jadwal.jam_mulai,jadwal.jam_selesai 
                              from jadwal inner join dokter inner join poliklinik on dokter.kd_dokter=jadwal.kd_dokter 
                              and jadwal.kd_poli=poliklinik.kd_poli where jadwal.hari_kerja='$namahari'";
					$hasil = bukaquery($_sql);
					$cek = mysqli_num_rows($hasil);

					if ($cek > 0) {
						while ($data = mysqli_fetch_array($hasil)) {
							echo "<tr>
										<td><b>" . $data['nm_dokter'] . "</b></td>
										<td><b>" . $data['nm_poli'] . "</b></td>
										<td><b>" . $data['jam_mulai'] . "</b></td>
										<td><b>" . $data['jam_selesai'] . "</b></td>
								</tr>";
						}
					} else {
						echo "<tr>
            					<td colspan='4' style='text-align:center; font-size:20px; padding:20px;'>
            						Jadwal praktek hari ini belum tersedia
            					</td>
          					</tr>";
					}

					?>
 			</tbody>
 		</table>
 	</div>
 </div>