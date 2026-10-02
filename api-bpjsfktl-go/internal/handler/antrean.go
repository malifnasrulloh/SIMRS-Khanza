package handler

import (
	"net/http"
	"time"

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
	KodePoli       model.FlexibleString `json:"kodepoli"`
	KodeDokter     model.FlexibleString `json:"kodedokter"`
	TanggalPeriksa model.FlexibleString `json:"tanggalperiksa"`
	JamPraktek     model.FlexibleString `json:"jampraktek"`
}

func (h *AntreanHandler) HandleStatusAntrean(w http.ResponseWriter, r *http.Request) {
	var req StatusAntreanReq
	model.DecodeBody(r, &req)

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
	today := time.Now().Format("2006-01-02")
	if req.TanggalPeriksa.String() < today {
		model.WriteError(w, 201, "Tanggal Periksa tidak berlaku")
		return
	}
	if req.JamPraktek == "" {
		model.WriteError(w, 201, "Jam Praktek tidak boleh kosong")
		return
	}
	jamPraktek := req.JamPraktek.String()
	if len(jamPraktek) < 11 || jamPraktek[5] != '-' {
		model.WriteError(w, 201, "Jam Praktek tidak sesuai")
		return
	}

	rsClinic, ok := h.cache.GetClinicMapping(req.KodePoli.String())
	if !ok {
		model.WriteError(w, 201, "Poli tidak ditemukan")
		return
	}
	rsDoctor, ok := h.cache.GetDoctorMapping(req.KodeDokter.String())
	if !ok {
		model.WriteError(w, 201, "Dokter tidak ditemukan")
		return
	}

	startTime := jamPraktek[:5]
	endTime := jamPraktek[6:11]
	sched, found := h.cache.GetSchedule(getHari(req.TanggalPeriksa.String()), rsDoctor, rsClinic, startTime, endTime)
	if !found || sched.Quota <= 0 {
		model.WriteError(w, 201, "Pendaftaran ke Poli ini tidak tersedia")
		return
	}

	status, err := h.repo.GetStatusAntrean(rsClinic, rsDoctor, req.TanggalPeriksa.String())
	if err != nil {
		model.WriteError(w, 401, "Gagal mengambil status antrean")
		return
	}

	quota := sched.Quota
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
	model.DecodeBody(r, &req)

	if req.NomorKartu == "" {
		model.WriteError(w, 201, "Nomor Kartu tidak boleh kosong")
		return
	}
	if len(req.NomorKartu) != 13 {
		model.WriteError(w, 201, "Nomor Kartu harus 13 digit")
		return
	}
	if req.NIK != "" {
		if len(req.NIK) != 16 {
			model.WriteError(w, 201, "NIK harus 16 digit ")
			return
		}
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
	today := time.Now().Format("2006-01-02")
	if req.TanggalPeriksa.String() < today {
		model.WriteError(w, 201, "Tanggal Periksa tidak berlaku mundur")
		return
	}
	if req.JamPraktek == "" {
		model.WriteError(w, 201, "Jam Praktek tidak boleh kosong")
		return
	}
	jamPraktek := req.JamPraktek.String()
	if len(jamPraktek) < 11 || jamPraktek[5] != '-' {
		model.WriteError(w, 201, "Jam Praktek tidak sesuai")
		return
	}
	if req.JenisKunjungan == "" {
		model.WriteError(w, 201, "Jenis Kunjungan tidak boleh kosong")
		return
	}
	jk := req.JenisKunjungan.String()
	if jk != "1" && jk != "2" && jk != "3" && jk != "4" {
		model.WriteError(w, 201, "Jenis Kunjungan tidak ditemukan")
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
		KodeBooking model.FlexibleString `json:"kodebooking"`
		Waktu       model.FlexibleInt64  `json:"waktu"`
	}
	model.DecodeBody(r, &body)
	if body.KodeBooking == "" {
		model.WriteError(w, 201, "Kode Booking tidak boleh kosong")
		return
	}
	if body.Waktu == 0 {
		model.WriteError(w, 201, "Waktu tidak boleh kosong")
		return
	}

	code, msg, err := h.opSvc.CheckinQueue(body.KodeBooking.String(), body.Waktu.Int64())
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}
	model.WriteResponse(w, 200, "Ok", nil)
}

func (h *AntreanHandler) HandleBatalAntrean(w http.ResponseWriter, r *http.Request) {
	var body struct {
		KodeBooking model.FlexibleString `json:"kodebooking"`
		Keterangan  model.FlexibleString `json:"keterangan"`
	}
	model.DecodeBody(r, &body)
	if body.KodeBooking == "" {
		model.WriteError(w, 201, "Kode Booking tidak boleh kosong")
		return
	}
	if body.Keterangan == "" {
		model.WriteError(w, 201, "Keterangan tidak boleh kosong")
		return
	}

	code, msg, err := h.opSvc.CancelQueue(body.KodeBooking.String(), body.Keterangan.String())
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}
	model.WriteResponse(w, 200, "Ok", nil)
}

func (h *AntreanHandler) HandleSisaAntrean(w http.ResponseWriter, r *http.Request) {
	var body struct {
		KodeBooking model.FlexibleString `json:"kodebooking"`
	}
	model.DecodeBody(r, &body)
	if body.KodeBooking == "" {
		model.WriteError(w, 201, "Kode Booking tidak boleh kosong")
		return
	}

	res, code, msg, err := h.opSvc.GetSisaAntrean(body.KodeBooking.String())
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}
	model.WriteOK(w, res)
}

func getHari(dateStr string) string {
	return service.GetHariIndo(dateStr)
}
