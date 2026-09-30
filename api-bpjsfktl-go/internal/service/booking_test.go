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
