package pe.onejoyeria.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ProveedorDto(
        Long id,
        String nombre,
        String ruc,
        String contacto,
        String telefono,
        String email,
        String direccion,
        String suministra,
        String observaciones,
        LocalDateTime fechaRegistro,
        /** Piezas del inventario vinculadas a este proveedor (sólo en el detalle). */
        List<ProductoDto> productos
) { }
