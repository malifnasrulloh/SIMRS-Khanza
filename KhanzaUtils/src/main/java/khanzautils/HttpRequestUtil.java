/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils;

import khanzautils.logger.SystemLogger;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author malifnasrulloh
 */
public class HttpRequestUtil {

    private static volatile HttpRequestUtil instance;

    public static HttpRequestUtil getInstance() {
        HttpRequestUtil localInstance = HttpRequestUtil.instance;
        if (localInstance == null) {
            synchronized (HttpRequestUtil.class) {
                localInstance = HttpRequestUtil.instance;
                if (localInstance == null) {
                    try {
                        HttpRequestUtil.instance = localInstance = new HttpRequestUtil();
                    } catch (Exception e) {
                        System.out.println("Failed to initialize HttpRequestUtil: " + e);
                        SystemLogger.error(e);
                    }
                }
            }
        }
        return localInstance;
    }

    private final RestTemplate restTemplate;

    private HttpRequestUtil() throws NoSuchAlgorithmException, KeyManagementException {
        this.restTemplate = new RestTemplate(getRequestFactory());
    }

    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    public <T, R> ResponseEntity<T> exchange(String url, HttpMethod method, R body, Class<T> responseType, HttpHeaders headers) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(url).build().toUri();
            RequestEntity<R> requestEntity = (body != null)
                    ? new RequestEntity(body, headers, method, uri)
                    : new RequestEntity(headers, method, uri);

            ResponseEntity<T> response = this.restTemplate.exchange(requestEntity, responseType);

            SystemLogger.http(String.format("%s request to %s completed with status %s", method, url, response.getStatusCode()));
            return response;
        } catch (Exception e) {
            SystemLogger.error(e);
            throw new RuntimeException("HTTP request [" + url + "] failed", e);
        }
    }

    private HttpComponentsClientHttpRequestFactory getRequestFactory() {
        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(30))
                        .build()
                )
                .setDefaultTlsConfig(TlsConfig.custom()
                        .setHandshakeTimeout(Timeout.ofSeconds(30))
                        .setSupportedProtocols(TLS.V_1_2, TLS.V_1_3)
                        .build())
                .setMaxConnPerRoute(100)
                .setMaxConnTotal(250)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(Timeout.ofSeconds(30))
                        .build())
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .evictIdleConnections(Timeout.ofSeconds(30))
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        factory.setConnectionRequestTimeout(Timeout.ofSeconds(30).toDuration());
        factory.setReadTimeout(Timeout.ofSeconds(30).toDuration());
        return factory;
    }
}
