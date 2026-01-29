package fungsi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import fungsi.logger.SystemLogger;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Practitioner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author malifnasrulloh
 */
public class SatuSehatHelper {

    private final String clientID;
    private final String clientSecret;
    private final String orgID;
    private final String authURL;
    private final String baseURL;
    private JsonNode mapper;
    private final FhirContext ctx = FhirContext.forR4();
    private final IParser parser = ctx.newJsonParser();
    private final DataWilayah dataWilayah;

    public SatuSehatHelper(String clientID, String clientSecret, String orgID, String authURL, String baseURL) {
        validate(clientID, "ClientID");
        validate(clientSecret, "ClientSecret");
        validate(orgID, "OrganizationID");
        validate(authURL, "Auth-URL");
        validate(baseURL, "Base-URL");

        this.clientSecret = clientSecret;
        this.clientID = clientID;
        this.orgID = orgID;
        this.authURL = authURL;
        this.baseURL = baseURL;
        this.dataWilayah = new DataWilayah("./cache/propinsi.iyem", "./cache/kabupaten.iyem", "./cache/kecamatan.iyem", "./cache/kelurahan.iyem");
    }

    private void validate(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException(name + " cannot be null or empty"));
            throw new IllegalArgumentException(name + " cannot be null or empty");
        }
    }

    public String getAuthToken() {
        HttpRequestUtil http = HttpRequestUtil.getInstance();

        String requestBody = String.format("client_id=%s&client_secret=%s", this.clientID, this.clientSecret);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String URL = UriComponentsBuilder.fromUriString(this.authURL + "/accesstoken").queryParam("grant_type", "client_credentials").build().toUriString();

        ResponseEntity<String> response = http.exchange(URL, HttpMethod.POST, requestBody, String.class, headers);

        if (response.getStatusCode().is2xxSuccessful()) {
            mapper = new ObjectMapper().readTree(response.getBody());
            return mapper.path("access_token").asString();
        }

        throw new IllegalStateException("Failed to get Auth Token");
    }

    public String getPatientIDByNIK(String NIK) {
        if (!NIK.matches("\\d+")) {
            throw new IllegalArgumentException("NIK should be only numeric");
        }
        HttpRequestUtil http = HttpRequestUtil.getInstance();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAuthToken());

        String URL = UriComponentsBuilder.fromUriString(this.baseURL + "/Patient").queryParam("identifier", "https://fhir.kemkes.go.id/id/nik|" + NIK).build().toUriString();

        ResponseEntity<String> response = http.exchange(URL, HttpMethod.GET, null, String.class, headers);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to fetch Patient");
        }

        Bundle bundle = parser.parseResource(Bundle.class, response.getBody());
        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Patient.class::isInstance)
                .map(Patient.class::cast)
                .flatMap(p -> p.getIdentifier().stream())
                .filter(id -> "https://fhir.kemkes.go.id/id/ihs-number".equals(id.getSystem()))
                .map(Identifier::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Patient ID not found"));

    }

    public String getPractitionerIDByNIK(String NIK) {
        if (!NIK.matches("\\d+")) {
            throw new IllegalArgumentException("NIK should be only numeric");
        }
        HttpRequestUtil http = HttpRequestUtil.getInstance();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAuthToken());

        String URL = UriComponentsBuilder.fromUriString(this.baseURL + "/Practitioner").queryParam("identifier", "https://fhir.kemkes.go.id/id/nik|" + NIK).build().toUriString();

        ResponseEntity<String> response = http.exchange(URL, HttpMethod.GET, null, String.class, headers);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to fetch Practitioner");
        }

        Bundle bundle = parser.parseResource(Bundle.class, response.getBody());
        return bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .filter(Practitioner.class::isInstance)
                .map(Practitioner.class::cast)
                .flatMap(p -> p.getIdentifier().stream())
                .filter(id -> "https://fhir.kemkes.go.id/id/nakes-his-number".equals(id.getSystem()))
                .map(Identifier::getValue)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Practitioner ID not found"));
    }

    public PatientModel getPatientData(String ihs_number) {
        PatientModel patientModel = new PatientModel();

        HttpRequestUtil http = HttpRequestUtil.getInstance();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAuthToken());

        String URL = UriComponentsBuilder.fromUriString(this.baseURL + "/Patient/" + ihs_number).build().toUriString();

        ResponseEntity<String> response = http.exchange(URL, HttpMethod.GET, null, String.class, headers);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Failed to fetch Patient");
        }

        Patient patient = (Patient) parser.parseResource(Bundle.class, response.getBody()).getEntryFirstRep().getResource();

        patientModel.idPasien = patient.getIdElement().getIdPart();
        patientModel.nik = patient.getIdentifier().stream().filter(id -> "https://fhir.kemkes.go.id/id/nik".equals(id.getSystem())).map(Identifier::getValue).findFirst().orElseThrow(() -> new IllegalStateException("Patient ID not found"));
        patientModel.nama = patient.hasName() ? patient.getNameFirstRep().getText() : "";
        patientModel.gender = patient.getGender() == AdministrativeGender.MALE ? "Laki-laki" : "Perempuan";
        patientModel.birthDate = patient.getBirthDate() != null ? patient.getBirthDate().toString() : "";
        patientModel.maritalStatus = patient.getMaritalStatus() != null ? patient.getMaritalStatus().getText() : "";

        for (ContactPoint cp : patient.getTelecom()) {
            if (cp.getSystem() == ContactPoint.ContactPointSystem.PHONE) {
                patientModel.phone = cp.getValue();
            }
            if (cp.getSystem() == ContactPoint.ContactPointSystem.EMAIL) {
                patientModel.email = cp.getValue();
            }
        }

        if (patient.hasAddress()) {
            Address addr = patient.getAddressFirstRep();
            patientModel.alamat = addr.hasLine() ? addr.getLine().get(0).getValue() : "";
            patientModel.postalCode = addr.getPostalCode();

            addr.getExtension().forEach(ext
                    -> ext.getExtension().forEach(e -> {
                        switch (e.getUrl()) {
                            case "province" ->
                                patientModel.province = e.getValue().primitiveValue();
                            case "city" ->
                                patientModel.city = e.getValue().primitiveValue();
                            case "district" ->
                                patientModel.district = e.getValue().primitiveValue();
                            case "village" ->
                                patientModel.village = e.getValue().primitiveValue();
                            case "rt" ->
                                patientModel.rt = e.getValue().primitiveValue();
                            case "rw" ->
                                patientModel.rw = e.getValue().primitiveValue();
                        }
                    })
            );

            patientModel.provinceName = dataWilayah.mapProvinceName(patientModel.province);
            patientModel.cityName = dataWilayah.mapCityName(patientModel.city, patientModel.province);
            patientModel.districtName = dataWilayah.mapDistrictName(patientModel.district, patientModel.city);
            patientModel.villageName = dataWilayah.mapVillageName(patientModel.village, patientModel.district);
        }

        return patientModel;
    }
}
