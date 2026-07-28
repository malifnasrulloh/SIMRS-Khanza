-- Migration: Add DICOM Modality column to satu_sehat_mapping_radiologi table
-- Default modality is 'CR' (Conventional Radiography)

ALTER TABLE `satu_sehat_mapping_radiologi` 
ADD COLUMN `modality` VARCHAR(10) NOT NULL DEFAULT 'CR' AFTER `kd_jenis_prw`;