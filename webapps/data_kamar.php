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
 		<h5 class="center"><i class="material-icons md-36">hotel</i> Daftar Ruang Rawat Inap</h5>
 		<table class="default">
 			<thead>
 				<tr>
 					<td align='left'><b>Kelas Kamar</b></td>
 					<td align='center'><b>Jumlah Bed</b></td>
 					<td align='center'><b>Bed Terisi</b></td>
 					<td align='center'><b>Bed Kosong</b></td>
 				</tr>
 			</thead>
 			<tbody>
 				<?php
					$_sql = "Select kelas from kamar where statusdata='1' group by kelas";
					$hasil = bukaquery($_sql);
					$cek = mysqli_num_rows($hasil);

					if ($cek > 0) {
						while ($data = mysqli_fetch_array($hasil)) {

							$total = mysqli_fetch_array(bukaquery("select count(kelas) from kamar where statusdata='1' and kelas='" . $data['kelas'] . "'"));
							$isi = mysqli_fetch_array(bukaquery("select count(kelas) from kamar where statusdata='1' and kelas='" . $data['kelas'] . "' and status='ISI'"));
							$kosong = mysqli_fetch_array(bukaquery("select count(kelas) from kamar where statusdata='1' and kelas='" . $data['kelas'] . "' and status='KOSONG'"));

							echo "<tr>
									<td align='left'><b>" . $data['kelas'] . "</b></td>
									<td align='center'>
									    <span class='badge-bed badge-total'>" . $total[0] . "</span>
									</td>
									<td align='center'>
									    <span class='badge-bed badge-isi'>" . $isi[0] . "</span>
									</td>
									<td align='center'>
									    <span class='badge-bed badge-kosong'>" . $kosong[0] . "</span>
									</td>
								</tr>";
						}
					} else {
						echo "<tr>
								<td colspan='4' style='text-align:center; font-size:20px; padding:20px;'>
								Data ruang rawat inap belum tersedia
								</td>
          					</tr>";
					}
					?>
 			</tbody>
 		</table>
 	</div>
 </div>