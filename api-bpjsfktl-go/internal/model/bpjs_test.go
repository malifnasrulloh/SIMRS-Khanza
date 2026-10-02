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

func TestFlexibleTypes(t *testing.T) {
	type Sample struct {
		StrVal FlexibleString `json:"str_val"`
		IntVal FlexibleInt64  `json:"int_val"`
	}

	// 1. Quoted string and numeric int
	json1 := `{"str_val": "217354", "int_val": 1790730000000}`
	var s1 Sample
	if err := json.Unmarshal([]byte(json1), &s1); err != nil {
		t.Fatalf("unmarshal s1 failed: %v", err)
	}
	if s1.StrVal.String() != "217354" || s1.IntVal.Int64() != 1790730000000 {
		t.Errorf("unexpected s1: %+v", s1)
	}

	// 2. Unquoted number into string, quoted string into int
	json2 := `{"str_val": 217354, "int_val": "1790730000000"}`
	var s2 Sample
	if err := json.Unmarshal([]byte(json2), &s2); err != nil {
		t.Fatalf("unmarshal s2 failed: %v", err)
	}
	if s2.StrVal.String() != "217354" || s2.IntVal.Int64() != 1790730000000 {
		t.Errorf("unexpected s2: %+v", s2)
	}

	// 3. Null values
	json3 := `{"str_val": null, "int_val": null}`
	var s3 Sample
	if err := json.Unmarshal([]byte(json3), &s3); err != nil {
		t.Fatalf("unmarshal s3 failed: %v", err)
	}
	if s3.StrVal.String() != "" || s3.IntVal.Int64() != 0 {
		t.Errorf("unexpected s3: %+v", s3)
	}
}
