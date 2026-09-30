package cache

import (
	"testing"
	"api-bpjsfktl-go/internal/config"
	"api-bpjsfktl-go/internal/store"
)

func TestMemoryCacheSyncAndLookup(t *testing.T) {
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

	c := NewMemoryCache(db)
	if err := c.Sync(); err != nil {
		t.Fatalf("cache sync failed: %v", err)
	}

	// In sik_temps: 470937 -> D0000013 (dr. CUPUWATIE CAHYANI, Sp.PD)
	rsDoctor, found := c.GetDoctorMapping("470937")
	if !found || rsDoctor != "D0000013" {
		t.Errorf("expected D0000013, got %s (found: %v)", rsDoctor, found)
	}

	// In sik_temps: INT -> U0003
	rsClinic, found := c.GetClinicMapping("INT")
	if !found || rsClinic != "U0003" {
		t.Errorf("expected U0003, got %s (found: %v)", rsClinic, found)
	}
}
