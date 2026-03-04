/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils.bpjs;

import khanzautils.LogTableModel;
import java.util.Map;
import khanzautils.HttpRequestUtil;
import khanzautils.koneksiDB;
import khanzautils.logger.LogType;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 *
 * @author malifnasrulloh
 */
public class BPJSHelper {

    public static ResponseEntity<Map> exchangeMobileJKN(String URL, String requestJson, BPJSSecurityUtil security, LogTableModel logTableModel) {
        try {
            BPJSSecurityUtil.SignatureResult resultSignature = security.generateSignaturePair();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("x-cons-id", security.getConsId());
            headers.add("x-timestamp", String.valueOf(resultSignature.timestamp));
            headers.add("x-signature", resultSignature.signature);
            headers.add("user_key", security.getUserKey());

            HttpRequestUtil http = HttpRequestUtil.getInstance();
            ResponseEntity<Map> responseEntity = http.exchange(URL, HttpMethod.POST, requestJson, Map.class, headers);

            Map<String, Map> responseMap = responseEntity.getBody();
            if (responseMap != null && responseMap.containsKey("metadata") && logTableModel != null) {
                Map<String, Object> resMetadata = responseMap.get("metadata");
                logTableModel.tambahData("Request URL: " + URL, LogType.HTTP);
                logTableModel.tambahData("Request JSON: " + requestJson, LogType.HTTP);
                logTableModel.tambahData("Respon WS BPJS : " + resMetadata.get("code").toString() + " " + resMetadata.get("message").toString());
            }
            return responseEntity;
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Response WS BPJS {'%s'} kosong / null", URL));
        }
    }
}
