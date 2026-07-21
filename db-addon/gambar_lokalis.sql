CREATE TABLE IF NOT EXISTS `gambar_lokalis` (
  `no_rawat` varchar(17) NOT NULL,
  `jenis_form` varchar(50) NOT NULL,
  `lokasi_gambar` varchar(500) NOT NULL,
  `tgl_simpan` datetime NOT NULL,
  PRIMARY KEY (`no_rawat`, `jenis_form`)
) ENGINE=InnoDB;
