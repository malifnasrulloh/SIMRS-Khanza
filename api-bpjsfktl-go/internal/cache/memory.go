package cache

import (
	"database/sql"
	"fmt"
	"strings"
	"sync"
	"time"

	"api-bpjsfktl-go/internal/store"
)

type ScheduleInfo struct {
	DoctorCode string
	DoctorName string
	ClinicCode string
	ClinicName string
	WorkDay    string
	StartTime  string
	EndTime    string
	Quota      int
}

type MemoryCache struct {
	db             *sql.DB
	mu             sync.RWMutex
	doctorBPJSToRS map[string]string
	clinicBPJSToRS map[string]string
	schedules      map[string]*ScheduleInfo
	hospitalName   string
	auth           *store.InsuranceAuth
	lastSync       time.Time
}

func NewMemoryCache(db *sql.DB) *MemoryCache {
	return &MemoryCache{
		db:             db,
		doctorBPJSToRS: make(map[string]string),
		clinicBPJSToRS: make(map[string]string),
		schedules:      make(map[string]*ScheduleInfo),
	}
}

func (c *MemoryCache) Sync() error {
	// 1. Doctor mappings
	docRows, err := c.db.Query(`SELECT kd_dokter, kd_dokter_bpjs FROM maping_dokter_dpjpvclaim`)
	if err != nil {
		return fmt.Errorf("sync doctor mappings: %w", err)
	}
	defer docRows.Close()

	newDoctors := make(map[string]string)
	for docRows.Next() {
		var rsCode, bpjsCode string
		if err := docRows.Scan(&rsCode, &bpjsCode); err == nil {
			newDoctors[strings.TrimSpace(bpjsCode)] = strings.TrimSpace(rsCode)
		}
	}

	// 2. Clinic mappings
	cliRows, err := c.db.Query(`SELECT kd_poli_rs, kd_poli_bpjs FROM maping_poli_bpjs`)
	if err != nil {
		return fmt.Errorf("sync clinic mappings: %w", err)
	}
	defer cliRows.Close()

	newClinics := make(map[string]string)
	for cliRows.Next() {
		var rsCode, bpjsCode string
		if err := cliRows.Scan(&rsCode, &bpjsCode); err == nil {
			newClinics[strings.TrimSpace(bpjsCode)] = strings.TrimSpace(rsCode)
		}
	}

	// 3. Schedules and quotas
	schedQuery := `SELECT j.kd_dokter, d.nm_dokter, j.kd_poli, p.nm_poli,
		j.hari_kerja, j.jam_mulai, j.jam_selesai, j.kuota
		FROM jadwal j
		INNER JOIN dokter d ON j.kd_dokter = d.kd_dokter
		INNER JOIN poliklinik p ON j.kd_poli = p.kd_poli`
	schedRows, err := c.db.Query(schedQuery)
	if err != nil {
		return fmt.Errorf("sync schedules: %w", err)
	}
	defer schedRows.Close()

	newSchedules := make(map[string]*ScheduleInfo)
	for schedRows.Next() {
		var s ScheduleInfo
		if err := schedRows.Scan(&s.DoctorCode, &s.DoctorName, &s.ClinicCode, &s.ClinicName,
			&s.WorkDay, &s.StartTime, &s.EndTime, &s.Quota); err == nil {
			if len(s.StartTime) >= 5 && len(s.EndTime) >= 5 {
				key := fmt.Sprintf("%s:%s:%s:%s:%s",
					strings.ToUpper(strings.TrimSpace(s.WorkDay)),
					strings.TrimSpace(s.DoctorCode),
					strings.TrimSpace(s.ClinicCode),
					s.StartTime[:5],
					s.EndTime[:5])
				newSchedules[key] = &s
			}
		}
	}

	// 4. Hospital name
	var hospitalName string
	_ = c.db.QueryRow(`SELECT nama_instansi FROM setting LIMIT 1`).Scan(&hospitalName)

	// 5. Dynamic Auth Credentials from password_asuransi
	auth, _ := store.LoadCredentials(c.db)

	c.mu.Lock()
	c.doctorBPJSToRS = newDoctors
	c.clinicBPJSToRS = newClinics
	c.schedules = newSchedules
	c.hospitalName = hospitalName
	if auth != nil {
		c.auth = auth
	}
	c.lastSync = time.Now()
	c.mu.Unlock()

	return nil
}

func (c *MemoryCache) StartAutoSync(interval time.Duration, stopCh <-chan struct{}) {
	ticker := time.NewTicker(interval)
	go func() {
		for {
			select {
			case <-ticker.C:
				_ = c.Sync()
			case <-stopCh:
				ticker.Stop()
				return
			}
		}
	}()
}

func (c *MemoryCache) GetDoctorMapping(bpjsCode string) (string, bool) {
	c.mu.RLock()
	defer c.mu.RUnlock()
	val, ok := c.doctorBPJSToRS[strings.TrimSpace(bpjsCode)]
	return val, ok
}

func (c *MemoryCache) GetClinicMapping(bpjsCode string) (string, bool) {
	c.mu.RLock()
	defer c.mu.RUnlock()
	val, ok := c.clinicBPJSToRS[strings.TrimSpace(bpjsCode)]
	return val, ok
}

func (c *MemoryCache) GetSchedule(day, doctorCode, clinicCode, startTime, endTime string) (*ScheduleInfo, bool) {
	c.mu.RLock()
	defer c.mu.RUnlock()

	if len(startTime) < 5 || len(endTime) < 5 {
		return nil, false
	}

	key := fmt.Sprintf("%s:%s:%s:%s:%s",
		strings.ToUpper(strings.TrimSpace(day)),
		strings.TrimSpace(doctorCode),
		strings.TrimSpace(clinicCode),
		startTime[:5],
		endTime[:5])

	sched, ok := c.schedules[key]
	return sched, ok
}

func (c *MemoryCache) GetHospitalName() string {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.hospitalName
}

func (c *MemoryCache) GetCredentials() *store.InsuranceAuth {
	c.mu.RLock()
	defer c.mu.RUnlock()
	return c.auth
}
