package com.freshworks.freddy.insights.handler.http.client;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.Arrays;

@Slf4j
@Component
public class ApacheHttp4ClientHandlerImpl extends AbstractHttpClientHandler<CloseableHttpClient> {
    private final CloseableHttpClient httpClient;

    public ApacheHttp4ClientHandlerImpl() {
        RequestConfig requestConfig = createRequestConfig();
        Registry<ConnectionSocketFactory> socketFactoryRegistry = createSocketFactoryRegistry();
        PoolingHttpClientConnectionManager connectionManager = createConnectionManager(socketFactoryRegistry);
        this.httpClient = createHttpClient(connectionManager, requestConfig);
    }

    private RequestConfig createRequestConfig() {
        return RequestConfig.custom()
                .setSocketTimeout(120 * 1000)
                .setConnectTimeout(120 * 1000)
                .build();
    }

    private Registry<ConnectionSocketFactory> createSocketFactoryRegistry() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", createTrustAllSSLSocketFactory())
                .build();
    }

    private SSLConnectionSocketFactory createTrustAllSSLSocketFactory() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }

                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                    }
            };

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            String[] tlsVersions = sslContext.getSupportedSSLParameters().getProtocols();
            String[] tlsOnly = Arrays.stream(tlsVersions).filter(p -> p.startsWith("TLS")).toArray(String[]::new);
            return new SSLConnectionSocketFactory(
                    sslContext, tlsOnly, null, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            log.error("Error creating SSLConnectionSocketFactory: {}. CAUSE {}", e.getMessage(),
                    ExceptionHelper.stackTrace(e));
            throw new AIResponseStatusException("Failed to create SSLConnectionSocketFactory : " + e.getMessage());
        }
    }

    private PoolingHttpClientConnectionManager createConnectionManager(Registry<ConnectionSocketFactory> registry) {
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
        manager.setMaxTotal(50);
        manager.setDefaultMaxPerRoute(20);
        return manager;
    }

    private CloseableHttpClient createHttpClient(PoolingHttpClientConnectionManager connectionManager,
                                                 RequestConfig requestConfig) {
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
    }

    public CloseableHttpClient httpClient() {
        return httpClient;
    }
}
