package pe.onejoyeria.controlador;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.onejoyeria.dto.ErrorDto;
import pe.onejoyeria.dto.LoginRequest;
import pe.onejoyeria.dto.LoginResponse;

/**
 * Inicio de sesión del administrador.
 * Las credenciales se configuran en application.properties (app.admin.*).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthControlador {

    @Value("${app.admin.usuario}")
    private String usuarioAdmin;

    @Value("${app.admin.contrasena}")
    private String contrasenaAdmin;

    @Value("${app.admin.nombre}")
    private String nombreAdmin;

    // POST /api/auth/login   { "usuario": "admin", "contrasena": "admin" }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody(required = false) LoginRequest req) {
        if (req == null || req.usuario() == null || req.usuario().isBlank()
                || req.contrasena() == null || req.contrasena().isBlank()) {
            return ResponseEntity.badRequest().body(new ErrorDto("Ingresa tu usuario y contraseña."));
        }
        if (usuarioAdmin.equals(req.usuario().trim()) && contrasenaAdmin.equals(req.contrasena())) {
            return ResponseEntity.ok(new LoginResponse(usuarioAdmin, nombreAdmin, "Administrador"));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorDto("Usuario o contraseña incorrectos."));
    }
}
