package handler

import (
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
		TanggalAwal  model.FlexibleString `json:"tanggalawal"`
		TanggalAkhir model.FlexibleString `json:"tanggalakhir"`
	}
	model.DecodeBody(r, &body)

	if body.TanggalAwal == "" {
		model.WriteError(w, 201, "Tanggal Awal tidak boleh kosong")
		return
	}
	today := time.Now().Format("2006-01-02")
	tglAwal := body.TanggalAwal.String()
	tglAkhir := body.TanggalAkhir.String()

	if tglAwal < today {
		model.WriteError(w, 201, "Tanggal Awal tidak berlaku mundur")
		return
	}
	if body.TanggalAkhir == "" {
		model.WriteError(w, 201, "Tanggal Akhir tidak boleh kosong")
		return
	}
	if tglAkhir < today {
		model.WriteError(w, 201, "Tanggal Akhir tidak berlaku mundur")
		return
	}
	if tglAwal > tglAkhir {
		model.WriteError(w, 201, "Format tanggal awal harus lebih kecil dari tanggal akhir")
		return
	}

	list, err := h.repo.GetJadwalOperasiRS(tglAwal, tglAkhir)
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
		NoPeserta model.FlexibleString `json:"nopeserta"`
	}
	model.DecodeBody(r, &body)

	if body.NoPeserta == "" {
		model.WriteError(w, 201, "Nomor Peserta tidak boleh kosong")
		return
	}
	noPeserta := body.NoPeserta.String()
	if len(noPeserta) != 13 {
		model.WriteError(w, 201, "Nomor Peserta harus 13 digit")
		return
	}

	list, err := h.repo.GetJadwalOperasiPasien(noPeserta)
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
