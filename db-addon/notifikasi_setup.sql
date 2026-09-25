-- ==============================================================
-- NOTIFIKASI SETUP & TRIGGERS FOR SIMRS KHANZA
-- ==============================================================

-- 0. Table: notification_queue (Identical to E-Dokter schema)
CREATE TABLE IF NOT EXISTS notification_queue (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  nik VARCHAR(20) NOT NULL,
  event_type VARCHAR(50) NOT NULL,
  title VARCHAR(255) NOT NULL,
  body TEXT NOT NULL,
  payload JSON DEFAULT NULL,
  created_at DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  source_table VARCHAR(50) DEFAULT NULL,
  source_pk VARCHAR(100) DEFAULT NULL,
  deleted_at DATETIME(3) DEFAULT NULL,
  INDEX idx_nik_created (nik, id),
  INDEX idx_source (source_table, source_pk)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 1. Table: notifikasi_user (Direct User Subscription modeled like table `user`)
CREATE TABLE IF NOT EXISTS notifikasi_user (
    id_user VARCHAR(50) NOT NULL PRIMARY KEY,
    lab_request ENUM('true','false') DEFAULT 'false',
    prescription_request ENUM('true','false') DEFAULT 'false',
    radiology_request ENUM('true','false') DEFAULT 'false',
    suara_bell ENUM('true','false') DEFAULT 'true'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ==============================================================
-- TRIGGERS FOR NOTIFICATION QUEUE
-- ==============================================================
DELIMITER //

-- 1. permintaan_lab Triggers ────────────────────────────────
DROP TRIGGER IF EXISTS trg_notif_ins_permintaan_lab//
CREATE TRIGGER trg_notif_ins_permintaan_lab
AFTER INSERT ON permintaan_lab
FOR EACH ROW
BEGIN
  DECLARE v_nm_pasien VARCHAR(100) DEFAULT 'Unknown';

  IF NEW.tgl_sampel = '0000-00-00' OR NEW.tgl_sampel IS NULL THEN
    SELECT COALESCE(p.nm_pasien, 'Unknown') INTO v_nm_pasien
    FROM reg_periksa rp LEFT JOIN pasien p ON p.no_rkm_medis = rp.no_rkm_medis
    WHERE rp.no_rawat = NEW.no_rawat LIMIT 1;

    INSERT INTO notification_queue (nik, event_type, title, body, payload, created_at, source_table, source_pk)
    VALUES (
      'LAB', 'lab_request', 'Permintaan Lab Baru',
      CONCAT('Permintaan lab untuk pasien ', COALESCE(v_nm_pasien, 'Unknown'), ' (', NEW.no_rawat, ')'),
      JSON_OBJECT('noorder', NEW.noorder, 'no_rawat', NEW.no_rawat, 'nm_pasien', COALESCE(v_nm_pasien, 'Unknown'), 'diagnosa_klinis', COALESCE(NEW.diagnosa_klinis, '')),
      NOW(3), 'permintaan_lab', NEW.noorder
    );
  END IF;
END//

DROP TRIGGER IF EXISTS trg_notif_upd_permintaan_lab//
CREATE TRIGGER trg_notif_upd_permintaan_lab
AFTER UPDATE ON permintaan_lab
FOR EACH ROW
BEGIN
  IF (OLD.tgl_sampel = '0000-00-00' OR OLD.tgl_sampel IS NULL)
     AND NEW.tgl_sampel IS NOT NULL
     AND NEW.tgl_sampel <> '0000-00-00' THEN
    UPDATE notification_queue
    SET deleted_at = NOW(3)
    WHERE source_table = 'permintaan_lab' AND source_pk = NEW.noorder AND deleted_at IS NULL;
  END IF;
END//

DROP TRIGGER IF EXISTS trg_notif_del_permintaan_lab//
CREATE TRIGGER trg_notif_del_permintaan_lab
AFTER DELETE ON permintaan_lab
FOR EACH ROW
BEGIN
  UPDATE notification_queue
  SET deleted_at = NOW(3)
  WHERE source_table = 'permintaan_lab' AND source_pk = OLD.noorder AND deleted_at IS NULL;
END//

-- 2. resep_obat Triggers ────────────────────────────────────
DROP TRIGGER IF EXISTS trg_notif_ins_resep_obat//
CREATE TRIGGER trg_notif_ins_resep_obat
AFTER INSERT ON resep_obat
FOR EACH ROW
BEGIN
  DECLARE v_nm_pasien VARCHAR(100) DEFAULT 'Unknown';

  IF NEW.tgl_perawatan = '0000-00-00' OR NEW.tgl_perawatan IS NULL THEN
    SELECT COALESCE(p.nm_pasien, 'Unknown') INTO v_nm_pasien
    FROM reg_periksa rp LEFT JOIN pasien p ON p.no_rkm_medis = rp.no_rkm_medis
    WHERE rp.no_rawat = NEW.no_rawat LIMIT 1;

    INSERT INTO notification_queue (nik, event_type, title, body, payload, created_at, source_table, source_pk)
    VALUES (
      'FARMASI', 'prescription_request', 'Resep Belum Tervalidasi',
      CONCAT('Resep baru untuk pasien ', COALESCE(v_nm_pasien, 'Unknown'), ' (No. Resep: ', NEW.no_resep, ')'),
      JSON_OBJECT('no_resep', NEW.no_resep, 'no_rawat', NEW.no_rawat, 'nm_pasien', COALESCE(v_nm_pasien, 'Unknown')),
      NOW(3), 'resep_obat', NEW.no_resep
    );
  END IF;
END//

DROP TRIGGER IF EXISTS trg_notif_upd_resep_obat//
CREATE TRIGGER trg_notif_upd_resep_obat
AFTER UPDATE ON resep_obat
FOR EACH ROW
BEGIN
  IF (OLD.tgl_perawatan = '0000-00-00' OR OLD.tgl_perawatan IS NULL)
     AND NEW.tgl_perawatan IS NOT NULL
     AND NEW.tgl_perawatan <> '0000-00-00' THEN
    UPDATE notification_queue
    SET deleted_at = NOW(3)
    WHERE source_table = 'resep_obat' AND source_pk = NEW.no_resep AND deleted_at IS NULL;
  END IF;
END//

DROP TRIGGER IF EXISTS trg_notif_del_resep_obat//
CREATE TRIGGER trg_notif_del_resep_obat
AFTER DELETE ON resep_obat
FOR EACH ROW
BEGIN
  UPDATE notification_queue
  SET deleted_at = NOW(3)
  WHERE source_table = 'resep_obat' AND source_pk = OLD.no_resep AND deleted_at IS NULL;
END//

-- 3. permintaan_radiologi Triggers ──────────────────────────
DROP TRIGGER IF EXISTS trg_notif_ins_permintaan_radiologi//
CREATE TRIGGER trg_notif_ins_permintaan_radiologi
AFTER INSERT ON permintaan_radiologi
FOR EACH ROW
BEGIN
  DECLARE v_nm_pasien VARCHAR(100) DEFAULT 'Unknown';

  IF NEW.tgl_sampel = '0000-00-00' OR NEW.tgl_sampel IS NULL THEN
    SELECT COALESCE(p.nm_pasien, 'Unknown') INTO v_nm_pasien
    FROM reg_periksa rp LEFT JOIN pasien p ON p.no_rkm_medis = rp.no_rkm_medis
    WHERE rp.no_rawat = NEW.no_rawat LIMIT 1;

    INSERT INTO notification_queue (nik, event_type, title, body, payload, created_at, source_table, source_pk)
    VALUES (
      'RADIOLOGI', 'radiology_request', 'Permintaan Radiologi Baru',
      CONCAT('Permintaan radiologi untuk pasien ', COALESCE(v_nm_pasien, 'Unknown'), ' (', NEW.no_rawat, ')'),
      JSON_OBJECT('noorder', NEW.noorder, 'no_rawat', NEW.no_rawat, 'nm_pasien', COALESCE(v_nm_pasien, 'Unknown')),
      NOW(3), 'permintaan_radiologi', NEW.noorder
    );
  END IF;
END//

DROP TRIGGER IF EXISTS trg_notif_upd_permintaan_radiologi//
CREATE TRIGGER trg_notif_upd_permintaan_radiologi
AFTER UPDATE ON permintaan_radiologi
FOR EACH ROW
BEGIN
  IF (OLD.tgl_sampel = '0000-00-00' OR OLD.tgl_sampel IS NULL)
     AND NEW.tgl_sampel IS NOT NULL
     AND NEW.tgl_sampel <> '0000-00-00' THEN
    UPDATE notification_queue
    SET deleted_at = NOW(3)
    WHERE source_table = 'permintaan_radiologi' AND source_pk = NEW.noorder AND deleted_at IS NULL;
  END IF;
END//

DROP TRIGGER IF EXISTS trg_notif_del_permintaan_radiologi//
CREATE TRIGGER trg_notif_del_permintaan_radiologi
AFTER DELETE ON permintaan_radiologi
FOR EACH ROW
BEGIN
  UPDATE notification_queue
  SET deleted_at = NOW(3)
  WHERE source_table = 'permintaan_radiologi' AND source_pk = OLD.noorder AND deleted_at IS NULL;
END//

DELIMITER ;
