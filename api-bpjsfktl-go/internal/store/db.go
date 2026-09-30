package store

import (
	"database/sql"
	"fmt"
	"time"

	_ "github.com/go-sql-driver/mysql"
	"api-bpjsfktl-go/internal/config"
)

func NewDB(cfg config.DatabaseConfig) (*sql.DB, error) {
	db, err := sql.Open("mysql", cfg.DSN())
	if err != nil {
		return nil, fmt.Errorf("open mysql connection: %w", err)
	}

	maxOpen := cfg.MaxOpenConns
	if maxOpen <= 0 {
		maxOpen = 50
	}
	maxIdle := cfg.MaxIdleConns
	if maxIdle <= 0 {
		maxIdle = 25
	}
	lifetime := time.Duration(cfg.ConnMaxLifetimeMinutes) * time.Minute
	if lifetime <= 0 {
		lifetime = 5 * time.Minute
	}

	db.SetMaxOpenConns(maxOpen)
	db.SetMaxIdleConns(maxIdle)
	db.SetConnMaxLifetime(lifetime)
	db.SetConnMaxIdleTime(2 * time.Minute)

	if err := db.Ping(); err != nil {
		db.Close()
		return nil, fmt.Errorf("ping mysql database: %w", err)
	}

	EnsureIndexes(db, cfg.DBName)

	return db, nil
}

func EnsureIndexes(db *sql.DB, dbName string) {
	indexes := []struct {
		table string
		index string
		cols  string
	}{
		{"reg_periksa", "idx_reg_periksa_tgl_poli_dokter", "tgl_registrasi, kd_poli, kd_dokter"},
		{"referensi_mobilejkn_bpjs", "idx_ref_mobilejkn_ref_status", "nomorreferensi, status"},
		{"booking_operasi", "idx_booking_operasi_tgl", "tanggal, jam_mulai"},
	}

	for _, idx := range indexes {
		var count int
		q := `SELECT COUNT(*) FROM information_schema.statistics
			WHERE table_schema = ? AND table_name = ? AND index_name = ?`
		_ = db.QueryRow(q, dbName, idx.table, idx.index).Scan(&count)
		if count == 0 {
			createSQL := fmt.Sprintf("CREATE INDEX %s ON %s (%s)", idx.index, idx.table, idx.cols)
			_, _ = db.Exec(createSQL)
		}
	}
}
