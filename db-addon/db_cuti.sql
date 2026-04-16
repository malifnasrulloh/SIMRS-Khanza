CREATE TABLE `pembatasan_cuti` (
  `tanggal` date NOT NULL,
  PRIMARY KEY (`tanggal`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

CREATE TABLE `set_pengaturan_cuti` (
  `maks_pengajuan` int(11) NOT NULL DEFAULT 0,
  `maks_jatah_semester` int(11) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- Initialize default settings
INSERT INTO `set_pengaturan_cuti` (`maks_pengajuan`, `maks_jatah_semester`) VALUES (3, 12);
