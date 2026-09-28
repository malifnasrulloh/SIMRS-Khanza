package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fungsi.koneksiDB;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Headless API service for Kemenkes SATUSEHAT KYC (Know Your Customer).
 * Implements native hybrid encryption (RSA-OAEP SHA1/MGF1 + AES-256-GCM) matching
 * the official Kemenkes KYC specifications.
 */
public class SatuSehatKYCApi {
    public static final String SERVER_PUBLIC_KEY_STAGING =
        "-----BEGIN PUBLIC KEY-----\n" +
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAwqoicEXIYWYV3PvLIdvB\n" +
        "qFkHn2IMhPGKTiB2XA56enpPb0UbI9oHoetRF41vfwMqfFsy5Yd5LABxMGyHJBbP\n" +
        "+3fk2/PIfv+7+9/dKK7h1CaRTeT4lzJBiUM81hkCFlZjVFyHUFtaNfvQeO2OYb7U\n" +
        "kK5JrdrB4sgf50gHikeDsyFUZD1o5JspdlfqDjANYAhfz3aam7kCjfYvjgneqkV8\n" +
        "pZDVqJpQA3MHAWBjGEJ+R8y03hs0aafWRfFG9AcyaA5Ct5waUOKHWWV9sv5DQXmb\n" +
        "EAoqcx0ZPzmHJDQYlihPW4FIvb93fMik+eW8eZF3A920DzuuFucpblWU9J9o5w+2\n" +
        "oQIDAQAB\n" +
        "-----END PUBLIC KEY-----";

    public static final String SERVER_PUBLIC_KEY_PRODUCTION =
        "-----BEGIN PUBLIC KEY-----\n" +
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxLwvebfOrPLIODIxAwFp\n" +
        "4Qhksdtn7bEby5OhkQNLTdClGAbTe2tOO5Tiib9pcdruKxTodo481iGXTHR5033I\n" +
        "A5X55PegFeoY95NH5Noj6UUhyTFfRuwnhtGJgv9buTeBa4pLgHakfebqzKXr0Lce\n" +
        "/Ff1MnmQAdJTlvpOdVWJggsb26fD3cXyxQsbgtQYntmek2qvex/gPM9Nqa5qYrXx\n" +
        "8KuGuqHIFQa5t7UUH8WcxlLVRHWOtEQ3+Y6TQr8sIpSVszfhpjh9+Cag1EgaMzk+\n" +
        "HhAxMtXZgpyHffGHmPJ9eXbBO008tUzrE88fcuJ5pMF0LATO6ayXTKgZVU0WO/4e\n" +
        "iQIDAQAB\n" +
        "-----END PUBLIC KEY-----";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final ApiSatuSehat apiSatuSehat;
    private final String baseUrl;

    public SatuSehatKYCApi() {
        this.apiSatuSehat = new ApiSatuSehat();
        this.baseUrl = resolveBaseUrl(koneksiDB.URLAUTHSATUSEHAT(), koneksiDB.URLFHIRSATUSEHAT(), koneksiDB.URLSATUSEHATKYC());
    }

    public SatuSehatKYCApi(ApiSatuSehat apiSatuSehat, String baseUrl) {
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
            return "https://api-satusehat-stg.dto.kemkes.go.id/kyc/v1";
        }
        return "https://api-satusehat.kemkes.go.id/kyc/v1";
    }

    public static KeyPair generateRsa2048KeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        return kpg.generateKeyPair();
    }

    public static String formatPublicKeyPem(PublicKey publicKey) {
        String base64 = Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(publicKey.getEncoded());
        return "-----BEGIN PUBLIC KEY-----\n" + base64 + "\n-----END PUBLIC KEY-----";
    }

    public static PublicKey loadRsaPublicKey(String pem) throws Exception {
        String clean = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                          .replace("-----END PUBLIC KEY-----", "")
                          .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(clean);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(spec);
    }

    private static final OAEPParameterSpec OAEP_SHA256 = new OAEPParameterSpec(
        "SHA-256", "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT
    );

    public static String encryptMessage(String jsonMessage, String serverPubPem) throws Exception {
        PublicKey serverPubKey = loadRsaPublicKey(serverPubPem);

        // 1. Generate 32-byte AES key
        byte[] aesKey = new byte[32];
        new SecureRandom().nextBytes(aesKey);

        // 2. Encrypt AES key using RSA OAEP SHA-256 with server's public key (256 bytes)
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, serverPubKey, OAEP_SHA256);
        byte[] wrappedAesKey = rsaCipher.doFinal(aesKey);

        // 3. Encrypt message using AES-256-GCM (12 bytes IV, 16 bytes tag)
        byte[] iv = new byte[12];
        new SecureRandom().nextBytes(iv);
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        aesCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new GCMParameterSpec(128, iv));
        byte[] ciphertextAndTag = aesCipher.doFinal(jsonMessage.getBytes("UTF-8"));

        // 4. Combine: wrappedAesKey (256) + iv (12) + ciphertextAndTag
        byte[] combined = new byte[wrappedAesKey.length + iv.length + ciphertextAndTag.length];
        System.arraycopy(wrappedAesKey, 0, combined, 0, wrappedAesKey.length);
        System.arraycopy(iv, 0, combined, wrappedAesKey.length, iv.length);
        System.arraycopy(ciphertextAndTag, 0, combined, wrappedAesKey.length + iv.length, ciphertextAndTag.length);

        // 5. Base64 encode and format into envelope with 76-character chunks
        String base64Payload = Base64.getMimeEncoder(76, new byte[]{'\r', '\n'}).encodeToString(combined);
        return "-----BEGIN ENCRYPTED MESSAGE-----\r\n" + base64Payload + "\r\n-----END ENCRYPTED MESSAGE-----";
    }

    public static String decryptMessage(String encryptedEnvelope, PrivateKey clientPrivateKey) throws Exception {
        String beginTag = "-----BEGIN ENCRYPTED MESSAGE-----";
        String endTag = "-----END ENCRYPTED MESSAGE-----";

        int start = encryptedEnvelope.indexOf(beginTag);
        int end = encryptedEnvelope.indexOf(endTag);
        if (start == -1 || end == -1) {
            throw new IllegalArgumentException("Invalid encrypted envelope: missing begin or end tag");
        }

        String base64Content = encryptedEnvelope.substring(start + beginTag.length(), end).replaceAll("\\s+", "");
        byte[] binaryData = Base64.getDecoder().decode(base64Content);

        if (binaryData.length < 256 + 12 + 16) {
            throw new IllegalArgumentException("Encrypted payload too short: " + binaryData.length + " bytes");
        }

        // Split wrapped key (first 256 bytes) and encrypted message
        byte[] wrappedKey = Arrays.copyOfRange(binaryData, 0, 256);
        byte[] iv = Arrays.copyOfRange(binaryData, 256, 256 + 12);
        byte[] ciphertextAndTag = Arrays.copyOfRange(binaryData, 256 + 12, binaryData.length);

        // Unwrap AES key using client RSA private key with OAEP SHA-256
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
        rsaCipher.init(Cipher.DECRYPT_MODE, clientPrivateKey, OAEP_SHA256);
        byte[] aesKey = rsaCipher.doFinal(wrappedKey);

        // Decrypt message with AES-256-GCM
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        aesCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"), new GCMParameterSpec(128, iv));
        byte[] decryptedBytes = aesCipher.doFinal(ciphertextAndTag);

        return new String(decryptedBytes, "UTF-8");
    }

    public static String buildGenerateUrlPayload(String agentNik, String agentName, String publicKeyPem) {
        try {
            ObjectNode node = MAPPER.createObjectNode();
            node.put("agent_name", agentName != null ? agentName.trim() : "");
            node.put("agent_nik", agentNik != null ? agentNik.trim() : "");
            node.put("public_key", publicKeyPem != null ? publicKeyPem.trim() : "");
            return MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            return "{}";
        }
    }

    public static String buildChallengeCodePayload(String nik, String name) {
        try {
            ObjectNode root = MAPPER.createObjectNode();
            ObjectNode metadata = root.putObject("metadata");
            metadata.put("method", "request_per_nik");
            ObjectNode data = root.putObject("data");
            data.put("nik", nik != null ? nik.trim() : "");
            data.put("name", name != null ? name.trim() : "");
            return MAPPER.writeValueAsString(root);
        } catch (Exception e) {
            return "{}";
        }
    }

    public static SatuSehatKYCResponse parseResponse(int statusCode, String jsonResponse) {
        SatuSehatKYCResponse res = new SatuSehatKYCResponse();
        res.setStatusCode(statusCode);

        if (jsonResponse == null || jsonResponse.trim().isEmpty()) {
            res.setSuccess(statusCode >= 200 && statusCode < 300);
            res.setMessage("Respons kosong dari server SATUSEHAT KYC");
            return res;
        }

        try {
            JsonNode root = MAPPER.readTree(jsonResponse);
            JsonNode metadata = root.path("metadata");
            if (metadata.isObject()) {
                if (metadata.has("code")) {
                    res.setStatusCode(metadata.path("code").asInt(statusCode));
                }
                if (metadata.has("message")) {
                    res.setMessage(metadata.path("message").asText());
                }
            }

            res.setSuccess(res.getStatusCode() >= 200 && res.getStatusCode() < 300);

            JsonNode dataNode = root.path("data");
            if (dataNode.isObject()) {
                if (dataNode.has("token")) {
                    res.setToken(dataNode.path("token").asText());
                }
                if (dataNode.has("url")) {
                    res.setValidationUrl(dataNode.path("url").asText());
                }
                if (dataNode.has("agent_name")) {
                    res.setAgentName(dataNode.path("agent_name").asText());
                }
                if (dataNode.has("agent_nik")) {
                    res.setAgentNik(dataNode.path("agent_nik").asText());
                }
                if (dataNode.has("ihs_number")) {
                    res.setIhsNumber(dataNode.path("ihs_number").asText());
                }
                if (dataNode.has("challenge_code")) {
                    res.setChallengeCode(dataNode.path("challenge_code").asText());
                }
                if (dataNode.has("created_timestamp")) {
                    res.setCreatedTimestamp(dataNode.path("created_timestamp").asText());
                }
                if (dataNode.has("expired_timestamp")) {
                    res.setExpiredTimestamp(dataNode.path("expired_timestamp").asText());
                }
            }

            if (root.has("fault")) {
                JsonNode fault = root.path("fault");
                res.setSuccess(false);
                res.setMessage(fault.path("faultstring").asText());
                res.setErrorCode(fault.path("detail").path("errorcode").asText());
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

    public SatuSehatKYCResponse generateUrl(String agentNik, String agentName) {
        try {
            KeyPair clientKeyPair = generateRsa2048KeyPair();
            String clientPubPem = formatPublicKeyPem(clientKeyPair.getPublic());
            String innerJson = buildGenerateUrlPayload(agentNik, agentName, clientPubPem);

            boolean isStaging = baseUrl.contains("-stg") || baseUrl.contains("dto.kemkes.go.id");
            String serverPubPem = isStaging ? SERVER_PUBLIC_KEY_STAGING : SERVER_PUBLIC_KEY_PRODUCTION;

            String encryptedPayload = encryptMessage(innerJson, serverPubPem);

            return executePostEncrypted(baseUrl + "/generate-url", innerJson, encryptedPayload, clientKeyPair.getPrivate());
        } catch (Exception e) {
            return SatuSehatKYCResponse.error(500, "Gagal enkripsi payload KYC: " + e.getMessage(), "ENCRYPT_ERROR");
        }
    }

    public SatuSehatKYCResponse generateChallengeCode(String nik, String name, String frameToken) {
        String payload = buildChallengeCodePayload(nik, name);
        return executePostJson(baseUrl + "/challenge-code", payload, "1", frameToken);
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

    private SatuSehatKYCResponse executePostEncrypted(String url, String rawInnerJson, String encryptedPayload, PrivateKey clientPrivateKey) {
        System.out.println("================== [SATUSEHAT KYC REQUEST] ==================");
        System.out.println("Endpoint : POST " + url);
        System.out.println("Inner JSON:\n" + prettyPrintJson(rawInnerJson));
        System.out.println("Encrypted Envelope:\n" + encryptedPayload.substring(0, Math.min(120, encryptedPayload.length())) + "...");
        System.out.println("---------------------------------------------------------------");

        try {
            String token = apiSatuSehat.TokenSatuSehat();
            if (token == null || token.trim().isEmpty()) {
                System.out.println("[SATUSEHAT KYC RESPONSE] Error: Gagal mendapatkan token autentikasi (UNAUTHORIZED)");
                System.out.println("===============================================================");
                SatuSehatKYCResponse res = SatuSehatKYCResponse.error(401, "Gagal mendapatkan token autentikasi SATUSEHAT", "UNAUTHORIZED");
                res.setRawRequestBody(rawInnerJson);
                return res;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "text/plain");
            headers.set("Authorization", "Bearer " + token);
            headers.set("X-Debug-Mode", "0");

            HttpEntity<String> entity = new HttpEntity<>(encryptedPayload, headers);
            ResponseEntity<String> response = apiSatuSehat.getRest().exchange(url, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();

            System.out.println("[SATUSEHAT KYC RAW RESPONSE]");
            System.out.println("HTTP Code: " + response.getStatusCode().value());
            System.out.println("Body     : " + (responseBody != null && responseBody.length() > 100 ? responseBody.substring(0, 100) + "..." : responseBody));

            String jsonToParse = responseBody;
            if (responseBody != null && responseBody.contains("-----BEGIN ENCRYPTED MESSAGE-----")) {
                try {
                    jsonToParse = decryptMessage(responseBody, clientPrivateKey);
                    System.out.println("[SATUSEHAT KYC DECRYPTED RESPONSE]");
                    System.out.println(prettyPrintJson(jsonToParse));
                } catch (Exception de) {
                    System.err.println("Gagal dekripsi respons: " + de.getMessage());
                }
            } else {
                System.out.println("[SATUSEHAT KYC PLAIN RESPONSE]");
                System.out.println(prettyPrintJson(jsonToParse));
            }
            System.out.println("===============================================================");

            SatuSehatKYCResponse res = parseResponse(response.getStatusCode().value(), jsonToParse);
            res.setRawRequestBody(rawInnerJson);
            res.setRawResponseBody(jsonToParse);
            return res;
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            System.out.println("[SATUSEHAT KYC RESPONSE (HTTP ERROR)]");
            System.out.println("HTTP Code: " + e.getStatusCode().value());
            System.out.println("Response :\n" + prettyPrintJson(responseBody));
            System.out.println("===============================================================");

            SatuSehatKYCResponse res = parseResponse(e.getStatusCode().value(), responseBody);
            res.setRawRequestBody(rawInnerJson);
            res.setRawResponseBody(responseBody);
            return res;
        } catch (Exception e) {
            System.out.println("[SATUSEHAT KYC RESPONSE (NETWORK ERROR)]");
            System.out.println("Error: " + e.getMessage());
            System.out.println("===============================================================");

            SatuSehatKYCResponse res = SatuSehatKYCResponse.error(500, "Kesalahan jaringan: " + e.getMessage(), "NETWORK_ERROR");
            res.setRawRequestBody(rawInnerJson);
            res.setRawResponseBody(e.getMessage());
            return res;
        }
    }

    private SatuSehatKYCResponse executePostJson(String url, String jsonPayload, String debugMode, String frameToken) {
        System.out.println("================== [SATUSEHAT KYC JSON REQUEST] ==================");
        System.out.println("Endpoint : POST " + url);
        System.out.println("Payload  :\n" + prettyPrintJson(jsonPayload));
        System.out.println("---------------------------------------------------------------");

        try {
            String token = apiSatuSehat.TokenSatuSehat();
            if (token == null || token.trim().isEmpty()) {
                SatuSehatKYCResponse res = SatuSehatKYCResponse.error(401, "Gagal mendapatkan token autentikasi SATUSEHAT", "UNAUTHORIZED");
                res.setRawRequestBody(jsonPayload);
                return res;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + token);
            if (debugMode != null) {
                headers.set("X-Debug-Mode", debugMode);
            }
            if (frameToken != null && !frameToken.isEmpty()) {
                headers.set("X-Frame-Token", frameToken);
            }

            HttpEntity<String> entity = new HttpEntity<>(jsonPayload, headers);
            ResponseEntity<String> response = apiSatuSehat.getRest().exchange(url, HttpMethod.POST, entity, String.class);
            String responseBody = response.getBody();

            System.out.println("[SATUSEHAT KYC JSON RESPONSE]");
            System.out.println("HTTP Code: " + response.getStatusCode().value());
            System.out.println("Response :\n" + prettyPrintJson(responseBody));
            System.out.println("===============================================================");

            SatuSehatKYCResponse res = parseResponse(response.getStatusCode().value(), responseBody);
            res.setRawRequestBody(jsonPayload);
            res.setRawResponseBody(responseBody);
            return res;
        } catch (HttpStatusCodeException e) {
            String responseBody = e.getResponseBodyAsString();
            System.out.println("[SATUSEHAT KYC JSON RESPONSE (HTTP ERROR)]");
            System.out.println("HTTP Code: " + e.getStatusCode().value());
            System.out.println("Response :\n" + prettyPrintJson(responseBody));
            System.out.println("===============================================================");

            SatuSehatKYCResponse res = parseResponse(e.getStatusCode().value(), responseBody);
            res.setRawRequestBody(jsonPayload);
            res.setRawResponseBody(responseBody);
            return res;
        } catch (Exception e) {
            SatuSehatKYCResponse res = SatuSehatKYCResponse.error(500, "Kesalahan jaringan: " + e.getMessage(), "NETWORK_ERROR");
            res.setRawRequestBody(jsonPayload);
            res.setRawResponseBody(e.getMessage());
            return res;
        }
    }
}
