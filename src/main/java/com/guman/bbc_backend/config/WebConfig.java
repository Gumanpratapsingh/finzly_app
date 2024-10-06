// package com.guman.bbc_backend.config;

// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
// import org.springframework.web.filter.CorsFilter;

// @Configuration
// public class WebConfig {

//     @Bean
//     public CorsFilter corsFilter() {
//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         CorsConfiguration config = new CorsConfiguration();
//         config.setAllowCredentials(true);
//         config.addAllowedOrigin("https://frontendfinzly-production.up.railway.app");
//         config.addAllowedHeader("*");
//         config.addAllowedMethod("*");
//         source.registerCorsConfiguration("/**", config);
//         return new CorsFilter(source);
//     }
// }

package com.guman.bbc_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
public class WebConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (needed for cookies, etc.)
        config.setAllowCredentials(true);

        // Allow the specific origin
        config.setAllowedOrigins(List.of("http://localhost:4200/"));

        // Allow all headers
        config.addAllowedHeader("*");

        // Allow all HTTP methods (GET, POST, etc.)
        config.addAllowedMethod("*");

        // Add specific methods to handle preflight requests
        config.addAllowedMethod("OPTIONS");

        // Register the configuration to apply to all paths
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
