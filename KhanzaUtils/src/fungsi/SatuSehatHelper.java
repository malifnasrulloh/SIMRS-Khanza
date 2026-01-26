package fungsi;

import fungsi.logger.SystemLogger;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
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

    public SatuSehatHelper(String clientID, String clientSecret, String orgID, String authURL, String baseURL) {
        if (clientID == null || clientID.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException("ClientID cannot be null or empty"));
            throw new IllegalArgumentException("ClientID cannot be null or empty");
        }
        if (clientSecret == null || clientSecret.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException("ClientSecret cannot be null or empty"));
            throw new IllegalArgumentException("ClientSecret cannot be null or empty");
        }
        if (orgID == null || orgID.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException("OrganizationID cannot be null or empty"));
            throw new IllegalArgumentException("OrganizationID cannot be null or empty");
        }
        if (authURL == null || authURL.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException("Auth-URL cannot be null or empty"));
            throw new IllegalArgumentException("Auth-URL cannot be null or empty");
        }
        if (baseURL == null || baseURL.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException("Base-URL cannot be null or empty"));
            throw new IllegalArgumentException("Base-URL cannot be null or empty");
        }

        this.clientSecret = clientSecret;
        this.clientID = clientID;
        this.orgID = orgID;
        this.authURL = authURL;
        this.baseURL = baseURL;
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
            throw new IllegalStateException("NIK should be only numeric");
        }
        HttpRequestUtil http = HttpRequestUtil.getInstance();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAuthToken());

        String URL = UriComponentsBuilder.fromUriString(this.baseURL + "/Patient").queryParam("identifier", "https://fhir.kemkes.go.id/id/nik|" + NIK).build().toUriString();

        ResponseEntity<String> response = http.exchange(URL, HttpMethod.GET, null, String.class, headers);

        if (response.getStatusCode().is2xxSuccessful()) {
            String PatientID = "";
            mapper = new ObjectMapper().readTree(response.getBody());
            for (JsonNode node : mapper.findValues("identifier")) {
                for (JsonNode identifier : node.values()) {
                    if ("https://fhir.kemkes.go.id/id/ihs-number".equals(identifier.path("system").asString())) {
                        PatientID = identifier.path("value").asString();
                    }
                }
            }
            return PatientID;
        }
        throw new IllegalStateException("Failed to get PatientID");
    }

    public String getPractitionerIDByNIK(String NIK) {
        if (!NIK.matches("\\d+")) {
            throw new IllegalStateException("NIK should be only numeric");
        }
        HttpRequestUtil http = HttpRequestUtil.getInstance();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(getAuthToken());

        String URL = UriComponentsBuilder.fromUriString(this.baseURL + "/Practitioner").queryParam("identifier", "https://fhir.kemkes.go.id/id/nik|" + NIK).build().toUriString();

        ResponseEntity<String> response = http.exchange(URL, HttpMethod.GET, null, String.class, headers);

        if (response.getStatusCode().is2xxSuccessful()) {
            String PractitionerID = "";
            mapper = new ObjectMapper().readTree(response.getBody());
            System.out.println(mapper.toPrettyString());
            for (JsonNode node : mapper.findValues("identifier")) {
                for (JsonNode identifier : node.values()) {
                    if ("https://fhir.kemkes.go.id/id/nakes-his-number".equals(identifier.path("system").asString())) {
                        PractitionerID = identifier.path("value").asString();
                    }
                }
            }
            return PractitionerID;
        }
        throw new IllegalStateException("Failed to get PractitionerID");
    }

    public static void main(String[] args) {
        SystemLogger.configure(Path.of("."), "testt");
        koneksiDB.condb();
        SatuSehatHelper a = new SatuSehatHelper(koneksiDB.CLIENTIDSATUSEHAT(), koneksiDB.SECRETKEYSATUSEHAT(), koneksiDB.IDSATUSEHAT(), koneksiDB.URLAUTHSATUSEHAT(), koneksiDB.URLFHIRSATUSEHAT());
//        System.out.println(a.getPatientIDByNIK("9104224509000003"));
        System.out.println(a.getPractitionerIDByNIK("3313096403900009"));
    }
}
