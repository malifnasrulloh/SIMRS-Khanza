ALTER TABLE `pengajuan_cuti`
ADD status_manajemen enum('Proses Pengajuan','Disetujui','Ditolak') CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT 'Proses Pengajuan';

ALTER TABLE `industrifarmasi`
MODIFY COLUMN `nama_industri` varchar(1000) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL AFTER `kode_industri`,
MODIFY COLUMN `alamat` varchar(1000) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL AFTER `nama_industri`;

ALTER TABLE `satu_sehat_mapping_obat`
MODIFY COLUMN `obat_display` varchar(1000) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL AFTER `obat_system`,
MODIFY COLUMN `form_display` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL AFTER `form_system`,
MODIFY COLUMN `numerator_system` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL AFTER `numerator_code`,
MODIFY COLUMN `denominator_system` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL AFTER `denominator_code`,
MODIFY COLUMN `route_display` varchar(100) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL AFTER `route_system`;

ALTER TABLE `satu_sehat_mapping_lab` 
MODIFY COLUMN `display` varchar(1000) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL AFTER `system`,
MODIFY COLUMN `sampel_display` varchar(1000) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL AFTER `sampel_system`;

ALTER TABLE `trackersql`
MODIFY COLUMN `sqle` longtext CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL AFTER `tanggal`;

ALTER TABLE `paket_operasi` 
MODIFY COLUMN `kategori` enum('Kebidanan','Operasi','Operasi Kecil','Operasi Sedang','Operasi Besar','Operasi Khusus') CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL AFTER `nm_perawatan`;

ALTER TABLE `data_triase_igd`
MODIFY COLUMN `id_observation_cara_masuk` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `nyeri`,
MODIFY COLUMN `id_observation_alat_transportasi` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_cara_masuk`,
MODIFY COLUMN `id_observation_alasan_kedatangan` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_alat_transportasi`,
MODIFY COLUMN `id_observation_macam_kasus` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_alasan_kedatangan`,
MODIFY COLUMN `id_observation_tekanan_darah` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_macam_kasus`,
MODIFY COLUMN `id_observation_nadi` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_tekanan_darah`,
MODIFY COLUMN `id_observation_pernapasan` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_nadi`,
MODIFY COLUMN `id_observation_suhu` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_pernapasan`,
MODIFY COLUMN `id_observation_saturasi_o2` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_suhu`,
MODIFY COLUMN `id_observation_nyeri` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_saturasi_o2`,
MODIFY COLUMN `id_composition` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_nyeri`;

ALTER TABLE `data_triase_igddetail_skala1`
MODIFY COLUMN `id_observation_skala1` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `kode_skala1`;

ALTER TABLE `data_triase_igddetail_skala2`
MODIFY COLUMN `id_observation_skala2` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `kode_skala2`;

ALTER TABLE `data_triase_igddetail_skala3`
MODIFY COLUMN `id_observation_skala3` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `kode_skala3`;

ALTER TABLE `data_triase_igddetail_skala4`
MODIFY COLUMN `id_observation_skala4` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `kode_skala4`;

ALTER TABLE `data_triase_igddetail_skala5`
MODIFY COLUMN `id_observation_skala5` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `kode_skala5`;

ALTER TABLE `data_triase_igdprimer`
MODIFY COLUMN `id_observation_keluhan_utama` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `nik`,
MODIFY COLUMN `id_observation_kebutuhan_khusus` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_keluhan_utama`,
MODIFY COLUMN `id_observation_catatan` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_kebutuhan_khusus`,
MODIFY COLUMN `id_careplan_keputusan` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_catatan`,
MODIFY COLUMN `id_observation_skala` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_careplan_keputusan`;

ALTER TABLE `data_triase_igdsekunder`
MODIFY COLUMN `id_clinicalimpression_anamnesa` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `nik`,
MODIFY COLUMN `id_observation_catatan` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_clinicalimpression_anamnesa`,
MODIFY COLUMN `id_careplan_keputusan` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_observation_catatan`,
MODIFY COLUMN `id_observation_skala` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL DEFAULT '' AFTER `id_careplan_keputusan`;
