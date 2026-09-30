package handler

import (
	"bytes"
	"net/http"
	"net/http/httptest"
	"testing"

	"api-bpjsfktl-go/internal/cache"
	"api-bpjsfktl-go/internal/config"
	"api-bpjsfktl-go/internal/security"
	"api-bpjsfktl-go/internal/service"
	"api-bpjsfktl-go/internal/store"
)

func TestDualPrefixRouting(t *testing.T) {
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
	_ = c.Sync()
	auth, _ := store.LoadCredentials(db)
	repo := store.NewRepository(db)
	bookingSvc := service.NewBookingService(db, c, auth)
	opSvc := service.NewOperationsService(db)
	pasienSvc := service.NewPasienService(db, auth)

	r := SetupRouter(db, c, func() *store.InsuranceAuth { return auth }, repo, bookingSvc, opSvc, pasienSvc, "123!!abc**", 3660)

	// Test GET /
	reqRoot := httptest.NewRequest("GET", "/", nil)
	recRoot := httptest.NewRecorder()
	r.ServeHTTP(recRoot, reqRoot)
	if recRoot.Code != http.StatusOK {
		t.Errorf("expected 200 on GET /, got %d", recRoot.Code)
	}

	// Test GET /auth with valid headers
	reqAuth := httptest.NewRequest("GET", "/auth", nil)
	reqAuth.Header.Set("x-username", auth.Username)
	reqAuth.Header.Set("x-password", auth.Password)
	recAuth := httptest.NewRecorder()
	r.ServeHTTP(recAuth, reqAuth)
	if recAuth.Code != http.StatusOK {
		t.Errorf("expected 200 on GET /auth, got %d (body: %s)", recAuth.Code, recAuth.Body.String())
	}

	// Test GET /?url=auth (legacy query rewriting)
	reqQueryAuth := httptest.NewRequest("GET", "/?url=auth", nil)
	reqQueryAuth.Header.Set("x-username", auth.Username)
	reqQueryAuth.Header.Set("x-password", auth.Password)
	recQueryAuth := httptest.NewRecorder()
	r.ServeHTTP(recQueryAuth, reqQueryAuth)
	if recQueryAuth.Code != http.StatusOK {
		t.Errorf("expected 200 on GET /?url=auth, got %d (body: %s)", recQueryAuth.Code, recQueryAuth.Body.String())
	}

	// Test GET /index.php?url=auth (legacy apache rewrite target)
	reqIndexPhpAuth := httptest.NewRequest("GET", "/index.php?url=auth", nil)
	reqIndexPhpAuth.Header.Set("x-username", auth.Username)
	reqIndexPhpAuth.Header.Set("x-password", auth.Password)
	recIndexPhpAuth := httptest.NewRecorder()
	r.ServeHTTP(recIndexPhpAuth, reqIndexPhpAuth)
	if recIndexPhpAuth.Code != http.StatusOK {
		t.Errorf("expected 200 on GET /index.php?url=auth, got %d (body: %s)", recIndexPhpAuth.Code, recIndexPhpAuth.Body.String())
	}

	token, _ := security.GenerateToken(auth.Username, "123!!abc**", 3660)

	// Test POST /sisaantrean route registration
	reqSisa := httptest.NewRequest("POST", "/sisaantrean", bytes.NewBufferString(`{"kodebooking":"NONEXISTENT"}`))
	reqSisa.Header.Set("x-username", auth.Username)
	reqSisa.Header.Set("x-token", token)
	recSisa := httptest.NewRecorder()
	r.ServeHTTP(recSisa, reqSisa)
	if recSisa.Code == http.StatusNotFound || recSisa.Code == http.StatusMethodNotAllowed {
		t.Errorf("POST /sisaantrean route missing, got %d", recSisa.Code)
	}

	// Test POST /statusantreanfarmasi route registration
	reqFarmasi := httptest.NewRequest("POST", "/statusantreanfarmasi", bytes.NewBufferString(`{"kodebooking":"NONEXISTENT"}`))
	reqFarmasi.Header.Set("x-username", auth.Username)
	reqFarmasi.Header.Set("x-token", token)
	recFarmasi := httptest.NewRecorder()
	r.ServeHTTP(recFarmasi, reqFarmasi)
	if recFarmasi.Code == http.StatusNotFound || recFarmasi.Code == http.StatusMethodNotAllowed {
		t.Errorf("POST /statusantreanfarmasi route missing, got %d", recFarmasi.Code)
	}
}
