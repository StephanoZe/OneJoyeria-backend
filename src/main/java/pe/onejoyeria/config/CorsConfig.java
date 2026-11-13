package pe.onejoyeria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS: permite que el frontend Angular, que corre en otra dirección
 * (http://localhost:4200 o una URL de GitHub Codespaces), consuma la API.
 * Sin esto el navegador bloquearía las peticiones.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /** Orígenes permitidos, definidos en application.properties (app.cors.origenes). */
    @Value("${app.cors.origenes}")
    private String[] origenes;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns(origenes)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location", "Content-Disposition");
    }
}
