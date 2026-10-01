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

	if req.NomorKartu == "" {
		model.WriteError(w, 201, "Nomor Kartu tidak boleh kosong")
		return
	}
	if len(req.NomorKartu) != 13 {
		model.WriteError(w, 201, "Nomor Kartu harus 13 digit")
		return
	}
	if req.NIK == "" {
		model.WriteError(w, 201, "NIK tidak boleh kosong ")
		return
	}
	if len(req.NIK) != 16 {
		model.WriteError(w, 201, "NIK harus 16 digit ")
		return
	}
	if req.NomorKK == "" {
		model.WriteError(w, 201, "Nomor KK tidak boleh kosong ")
		return
	}
	if len(req.NomorKK) != 16 {
		model.WriteError(w, 201, "Nomor KK harus 16 digit ")
		return
	}
	if req.Nama == "" {
		model.WriteError(w, 201, "Nama tidak boleh kosong")
		return
	}
	if req.JenisKelamin == "" {
		model.WriteError(w, 201, "Jenis Kelamin tidak boleh kosong")
		return
	}
	if req.TanggalLahir == "" {
		model.WriteError(w, 201, "Tanggal Lahir tidak boleh kosong")
		return
	}
	if req.NoHP == "" {
		model.WriteError(w, 201, "No.HP tidak boleh kosong")
		return
	}
	if req.Alamat == "" {
		model.WriteError(w, 201, "Alamat tidak boleh kosong")
		return
	}
	if req.KodeProp == "" {
		model.WriteError(w, 201, "Kode Propinsi tidak boleh kosong")
		return
	}
	if req.NamaProp == "" {
		model.WriteError(w, 201, "Nama Propinsi tidak boleh kosong")
		return
	}
	if req.KodeDati2 == "" {
		model.WriteError(w, 201, "Kode Dati 2 tidak boleh kosong")
		return
	}
	if req.NamaDati2 == "" {
		model.WriteError(w, 201, "Nama Dati 2 tidak boleh kosong")
		return
	}
	if req.KodeKec == "" {
		model.WriteError(w, 201, "Kode Kecamatan tidak boleh kosong")
		return
	}
	if req.NamaKec == "" {
		model.WriteError(w, 201, "Nama Kecamatan tidak boleh kosong")
		return
	}
	if req.KodeKel == "" {
		model.WriteError(w, 201, "Kode Kelurahan tidak boleh kosong")
		return
	}
	if req.NamaKel == "" {
		model.WriteError(w, 201, "Nama Kelurahan tidak boleh kosong")
		return
	}
	if req.RW == "" {
		model.WriteError(w, 201, "RW tidak boleh kosong")
		return
	}
	if req.RT == "" {
		model.WriteError(w, 201, "RT tidak boleh kosong")
		return
	}

	norm, code, msg, err := h.pasienSvc.RegisterPatient(&req)
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}

	model.WriteResponse(w, 200, msg, map[string]string{"norm": norm})
}
