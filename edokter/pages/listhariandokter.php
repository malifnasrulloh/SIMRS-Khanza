<?php
    if (!isset($_SESSION["ses_dokter"])) {
        JSRedirect("index.php?act=Home");
        exit;
    }

    $kd_dokter = validTeks4(encrypt_decrypt($_SESSION["ses_dokter"], "d"), 20);

    // Filter defaults
    $tgl1 = isset($_POST['tgl1']) ? validTeks($_POST['tgl1']) : (isset($_GET['tgl1']) ? validTeks($_GET['tgl1']) : date('Y-m-d'));
    $tgl2 = isset($_POST['tgl2']) ? validTeks($_POST['tgl2']) : (isset($_GET['tgl2']) ? validTeks($_GET['tgl2']) : date('Y-m-d'));
    $status = isset($_POST['status']) ? validTeks($_POST['status']) : (isset($_GET['status']) ? validTeks($_GET['status']) : 'Semua');
    $kd_pj = isset($_POST['kd_pj']) ? validTeks($_POST['kd_pj']) : (isset($_GET['kd_pj']) ? validTeks($_GET['kd_pj']) : 'Semua');

    // Checklist categories (defaulting to checked)
    $show_rj = isset($_POST['submit']) || isset($_POST['tgl1']) ? (isset($_POST['show_rj']) ? 1 : 0) : 1;
    $show_ri = isset($_POST['submit']) || isset($_POST['tgl1']) ? (isset($_POST['show_ri']) ? 1 : 0) : 1;
    $show_op = isset($_POST['submit']) || isset($_POST['tgl1']) ? (isset($_POST['show_op']) ? 1 : 0) : 1;
    $show_lab = isset($_POST['submit']) || isset($_POST['tgl1']) ? (isset($_POST['show_lab']) ? 1 : 0) : 1;
    $show_rad = isset($_POST['submit']) || isset($_POST['tgl1']) ? (isset($_POST['show_rad']) ? 1 : 0) : 1;

    // Database connection check and escaping
    $db_connection = bukakoneksi();
    $esc_kd_dokter = mysqli_real_escape_string($db_connection, $kd_dokter);
    $esc_tgl1 = mysqli_real_escape_string($db_connection, $tgl1);
    $esc_tgl2 = mysqli_real_escape_string($db_connection, $tgl2);
    $esc_tgl1_dt = $esc_tgl1 . ' 00:00:00';
    $esc_tgl2_dt = $esc_tgl2 . ' 23:59:59';

    // Cara bayar filter
    $cara_bayar_where = "";
    if ($kd_pj !== 'Semua') {
        $esc_kd_pj = mysqli_real_escape_string($db_connection, $kd_pj);
        $cara_bayar_where = " and reg_periksa.kd_pj = '{$esc_kd_pj}' ";
    }

    // Helper function for status join/where (same logic as Java)
    if (!function_exists('getStatusQueryParts')) {
        function getStatusQueryParts($status, $regTableAlias = 'reg_periksa') {
            $join = "";
            $where = "";
            if ($status === 'Piutang Belum Lunas') {
                $join = " inner join piutang_pasien on {$regTableAlias}.no_rawat = piutang_pasien.no_rawat ";
                $where = " and {$regTableAlias}.status_bayar = 'Sudah Bayar' and piutang_pasien.status = 'Belum Lunas' ";
            } else if ($status === 'Piutang Sudah Lunas') {
                $join = " inner join piutang_pasien on {$regTableAlias}.no_rawat = piutang_pasien.no_rawat ";
                $where = " and {$regTableAlias}.status_bayar = 'Sudah Bayar' and piutang_pasien.status = 'Lunas' ";
            } else if ($status === 'Sudah Bayar Non Piutang') {
                $where = " and {$regTableAlias}.status_bayar = 'Sudah Bayar' and {$regTableAlias}.no_rawat not in (select no_rawat from piutang_pasien) ";
            } else if ($status === 'Belum Terclosing Kasir') {
                $where = " and {$regTableAlias}.status_bayar = 'Belum Bayar' ";
            }
            return ['join' => $join, 'where' => $where];
        }
    }

    $status_parts = getStatusQueryParts($status, 'reg_periksa');
    $status_join = $status_parts['join'];
    $status_where = $status_parts['where'];

    $transactions = [];
    $total_rj = 0;
    $total_ri = 0;
    $total_op = 0;
    $total_lab = 0;
    $total_rad = 0;

    // 1. RAWAT JALAN
    if ($show_rj) {
        // rawat_jl_dr
        $sql_rj1 = "SELECT pasien.nm_pasien, rawat_jl_dr.tarif_tindakandr AS tarif, jns_perawatan.nm_perawatan, 
                           rawat_jl_dr.tgl_perawatan AS tgl, rawat_jl_dr.jam_rawat AS jam, reg_periksa.kd_pj, 
                           rawat_jl_dr.kd_jenis_prw AS kd_tindakan, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                           'Rawat Jalan (Dokter)' AS tipe, 'RJ' AS kat_code
                    FROM pasien 
                    INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis 
                    INNER JOIN rawat_jl_dr ON rawat_jl_dr.no_rawat = reg_periksa.no_rawat 
                    INNER JOIN jns_perawatan ON rawat_jl_dr.kd_jenis_prw = jns_perawatan.kd_jenis_prw 
                    INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj 
                    {$status_join}
                    WHERE concat(reg_periksa.tgl_registrasi, ' ', reg_periksa.jam_reg) BETWEEN '{$esc_tgl1_dt}' AND '{$esc_tgl2_dt}' 
                      AND rawat_jl_dr.kd_dokter = '{$esc_kd_dokter}' 
                      AND rawat_jl_dr.tarif_tindakandr > 0
                      {$status_where}
                      {$cara_bayar_where}
                    ORDER BY reg_periksa.tgl_registrasi, jns_perawatan.nm_perawatan";
        $res_rj1 = bukaquery2($sql_rj1);
        if ($res_rj1) {
            while ($row = mysqli_fetch_array($res_rj1)) {
                $transactions[] = $row;
                $total_rj += $row['tarif'];
            }
        }

        // rawat_jl_drpr
        $sql_rj2 = "SELECT pasien.nm_pasien, rawat_jl_drpr.tarif_tindakandr AS tarif, jns_perawatan.nm_perawatan, 
                           rawat_jl_drpr.tgl_perawatan AS tgl, rawat_jl_drpr.jam_rawat AS jam, reg_periksa.kd_pj, 
                           rawat_jl_drpr.kd_jenis_prw AS kd_tindakan, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                           'Rawat Jalan (Dokter & Perawat)' AS tipe, 'RJ' AS kat_code
                    FROM pasien 
                    INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis 
                    INNER JOIN rawat_jl_drpr ON rawat_jl_drpr.no_rawat = reg_periksa.no_rawat 
                    INNER JOIN jns_perawatan ON rawat_jl_drpr.kd_jenis_prw = jns_perawatan.kd_jenis_prw 
                    INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj 
                    {$status_join}
                    WHERE concat(reg_periksa.tgl_registrasi, ' ', reg_periksa.jam_reg) BETWEEN '{$esc_tgl1_dt}' AND '{$esc_tgl2_dt}' 
                      AND rawat_jl_drpr.kd_dokter = '{$esc_kd_dokter}' 
                      AND rawat_jl_drpr.tarif_tindakandr > 0
                      {$status_where}
                      {$cara_bayar_where}
                    ORDER BY reg_periksa.tgl_registrasi, jns_perawatan.nm_perawatan";
        $res_rj2 = bukaquery2($sql_rj2);
        if ($res_rj2) {
            while ($row = mysqli_fetch_array($res_rj2)) {
                $transactions[] = $row;
                $total_rj += $row['tarif'];
            }
        }
    }

    // 2. RAWAT INAP
    if ($show_ri) {
        // rawat_inap_dr
        $sql_ri1 = "SELECT pasien.nm_pasien, rawat_inap_dr.tarif_tindakandr AS tarif, jns_perawatan_inap.nm_perawatan, 
                           rawat_inap_dr.tgl_perawatan AS tgl, rawat_inap_dr.jam_rawat AS jam, reg_periksa.kd_pj, 
                           rawat_inap_dr.kd_jenis_prw AS kd_tindakan, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                           'Rawat Inap (Dokter)' AS tipe, 'RI' AS kat_code
                    FROM pasien 
                    INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis 
                    INNER JOIN rawat_inap_dr ON rawat_inap_dr.no_rawat = reg_periksa.no_rawat 
                    INNER JOIN jns_perawatan_inap ON rawat_inap_dr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw 
                    INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj 
                    {$status_join}
                    WHERE concat(rawat_inap_dr.tgl_perawatan, ' ', rawat_inap_dr.jam_rawat) BETWEEN '{$esc_tgl1_dt}' AND '{$esc_tgl2_dt}' 
                      AND rawat_inap_dr.kd_dokter = '{$esc_kd_dokter}' 
                      AND rawat_inap_dr.tarif_tindakandr > 0
                      {$status_where}
                      {$cara_bayar_where}
                    ORDER BY rawat_inap_dr.tgl_perawatan, rawat_inap_dr.jam_rawat, jns_perawatan_inap.nm_perawatan";
        $res_ri1 = bukaquery2($sql_ri1);
        if ($res_ri1) {
            while ($row = mysqli_fetch_array($res_ri1)) {
                $transactions[] = $row;
                $total_ri += $row['tarif'];
            }
        }

        // rawat_inap_drpr
        $sql_ri2 = "SELECT pasien.nm_pasien, rawat_inap_drpr.tarif_tindakandr AS tarif, jns_perawatan_inap.nm_perawatan, 
                           rawat_inap_drpr.tgl_perawatan AS tgl, rawat_inap_drpr.jam_rawat AS jam, reg_periksa.kd_pj, 
                           rawat_inap_drpr.kd_jenis_prw AS kd_tindakan, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                           'Rawat Inap (Dokter & Perawat)' AS tipe, 'RI' AS kat_code
                    FROM pasien 
                    INNER JOIN reg_periksa ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis 
                    INNER JOIN rawat_inap_drpr ON rawat_inap_drpr.no_rawat = reg_periksa.no_rawat 
                    INNER JOIN jns_perawatan_inap ON rawat_inap_drpr.kd_jenis_prw = jns_perawatan_inap.kd_jenis_prw 
                    INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj 
                    {$status_join}
                    WHERE concat(rawat_inap_drpr.tgl_perawatan, ' ', rawat_inap_drpr.jam_rawat) BETWEEN '{$esc_tgl1_dt}' AND '{$esc_tgl2_dt}' 
                      AND rawat_inap_drpr.kd_dokter = '{$esc_kd_dokter}' 
                      AND rawat_inap_drpr.tarif_tindakandr > 0
                      {$status_where}
                      {$cara_bayar_where}
                    ORDER BY rawat_inap_drpr.tgl_perawatan, rawat_inap_drpr.jam_rawat, jns_perawatan_inap.nm_perawatan";
        $res_ri2 = bukaquery2($sql_ri2);
        if ($res_ri2) {
            while ($row = mysqli_fetch_array($res_ri2)) {
                $transactions[] = $row;
                $total_ri += $row['tarif'];
            }
        }
    }

    // 3. OPERASI / VK
    if ($show_op) {
        $operasi_roles = [
            ['doc_field' => 'operator1', 'fee_field' => 'biayaoperator1', 'label' => 'Operator 1'],
            ['doc_field' => 'operator2', 'fee_field' => 'biayaoperator2', 'label' => 'Operator 2'],
            ['doc_field' => 'operator3', 'fee_field' => 'biayaoperator3', 'label' => 'Operator 3'],
            ['doc_field' => 'dokter_anak', 'fee_field' => 'biayadokter_anak', 'label' => 'dr Anak'],
            ['doc_field' => 'dokter_anestesi', 'fee_field' => 'biayadokter_anestesi', 'label' => 'dr Anestesi'],
            ['doc_field' => 'dokter_pjanak', 'fee_field' => 'biaya_dokter_pjanak', 'label' => 'dr Pj Anak'],
            ['doc_field' => 'dokter_umum', 'fee_field' => 'biaya_dokter_umum', 'label' => 'dr Umum']
        ];

        foreach ($operasi_roles as $role) {
            $sql_op = "SELECT pasien.nm_pasien, paket_operasi.nm_perawatan, operasi.{$role['fee_field']} AS tarif, 
                              operasi.tgl_operasi AS tgl, '00:00:00' AS jam, reg_periksa.kd_pj, 
                              operasi.kode_paket AS kd_tindakan, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                              'Operasi ({$role['label']})' AS tipe, 'OP' AS kat_code
                       FROM operasi 
                       INNER JOIN reg_periksa ON operasi.no_rawat = reg_periksa.no_rawat 
                       INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis 
                       INNER JOIN paket_operasi ON operasi.kode_paket = paket_operasi.kode_paket 
                       INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj 
                       {$status_join}
                       WHERE operasi.tgl_operasi BETWEEN '{$esc_tgl1}' AND '{$esc_tgl2}' 
                         AND operasi.{$role['doc_field']} = '{$esc_kd_dokter}' 
                         AND operasi.{$role['fee_field']} > 0
                         {$status_where}
                         {$cara_bayar_where}
                       ORDER BY operasi.tgl_operasi, paket_operasi.nm_perawatan";
            $res_op = bukaquery2($sql_op);
            if ($res_op) {
                while ($row = mysqli_fetch_array($res_op)) {
                    $transactions[] = $row;
                    $total_op += $row['tarif'];
                }
            }
        }
    }

    // 4. LABORATORIUM
    if ($show_lab) {
        // periksa_lab (dokter)
        $sql_lab1 = "SELECT pasien.nm_pasien, jns_perawatan_lab.nm_perawatan, periksa_lab.tarif_tindakan_dokter AS tarif, 
                           periksa_lab.tgl_periksa AS tgl, periksa_lab.jam, reg_periksa.kd_pj, 
                           periksa_lab.kd_jenis_prw AS kd_tindakan, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                           'Lab (Pemeriksaan Dokter)' AS tipe, 'LAB' AS kat_code
                    FROM periksa_lab 
                    INNER JOIN reg_periksa ON periksa_lab.no_rawat = reg_periksa.no_rawat 
                    INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis 
                    INNER JOIN jns_perawatan_lab ON periksa_lab.kd_jenis_prw = jns_perawatan_lab.kd_jenis_prw 
                    INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj 
                    {$status_join}
                    WHERE concat(periksa_lab.tgl_periksa, ' ', periksa_lab.jam) BETWEEN '{$esc_tgl1_dt}' AND '{$esc_tgl2_dt}' 
                      AND periksa_lab.kd_dokter = '{$esc_kd_dokter}' 
                      AND periksa_lab.tarif_tindakan_dokter > 0
                      {$status_where}
                      {$cara_bayar_where}
                    ORDER BY periksa_lab.tgl_periksa, periksa_lab.jam, jns_perawatan_lab.nm_perawatan";
        $res_lab1 = bukaquery2($sql_lab1);
        if ($res_lab1) {
            while ($row = mysqli_fetch_array($res_lab1)) {
                $transactions[] = $row;
                $total_lab += $row['tarif'];
            }
        }

        // detail_periksa_lab (bagian_dokter)
        $sql_lab2 = "SELECT pasien.nm_pasien, template_laboratorium.Pemeriksaan AS nm_perawatan, detail_periksa_lab.bagian_dokter AS tarif, 
                           periksa_lab.tgl_periksa AS tgl, periksa_lab.jam, reg_periksa.kd_pj, 
                           periksa_lab.kd_jenis_prw AS kd_tindakan, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                           'Lab (Detail Pemeriksaan)' AS tipe, 'LAB' AS kat_code
                    FROM detail_periksa_lab 
                    INNER JOIN periksa_lab ON periksa_lab.no_rawat = detail_periksa_lab.no_rawat 
                                          AND periksa_lab.kd_jenis_prw = detail_periksa_lab.kd_jenis_prw 
                                          AND periksa_lab.tgl_periksa = detail_periksa_lab.tgl_periksa 
                                          AND periksa_lab.jam = detail_periksa_lab.jam
                    INNER JOIN reg_periksa ON periksa_lab.no_rawat = reg_periksa.no_rawat 
                    INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis 
                    INNER JOIN template_laboratorium ON detail_periksa_lab.id_template = template_laboratorium.id_template
                    INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj 
                    {$status_join}
                    WHERE concat(detail_periksa_lab.tgl_periksa, ' ', detail_periksa_lab.jam) BETWEEN '{$esc_tgl1_dt}' AND '{$esc_tgl2_dt}' 
                      AND periksa_lab.kd_dokter = '{$esc_kd_dokter}' 
                      AND detail_periksa_lab.bagian_dokter > 0
                      {$status_where}
                      {$cara_bayar_where}
                    ORDER BY periksa_lab.tgl_periksa, periksa_lab.jam";
        $res_lab2 = bukaquery2($sql_lab2);
        if ($res_lab2) {
            while ($row = mysqli_fetch_array($res_lab2)) {
                $transactions[] = $row;
                $total_lab += $row['tarif'];
            }
        }

        // periksa_lab (perujuk)
        $sql_lab3 = "SELECT pasien.nm_pasien, jns_perawatan_lab.nm_perawatan, periksa_lab.tarif_perujuk AS tarif, 
                           periksa_lab.tgl_periksa AS tgl, periksa_lab.jam, reg_periksa.kd_pj, 
                           periksa_lab.kd_jenis_prw AS kd_tindakan, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                           'Lab (Perujuk)' AS tipe, 'LAB' AS kat_code
                    FROM periksa_lab 
                    INNER JOIN reg_periksa ON periksa_lab.no_rawat = reg_periksa.no_rawat 
                    INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis 
                    INNER JOIN jns_perawatan_lab ON periksa_lab.kd_jenis_prw = jns_perawatan_lab.kd_jenis_prw 
                    INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj 
                    {$status_join}
                    WHERE concat(periksa_lab.tgl_periksa, ' ', periksa_lab.jam) BETWEEN '{$esc_tgl1_dt}' AND '{$esc_tgl2_dt}' 
                      AND periksa_lab.dokter_perujuk = '{$esc_kd_dokter}' 
                      AND periksa_lab.tarif_perujuk > 0
                      {$status_where}
                      {$cara_bayar_where}
                    ORDER BY periksa_lab.tgl_periksa, periksa_lab.jam, jns_perawatan_lab.nm_perawatan";
        $res_lab3 = bukaquery2($sql_lab3);
        if ($res_lab3) {
            while ($row = mysqli_fetch_array($res_lab3)) {
                $transactions[] = $row;
                $total_lab += $row['tarif'];
            }
        }

        // detail_periksa_lab (bagian_perujuk)
        $sql_lab4 = "SELECT pasien.nm_pasien, template_laboratorium.Pemeriksaan AS nm_perawatan, detail_periksa_lab.bagian_perujuk AS tarif, 
                           periksa_lab.tgl_periksa AS tgl, periksa_lab.jam, reg_periksa.kd_pj, 
                           periksa_lab.kd_jenis_prw AS kd_tindakan, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                           'Lab (Detail Perujuk)' AS tipe, 'LAB' AS kat_code
                    FROM detail_periksa_lab 
                    INNER JOIN periksa_lab ON periksa_lab.no_rawat = detail_periksa_lab.no_rawat 
                                          AND periksa_lab.kd_jenis_prw = detail_periksa_lab.kd_jenis_prw 
                                          AND periksa_lab.tgl_periksa = detail_periksa_lab.tgl_periksa 
                                          AND periksa_lab.jam = detail_periksa_lab.jam
                    INNER JOIN reg_periksa ON periksa_lab.no_rawat = reg_periksa.no_rawat 
                    INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis 
                    INNER JOIN template_laboratorium ON detail_periksa_lab.id_template = template_laboratorium.id_template
                    INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj 
                    {$status_join}
                    WHERE concat(detail_periksa_lab.tgl_periksa, ' ', detail_periksa_lab.jam) BETWEEN '{$esc_tgl1_dt}' AND '{$esc_tgl2_dt}' 
                      AND periksa_lab.dokter_perujuk = '{$esc_kd_dokter}' 
                      AND detail_periksa_lab.bagian_perujuk > 0
                      {$status_where}
                      {$cara_bayar_where}
                    ORDER BY periksa_lab.tgl_periksa, periksa_lab.jam";
        $res_lab4 = bukaquery2($sql_lab4);
        if ($res_lab4) {
            while ($row = mysqli_fetch_array($res_lab4)) {
                $transactions[] = $row;
                $total_lab += $row['tarif'];
            }
        }
    }

    // 5. RADIOLOGI
    if ($show_rad) {
        // periksa_radiologi (dokter)
        $sql_rad1 = "SELECT pasien.nm_pasien, jns_perawatan_radiologi.nm_perawatan, periksa_radiologi.tarif_tindakan_dokter AS tarif, 
                           periksa_radiologi.tgl_periksa AS tgl, periksa_radiologi.jam, reg_periksa.kd_pj, 
                           periksa_radiologi.kd_jenis_prw AS kd_tindakan, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                           'Radiologi (Pemeriksaan Dokter)' AS tipe, 'RAD' AS kat_code
                    FROM periksa_radiologi 
                    INNER JOIN reg_periksa ON periksa_radiologi.no_rawat = reg_periksa.no_rawat 
                    INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis 
                    INNER JOIN jns_perawatan_radiologi ON periksa_radiologi.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw 
                    INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj 
                    {$status_join}
                    WHERE concat(periksa_radiologi.tgl_periksa, ' ', periksa_radiologi.jam) BETWEEN '{$esc_tgl1_dt}' AND '{$esc_tgl2_dt}' 
                      AND periksa_radiologi.kd_dokter = '{$esc_kd_dokter}' 
                      AND periksa_radiologi.tarif_tindakan_dokter > 0
                      {$status_where}
                      {$cara_bayar_where}
                    ORDER BY periksa_radiologi.tgl_periksa, periksa_radiologi.jam, jns_perawatan_radiologi.nm_perawatan";
        $res_rad1 = bukaquery2($sql_rad1);
        if ($res_rad1) {
            while ($row = mysqli_fetch_array($res_rad1)) {
                $transactions[] = $row;
                $total_rad += $row['tarif'];
            }
        }

        // periksa_radiologi (perujuk)
        $sql_rad2 = "SELECT pasien.nm_pasien, jns_perawatan_radiologi.nm_perawatan, periksa_radiologi.tarif_perujuk AS tarif, 
                           periksa_radiologi.tgl_periksa AS tgl, periksa_radiologi.jam, reg_periksa.kd_pj, 
                           periksa_radiologi.kd_jenis_prw AS kd_tindakan, reg_periksa.no_rawat, reg_periksa.no_rkm_medis,
                           'Radiologi (Perujuk)' AS tipe, 'RAD' AS kat_code
                    FROM periksa_radiologi 
                    INNER JOIN reg_periksa ON periksa_radiologi.no_rawat = reg_periksa.no_rawat 
                    INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis 
                    INNER JOIN jns_perawatan_radiologi ON periksa_radiologi.kd_jenis_prw = jns_perawatan_radiologi.kd_jenis_prw 
                    INNER JOIN penjab ON reg_periksa.kd_pj = penjab.kd_pj 
                    {$status_join}
                    WHERE concat(periksa_radiologi.tgl_periksa, ' ', periksa_radiologi.jam) BETWEEN '{$esc_tgl1_dt}' AND '{$esc_tgl2_dt}' 
                      AND periksa_radiologi.dokter_perujuk = '{$esc_kd_dokter}' 
                      AND periksa_radiologi.tarif_perujuk > 0
                      {$status_where}
                      {$cara_bayar_where}
                    ORDER BY periksa_radiologi.tgl_periksa, periksa_radiologi.jam, jns_perawatan_radiologi.nm_perawatan";
        $res_rad2 = bukaquery2($sql_rad2);
        if ($res_rad2) {
            while ($row = mysqli_fetch_array($res_rad2)) {
                $transactions[] = $row;
                $total_rad += $row['tarif'];
            }
        }
    }

    $grand_total = $total_rj + $total_ri + $total_op + $total_lab + $total_rad;

    // Fetch Cara Bayar options for filter
    $cara_bayar_options = [];
    $res_pj = bukaquery2("SELECT kd_pj, png_jawab FROM penjab ORDER BY png_jawab");
    if ($res_pj) {
        while ($pj_row = mysqli_fetch_array($res_pj)) {
            $cara_bayar_options[] = $pj_row;
        }
    }
?>

<div class="block-header">
    <h2><center>REKAP HARIAN JASA MEDIS DOKTER</center></h2>
</div>

<!-- Filter Form Card -->
<div class="row clearfix">
    <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
        <div class="card" style="border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.08);">
            <div class="header" style="border-bottom: 1px solid #eee; background-color: #fafafa; border-top-left-radius: 8px; border-top-right-radius: 8px; display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; padding: 15px 20px;">
                <h2 style="margin: 0; padding: 5px 0;">
                    <i class="material-icons" style="vertical-align: middle; margin-right: 5px;">filter_list</i> Filter Data Jasa Medis
                </h2>
                <div class="preset-buttons" style="padding: 5px 0;">
                    <button type="button" onclick="setDatePresetAndSubmit('today')" class="btn btn-xs bg-pink waves-effect" style="margin-right: 4px; border-radius: 3px; font-weight: bold; padding: 3px 8px;">Hari Ini</button>
                    <button type="button" onclick="setDatePresetAndSubmit('yesterday')" class="btn btn-xs bg-cyan waves-effect" style="margin-right: 4px; border-radius: 3px; font-weight: bold; padding: 3px 8px;">Kemarin</button>
                    <button type="button" onclick="setDatePresetAndSubmit('this_month')" class="btn btn-xs bg-light-green waves-effect" style="margin-right: 4px; border-radius: 3px; font-weight: bold; padding: 3px 8px;">Bulan Ini</button>
                    <button type="button" onclick="setDatePresetAndSubmit('last_month')" class="btn btn-xs bg-amber waves-effect" style="margin-right: 4px; border-radius: 3px; font-weight: bold; padding: 3px 8px;">Bulan Lalu</button>
                    <button type="button" onclick="setDatePresetAndSubmit('this_year')" class="btn btn-xs bg-purple waves-effect" style="border-radius: 3px; font-weight: bold; padding: 3px 8px;">Tahun Ini</button>
                </div>
            </div>
            <div class="body">
                <form action="index.php?act=HarianDokter" method="POST">
                    <div class="row clearfix">
                        <!-- Date Range Selection -->
                        <div class="col-md-3">
                            <label for="tgl1">Tanggal Mulai</label>
                            <div class="form-group">
                                <div class="form-line">
                                    <input type="date" id="tgl1" name="tgl1" class="form-control" value="<?=$tgl1;?>">
                                </div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <label for="tgl2">Tanggal Akhir</label>
                            <div class="form-group">
                                <div class="form-line">
                                    <input type="date" id="tgl2" name="tgl2" class="form-control" value="<?=$tgl2;?>">
                                </div>
                            </div>
                        </div>

                        <!-- Status Bayar Filter -->
                        <div class="col-md-3">
                            <label for="status">Status Closing Kasir</label>
                            <div class="form-group">
                                <div class="form-line">
                                    <select class="form-control show-tick" id="status" name="status">
                                        <option value="Semua" <?=$status=='Semua'?'selected':'';?>>Semua</option>
                                        <option value="Piutang Belum Lunas" <?=$status=='Piutang Belum Lunas'?'selected':'';?>>Piutang Belum Lunas</option>
                                        <option value="Piutang Sudah Lunas" <?=$status=='Piutang Sudah Lunas'?'selected':'';?>>Piutang Sudah Lunas</option>
                                        <option value="Sudah Bayar Non Piutang" <?=$status=='Sudah Bayar Non Piutang'?'selected':'';?>>Sudah Bayar Non Piutang</option>
                                        <option value="Belum Terclosing Kasir" <?=$status=='Belum Terclosing Kasir'?'selected':'';?>>Belum Terclosing Kasir</option>
                                    </select>
                                </div>
                            </div>
                        </div>

                        <!-- Cara Bayar / Penjab Filter -->
                        <div class="col-md-3">
                            <label for="kd_pj">Cara Bayar</label>
                            <div class="form-group">
                                <div class="form-line">
                                    <select class="form-control show-tick" id="kd_pj" name="kd_pj">
                                        <option value="Semua">Semua Cara Bayar</option>
                                        <?php foreach ($cara_bayar_options as $option): ?>
                                            <option value="<?=$option['kd_pj'];?>" <?=$kd_pj==$option['kd_pj']?'selected':'';?>><?=htmlspecialchars($option['png_jawab']);?></option>
                                        <?php endforeach; ?>
                                    </select>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Category Checkboxes -->
                    <div class="row clearfix" style="margin-top: 10px; margin-bottom: 10px;">
                        <div class="col-md-12">
                            <label style="margin-right: 15px; font-weight: bold; color: #555;">Kategori Tindakan:</label>
                            <input type="checkbox" id="show_rj" name="show_rj" class="filled-in chk-col-pink" <?=$show_rj?'checked':'';?>>
                            <label for="show_rj" style="margin-right: 15px;">Rawat Jalan</label>

                            <input type="checkbox" id="show_ri" name="show_ri" class="filled-in chk-col-cyan" <?=$show_ri?'checked':'';?>>
                            <label for="show_ri" style="margin-right: 15px;">Rawat Inap</label>

                            <input type="checkbox" id="show_op" name="show_op" class="filled-in chk-col-light-green" <?=$show_op?'checked':'';?>>
                            <label for="show_op" style="margin-right: 15px;">Operasi/VK</label>

                            <input type="checkbox" id="show_lab" name="show_lab" class="filled-in chk-col-amber" <?=$show_lab?'checked':'';?>>
                            <label for="show_lab" style="margin-right: 15px;">Laboratorium</label>

                            <input type="checkbox" id="show_rad" name="show_rad" class="filled-in chk-col-purple" <?=$show_rad?'checked':'';?>>
                            <label for="show_rad">Radiologi</label>
                        </div>
                    </div>

                    <div class="row clearfix">
                        <div class="col-md-12" style="text-align: right; margin-bottom: 0;">
                            <button type="submit" name="submit" class="btn bg-pink waves-effect" style="border-radius: 4px; padding: 6px 18px; font-weight: bold;">
                                <i class="material-icons" style="vertical-align: middle; margin-right: 5px;">search</i> CARI DATA
                            </button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- Summary and Chart Area -->
<div class="row clearfix">
    <!-- Left column: Summary Info Boxes (8 columns) -->
    <div class="col-lg-8 col-md-8 col-sm-12 col-xs-12">
        <div class="row clearfix">
            <!-- Rawat Jalan Card -->
            <div class="col-lg-4 col-md-4 col-sm-6 col-xs-12" style="padding-left: 5px; padding-right: 5px;">
                <div class="info-box bg-pink hover-expand-effect" style="border-radius: 8px; margin-bottom: 15px; height: 75px;">
                    <div class="icon" style="background-color: rgba(0,0,0,0.05); width: 60px; line-height: 75px; height: 75px;"><i class="material-icons" style="line-height: 75px;">airline_seat_recline_normal</i></div>
                    <div class="content" style="margin-left: 60px; padding: 10px 10px;">
                        <div class="text" style="font-weight: bold; font-size: 10px; margin-top: 2px;">RAWAT JALAN</div>
                        <div class="number" style="font-size: 13px; font-weight: bold; margin-top: 3px;"><?=formatDuit2($total_rj);?></div>
                    </div>
                </div>
            </div>
            <!-- Rawat Inap Card -->
            <div class="col-lg-4 col-md-4 col-sm-6 col-xs-12" style="padding-left: 5px; padding-right: 5px;">
                <div class="info-box bg-cyan hover-expand-effect" style="border-radius: 8px; margin-bottom: 15px; height: 75px;">
                    <div class="icon" style="background-color: rgba(0,0,0,0.05); width: 60px; line-height: 75px; height: 75px;"><i class="material-icons" style="line-height: 75px;">local_hotel</i></div>
                    <div class="content" style="margin-left: 60px; padding: 10px 10px;">
                        <div class="text" style="font-weight: bold; font-size: 10px; margin-top: 2px;">RAWAT INAP</div>
                        <div class="number" style="font-size: 13px; font-weight: bold; margin-top: 3px;"><?=formatDuit2($total_ri);?></div>
                    </div>
                </div>
            </div>
            <!-- Operasi Card -->
            <div class="col-lg-4 col-md-4 col-sm-6 col-xs-12" style="padding-left: 5px; padding-right: 5px;">
                <div class="info-box bg-light-green hover-expand-effect" style="border-radius: 8px; margin-bottom: 15px; height: 75px;">
                    <div class="icon" style="background-color: rgba(0,0,0,0.05); width: 60px; line-height: 75px; height: 75px;"><i class="material-icons" style="line-height: 75px;">content_cut</i></div>
                    <div class="content" style="margin-left: 60px; padding: 10px 10px;">
                        <div class="text" style="font-weight: bold; font-size: 10px; margin-top: 2px;">OPERASI / VK</div>
                        <div class="number" style="font-size: 13px; font-weight: bold; margin-top: 3px;"><?=formatDuit2($total_op);?></div>
                    </div>
                </div>
            </div>
            <!-- Lab Card -->
            <div class="col-lg-4 col-md-4 col-sm-6 col-xs-12" style="padding-left: 5px; padding-right: 5px;">
                <div class="info-box bg-amber hover-expand-effect" style="border-radius: 8px; margin-bottom: 15px; height: 75px;">
                    <div class="icon" style="background-color: rgba(0,0,0,0.05); width: 60px; line-height: 75px; height: 75px;"><i class="material-icons" style="line-height: 75px;">opacity</i></div>
                    <div class="content" style="margin-left: 60px; padding: 10px 10px;">
                        <div class="text" style="font-weight: bold; font-size: 10px; margin-top: 2px;">LABORATORIUM</div>
                        <div class="number" style="font-size: 13px; font-weight: bold; margin-top: 3px;"><?=formatDuit2($total_lab);?></div>
                    </div>
                </div>
            </div>
            <!-- Radiologi Card -->
            <div class="col-lg-4 col-md-4 col-sm-6 col-xs-12" style="padding-left: 5px; padding-right: 5px;">
                <div class="info-box bg-purple hover-expand-effect" style="border-radius: 8px; margin-bottom: 15px; height: 75px;">
                    <div class="icon" style="background-color: rgba(0,0,0,0.05); width: 60px; line-height: 75px; height: 75px;"><i class="material-icons" style="line-height: 75px;">settings_brightness</i></div>
                    <div class="content" style="margin-left: 60px; padding: 10px 10px;">
                        <div class="text" style="font-weight: bold; font-size: 10px; margin-top: 2px;">RADIOLOGI</div>
                        <div class="number" style="font-size: 13px; font-weight: bold; margin-top: 3px;"><?=formatDuit2($total_rad);?></div>
                    </div>
                </div>
            </div>
            <!-- Grand Total Card -->
            <div class="col-lg-4 col-md-4 col-sm-6 col-xs-12" style="padding-left: 5px; padding-right: 5px;">
                <div class="info-box bg-teal hover-expand-effect" style="border-radius: 8px; margin-bottom: 15px; height: 75px; box-shadow: 0 4px 15px rgba(0, 150, 136, 0.3);">
                    <div class="icon" style="background-color: rgba(0,0,0,0.08); width: 60px; line-height: 75px; height: 75px;"><i class="material-icons" style="line-height: 75px;">attach_money</i></div>
                    <div class="content" style="margin-left: 60px; padding: 10px 10px;">
                        <div class="text" style="font-weight: 800; font-size: 10px; letter-spacing: 0.5px; margin-top: 2px;">TOTAL JASA MEDIS</div>
                        <div class="number" style="font-size: 13px; font-weight: 800; color: #fff; margin-top: 3px;"><?=formatDuit2($grand_total);?></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <!-- Right column: Chart Card (4 columns) -->
    <div class="col-lg-4 col-md-4 col-sm-12 col-xs-12" style="padding-left: 5px; padding-right: 5px;">
        <div class="card" style="border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); margin-bottom: 15px; height: 165px;">
            <div class="header" style="border-bottom: 1px solid #eee; background-color: #fafafa; border-top-left-radius: 8px; border-top-right-radius: 8px; padding: 10px 15px;">
                <h2 style="font-size: 12px; font-weight: bold; color: #333; margin: 0;">
                    <i class="material-icons" style="vertical-align: middle; margin-right: 5px; font-size: 18px;">pie_chart</i>Proporsi Jasa Medis
                </h2>
            </div>
            <div class="body" style="padding: 10px; text-align: center; display: flex; justify-content: center; align-items: center; height: 110px;">
                <?php if ($grand_total > 0): ?>
                    <div style="width: 90px; height: 90px; display: inline-block;">
                        <canvas id="jmChart" width="90" height="90"></canvas>
                    </div>
                    <div id="chartLegend" style="display: inline-block; text-align: left; margin-left: 15px; font-size: 9px; line-height: 1.3;">
                        <div><span style="display:inline-block; width:6px; height:6px; background-color:#e91e63; margin-right:4px; border-radius:1px;"></span>RJ (<?=round($total_rj/$grand_total*100);?>%)</div>
                        <div><span style="display:inline-block; width:6px; height:6px; background-color:#00bcd4; margin-right:4px; border-radius:1px;"></span>RI (<?=round($total_ri/$grand_total*100);?>%)</div>
                        <div><span style="display:inline-block; width:6px; height:6px; background-color:#8bc34a; margin-right:4px; border-radius:1px;"></span>OP (<?=round($total_op/$grand_total*100);?>%)</div>
                        <div><span style="display:inline-block; width:6px; height:6px; background-color:#ffc107; margin-right:4px; border-radius:1px;"></span>LAB (<?=round($total_lab/$grand_total*100);?>%)</div>
                        <div><span style="display:inline-block; width:6px; height:6px; background-color:#9c27b0; margin-right:4px; border-radius:1px;"></span>RAD (<?=round($total_rad/$grand_total*100);?>%)</div>
                    </div>
                <?php else: ?>
                    <div style="color: #999; font-style: italic; font-size: 11px; padding: 20px;">Belum ada data untuk grafik</div>
                <?php endif; ?>
            </div>
        </div>
    </div>
</div>

<!-- Detailed Data Table Card -->
<div class="row clearfix">
    <div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
        <div class="card" style="border-radius: 8px; box-shadow: 0 4px 20px rgba(0,0,0,0.08);">
            <div class="header" style="border-bottom: 1px solid #eee; border-top-left-radius: 8px; border-top-right-radius: 8px; padding: 15px 20px;">
                <h2>
                    Rincian Transaksi Jasa Medis Dokter
                    <small>Periode: <strong><?=konversiTanggal($tgl1);?></strong> s.d. <strong><?=konversiTanggal($tgl2);?></strong> | Status Closing: <strong><?=$status;?></strong></small>
                </h2>
            </div>
            <div class="body">
                <div class="table-responsive">
                    <table class="table table-bordered table-striped table-hover js-exportable dataTable">
                        <thead>
                            <tr>
                                <th width="4%"><center>No</center></th>
                                <th width="12%"><center>Tanggal & Jam</center></th>
                                <th width="12%"><center>Kategori</center></th>
                                <th width="15%"><center>No. Rawat / RM</center></th>
                                <th width="20%"><center>Nama Pasien (Cara Bayar)</center></th>
                                <th width="25%"><center>Nama Perawatan (Peran)</center></th>
                                <th width="12%"><center>Jasa Medis (Rp)</center></th>
                            </tr>
                        </thead>
                        <tbody>
                            <?php 
                            $no = 1;
                            foreach ($transactions as $tx): 
                                // Color badges for categories
                                $badge_class = 'bg-grey';
                                if ($tx['kat_code'] === 'RJ') $badge_class = 'bg-pink';
                                else if ($tx['kat_code'] === 'RI') $badge_class = 'bg-cyan';
                                else if ($tx['kat_code'] === 'OP') $badge_class = 'bg-light-green';
                                else if ($tx['kat_code'] === 'LAB') $badge_class = 'bg-amber';
                                else if ($tx['kat_code'] === 'RAD') $badge_class = 'bg-purple';
                            ?>
                                <tr>
                                    <td><center><?=$no++;?></center></td>
                                    <td><?=htmlspecialchars($tx['tgl']);?> <?=htmlspecialchars($tx['jam']);?></td>
                                    <td>
                                        <center><span class="label <?=$badge_class;?>" style="padding: 4px 8px; border-radius: 3px; font-weight: bold; font-size: 10px;"><?=htmlspecialchars($tx['tipe']);?></span></center>
                                    </td>
                                    <td>
                                        <strong><?=htmlspecialchars($tx['no_rawat']);?></strong><br>
                                        <span style="font-size: 11px; color: #777;">RM: <?=htmlspecialchars($tx['no_rkm_medis']);?></span>
                                    </td>
                                    <td>
                                        <?=htmlspecialchars($tx['nm_pasien']);?><br>
                                        <span class="label bg-blue-grey" style="font-size: 10px;"><?=htmlspecialchars($tx['kd_pj']);?></span>
                                    </td>
                                    <td>
                                        <strong><?=htmlspecialchars($tx['nm_perawatan']);?></strong><br>
                                        <span style="font-size: 11px; color: #888;">Kode: <?=htmlspecialchars($tx['kd_tindakan']);?></span>
                                    </td>
                                    <td align="right" style="font-weight: bold; font-size: 13px;">
                                        <?=formatDuit2($tx['tarif']);?>
                                    </td>
                                </tr>
                            <?php endforeach; ?>
                        </tbody>
                        <tfoot>
                            <tr style="background-color: #f5f5f5; font-weight: 800; font-size: 14px;">
                                <td colspan="6" align="right">GRAND TOTAL JASA MEDIS (Rp):</td>
                                <td align="right" style="color: #e91e63;"><?=formatDuit2($grand_total);?></td>
                            </tr>
                        </tfoot>
                    </table>
                </div>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">
    function setDatePreset(preset) {
        var today = new Date();
        var startInput = document.getElementById('tgl1');
        var endInput = document.getElementById('tgl2');
        
        var formatDate = function(date) {
            var yyyy = date.getFullYear();
            var mm = String(date.getMonth() + 1).padStart(2, '0');
            var dd = String(date.getDate()).padStart(2, '0');
            return yyyy + '-' + mm + '-' + dd;
        };
        
        if (preset === 'today') {
            startInput.value = formatDate(today);
            endInput.value = formatDate(today);
        } else if (preset === 'yesterday') {
            var yesterday = new Date();
            yesterday.setDate(today.getDate() - 1);
            startInput.value = formatDate(yesterday);
            endInput.value = formatDate(yesterday);
        } else if (preset === 'this_month') {
            var firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
            var lastDay = new Date(today.getFullYear(), today.getMonth() + 1, 0);
            startInput.value = formatDate(firstDay);
            endInput.value = formatDate(lastDay);
        } else if (preset === 'last_month') {
            var firstDay = new Date(today.getFullYear(), today.getMonth() - 1, 1);
            var lastDay = new Date(today.getFullYear(), today.getMonth(), 0);
            startInput.value = formatDate(firstDay);
            endInput.value = formatDate(lastDay);
        } else if (preset === 'this_year') {
            var firstDay = new Date(today.getFullYear(), 0, 1);
            var lastDay = new Date(today.getFullYear(), 11, 31);
            startInput.value = formatDate(firstDay);
            endInput.value = formatDate(lastDay);
        }
    }

    function setDatePresetAndSubmit(preset) {
        setDatePreset(preset);
        var form = document.querySelector('form[action="index.php?act=HarianDokter"]');
        if (form) {
            form.submit();
        }
    }
</script>

<?php if ($grand_total > 0): ?>
<script type="text/javascript">
    document.addEventListener("DOMContentLoaded", function() {
        var ctx = document.getElementById('jmChart');
        if (ctx) {
            var myChart = new Chart(ctx.getContext('2d'), {
                type: 'doughnut',
                data: {
                    datasets: [{
                        data: [<?=$total_rj;?>, <?=$total_ri;?>, <?=$total_op;?>, <?=$total_lab;?>, <?=$total_rad;?>],
                        backgroundColor: ['#e91e63', '#00bcd4', '#8bc34a', '#ffc107', '#9c27b0'],
                        borderWidth: 1
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: true,
                    legend: {
                        display: false
                    },
                    tooltips: {
                        callbacks: {
                            label: function(tooltipItem, data) {
                                var categories = ['Rawat Jalan', 'Rawat Inap', 'Operasi', 'Laboratorium', 'Radiologi'];
                                var val = data.datasets[0].data[tooltipItem.index];
                                return categories[tooltipItem.index] + ': Rp. ' + Number(val).toLocaleString('id-ID');
                            }
                        }
                    }
                }
            });
        }
    });
</script>
<?php endif; ?>
