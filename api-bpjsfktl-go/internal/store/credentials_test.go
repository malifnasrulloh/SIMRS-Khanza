package store

import (
	"testing"
	"api-bpjsfktl-go/internal/config"
)

func TestLoadCredentialsFromSikTemps(t *testing.T) {
	cfg := config.DatabaseConfig{
		Host:     "127.0.0.1",
		Port:     3306,
		User:     "root",
		Password: "",
		DBName:   "sik_temps",
	}

	db, err := NewDB(cfg)
	if err != nil {
		t.Fatalf("failed to connect to sik_temps: %v", err)
	}
	defer db.Close()

	auth, err := LoadCredentials(db)
	if err != nil {
		t.Fatalf("failed to load credentials: %v", err)
	}

	if auth.Username == "" || auth.Password == "" {
		t.Errorf("empty decrypted credentials: %+v", auth)
	}
	if auth.CaraBayar != "BPJ" {
		t.Errorf("expected kd_pj BPJ, got %s", auth.CaraBayar)
	}
}
