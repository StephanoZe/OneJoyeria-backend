package pe.onejoyeria.dto;

/** Respuesta uniforme de error: Postman y la página siempre reciben {"mensaje": "..."}. */
public record ErrorDto(String mensaje) { }
