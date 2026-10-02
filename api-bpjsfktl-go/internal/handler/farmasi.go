package handler

import (
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
		KodeBooking model.FlexibleString `json:"kodebooking"`
	}
	model.DecodeBody(r, &body)
	if body.KodeBooking == "" {
		model.WriteError(w, 201, "Kode Booking tidak boleh kosong")
		return
	}

	res, code, msg, err := h.opSvc.AmbilAntreanFarmasi(body.KodeBooking.String())
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}
	model.WriteOK(w, res)
}

func (h *FarmasiHandler) HandleStatusAntreanFarmasi(w http.ResponseWriter, r *http.Request) {
	var body struct {
		KodeBooking model.FlexibleString `json:"kodebooking"`
	}
	model.DecodeBody(r, &body)
	if body.KodeBooking == "" {
		model.WriteError(w, 201, "Kode Booking tidak boleh kosong")
		return
	}

	res, code, msg, err := h.opSvc.GetStatusAntreanFarmasi(body.KodeBooking.String())
	if err != nil || code != 200 {
		model.WriteError(w, code, msg)
		return
	}
	model.WriteOK(w, res)
}
