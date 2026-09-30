package model

import (
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"testing"
)

func TestWriteOK(t *testing.T) {
	rec := httptest.NewRecorder()
	payload := map[string]string{"token": "xyz123"}
	WriteOK(rec, payload)

	if rec.Code != http.StatusOK {
		t.Errorf("expected http 200, got %d", rec.Code)
	}

	var env Envelope
	if err := json.Unmarshal(rec.Body.Bytes(), &env); err != nil {
		t.Fatalf("failed to parse envelope: %v", err)
	}

	if env.Metadata.Code != 200 || env.Metadata.Message != "Ok" {
		t.Errorf("expected metadata 200 Ok, got %d %s", env.Metadata.Code, env.Metadata.Message)
	}
}

func TestWriteError(t *testing.T) {
	rec := httptest.NewRecorder()
	WriteError(rec, 201, "Kode Poli tidak boleh kosong")

	if rec.Code != 201 {
		t.Errorf("expected http status 201, got %d", rec.Code)
	}

	var env Envelope
	if err := json.Unmarshal(rec.Body.Bytes(), &env); err != nil {
		t.Fatalf("failed to parse envelope: %v", err)
	}

	if env.Metadata.Code != 201 || env.Metadata.Message != "Kode Poli tidak boleh kosong" {
		t.Errorf("unexpected metadata: %+v", env.Metadata)
	}
	if env.Response != nil {
		t.Errorf("expected nil response on error, got %v", env.Response)
	}
}
