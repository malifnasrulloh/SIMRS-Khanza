/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils.bpjs;

import khanzautils.logger.SystemLogger;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author malifnasrulloh
 */
public class BPJSSecurityUtil {

    private final String consId;
    private final String secretKey;
    private final String userKey;

    public BPJSSecurityUtil(String consId, String secretKey, String userKey) {
        if (consId == null || consId.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException("Cons-ID cannot be null or empty"));
            throw new IllegalArgumentException("Cons-ID cannot be null or empty");
        }
        if (secretKey == null || secretKey.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException("Secret Key cannot be null or empty"));
            throw new IllegalArgumentException("Secret Key cannot be null or empty");
        }
        if (userKey == null || userKey.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException("User Key cannot be null or empty"));
            throw new IllegalArgumentException("User Key cannot be null or empty");
        }
        this.consId = consId;
        this.secretKey = secretKey;
        this.userKey = userKey;
    }

    public long getCurrentTimestamp() {
        return Instant.now().atZone(ZoneId.of("UTC")).toEpochSecond();
    }

    public String generateSignature(long timestamp) {
        String data = getConsId() + "&" + timestamp;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(keySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            System.out.println("Failed to generate HMAC signature" + e);
            SystemLogger.error(e);
            return "";
        }
    }

    public SignatureResult generateSignaturePair() {
        long timestamp = getCurrentTimestamp();
        String signature = generateSignature(timestamp);
        return new SignatureResult(timestamp, signature);
    }

    public static class SignatureResult {

        public final long timestamp;
        public final String signature;

        public SignatureResult(long timestamp, String signature) {
            this.timestamp = timestamp;
            this.signature = signature;
        }
    }

    /**
     * @return the consId
     */
    public String getConsId() {
        return consId;
    }

    /**
     * @return the secretKey
     */
    public String getSecretKey() {
        return secretKey;
    }
    
    /**
     * @return the userKey
     */
    public String getUserKey() {
        return userKey;
    }
}
