package config

import (
	"encoding/json"
	"fmt"
	"net/url"
	"os"
)

type ServerConfig struct {
	Port                int `json:"port"`
	ReadTimeoutSeconds  int `json:"read_timeout_seconds"`
	WriteTimeoutSeconds int `json:"write_timeout_seconds"`
}

type DatabaseConfig struct {
	Driver                 string `json:"driver"`
	Host                   string `json:"host"`
	Port                   int    `json:"port"`
	User                   string `json:"user"`
	Password               string `json:"password"`
	DBName                 string `json:"dbname"`
	MaxOpenConns           int    `json:"max_open_conns"`
	MaxIdleConns           int    `json:"max_idle_conns"`
	ConnMaxLifetimeMinutes int    `json:"conn_max_lifetime_minutes"`
}

func (d DatabaseConfig) DSN() string {
	loc := url.QueryEscape("Asia/Jakarta")
	return fmt.Sprintf("%s:%s@tcp(%s:%d)/%s?parseTime=true&loc=%s",
		d.User, d.Password, d.Host, d.Port, d.DBName, loc)
}

type JWTConfig struct {
	SecretKey         string `json:"secret_key"`
	ExpirationSeconds int    `json:"expiration_seconds"`
}

type CacheConfig struct {
	SyncIntervalMinutes int `json:"sync_interval_minutes"`
}

type Config struct {
	Server   ServerConfig   `json:"server"`
	Database DatabaseConfig `json:"database"`
	JWT      JWTConfig      `json:"jwt"`
	Cache    CacheConfig    `json:"cache"`
}

func Load(path string) (*Config, error) {
	data, err := os.ReadFile(path)
	if err != nil {
		return nil, fmt.Errorf("read config file: %w", err)
	}

	var cfg Config
	if err := json.Unmarshal(data, &cfg); err != nil {
		return nil, fmt.Errorf("parse config json: %w", err)
	}

	if cfg.Server.Port == 0 {
		cfg.Server.Port = 8088
	}
	if cfg.Database.MaxOpenConns == 0 {
		cfg.Database.MaxOpenConns = 50
	}
	if cfg.Database.MaxIdleConns == 0 {
		cfg.Database.MaxIdleConns = 25
	}
	if cfg.JWT.SecretKey == "" {
		cfg.JWT.SecretKey = "123!!abc**"
	}
	if cfg.JWT.ExpirationSeconds == 0 {
		cfg.JWT.ExpirationSeconds = 3660
	}
	return &cfg, nil
}
