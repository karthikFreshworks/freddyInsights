package com.freshworks.freddy.insights.handler.http.client;

import com.freshworks.freddy.insights.exception.AIResponseStatusException;
import com.freshworks.freddy.insights.helper.AppConfigHelper;
import com.freshworks.freddy.insights.helper.ExceptionHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import java.util.Arrays;

@Slf4j
@Component
public class ApacheHttpClientHandlerImpl extends AbstractHttpClientHandler<CloseableHttpClient> {
    private final CloseableHttpClient httpClient;
    private final AppConfigHelper appConfigHelper;

    public ApacheHttpClientHandlerImpl(AppConfigHelper appConfigHelper) {
        this.appConfigHelper = appConfigHelper;
        RequestConfig requestConfig = createRequestConfig();
        Registry<ConnectionSocketFactory> socketFactoryRegistry = createSocketFactoryRegistry();
        PoolingHttpClientConnectionManager connectionManager = createConnectionManager(socketFactoryRegistry);
        this.httpClient = createHttpClient(connectionManager, requestConfig);
    }

    private RequestConfig createRequestConfig() {
        return RequestConfig.custom()
                .setConnectionKeepAlive(TimeValue.ofMinutes(5))
                .setContentCompressionEnabled(true)
                .setResponseTimeout(Timeout.ofSeconds(120))
                .setConnectionRequestTimeout(Timeout.ofSeconds(120)).build();
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
        manager.setMaxTotal(appConfigHelper.getApacheHttpMaxConnection());
        manager.setDefaultMaxPerRoute(appConfigHelper.getApacheHttpMaxConnectionPerRoute());
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
