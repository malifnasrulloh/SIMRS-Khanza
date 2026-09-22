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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

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
                    Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}(?>\\.\\d+)?)(?!(?:\\.\\d+)?(?:Z|\\+00:?00|-00:?00))(\\s*[+-](?!00:?00)\\d{2}:?\\d{2})?");
                    Matcher matcher = pattern.matcher(bodyStr);
                    StringBuffer sb = new StringBuffer();
                    while (matcher.find()) {
                        String matchedDateTime = matcher.group(0);
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
            String str = localDateTime.trim();
            if (str.length() == 10) {
                str += " 00:00:00";
            } else if (str.length() == 16) {
                str += ":00";
            }
            // Normalize space before offset: "2026-09-22 10:05:26 +07:00" -> "2026-09-22T10:05:26+07:00"
            str = str.replaceAll("\\s+([+-])", "$1");
            str = str.replace(" ", "T");

            boolean hasOffset = str.matches(".*[+-]\\d{2}:?\\d{2}$");
            OffsetDateTime odt;
            if (hasOffset) {
                if (str.matches(".*[+-]\\d{4}$")) {
                    int len = str.length();
                    str = str.substring(0, len - 2) + ":" + str.substring(len - 2);
                }
                odt = OffsetDateTime.parse(str, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            } else {
                LocalDateTime ldt = LocalDateTime.parse(str, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                odt = ldt.atZone(ZoneId.of("Asia/Jakarta")).toOffsetDateTime();
            }
            OffsetDateTime utc = odt.withOffsetSameInstant(ZoneOffset.UTC);

            return utc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'+00:00'"));
        } catch (Exception e) {
            return localDateTime.replaceAll(" ", "T") + "+00:00";
        }
    }

}

