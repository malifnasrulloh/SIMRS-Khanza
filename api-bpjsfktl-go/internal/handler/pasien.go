package handler

import (
	"encoding/json"
	"net/http"

	"api-bpjsfktl-go/internal/model"
	"api-bpjsfktl-go/internal/service"
)

type PasienHandler struct {
	pasienSvc *service.PasienService
}

func NewPasienHandler(pasienSvc *service.PasienService) *PasienHandler {
	return &PasienHandler{pasienSvc: pasienSvc}
}

func (h *PasienHandler) HandlePasienBaru(w http.ResponseWriter, r *http.Request) {
	var req service.PasienBaruParams
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		model.WriteError(w, 201, "Format JSON tidak valid")
		return
	}

	if req.NomorKartu == "" || len(req.NomorKartu) != 13 {
		model.WriteError(w, 201, "Nomor Kartu harus 13 digit")
		return
	}
	if req.NIK == "" || len(req.NIK) != 16 {
		model.WriteError(w, 201, "NIK harus 16 digit ")
		return
	}
	if req.Nama == "" {
		model.WriteError(w, 201, "Nama tidak boleh kosong")
		return
	}

	norm, code, msg, err := h.pasienSvc.RegisterPatient(&req)
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}

	model.WriteResponse(w, 200, msg, map[string]string{"norm": norm})
}
