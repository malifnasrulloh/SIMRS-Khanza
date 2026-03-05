/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package khanzautils;

import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import khanzautils.logger.SystemLogger;
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
import org.springframework.http.HttpStatusCode;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 *
 * @author malifnasrulloh
 */
public class HttpRequestUtil {

    private static volatile HttpRequestUtil instance;

    public static HttpRequestUtil getInstance() {
        HttpRequestUtil localInstance = instance;
        if (localInstance == null) {
            synchronized (HttpRequestUtil.class) {
                localInstance = instance;
                if (localInstance == null) {
                    try {
                        instance = localInstance = new HttpRequestUtil(HttpRequestConfig.defaults());
                    } catch (NoSuchAlgorithmException | KeyManagementException e) {
                        SystemLogger.error(e);
                        throw new ExceptionInInitializerError(e);
                    }
                }
            }
        }
        return localInstance;
    }

    private static HttpComponentsClientHttpRequestFactory buildRequestFactory(HttpRequestConfig config) {
        HttpClientConnectionManager cm = PoolingHttpClientConnectionManagerBuilder.create()
                .setDefaultConnectionConfig(ConnectionConfig.custom()
                        .setConnectTimeout(Timeout.ofSeconds(config.connectTimeoutSeconds()))
                        .build())
                .setDefaultTlsConfig(TlsConfig.custom()
                        .setHandshakeTimeout(Timeout.ofSeconds(config.tlsHandshakeTimeoutSeconds()))
                        .setSupportedProtocols(TLS.V_1_2, TLS.V_1_3)
                        .build())
                .setMaxConnPerRoute(config.maxConnectionsPerRoute())
                .setMaxConnTotal(config.maxConnectionsTotal())
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setResponseTimeout(Timeout.ofSeconds(config.responseTimeoutSeconds()))
                        .build())
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .evictIdleConnections(Timeout.ofSeconds(config.idleTimeoutSeconds()))
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectionRequestTimeout(
                Timeout.ofSeconds(config.connectionRequestTimeoutSeconds()).toDuration());
        factory.setReadTimeout(
                Timeout.ofSeconds(config.readTimeoutSeconds()).toDuration());

        return factory;
    }

    private final RestTemplate restTemplate;

    private HttpRequestUtil(HttpRequestConfig config) throws NoSuchAlgorithmException, KeyManagementException {
        this.restTemplate = new RestTemplate(buildRequestFactory(config));
    }

    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    public <T, R> ResponseEntity<T> exchange(String url, HttpMethod method, R body, Class<T> responseType, HttpHeaders headers) {
        Objects.requireNonNull(url, "url must not be null");
        Objects.requireNonNull(method, "method must not be null");
        Objects.requireNonNull(responseType, "responseType must not be null");

        long startTime = System.currentTimeMillis();
        SystemLogger.http(String.format("%s %s — request initiated", method, url));

        try {
            URI uri = UriComponentsBuilder.fromUriString(url).build().toUri();

            RequestEntity<R> requestEntity = (body != null)
                    ? new RequestEntity<>(body, headers, method, uri)
                    : new RequestEntity<>(headers, method, uri);

            ResponseEntity<T> response = this.restTemplate.exchange(requestEntity, responseType);

            long elapsed = System.currentTimeMillis() - startTime;
            SystemLogger.http(String.format("%s %s — %s (%d ms)",
                    method, url, response.getStatusCode(), elapsed));

            return response;

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logAndThrow(url, method, e.getStatusCode(), e, startTime);
        } catch (ResourceAccessException e) {
            long elapsed = System.currentTimeMillis() - startTime;
            SystemLogger.http(String.format("%s %s — connection/timeout error (%d ms): %s", method, url, elapsed, e.getMessage()));
            throw new HttpRequestException(url, method, e);
        } catch (Exception e) {
            long elapsed = System.currentTimeMillis() - startTime;
            SystemLogger.http(String.format("%s %s — unexpected error (%d ms): %s", method, url, elapsed, e.getMessage()));
            throw new HttpRequestException(url, method, e);
        }

        throw new AssertionError("unreachable");
    }

    public <T> ResponseEntity<T> get(String url, Class<T> responseType, HttpHeaders headers) {
        return exchange(url, HttpMethod.GET, null, responseType, headers);
    }

    public <T, R> ResponseEntity<T> post(String url, R body, Class<T> responseType, HttpHeaders headers) {
        return exchange(url, HttpMethod.POST, body, responseType, headers);
    }

    public <T, R> ResponseEntity<T> put(String url, R body, Class<T> responseType, HttpHeaders headers) {
        return exchange(url, HttpMethod.PUT, body, responseType, headers);
    }

    public <T> ResponseEntity<T> delete(String url, Class<T> responseType, HttpHeaders headers) {
        return exchange(url, HttpMethod.DELETE, null, responseType, headers);
    }

    private void logAndThrow(String url, HttpMethod method, HttpStatusCode status, Exception cause, long startTime) {
        long elapsed = System.currentTimeMillis() - startTime;
        SystemLogger.error(cause);
        SystemLogger.http(String.format("%s %s — %s (%d ms)", method, url, status, elapsed));
        throw new HttpRequestException(url, method, status, cause);
    }

    public record HttpRequestConfig(
            long connectTimeoutSeconds,
            long responseTimeoutSeconds,
            long readTimeoutSeconds,
            long connectionRequestTimeoutSeconds,
            long tlsHandshakeTimeoutSeconds,
            long idleTimeoutSeconds,
            int maxConnectionsPerRoute,
            int maxConnectionsTotal
            ) {

        public static HttpRequestConfig defaults() {
            return new HttpRequestConfig(30, 30, 30, 30, 30, 30, 100, 250);
        }
    }
}
