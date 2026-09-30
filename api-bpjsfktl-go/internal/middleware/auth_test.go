package middleware

import (
	"net/http"
	"net/http/httptest"
	"testing"

	"api-bpjsfktl-go/internal/security"
	"api-bpjsfktl-go/internal/store"
)

func TestAuthMiddleware(t *testing.T) {
	secret := "123!!abc**"
	mockAuth := func() *store.InsuranceAuth {
		return &store.InsuranceAuth{
			Username:  "admin",
			Password:  "pass",
			CaraBayar: "BPJ",
		}
	}

	token, _ := security.GenerateToken("admin", secret, 3600)

	nextHandler := http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		w.WriteHeader(http.StatusOK)
		_, _ = w.Write([]byte("authorized"))
	})

	mw := RequireAuth(secret, mockAuth)
	handler := mw(nextHandler)

	// Valid headers
	req := httptest.NewRequest("POST", "/statusantrean", nil)
	req.Header.Set("x-username", "admin")
	req.Header.Set("x-token", token)
	rec := httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != http.StatusOK {
		t.Errorf("expected 200, got %d", rec.Code)
	}

	// Missing token
	req = httptest.NewRequest("POST", "/statusantrean", nil)
	req.Header.Set("x-username", "admin")
	rec = httptest.NewRecorder()
	handler.ServeHTTP(rec, req)
	if rec.Code != 201 {
		t.Errorf("expected 201 on missing token, got %d", rec.Code)
	}
}
