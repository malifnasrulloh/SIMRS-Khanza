<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){ exit(header("Location:../index.php")); }

    $iyem = trim(isset($_GET['iyem']))?trim($_GET['iyem']):NULL;
    $iyem = json_decode(encrypt_decrypt($iyem,"d"),true);
    if(isset($iyem["norawat"])){
        $norawat    = validTeks3($iyem["norawat"],17);
        $tglperiksa = validTeks3($iyem["tglperiksa"],10);
        $jam        = validTeks4($iyem["jam"],8);
        $querydetail = bukaquery(
            "SELECT date_format(periksa_radiologi.tgl_periksa,'%d-%m-%Y') as tanggalperiksa, periksa_radiologi.jam, ".
            "periksa_radiologi.no_rawat, reg_periksa.no_rkm_medis, pasien.nm_pasien, ".
            "dokterperujuk.nm_dokter as nm_dokter_perujuk, jns_perawatan_radiologi.nm_perawatan FROM periksa_radiologi ".
            "inner join reg_periksa on periksa_radiologi.no_rawat=reg_periksa.no_rawat ".
            "inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis ".
            "inner join dokter as dokterperujuk on periksa_radiologi.dokter_perujuk=dokterperujuk.kd_dokter ".
            "inner join jns_perawatan_radiologi on periksa_radiologi.kd_jenis_prw=jns_perawatan_radiologi.kd_jenis_prw ".
            "WHERE periksa_radiologi.no_rawat='$norawat' AND periksa_radiologi.tgl_periksa='$tglperiksa' AND periksa_radiologi.jam='$jam'"
        );
        if($rsdetail = mysqli_fetch_array($querydetail)){
            echo "<div class='block-header'><h2><center>HASIL BACAAN RADIOLOGI</center></h2></div>
                  <div class='row clearfix'>
                    <div class='col-lg-12 col-md-12 col-sm-12 col-xs-12'>
                      <div class='card'>
                        <div class='header'><h2>Data Pemeriksaan</h2></div>
                        <div class='body'>
                          <form method='POST' onsubmit='return validasiIsi();' enctype=multipart/form-data>
                            <input type='hidden' name='norawat' value='$norawat'>
                            <input type='hidden' name='tglperiksa' value='$tglperiksa'>
                            <input type='hidden' name='jam' value='$jam'>
                            <table width='100%' class='table table-hover js-basic-example dataTable'>
                              <tr><td width='25%'>No.Rawat</td><td>: {$rsdetail['no_rawat']}</td></tr>
                              <tr><td>Tanggal</td><td>: {$rsdetail['tanggalperiksa']} {$rsdetail['jam']}</td></tr>
                              <tr><td>No.RM</td><td>: {$rsdetail['no_rkm_medis']}</td></tr>
                              <tr><td>Nama Pasien</td><td>: <b>{$rsdetail['nm_pasien']}</b></td></tr>
                              <tr><td>Dokter Perujuk</td><td>: {$rsdetail['nm_dokter_perujuk']}</td></tr>
                              <tr><td>Jenis Pemeriksaan</td><td>: <span class='badge bg-indigo'>{$rsdetail['nm_perawatan']}</span></td></tr>
                            </table>";

            // ---- PACS ORTHANC LOOKUP ----
            $orthanc    = getOrthancSettings();
            $patient_id = $rsdetail["no_rkm_medis"];
            $pacs_loaded = false;
            $series_id   = "";

            $ch = @curl_init($orthanc['url'].":".$orthanc['port']."/tools/find");
            if($ch !== false){
                @curl_setopt_array($ch, [
                    CURLOPT_RETURNTRANSFER => true,
                    CURLOPT_POST           => true,
                    CURLOPT_POSTFIELDS     => json_encode(["Level"=>"Study","Expand"=>true,"Query"=>["PatientID"=>$patient_id]]),
                    CURLOPT_HTTPHEADER     => ['Content-Type: application/json'],
                    CURLOPT_USERPWD        => $orthanc['user'].":".$orthanc['pass'],
                    CURLOPT_TIMEOUT        => 3,
                ]);
                $resp = @curl_exec($ch);
                $code = @curl_getinfo($ch, CURLINFO_HTTP_CODE);
                @curl_close($ch);
                if($code==200){
                    $studies = json_decode($resp, true);
                    if(is_array($studies) && count($studies)>0){
                        $sid_url = $orthanc['url'].":".$orthanc['port']."/studies/".$studies[0]['ID'];
                        $ch2 = @curl_init($sid_url);
                        if($ch2){
                            @curl_setopt_array($ch2,[CURLOPT_RETURNTRANSFER=>true,CURLOPT_USERPWD=>$orthanc['user'].":".$orthanc['pass'],CURLOPT_TIMEOUT=>3]);
                            $s2 = @curl_exec($ch2); @curl_close($ch2);
                            $si = json_decode($s2, true);
                            if(isset($si["Series"]) && count($si["Series"])>0){ $series_id=$si["Series"][0]; $pacs_loaded=true; }
                        }
                    }
                }
            }

            // Status badge
            $pacsBadge = $pacs_loaded
                ? "<span class='badge bg-teal'>&#9679; PACS Online</span>"
                : "<span class='badge bg-orange'>&#9679; Fallback Lokal</span>";

            echo "<h7>Gambar Radiologi : $pacsBadge</h7>";

            if($pacs_loaded){
                $viewer_url = $orthanc['url'].":".$orthanc['port']."/web-viewer/app/viewer.html?series=".$series_id;
                echo "<div style='width:100%;height:550px;background:#000;border-radius:8px;overflow:hidden;margin:12px 0;border:2px solid #263238;'>
                        <iframe src='$viewer_url' style='width:100%;height:100%;border:none;' allowfullscreen></iframe>
                      </div>";
            } else {
                $q_img = bukaquery("SELECT lokasi_gambar FROM gambar_radiologi WHERE no_rawat='$norawat' AND tgl_periksa='$tglperiksa' AND jam='$jam'");
                $images = [];
                while($r_img = mysqli_fetch_assoc($q_img)) {
                    $images[] = $r_img['lokasi_gambar'];
                }

                if(!empty($images)){
                    $webapps_url = getWebappsUrl();
                    echo "<div class='row'>";
                    foreach($images as $index => $img) {
                        $src = $webapps_url . "/radiologi/" . $img;
                        $viewer_id = "imgViewer_" . $index;
                        $rad_img_id = "radImg_" . $index;
                        $vp_id = "vp_" . $index;
                        $btn_zi = "btnZI_" . $index;
                        $btn_zo = "btnZO_" . $index;
                        $btn_zr = "btnZR_" . $index;
                        $btn_fs = "btnFS_" . $index;
                        
                        echo "
                        <div class='col-xs-12 col-md-6' style='margin-bottom:20px;'>
                            <div class='thumbnail' style='border:1px solid #ccc; padding:6px; background:#fafafa; border-radius:6px;'>
                                <div style='padding:4px 8px; background:#eee; font-weight:600; color:#333; border-radius:4px; margin-bottom:6px; display:flex; justify-content:space-between; align-items:center;'>
                                    <span>Gambar " . ($index + 1) . " / " . count($images) . "</span>
                                    <small style='color:#777;'>" . htmlspecialchars($img) . "</small>
                                </div>
                                <div id='$viewer_id' style='position:relative; width:100%; height:450px; background:#111; border-radius:6px; overflow:hidden; border:2px solid #263238;'>
                                    <div style='position:absolute; top:10px; left:10px; z-index:10; display:flex; gap:6px; background:rgba(0,0,0,.5); padding:6px; border-radius:6px;'>
                                        <button type='button' class='btn btn-xs btn-inverse' id='$btn_zi'><i class='material-icons' style='font-size:16px;'>zoom_in</i></button>
                                        <button type='button' class='btn btn-xs btn-inverse' id='$btn_zo'><i class='material-icons' style='font-size:16px;'>zoom_out</i></button>
                                        <button type='button' class='btn btn-xs btn-inverse' id='$btn_zr'><i class='material-icons' style='font-size:16px;'>refresh</i></button>
                                        <button type='button' class='btn btn-xs btn-inverse' id='$btn_fs'><i class='material-icons' style='font-size:16px;'>fullscreen</i></button>
                                    </div>
                                    <div id='$vp_id' style='width:100%; height:100%; display:flex; align-items:center; justify-content:center; cursor:grab; overflow:hidden;'>
                                        <img id='$rad_img_id' src='$src' style='max-width:100%; max-height:100%; transform:scale(1) translate(0,0); user-select:none;' draggable='false' onerror=\"this.src='images/broken-image.png'; alert('Gagal memuat file gambar dari Webapps. Periksa konfigurasi URL webapps anda.');\">
                                    </div>
                                </div>
                            </div>
                        </div>
                        <script>
                        (function(){
                          var sc=1, tx=0, ty=0, drag=false, sx, sy;
                          var img=document.getElementById('$rad_img_id'), vp=document.getElementById('$vp_id');
                          function upd(){ img.style.transform='scale('+sc+') translate('+(tx/sc)+'px,'+(ty/sc)+'px)'; }
                          document.getElementById('$btn_zi').onclick=function(){ sc=Math.min(sc+.25, 5); upd(); };
                          document.getElementById('$btn_zo').onclick=function(){ if(sc>.5){ sc-=.25; upd(); } };
                          document.getElementById('$btn_zr').onclick=function(){ sc=1; tx=0; ty=0; upd(); };
                          document.getElementById('$btn_fs').onclick=function(){
                              var c=document.getElementById('$viewer_id');
                              if(!document.fullscreenElement) c.requestFullscreen(); else document.exitFullscreen();
                          };
                          vp.onmousedown=function(e){ drag=true; vp.style.cursor='grabbing'; sx=e.clientX-tx; sy=e.clientY-ty; };
                          window.onmouseup=function(){ drag=false; vp.style.cursor='grab'; };
                          vp.onmousemove=function(e){ if(!drag) return; tx=e.clientX-sx; ty=e.clientY-sy; upd(); };
                        })();
                        <\/script>";
                    }
                    echo "</div>";
                } else {
                    echo "<div class='alert alert-warning' style='margin:12px 0;'>Citra radiologi belum tersedia untuk pemeriksaan ini.</div>";
                }
            }

            echo "          <h7>Hasil Bacaan :</h7>
                            <div class='form-group'><div class='form-line'>
                              <textarea name='hasil' oninput=\"this.value=this.value.replace(/[^a-zA-Z0-9 \\-\\n]/g,''); setDefault(this, document.getElementById('MsgIsi1'));\"
                                id='TxtIsi1' class='form-control' rows='7' maxlength='2000' placeholder='a-z A-Z 0-9 (Maksimal 2000 karakter)' required></textarea>
                              <span id='MsgIsi1' style='color:#CC0000; font-size:10px;'></span>
                            </div></div>
                            <center>
                              <button class='btn btn-success waves-effect' type='submit' name='BtnSimpan'>Simpan</button>&nbsp;&nbsp;
                              <button class='btn btn-warning waves-effect' type='reset' name='BtnReset'>Reset</button>&nbsp;&nbsp;
                              <a href='index.php?act=HasilRadiologi' class='btn btn-danger waves-effect'>Kembali</a>
                            </center>
                          </form>
                        </div>
                      </div>
                    </div>
                  </div>";

            $BtnSimpan = isset($_POST['BtnSimpan'])?$_POST['BtnSimpan']:NULL;
            if(isset($BtnSimpan)){
                $norawat2    = validTeks4($_POST["norawat"],17);
                $tglperiksa2 = validTeks4($_POST["tglperiksa"],10);
                $jam2        = validTeks4($_POST["jam"],8);
                $hasil       = validTeks4($_POST["hasil"],2000);
                if($norawat2 && $tglperiksa2 && $jam2 && $hasil){
                    try {
                        Tambah3("hasil_radiologi","'$norawat2','$tglperiksa2','$jam2','$hasil'");
                        JSRedirect("index.php?act=HasilRadiologi");
                    } catch(mysqli_sql_exception $e){
                        $msg = $e->getCode()==1062 ? "Hasil bacaan sudah diisi!" : "Gagal menyimpan hasil bacaan!";
                        echo "<div class='row clearfix'><div class='col-xs-12'><div class='card'><div class='body bg-orange'><center><h4>$msg</h4></center></div></div></div></div>";
                    }
                } else {
                    echo "<div class='row clearfix'><div class='col-xs-12'><div class='card'><div class='body bg-orange'><center><h4>Semua field wajib diisi!</h4></center></div></div></div></div>";
                }
            }
        }
    } else {
        echo "<div class='block-header'><h2><center>HASIL BACAAN RADIOLOGI</center></h2></div>
              <div class='row clearfix'><div class='col-xs-12'><div class='card'><div class='body'><center>Data tidak ditemukan.</center></div></div></div></div>";
        JSRedirect2("index.php?act=HasilRadiologi",3);
    }
?>