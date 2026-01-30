/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fungsi.fhir;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Reference;
import tools.jackson.databind.node.ObjectNode;

/**
 *
 * @author malifnasrulloh
 */
public class EncounterService {

    public static Encounter buildEncounter(ObjectNode data) {
        Encounter encounter = new Encounter();

        encounter.setStatus(Encounter.EncounterStatus.fromCode(data.path("status").asString()));
        encounter.setClass_(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/v3-ActCode").setCode(data.path("code_class").asString()).setDisplay(data.path("display_class").asString()));
        encounter.setSubject(new Reference("Patient/" + data.path("id_pasien").asString()).setDisplay(data.path("nm_pasien").asString()));

        Encounter.EncounterParticipantComponent participant = new Encounter.EncounterParticipantComponent();
        participant.addType(new CodeableConcept().addCoding(new Coding().setSystem("http://terminology.hl7.org/CodeSystem/v3-ParticipationType").setCode("ATND").setDisplay("attender")));
        participant.setIndividual(new Reference("Practitioner/" + data.path("id_dokter").asString()).setDisplay(data.path("nama_dokter").asString()));
        encounter.addParticipant(participant);

        Period period = new Period();
        period.setStartElement(new DateTimeType(data.path("tgl_registrasi").asString() + "T" + data.path("jam_reg").asString() + "+07:00"));
        encounter.setPeriod(period);

        Encounter.EncounterLocationComponent location = new Encounter.EncounterLocationComponent();
        location.setLocation(new Reference("Location/" + data.path("id_lokasi").asString()).setDisplay(data.path("nm_poli").asString()));
        encounter.addLocation(location);

        Encounter.StatusHistoryComponent statusHistory = new Encounter.StatusHistoryComponent();
        statusHistory.setStatus(Encounter.EncounterStatus.ARRIVED);
        statusHistory.setPeriod(new Period().setStartElement(new DateTimeType(data.path("tgl_registrasi").asString() + "T" + data.path("jam_reg").asString() + "+07:00")).setEndElement(new DateTimeType(data.path("pulang").asString())));
        encounter.addStatusHistory(statusHistory);

        encounter.setServiceProvider(new Reference("Organization/" + data.path("service_provider_id").asString()));
        encounter.addIdentifier(new Identifier().setSystem("http://sys-ids.kemkes.go.id/encounter/" + data.path("service_provider_id").asString()).setValue(data.path("no_rawat").asString()));

        return encounter;
    }

    public static Encounter buildEncounterWithID(String encounterID, ObjectNode data) {
        Encounter encounter = buildEncounter(data);
        if (encounterID != null && !encounterID.isEmpty()) {
            encounter.setId(encounterID);
        }
        return encounter;

    }

}
