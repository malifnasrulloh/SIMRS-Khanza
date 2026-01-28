package fungsi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import fungsi.logger.SystemLogger;
import java.io.IOException;
import java.nio.file.Path;
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

    private void validate(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException(name + " cannot be null or empty"));
            throw new IllegalArgumentException(name + " cannot be null or empty");
        }
    }

    public SatuSehatHelper(String clientID, String clientSecret, String orgID, String authURL, String baseURL) throws IOException {
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

//        Patient patient = (Patient) parser.parseResource(Bundle.class, response.getBody()).getEntryFirstRep().getResource();
        Patient patient = parser.parseResource(Patient.class, "{\n"
                + "\"resourceType\": \"Patient\",\n"
                + "\"id\": \"100000030009\",\n"
                + "\"meta\": {\n"
                + "\"versionId\": \"MTY2MDk5NzY0NTUyNjE4MzAwMA\",\n"
                + "\"lastUpdated\": \"2022-08-20T12:14:05.526183+00:00\",\n"
                + "\"profile\": [\n"
                + "\"https://fhir.kemkes.go.id/r4/StructureDefinition/Patient|4.0.1\",\n"
                + "\"https://fhir.kemkes.go.id/r4/StructureDefinition/Patient\"\n"
                + "]\n"
                + "},\n"
                + "\"extension\": [\n"
                + "{\n"
                + "\"url\": \"https://fhir.kemkes.go.id/r4/StructureDefinition/birthPlace\",\n"
                + "\"valueAddress\": {\n"
                + "\"city\": \"Jakarta\",\n"
                + "\"country\": \"ID\"\n"
                + "}\n"
                + "}\n"
                + "],\n"
                + "\"identifier\": [\n"
                + "{\n"
                + "\"use\": \"official\",\n"
                + "\"system\": \"https://fhir.kemkes.go.id/id/ihs-number\",\n"
                + "\"value\": \"100000030009\"\n"
                + "},\n"
                + "{\n"
                + "\"use\": \"official\",\n"
                + "\"system\": \"https://fhir.kemkes.go.id/id/nik\",\n"
                + "\"value\": \"3171022809990001\"\n"
                + "}\n"
                + "],\n"
                + "\"active\": true,\n"
                + "\"name\": [\n"
                + "{\n"
                + "\"use\": \"official\",\n"
                + "\"text\": \"Budi Santoso\",\n"
                + "\"family\": \"Santoso\",\n"
                + "\"given\": [\n"
                + "\"Budi\"\n"
                + "],\n"
                + "\"suffix\": [\n"
                + "\"MSc\"\n"
                + "]\n"
                + "}\n"
                + "],\n"
                + "\"telecom\": [\n"
                + "{\n"
                + "\"system\": \"phone\",\n"
                + "\"value\": \"08123456789\",\n"
                + "\"use\": \"mobile\"\n"
                + "},\n"
                + "{\n"
                + "\"system\": \"email\",\n"
                + "\"value\": \"budi.santoso@xyz.com\",\n"
                + "\"use\": \"home\"\n"
                + "}\n"
                + "],\n"
                + "\"gender\": \"male\",\n"
                + "\"birthDate\": \"1944-11-17\",\n"
                + "\"_birthDate\": {\n"
                + "\"extension\": [\n"
                + "{\n"
                + "\"url\": \"https://fhir.kemkes.go.id/r4/StructureDefinition/patient-birthTime\",\n"
                + "\"valueDateTime\": \"1944-11-17T15:39:00+07:00\"\n"
                + "}\n"
                + "]\n"
                + "},\n"
                + "\"deceasedBoolean\": false,\n"
                + "\"address\": [\n"
                + "{\n"
                + "\"extension\": [\n"
                + "{\n"
                + "\"extension\": [\n"
                + "{\n"
                + "\"url\": \"province\",\n"
                + "\"valueCode\": \"35\"\n"
                + "},\n"
                + "{\n"
                + "\"url\": \"city\",\n"
                + "\"valueCode\": \"3517\"\n"
                + "},\n"
                + "{\n"
                + "\"url\": \"district\",\n"
                + "\"valueCode\": \"351708\"\n"
                + "},\n"
                + "{\n"
                + "\"url\": \"village\",\n"
                + "\"valueCode\": \"3517082015\"\n"
                + "},\n"
                + "{\n"
                + "\"url\": \"rt\",\n"
                + "\"valueCode\": \"1\"\n"
                + "},\n"
                + "{\n"
                + "\"url\": \"rw\",\n"
                + "\"valueCode\": \"2\"\n"
                + "}\n"
                + "],\n"
                + "\"url\": \"https://fhir.kemkes.go.id/r4/StructureDefinition/AdministrativeCode\"\n"
                + "}\n"
                + "],\n"
                + "\"use\": \"home\",\n"
                + "\"line\": [\n"
                + "\"Gd. Prof. Dr. Sujudi Lt.5, Jl. H.R. Rasuna Said Blok X5 Kav. 4-9 Kuningan\"\n"
                + "],\n"
                + "\"city\": \"Jakarta\",\n"
                + "\"postalCode\": \"12950\",\n"
                + "\"country\": \"ID\"\n"
                + "}\n"
                + "],\n"
                + "\"maritalStatus\": {\n"
                + "\"coding\": [\n"
                + "{\n"
                + "\"system\": \"http://terminology.hl7.org/CodeSystem/v3-MaritalStatus\",\n"
                + "\"code\": \"M\",\n"
                + "\"display\": \"Married\"\n"
                + "}\n"
                + "],\n"
                + "\"text\": \"Married\"\n"
                + "},\n"
                + "\"multipleBirthBoolean\": false,\n"
                + "\"contact\": [\n"
                + "{\n"
                + "\"relationship\": [\n"
                + "{\n"
                + "\"coding\": [\n"
                + "{\n"
                + "\"system\": \"http://terminology.hl7.org/CodeSystem/v2-0131\",\n"
                + "\"code\": \"C\"\n"
                + "}\n"
                + "]\n"
                + "}\n"
                + "],\n"
                + "\"name\": {\n"
                + "\"use\": \"official\",\n"
                + "\"family\": \"Smith\",\n"
                + "\"given\": [\n"
                + "\"Rebecca\"\n"
                + "]\n"
                + "},\n"
                + "\"telecom\": [\n"
                + "{\n"
                + "\"system\": \"phone\",\n"
                + "\"value\": \"0690383372\",\n"
                + "\"use\": \"mobile\"\n"
                + "}\n"
                + "]\n"
                + "}\n"
                + "],\n"
                + "\"communication\": [\n"
                + "{\n"
                + "\"language\": {\n"
                + "\"coding\": [\n"
                + "{\n"
                + "\"system\": \"urn:ietf:bcp:47\",\n"
                + "\"code\": \"id\",\n"
                + "\"display\": \"Indonesian\"\n"
                + "}\n"
                + "],\n"
                + "\"text\": \"Indonesian\"\n"
                + "},\n"
                + "\"preferred\": true\n"
                + "}\n"
                + "]\n"
                + "}");

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

    public static void main(String[] args) throws IOException {
        SystemLogger.configure(Path.of("."), "testt");
        koneksiDB.condb();
        SatuSehatHelper a = new SatuSehatHelper(koneksiDB.CLIENTIDSATUSEHAT(), koneksiDB.SECRETKEYSATUSEHAT(), koneksiDB.IDSATUSEHAT(), koneksiDB.URLAUTHSATUSEHAT(), koneksiDB.URLFHIRSATUSEHAT());
        System.out.println(a.getPatientIDByNIK("9104224509000003"));
        System.out.println("-----------");
//        System.out.println(a.getPractitionerIDByNIK("3313096403900009"));
        System.out.println(a.getPatientData(a.getPatientIDByNIK("9104224509000003")).toString());
    }

}
