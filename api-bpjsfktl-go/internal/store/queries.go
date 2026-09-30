package store

import (
	"database/sql"
	"fmt"
	"strings"
	"time"
)

type Repository struct {
	db *sql.DB
}

func NewRepository(db *sql.DB) *Repository {
	return &Repository{db: db}
}

type StatusAntreanResult struct {
	NamaPoli       string
	NamaDokter     string
	TotalAntrean   int
	SisaAntrean    int
	AntreanPanggil string
}

func (r *Repository) GetStatusAntrean(clinicRS, doctorRS, date string) (*StatusAntreanResult, error) {
	query := `SELECT poliklinik.nm_poli, dokter.nm_dokter,
		COUNT(reg_periksa.no_rawat) as total_antrean,
		IFNULL(SUM(CASE WHEN reg_periksa.stts = 'Belum' THEN 1 ELSE 0 END), 0) as sisa_antrean
		FROM poliklinik
		CROSS JOIN dokter
		LEFT JOIN reg_periksa ON reg_periksa.kd_poli = poliklinik.kd_poli
			AND reg_periksa.kd_dokter = dokter.kd_dokter
			AND reg_periksa.tgl_registrasi = ?
		WHERE poliklinik.kd_poli = ? AND dokter.kd_dokter = ?
		GROUP BY poliklinik.nm_poli, dokter.nm_dokter`

	var res StatusAntreanResult
	err := r.db.QueryRow(query, date, clinicRS, doctorRS).Scan(
		&res.NamaPoli, &res.NamaDokter, &res.TotalAntrean, &res.SisaAntrean,
	)
	if err != nil {
		return nil, fmt.Errorf("query status antrean: %w", err)
	}

	var activeNoReg sql.NullString
	callQuery := `SELECT no_reg FROM reg_periksa
		WHERE stts = 'Belum' AND kd_dokter = ? AND kd_poli = ? AND tgl_registrasi = ?
		ORDER BY CONVERT(RIGHT(no_reg, 3), SIGNED) ASC LIMIT 1`
	_ = r.db.QueryRow(callQuery, doctorRS, clinicRS, date).Scan(&activeNoReg)

	if activeNoReg.Valid && activeNoReg.String != "" {
		res.AntreanPanggil = fmt.Sprintf("%s-%s", clinicRS, activeNoReg.String)
	} else {
		res.AntreanPanggil = fmt.Sprintf("%s-", clinicRS)
	}

	return &res, nil
}

type OperasiItem struct {
	KodeBooking    string `json:"kodebooking"`
	TanggalOperasi string `json:"tanggaloperasi"`
	JenisTindakan  string `json:"jenistindakan"`
	KodePoli       string `json:"kodepoli"`
	NamaPoli       string `json:"namapoli"`
	Terlaksana     int    `json:"terlaksana"`
	NoPeserta      string `json:"nopeserta,omitempty"`
	LastUpdate     int64  `json:"lastupdate,omitempty"`
}

func (r *Repository) GetJadwalOperasiRS(startDate, endDate string) ([]OperasiItem, error) {
	query := `SELECT booking_operasi.no_rawat, booking_operasi.tanggal,
		paket_operasi.nm_perawatan, maping_poli_bpjs.kd_poli_bpjs,
		maping_poli_bpjs.nm_poli_bpjs, booking_operasi.status, pasien.no_peserta
		FROM booking_operasi
		INNER JOIN reg_periksa ON booking_operasi.no_rawat = reg_periksa.no_rawat
		INNER JOIN pasien ON pasien.no_rkm_medis = reg_periksa.no_rkm_medis
		INNER JOIN paket_operasi ON booking_operasi.kode_paket = paket_operasi.kode_paket
		INNER JOIN maping_poli_bpjs ON maping_poli_bpjs.kd_poli_rs = reg_periksa.kd_poli
		WHERE booking_operasi.tanggal BETWEEN ? AND ?
		ORDER BY booking_operasi.tanggal, booking_operasi.jam_mulai`

	rows, err := r.db.Query(query, startDate, endDate)
	if err != nil {
		return nil, fmt.Errorf("query jadwal operasi rs: %w", err)
	}
	defer rows.Close()

	nowMillis := time.Now().UnixMilli()
	var list []OperasiItem
	for rows.Next() {
		var item OperasiItem
		var status string
		err := rows.Scan(&item.KodeBooking, &item.TanggalOperasi, &item.JenisTindakan,
			&item.KodePoli, &item.NamaPoli, &status, &item.NoPeserta)
		if err == nil {
			if strings.EqualFold(status, "Menunggu") {
				item.Terlaksana = 0
			} else {
				item.Terlaksana = 1
			}
			item.LastUpdate = nowMillis
			list = append(list, item)
		}
	}
	return list, nil
}

func (r *Repository) GetJadwalOperasiPasien(noPeserta string) ([]OperasiItem, error) {
	query := `SELECT booking_operasi.no_rawat, booking_operasi.tanggal,
		paket_operasi.nm_perawatan, maping_poli_bpjs.kd_poli_bpjs,
		maping_poli_bpjs.nm_poli_bpjs, booking_operasi.status
		FROM booking_operasi
		INNER JOIN reg_periksa ON booking_operasi.no_rawat = reg_periksa.no_rawat
		INNER JOIN pasien ON pasien.no_rkm_medis = reg_periksa.no_rkm_medis
		INNER JOIN paket_operasi ON booking_operasi.kode_paket = paket_operasi.kode_paket
		INNER JOIN maping_poli_bpjs ON maping_poli_bpjs.kd_poli_rs = reg_periksa.kd_poli
		WHERE pasien.no_peserta = ?
		ORDER BY booking_operasi.tanggal, booking_operasi.jam_mulai`

	rows, err := r.db.Query(query, noPeserta)
	if err != nil {
		return nil, fmt.Errorf("query jadwal operasi pasien: %w", err)
	}
	defer rows.Close()

	var list []OperasiItem
	for rows.Next() {
		var item OperasiItem
		var status string
		err := rows.Scan(&item.KodeBooking, &item.TanggalOperasi, &item.JenisTindakan,
			&item.KodePoli, &item.NamaPoli, &status)
		if err == nil {
			if strings.EqualFold(status, "Menunggu") {
				item.Terlaksana = 0
			} else {
				item.Terlaksana = 1
			}
			list = append(list, item)
		}
	}
	return list, nil
}
