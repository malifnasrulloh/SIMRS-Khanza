<?php
    if(strpos($_SERVER['REQUEST_URI'],"pages")){
        exit(header("Location:../index.php"));
    }
    
    $namars       = getOne("select setting.nama_instansi from setting");
    $nosurat      = "";
    $norawat      = "";

    // Priority 1: no_surat passed directly in URL (multi-workstation safe)
    // Priority 2: fall back to shared queue table (legacy single-workstation)
    if (!empty($_GET['no_surat'])) {
        $nosurat = validTeks4($_GET['no_surat'], 20);
        $norawat = getOne("select no_rawat from surat_persetujuan_umum where no_surat='$nosurat'");
    } else {
        $_sql  = "select * from antripersetujuanumum";
        $hasil = bukaquery2($_sql);
        while ($data = mysqli_fetch_array($hasil)) {
            $nosurat = $data['no_surat'];
            $norawat = $data['no_rawat'];
        }
    }
    
    $no_rkm_medis = "";
    $nm_pasien    = "";
    $jk           = "";
    $umur         = "";
    $tgl_lahir    = "";
    $alamat       = "";
    $no_tlp       = "";
    
    $_sql2  = "select reg_periksa.no_rawat,pasien.no_rkm_medis,pasien.nm_pasien,if(pasien.jk='L','LAKI-LAKI','PEREMPUAN') as jk,
               pasien.umur,DATE_FORMAT(pasien.tgl_lahir,'%d-%m-%Y') as tgl_lahir,concat(pasien.alamat,', ',kelurahan.nm_kel,', ',kecamatan.nm_kec,', ',kabupaten.nm_kab) as alamat, 
               pasien.no_tlp from reg_periksa inner join pasien on reg_periksa.no_rkm_medis=pasien.no_rkm_medis 
               inner join kelurahan on pasien.kd_kel=kelurahan.kd_kel
               inner join kecamatan on pasien.kd_kec=kecamatan.kd_kec 
               inner join kabupaten on pasien.kd_kab=kabupaten.kd_kab
               where reg_periksa.no_rawat='".$norawat."'" ;  
    $hasil2 = bukaquery2($_sql2);
    while ($data2  = mysqli_fetch_array ($hasil2)){
        $no_rkm_medis = $data2['no_rkm_medis'];
        $nm_pasien    = $data2['nm_pasien'];
        $jk           = $data2['jk'];
        $umur         = $data2['umur'];
        $tgl_lahir    = $data2['tgl_lahir'];
        $alamat       = $data2['alamat'];
        $no_tlp       = $data2['no_tlp'];
    }
    
    $tanggal        = "";
    $nama_pj        = "";
    $umur_pj        = "";
    $no_ktppj       = "";
    $jkpj           = "";
    $bertindak_atas = "";
    $no_telp        = "";
    $_sql2  = "select DATE_FORMAT(surat_persetujuan_umum.tanggal,'%d-%m-%Y') as tanggal,surat_persetujuan_umum.nama_pj,surat_persetujuan_umum.umur_pj,
               surat_persetujuan_umum.no_ktppj,if(surat_persetujuan_umum.jkpj='L','LAKI-LAKI','PEREMPUAN') as jkpj,surat_persetujuan_umum.bertindak_atas,
               surat_persetujuan_umum.no_telp from surat_persetujuan_umum where surat_persetujuan_umum.no_surat='$nosurat'" ;  
    $hasil2 = bukaquery2($_sql2);
    while ($data2  = mysqli_fetch_array ($hasil2)){
        $tanggal        = $data2['tanggal'];
        $nama_pj        = $data2['nama_pj'];
        $umur_pj        = $data2['umur_pj'];
        $no_ktppj       = $data2['no_ktppj'];
        $jkpj           = $data2['jkpj'];
        $bertindak_atas = $data2['bertindak_atas'];
        $no_telp        = $data2['no_telp'];
    }
?>

<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>General Consent - SIMKES Khanza</title>
    <!-- Google Fonts -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@400;500;700;800&family=Plus+Jakarta+Sans:ital,wght@0,300;0,400;0,500;0,600;0,700;0,800;1,400&display=swap" rel="stylesheet">
    <!-- Bootstrap CSS for spacing/utilities fallback -->
    <link rel="stylesheet" href="css/bootstrap.min.css" />
    <!-- Custom State-of-the-Art CSS -->
    <link rel="stylesheet" href="css/modern.css" />
</head>
<body>
    <div class="consent-wrapper">
        <!-- Top Modern Header -->
        <header class="brand-section">
            <div class="brand-logo-area">
                <div class="brand-icon">SDH</div>
                <div class="hospital-meta">
                    <h2><?=$namars;?></h2>
                    <p>Jl. KH Hasyim Asy’ari No.123 Jombang | Telp: 0321-860777</p>
                </div>
            </div>
            <div class="form-id-badge">
                <svg width="16" height="16" fill="currentColor" viewBox="0 0 20 20" xmlns="http://www.w3.org/2000/svg"><path fill-rule="evenodd" d="M6 2a2 2 0 00-2 2v12a2 2 0 002 2h8a2 2 0 002-2V7.414A2 2 0 0015.414 6L12 2.586A2 2 0 0010.586 2H6zm5 6a1 1 0 10-2 0v3.586l-1.293-1.293a1 1 0 10-1.414 1.414l3 3a1 1 0 001.414 0l3-3a1 1 0 08-1.414-1.414L11 11.586V8z" clip-rule="evenodd"></path></svg>
                General Consent Rawat Jalan | No. <?=$nosurat;?>
            </div>
        </header>

        <!-- Dynamic Two-Column Layout -->
        <div class="consent-grid">
            
            <!-- LEFT PANEL: Interactive Rules, Rights & Obligations -->
            <section class="info-panel">
                <nav class="custom-tabs-nav">
                    <button type="button" class="tab-btn active" onclick="switchTab(event, 'tab-rules')">
                        <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                        Ketentuan Layanan
                    </button>
                    <button type="button" class="tab-btn" onclick="switchTab(event, 'tab-rights')">
                        <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z"></path></svg>
                        Hak Pasien
                    </button>
                    <button type="button" class="tab-btn" onclick="switchTab(event, 'tab-obligations')">
                        <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path></svg>
                        Kewajiban Pasien
                    </button>
                </nav>                <!-- TAB CONTENT: Rules -->
                <div id="tab-rules" class="tab-content-pane active">
                    <h3 class="mb-3">KETENTUAN YANG WAJIB DITAATI WAKTU RAWAT JALAN DI RS BEDAH SURYA DHARMA HUSADA</h3>
                    <div class="rules-list">
                        <div class="list-card rule-highlight">
                            <div class="num-badge">1</div>
                            <p>Dilarang membawa barang-barang berharga/perhiasan.</p>
                        </div>
                        <div class="list-card rule-highlight">
                            <div class="num-badge">2</div>
                            <p>Dilarang membawa senjata tajam/senjata api.</p>
                        </div>
                        <div class="list-card">
                            <div class="num-badge">3</div>
                            <p>Jam Kunjungan:<br>
                                • <strong>Poli Bedah:</strong> 08.00 - 11.00 & 19.00 – 21.00<br>
                                • <strong>Poli Penyakit Dalam:</strong> 18.00 – 19.00
                            </p>
                        </div>
                        <div class="list-card">
                            <div class="num-badge">4</div>
                            <p>Dilarang merokok di luar/di dalam ruangan (lingkungan rumah sakit).</p>
                        </div>
                        <div class="list-card">
                            <div class="num-badge">5</div>
                            <p>Dilarang memindahkan alat, mengubah-ubah, dan membawa pulang alat perawatan yang ada.</p>
                        </div>
                        <div class="list-card">
                            <div class="num-badge">6</div>
                            <p>Untuk menjaga privasi, dilarang mengambil gambar, merekam suara maupun video terhadap pelayanan yang diberikan kepada pasien tanpa izin dari pihak rumah sakit.</p>
                        </div>
                        <div class="list-card">
                            <div class="num-badge">7</div>
                            <p>Pasien yang menggunakan BPJS/Asuransi, jika disarankan untuk operasi, maka waktu berunding maksimal 3x24 jam setelah melakukan konsultasi dokter.</p>
                        </div>
                    </div>
                </div>

                <!-- TAB CONTENT: Rights -->
                <div id="tab-rights" class="tab-content-pane">
                    <h3 class="mb-3">HAK PASIEN</h3>
                    <div class="rights-list">
                        <div class="list-card"><div class="num-badge">1</div><p>Pasien berhak memperoleh informasi mengenai tata tertib dan peraturan yang berlaku di rumah sakit.</p></div>
                        <div class="list-card"><div class="num-badge">2</div><p>Pasien berhak memperoleh informasi tentang hak dan kewajiban pasien.</p></div>
                        <div class="list-card"><div class="num-badge">3</div><p>Pasien berhak memperoleh pelayanan yang manusiawi, adil, jujur, tanpa diskriminasi.</p></div>
                        <div class="list-card"><div class="num-badge">4</div><p>Pasien berhak memperoleh layanan kesehatan yang bermutu sesuai dengan standar profesi dan standar prosedur operasional.</p></div>
                        <div class="list-card"><div class="num-badge">5</div><p>Pasien berhak memperoleh layanan yang efektif dan efisien sehingga pasien terhindar dari kerugian fisik dan materi.</p></div>
                        <div class="list-card"><div class="num-badge">6</div><p>Pasien berhak mengajukan pengaduan atas kualitas pelayanan yang didapatkan.</p></div>
                        <div class="list-card"><div class="num-badge">7</div><p>Pasien berhak memilih dokter dan kelas perawatan sesuai dengan keinginan dan ketentuan yang berlaku di rumah sakit.</p></div>
                        <div class="list-card"><div class="num-badge">8</div><p>Pasien berhak meminta konsultasi tentang penyakit yang dideritanya kepada dokter lain yang mempunyai Surat Izin Praktik (SIP) baik di dalam maupun di luar rumah sakit.</p></div>
                        <div class="list-card"><div class="num-badge">9</div><p>Pasien berhak mendapat privasi dan kerahasiaan penyakit yang diderita termasuk data-data medisnya.</p></div>
                        <div class="list-card"><div class="num-badge">10</div><p>Pasien berhak mendapat informasi yang meliputi diagnosis dan tata cara tindakan medis, tujuan tindakan medis, alternatif tindakan, risiko, komplikasi yang mungkin terjadi, dan prognosis terhadap tindakan yang dilakukan serta perkiraan biaya pengobatan.</p></div>
                        <div class="list-card"><div class="num-badge">11</div><p>Pasien berhak memberikan persetujuan atau menolak atas tindakan yang akan dilakukan oleh tenaga kesehatan terhadap penyakit yang dideritanya.</p></div>
                        <div class="list-card"><div class="num-badge">12</div><p>Pasien berhak didampingi keluarganya dalam keadaan kritis.</p></div>
                        <div class="list-card"><div class="num-badge">13</div><p>Pasien berhak menjalankan ibadah sesuai agama atau kepercayaan yang dianutnya selama itu tidak mengganggu pasien lain.</p></div>
                        <div class="list-card"><div class="num-badge">14</div><p>Pasien berhak memperoleh keamanan dan keselamatan dirinya selama dalam perawatan di rumah sakit.</p></div>
                        <div class="list-card"><div class="num-badge">15</div><p>Pasien berhak mengajukan usul, saran, perbaikan atas perilaku rumah sakit terhadap dirinya.</p></div>
                        <div class="list-card"><div class="num-badge">16</div><p>Pasien berhak menolak pelayanan bimbingan rohani yang tidak sesuai dengan agama dan kepercayaan yang dianutnya.</p></div>
                        <div class="list-card"><div class="num-badge">17</div><p>Pasien berhak menggugat dan/atau menuntut rumah sakit apabila rumah sakit diduga memberikan pelayanan tidak sesuai dengan standar baik secara perdata maupun pidana.</p></div>
                        <div class="list-card"><div class="num-badge">18</div><p>Pasien berhak mengeluhkan pelayanan rumah sakit yang tidak sesuai dengan standar pelayanan melalui media cetak atau elektronik sesuai dengan ketentuan peraturan perundang-undangan.</p></div>
                    </div>
                </div>

                <!-- TAB CONTENT: Obligations -->
                <div id="tab-obligations" class="tab-content-pane">
                    <h3 class="mb-3">KEWAJIBAN PASIEN</h3>
                    <div class="obligations-list">
                        <div class="list-card"><div class="num-badge">1</div><p>Mematuhi peraturan yang berlaku di rumah sakit.</p></div>
                        <div class="list-card"><div class="num-badge">2</div><p>Menggunakan fasilitas rumah sakit secara bertanggung jawab.</p></div>
                        <div class="list-card"><div class="num-badge">3</div><p>Menghormati hak pasien lain, pengunjung, dan hak tenaga kesehatan serta petugas lainnya yang bekerja di rumah sakit.</p></div>
                        <div class="list-card"><div class="num-badge">4</div><p>Memberikan informasi yang jujur, lengkap, dan akurat sesuai dengan kemampuan dan pengetahuannya tentang masalah kesehatannya.</p></div>
                        <div class="list-card"><div class="num-badge">5</div><p>Memberikan informasi mengenai kemampuan finansial dan jaminan kesehatan yang dimilikinya.</p></div>
                        <div class="list-card"><div class="num-badge">6</div><p>Mematuhi rencana terapi yang direkomendasikan oleh tenaga kesehatan di rumah sakit dan disetujui oleh pasien yang bersangkutan setelah mendapatkan penjelasan sesuai dengan ketentuan peraturan perundang-undangan.</p></div>
                        <div class="list-card"><div class="num-badge">7</div><p>Menerima segala konsekuensi atas keputusan pribadinya untuk menolak rencana terapi yang direkomendasikan oleh tenaga kesehatan dan/atau tidak mematuhi petunjuk yang diberikan oleh tenaga kesehatan untuk penyembuhan penyakit atau masalah kesehatannya.</p></div>
                        <div class="list-card"><div class="num-badge">8</div><p>Meminta imbalan jasa atas pelayanan yang diterima.</p></div>
                    </div>
                </div>
            </section>

            <!-- RIGHT PANEL: State-of-the-Art Multi-Step Consent Form -->
            <section class="form-panel">
                <form id="consent-form" method="POST" action="pages/storeImage.php" onsubmit="return handleFormSubmit(event);" enctype="multipart/form-data">
                    <input type="hidden" name="nosurat" value="<?=$nosurat;?>">
                    <input type="hidden" name="image" class="image-tag" id="image-base64">

                    <!-- Wizard Progress Header -->
                    <div class="wizard-header">
                        <div class="wizard-steps">
                            <div class="wizard-progress" id="wizard-progress-bar"></div>
                            
                            <div class="step-node active" onclick="goToStep(1)">
                                <div class="step-circle">1</div>
                                <span class="step-label">Profil</span>
                            </div>
                            <div class="step-node" onclick="goToStep(2)">
                                <div class="step-circle">2</div>
                                <span class="step-label">Persetujuan</span>
                            </div>
                            <div class="step-node" onclick="goToStep(3)">
                                <div class="step-circle">3</div>
                                <span class="step-label">Verifikasi</span>
                            </div>
                        </div>
                    </div>

                    <!-- STEP 1: Profil Pasien & Pembuat Pernyataan -->
                    <div id="step-1" class="wizard-step-panel active">
                        <h4 class="mb-4 text-teal">I. Profil Identitas Pasien & Wali</h4>
                        
                        <!-- Read-only Patient Card -->
                        <div class="patient-info-grid">
                            <div class="info-item full-width">
                                <span class="info-item-label">Nama Pasien</span>
                                <span class="info-item-value"><?=$nm_pasien;?></span>
                            </div>
                            <div class="info-item">
                                <span class="info-item-label">No. Rekam Medis</span>
                                <span class="info-item-value"><?=$no_rkm_medis;?></span>
                            </div>
                            <div class="info-item">
                                <span class="info-item-label">Tgl Lahir / Gender</span>
                                <span class="info-item-value"><?=$tgl_lahir;?> / <?=$jk;?></span>
                            </div>
                            <div class="info-item full-width">
                                <span class="info-item-label">Alamat Lengkap</span>
                                <span class="info-item-value"><?=$alamat;?></span>
                            </div>
                        </div>

                        <!-- Read-only Pernyataan Maker / PJ info -->
                        <h5 class="mb-3 text-teal">Identitas Penanggung Jawab / Wali</h5>
                        <div class="patient-info-grid">
                            <div class="info-item">
                                <span class="info-item-label">Nama Wali</span>
                                <span class="info-item-value"><?=$nama_pj;?></span>
                            </div>
                            <div class="info-item">
                                <span class="info-item-label">Hubungan Pasien</span>
                                <span class="info-item-value"><?=$bertindak_atas;?></span>
                            </div>
                            <div class="info-item">
                                <span class="info-item-label">No. Identitas (KTP)</span>
                                <span class="info-item-value"><?=$no_ktppj;?></span>
                            </div>
                            <div class="info-item">
                                <span class="info-item-label">No. Telpon / HP</span>
                                <span class="info-item-value"><?=$no_telp;?></span>
                            </div>
                            <div class="info-item">
                                <span class="info-item-label">Umur / Gender Wali</span>
                                <span class="info-item-value"><?=$umur_pj;?> / <?=$jkpj;?></span>
                            </div>
                            <div class="info-item">
                                <span class="info-item-label">Tanggal Pernyataan</span>
                                <span class="info-item-value"><?=$tanggal;?></span>
                            </div>
                        </div>
                    </div>

                    <!-- STEP 2: Persetujuan Medis & Preferensi Nilai -->
                    <div id="step-2" class="wizard-step-panel">
                        <h4 class="mb-4 text-teal">II. Klausul Persetujuan & Preferensi</h4>

                        <!-- Elegant Legal Document Viewer (Using custom CSS) -->
                        <div class="legal-document-viewer mb-4">
                            <!-- Section 1 -->
                            <div class="legal-section">
                                <h5 class="legal-title">1. PERSETUJUAN UNTUK PERAWATAN DAN PENGOBATAN</h5>
                                <div class="legal-body">
                                    <p>
                                        <strong>a.</strong> Saya mengetahui bahwa saya memiliki kondisi yang membutuhkan perawatan medis. Saya mengizinkan dokter dan profesional kesehatan lainnya untuk melakukan prosedur diagnostik dan memberikan pengobatan medis seperti yang diperlukan dalam penilaian profesional mereka. Prosedur diagnostik dan perawatan medis termasuk terapi electrocardigarm, X-ray, tes darah, pemeriksaan laboratorium lainnya, terapi fisik, dan pemberian obat (kecuali yang membutuhkan persetujuan khusus/tertulis).
                                    </p>
                                    <p>
                                        <strong>b.</strong> Saya sadar bahwa praktik kedokteran dan bedah bukanlah ilmu pasti dan saya mengakui bahwa tidak ada jaminan atas hasil apa pun terhadap perawatan, prosedur, atau pemeriksaan apa pun yang dilakukan kepada saya.
                                    </p>
                                    <p>
                                        <strong>c.</strong> Saya mengerti dan memahami bahwa:
                                    </p>
                                    <ol class="legal-sublist">
                                        <li>Saya memiliki hak untuk mengajukan pertanyaan tentang pengobatan yang diusulkan termasuk identitas setiap orang yang memberikan atau mengamati pengobatan;</li>
                                        <li>Saya memiliki hak untuk menyetujui atau menolak persetujuan untuk setiap prosedur terapi;</li>
                                        <li>Saya mengerti bahwa ada staf medis rumah sakit bukan karyawan (staf tamu) yang telah diberikan hak untuk menggunakan fasilitas untuk perawatan dan pengobatan pasien mereka.</li>
                                    </ol>
                                    <p>
                                        <strong>d.</strong> Persetujuan yang saya berikan tidak termasuk persetujuan untuk prosedur/tindakan invasif atau tindakan yang memiliki risiko tinggi.
                                    </p>
                                    <p>
                                        <strong>e.</strong> Jika saya memutuskan untuk menghentikan perawatan medis untuk diri saya sendiri, saya memahami dan menyadari bahwa RS Bedah Surya Dharma Husada atau dokter dan profesional pemberi asuhan tidak bertanggung jawab atas hasil yang merugikan saya.
                                    </p>
                                </div>
                            </div>

                            <!-- Section 2 -->
                            <div class="legal-section">
                                <h5 class="legal-title">2. BARANG MILIK PASIEN</h5>
                                <div class="legal-body">
                                    <p>Saya memahami bahwa rumah sakit tidak bertanggung jawab atas semua kehilangan barang-barang milik saya dan saya pribadi bertanggung jawab atas barang berharga yang saya miliki termasuk uang, perhiasan, buku, cek, kartu kredit, handphone, atau barang berharga lainnya.</p>
                                </div>
                            </div>

                            <!-- Section 3 -->
                            <div class="legal-section">
                                <h5 class="legal-title">3. PERSETUJUAN PELEPASAN INFORMASI</h5>
                                <div class="legal-body">
                                    <p>Saya memahami informasi yang ada di dalam diri saya termasuk diagnosis, hasil laboratorium, dan hasil tes diagnosis yang akan digunakan untuk perawatan medis, RS Bedah Surya Dharma Husada akan menjamin kerahasiaannya. Saya memberi wewenang kepada RS Bedah Surya Dharma Husada untuk memberikan informasi tentang diagnosis, hasil pelayanan, dan pengobatan saya kepada anggota keluarga saya tersebut.</p>
                                </div>
                            </div>

                            <!-- Section 4 -->
                            <div class="legal-section">
                                <h5 class="legal-title">4. HAK DAN TANGGUNG JAWAB PASIEN</h5>
                                <div class="legal-body">
                                    <p>Saya memiliki hak untuk mengambil bagian dalam keputusan mengenai penyakit saya dan dalam hal perawatan medis dan rencana pengobatan. Saya telah mendapat informasi tentang "Hak dan Kewajiban Pasien" di RS Bedah Surya Dharma Husada melalui leaflet dan banner yang disediakan petugas.</p>
                                </div>
                            </div>

                            <!-- Section 5 -->
                            <div class="legal-section">
                                <h5 class="legal-title">5. INFORMASI RAWAT JALAN</h5>
                                <div class="legal-body">
                                    <p>Saya telah menerima informasi tentang peraturan yang telah diberlakukan di rumah sakit dan saya beserta keluarga bersedia untuk mematuhinya, termasuk mematuhi jam berkunjung pasien sesuai dengan aturan di rumah sakit.</p>
                                </div>
                            </div>

                            <!-- Section 6 -->
                            <div class="legal-section">
                                <h5 class="legal-title">6. INFORMASI BIAYA</h5>
                                <div class="legal-body">
                                    <p>Saya memahami tentang informasi biaya pengobatan atau biaya tindakan yang dijelaskan oleh petugas rumah sakit. Apabila saya adalah pasien BPJS/asuransi, saya bersedia mematuhi peraturan yang ditentukan oleh pihak penjamin untuk mendapatkan pelayanan kesehatan.</p>
                                </div>
                            </div>
                        </div>

                        <!-- Authorized Medical Record Receivers Deck (Dynamic) -->
                        <div class="receivers-deck-container mt-4 mb-4">
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <label class="font-weight-bold text-teal mb-0" style="font-size: 0.9rem; letter-spacing: 0.02em;">
                                    Daftar Penerima Informasi Medis/Hasil Rekam Medis (Maksimal 3 Orang):
                                </label>
                                <button type="button" class="btn btn-sm btn-outline-teal d-flex align-items-center gap-1" id="btn-add-receiver" onclick="addReceiverRow()" style="border-radius: 20px; font-size: 0.8rem; padding: 0.35rem 0.75rem; font-weight: 600;">
                                    <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" style="margin-right: 2px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path></svg>
                                    Tambah Penerima
                                </button>
                            </div>
                            
                            <div id="receivers-list" class="d-flex flex-column gap-3">
                                <!-- Dynamic receiver rows will be rendered here by JavaScript -->
                            </div>
                            
                            <!-- Hidden input fields to hold value for form submission -->
                            <input type="hidden" name="pengobatan_kepada" id="pengobatan_kepada" value="-">
                            <input type="hidden" name="penerima_nama_1" id="penerima_nama_1" value="-">
                            <input type="hidden" name="penerima_hubungan_1" id="penerima_hubungan_1" value="-">
                            <input type="hidden" name="penerima_nama_2" id="penerima_nama_2" value="-">
                            <input type="hidden" name="penerima_hubungan_2" id="penerima_hubungan_2" value="-">
                            <input type="hidden" name="penerima_nama_3" id="penerima_nama_3" value="-">
                            <input type="hidden" name="penerima_hubungan_3" id="penerima_hubungan_3" value="-">
                        </div>

                        <div class="form-group-custom">
                            <label for="nilai_kepercayaan">Nilai/Kepercayaan Khusus dalam Perawatan (Jika Ada):</label>
                            <input type="text" name="nilai_kepercayaan" id="nilai_kepercayaan" value="<?=$nilai_kepercayaan;?>" class="form-input-custom" placeholder="Contoh: Tidak ingin transfusi darah kecuali darurat, dll." autocomplete="off" maxlength="50">
                        </div>
                    </div>

                    <!-- STEP 3: Tanda Tangan & Foto Verifikasi (Camera & Canvas) -->
                    <div id="step-3" class="wizard-step-panel">
                        <h4 class="mb-4 text-teal">III. Verifikasi Dokumen</h4>
                        
                        <!-- Premium Segmented Verification Toggle -->
                        <div class="verification-selector">
                            <label class="d-block font-weight-bold mb-3 text-teal" style="font-size: 0.85rem; letter-spacing: 0.03em; text-transform: uppercase;">Pilih Metode Verifikasi:</label>
                            <div class="btn-group" role="group">
                                <input type="radio" class="btn-check" name="vmethod" id="vmethod-cam" value="cam" checked autocomplete="off" onchange="toggleVerificationMethod('cam')">
                                <label class="btn btn-outline-teal" for="vmethod-cam">Foto Kamera</label>

                                <input type="radio" class="btn-check" name="vmethod" id="vmethod-sig" value="sig" autocomplete="off" onchange="toggleVerificationMethod('sig')">
                                <label class="btn btn-outline-teal" for="vmethod-sig">Tanda Tangan Digital</label>
                            </div>
                        </div>

                        <div class="glass-alert" id="step3-instruction-alert">
                            <svg fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                            <span id="instruction-text">Silakan ambil foto wajah verifikasi pembuat pernyataan/wali bersama kartu identitas.</span>
                        </div>

                        <!-- Split Video Capture & Signature Pad -->
                        <div class="capture-container">
                            
                            <!-- Webcam Capture Area -->
                            <div class="capture-box active" id="webcam-box">
                                <div class="capture-title-bar">
                                    <div class="status-dot" id="camera-status"></div>
                                    <span>Kamera Verifikasi</span>
                                </div>
                                
                                <!-- Video Stream Display -->
                                <div class="webcam-viewport" id="video-container">
                                    <video id="webcam-video" autoplay playsinline muted></video>
                                </div>

                                <!-- Image Snapshot Preview (Hidden initially) -->
                                <div class="snap-preview d-none" id="snapshot-preview"></div>

                                <!-- Camera Switcher & Action Button -->
                                <div class="camera-select-wrapper mt-2">
                                    <select id="camera-selector" class="form-input-custom form-input-custom-sm py-1" onchange="switchCamera(this.value)">
                                        <option value="">Mencari kamera...</option>
                                    </select>
                                </div>

                                <button type="button" class="btn-modern btn-accent-modern btn-sm-modern mt-3" id="btn-snap" onclick="takeSnapshot()">
                                    <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
                                    Ambil Foto
                                </button>
                                <button type="button" class="btn-modern btn-secondary-modern btn-sm-modern mt-3 d-none" id="btn-retake" onclick="retakePhoto()">
                                    Ulangi Foto
                                </button>
                            </div>

                            <!-- Digital Signature Canvas Pad -->
                            <div class="capture-box d-none" id="signature-pad-box">
                                <div class="capture-title-bar">
                                    <div class="status-dot active"></div>
                                    <span>Tanda Tangan Pembuat Pernyataan</span>
                                </div>
                                
                                <canvas id="signature-pad" class="signature-canvas-pad" width="320" height="240"></canvas>
                                
                                <div class="canvas-controls">
                                    <button type="button" class="btn-modern btn-secondary-modern btn-sm-modern w-100" onclick="clearSignaturePad()">
                                        <svg width="14" height="14" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                                        Bersihkan
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Wizard Navigation Footer Buttons -->
                    <div class="wizard-footer">
                        <button type="button" class="btn-modern btn-secondary-modern" id="btn-prev" onclick="prevStep()" disabled>
                            <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18"></path></svg>
                            Kembali
                        </button>
                        
                        <button type="button" class="btn-modern btn-primary-modern" id="btn-next" onclick="nextStep()">
                            Lanjut
                            <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M14 5l7 7m0 0l-7 7m7-7H3"></path></svg>
                        </button>
                        
                        <button type="submit" class="btn-modern btn-primary-modern d-none" id="btn-submit">
                            <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3 3m0 0l-3-3m3 3V4"></path></svg>
                            Simpan & Selesai
                        </button>
                    </div>
                </form>
            </section>
        </div>
    </div>

    <!-- State-of-the-Art Application Logic -->
    <script>
        // Dynamic Receivers Deck Logic
        const patientName = <?=json_encode($nm_pasien);?>;
        var activeReceivers = [];

        function renderReceivers() {
            var container = document.getElementById("receivers-list");
            container.innerHTML = "";

            if (activeReceivers.length === 0) {
                container.innerHTML = `
                    <div class="text-center py-4 text-muted" style="border: 1px dashed var(--border-color); border-radius: 12px; font-size: 0.85rem;">
                        <svg width="24" height="24" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg" class="mb-2" style="opacity: 0.5; display: block; margin: 0 auto 8px;"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                        <div>Belum ada penerima informasi tambahan yang ditambahkan. Silakan klik tombol di atas.</div>
                    </div>
                `;
                document.getElementById("btn-add-receiver").disabled = false;
                updateHiddenInputs();
                return;
            }

            activeReceivers.forEach(function(receiver, idx) {
                var index = idx + 1;
                var isCustom = !['Diri Sendiri','Suami','Istri','Anak','Ayah','Ibu','Saudara','Kakak','Adik','-'].includes(receiver.relation);
                var html = `
                    <div class="receiver-row-card" id="receiver-row-${index}" style="opacity: 0; transform: translateY(10px); transition: all 0.3s ease;">
                        <div class="receiver-card-header d-flex justify-content-between align-items-center">
                            <span class="receiver-badge">Penerima #${index}</span>
                            <button type="button" class="btn-remove-receiver" onclick="removeReceiverRow(${idx})" title="Hapus Penerima">
                                <svg width="16" height="16" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                            </button>
                        </div>
                        <div class="receiver-card-body">
                            <div class="form-group-custom mb-2">
                                <label>Hubungan dengan Pasien:</label>
                                <select class="form-input-custom py-1 receiver-relation-select" id="receiver-relation-${index}" onchange="handleRelationChange(${idx}, this.value)">
                                    <option value="-">- Pilih Hubungan -</option>
                                    <option value="Diri Sendiri" ${receiver.relation === 'Diri Sendiri' ? 'selected' : ''}>Diri Sendiri (Pasien)</option>
                                    <option value="Suami" ${receiver.relation === 'Suami' ? 'selected' : ''}>Suami</option>
                                    <option value="Istri" ${receiver.relation === 'Istri' ? 'selected' : ''}>Istri</option>
                                    <option value="Anak" ${receiver.relation === 'Anak' ? 'selected' : ''}>Anak</option>
                                    <option value="Ayah" ${receiver.relation === 'Ayah' ? 'selected' : ''}>Ayah</option>
                                    <option value="Ibu" ${receiver.relation === 'Ibu' ? 'selected' : ''}>Ibu</option>
                                    <option value="Saudara" ${receiver.relation === 'Saudara' ? 'selected' : ''}>Saudara</option>
                                    <option value="Kakak" ${receiver.relation === 'Kakak' ? 'selected' : ''}>Kakak</option>
                                    <option value="Adik" ${receiver.relation === 'Adik' ? 'selected' : ''}>Adik</option>
                                    <option value="Lainnya" ${isCustom ? 'selected' : ''}>Lainnya...</option>
                                </select>
                                <input type="text" class="form-input-custom mt-2 py-1 receiver-custom-relation-input ${isCustom ? '' : 'd-none'}" id="receiver-custom-relation-${index}" placeholder="Tulis Hubungan Lainnya..." value="${isCustom ? receiver.relation : ''}" oninput="syncReceiverData(${idx})">
                            </div>
                            <div class="form-group-custom mb-0">
                                <label>Nama Lengkap Penerima:</label>
                                <input type="text" class="form-input-custom py-1 receiver-name-input" id="receiver-name-${index}" placeholder="Masukkan nama lengkap..." value="${receiver.name}" oninput="syncReceiverData(${idx})" autocomplete="off" ${receiver.relation === 'Diri Sendiri' ? 'readonly style="background-color: #f1f5f9;"' : ''}>
                            </div>
                        </div>
                    </div>
                `;
                container.insertAdjacentHTML('beforeend', html);

                // Quick fade-in animation trigger
                setTimeout(function() {
                    var el = document.getElementById(`receiver-row-${index}`);
                    if (el) {
                        el.style.opacity = "1";
                        el.style.transform = "translateY(0)";
                    }
                }, 50);
            });

            // Enable or disable addition button based on max limit (3)
            document.getElementById("btn-add-receiver").disabled = (activeReceivers.length >= 3);
            updateHiddenInputs();
        }

        function addReceiverRow() {
            if (activeReceivers.length < 3) {
                activeReceivers.push({ name: "", relation: "-" });
                renderReceivers();
            }
        }

        function removeReceiverRow(idx) {
            var index = idx + 1;
            var el = document.getElementById(`receiver-row-${index}`);
            if (el) {
                el.style.opacity = "0";
                el.style.transform = "translateY(10px)";
                setTimeout(function() {
                    activeReceivers.splice(idx, 1);
                    renderReceivers();
                }, 300);
            } else {
                activeReceivers.splice(idx, 1);
                renderReceivers();
            }
        }

        function handleRelationChange(idx, val) {
            var index = idx + 1;
            var customInput = document.getElementById(`receiver-custom-relation-${index}`);
            var nameInput = document.getElementById(`receiver-name-${index}`);

            if (val === 'Lainnya') {
                customInput.classList.remove("d-none");
                customInput.focus();
                activeReceivers[idx].relation = customInput.value || "";
                nameInput.readOnly = false;
                nameInput.style.backgroundColor = "";
            } else {
                customInput.classList.add("d-none");
                customInput.value = "";
                activeReceivers[idx].relation = val;
                
                if (val === 'Diri Sendiri') {
                    activeReceivers[idx].name = patientName;
                    nameInput.value = patientName;
                    nameInput.readOnly = true;
                    nameInput.style.backgroundColor = "#f1f5f9";
                } else {
                    nameInput.readOnly = false;
                    nameInput.style.backgroundColor = "";
                }
            }
            updateHiddenInputs();
        }

        function syncReceiverData(idx) {
            var index = idx + 1;
            var selectVal = document.getElementById(`receiver-relation-${index}`).value;
            var customVal = document.getElementById(`receiver-custom-relation-${index}`).value;
            var nameVal = document.getElementById(`receiver-name-${index}`).value;

            activeReceivers[idx].name = nameVal;
            if (selectVal === 'Lainnya') {
                activeReceivers[idx].relation = customVal;
            } else {
                activeReceivers[idx].relation = selectVal;
            }
            updateHiddenInputs();
        }

        function updateHiddenInputs() {
            // Reset all hidden fields to default "-"
            for (var i = 1; i <= 3; i++) {
                document.getElementById(`penerima_nama_${i}`).value = "-";
                document.getElementById(`penerima_hubungan_${i}`).value = "-";
            }

            // Sync with current list
            activeReceivers.forEach(function(rec, idx) {
                var index = idx + 1;
                document.getElementById(`penerima_nama_${index}`).value = rec.name.trim() || "-";
                document.getElementById(`penerima_hubungan_${index}`).value = rec.relation.trim() || "-";
            });

            // Sync pengobatan_kepada (legacy field) with relation of first receiver
            var firstRel = "-";
            if (activeReceivers.length > 0) {
                var rel = activeReceivers[0].relation;
                // ENUM: 'Suami','Istri','Anak','Ayah','Ibu','Saudara','Keponakan','Adik','Kakak','Orang Tua','Diri Sendiri','-'
                if (['Suami','Istri','Anak','Ayah','Ibu','Saudara','Keponakan','Adik','Kakak','Orang Tua','Diri Sendiri'].includes(rel)) {
                    firstRel = rel;
                } else if (rel !== "-" && rel !== "") {
                    firstRel = "Saudara"; // Fallback to ENUM-compatible 'Saudara' for custom relations
                }
            }
            document.getElementById("pengobatan_kepada").value = firstRel;
        }

        // Initialize receivers list
        document.addEventListener("DOMContentLoaded", function() {
            renderReceivers();
        });

        // Tab Navigation Logic
        function switchTab(evt, tabId) {
            var i, tabcontent, tablinks;
            tabcontent = document.getElementsByClassName("tab-content-pane");
            for (i = 0; i < tabcontent.length; i++) {
                tabcontent[i].classList.remove("active");
            }
            tablinks = document.getElementsByClassName("tab-btn");
            for (i = 0; i < tablinks.length; i++) {
                tablinks[i].classList.remove("active");
            }
            document.getElementById(tabId).classList.add("active");
            evt.currentTarget.classList.add("active");
        }

        // Wizard Flow Control
        var currentWizardStep = 1;
        var totalWizardSteps = 3;

        function updateWizardUI() {
            // Update active panels
            for (var i = 1; i <= totalWizardSteps; i++) {
                var panel = document.getElementById("step-" + i);
                if (i === currentWizardStep) {
                    panel.classList.add("active");
                } else {
                    panel.classList.remove("active");
                }
            }

            // Update header steps
            var steps = document.getElementsByClassName("step-node");
            for (var j = 0; j < steps.length; j++) {
                var stepNum = j + 1;
                if (stepNum < currentWizardStep) {
                    steps[j].classList.add("completed");
                    steps[j].classList.remove("active");
                } else if (stepNum === currentWizardStep) {
                    steps[j].classList.add("active");
                    steps[j].classList.remove("completed");
                } else {
                    steps[j].classList.remove("active", "completed");
                }
            }

            // Progress bar width
            var progressPct = ((currentWizardStep - 1) / (totalWizardSteps - 1)) * 90;
            document.getElementById("wizard-progress-bar").style.width = progressPct + "%";

            // Footer navigation buttons state
            var btnPrev = document.getElementById("btn-prev");
            var btnNext = document.getElementById("btn-next");
            var btnSubmit = document.getElementById("btn-submit");

            if (currentWizardStep === 1) {
                btnPrev.disabled = true;
                btnNext.classList.remove("d-none");
                btnSubmit.classList.add("d-none");
            } else if (currentWizardStep === totalWizardSteps) {
                btnPrev.disabled = false;
                btnNext.classList.add("d-none");
                btnSubmit.classList.remove("d-none");
                
                // Initialize active verification display
                var method = document.querySelector('input[name="vmethod"]:checked').value;
                toggleVerificationMethod(method);
            } else {
                btnPrev.disabled = false;
                btnNext.classList.remove("d-none");
                btnSubmit.classList.add("d-none");
            }
        }

        function nextStep() {
            if (currentWizardStep < totalWizardSteps) {
                currentWizardStep++;
                updateWizardUI();
            }
        }

        function prevStep() {
            if (currentWizardStep > 1) {
                currentWizardStep--;
                updateWizardUI();
            }
        }

        function goToStep(stepNum) {
            // Prevent going to step 3 directly if step 2 is not completed (safety gate)
            if (stepNum === 3 && currentWizardStep === 1) return;
            currentWizardStep = stepNum;
            updateWizardUI();
        }

        // --- Dynamic Verification Method Toggling ---
        function toggleVerificationMethod(method) {
            var webcamBox = document.getElementById("webcam-box");
            var sigBox = document.getElementById("signature-pad-box");
            var captureContainer = document.querySelector(".capture-container");
            var alertText = document.getElementById("instruction-text");

            if (method === "sig") {
                webcamBox.classList.add("d-none");
                sigBox.classList.remove("d-none");
                captureContainer.style.gridTemplateColumns = "1fr";
                alertText.innerText = "Silakan bubuhkan tanda tangan digital Anda pada panel di bawah ini.";
                
                stopCameraStream();
            } else if (method === "cam") {
                webcamBox.classList.remove("d-none");
                sigBox.classList.add("d-none");
                captureContainer.style.gridTemplateColumns = "1fr";
                alertText.innerText = "Silakan ambil foto wajah verifikasi pembuat pernyataan/wali bersama kartu identitas.";
                
                initCamera();
            }
        }

        // --- HTML5 Premium Camera Stream & Controls ---
        var videoStream = null;
        var snapshotBase64 = null;
        var videoDevices = [];
        var activeCameraId = null;

        function initCamera() {
            if (videoStream) return; // already active
            
            var videoEl = document.getElementById("webcam-video");
            
            // Enumerate Devices & Prioritize Back Camera
            navigator.mediaDevices.enumerateDevices()
                .then(function(devices) {
                    videoDevices = devices.filter(function(device) {
                        return device.kind === "videoinput";
                    });

                    var selectEl = document.getElementById("camera-selector");
                    selectEl.innerHTML = "";

                    if (videoDevices.length === 0) {
                        var opt = document.createElement("option");
                        opt.text = "Kamera tidak terdeteksi";
                        selectEl.appendChild(opt);
                        return;
                    }

                    // Populate select options
                    videoDevices.forEach(function(device, idx) {
                        var opt = document.createElement("option");
                        opt.value = device.deviceId;
                        opt.text = device.label || "Kamera " + (idx + 1);
                        selectEl.appendChild(opt);
                    });

                    // Search for back camera (environment facing mode)
                    var backCam = videoDevices.find(function(device) {
                        var label = device.label.toLowerCase();
                        return label.indexOf("back") !== -1 || 
                               label.indexOf("rear") !== -1 || 
                               label.indexOf("environment") !== -1 ||
                               label.indexOf("belakang") !== -1;
                    });

                    if (backCam) {
                        selectEl.value = backCam.deviceId;
                        activeCameraId = backCam.deviceId;
                    } else {
                        activeCameraId = videoDevices[0].deviceId;
                        selectEl.value = activeCameraId;
                    }

                    startCamera(activeCameraId);
                })
                .catch(function(err) {
                    console.error("Camera listing failed: ", err);
                    startCamera(null);
                });
        }

        function startCamera(deviceId) {
            if (videoStream) {
                videoStream.getTracks().forEach(function(track) {
                    track.stop();
                });
            }

            var constraints = {
                audio: false
            };

            if (deviceId) {
                constraints.video = { deviceId: { exact: deviceId } };
            } else {
                constraints.video = { facingMode: "environment" };
            }

            navigator.mediaDevices.getUserMedia(constraints)
                .then(function(stream) {
                    videoStream = stream;
                    var videoEl = document.getElementById("webcam-video");
                    videoEl.srcObject = stream;
                    videoEl.play();
                    
                    document.getElementById("camera-status").className = "status-dot active";
                })
                .catch(function(err) {
                    console.error("Camera start error: ", err);
                    navigator.mediaDevices.getUserMedia({ video: true, audio: false })
                        .then(function(stream) {
                            videoStream = stream;
                            var videoEl = document.getElementById("webcam-video");
                            videoEl.srcObject = stream;
                            videoEl.play();
                            
                            document.getElementById("camera-status").className = "status-dot active";
                        })
                        .catch(function(fallbackErr) {
                            alert("Kamera gagal diakses. Pastikan izin kamera telah diberikan.");
                        });
                });
        }

        function stopCameraStream() {
            if (videoStream) {
                videoStream.getTracks().forEach(function(track) {
                    track.stop();
                });
                videoStream = null;
                document.getElementById("camera-status").className = "status-dot";
            }
        }

        function switchCamera(deviceId) {
            if (!deviceId) return;
            activeCameraId = deviceId;
            startCamera(deviceId);
        }

        function takeSnapshot() {
            var videoEl = document.getElementById("webcam-video");
            
            // Create snapshot canvas with 640x480 resolution
            var snapCanvas = document.createElement("canvas");
            snapCanvas.width = 640;
            snapCanvas.height = 480;
            var ctx = snapCanvas.getContext("2d");
            
            ctx.drawImage(videoEl, 0, 0, 640, 480);
            
            snapshotBase64 = snapCanvas.toDataURL("image/jpeg", 0.9);
            
            var previewEl = document.getElementById("snapshot-preview");
            previewEl.innerHTML = '<img src="' + snapshotBase64 + '" id="captured-image"/>';
            
            document.getElementById("video-container").classList.add("d-none");
            previewEl.classList.remove("d-none");
            
            document.getElementById("btn-snap").classList.add("d-none");
            document.getElementById("btn-retake").classList.remove("d-none");
            document.getElementById("camera-selector").disabled = true;
        }

        function retakePhoto() {
            snapshotBase64 = null;
            document.getElementById("video-container").classList.remove("d-none");
            document.getElementById("snapshot-preview").classList.add("d-none");
            document.getElementById("snapshot-preview").innerHTML = "";
            
            document.getElementById("btn-snap").classList.remove("d-none");
            document.getElementById("btn-retake").classList.add("d-none");
            document.getElementById("camera-selector").disabled = false;
        }

        // --- HTML5 Responsive Signature Pad Canvas ---
        var sigCanvas = document.getElementById("signature-pad");
        var sigCtx = sigCanvas.getContext("2d");
        var drawing = false;
        var hasSigned = false;

        // Custom stroke style
        sigCtx.strokeStyle = "#0f766e"; // Slate Teal Primary
        sigCtx.lineWidth = 3.5;
        sigCtx.lineCap = "round";
        sigCtx.lineJoin = "round";

        function getMousePos(e) {
            var rect = sigCanvas.getBoundingClientRect();
            var clientX = e.touches ? e.touches[0].clientX : e.clientX;
            var clientY = e.touches ? e.touches[0].clientY : e.clientY;
            return {
                x: clientX - rect.left,
                y: clientY - rect.top
            };
        }

        function startSigDraw(e) {
            drawing = true;
            hasSigned = true;
            var pos = getMousePos(e);
            sigCtx.beginPath();
            sigCtx.moveTo(pos.x, pos.y);
            e.preventDefault();
        }

        function sigDraw(e) {
            if (!drawing) return;
            var pos = getMousePos(e);
            sigCtx.lineTo(pos.x, pos.y);
            sigCtx.stroke();
            e.preventDefault();
        }

        function stopSigDraw() {
            drawing = false;
        }

        // Listeners for both mouse and touch input
        sigCanvas.addEventListener("mousedown", startSigDraw);
        sigCanvas.addEventListener("mousemove", sigDraw);
        sigCanvas.addEventListener("mouseup", stopSigDraw);
        sigCanvas.addEventListener("mouseleave", stopSigDraw);

        sigCanvas.addEventListener("touchstart", startSigDraw, { passive: false });
        sigCanvas.addEventListener("touchmove", sigDraw, { passive: false });
        sigCanvas.addEventListener("touchend", stopSigDraw);

        function clearSignaturePad() {
            sigCtx.clearRect(0, 0, sigCanvas.width, sigCanvas.height);
            hasSigned = false;
        }

        // --- Frontend Canvas Blending & Overlap Overlay ---
        function handleFormSubmit(event) {
            event.preventDefault(); // Stop immediate submission

            var valKepercayaan = document.getElementById("nilai_kepercayaan").value.trim();
            if (valKepercayaan === "") {
                document.getElementById("nilai_kepercayaan").value = "-";
            }

            var method = document.querySelector('input[name="vmethod"]:checked').value;

            if (method === "sig") {
                // METHOD A: Signature Only
                if (!hasSigned) {
                    alert("Harap bubuhkan tanda tangan pembuat pernyataan terlebih dahulu.");
                    return false;
                }

                // Compile elegant Verification Certificate JPEG
                var blendCanvas = document.createElement("canvas");
                blendCanvas.width = 640;
                blendCanvas.height = 480;
                var bCtx = blendCanvas.getContext("2d");

                // Background
                bCtx.fillStyle = "#ffffff";
                bCtx.fillRect(0, 0, 640, 480);

                // Elegant dual frame borders
                bCtx.strokeStyle = "#0f766e";
                bCtx.lineWidth = 4;
                bCtx.strokeRect(15, 15, 610, 450);

                bCtx.strokeStyle = "#cbd5e1";
                bCtx.lineWidth = 1;
                bCtx.strokeRect(22, 22, 596, 436);

                // Header
                bCtx.fillStyle = "#0f766e";
                bCtx.textAlign = "center";
                bCtx.font = "bold 16px 'Outfit', Arial";
                bCtx.fillText("<?=$namars;?>", 320, 55);

                bCtx.fillStyle = "#475569";
                bCtx.font = "normal 10px 'Plus Jakarta Sans', Arial";
                bCtx.fillText("VERIFIKASI PERSATUJUAN UMUM RAWAT JALAN", 320, 75);
                bCtx.fillText("No. Surat: <?=$nosurat;?>  |  No. RM: <?=$no_rkm_medis;?>", 320, 92);

                bCtx.strokeStyle = "#e2e8f0";
                bCtx.lineWidth = 1;
                bCtx.beginPath();
                bCtx.moveTo(40, 110);
                bCtx.lineTo(600, 110);
                bCtx.stroke();

                bCtx.fillStyle = "#94a3b8";
                bCtx.font = "italic 11px 'Plus Jakarta Sans', Arial";
                bCtx.fillText("Dokumen ini diverifikasi secara digital oleh penanggung jawab:", 320, 140);

                // Load signature canvas image
                var sigImg = new Image();
                sigImg.onload = function() {
                    bCtx.drawImage(sigImg, 160, 160, 320, 160);

                    // Signer footer details
                    bCtx.fillStyle = "#0f172a";
                    bCtx.font = "bold 12px 'Plus Jakarta Sans', Arial";
                    bCtx.fillText("YANG MEMBUAT PERNYATAAN / WALI", 320, 360);

                    bCtx.font = "bold 14px 'Plus Jakarta Sans', Arial";
                    bCtx.fillText("<?=strtoupper($nama_pj);?>", 320, 395);

                    bCtx.fillStyle = "#475569";
                    bCtx.font = "normal 11px 'Plus Jakarta Sans', Arial";
                    bCtx.fillText("Jombang, " + "<?=$tanggal;?>", 320, 415);

                    var blendedDataUrl = blendCanvas.toDataURL("image/jpeg", 0.95);
                    document.getElementById("image-base64").value = blendedDataUrl;

                    stopCameraStream();
                    document.getElementById("consent-form").submit();
                };
                sigImg.src = sigCanvas.toDataURL();

            } else if (method === "cam") {
                // METHOD B: Camera Only
                if (!snapshotBase64) {
                    alert("Harap ambil foto wajah verifikasi terlebih dahulu.");
                    return false;
                }

                // Final output image is just the captured camera snapshot
                document.getElementById("image-base64").value = snapshotBase64;
                
                stopCameraStream();
                document.getElementById("consent-form").submit();
            }
        }
    </script>
</body>
</html>