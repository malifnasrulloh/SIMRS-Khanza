package config

import (
	"os"
	"path/filepath"
	"testing"
)

func TestLoadConfig(t *testing.T) {
	tempDir := t.TempDir()
	configPath := filepath.Join(tempDir, "config.json")
	content := `{
		"server": { "port": 8088, "read_timeout_seconds": 15, "write_timeout_seconds": 15 },
		"database": {
			"driver": "mysql",
			"host": "127.0.0.1",
			"port": 3306,
			"user": "root",
			"password": "",
			"dbname": "sik_temps",
			"max_open_conns": 50,
			"max_idle_conns": 25,
			"conn_max_lifetime_minutes": 5
		},
		"jwt": { "secret_key": "123!!abc**", "expiration_seconds": 3660 },
		"cache": { "sync_interval_minutes": 5 }
	}`
	if err := os.WriteFile(configPath, []byte(content), 0644); err != nil {
		t.Fatalf("failed to write temp config: %v", err)
	}

	cfg, err := Load(configPath)
	if err != nil {
		t.Fatalf("unexpected error loading config: %v", err)
	}

	if cfg.Server.Port != 8088 {
		t.Errorf("expected port 8088, got %d", cfg.Server.Port)
	}
	if cfg.Database.DBName != "sik_temps" {
		t.Errorf("expected dbname sik_temps, got %s", cfg.Database.DBName)
	}
	if cfg.JWT.SecretKey != "123!!abc**" {
		t.Errorf("expected secret 123!!abc**, got %s", cfg.JWT.SecretKey)
	}
	if cfg.Database.DSN() != "root:@tcp(127.0.0.1:3306)/sik_temps?parseTime=true&loc=Asia%2FJakarta" {
		t.Errorf("unexpected DSN string: %s", cfg.Database.DSN())
	}
}
