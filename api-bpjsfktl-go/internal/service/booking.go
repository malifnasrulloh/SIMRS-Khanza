package service

import (
	"database/sql"
	"fmt"
	"strconv"
	"strings"
	"sync"
	"time"

	"api-bpjsfktl-go/internal/cache"
	"api-bpjsfktl-go/internal/store"
)

type BookingService struct {
	db      *sql.DB
	cache   *cache.MemoryCache
	auth    *store.InsuranceAuth
	mutexes sync.Map
}

func NewBookingService(db *sql.DB, c *cache.MemoryCache, auth *store.InsuranceAuth) *BookingService {
	return &BookingService{
		db:    db,
		cache: c,
		auth:  auth,
	}
}

func (s *BookingService) getMutex(clinicRS, doctorRS, date string) *sync.Mutex {
	key := fmt.Sprintf("%s:%s:%s", clinicRS, doctorRS, date)
	m, _ := s.mutexes.LoadOrStore(key, &sync.Mutex{})
	return m.(*sync.Mutex)
}

func (s *BookingService) CalculateNoReg(clinicRS, doctorRS, date string) (string, error) {
	query := `SELECT GREATEST(
		(SELECT ifnull(MAX(CONVERT(no_reg, signed)), 0) + 1 FROM booking_registrasi WHERE kd_poli = ? AND kd_dokter = ? AND tanggal_periksa = ?),
		(SELECT ifnull(MAX(CONVERT(no_reg, signed)), 0) + 1 FROM reg_periksa WHERE kd_poli = ? AND kd_dokter = ? AND tgl_registrasi = ?)
	)`
	var nextVal int
	err := s.db.QueryRow(query, clinicRS, doctorRS, date, clinicRS, doctorRS, date).Scan(&nextVal)
	if err != nil {
		return "", fmt.Errorf("calculate no_reg: %w", err)
	}
	if nextVal <= 0 {
		nextVal = 1
	}
	return fmt.Sprintf("%03d", nextVal), nil
}

type BookingParams struct {
	NomorKartu     string `json:"nomorkartu"`
	NIK            string `json:"nik"`
	NoHP           string `json:"nohp"`
	KodePoli       string `json:"kodepoli"`
	Norm           string `json:"norm"`
	TanggalPeriksa string `json:"tanggalperiksa"`
	KodeDokter     string `json:"kodedokter"`
	JamPraktek     string `json:"jampraktek"`
	JenisKunjungan string `json:"jeniskunjungan"`
	NomorReferensi string `json:"nomorreferensi"`
}

type BookingResult struct {
	NomorAntrean     string `json:"nomorantrean"`
	AngkaAntrean     int    `json:"angkaantrean"`
	KodeBooking      string `json:"kodebooking"`
	PasienBaru       int    `json:"pasienbaru"`
	Norm             string `json:"norm"`
	NamaPoli         string `json:"namapoli"`
	NamaDokter       string `json:"namadokter"`
	EstimasiDilayani int64  `json:"estimasidilayani"`
	SisaKuotaJKN     int    `json:"sisakuotajkn"`
	KuotaJKN         int    `json:"kuotajkn"`
	SisaKuotaNonJKN  int    `json:"sisakuotanonjkn"`
	KuotaNonJKN      int    `json:"kuotanonjkn"`
	Keterangan       string `json:"keterangan"`
}

type PatientInfo struct {
	NoRkmMedis string
	Nama       string
	TglDaftar  string
	NamaKlg    string
	AlamatPj   string
	Kelurahan  string
	Kecamatan  string
	Kabupaten  string
	Propinsi   string
	Keluarga   string
	UmurTahun  int
	UmurBulan  int
	UmurHari   int
}

func (s *BookingService) FindPatient(nik, noKartu string) (*PatientInfo, error) {
	query := `SELECT no_rkm_medis, nm_pasien, tgl_daftar, namakeluarga, alamatpj,
		kelurahanpj, kecamatanpj, kabupatenpj, propinsipj, keluarga,
		TIMESTAMPDIFF(YEAR, tgl_lahir, CURDATE()) as tahun,
		(TIMESTAMPDIFF(MONTH, tgl_lahir, CURDATE()) - ((TIMESTAMPDIFF(MONTH, tgl_lahir, CURDATE()) div 12) * 12)) as bulan,
		TIMESTAMPDIFF(DAY, DATE_ADD(DATE_ADD(tgl_lahir, INTERVAL TIMESTAMPDIFF(YEAR, tgl_lahir, CURDATE()) YEAR), INTERVAL TIMESTAMPDIFF(MONTH, tgl_lahir, CURDATE()) - ((TIMESTAMPDIFF(MONTH, tgl_lahir, CURDATE()) div 12) * 12) MONTH), CURDATE()) as hari
		FROM pasien WHERE no_ktp = ? AND no_peserta = ? LIMIT 1`

	var p PatientInfo
	err := s.db.QueryRow(query, nik, noKartu).Scan(
		&p.NoRkmMedis, &p.Nama, &p.TglDaftar, &p.NamaKlg, &p.AlamatPj,
		&p.Kelurahan, &p.Kecamatan, &p.Kabupaten, &p.Propinsi, &p.Keluarga,
		&p.UmurTahun, &p.UmurBulan, &p.UmurHari,
	)
	if err == sql.ErrNoRows {
		return nil, nil
	}
	if err != nil {
		return nil, fmt.Errorf("find patient: %w", err)
	}
	return &p, nil
}

func (s *BookingService) BookQueue(req *BookingParams) (*BookingResult, int, string, error) {
	// Past date check
	today := time.Now().Format("2006-01-02")
	if req.TanggalPeriksa < today {
		return nil, 201, "Pendaftaran ke Poli ini sudah tutup", nil
	}

	// JamPraktek length check
	if len(req.JamPraktek) < 11 || req.JamPraktek[5] != '-' {
		return nil, 201, "Jam Praktek tidak sesuai", nil
	}

	// 1. Resolve mappings
	rsClinic, ok := s.cache.GetClinicMapping(req.KodePoli)
	if !ok {
		return nil, 201, "Poli tidak ditemukan", nil
	}
	rsDoctor, ok := s.cache.GetDoctorMapping(req.KodeDokter)
	if !ok {
		return nil, 201, "Dokter tidak ditemukan", nil
	}

	// 2. Validate patient exists
	patient, err := s.FindPatient(req.NIK, req.NomorKartu)
	if err != nil {
		return nil, 401, "Gagal memverifikasi data pasien", err
	}
	if patient == nil {
		return nil, 202, "Data pasien ini tidak ditemukan", nil
	}

	// 3. Acquire Tier-1 In-Process Sharded Mutex
	mu := s.getMutex(rsClinic, rsDoctor, req.TanggalPeriksa)
	mu.Lock()
	defer mu.Unlock()

	// 4. Duplicate checks
	var dupCount int
	dupRefQuery := `SELECT COUNT(nomorreferensi) FROM referensi_mobilejkn_bpjs
		WHERE (status = 'Belum' OR status = 'Checkin') AND nomorreferensi = ?`
	_ = s.db.QueryRow(dupRefQuery, req.NomorReferensi).Scan(&dupCount)
	if dupCount > 0 {
		return nil, 201, "Anda sudah terdaftar dalam antrian menggunakan nomor referensi yang sama", nil
	}

	var dupBookingCount int
	dupBookingQuery := `SELECT COUNT(reg_periksa.no_rawat) FROM reg_periksa
		INNER JOIN pasien ON reg_periksa.no_rkm_medis = pasien.no_rkm_medis
		WHERE reg_periksa.kd_poli = ? AND reg_periksa.kd_dokter = ?
		AND reg_periksa.tgl_registrasi = ? AND pasien.no_peserta = ?`
	_ = s.db.QueryRow(dupBookingQuery, rsClinic, rsDoctor, req.TanggalPeriksa, req.NomorKartu).Scan(&dupBookingCount)
	if dupBookingCount > 0 {
		return nil, 201, "Nomor Antrean hanya dapat diambil 1 kali pada Tanggal, Dokter dan Poli yang sama", nil
	}

	// 5. Tier-2 Database Transaction with pessimistic locking
	tx, err := s.db.Begin()
	if err != nil {
		return nil, 401, "Gagal memulai transaksi antrean", err
	}
	defer tx.Rollback()

	// Check Quota
	var bookedCount int
	lockQuotaQuery := `SELECT COUNT(no_rawat) FROM reg_periksa
		WHERE kd_poli = ? AND kd_dokter = ? AND tgl_registrasi = ? FOR UPDATE`
	err = tx.QueryRow(lockQuotaQuery, rsClinic, rsDoctor, req.TanggalPeriksa).Scan(&bookedCount)
	if err != nil {
		return nil, 401, "Gagal memeriksa kuota antrean", err
	}

	// Fetch quota from jadwal
	startTime := req.JamPraktek[:5]
	endTime := req.JamPraktek[6:11]
	sched, found := s.cache.GetSchedule(GetHariIndo(req.TanggalPeriksa), rsDoctor, rsClinic, startTime, endTime)
	if !found || sched.Quota <= 0 {
		return nil, 201, "Pendaftaran ke Poli ini tidak tersedia", nil
	}

	if bookedCount >= sched.Quota {
		return nil, 201, "Kuota penuuuh...!", nil
	}

	// Sequence generation
	noRegStr, err := s.CalculateNoReg(rsClinic, rsDoctor, req.TanggalPeriksa)
	if err != nil {
		return nil, 401, "Gagal menghitung nomor registrasi", err
	}
	noRegInt, _ := strconv.Atoi(noRegStr)

	var maxRawat int
	err = tx.QueryRow(`SELECT ifnull(MAX(CONVERT(RIGHT(no_rawat,6), signed)), 0) + 1
		FROM reg_periksa WHERE tgl_registrasi = ? FOR UPDATE`, req.TanggalPeriksa).Scan(&maxRawat)
	if err != nil {
		return nil, 401, "Gagal menghitung nomor rawat", err
	}
	noRawat := fmt.Sprintf("%s/%06d", strings.ReplaceAll(req.TanggalPeriksa, "-", "/"), maxRawat)

	var maxBooking int
	err = tx.QueryRow(`SELECT ifnull(MAX(CONVERT(RIGHT(nobooking,6), signed)), 0) + 1
		FROM referensi_mobilejkn_bpjs WHERE tanggalperiksa = ? FOR UPDATE`, req.TanggalPeriksa).Scan(&maxBooking)
	if err != nil {
		return nil, 401, "Gagal menghitung nomor booking", err
	}
	noBooking := fmt.Sprintf("%s%06d", strings.ReplaceAll(req.TanggalPeriksa, "-", ""), maxBooking)

	// Compute status and waiting time
	var statusPoli string = "Baru"
	var prevPoliVisit int
	_ = tx.QueryRow(`SELECT COUNT(no_rkm_medis) FROM reg_periksa WHERE no_rkm_medis = ? AND kd_poli = ?`, patient.NoRkmMedis, rsClinic).Scan(&prevPoliVisit)
	if prevPoliVisit > 0 {
		statusPoli = "Lama"
	}

	loc, err := time.LoadLocation("Asia/Jakarta")
	if err != nil {
		loc = time.Local
	}

	waktuTungguMinutes := noRegInt * 5
	parsedDate, _ := time.ParseInLocation("2006-01-02 15:04", fmt.Sprintf("%s %s", req.TanggalPeriksa, startTime), loc)
	estimasiMillis := parsedDate.Add(time.Duration(waktuTungguMinutes) * time.Minute).UnixMilli()

	caraBayar := s.auth.CaraBayar
	if caraBayar == "" {
		caraBayar = "BPJ"
	}

	umur := patient.UmurTahun
	sttsUmur := "Th"
	if umur == 0 {
		if patient.UmurBulan > 0 {
			umur = patient.UmurBulan
			sttsUmur = "Bl"
		} else {
			umur = patient.UmurHari
			sttsUmur = "Hr"
		}
	}

	sttsDaftar := "0"
	sttsDaftarText := "Lama"
	if patient.TglDaftar == req.TanggalPeriksa {
		sttsDaftar = "1"
		sttsDaftarText = "Baru"
	}

	var regLamaCost float64
	_ = tx.QueryRow(`SELECT registrasilama FROM poliklinik WHERE kd_poli = ?`, rsClinic).Scan(&regLamaCost)

	// Inserts
	insertBookingQuery := `INSERT INTO referensi_mobilejkn_bpjs VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Belum', '0000-00-00 00:00:00', 'Belum')`
	_, err = tx.Exec(insertBookingQuery,
		noBooking, noRawat, req.NomorKartu, req.NIK, req.NoHP, req.KodePoli,
		sttsDaftar, patient.NoRkmMedis, req.TanggalPeriksa, req.KodeDokter, req.JamPraktek,
		formatJenisKunjungan(req.JenisKunjungan), req.NomorReferensi,
		fmt.Sprintf("%s-%s", rsClinic, noRegStr), noRegStr, estimasiMillis,
		sched.Quota-bookedCount-1, sched.Quota, sched.Quota-bookedCount-1, sched.Quota,
	)
	if err != nil {
		return nil, 401, "Gagal menyimpan referensi booking", err
	}

	pjAddress := fmt.Sprintf("%s, %s, %s, %s, %s",
		patient.AlamatPj, patient.Kelurahan, patient.Kecamatan, patient.Kabupaten, patient.Propinsi)

	insertRegQuery := `INSERT INTO reg_periksa VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Belum', ?, 'Ralan', ?, ?, ?, 'Belum Bayar', ?)`
	_, err = tx.Exec(insertRegQuery,
		noRegStr, noRawat, req.TanggalPeriksa, startTime+":00", rsDoctor,
		patient.NoRkmMedis, rsClinic, patient.NamaKlg, pjAddress, patient.Keluarga,
		regLamaCost, sttsDaftarText, caraBayar, umur, sttsUmur, statusPoli,
	)
	if err != nil {
		return nil, 401, "Gagal menyimpan registrasi periksa", err
	}

	if err := tx.Commit(); err != nil {
		return nil, 401, "Gagal mengonfirmasi transaksi antrean", err
	}

	res := &BookingResult{
		NomorAntrean:     fmt.Sprintf("%s-%s", rsClinic, noRegStr),
		AngkaAntrean:     noRegInt,
		KodeBooking:      noBooking,
		PasienBaru:       0,
		Norm:             patient.NoRkmMedis,
		NamaPoli:         sched.ClinicName,
		NamaDokter:       sched.DoctorName,
		EstimasiDilayani: estimasiMillis,
		SisaKuotaJKN:     sched.Quota - bookedCount - 1,
		KuotaJKN:         sched.Quota,
		SisaKuotaNonJKN:  sched.Quota - bookedCount - 1,
		KuotaNonJKN:      sched.Quota,
		Keterangan:       "Peserta harap 30 menit lebih awal guna pencatatan administrasi.",
	}

	return res, 200, "Ok", nil
}

func GetHariIndo(dateStr string) string {
	t, _ := time.Parse("2006-01-02", dateStr)
	switch t.Weekday() {
	case time.Sunday:
		return "AKHAD"
	case time.Monday:
		return "SENIN"
	case time.Tuesday:
		return "SELASA"
	case time.Wednesday:
		return "RABU"
	case time.Thursday:
		return "KAMIS"
	case time.Friday:
		return "JUMAT"
	case time.Saturday:
		return "SABTU"
	default:
		return ""
	}
}

func formatJenisKunjungan(k string) string {
	switch k {
	case "1":
		return "1 (Rujukan FKTP)"
	case "2":
		return "2 (Rujukan Internal)"
	case "3":
		return "3 (Kontrol)"
	case "4":
		return "4 (Rujukan Antar RS)"
	default:
		return "1 (Rujukan FKTP)"
	}
}
