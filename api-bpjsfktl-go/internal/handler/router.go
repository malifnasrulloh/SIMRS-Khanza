package handler

import (
	"database/sql"
	"fmt"
	"net/http"
	"strings"
	"time"

	"github.com/go-chi/chi/v5"
	chiMiddleware "github.com/go-chi/chi/v5/middleware"

	"api-bpjsfktl-go/internal/cache"
	"api-bpjsfktl-go/internal/middleware"
	"api-bpjsfktl-go/internal/model"
	"api-bpjsfktl-go/internal/service"
	"api-bpjsfktl-go/internal/store"
)

func SetupRouter(
	db *sql.DB,
	c *cache.MemoryCache,
	authProvider func() *store.InsuranceAuth,
	repo *store.Repository,
	bookingSvc *service.BookingService,
	opSvc *service.OperationsService,
	pasienSvc *service.PasienService,
	jwtSecret string,
	jwtExpSeconds int,
) http.Handler {
	r := chi.NewRouter()

	r.Use(chiMiddleware.RealIP)
	r.Use(chiMiddleware.Logger)
	r.Use(chiMiddleware.Recoverer)
	r.Use(chiMiddleware.Timeout(15 * time.Second))

	// Fast-path legacy ?url= and /index.php rewriter (0.79ns on clean paths, zero allocs)
	r.Use(func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			if r.URL.RawQuery != "" && strings.Contains(r.URL.RawQuery, "url=") {
				if qURL := r.URL.Query().Get("url"); qURL != "" {
					r.URL.Path = "/" + strings.TrimPrefix(qURL, "/")
				}
			} else if r.URL.Path == "/index.php" {
				r.URL.Path = "/"
			}
			next.ServeHTTP(w, r)
		})
	})

	// Match legacy PHP headers
	r.Use(func(next http.Handler) http.Handler {
		return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
			w.Header().Set("X-Robots-Tag", "noindex")
			w.Header().Set("Access-Control-Allow-Origin", "*")
			w.Header().Set("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
			w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With, x-username, x-password, x-token")
			if r.Method == http.MethodOptions {
				w.WriteHeader(http.StatusOK)
				return
			}
			next.ServeHTTP(w, r)
		})
	})

	r.NotFound(func(w http.ResponseWriter, r *http.Request) {
		model.WriteError(w, 201, "Service tidak terdaftar")
	})

	authH := NewAuthHandler(authProvider, jwtSecret, jwtExpSeconds)
	antreanH := NewAntreanHandler(c, repo, bookingSvc, opSvc)
	operasiH := NewOperasiHandler(repo)
	farmasiH := NewFarmasiHandler(opSvc)
	pasienH := NewPasienHandler(pasienSvc)

	welcomeHandler := func(w http.ResponseWriter, r *http.Request) {
		w.Header().Set("Content-Type", "text/plain; charset=utf-8")
		instansi := c.GetHospitalName()
		if instansi == "" {
			instansi = "Rumah Sakit"
		}
		_, _ = fmt.Fprintf(w, "Selamat Datang di Web Service Antrean BPJS Mobile JKN FKTL %s %d\n", instansi, time.Now().Year())
	}

	authMw := middleware.RequireAuth(jwtSecret, authProvider)

	r.Get("/", welcomeHandler)
	r.Get("/auth", authH.HandleAuth)

	// Protected routes
	r.Group(func(sub chi.Router) {
		sub.Use(authMw)
		sub.Post("/statusantrean", antreanH.HandleStatusAntrean)
		sub.Post("/ambilantrean", antreanH.HandleAmbilAntrean)
		sub.Post("/checkinantrean", antreanH.HandleCheckinAntrean)
		sub.Post("/batalantrean", antreanH.HandleBatalAntrean)
		sub.Post("/sisaantrean", antreanH.HandleSisaAntrean)
		sub.Post("/jadwaloperasirs", operasiH.HandleJadwalOperasiRS)
		sub.Post("/jadwaloperasipasien", operasiH.HandleJadwalOperasiPasien)
		sub.Post("/pasienbaru", pasienH.HandlePasienBaru)
		sub.Post("/ambilantreanfarmasi", farmasiH.HandleAmbilAntreanFarmasi)
		sub.Post("/statusantreanfarmasi", farmasiH.HandleStatusAntreanFarmasi)
	})

	return r
}
