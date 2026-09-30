package service

import (
	"database/sql"
	"fmt"
	"time"

	"api-bpjsfktl-go/internal/store"
)

type PasienService struct {
	db   *sql.DB
	auth *store.InsuranceAuth
}

func NewPasienService(db *sql.DB, auth *store.InsuranceAuth) *PasienService {
	return &PasienService{db: db, auth: auth}
}

type PasienBaruParams struct {
	NomorKartu   string `json:"nomorkartu"`
	NIK          string `json:"nik"`
	NomorKK      string `json:"nomorkk"`
	Nama         string `json:"nama"`
	JenisKelamin string `json:"jeniskelamin"`
	TanggalLahir string `json:"tanggallahir"`
	NoHP         string `json:"nohp"`
	Alamat       string `json:"alamat"`
	KodeProp     string `json:"kodeprop"`
	NamaProp     string `json:"namaprop"`
	KodeDati2    string `json:"kodedati2"`
	NamaDati2    string `json:"namadati2"`
	KodeKec      string `json:"kodekec"`
	NamaKec      string `json:"namakec"`
	KodeKel      string `json:"kodekel"`
	NamaKel      string `json:"namakel"`
	RW           string `json:"rw"`
	RT           string `json:"rt"`
}

type SetUrutRM struct {
	Urutan           string
	Tahun            string
	Bulan            string
	PosisiTahunBulan string
}

func formatTerminalDigit(n int) string {
	s := fmt.Sprintf("%06d", n)
	// Terminal: digits [4:6] + [2:4] + [0:2]
	return s[4:6] + s[2:4] + s[0:2]
}

func formatMiddleDigit(n int) string {
	s := fmt.Sprintf("%06d", n)
	// Middle: digits [2:4] + [0:2] + [4:6]
	return s[2:4] + s[0:2] + s[4:6]
}

func (s *PasienService) calculateNextNoRM(tx *sql.Tx) (string, error) {
	var rules SetUrutRM
	query := `SELECT urutan, tahun, bulan, posisi_tahun_bulan FROM set_urut_no_rkm_medis LIMIT 1`
	err := tx.QueryRow(query).Scan(&rules.Urutan, &rules.Tahun, &rules.Bulan, &rules.PosisiTahunBulan)
	if err != nil {
		rules.Urutan = "Straight"
		rules.PosisiTahunBulan = "Depan"
	}

	awalanTahun := ""
	awalanBulan := ""
	if rules.Tahun == "Yes" {
		awalanTahun = time.Now().Format("06")
	}
	if rules.Bulan == "Yes" {
		awalanBulan = time.Now().Format("01")
	}

	var nourut string
	var maxVal int

	if rules.PosisiTahunBulan == "Depan" {
		switch rules.Urutan {
		case "Terminal":
			q := `SELECT ifnull(MAX(CONVERT(CONCAT(SUBSTRING(RIGHT(no_rkm_medis,6),5,2),SUBSTRING(RIGHT(no_rkm_medis,6),3,2),SUBSTRING(RIGHT(no_rkm_medis,6),1,2)),signed)),0)+1 FROM set_no_rkm_medis FOR UPDATE`
			_ = tx.QueryRow(q).Scan(&maxVal)
			nourut = formatTerminalDigit(maxVal)
		case "Middle":
			q := `SELECT ifnull(MAX(CONVERT(CONCAT(SUBSTRING(RIGHT(no_rkm_medis,6),3,2),SUBSTRING(RIGHT(no_rkm_medis,6),1,2),SUBSTRING(RIGHT(no_rkm_medis,6),5,2)),signed)),0)+1 FROM set_no_rkm_medis FOR UPDATE`
			_ = tx.QueryRow(q).Scan(&maxVal)
			nourut = formatMiddleDigit(maxVal)
		default: // Straight
			q := `SELECT ifnull(MAX(CONVERT(RIGHT(no_rkm_medis, 6), signed)), 0) + 1 FROM set_no_rkm_medis FOR UPDATE`
			_ = tx.QueryRow(q).Scan(&maxVal)
			nourut = fmt.Sprintf("%06d", maxVal)
		}
		return fmt.Sprintf("%s%s%s", awalanTahun, awalanBulan, nourut), nil
	}

	// PosisiTahunBulan == "Belakang"
	switch rules.Urutan {
	case "Terminal":
		q := `SELECT ifnull(MAX(CONVERT(CONCAT(SUBSTRING(LEFT(no_rkm_medis,6),5,2),SUBSTRING(LEFT(no_rkm_medis,6),3,2),SUBSTRING(LEFT(no_rkm_medis,6),1,2)),signed)),0)+1 FROM set_no_rkm_medis FOR UPDATE`
		_ = tx.QueryRow(q).Scan(&maxVal)
		nourut = formatTerminalDigit(maxVal)
	case "Middle":
		q := `SELECT ifnull(MAX(CONVERT(CONCAT(SUBSTRING(LEFT(no_rkm_medis,6),3,2),SUBSTRING(LEFT(no_rkm_medis,6),1,2),SUBSTRING(LEFT(no_rkm_medis,6),5,2)),signed)),0)+1 FROM set_no_rkm_medis FOR UPDATE`
		_ = tx.QueryRow(q).Scan(&maxVal)
		nourut = formatMiddleDigit(maxVal)
	default: // Straight
		q := `SELECT ifnull(MAX(CONVERT(LEFT(no_rkm_medis, 6), signed)), 0) + 1 FROM set_no_rkm_medis FOR UPDATE`
		_ = tx.QueryRow(q).Scan(&maxVal)
		nourut = fmt.Sprintf("%06d", maxVal)
	}

	if len(awalanBulan+awalanTahun) > 0 {
		return fmt.Sprintf("%s-%s%s", nourut, awalanBulan, awalanTahun), nil
	}
	return nourut, nil
}

func (s *PasienService) RegisterPatient(req *PasienBaruParams) (string, int, string, error) {
	var existing int
	err := s.db.QueryRow(`SELECT COUNT(no_rkm_medis) FROM pasien WHERE no_ktp = ? OR no_peserta = ?`, req.NIK, req.NomorKartu).Scan(&existing)
	if err == nil && existing > 0 {
		return "", 201, "Pasien dengan NIK dan No.Kartu tersebut sudah terdaftar", nil
	}

	tx, err := s.db.Begin()
	if err != nil {
		return "", 401, "Gagal memulai registrasi pasien", err
	}
	defer tx.Rollback()

	norm, err := s.calculateNextNoRM(tx)
	if err != nil {
		return "", 401, "Gagal membuat nomor RM baru", err
	}

	// Upsert geographic masters
	_, _ = tx.Exec(`INSERT IGNORE INTO kelurahan VALUES ('0', ?)`, req.NamaKel)
	_, _ = tx.Exec(`INSERT IGNORE INTO kecamatan VALUES ('0', ?)`, req.NamaKec)
	_, _ = tx.Exec(`INSERT IGNORE INTO kabupaten VALUES ('0', ?)`, req.NamaDati2)
	_, _ = tx.Exec(`INSERT IGNORE INTO propinsi VALUES ('0', ?)`, req.NamaProp)

	var kdKel, kdKec, kdKab, kdProp int
	_ = tx.QueryRow(`SELECT kd_kel FROM kelurahan WHERE nm_kel = ? LIMIT 1`, req.NamaKel).Scan(&kdKel)
	_ = tx.QueryRow(`SELECT kd_kec FROM kecamatan WHERE nm_kec = ? LIMIT 1`, req.NamaKec).Scan(&kdKec)
	_ = tx.QueryRow(`SELECT kd_kab FROM kabupaten WHERE nm_kab = ? LIMIT 1`, req.NamaDati2).Scan(&kdKab)
	_ = tx.QueryRow(`SELECT kd_prop FROM propinsi WHERE nm_prop = ? LIMIT 1`, req.NamaProp).Scan(&kdProp)

	caraBayar := s.auth.CaraBayar
	if caraBayar == "" {
		caraBayar = "BPJ"
	}

	insertPasien := `INSERT INTO pasien VALUES (?, ?, ?, ?, '-', ?, '-', ?, '-', '-', 'JOMBLO', '-', CURRENT_DATE(), ?, '0', '-', 'SAUDARA', '-', ?, ?, ?, ?, ?, '-', ?, ?, ?, ?, '-', '1', '1', '1', '-', '-', ?, ?)`
	_, err = tx.Exec(insertPasien,
		norm, req.Nama, req.NIK, req.JenisKelamin, req.TanggalLahir, req.Alamat,
		req.NoHP, caraBayar, req.NomorKartu, kdKel, kdKec, kdKab,
		req.Alamat, req.NamaKel, req.NamaKec, req.NamaDati2, kdProp, req.NamaProp,
	)
	if err != nil {
		return "", 401, "Gagal menyimpan pasien baru", err
	}

	_, _ = tx.Exec(`DELETE FROM set_no_rkm_medis`)
	_, _ = tx.Exec(`INSERT INTO set_no_rkm_medis VALUES (?)`, norm)

	if err := tx.Commit(); err != nil {
		return "", 401, "Gagal konfirmasi nomor RM baru", err
	}

	return norm, 200, "Pasien berhasil mendapatkann nomor RM, silahkan lanjutkan ke booking. Pasien tidak perlu ke admisi", nil
}
