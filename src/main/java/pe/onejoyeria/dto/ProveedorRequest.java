package pe.onejoyeria.dto;

public record ProveedorRequest(
        String nombre,
        String ruc,
        String contacto,
        String telefono,
        String email,
        String direccion,
        String suministra,
        String observaciones
) { }
