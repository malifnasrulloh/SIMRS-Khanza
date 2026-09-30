package service

import (
	"testing"
	"api-bpjsfktl-go/internal/config"
	"api-bpjsfktl-go/internal/store"
)

func TestGenerateNoRkmMedis(t *testing.T) {
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

	auth, _ := store.LoadCredentials(db)
	pSvc := NewPasienService(db, auth)

	tx, err := db.Begin()
	if err != nil {
		t.Fatalf("begin tx error: %v", err)
	}
	defer tx.Rollback()

	norm, err := pSvc.calculateNextNoRM(tx)
	if err != nil {
		t.Fatalf("failed to calculate next norm: %v", err)
	}
	if norm == "" {
		t.Errorf("empty medical record number generated")
	}
	t.Logf("generated medical record number: %s", norm)
}

func TestFormatTerminalAndMiddleDigit(t *testing.T) {
	// 6-digit sequence 123456
	// Straight: 123456
	// Terminal: digits [4:6] + [2:4] + [0:2] -> 56 + 34 + 12 = 563412
	// Middle:   digits [2:4] + [0:2] + [4:6] -> 34 + 12 + 56 = 341256
	num := 123456
	terminal := formatTerminalDigit(num)
	if terminal != "563412" {
		t.Errorf("expected 563412 for terminal digit, got %s", terminal)
	}

	middle := formatMiddleDigit(num)
	if middle != "341256" {
		t.Errorf("expected 341256 for middle digit, got %s", middle)
	}
}
