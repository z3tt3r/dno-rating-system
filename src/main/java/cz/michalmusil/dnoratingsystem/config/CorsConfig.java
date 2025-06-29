package cz.michalmusil.dnoratingsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**") // Povolí CORS pro všechny endpointy pod /api/
                        .allowedOrigins("http://localhost:5173") // Povolí požadavky z vašeho React frontendu
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Povolí tyto HTTP metody
                        .allowedHeaders("*") // Povolí všechny hlavičky
                        .allowCredentials(true); // Povolí posílání cookies a autentizačních hlaviček
            }
        };
    }
}