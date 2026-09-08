CREATE TABLE IF NOT EXISTS `satu_sehat_nutritionorder` (
  `no_rawat` varchar(17) NOT NULL,
  `tanggal` datetime NOT NULL,
  `id_nutritionorder` varchar(64) DEFAULT NULL,
  `status` varchar(20) DEFAULT 'active',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`no_rawat`, `tanggal`),
  KEY `idx_id_nutritionorder` (`id_nutritionorder`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
