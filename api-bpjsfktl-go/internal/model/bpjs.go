package model

import (
	"encoding/json"
	"net/http"
)

type Metadata struct {
	Message string `json:"message"`
	Code    int    `json:"code"`
}

type Envelope struct {
	Response any      `json:"response,omitempty"`
	Metadata Metadata `json:"metadata"`
}

func WriteResponse(w http.ResponseWriter, httpCode int, message string, data any) {
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(httpCode)

	env := Envelope{
		Response: data,
		Metadata: Metadata{
			Message: message,
			Code:    httpCode,
		},
	}
	_ = json.NewEncoder(w).Encode(env)
}

func WriteOK(w http.ResponseWriter, data any) {
	WriteResponse(w, http.StatusOK, "Ok", data)
}

func WriteError(w http.ResponseWriter, code int, message string) {
	WriteResponse(w, code, message, nil)
}
