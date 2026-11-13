package pe.onejoyeria.excepcion;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pe.onejoyeria.dto.ErrorDto;

/** Convierte cualquier error en un JSON {"mensaje": "..."} con el código HTTP adecuado. */
@RestControllerAdvice
public class ManejadorErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErrores.class);

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorDto> negocio(ApiException ex) {
        return ResponseEntity.status(ex.getEstado()).body(new ErrorDto(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> cuerpoInvalido(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorDto("El cuerpo de la petición no es un JSON válido o falta."));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorDto("El parámetro '" + ex.getName() + "' tiene un formato no válido."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorDto> rutaInexistente(NoResourceFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorDto("La ruta solicitada no existe."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorDto> inesperado(Exception ex) {
        // Errores propios de Spring MVC (405 método no permitido, 415 tipo no soportado...)
        if (ex instanceof ErrorResponse respuesta) {
            return ResponseEntity.status(respuesta.getStatusCode())
                    .body(new ErrorDto("Petición no válida: " + ex.getMessage()));
        }
        log.error("Error no controlado", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorDto("Ocurrió un error interno: " + ex.getMessage()));
    }
}
