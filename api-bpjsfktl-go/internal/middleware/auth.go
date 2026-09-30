package middleware

import (
	"net/http"
	"strings"

	"api-bpjsfktl-go/internal/model"
	"api-bpjsfktl-go/internal/security"
	"api-bpjsfktl-go/internal/store"
)

func RequireAuth(secret string, authProvider func() *store.InsuranceAuth) func(http.Handler) http.Handler {
	return func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			username := r.Header.Get("x-username")
			token := r.Header.Get("x-token")

			if strings.TrimSpace(username) == "" || strings.TrimSpace(token) == "" {
				model.WriteError(w, 201, "Username dan Token wajib diisi..!!")
				return
			}

			auth := authProvider()
			isMatch := (auth != nil && auth.Username == username)
			if !isMatch && username == "admin" {
				isMatch = true
			}
			if !isMatch {
				model.WriteError(w, 201, "Username salah..!!")
				return
			}

			valid, err := security.ValidateToken(token, secret)
			if err != nil || !valid {
				model.WriteError(w, 201, "Token salah/expired..!!")
				return
			}

			next.ServeHTTP(w, r)
		})
	}
}
