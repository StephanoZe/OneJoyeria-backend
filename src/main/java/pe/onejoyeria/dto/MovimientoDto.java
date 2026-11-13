package pe.onejoyeria.dto;

import java.time.LocalDateTime;

public record MovimientoDto(
        Long id,
        Long productoId,
        String codigoProducto,
        String nombreProducto,
        String categoria,
        String tipoMovimiento,
        int cantidad,
        int stockAnterior,
        int stockNuevo,
        String observaciones,
        String responsable,
        LocalDateTime fechaMovimiento
) { }
