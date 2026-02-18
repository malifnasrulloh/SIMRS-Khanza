/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils.bpjs;

import khanzautils.JsonUtil;

/**
 *
 * @author malifnasrulloh
 */
public class AntreanHelper {

    public static String buildAddPharmacyQueueJson(String bookingCode, String recipeType, Integer queueNumber, String description) {
        return JsonUtil.createObject()
                .put("kodebooking", bookingCode)
                .put("jenisresep", recipeType)
                .put("nomorantrean", queueNumber)
                .put("keterangan", description)
                .build();
    }

    public static String buildAddQueueMobileJKNJson(String bookingCode, String patientType, String cardNumber, String nationalId, String phoneNumber, String poliCode, String poliName, String isNewPatient, String medicalRecordNumber, String examinationDate, String doctorCode, String doctorName, String practiceHours, int visitType, String referenceNumber, String queueNumber, int queueIndex, long estimatedServiceTime, int remainingJknQuota, String totalJknQuota, int remainingNonJknQuota, String totalNonJknQuota, String note) {
        return JsonUtil.createObject()
                .put("kodebooking", bookingCode)
                .put("jenispasien", patientType)
                .put("nomorkartu", cardNumber)
                .put("nik", nationalId)
                .put("nohp", phoneNumber)
                .put("kodepoli", poliCode)
                .put("namapoli", poliName)
                .put("pasienbaru", isNewPatient)
                .put("norm", medicalRecordNumber)
                .put("tanggalperiksa", examinationDate)
                .put("kodedokter", doctorCode)
                .put("namadokter", doctorName)
                .put("jampraktek", practiceHours)
                .put("jeniskunjungan", visitType)
                .put("nomorreferensi", referenceNumber)
                .put("nomorantrean", queueNumber)
                .put("angkaantrean", queueIndex)
                .put("estimasidilayani", estimatedServiceTime)
                .put("sisakuotajkn", remainingJknQuota)
                .put("kuotajkn", totalJknQuota)
                .put("sisakuotanonjkn", remainingNonJknQuota)
                .put("kuotanonjkn", totalNonJknQuota)
                .put("keterangan", note)
                .build();
    }

    public static String buildTimeUpdateJson(String bookingCode, String taskId, long timestamp) {
        return JsonUtil.createObject()
                .put("kodebooking", bookingCode)
                .put("taskid", taskId)
                .put("waktu", timestamp)
                .build();
    }

    public static String buildCancelQueueJson(String bookingCode, String description) {
        return JsonUtil.createObject()
                .put("kodebooking", bookingCode)
                .put("keterangan", description)
                .build();
    }

}
