package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.koneksiDB;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Headless API service for SATUSEHAT Rekam Medis Elektronik (SSRME) V2.0.
 */
public class SatuSehatRMEApi {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Pattern DUP_KEY_PATTERN = Pattern.compile("shlinkID:\\s*\\\\?\"?([a-fA-F0-9]+)\\\\?\"?");
    private final ApiSatuSehat apiSatuSehat;
    private final String baseUrl;

    public SatuSehatRMEApi() {
        this.apiSatuSehat = new ApiSatuSehat();
        this.baseUrl = resolveBaseUrl(koneksiDB.URLAUTHSATUSEHAT(), koneksiDB.URLFHIRSATUSEHAT(), koneksiDB.URLSATUSEHATRME());
    }

    public SatuSehatRMEApi(ApiSatuSehat apiSatuSehat, String baseUrl) {
        this.apiSatuSehat = apiSatuSehat;
        this.baseUrl = baseUrl;
    }

    public static String resolveBaseUrl(String authUrl, String fhirUrl, String explicitOverride) {
        if (explicitOverride != null && !explicitOverride.trim().isEmpty()) {
            return explicitOverride.trim().replaceAll("/+$", "");
        }
        boolean isStaging = (authUrl != null && (authUrl.contains("-stg") || authUrl.contains("dto.kemkes.go.id")))
                || (fhirUrl != null && (fhirUrl.contains("-stg") || fhirUrl.contains("dto.kemkes.go.id")));
        if (isStaging) {
            return "https://api-satusehat-stg.dto.kemkes.go.id/ssrme/v2/ntl";
        }
        return "https://api-satusehat.kemkes.go.id/ssrme/v2/ntl";
    }

    public static String getVerificationBaseUrl(String baseUrl) {
        boolean isStaging = baseUrl != null && (baseUrl.contains("-stg") || baseUrl.contains("dto.kemkes.go.id"));
        return isStaging ? "https://satusehat-stg.dto.kemkes.go.id/rekammedis/consent?launch="
                         : "https://satusehat.kemkes.go.id/rekammedis/consent?launch=";
    }

    public static String buildPayload(String patientId, String patientName, String practitionerId,
                                      String practitionerName, String orgId, String orgName, boolean isEmergency) {
        try {
            ObjectNode node = MAPPER.createObjectNode();
            node.put("patient_id", patientId != null ? patientId.trim() : "");
            node.put("patient_name", patientName != null ? patientName.trim() : "");
            node.put("practitioner_id", practitionerId != null ? practitionerId.trim() : "");
            node.put("practitioner_name", practitionerName != null ? practitionerName.trim() : "");
            node.put("organization_id", orgId != null ? orgId.trim() : "");
            node.put("organization_name", orgName != null ? orgName.trim() : "");
            if (isEmergency) {
                node.put("type_medical_summary", "EMERGENCY");
            }
            return MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }

    public static SatuSehatRMEResponse parseResponse(int statusCode, String jsonResponse) {
        SatuSehatRMEResponse res = new SatuSehatRMEResponse();
        res.setStatusCode(statusCode);

        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            res.setSuccess(statusCode >= 200 && statusCode < 300);
            res.setMessage("Respons kosong dari server SATUSEHAT");
            return res;
        }

        try {
            JsonNode root = MAPPER.readTree(jsonResponse);

            if (root.has("success")) {
                res.setSuccess(root.path("success").asBoolean(false));
            } else {
                res.setSuccess(statusCode >= 200 && statusCode < 300);
            }

            if (root.has("code")) {
                res.setStatusCode(root.path("code").asInt(statusCode));
            }
            if (root.has("message")) {
                res.setMessage(root.path("message").asText());
            }
            if (root.has("request_id")) {
                res.setRequestId(root.path("request_id").asText());
            }

            JsonNode dataNode = root.path("data");
            if (dataNode.isObject()) {
                if (dataNode.has("shlinkId")) {
                    res.setShlinkId(dataNode.path("shlinkId").asText());
                }
                if (dataNode.has("verificationUrl")) {
                    res.setVerificationUrl(dataNode.path("verificationUrl").asText());
                }
                if (dataNode.has("shlinkUrl")) {
                    res.setShlinkUrl(dataNode.path("shlinkUrl").asText());
                }
                if (dataNode.has("consentId")) {
                    res.setConsentId(dataNode.path("consentId").asText());
                }
                if (dataNode.has("expiredAt")) {
                    res.setExpiredAt(dataNode.path("expiredAt").asText());
                }
                if (dataNode.has("code")) {
                    res.setErrorCode(dataNode.path("code").asText());
                }
            } else if (dataNode.isTextual()) {
                String dataStr = dataNode.asText();
                if (res.getMessage().isEmpty()) {
                    res.setMessage(dataStr);
                }
                res.setErrorCode(dataStr);
            }

            // Check for fault structure from Apigee
            if (root.has("fault")) {
                JsonNode fault = root.path("fault");
                res.setSuccess(false);
                res.setMessage(fault.path("faultstring").asText());
                res.setErrorCode(fault.path("detail").path("errorcode").asText());
            }

            // Detect CONSENT_REQUIRED
            if (res.getStatusCode() == 403 || "consent required".equalsIgnoreCase(res.getMessage())
                    || "CONSENT_REQUIRED".equalsIgnoreCase(res.getErrorCode())) {
                res.setErrorCode("CONSENT_REQUIRED");
            }

            // Detect Duplicate Key Error from MongoDB (E11000 duplicate key error)
            String rawMsg = res.getMessage();
            if (rawMsg != null && (rawMsg.contains("duplicate key error") || rawMsg.contains("E11000") || rawMsg.contains("uq_rme_consent_launch_shlink_id"))) {
                res.setErrorCode("DUPLICATE_KEY_ERROR");
                Matcher m = DUP_KEY_PATTERN.matcher(rawMsg);
                if (m.find()) {
                    String extractedShlinkId = m.group(1);
                    res.setShlinkId(extractedShlinkId);
                    boolean isStg = (jsonResponse != null && jsonResponse.contains("-stg")) || (rawMsg != null && rawMsg.contains("-stg"));
                    String verifyBase = isStg ? "https://satusehat-stg.dto.kemkes.go.id/rekammedis/consent?launch="
                                              : "https://satusehat.kemkes.go.id/rekammedis/consent?launch=";
                    res.setVerificationUrl(verifyBase + extractedShlinkId);
                }
            }

        } catch (Exception e) {
            res.setSuccess(false);
            res.setMessage("Gagal membaca respons: " + e.getMessage());
            res.setErrorCode("PARSE_ERROR");
        }

        return res;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public SatuSehatRMEResponse createConsentHealthLink(String patientId, String patientName,
                                                        String practitionerId, String practitionerName,
                                                        String orgId, String orgName) {
        String payload = buildPayload(patientId, patientName, practitionerId, practitionerName, orgId, orgName, false);
        return executePost(baseUrl + "/chl", payload);
    }

    public SatuSehatRMEResponse createEmergencyConsentHealthLink(String patientId, String patientName,
                                                                 String practitionerId, String practitionerName,
                                                                 String orgId, String orgName) {
        String payload = buildPayload(patientId, patientName, practitionerId, practitionerName, orgId, orgName, true);
        return executePost(baseUrl + "/chl", payload);
    }

    public SatuSehatRMEResponse openSmartHealthLink(String patientId, String patientName,
                                                    String practitionerId, String practitionerName,
                                                    String orgId, String orgName) {
        String payload = buildPayload(patientId, patientName, practitionerId, practitionerName, orgId, orgName, false);
        return executePost(baseUrl + "/shl", payload);
    }

    public SatuSehatRMEResponse openEmergencySmartHealthLink(String patientId, String patientName,
                                                             String practitionerId, String practitionerName,
                                                             String orgId, String orgName) {
        String payload = buildPayload(patientId, patientName, practitionerId, practitionerName, orgId, orgName, true);
        return executePost(baseUrl + "/shl", payload);
    }

    public static String prettyPrintJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return "";
        }
        try {
            Object obj = MAPPER.readValue(json, Object.class);
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            return json;
        }
    }

    private SatuSehatRMEResponse executePost(String url, String jsonPayload) {
        System.out.println("================== [SATUSEHAT SSRME REQUEST] ==================");
        System.out.println("Endpoint : POST " + url);
        System.out.println("Payload  :\n" + prettyPrintJson(jsonPayload));
        System.out.println("---------------------------------------------------------------");

        try {
            String token = apiSatuSehat.TokenSatuSehat();
            if (token == null || token.trim().isEmpty()) {
                System.out.println("[SATUSEHAT SSRME RESPONSE] Error: Gagal mendapatkan token autentikasi (UNAUTHORIZED)");
                System.out.println("===============================================================");
                SatuSehatRMEResponse res = SatuSehatRMEResponse.error(401, "Gagal mendapatkan token autentikasi SATUSEHAT", "UNAUTHORIZED");
                res.setRawRequestBody(jsonPayload);
                return res;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);

            ResponseEntity<String> response = apiSatuSehat.getRest().exchange(url, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();

            System.out.println("[SATUSEHAT SSRME RESPONSE]");
            System.out.println("HTTP Code: " + response.getStatusCode().value());
            System.out.println("Response :\n" + prettyPrintJson(responseBody));
            System.out.println("===============================================================");

            SatuSehatRMEResponse res = parseResponse(response.getStatusCode().value(), responseBody);
            if (res.isDuplicateKeyError() && !res.getShlinkId().isEmpty()) {
                res.setVerificationUrl(getVerificationBaseUrl(baseUrl) + res.getShlinkId());
            }
            res.setRawRequestBody(jsonPayload);
            res.setRawResponseBody(responseBody);
            return res;
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();

            System.out.println("[SATUSEHAT SSRME RESPONSE (HTTP ERROR)]");
            System.out.println("HTTP Code: " + e.getStatusCode().value());
            System.out.println("Response :\n" + prettyPrintJson(responseBody));
            System.out.println("===============================================================");

            SatuSehatRMEResponse res = parseResponse(e.getStatusCode().value(), responseBody);
            if (res.isDuplicateKeyError() && !res.getShlinkId().isEmpty()) {
                res.setVerificationUrl(getVerificationBaseUrl(baseUrl) + res.getShlinkId());
            }
            res.setRawRequestBody(jsonPayload);
            res.setRawResponseBody(responseBody);
            return res;
        } catch (Exception e) {
            System.out.println("[SATUSEHAT SSRME RESPONSE (NETWORK ERROR)]");
            System.out.println("Error: " + e.getMessage());
            System.out.println("===============================================================");

            SatuSehatRMEResponse res = SatuSehatRMEResponse.error(500, "Kesalahan jaringan: " + e.getMessage(), "NETWORK_ERROR");
            res.setRawRequestBody(jsonPayload);
            res.setRawResponseBody(e.getMessage());
            return res;
        }
    }
}
