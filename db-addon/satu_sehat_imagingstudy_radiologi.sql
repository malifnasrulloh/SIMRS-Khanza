/*
 Navicat Premium Dump SQL

 Source Server         : SH
 Source Server Type    : MySQL
 Source Server Version : 100420 (10.4.20-MariaDB-log)
 Source Host           : 192.168.2.213:3306
 Source Schema         : sik_rsupp

 Target Server Type    : MySQL
 Target Server Version : 100420 (10.4.20-MariaDB-log)
 File Encoding         : 65001

 Date: 16/04/2026 20:34:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for satu_sehat_imagingstudy_radiologi
-- ----------------------------
DROP TABLE IF EXISTS `satu_sehat_imagingstudy_radiologi`;
CREATE TABLE `satu_sehat_imagingstudy_radiologi`  (
  `noorder` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `kd_jenis_prw` varchar(15) CHARACTER SET latin1 COLLATE latin1_swedish_ci NOT NULL,
  `id_servicerequest` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `id_imaging` varchar(40) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  `acsn` varchar(50) CHARACTER SET latin1 COLLATE latin1_swedish_ci NULL DEFAULT NULL,
  PRIMARY KEY (`noorder`, `kd_jenis_prw`) USING BTREE,
  INDEX `kd_jenis_prw`(`kd_jenis_prw` ASC) USING BTREE,
  CONSTRAINT `satu_sehat_imagingstudy_radiologi_ibfk_1` FOREIGN KEY (`noorder`) REFERENCES `permintaan_radiologi` (`noorder`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `satu_sehat_imagingstudy_radiologi_ibfk_2` FOREIGN KEY (`kd_jenis_prw`) REFERENCES `jns_perawatan_radiologi` (`kd_jenis_prw`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = latin1 COLLATE = latin1_swedish_ci ROW_FORMAT = DYNAMIC;

SET FOREIGN_KEY_CHECKS = 1;
