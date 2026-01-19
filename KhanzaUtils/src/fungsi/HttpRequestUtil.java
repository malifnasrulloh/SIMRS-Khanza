/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package fungsi;

import fungsi.logger.SystemLogger;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
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

    private HttpRequestUtil() throws NoSuchAlgorithmException, KeyManagementException {
        this.restTemplate = new RestTemplate(getRequestFactory());
    }

    private HttpComponentsClientHttpRequestFactory getRequestFactory() {
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(
                        PoolingHttpClientConnectionManagerBuilder.create().build()
                )
                .build();

        HttpComponentsClientHttpRequestFactory factory
                = new HttpComponentsClientHttpRequestFactory(httpClient);

        factory.setConnectionRequestTimeout(10_000);
        factory.setReadTimeout(30_000);
        return factory;
    }
}
