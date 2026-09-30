package store

import (
	"testing"
	"api-bpjsfktl-go/internal/config"
)

func TestQueryOperationsAgainstSikTemps(t *testing.T) {
	cfg := config.DatabaseConfig{
		Host:     "127.0.0.1",
		Port:     3306,
		User:     "root",
		Password: "",
		DBName:   "sik_temps",
	}

	db, err := NewDB(cfg)
	if err != nil {
		t.Fatalf("database connect error: %v", err)
	}
	defer db.Close()

	repo := NewRepository(db)

	// Test Status Antrean (non-existent or sample date)
	res, err := repo.GetStatusAntrean("U0003", "D0000013", "2026-09-30")
	if err != nil {
		t.Fatalf("GetStatusAntrean query error: %v", err)
	}
	if res.TotalAntrean < 0 {
		t.Errorf("unexpected negative antrean count")
	}

	// Test Surgery RS query
	surgeries, err := repo.GetJadwalOperasiRS("2026-01-01", "2026-12-31")
	if err != nil {
		t.Fatalf("GetJadwalOperasiRS query error: %v", err)
	}
	t.Logf("retrieved %d surgeries in 2026", len(surgeries))
}
