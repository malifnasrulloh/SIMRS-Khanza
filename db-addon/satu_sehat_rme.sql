CREATE TABLE IF NOT EXISTS `satu_sehat_rme_akses` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `no_rawat` VARCHAR(17) NOT NULL,
  `id_pasien_satusehat` VARCHAR(30) NOT NULL,
  `id_praktisi_satusehat` VARCHAR(30) NOT NULL,
  `shlink_id` VARCHAR(100) DEFAULT NULL,
  `shlink_url` TEXT NOT NULL,
  `consent_id` VARCHAR(100) DEFAULT NULL,
  `tipe_akses` ENUM('NORMAL', 'EMERGENCY') NOT NULL DEFAULT 'NORMAL',
  `alasan_darurat` VARCHAR(255) DEFAULT NULL,
  `nama_pengantar` VARCHAR(100) DEFAULT NULL,
  `waktu_akses` DATETIME NOT NULL,
  `expired_at` DATETIME NOT NULL,
  `user_akses` VARCHAR(50) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_no_rawat` (`no_rawat`),
  KEY `idx_expired` (`expired_at`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
