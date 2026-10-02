package model

import (
	"encoding/json"
	"net/http"
	"strconv"
	"strings"
)

type Metadata struct {
	Message string `json:"message"`
	Code    int    `json:"code"`
}

type Envelope struct {
	Response any      `json:"response,omitempty"`
	Metadata Metadata `json:"metadata"`
}

type FlexibleString string

func (f *FlexibleString) UnmarshalJSON(data []byte) error {
	s := strings.TrimSpace(string(data))
	if s == "null" || s == "" {
		*f = ""
		return nil
	}
	if len(s) >= 2 && s[0] == '"' && s[len(s)-1] == '"' {
		var str string
		if err := json.Unmarshal(data, &str); err != nil {
			return err
		}
		*f = FlexibleString(strings.TrimSpace(str))
		return nil
	}
	*f = FlexibleString(s)
	return nil
}

func (f FlexibleString) String() string {
	return string(f)
}

type FlexibleInt64 int64

func (f *FlexibleInt64) UnmarshalJSON(data []byte) error {
	s := strings.TrimSpace(string(data))
	if s == "null" || s == "" {
		*f = 0
		return nil
	}
	if len(s) >= 2 && s[0] == '"' && s[len(s)-1] == '"' {
		var str string
		if err := json.Unmarshal(data, &str); err != nil {
			return err
		}
		str = strings.TrimSpace(str)
		if str == "" {
			*f = 0
			return nil
		}
		val, err := strconv.ParseInt(str, 10, 64)
		if err != nil {
			return err
		}
		*f = FlexibleInt64(val)
		return nil
	}
	val, err := strconv.ParseInt(s, 10, 64)
	if err != nil {
		return err
	}
	*f = FlexibleInt64(val)
	return nil
}

func (f FlexibleInt64) Int64() int64 {
	return int64(f)
}

func DecodeBody(r *http.Request, v any) {
	if r.Body != nil {
		_ = json.NewDecoder(r.Body).Decode(v)
	}
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
