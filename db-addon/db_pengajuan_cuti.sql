-- pastikan database sudah tersinkron dengan sik terbaru
ALTER TABLE sik.pengajuan_cuti ADD status_manajemen enum('Proses Pengajuan','Disetujui','Ditolak') CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT 'Proses Pengajuan';
