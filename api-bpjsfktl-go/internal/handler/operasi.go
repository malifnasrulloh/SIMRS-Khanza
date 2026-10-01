package handler

import (
	"encoding/json"
	"net/http"
	"time"

	"api-bpjsfktl-go/internal/model"
	"api-bpjsfktl-go/internal/store"
)

type OperasiHandler struct {
	repo *store.Repository
}

func NewOperasiHandler(repo *store.Repository) *OperasiHandler {
	return &OperasiHandler{repo: repo}
}

func (h *OperasiHandler) HandleJadwalOperasiRS(w http.ResponseWriter, r *http.Request) {
	var body struct {
		TanggalAwal  string `json:"tanggalawal"`
		TanggalAkhir string `json:"tanggalakhir"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		model.WriteError(w, 201, "Format JSON tidak valid")
		return
	}
	if body.TanggalAwal == "" {
		model.WriteError(w, 201, "Tanggal Awal tidak boleh kosong")
		return
	}
	today := time.Now().Format("2006-01-02")
	if body.TanggalAwal < today {
		model.WriteError(w, 201, "Tanggal Awal tidak berlaku mundur")
		return
	}
	if body.TanggalAkhir == "" {
		model.WriteError(w, 201, "Tanggal Akhir tidak boleh kosong")
		return
	}
	if body.TanggalAkhir < today {
		model.WriteError(w, 201, "Tanggal Akhir tidak berlaku mundur")
		return
	}
	if body.TanggalAwal > body.TanggalAkhir {
		model.WriteError(w, 201, "Format tanggal awal harus lebih kecil dari tanggal akhir")
		return
	}

	list, err := h.repo.GetJadwalOperasiRS(body.TanggalAwal, body.TanggalAkhir)
	if err != nil {
		model.WriteError(w, 401, "Gagal mengambil jadwal operasi")
		return
	}
	if len(list) == 0 {
		model.WriteError(w, 201, "Maaf tidak ada Jadwal Operasi pada tanggal tersebut")
		return
	}

	model.WriteOK(w, map[string]any{"list": list})
}

func (h *OperasiHandler) HandleJadwalOperasiPasien(w http.ResponseWriter, r *http.Request) {
	var body struct {
		NoPeserta string `json:"nopeserta"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		model.WriteError(w, 201, "Format JSON tidak valid")
		return
	}
	if body.NoPeserta == "" {
		model.WriteError(w, 201, "Nomor Peserta tidak boleh kosong")
		return
	}
	if len(body.NoPeserta) != 13 {
		model.WriteError(w, 201, "Nomor Peserta harus 13 digit")
		return
	}

	list, err := h.repo.GetJadwalOperasiPasien(body.NoPeserta)
	if err != nil {
		model.WriteError(w, 401, "Gagal mengambil jadwal operasi pasien")
		return
	}
	if len(list) == 0 {
		model.WriteError(w, 201, "Maaf anda tidak memiliki jadwal operasi")
		return
	}

	model.WriteOK(w, map[string]any{"list": list})
}
