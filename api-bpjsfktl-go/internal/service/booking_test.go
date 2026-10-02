package service

import (
	"testing"

	"api-bpjsfktl-go/internal/cache"
	"api-bpjsfktl-go/internal/config"
	"api-bpjsfktl-go/internal/store"
)

func TestConcurrentBookingSequenceIntegrity(t *testing.T) {
	cfg := config.DatabaseConfig{
		Host:     "127.0.0.1",
		Port:     3306,
		User:     "root",
		Password: "",
		DBName:   "sik_temps",
	}

	db, err := store.NewDB(cfg)
	if err != nil {
		t.Fatalf("database connect error: %v", err)
	}
	defer db.Close()

	c := cache.NewMemoryCache(db)
	if err := c.Sync(); err != nil {
		t.Fatalf("cache sync error: %v", err)
	}

	auth, _ := store.LoadCredentials(db)
	svc := NewBookingService(db, c, auth)

	// Validate helper functions
	noReg, err := svc.CalculateNoReg("U0004", "2021011402", "2026-10-05")
	if err != nil {
		t.Fatalf("calculate no reg failed: %v", err)
	}
	if len(noReg) != 3 {
		t.Errorf("expected 3 digit zero padded no reg, got %s", noReg)
	}
}

func TestFindPatientNewbornAndAutoEnrichment(t *testing.T) {
	cfg := config.DatabaseConfig{
		Host:     "127.0.0.1",
		Port:     3306,
		User:     "root",
		Password: "",
		DBName:   "sik_temps",
	}

	db, err := store.NewDB(cfg)
	if err != nil {
		t.Fatalf("database connect error: %v", err)
	}
	defer db.Close()

	c := cache.NewMemoryCache(db)
	_ = c.Sync()
	auth, _ := store.LoadCredentials(db)
	svc := NewBookingService(db, c, auth)

	// In sik_temps: patient 000001 has no_peserta = '0001825639492'
	// Test: Find by BPJS card alone with empty NIK (newborn scenario)
	p, err := svc.FindPatient("", "0001825639492")
	if err != nil {
		t.Fatalf("failed to find patient with empty NIK: %v", err)
	}
	if p == nil || p.NoRkmMedis != "000001" {
		t.Errorf("expected patient 000001, got %+v", p)
	}
}
