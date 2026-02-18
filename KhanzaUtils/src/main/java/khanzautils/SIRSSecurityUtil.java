/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils;

import khanzautils.logger.SystemLogger;
import java.time.Instant;
import java.time.ZoneId;
import org.apache.commons.codec.digest.DigestUtils;

/**
 *
 * @author malifnasrulloh
 */
public class SIRSSecurityUtil {

    private final String rsId;
    private final String pass;

    public SIRSSecurityUtil(String rsId, String pass) {
        if (rsId == null || rsId.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException("RS-ID cannot be null or empty"));
            throw new IllegalArgumentException("RS-ID cannot be null or empty");
        }
        if (pass == null || pass.trim().isEmpty()) {
            SystemLogger.error(new IllegalArgumentException("Password cannot be null or empty"));
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        this.rsId = rsId;
        this.pass = pass;
    }

    public long getCurrentTimestamp() {
        return Instant.now().atZone(ZoneId.of("UTC")).toEpochSecond();
    }

    public String generateMD5Pass() {
        try {
            return DigestUtils.md5Hex(pass);
        } catch (Exception e) {
            System.out.println("Failed to generate MD5 Hex" + e);
            SystemLogger.error(e);
            return "";
        }
    }

    public SignatureResult generateSignaturePair() {
        long timestamp = getCurrentTimestamp();
        String md5pass = generateMD5Pass();
        return new SignatureResult(timestamp, rsId, md5pass);
    }

    public static class SignatureResult {

        public final long timestamp;
        public final String rsId;
        public final String md5pass;

        public SignatureResult(long timestamp, String rsId, String md5pass) {
            this.timestamp = timestamp;
            this.rsId = rsId;
            this.md5pass = md5pass;
        }
    }
}
