/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AESsecurity;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author khanzamedia
 */
public class EnkripsiAES {

    private static final String KEY = "Bar12345Bar12345"; // 128 bit key
    private static final String INITVECTOR = "sayangsamakhanza"; // 16 bytes IV

    public static String decrypt(String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INITVECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] decoded = Base64.decodeBase64(encrypted);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(ex);
            System.out.println("Ciluk Baaaaaaaa!!!!!");
        }

        return null;
    }

    public static String encrypt(String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(INITVECTOR.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec skeySpec = new SecretKeySpec(KEY.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            System.out.println("Ciluk Baaaaaaaa!!!!!");
        }
        return null;
    }

    public static String encrypt_decrypt(String value, String action) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String keyHex = bytesToHex(digest.digest(KEY.getBytes(StandardCharsets.UTF_8)));
            String ivHex = bytesToHex(digest.digest(INITVECTOR.getBytes(StandardCharsets.UTF_8)));

            SecretKeySpec skeySpec = new SecretKeySpec(Arrays.copyOf(keyHex.getBytes(StandardCharsets.UTF_8), 32), "AES");
            IvParameterSpec iv = new IvParameterSpec(Arrays.copyOf(ivHex.getBytes(StandardCharsets.UTF_8), 16));

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(action.equalsIgnoreCase("e") ? Cipher.ENCRYPT_MODE : Cipher.DECRYPT_MODE, skeySpec, iv);

            if (action.equalsIgnoreCase("e")) {
                String encoded1 = Base64.encodeBase64String(cipher.doFinal(value.getBytes(StandardCharsets.UTF_8)));
                return Base64.encodeBase64String(encoded1.getBytes(StandardCharsets.UTF_8));
            } else {
                byte[] decoded1 = Base64.decodeBase64(value);
                byte[] decoded2 = Base64.decodeBase64(new String(decoded1, StandardCharsets.UTF_8));
                return new String(cipher.doFinal(decoded2), StandardCharsets.UTF_8);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("Ciluk Baaaaaaaa!!!!!");
        }
        return null;
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
