CREATE TABLE IF NOT EXISTS `satu_sehat_kyc_log` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `no_rkm_medis` VARCHAR(15) DEFAULT NULL,
  `nik_pasien` VARCHAR(20) NOT NULL,
  `nama_pasien` VARCHAR(100) NOT NULL,
  `nik_petugas` VARCHAR(20) NOT NULL,
  `nama_petugas` VARCHAR(100) NOT NULL,
  `tanggal_akses` DATETIME NOT NULL,
  `token` VARCHAR(100) NOT NULL,
  `url_validasi` TEXT NOT NULL,
  `status` ENUM('Proses','Sukses','Batal') DEFAULT 'Proses',
  PRIMARY KEY (`id`),
  KEY `idx_nik_pasien` (`nik_pasien`),
  KEY `idx_tanggal` (`tanggal_akses`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
