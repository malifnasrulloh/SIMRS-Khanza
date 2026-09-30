package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"os/signal"
	"syscall"
	"time"

	"api-bpjsfktl-go/internal/cache"
	"api-bpjsfktl-go/internal/config"
	"api-bpjsfktl-go/internal/handler"
	"api-bpjsfktl-go/internal/service"
	"api-bpjsfktl-go/internal/store"
)

func main() {
	configPath := "config.json"
	if len(os.Args) > 1 {
		configPath = os.Args[1]
	}

	cfg, err := config.Load(configPath)
	if err != nil {
		log.Fatalf("Failed to load config: %v", err)
	}

	db, err := store.NewDB(cfg.Database)
	if err != nil {
		log.Fatalf("Failed to connect to database: %v", err)
	}
	defer db.Close()
	log.Printf("Connected to MySQL database %s", cfg.Database.DBName)

	memCache := cache.NewMemoryCache(db)
	if err := memCache.Sync(); err != nil {
		log.Fatalf("Initial cache sync failed: %v", err)
	}
	stopCache := make(chan struct{})
	memCache.StartAutoSync(time.Duration(cfg.Cache.SyncIntervalMinutes)*time.Minute, stopCache)
	defer close(stopCache)
	log.Printf("In-memory cache warmed up successfully")

	authCreds := memCache.GetCredentials()
	if authCreds == nil {
		log.Fatalf("Failed to load credentials from password_asuransi")
	}
	log.Printf("Loaded BPJS API credentials for user: %s (cara bayar: %s)", authCreds.Username, authCreds.CaraBayar)

	repo := store.NewRepository(db)
	bookingSvc := service.NewBookingService(db, memCache, authCreds)
	opSvc := service.NewOperationsService(db)
	pasienSvc := service.NewPasienService(db, authCreds)

	router := handler.SetupRouter(
		db, memCache, func() *store.InsuranceAuth { return memCache.GetCredentials() },
		repo, bookingSvc, opSvc, pasienSvc,
		cfg.JWT.SecretKey, cfg.JWT.ExpirationSeconds,
	)

	serverAddr := fmt.Sprintf(":%d", cfg.Server.Port)
	srv := &http.Server{
		Addr:         serverAddr,
		Handler:      router,
		ReadTimeout:  time.Duration(cfg.Server.ReadTimeoutSeconds) * time.Second,
		WriteTimeout: time.Duration(cfg.Server.WriteTimeoutSeconds) * time.Second,
	}

	go func() {
		log.Printf("Starting BPJS FKTL Inbound Microservice on port %d...", cfg.Server.Port)
		if err := srv.ListenAndServe(); err != nil && err != http.ErrServerClosed {
			log.Fatalf("Server listen error: %v", err)
		}
	}()

	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit
	log.Println("Shutting down server gracefully...")

	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	if err := srv.Shutdown(ctx); err != nil {
		log.Printf("Server shutdown forced: %v", err)
	}
	log.Println("Server stopped")
}
