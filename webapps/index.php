<?php
    header("X-Robots-Tag: noindex", true);
    session_start();
    require_once('conf/conf.php');
    header("Expires: Mon, 26 Jul 1997 05:00:00 GMT"); 
    header("Last-Modified: ".gmdate("D, d M Y H:i:s")." GMT"); 
    header("Cache-Control: no-store, no-cache, must-revalidate"); 
    header("Cache-Control: post-check=0, pre-check=0", false);
    header("Pragma: no-cache"); // HTTP/1.0
    $action      = isset($_GET['aksi'])?$_GET['aksi']:NULL;
    if($action=="Keluar"){
        session_start();
        $_SESSION["ses_admin_login"]=null;
        unset($_SESSION["ses_admin_login"]); 
        session_destroy();
        exit(header("Location:index.php"));
    }
?>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta name="robots" content="noindex,nofollow">
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Edukasi, Konfirmasi & Persetujuan</title>
    <link href="css/login.css" rel="stylesheet" type="text/css" />
    <script type="text/javascript" src="conf/validator.js"></script>
    <script>
        function PopupCenter(pageURL, title,w,h) {
            var left = (screen.width/2)-(w/2);
            var top = (screen.height/2)-(h/2);
            var targetWin = window.open (pageURL, title, 'toolbar=no, location=no, directories=no, status=no, menubar=no, scrollbars=no, resizable=no, copyhistory=no, width='+w+', height='+h+', top='+top+', left='+left);        
        }
    </script>
</head>
<body>
    <div class="bg"></div>
    <div class="bg bg2"></div>
    <div class="bg bg3"></div>
    <div id="content">
        <?php 
           $sesilogin=isset($_SESSION['ses_admin_login'])?$_SESSION['ses_admin_login']:NULL;
           if ($sesilogin==USERHYBRIDWEB.PASHYBRIDWEB){
                echo "
                    <div id=\"navcontainer\">
                        <div style='width: 100%; height: 120%; overflow: auto;'> 
                            <table width='100%' align='center' height='100%'>
                                <tr width='100%' align='center'>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=persetujuanumum/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/5868931_architecture_building_coronavirus_hospital_corona_icon.png'/><br>
                                         Persetujuan Umum                                                  
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=persetujuantindakan/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/6771569_education_learning_pencil_school_signature_icon.png'/><br>
                                         Persetujuan/Penolakan Tindakan                                                  
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=perencanaanpemulangan/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/6141469_coronavirus_covid_covid19_hospital_infected_icon.png'/><br>
                                         Perencanaan Pemulangan Pasien                                                  
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=penyerahanresep/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/1360485894_add-notes.png'/><br>
                                         Penyerahan Resep Rawat Jalan                                                 
                                      </a>
                                    </td>
                                </tr>
                                <tr width='100%' align='center'>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=pernyataanumum/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/Edit-Male-User.png'/><br>
                                         Pernyataan Pasien Umum                                                  
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=pulangaps/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/5947112_clinic_doctor_healthcare_hospital_medical_icon.png'/><br>
                                         Pernyataan Pulang Atas Permintaan Sendiri                                                 
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=persetujuantransferruang/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/6009596_case_coronavirus_covid19_hospital_patient_icon.png'/><br>
                                         Persetujuan Transfer Antar Ruang                                                
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=persetujuanrawatinap/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/5983455_bed_hospital_medical_patient_icon.png'/><br>
                                         Persetujuan Rawat Inap                                                
                                      </a>
                                    </td>
                                </tr>
                                <tr width='100%' align='center'>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=penundaanpelayanan/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/9160890_clock_commerce_shopping_online_store_icon.png'/><br>
                                         Persetujuan Penundaan Pelayanan                                                
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=penolakananjuranmedis/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/8960611_hospitals_hospital_building_medic_health_icon.png'/><br>
                                         Penolakan Anjuran Medis                                               
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=pengkajianrestrain/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/3841816_chain_hyperlink_interface_link_multimedia_icon.png'/><br>
                                         Persetujuan Restrain                                               
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=pelaksanaanedukasi/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/11211449_book_library_learning_knowledge_education_icon.png'/><br>
                                         Bukti Pelaksanaan Informasi & Edukasi                                               
                                      </a>
                                    </td>
                                </tr>
                                <tr width='100%' align='center'>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=layanankedokteranfisikrehabilitasi/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/4082071_healthcare_hospital_medical_icon.png'/><br>
                                         Bukti Pelayanan Kedokteran Fisik & Rehabilitasi                                               
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=layananprogramkfr/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/8960631_crutches_crutch_orthopedic_physiotherapy_rehabilitation_icon.png'/><br>
                                         Bukti Pelayanan Program KFR                                            
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=persetujuanpemeriksaanhiv/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/6217201_corona_coronavirus_test_tube_virus_icon.png'/><br>
                                         Bukti Persetujuan Pemeriksaan HIV                                           
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=pernyataanmemilihdpjp/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/5898997_avatar_doctor_man_mask_user_icon.png'/><br>
                                         Pernyataan Memilih DPJP                                          
                                      </a>
                                    </td>
                                </tr>
                                <tr width='100%' align='center'>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=pernyataanmenerimabarang/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/file-manager.png'/><br>
                                         Serah Terima Anggota Tubuh/Barang                                          
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=permintaanbinrohtal/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/mental-health_18376031-2.png'/><br>
                                         Persetujuan Bimbingan Rohani & Mental                                         
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=perlindungankekerasan/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/boxing_2043124.png'/><br>
                                         Permintaan Perlindungan Dari Kekerasan                                        
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=permohonanprivasi/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/compliant_4252354.png'/><br>
                                         Permohonan Privasi Pasien                                        
                                      </a>
                                    </td>
                                </tr>
                                <tr width='100%' align='center'>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=secondopinion/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/conversation_3601377.png'/><br>
                                         Permintaan Second Opinion                                       
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=penolakanresusitasi/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/services_6007742.png'/><br>
                                         Penolakan Resusitasi                                      
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a target=_blank href=pengajuancutiperawatan/login.php?iyem=".encrypt_decrypt("{\"usere\":\"".USERHYBRIDWEB."\",\"passwordte\":\"".PASHYBRIDWEB."\"}","e").">                                                 
                                         <img src='images/writing_2593639.png'/><br>
                                         Pengajuan Cuti Perawatan                                     
                                      </a>
                                    </td>
                                    <td width='25%' align='center'>
                                      <a href='?aksi=Keluar'>                                                 
                                         <img src='images/1360484978_application-pgp-signature.png'/><br>
                                         Keluar                                               
                                      </a>
                                    </td>
                                </tr>
                            </table> 
                        </div>
                    </div>";		
           }else{
               $pesan_warning = "";
               $is_locked = false;

               if (!function_exists('cekKompleksitasPassword')) {
                   function cekKompleksitasPassword($pwd) {
                       if (strlen($pwd) < 8) return false;
                       if (!preg_match('/[A-Z]/', $pwd)) return false; // Huruf besar
                       if (!preg_match('/[a-z]/', $pwd)) return false; // Huruf kecil
                       if (!preg_match('/[0-9]/', $pwd)) return false; // Angka
                       if (!preg_match('/[^a-zA-Z0-9]/', $pwd)) return false; // Karakter khusus
                       return true;
                   }
               }

               if (!isset($_SESSION['login_fail_count'])) {
                   $_SESSION['login_fail_count'] = 0;
               }
               if (!isset($_SESSION['login_lock_time'])) {
                   $_SESSION['login_lock_time'] = 0;
               }

               // Cek status Lock Out temporer (Aspek 4)
               if ($_SESSION['login_lock_time'] > time()) {
                   $is_locked = true;
                   $sisa_menit = ceil(($_SESSION['login_lock_time'] - time()) / 60);
                   $pesan_warning = "Gagal Login: Terlalu banyak percobaan salah. Akun/Akses diblokir sementara (Lock Out) selama $sisa_menit menit lagi.";
               }

               $BtnLogin=isset($_POST['BtnLogin'])?$_POST['BtnLogin']:NULL;
               if (isset($BtnLogin)) {
                   if ($is_locked) {
                       $pesan_warning = "Gagal Login: Terlalu banyak percobaan salah. Akun/Akses diblokir sementara (Lock Out). Silahkan tunggu beberapa saat.";
                   } else {
                       $usere      = validTeks4($_POST['usere'],30);
                       $raw_pass   = isset($_POST['passworde']) ? $_POST['passworde'] : '';
                       $raw_pass   = substr(str_replace("\0", "", $raw_pass), 0, 30);
                       $konektor   = bukakoneksi();
                       $passworde  = mysqli_real_escape_string($konektor, $raw_pass);

                       if(getOne("select count(admin.passworde) from admin where admin.usere=aes_encrypt('$usere','nur') and admin.passworde=aes_encrypt('$passworde','windi')")>0){
                           $_SESSION["ses_admin_login"]= USERHYBRIDWEB.PASHYBRIDWEB;
                           $_SESSION['login_fail_count'] = 0;
                           $_SESSION['login_lock_time'] = 0;
                           exit(header("Location:index.php"));
                       }else if(getOne("select count(user.password) from user where user.id_user=aes_encrypt('$usere','nur') and user.password=aes_encrypt('$passworde','windi')")>0){
                           $_SESSION["ses_admin_login"]= USERHYBRIDWEB.PASHYBRIDWEB;
                           $_SESSION['login_fail_count'] = 0;
                           $_SESSION['login_lock_time'] = 0;
                           exit(header("Location:index.php"));
                       }else{
                           // Delay time membatasi brute force berkali-kali (Aspek 3)
                           usleep(500000);
                           $_SESSION['login_fail_count']++;

                           // Lock Out Login temporer setelah 3 kali salah (Aspek 4)
                           if ($_SESSION['login_fail_count'] >= 3) {
                               $_SESSION['login_lock_time'] = time() + 300; // 5 menit
                               $pesan_warning = "Gagal Login: Percobaan login salah telah mencapai 3 kali. Akun/Akses diblokir sementara (Lock Out) selama 5 menit.";
                           } else if (!cekKompleksitasPassword($raw_pass)) {
                               // Aspek 7: Kebijakan Kompleksitas Password minimal 8 karakter memuat kombinasi huruf besar, kecil, angka, dan karakter khusus
                               $pesan_warning = "Gagal Login: Password tidak memenuhi kebijakan keamanan minimal (panjang minimal 8 karakter, memuat kombinasi huruf besar, huruf kecil, angka, dan karakter khusus seperti *, #, $, !, &).";
                           } else {
                               // Warning gagal login generik tidak membedakan user/password (Aspek 5 & 6)
                               $pesan_warning = "Gagal Login: Username atau Password Tidak Sesuai";
                           }
                       }
                   }
               }

               $alert_html = "";
               if (!empty($pesan_warning)) {
                   $alert_html = "<tr width='100%' align='center' border='0'>
                                      <td width='100%' align='center' border='0' style='padding: 8px;'>
                                          <div style='background-color: #fee2e2; border: 1px solid #ef4444; color: #b91c1c; padding: 10px; border-radius: 6px; font-size: 13px; font-weight: bold; font-family: Tahoma; text-align: center;'>
                                              $pesan_warning
                                          </div>
                                      </td>
                                  </tr>";
               }

               echo "  <form id=\"pengenmasuk-form\" role=\"form\" onsubmit=\"return validasiIsi();\" method=\"post\" action=\"\" enctype=multipart/form-data>
                           <table width='100%' align='center' height='100%' border='0'>
                               <tr width='100%' align='center' height='100%' border='0'>
                                   <td width='100%' height='100%' alin='center' valign='middle' border='0'>
                                       <table width='450px' align='center' height='100px' border='0'>
                                           $alert_html
                                           <tr width='100%' align='center' border='0'>
                                               <td width='100%' align='center' border='0' class=\"text\">
                                                   <input type=\"password\" name=\"usere\" class=\"text\" pattern=\"[a-zA-Z0-9, ./_]{1,30}\" title=\" a-zA-Z0-9, ./_ (Maksimal 30 karakter)\" required placeholder=\"User Login\" onkeydown=\"setDefault(this, document.getElementById('MsgIsi1'));\" id=\"TxtIsi1\" autocomplete=\"off\" size=\"20\" maxlength=\"30\" autofocus/>
                                                   <span id=\"MsgIsi1\" style=\"color:#CC0000; font-size:10px;\"></span>
                                               </td>
                                           </tr>
                                           <tr width='100%' align='center' border='0'>
                                               <td width='100%' align='center' border='0' class=\"text\">
                                                   <input type=\"password\" name=\"passworde\" class=\"text\" minlength=\"8\" maxlength=\"30\" pattern=\"(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,30}\" title=\"Kebijakan Password: Minimal 8 karakter, memuat kombinasi huruf besar (A-Z), huruf kecil (a-z), angka (0-9), dan karakter khusus (*, #, $, !, &, dll)\" required placeholder=\"Password (Min. 8 Karakter & Simbol)\" onkeydown=\"setDefault(this, document.getElementById('MsgIsi2'));\" id=\"TxtIsi2\" autocomplete=\"off\" size=\"20\"/>
                                                   <span id=\"MsgIsi2\" style=\"color:#CC0000; font-size:10px;\"></span>
                                               </td>
                                           </tr>
                                           <tr width='100%' align='center' border='0'>
                                               <td width='100%' border='0' class=\"text\">
                                                   <div align=\"center\"><input name=\"BtnLogin\" type=\"submit\" class=\"button\" value=\"Log-In\">&nbsp<input name=\"BtnKosong\" type=\"reset\" class=\"button\" value=\"Batal\"></div>
                                               </td>
                                           </tr>
                                           <tr width='100%' align='center' border='0'>
                                               <td width='100%' border='0' style='padding-top: 10px; font-size: 11px; color: #ffffff; text-align: center; text-shadow: 1px 1px 2px #000;'>
                                                   * Kebijakan Keamanan: Password minimal 8 karakter, kombinasi huruf besar, huruf kecil, angka, dan simbol (*, #, $, !, &, dll).
                                               </td>
                                           </tr>
                                       </table>
                                   </td>
                               </tr>
                           </table>
                       </form>";
           }
        ?>
    </div>    
</body>
</html>
