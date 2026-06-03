package bridging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fungsi.koneksiDB;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.HttpRequest;
import java.util.Collections;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ApiSatuSehat {        
    private static final long TOKEN_CACHE_MS = 60_000L;
    private String key, clientid, urlauth, token;
    private long tokenRetrievedAt;
    private long millis;
    private SSLContext sslContext;
    private SSLSocketFactory sslFactory;
    private Scheme scheme;
    private HttpComponentsClientHttpRequestFactory factory;
    private ApiBPJSAesKeySpec mykey;
    private HttpHeaders header ;
    private JsonNode root;
    private HttpEntity requestEntity;
    private ObjectMapper mapper = new ObjectMapper();
    
    public ApiSatuSehat(){
        try {
            key = koneksiDB.SECRETKEYSATUSEHAT();
            clientid = koneksiDB.CLIENTIDSATUSEHAT();
            urlauth = koneksiDB.URLAUTHSATUSEHAT();
        } catch (Exception ex) {
            System.out.println("Notifikasi : "+ex);
        }
    }

    public String TokenSatuSehat(){
        long now = System.currentTimeMillis();
        if (token != null && (now - tokenRetrievedAt) < TOKEN_CACHE_MS) {
            return token;
        }

        try {
            header = new HttpHeaders();
            header.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            requestEntity = new HttpEntity("client_id=" + clientid + "&client_secret=" + key, header);
            root = mapper.readTree(getRest().exchange(urlauth + "/accesstoken?grant_type=client_credentials", HttpMethod.POST, requestEntity, String.class).getBody());
            String refreshedToken = root.path("access_token").asText();
            if (refreshedToken != null && !refreshedToken.isEmpty()) {
                token = refreshedToken;
                tokenRetrievedAt = now;
            }
        } catch (Exception ex) {
            System.out.println("Notifikasi : " + ex);
            if (token != null && !token.isEmpty()) {
                return token;
            }
        }
        return token;
    }
        
    public long GetUTCdatetimeAsString(){    
        millis = System.currentTimeMillis();   
        return millis/1000;
    }
    
    public String Decrypt(String data,String utc)throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        System.out.println(data);
        mykey = ApiBPJSEnc.generateKey(clientid+key+utc);
        data=ApiBPJSEnc.decrypt(data, mykey.getKey(), mykey.getIv());
        data=ApiBPJSLZString.decompressFromEncodedURIComponent(data);
        System.out.println(data);
        return data;
    }
    
    public RestTemplate getRest() throws NoSuchAlgorithmException, KeyManagementException {
        sslContext = SSLContext.getInstance("TLSv1.2");
        TrustManager[] trustManagers= {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {return null;}
                public void checkServerTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
                public void checkClientTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}
            }
        };
        sslContext.init(null,trustManagers , new SecureRandom());
        sslFactory=new SSLSocketFactory(sslContext,SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        scheme=new Scheme("https",443,sslFactory);
        factory=new HttpComponentsClientHttpRequestFactory();
        factory.getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        ClientHttpRequestInterceptor interceptor = new ClientHttpRequestInterceptor() {
            @Override
            public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws java.io.IOException {
                try {
                    String bodyStr = new String(body, "UTF-8");
                    Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2})(?!\\+00:00|\\+0000|Z)(?:\\+07:00|\\+0700)?");
                    Matcher matcher = pattern.matcher(bodyStr);
                    StringBuffer sb = new StringBuffer();
                    while (matcher.find()) {
                        String matchedDateTime = matcher.group(1);
                        String utcDateTime = convertLocalToUtc(matchedDateTime);
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(utcDateTime));
                    }
                    matcher.appendTail(sb);
                    body = sb.toString().getBytes("UTF-8");
                } catch (Exception e) {
                    System.out.println("ApiSatuSehat Interceptor Error: " + e);
                }
                return execution.execute(request, body);
            }
        };
        restTemplate.setInterceptors(new ClientHttpRequestInterceptor[] { interceptor });
        
        return restTemplate;
    }

    public String convertLocalToUtc(String localDateTime) {
        try {
            if (localDateTime == null) return "";
            localDateTime = localDateTime.trim();
            if (localDateTime.contains(".")) {
                localDateTime = localDateTime.split("\\.")[0];
            }
            if (localDateTime.length() == 16) {
                localDateTime += ":00";
            }
            if (localDateTime.length() == 10) {
                localDateTime += " 00:00:00";
            }
            java.text.SimpleDateFormat sdfInput = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdfInput.setTimeZone(java.util.TimeZone.getTimeZone("Asia/Jakarta"));
            java.util.Date date = sdfInput.parse(localDateTime.replace("T", " "));
            java.text.SimpleDateFormat sdfOutput = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'+00:00'");
            sdfOutput.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return sdfOutput.format(date);
        } catch (Exception e) {
            return localDateTime.replaceAll(" ", "T") + "+00:00";
        }
    }

}

