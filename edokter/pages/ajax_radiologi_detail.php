<?php
require_once("../conf/conf.php");
session_start();
if(!isset($_SESSION["ses_dokter"])) { header("Content-Type: text/html; charset=utf-8"); echo "<div class='alert alert-danger'>Sesi berakhir. Silakan login kembali.</div>"; exit; }

$norawat  = validTeks(isset($_POST['no_rawat']) ? $_POST['no_rawat'] : '');
$tgl      = validTeks(isset($_POST['tgl_periksa']) ? $_POST['tgl_periksa'] : '');
$jam      = validTeks(isset($_POST['jam']) ? $_POST['jam'] : '');

if(empty($norawat) || empty($tgl) || empty($jam)){
    echo "<div class='alert alert-danger'>Parameter tidak lengkap.</div>"; exit;
}

// Fetch Patient ID and Exam Info
$qh = bukaquery("
    SELECT rp.no_rkm_medis, p.nm_pasien, j.nm_perawatan, IFNULL(hr.hasil,'') as hasil_bacaan
    FROM periksa_radiologi pr
    INNER JOIN reg_periksa rp ON pr.no_rawat = rp.no_rawat
    INNER JOIN pasien p ON rp.no_rkm_medis = p.no_rkm_medis
    INNER JOIN jns_perawatan_radiologi j ON pr.kd_jenis_prw = j.kd_jenis_prw
    LEFT JOIN hasil_radiologi hr ON pr.no_rawat=hr.no_rawat AND pr.tgl_periksa=hr.tgl_periksa AND pr.jam=hr.jam
    WHERE pr.no_rawat='$norawat' AND pr.tgl_periksa='$tgl' AND pr.jam='$jam'
    LIMIT 1
");
$info = mysqli_fetch_assoc($qh);
if(!$info){
    echo "<div class='alert alert-warning'>Data pemeriksaan tidak ditemukan.</div>"; exit;
}

$patient_id  = $info['no_rkm_medis'];
$nama_pasien = $info['nm_pasien'];
$perawatan   = $info['nm_perawatan'];
$hasil       = $info['hasil_bacaan'];

// ---- PACS ORTHANC LOOKUP ----
$orthanc = getOrthancSettings();
$pacs_loaded = false;
$series_id = "";

// 1. Clean the base URL by stripping trailing slashes
$orthanc_url = rtrim($orthanc['url'], '/');
$orthanc_port = trim($orthanc['port']);

// 2. Safely check if the URL already has a port specified (e.g. "http://host:8042" has 2 colons)
$has_port = (substr_count($orthanc_url, ':') >= 2);

// 3. Construct the clean Orthanc API base URL for server-side cURL requests
$orthanc_api_base = $has_port ? $orthanc_url : (empty($orthanc_port) ? $orthanc_url : $orthanc_url . ":" . $orthanc_port);

// 4. Construct the URL that the browser will use for the PACS Stone Viewer iframe.
// If the configured URL is 'localhost' or '127.0.0.1', replace it with the visitor's incoming host header
// so that the doctor's web browser can access the hospital PACS server successfully!
$visitor_host = isset($_SERVER['HTTP_HOST']) ? $_SERVER['HTTP_HOST'] : '';
$visitor_domain = parse_url('http://' . $visitor_host, PHP_URL_HOST);

$orthanc_client_base = $orthanc_api_base;
if (!empty($visitor_domain)) {
    $orthanc_client_base = str_replace(['localhost', '127.0.0.1'], $visitor_domain, $orthanc_client_base);
}

// 5. Query Orthanc PACS server-side
$ch = @curl_init($orthanc_api_base . "/tools/find");
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
            $sid_url = $orthanc_api_base . "/studies/" . $studies[0]['ID'];
            $ch2 = @curl_init($sid_url);
            if($ch2){
                @curl_setopt_array($ch2,[CURLOPT_RETURNTRANSFER=>true,CURLOPT_USERPWD=>$orthanc['user'].":".$orthanc['pass'],CURLOPT_TIMEOUT=>3]);
                $s2 = @curl_exec($ch2); @curl_close($ch2);
                $si = json_decode($s2, true);
                if(isset($si["Series"]) && count($si["Series"])>0){
                    $series_id = $si["Series"][0];
                    $pacs_loaded = true;
                }
            }
        }
    }
}

// Render layout
echo "<div style='background:#fcfcfc; padding:15px; border:1px solid #ddd; border-radius:6px; margin-bottom:15px;'>
        <div style='display:flex; justify-content:space-between; align-items:center; flex-wrap:wrap; gap:10px; margin-bottom:10px;'>
            <h5 style='margin:0; font-weight:700; color:#333;'><i class='material-icons' style='vertical-align:middle; margin-right:4px;'>settings_overscan</i> $perawatan</h5>
            <div>" . ($pacs_loaded ? "<span class='badge bg-teal'>&#9679; PACS Online</span>" : "<span class='badge bg-orange'>&#9679; Fallback Lokal</span>") . "</div>
        </div>";

if(!empty($hasil)){
    echo "<div style='background:#fff; border-left:4px solid #ff9800; padding:10px 14px; margin-bottom:15px; border-radius:0 4px 4px 0;'>
            <b style='color:#555;'>Hasil Bacaan/Ekspertise:</b>
            <p style='margin:6px 0 0 0; white-space:pre-line; font-size:13px; line-height:1.6; color:#333;'>" . htmlspecialchars($hasil) . "</p>
          </div>";
} else {
    echo "<div class='alert alert-info' style='padding:8px 12px; margin-bottom:15px; font-size:12px;'>Belum ada hasil bacaan / ekspertise dokter spesialis radiologi.</div>";
}

if($pacs_loaded){
    $viewer_url = $orthanc_client_base . "/web-viewer/app/viewer.html?series=" . $series_id;
    echo "<div style='width:100%; height:450px; background:#000; border-radius:6px; overflow:hidden; border:2px solid #263238;'>
            <iframe src='$viewer_url' style='width:100%; height:100%; border:none;' allowfullscreen></iframe>
          </div>";
} else {
    $q_img = bukaquery("SELECT lokasi_gambar FROM gambar_radiologi WHERE no_rawat='$norawat' AND tgl_periksa='$tgl' AND jam='$jam'");
    $images = [];
    while($r_img = mysqli_fetch_assoc($q_img)){ $images[] = $r_img['lokasi_gambar']; }

    if(!empty($images)){
        $webapps_url = getWebappsUrl();
        echo "<div class='row'>";
        foreach($images as $index => $img){
            $src = $webapps_url . "/radiologi/" . $img;
            $v_id = "qViewer_" . $index;
            $img_id = "qRadImg_" . $index;
            $vp_id = "qVp_" . $index;
            $b_zi = "qZI_" . $index;
            $b_zo = "qZO_" . $index;
            $b_zr = "qZR_" . $index;
            $b_fs = "qFS_" . $index;
            echo "
            <div class='col-xs-12 col-md-6' style='margin-bottom:15px;'>
                <div class='thumbnail' style='border:1px solid #ccc; padding:6px; background:#fafafa; border-radius:6px; margin:0;'>
                    <div style='padding:4px 8px; background:#eee; font-weight:600; color:#333; border-radius:4px; margin-bottom:6px; display:flex; justify-content:space-between; align-items:center;'>
                        <span>Citra " . ($index + 1) . " / " . count($images) . "</span>
                        <small style='color:#777;'>" . htmlspecialchars($img) . "</small>
                    </div>
                    <div id='$v_id' style='position:relative; width:100%; height:320px; background:#111; border-radius:6px; overflow:hidden; border:1px solid #263238;'>
                        <div style='position:absolute; top:8px; left:8px; z-index:10; display:flex; gap:4px; background:rgba(0,0,0,.5); padding:4px; border-radius:4px;'>
                            <button type='button' class='btn btn-xs btn-inverse' id='$b_zi' style='padding:2px;'><i class='material-icons' style='font-size:14px;'>zoom_in</i></button>
                            <button type='button' class='btn btn-xs btn-inverse' id='$b_zo' style='padding:2px;'><i class='material-icons' style='font-size:14px;'>zoom_out</i></button>
                            <button type='button' class='btn btn-xs btn-inverse' id='$b_zr' style='padding:2px;'><i class='material-icons' style='font-size:14px;'>refresh</i></button>
                            <button type='button' class='btn btn-xs btn-inverse' id='$b_fs' style='padding:2px;'><i class='material-icons' style='font-size:14px;'>fullscreen</i></button>
                        </div>
                        <div id='$vp_id' style='width:100%; height:100%; display:flex; align-items:center; justify-content:center; cursor:grab; overflow:hidden;'>
                            <img id='$img_id' src='$src' style='max-width:100%; max-height:100%; transform:scale(1) translate(0,0); user-select:none;' draggable='false' onerror=\"this.src='images/broken-image.png';\">
                        </div>
                    </div>
                </div>
            </div>
            <script>
            (function(){
              var sc=1, tx=0, ty=0, drag=false, sx, sy;
              var img=document.getElementById('$img_id'), vp=document.getElementById('$vp_id');
              function upd(){ img.style.transform='scale('+sc+') translate('+(tx/sc)+'px,'+(ty/sc)+'px)'; }
              document.getElementById('$b_zi').onclick=function(){ sc=Math.min(sc+.25, 4); upd(); };
              document.getElementById('$b_zo').onclick=function(){ if(sc>.5){ sc-=.25; upd(); } };
              document.getElementById('$b_zr').onclick=function(){ sc=1; tx=0; ty=0; upd(); };
              document.getElementById('$b_fs').onclick=function(){
                  var c=document.getElementById('$v_id');
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
        echo "<div class='alert alert-warning' style='margin:0;'>Citra radiologi belum di-upload untuk pemeriksaan ini di webapps.</div>";
    }
}

echo "</div>";
