package handler

import (
	"encoding/json"
	"net/http"

	"api-bpjsfktl-go/internal/model"
	"api-bpjsfktl-go/internal/service"
)

type FarmasiHandler struct {
	opSvc *service.OperationsService
}

func NewFarmasiHandler(opSvc *service.OperationsService) *FarmasiHandler {
	return &FarmasiHandler{opSvc: opSvc}
}

func (h *FarmasiHandler) HandleAmbilAntreanFarmasi(w http.ResponseWriter, r *http.Request) {
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

	res, code, msg, err := h.opSvc.AmbilAntreanFarmasi(body.KodeBooking)
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}
	model.WriteOK(w, res)
}

func (h *FarmasiHandler) HandleStatusAntreanFarmasi(w http.ResponseWriter, r *http.Request) {
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

	res, code, msg, err := h.opSvc.GetStatusAntreanFarmasi(body.KodeBooking)
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}
	model.WriteOK(w, res)
}
