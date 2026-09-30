package handler

import (
	"encoding/json"
	"net/http"

	"api-bpjsfktl-go/internal/cache"
	"api-bpjsfktl-go/internal/model"
	"api-bpjsfktl-go/internal/service"
	"api-bpjsfktl-go/internal/store"
)

type AntreanHandler struct {
	cache      *cache.MemoryCache
	repo       *store.Repository
	bookingSvc *service.BookingService
	opSvc      *service.OperationsService
}

func NewAntreanHandler(c *cache.MemoryCache, repo *store.Repository, bookingSvc *service.BookingService, opSvc *service.OperationsService) *AntreanHandler {
	return &AntreanHandler{
		cache:      c,
		repo:       repo,
		bookingSvc: bookingSvc,
		opSvc:      opSvc,
	}
}

type StatusAntreanReq struct {
	KodePoli       string `json:"kodepoli"`
	KodeDokter     string `json:"kodedokter"`
	TanggalPeriksa string `json:"tanggalperiksa"`
	JamPraktek     string `json:"jampraktek"`
}

func (h *AntreanHandler) HandleStatusAntrean(w http.ResponseWriter, r *http.Request) {
	var req StatusAntreanReq
	if err := json.NewDecoder(r.Body).Decode(&req); err != nil {
		model.WriteError(w, 201, "Format JSON tidak valid")
		return
	}

	if req.KodePoli == "" {
		model.WriteError(w, 201, "Kode Poli tidak boleh kosong")
		return
	}
	if req.KodeDokter == "" {
		model.WriteError(w, 201, "Kode Dokter tidak boleh kosong")
		return
	}
	if req.TanggalPeriksa == "" {
		model.WriteError(w, 201, "Tanggal tidak boleh kosong")
		return
	}
	if req.JamPraktek == "" {
		model.WriteError(w, 201, "Jam Praktek tidak boleh kosong")
		return
	}
	if len(req.JamPraktek) < 11 || req.JamPraktek[5] != '-' {
		model.WriteError(w, 201, "Jam Praktek tidak sesuai")
		return
	}

	rsClinic, ok := h.cache.GetClinicMapping(req.KodePoli)
	if !ok {
		model.WriteError(w, 201, "Poli tidak ditemukan")
		return
	}
	rsDoctor, ok := h.cache.GetDoctorMapping(req.KodeDokter)
	if !ok {
		model.WriteError(w, 201, "Dokter tidak ditemukan")
		return
	}

	status, err := h.repo.GetStatusAntrean(rsClinic, rsDoctor, req.TanggalPeriksa)
	if err != nil {
		model.WriteError(w, 401, "Gagal mengambil status antrean")
		return
	}

	startTime := req.JamPraktek[:5]
	endTime := req.JamPraktek[6:11]
	sched, found := h.cache.GetSchedule(getHari(req.TanggalPeriksa), rsDoctor, rsClinic, startTime, endTime)
	quota := 0
	if found {
		quota = sched.Quota
	}

	sisaKuota := quota - status.TotalAntrean
	if sisaKuota < 0 {
		sisaKuota = 0
	}

	resp := map[string]any{
		"namapoli":        status.NamaPoli,
		"namadokter":      status.NamaDokter,
		"totalantrean":    status.TotalAntrean,
		"sisaantrean":     status.SisaAntrean,
		"antreanpanggil":  status.AntreanPanggil,
		"sisakuotajkn":    sisaKuota,
		"kuotajkn":        quota,
		"sisakuotanonjkn": sisaKuota,
		"kuotanonjkn":     quota,
		"keterangan":      "Datanglah Minimal 30 Menit, jika no antrian anda terlewat, silakan konfirmasi ke bagian Pendaftaran atau Perawat Poli, Terima Kasih ..",
	}

	model.WriteOK(w, resp)
}

func (h *AntreanHandler) HandleAmbilAntrean(w http.ResponseWriter, r *http.Request) {
	var req service.BookingParams
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
	if req.NoHP == "" {
		model.WriteError(w, 201, "No.HP tidak boleh kosong")
		return
	}
	if req.KodePoli == "" {
		model.WriteError(w, 201, "Kode Poli tidak boleh kosong")
		return
	}
	if req.KodeDokter == "" {
		model.WriteError(w, 201, "Kode Dokter tidak boleh kosong")
		return
	}
	if req.TanggalPeriksa == "" {
		model.WriteError(w, 201, "Tanggal tidak boleh kosong")
		return
	}
	if req.JamPraktek == "" {
		model.WriteError(w, 201, "Jam Praktek tidak boleh kosong")
		return
	}
	if len(req.JamPraktek) < 11 || req.JamPraktek[5] != '-' {
		model.WriteError(w, 201, "Jam Praktek tidak sesuai")
		return
	}
	if req.JenisKunjungan == "" {
		model.WriteError(w, 201, "Jenis Kunjungan tidak boleh kosong")
		return
	}
	if req.NomorReferensi == "" {
		model.WriteError(w, 201, "Nomor Referensi tidak boleh kosong")
		return
	}

	res, code, msg, err := h.bookingSvc.BookQueue(&req)
	if err != nil {
		model.WriteError(w, code, msg)
		return
	}
	if code != 200 {
		model.WriteError(w, code, msg)
		return
	}

	model.WriteOK(w, res)
}

func (h *AntreanHandler) HandleCheckinAntrean(w http.ResponseWriter, r *http.Request) {
	var body struct {
		KodeBooking string `json:"kodebooking"`
		Waktu       int64  `json:"waktu"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		model.WriteError(w, 201, "Format JSON tidak valid")
		return
	}
	if body.KodeBooking == "" {
		model.WriteError(w, 201, "Kode Booking tidak boleh kosong")
		return
	}
	if body.Waktu == 0 {
		model.WriteError(w, 201, "Waktu tidak boleh kosong")
		return
	}

	code, msg, err := h.opSvc.CheckinQueue(body.KodeBooking, body.Waktu)
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}
	model.WriteResponse(w, 200, "Ok", nil)
}

func (h *AntreanHandler) HandleBatalAntrean(w http.ResponseWriter, r *http.Request) {
	var body struct {
		KodeBooking string `json:"kodebooking"`
		Keterangan  string `json:"keterangan"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		model.WriteError(w, 201, "Format JSON tidak valid")
		return
	}
	if body.KodeBooking == "" {
		model.WriteError(w, 201, "Kode Booking tidak boleh kosong")
		return
	}
	if body.Keterangan == "" {
		model.WriteError(w, 201, "Keterangan tidak boleh kosong")
		return
	}

	code, msg, err := h.opSvc.CancelQueue(body.KodeBooking, body.Keterangan)
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}
	model.WriteResponse(w, 200, "Ok", nil)
}

func (h *AntreanHandler) HandleSisaAntrean(w http.ResponseWriter, r *http.Request) {
	var body struct {
		KodeBooking string `json:"kodebooking"`
	}
	if err := json.NewDecoder(r.Body).Decode(&body); err != nil {
		model.WriteError(w, 201, "Format JSON tidak valid")
		return
	}
	if body.KodeBooking == "" {
		model.WriteError(w, 201, "Kode Booking tidak boleh kosong")
		return
	}

	res, code, msg, err := h.opSvc.GetSisaAntrean(body.KodeBooking)
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}
	model.WriteOK(w, res)
}

func getHari(dateStr string) string {
	return service.GetHariIndo(dateStr)
}
