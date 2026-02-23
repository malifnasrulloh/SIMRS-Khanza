<?php
//fitur update kamar aplicare ini adalah penyempurnaan dari kontribusi Mas Tirta dari RSUK Ciracas Jakarta Timur
session_start();
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
<!doctype html>
<html lang="en">

<head>

    <title>Layar Informasi</title>

    <!-- Meta START -->
    <link rel="icon" href="assets/img/rs.png" type="image/x-icon">
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no" />
    <link type="text/css" rel="stylesheet" href="assets/css/materialize.min.css" media="screen,projection" />
    <link type="text/css" rel="stylesheet" href="assets/css/jquery-ui.css" media="screen,projection" />
    <link rel="stylesheet" href="assets/css/marquee.css" />
    <link rel="stylesheet" href="assets/css/example.css" />
    <link rel="stylesheet" href="assets/css/ok.css" />
    <style type="text/css">
        body {
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            background: linear-gradient(135deg, #e3f2fd, #bbdefb);
        }

        .bg::before {
            content: '';
            background-image: url('./assets/img/background.jpg');
            background-size: cover;
            background-repeat: no-repeat;
            background-attachment: fixed;
            position: fixed;
            z-index: -1;
            top: 0;
            bottom: 0;
            left: 0;
            right: 0;
            opacity: 0.05;
            filter: blur(4px);
        }

        /* ===== NAVBAR ===== */
        nav {
            background: linear-gradient(45deg, #42a5f5, #1e88e5);
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
        }

        .nav-wrapper a {
            font-weight: 600;
            font-size: 18px;
            letter-spacing: 1px;
        }

        /* ===== HEADER CARD ===== */
        #header-instansi .card {
            border-radius: 20px;
            background: linear-gradient(135deg, #42a5f5, #1976d2);
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.25);
            padding: 15px;
        }

        .logo {
            width: 95px;
            height: 95px;
            border-radius: 50%;
            border: 4px solid white;
            margin-right: 20px;
            object-fit: cover;
            box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
        }

        .ins {
            font-weight: 700;
            font-size: 30px;
            margin-bottom: 5px;
        }

        .almt {
            font-size: 15px;
            opacity: 0.95;
        }

        /* ===== VIDEO ===== */
        .player {
            width: 100%;
            border-radius: 20px;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
        }

        /* ===== DATE TIME ===== */
        #header-date,
        #header-time {
            font-size: 20px;
            font-weight: bold;
        }

        /* ===== DATA CONTAINER ===== */
        #data {
            margin-top: 25px;
            padding: 20px;
            border-radius: 20px;
            background: rgba(255, 255, 255, 0.95);
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
            min-height: 250px;
        }

        /* ====== RUANG RANAP TABLE ====== */
        h5.center {
            font-weight: 700;
            font-size: 28px;
            color: #1565c0;
            margin-bottom: 25px;
            letter-spacing: 1px;
        }

        .default {
            width: 100%;
            border-collapse: collapse;
            background: #ffffff;
            border-radius: 20px;
            overflow: hidden;
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
        }

        .default thead {
            background: linear-gradient(45deg, #64b5f6, #1e88e5);
            color: white;
            font-size: 18px;
        }

        .default th {
            padding: 16px;
            text-align: center;
        }

        .default td {
            padding: 16px;
            font-size: 18px;
            text-align: center;
        }

        .default tbody tr:nth-child(even) {
            background-color: #e3f2fd;
        }

        tbody tr:hover {
            background-color: #bbdefb;
            transform: scale(1.01);
            transition: 0.3s ease;
        }

        tbody tr {
            transition: all 0.3s ease;
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(15px);
            }

            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        /* ===== FOOTER ===== */
        .page-footer {
            margin-top: 20px;
        }

        .footer-copyright {
            font-size: 18px;
            font-weight: 600;
            letter-spacing: 1px;
            background: linear-gradient(45deg, #42a5f5, #1565c0);
        }

        .marquee {
            margin-left: 5%;
        }

        .marquee-sibling {
            display: contents;
        }

        .marquee-sibling,
        .simple-marquee-container {
            font-size: 18px;
            font-weight: 600;
            letter-spacing: 1px;
            background-color: transparent;
        }

        .marquee-content-items {
            font-size: 18px;
            font-weight: bold;
            color: #fff;
        }

        /* ===== Animations ===== */
        #header-instansi,
        .player {
            animation: fadeInUp 1s ease-in-out;
        }

        @keyframes fadeInUp {
            from {
                opacity: 0;
                transform: translateY(20px);
            }

            to {
                opacity: 1;
                transform: translateY(0);
            }
        }
    </style>
    <!-- Global style END -->

</head>

<!-- Body START -->

<body class="bg">

    <!-- Header START -->
    <header>

        <nav class="accent-3">
            <div class="nav-wrapper">
                <ul class="center hide-on-med-and-down" id="nv">
                    <li>
                        <a href="./" class="ams hide-on-med-and-down"><i class="material-icons md-36">local_hospital</i> Informasi</a>
                    </li>
                    <li class="right" style="margin-right: 10px;">
                        <i class="material-icons">perm_contact_calendar</i>
                        <a href="" class="white-text" id="header-date"></a>
                        <i class="material-icons md-12">query_builder</i>
                        <a href="" class="white-text" id="header-time"></a>
                    </li>
                </ul>
            </div>
        </nav>

    </header>
    <!-- Header END -->

    <!-- Main START -->
    <main>

        <!-- container START -->
        <div class="container-fluid">
            <!-- Row START -->
            <div class="row">
                <?php $setting =  mysqli_fetch_array(bukaquery("select setting.nama_instansi,setting.alamat_instansi,setting.kabupaten,setting.propinsi,setting.kontak,setting.email,setting.logo from setting"));
                ?>
                <div class="col s8" id="header-instansi">
                    <div class="card accent-3 white-text">
                        <div class="card-content">
                            <div class="left">
                                <img class="logo" src="data:image/jpeg;base64,<?php echo base64_encode($setting['logo']); ?>" />
                            </div>
                            <h5 class="ins"><?php echo $setting["nama_instansi"] ?></h5>
                            <p class="almt"><?php echo $setting["alamat_instansi"] ?>, <?php echo $setting["kabupaten"] ?>, <?php echo $setting["propinsi"] ?>, <?php echo $setting["kontak"] ?>, <?php echo $setting["email"] ?>

                            </p>
                        </div>
                    </div>
                </div>
                <!-- <div class="col s4">
                    <video autoplay class="player">
                        <source src="assets/wew.mp4" type="video/mp4">
                    </video>
                </div> -->
            </div>
            <!-- Row END -->
        </div>
        <!-- container END -->
        <div class="container-fluid" id="data"></div>

    </main>
    <!-- Main END -->

    <!-- Include Footer START -->

    <!-- Footer START -->
    <footer class="page-footer">
        <div class="footer-copyright white-text">
            <div class="container simple-marquee-container">
                <div class="marquee-sibling">
                    Tarif Kamar Umum
                </div>
                <marquee class="marquee" scrollamount="4">
                    <?php
                    $sql = "SELECT kelas, trf_kamar FROM kamar WHERE statusdata='1' GROUP BY kelas";
                    $hasil = bukaquery($sql);
                    while ($data = mysqli_fetch_array($hasil)) {
                    ?>
                        <span class="marquee-content-items">| <?= $data['kelas']; ?> Rp <?= number_format($data['trf_kamar'], 0, ".", ","); ?></span>
                    <?php } ?>
                </marquee>
            </div>
        </div>
    </footer>
    <!-- Footer END -->

    <!-- Javascript START -->
    <script type="text/javascript" src="assets/js/jquery-2.1.1.min.js"></script>
    <script type="text/javascript" src="assets/js/materialize.min.js"></script>
    <script type="text/javascript" src="assets/js/jquery-ui.min.js"></script>
    <script type="text/javascript" src="assets/js/bootstrap.min.js"></script>
    <script data-pace-options='{ "ajax": false }' src='assets/js/pace.min.js'></script>
    <script type="text/javascript" src="assets/js/marquee.js"></script>
    <script type="text/javascript">
        window.onload = function() {
            jam();
        }

        function jam() {
            var ht = document.getElementById('header-time'),
                hd = document.getElementById('header-date'),
                date = new Date()

            var d = date.toLocaleDateString("id-ID", {
                weekday: 'long',
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            })

            var t = date.toLocaleTimeString("id-ID", {
                hour12: false,
                hour: '2-digit',
                minute: '2-digit',
                second: "2-digit"
            });

            hd.innerHTML = d
            ht.innerHTML = t;

            setTimeout('jam()', 1000);
        }
    </script>

    <script type="text/javascript" src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.0/jquery.min.js"></script>
    <script type="text/javascript">
        var auto_refresh = setInterval(function() {
            $('#data').load('data_jadwaloperasi.php');
        }, 5000);
    </script>

</body>
<!-- Body END -->

</html>