/*
 Navicat Premium Data Transfer

 Source Server         : DB SERVER UBUNTU
 Source Server Type    : MariaDB
 Source Server Version : 100339
 Source Host           : 192.168.15.111:3306
 Source Schema         : SIMRS_INDRIATI

 Target Server Type    : MariaDB
 Target Server Version : 100339
 File Encoding         : 65001

 Date: 31/10/2024 09:58:35
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for satu_sehat_ihs_patient
-- ----------------------------
DROP TABLE IF EXISTS `satu_sehat_ihs_patient`;
CREATE TABLE `satu_sehat_ihs_patient` (
  `nikpasien` varchar(20) NOT NULL,
  `ihspasien` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`nikpasien`) USING BTREE,
  CONSTRAINT `ihs` FOREIGN KEY (`nikpasien`) REFERENCES `pasien` (`no_ktp`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci ROW_FORMAT=COMPACT;

SET FOREIGN_KEY_CHECKS = 1;
