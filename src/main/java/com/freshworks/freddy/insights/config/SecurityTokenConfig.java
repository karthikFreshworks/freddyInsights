package com.freshworks.freddy.insights.config;

//@Configuration
//@EnableWebSecurity
public class SecurityTokenConfig {
    public static String[] URL_PATHS = new String[]{
            "/v3/api-docs**",
            "/swagger-ui**",
            "/actuator/**",
            "/"
    };

    //    @Bean
    //    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    //        http.authorizeHttpRequests(authorizeRequest ->
    //                         authorizeRequest.anyRequest().permitAll());
    //        return http.build();
    //    }
}
