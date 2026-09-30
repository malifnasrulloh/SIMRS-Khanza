package service

import (
	"database/sql"
	"fmt"
	"time"
)

type OperationsService struct {
	db *sql.DB
}

func NewOperationsService(db *sql.DB) *OperationsService {
	return &OperationsService{db: db}
}

func calculateCheckinDiffMinutes(schedTime time.Time, checkinTimeMillis int64) int {
	checkinTime := time.UnixMilli(checkinTimeMillis).In(schedTime.Location())
	return int(checkinTime.Sub(schedTime).Minutes())
}

func (s *OperationsService) CheckinQueue(bookingCode string, checkinTimeMillis int64) (int, string, error) {
	var noRawat, tglPeriksa, status, validasi, jamPraktek string
	query := `SELECT no_rawat, tanggalperiksa, status, validasi, LEFT(jampraktek, 5)
		FROM referensi_mobilejkn_bpjs WHERE nobooking = ? LIMIT 1`

	err := s.db.QueryRow(query, bookingCode).Scan(&noRawat, &tglPeriksa, &status, &validasi, &jamPraktek)
	if err == sql.ErrNoRows {
		return 201, "Data Booking tidak ditemukan", nil
	}
	if err != nil {
		return 401, "Gagal membaca data booking", err
	}

	if status == "Batal" {
		return 201, fmt.Sprintf("Booking Anda Sudah Dibatalkan pada tanggal %s", validasi), nil
	}
	if status == "Checkin" {
		return 201, fmt.Sprintf("Anda Sudah Checkin pada tanggal %s", validasi), nil
	}
	if status != "Belum" {
		return 201, "Status booking tidak valid", nil
	}

	loc, err := time.LoadLocation("Asia/Jakarta")
	if err != nil {
		loc = time.Local
	}

	schedTime, err := time.ParseInLocation("2006-01-02 15:04", fmt.Sprintf("%s %s", tglPeriksa, jamPraktek), loc)
	if err != nil {
		return 401, "Format tanggal jadwal tidak valid", err
	}

	diffMinutes := calculateCheckinDiffMinutes(schedTime, checkinTimeMillis)
	if diffMinutes >= 60 {
		return 201, "Chekin Anda sudah expired. Silahkan konfirmasi ke loket pendaftaran", nil
	}
	if diffMinutes <= -60 {
		return 201, "Chekin Anda masih harus menunggu lagi. Silahkan konfirmasi ke loket pendaftaran", nil
	}

	tx, err := s.db.Begin()
	if err != nil {
		return 401, "Gagal memulai transaksi checkin", err
	}
	defer tx.Rollback()

	_, err = tx.Exec(`UPDATE referensi_mobilejkn_bpjs SET status = 'Checkin', validasi = NOW() WHERE nobooking = ?`, bookingCode)
	if err != nil {
		return 401, "Gagal update status checkin", err
	}

	_, err = tx.Exec(`UPDATE reg_periksa SET jam_reg = CURRENT_TIME() WHERE no_rawat = ?`, noRawat)
	if err != nil {
		return 401, "Gagal update jam registrasi", err
	}

	if err := tx.Commit(); err != nil {
		return 401, "Gagal konfirmasi checkin", err
	}

	return 200, "Ok", nil
}

func (s *OperationsService) CancelQueue(bookingCode, reason string) (int, string, error) {
	var noRawat, tglPeriksa, status, validasi, noRef, noRM string
	query := `SELECT no_rawat, tanggalperiksa, status, validasi, nomorreferensi, norm
		FROM referensi_mobilejkn_bpjs WHERE nobooking = ? LIMIT 1`

	err := s.db.QueryRow(query, bookingCode).Scan(&noRawat, &tglPeriksa, &status, &validasi, &noRef, &noRM)
	if err == sql.ErrNoRows {
		return 201, "Data Booking tidak ditemukan", nil
	}
	if err != nil {
		return 401, "Gagal membaca booking", err
	}

	if status == "Batal" {
		return 201, fmt.Sprintf("Booking Anda Sudah Dibatalkan pada tanggal %s", validasi), nil
	}
	if status == "Checkin" {
		return 201, fmt.Sprintf("Anda Sudah Checkin Pada Tanggal %s, Pendaftaran Tidak Bisa Dibatalkan", validasi), nil
	}

	today := time.Now().Format("2006-01-02")
	if today > tglPeriksa {
		return 201, "Pembatalan Antrean tidak berlaku mundur", nil
	}

	tx, err := s.db.Begin()
	if err != nil {
		return 401, "Gagal memulai pembatalan", err
	}
	defer tx.Rollback()

	_, err = tx.Exec(`UPDATE referensi_mobilejkn_bpjs SET status = 'Batal', validasi = NOW() WHERE nobooking = ?`, bookingCode)
	if err != nil {
		return 401, "Gagal update status booking", err
	}

	_, err = tx.Exec(`DELETE FROM reg_periksa WHERE no_rawat = ?`, noRawat)
	if err != nil {
		return 401, "Gagal menghapus antrean di reg_periksa", err
	}

	insertBatalQuery := `INSERT INTO referensi_mobilejkn_bpjs_batal VALUES (?, ?, ?, NOW(), ?, 'Belum', ?)`
	_, _ = tx.Exec(insertBatalQuery, noRM, noRawat, noRef, reason, bookingCode)

	if err := tx.Commit(); err != nil {
		return 401, "Gagal konfirmasi pembatalan", err
	}

	return 200, "Ok", nil
}

type SisaAntreanResult struct {
	NomorAntrean   string `json:"nomorantrean"`
	NamaPoli       string `json:"namapoli"`
	NamaDokter     string `json:"namadokter"`
	SisaAntrean    int    `json:"sisaantrean"`
	AntreanPanggil string `json:"antreanpanggil"`
	WaktuTunggu    int    `json:"waktutunggu"`
	Keterangan     string `json:"keterangan"`
}

func (s *OperationsService) GetSisaAntrean(bookingCode string) (*SisaAntreanResult, int, string, error) {
	var noRawat, tglPeriksa, status, bpjsDokter, bpjsPoli string
	query := `SELECT no_rawat, tanggalperiksa, status, kodedokter, kodepoli
		FROM referensi_mobilejkn_bpjs WHERE nobooking = ? LIMIT 1`

	err := s.db.QueryRow(query, bookingCode).Scan(&noRawat, &tglPeriksa, &status, &bpjsDokter, &bpjsPoli)
	if err == sql.ErrNoRows {
		return nil, 201, "Data Booking tidak ditemukan", nil
	}
	if err != nil {
		return nil, 401, "Gagal membaca booking", err
	}

	if status == "Batal" {
		return nil, 201, "Data booking sudah dibatalkan", nil
	}

	var rsDokter, rsPoli, noReg, nmPoli, nmDokter string
	_ = s.db.QueryRow(`SELECT kd_dokter FROM maping_dokter_dpjpvclaim WHERE kd_dokter_bpjs = ? LIMIT 1`, bpjsDokter).Scan(&rsDokter)
	_ = s.db.QueryRow(`SELECT kd_poli_rs FROM maping_poli_bpjs WHERE kd_poli_bpjs = ? LIMIT 1`, bpjsPoli).Scan(&rsPoli)
	_ = s.db.QueryRow(`SELECT no_reg FROM reg_periksa WHERE no_rawat = ? LIMIT 1`, noRawat).Scan(&noReg)

	var sisaAntrean int
	countQuery := `SELECT poliklinik.nm_poli, dokter.nm_dokter,
		IFNULL(SUM(CASE WHEN reg_periksa.stts = 'Belum' THEN 1 ELSE 0 END), 0) as sisa
		FROM reg_periksa
		INNER JOIN poliklinik ON poliklinik.kd_poli = reg_periksa.kd_poli
		INNER JOIN dokter ON dokter.kd_dokter = reg_periksa.kd_dokter
		WHERE reg_periksa.kd_dokter = ? AND reg_periksa.kd_poli = ?
		AND reg_periksa.tgl_registrasi = ?
		AND CONVERT(RIGHT(reg_periksa.no_reg, 3), SIGNED) < CONVERT(RIGHT(?, 3), SIGNED)
		GROUP BY poliklinik.nm_poli, dokter.nm_dokter`

	err = s.db.QueryRow(countQuery, rsDokter, rsPoli, tglPeriksa, noReg).Scan(&nmPoli, &nmDokter, &sisaAntrean)
	if err != nil && err != sql.ErrNoRows {
		return nil, 401, "Gagal menghitung sisa antrean", err
	}

	var activeReg sql.NullString
	_ = s.db.QueryRow(`SELECT no_reg FROM reg_periksa
		WHERE stts = 'Belum' AND kd_dokter = ? AND kd_poli = ? AND tgl_registrasi = ?
		AND CONVERT(RIGHT(no_reg, 3), SIGNED) <= CONVERT(RIGHT(?, 3), SIGNED)
		ORDER BY CONVERT(RIGHT(no_reg, 3), SIGNED) ASC LIMIT 1`,
		rsDokter, rsPoli, tglPeriksa, noReg).Scan(&activeReg)

	antreanPanggil := fmt.Sprintf("%s-", rsPoli)
	if activeReg.Valid && activeReg.String != "" {
		antreanPanggil = fmt.Sprintf("%s-%s", rsPoli, activeReg.String)
	}

	waktuTunggu := sisaAntrean * 5 * 1000 // 5 minutes in ms

	res := &SisaAntreanResult{
		NomorAntrean:   fmt.Sprintf("%s-%s", rsPoli, noReg),
		NamaPoli:       nmPoli,
		NamaDokter:     nmDokter,
		SisaAntrean:    sisaAntrean,
		AntreanPanggil: antreanPanggil,
		WaktuTunggu:    waktuTunggu,
		Keterangan:     "Datanglah Minimal 30 Menit, jika no antrian anda terlewat, silakan konfirmasi ke bagian Pendaftaran atau Perawat Poli, Terima Kasih ..",
	}

	return res, 200, "Ok", nil
}

type FarmasiResult struct {
	JenisResep   string `json:"jenisresep"`
	NomorAntrean int    `json:"nomorantrean"`
	Keterangan   string `json:"keterangan"`
}

func (s *OperationsService) AmbilAntreanFarmasi(bookingCode string) (*FarmasiResult, int, string, error) {
	var noRawat, status string
	query := `SELECT no_rawat, status FROM referensi_mobilejkn_bpjs WHERE nobooking = ? LIMIT 1`
	err := s.db.QueryRow(query, bookingCode).Scan(&noRawat, &status)
	if err == sql.ErrNoRows {
		return nil, 201, "Data Booking tidak ditemukan", nil
	}
	if status == "Belum" {
		return nil, 201, "Anda Belum Melakukan Checkin", nil
	}

	var noResep string
	var urut int
	queryResep := `SELECT no_resep, CONVERT(RIGHT(no_resep, 4), SIGNED) as urut
		FROM resep_obat WHERE no_rawat = ? LIMIT 1`
	err = s.db.QueryRow(queryResep, noRawat).Scan(&noResep, &urut)
	if err == sql.ErrNoRows {
		return nil, 201, "Anda tidak memiliki resep dari dokter yang anda tuju, silahkan konfirmasi petugas poli", nil
	}

	var racikanCount int
	_ = s.db.QueryRow(`SELECT COUNT(no_resep) FROM resep_dokter_racikan WHERE no_resep = ?`, noResep).Scan(&racikanCount)

	jenis := "Non Racikan"
	if racikanCount > 0 {
		jenis = "Racikan"
	}

	return &FarmasiResult{
		JenisResep:   jenis,
		NomorAntrean: urut,
		Keterangan:   "Resep dibuat secara elektronik di poli",
	}, 200, "Ok", nil
}

type StatusFarmasiResult struct {
	JenisResep     string `json:"jenisresep"`
	TotalAntrean   int    `json:"totalantrean"`
	SisaAntrean    int    `json:"sisaantrean"`
	AntreanPanggil int    `json:"antreanpanggil"`
	Keterangan     string `json:"keterangan"`
}

func (s *OperationsService) GetStatusAntreanFarmasi(bookingCode string) (*StatusFarmasiResult, int, string, error) {
	var noRawat, status, validasi string
	query := `SELECT no_rawat, status, validasi FROM referensi_mobilejkn_bpjs WHERE nobooking = ? LIMIT 1`
	err := s.db.QueryRow(query, bookingCode).Scan(&noRawat, &status, &validasi)
	if err == sql.ErrNoRows {
		return nil, 201, "Data Booking tidak ditemukan", nil
	}
	if err != nil {
		return nil, 401, "Gagal membaca booking", err
	}

	if status == "Batal" {
		return nil, 201, fmt.Sprintf("Booking Anda Sudah Dibatalkan pada tanggal %s", validasi), nil
	}
	if status == "Belum" {
		return nil, 201, "Anda Belum Melakukan Checkin", nil
	}

	var noResep, tglPeresepan, marking string
	queryResep := `SELECT no_resep, tgl_peresepan, LEFT(no_resep, 8) FROM resep_obat WHERE no_rawat = ? LIMIT 1`
	err = s.db.QueryRow(queryResep, noRawat).Scan(&noResep, &tglPeresepan, &marking)
	if err == sql.ErrNoRows {
		return nil, 201, "Anda tidak memiliki resep dari dokter yang anda tuju, silahkan konfirmasi petugas poli", nil
	}

	var racikanCount, totalAntrean, sisaAntrean, antreanPanggil int
	_ = s.db.QueryRow(`SELECT COUNT(no_resep) FROM resep_dokter_racikan WHERE no_resep = ?`, noResep).Scan(&racikanCount)
	_ = s.db.QueryRow(`SELECT COUNT(no_resep) FROM resep_obat WHERE tgl_peresepan = ?`, tglPeresepan).Scan(&totalAntrean)
	_ = s.db.QueryRow(`SELECT COUNT(no_resep) FROM resep_obat WHERE tgl_perawatan = '0000-00-00' AND tgl_peresepan = ?`, tglPeresepan).Scan(&sisaAntrean)
	_ = s.db.QueryRow(`SELECT ifnull(CONVERT(RIGHT(no_resep, 4), SIGNED), 0) FROM antriapotek2 WHERE LEFT(no_resep, 8) = ? LIMIT 1`, marking).Scan(&antreanPanggil)

	jenis := "Non Racikan"
	if racikanCount > 0 {
		jenis = "Racikan"
	}

	return &StatusFarmasiResult{
		JenisResep:     jenis,
		TotalAntrean:   totalAntrean,
		SisaAntrean:    sisaAntrean,
		AntreanPanggil: antreanPanggil,
		Keterangan:     "",
	}, 200, "Ok", nil
}
