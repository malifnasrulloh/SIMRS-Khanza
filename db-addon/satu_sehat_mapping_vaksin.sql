/*
 Navicat Premium Data Transfer

 Source Server         : dongdot medika
 Source Server Type    : MariaDB
 Source Server Version : 101113 (10.11.13-MariaDB-0ubuntu0.24.04.1)
 Source Host           : 192.168.196.115:3306
 Source Schema         : dongdot

 Target Server Type    : MariaDB
 Target Server Version : 101113 (10.11.13-MariaDB-0ubuntu0.24.04.1)
 File Encoding         : 65001

 Date: 13/04/2026 20:44:52
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for satu_sehat_mapping_vaksin
-- ----------------------------
DROP TABLE IF EXISTS `satu_sehat_mapping_vaksin`;
CREATE TABLE `satu_sehat_mapping_vaksin` (
  `kode_brng` varchar(15) NOT NULL,
  `vaksin_code` varchar(15) DEFAULT NULL,
  `vaksin_system` varchar(100) NOT NULL,
  `vaksin_display` varchar(80) DEFAULT NULL,
  `route_code` varchar(30) DEFAULT NULL,
  `route_system` varchar(100) DEFAULT NULL,
  `route_display` varchar(80) DEFAULT NULL,
  `dose_quantity_code` varchar(15) DEFAULT NULL,
  `dose_quantity_system` varchar(80) DEFAULT NULL,
  `dose_quantity_unit` varchar(15) DEFAULT NULL,
  PRIMARY KEY (`kode_brng`),
  CONSTRAINT `satu_sehat_mapping_vaksin_ibfk_1` FOREIGN KEY (`kode_brng`) REFERENCES `databarang` (`kode_brng`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

-- ----------------------------
-- Records of satu_sehat_mapping_vaksin
-- ----------------------------
BEGIN;
INSERT INTO `satu_sehat_mapping_vaksin` (`kode_brng`, `vaksin_code`, `vaksin_system`, `vaksin_display`, `route_code`, `route_system`, `route_display`, `dose_quantity_code`, `dose_quantity_system`, `dose_quantity_unit`) VALUES ('VAK0003', '93005208', 'http://sys-ids.kemkes.go.id/kfa', 'BCG', 'P', 'http://www.whocc.no/atc', 'parenteral', 'mL', 'http://unitsofmeasure.org/', 'mL');
INSERT INTO `satu_sehat_mapping_vaksin` (`kode_brng`, `vaksin_code`, `vaksin_system`, `vaksin_display`, `route_code`, `route_system`, `route_display`, `dose_quantity_code`, `dose_quantity_system`, `dose_quantity_unit`) VALUES ('VAK0004', '93027069', 'http://sys-ids.kemkes.go.id/kfa', 'Td (adult), 5 Lf tetanus toxoid, preservative free, adsorbed', 'P', 'http://www.whocc.no/atc', 'parenteral', 'mL', 'http://unitsofmeasure.org/', 'mL');
INSERT INTO `satu_sehat_mapping_vaksin` (`kode_brng`, `vaksin_code`, `vaksin_system`, `vaksin_display`, `route_code`, `route_system`, `route_display`, `dose_quantity_code`, `dose_quantity_system`, `dose_quantity_unit`) VALUES ('VAK0009', '93004983', 'http://sys-ids.kemkes.go.id/kfa', 'POLIO', 'O', 'http://www.whocc.no/atc', 'oral', 'mL', 'http://unitsofmeasure.org/', 'mL');
INSERT INTO `satu_sehat_mapping_vaksin` (`kode_brng`, `vaksin_code`, `vaksin_system`, `vaksin_display`, `route_code`, `route_system`, `route_display`, `dose_quantity_code`, `dose_quantity_system`, `dose_quantity_unit`) VALUES ('VAK0011', '180', 'http://sys-ids.kemkes.go.id/kfa', 'tetanus immune globulin', 'P', 'http://www.whocc.no/atc', 'parenteral', 'mL', 'http://unitsofmeasure.org/', 'mL');
INSERT INTO `satu_sehat_mapping_vaksin` (`kode_brng`, `vaksin_code`, `vaksin_system`, `vaksin_display`, `route_code`, `route_system`, `route_display`, `dose_quantity_code`, `dose_quantity_system`, `dose_quantity_unit`) VALUES ('VAK0013', '93026075', 'http://sys-ids.kemkes.go.id/kfa', 'HepB', 'P', 'http://www.whocc.no/atc', 'parenteral', 'mL', 'http://unitsofmeasure.org/', 'mL');
INSERT INTO `satu_sehat_mapping_vaksin` (`kode_brng`, `vaksin_code`, `vaksin_system`, `vaksin_display`, `route_code`, `route_system`, `route_display`, `dose_quantity_code`, `dose_quantity_system`, `dose_quantity_unit`) VALUES ('VAK0014', '93022999', 'http://sys-ids.kemkes.go.id/kfa', 'FLU', 'P', 'http://www.whocc.no/atc', 'parenteral', 'mL', 'http://unitsofmeasure.org/', 'mL');
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
