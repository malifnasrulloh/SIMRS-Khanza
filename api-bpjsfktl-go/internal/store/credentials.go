package store

import (
	"database/sql"
	"fmt"
)

type InsuranceAuth struct {
	Username  string
	Password  string
	CaraBayar string
}

func LoadCredentials(db *sql.DB) (*InsuranceAuth, error) {
	query := `SELECT kd_pj,
		IFNULL(CAST(AES_DECRYPT(usere, 'nur') AS CHAR), ''),
		IFNULL(CAST(AES_DECRYPT(passworde, 'windi') AS CHAR), '')
		FROM password_asuransi LIMIT 1`

	var auth InsuranceAuth
	err := db.QueryRow(query).Scan(&auth.CaraBayar, &auth.Username, &auth.Password)
	if err != nil {
		return nil, fmt.Errorf("query password_asuransi: %w", err)
	}
	return &auth, nil
}
