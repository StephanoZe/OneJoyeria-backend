package pe.onejoyeria;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del sistema de inventario de One Joyería.
 *
 * Al ejecutar esta clase (botón "Run" en VS Code) se levanta:
 *   - La página web en      http://localhost:8080
 *   - La API REST en        http://localhost:8080/api/...   (la que se prueba con Postman)
 *   - La consola de BD en   http://localhost:8080/h2-console
 */
@SpringBootApplication
public class OneJoyeriaApplication {

    public static void main(String[] args) {
        SpringApplication.run(OneJoyeriaApplication.class, args);
    }
}
