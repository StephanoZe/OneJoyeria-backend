package pe.onejoyeria.dto;

import java.math.BigDecimal;

/**
 * Cuerpo para crear o actualizar una pieza.
 * - codigo: si se deja vacío al crear, el sistema genera uno (JOY-021, JOY-022...).
 * - stockActual: sólo se usa al crear (se registra como movimiento "Stock inicial").
 *   Para cambiar el stock después se usan los movimientos de entrada/salida.
 */
public record ProductoRequest(
        String codigo,
        String nombre,
        String descripcion,
        Long categoriaId,
        Long materialId,
        Long proveedorId,
        BigDecimal precio,
        Integer stockActual,
        Integer stockMinimo
) { }
