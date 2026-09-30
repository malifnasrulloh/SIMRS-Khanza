package service

import (
	"testing"
	"time"

	"api-bpjsfktl-go/internal/config"
	"api-bpjsfktl-go/internal/store"
)

func TestCheckinValidation(t *testing.T) {
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

	opSvc := NewOperationsService(db)

	// Test non-existent booking code
	code, msg, err := opSvc.CheckinQueue("INVALIDBOOKING123", 1790730000000)
	if err != nil {
		t.Fatalf("checkin error: %v", err)
	}
	if code != 201 || msg != "Data Booking tidak ditemukan" {
		t.Errorf("expected 201 Data Booking tidak ditemukan, got %d %s", code, msg)
	}
}

func TestCheckinTimeWindowCalculation(t *testing.T) {
	// Schedule: 2026-10-05 08:00 (WIB / UTC+7)
	// On-time check-in: 08:15 WIB -> diff = +15 minutes (valid)
	loc, err := time.LoadLocation("Asia/Jakarta")
	if err != nil {
		t.Fatalf("failed to load Asia/Jakarta timezone: %v", err)
	}

	schedTime := time.Date(2026, 10, 5, 8, 0, 0, 0, loc)
	checkinTime := time.Date(2026, 10, 5, 8, 15, 0, 0, loc)

	diff := calculateCheckinDiffMinutes(schedTime, checkinTime.UnixMilli())
	if diff != 15 {
		t.Errorf("expected diff +15 minutes, got %d", diff)
	}

	// Expired check-in: 09:30 WIB -> diff = +90 minutes (expired)
	checkinExpired := time.Date(2026, 10, 5, 9, 30, 0, 0, loc)
	diffExp := calculateCheckinDiffMinutes(schedTime, checkinExpired.UnixMilli())
	if diffExp != 90 {
		t.Errorf("expected diff +90 minutes, got %d", diffExp)
	}
}
