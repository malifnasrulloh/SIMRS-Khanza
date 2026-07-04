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