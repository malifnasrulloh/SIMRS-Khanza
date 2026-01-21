/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fungsi;

import fungsi.logger.SystemLogger;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.TlsConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.client5.http.io.HttpClientConnectionManager;
import org.apache.hc.core5.http.ssl.TLS;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

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

    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }

    private HttpRequestUtil() throws NoSuchAlgorithmException, KeyManagementException {
        this.restTemplate = new RestTemplate(getRequestFactory());
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
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(50)
                .build();

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(cm)
                .evictExpiredConnections()
                .evictIdleConnections(Timeout.ofSeconds(30))
                .build();

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

        factory.setConnectionRequestTimeout(30_000);
        factory.setReadTimeout(30_000);
        return factory;
    }
}
