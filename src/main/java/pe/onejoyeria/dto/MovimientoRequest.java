package pe.onejoyeria.dto;

/** Cuerpo para registrar una entrada o salida de stock. */
public record MovimientoRequest(
        /** "Entrada" o "Salida". */
        String tipoMovimiento,
        Integer cantidad,
        String responsable,
        String observaciones
) { }
