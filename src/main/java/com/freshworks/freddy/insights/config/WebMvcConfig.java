package com.freshworks.freddy.insights.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Value("${freddy-cx-auth.cors.enable:false}")
    private Boolean corsEnable;
    @Value("${freddy-cx-auth.cors.allow-credentials:false}")
    private Boolean corsAllowCredentials;
    @Value("${freddy-cx-auth.cors.path-pattern:/**}")
    private String corsPathPattern;
    @Value("${freddy-cx-auth.cors.allowed-methods:}")
    private String[] corsAllowedMethods;

    @Value("${freddy-cx-auth.cors.allowed-origins:}")
    private String[] corsAllowedOrigins;
    @Value("${freddy-cx-auth.cors.allowed-headers:}")
    private String[] corsAllowedHeaders;
    @Value("${freddy-cx-auth.cors.exposed-headers:}")
    private String[] corsExposedHeaders;
    @Value("${freddy-cx-auth.resttemplate.timeout:15000}")
    private Integer timeout;

    /*
     * RestTemplate customisations for timeout and logging.
     */
    //    @Bean
    //    @Autowired
    //    public RestTemplate configureRestTemplate(RestTemplateBuilder restTemplateBuilder) {
    //        ClientHttpRequestFactory factory =
    //                new BufferingClientHttpRequestFactory(getClientHttpRequestFactory());
    //        RestTemplate restTemplate = new RestTemplate(factory);
    //        //Add Inteceptors
    //        List<ClientHttpRequestInterceptor> interceptors = restTemplate.getInterceptors();
    //
    //        if (CollectionUtils.isEmpty(interceptors)) {
    //            interceptors = new ArrayList<>();
    //        }
    //        interceptors.add(new RestTemplateLoggingInterceptor());
    //        restTemplate.setInterceptors(interceptors);
    //        //Update StringHttpMessageConverter to remove all AcceptCharsets.
    //        List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
    //
    //        for (HttpMessageConverter converter : messageConverters) {
    //            if (converter instanceof StringHttpMessageConverter) {
    //                ((StringHttpMessageConverter) converter).setWriteAcceptCharset(false);
    //            }
    //        }
    //        return restTemplate;
    //    }

    //Override timeouts in request factory
    //    private HttpComponentsClientHttpRequestFactory getClientHttpRequestFactory() {
    //        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory
    //                = new HttpComponentsClientHttpRequestFactory();
    //        //Connect timeout
    //        clientHttpRequestFactory.setConnectTimeout(timeout);
    //        //Read timeout
    //        //clientHttpRequestFactory.setReadTimeout(timeout);
    //        return clientHttpRequestFactory;
    //    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (corsEnable) {
            CorsRegistration corsRegistration = registry.addMapping(corsPathPattern);
            corsRegistration.allowCredentials(corsAllowCredentials);
            corsRegistration.allowedMethods(corsAllowedMethods);
            corsRegistration.allowedOrigins("*");
            corsRegistration.allowedHeaders("*");
            corsRegistration.exposedHeaders(corsExposedHeaders);
        }
    }
}
