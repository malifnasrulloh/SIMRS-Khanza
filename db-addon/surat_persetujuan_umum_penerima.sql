CREATE TABLE `surat_persetujuan_umum_penerima` (
    `no_surat` varchar(20) NOT NULL,
    `no_urut` tinyint NOT NULL,
    `nama` varchar(50) NOT NULL,
    `hubungan` varchar(30) NOT NULL,
    PRIMARY KEY (`no_surat`, `no_urut`),
    FOREIGN KEY (`no_surat`) REFERENCES `surat_persetujuan_umum` (`no_surat`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
