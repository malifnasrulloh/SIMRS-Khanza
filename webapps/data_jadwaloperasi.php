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
 		<h5 class="center"><i class="material-icons md-36">favorite</i> Jadwal Operasi</h5>
 		<table class="default">
 			<thead>
 				<tr>
 					<td><b>No.Rawat</b></td>
 					<td><b>Umur</b></td>
 					<td><b>J.K.</b></td>
 					<td><b>Mulai</b></td>
 					<td><b>Selesai</b></td>
 					<td><b>Status</b></td>
 					<td><b>Operasi</b></td>
 					<td><b>Operator</b></td>
 					<td><b>Ruang Operasi</b></td>
 				</tr>
 			</thead>
 			<tbody>
 				<?php
					$_sql = "select booking_operasi.no_rawat,reg_periksa.no_rkm_medis,pasien.nm_pasien,booking_operasi.tanggal, booking_operasi.jam_mulai,booking_operasi.jam_selesai,
								booking_operasi.status,booking_operasi.kd_dokter, dokter.nm_dokter,booking_operasi.kode_paket,paket_operasi.nm_perawatan,concat(reg_periksa.umurdaftar,' ',reg_periksa.sttsumur) as umur,
								pasien.jk,ruang_ok.nm_ruang_ok from booking_operasi inner join reg_periksa on booking_operasi.no_rawat=reg_periksa.no_rawat
								inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis
								inner join paket_operasi on booking_operasi.kode_paket=paket_operasi.kode_paket
								inner join dokter on booking_operasi.kd_dokter=dokter.kd_dokter  
								inner join ruang_ok on booking_operasi.kd_ruang_ok=ruang_ok.kd_ruang_ok
								where tanggal='" . date("Y-m-d", $tanggal) . "' order by booking_operasi.tanggal,booking_operasi.jam_mulai";
					$hasil = bukaquery($_sql);
					$cek = mysqli_num_rows($hasil);

					if ($cek > 0) {
						while ($data = mysqli_fetch_array($hasil)) {

							echo "<tr>
                          			<td>" . $data['no_rawat'] . "</td>
                          			<td>" . $data['umur'] . "</td>
                          			<td>" . $data['jk'] . "</td>
                          			<td>" . $data['jam_mulai'] . "</td>
                          			<td>" . $data['jam_selesai'] . "</td>
                          			<td>" . $data['status'] . "</td>
                          			<td>" . $data['nm_perawatan'] . "</td>
                          			<td>" . $data['nm_dokter'] . "</td>
                          			<td>" . $data['nm_ruang_ok'] . "</td>
                      			</tr> ";
						}
					} else {
						echo "<tr>
								<td colspan='9' style='text-align:center; font-size:20px; padding:20px;'>
								Tidak ada jadwal operasi pada hari ini
								</td>
          					</tr>";
					}
					?>
 			</tbody>
 		</table>
 	</div>
 </div>