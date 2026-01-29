/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fungsi.fhir;

import fungsi.JsonUtil;
import java.util.Map;

/**
 *
 * @author malifnasrulloh
 */
public class EncounterService {
    
    public static String buildEncounterBody(Map<String, String> data) {
        String encounterId = data.get("id_encounter");
        String status = data.get("status");
        String codeClass = data.get("code_class");
        String displayClass = data.get("display_class");
        String idPasien = data.get("id_pasien");
        String nmPasien = data.get("nm_pasien");
        String idDokter = data.get("id_dokter");
        String namaDokter = data.get("nama_dokter");
        String tglRegistrasi = data.get("tgl_registrasi");
        String jamReg = data.get("jam_reg");
        String pulang = data.get("pulang");
        String idLokasi = data.get("id_lokasi");
        String nmPoli = data.get("nm_poli");
        String noRawat = data.get("no_rawat");
        String serviceProviderId = data.get("service_provider_id");

        JsonUtil.JsonObjectBuilder jsonBuilder = JsonUtil.createObject();

        if (encounterId != null) {
            jsonBuilder.put("id", encounterId);
        }

        JsonUtil.JsonObjectBuilder classNode = JsonUtil.createObject()
                .put("system", "http://terminology.hl7.org/CodeSystem/v3-ActCode")
                .put("code", codeClass)
                .put("display", displayClass);

        JsonUtil.JsonObjectBuilder subjectNode = JsonUtil.createObject()
                .put("reference", "Patient/" + idPasien)
                .put("display", nmPasien);

        JsonUtil.JsonObjectBuilder codingNode = JsonUtil.createObject()
                .put("system", "http://terminology.hl7.org/CodeSystem/v3-ParticipationType")
                .put("code", "ATND")
                .put("display", "attender");

        JsonUtil.JsonObjectBuilder typeNode = JsonUtil.createObject()
                .put("coding", JsonUtil.createArray().add(codingNode));

        JsonUtil.JsonObjectBuilder individualNode = JsonUtil.createObject()
                .put("reference", "Practitioner/" + idDokter)
                .put("display", namaDokter);

        JsonUtil.JsonObjectBuilder participantNode = JsonUtil.createObject()
                .put("type", JsonUtil.createArray().add(typeNode))
                .put("individual", individualNode);

        JsonUtil.JsonArrayBuilder participantArray = JsonUtil.createArray().add(participantNode);

        JsonUtil.JsonObjectBuilder periodNode = JsonUtil.createObject()
                .put("start", tglRegistrasi + "T" + jamReg + "+07:00");

        JsonUtil.JsonObjectBuilder locationNode = JsonUtil.createObject()
                .put("location", JsonUtil.createObject()
                        .put("reference", "Location/" + idLokasi)
                        .put("display", nmPoli));

        JsonUtil.JsonArrayBuilder locationArray = JsonUtil.createArray().add(locationNode);

        JsonUtil.JsonObjectBuilder statusHistoryNode = JsonUtil.createObject()
                .put("status", "arrived")
                .put("period", JsonUtil.createObject()
                        .put("start", tglRegistrasi + "T" + jamReg + "+07:00")
                        .put("end", pulang));

        JsonUtil.JsonArrayBuilder statusHistoryArray = JsonUtil.createArray().add(statusHistoryNode);

        JsonUtil.JsonObjectBuilder serviceProviderNode = JsonUtil.createObject()
                .put("reference", "Organization/" + serviceProviderId);

        JsonUtil.JsonObjectBuilder identifierNode = JsonUtil.createObject()
                .put("system", "http://sys-ids.kemkes.go.id/encounter/" + serviceProviderId)
                .put("value", noRawat);

        JsonUtil.JsonArrayBuilder identifierArray = JsonUtil.createArray().add(identifierNode);

        jsonBuilder
                .put("resourceType", "Encounter")
                .put("status", status)
                .put("class", classNode)
                .put("subject", subjectNode)
                .put("participant", participantArray)
                .put("period", periodNode)
                .put("location", locationArray)
                .put("statusHistory", statusHistoryArray)
                .put("serviceProvider", serviceProviderNode)
                .put("identifier", identifierArray);

        return jsonBuilder.build();
    }
}
