package pe.onejoyeria.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductoDto(
        Long id,
        String codigo,
        String nombre,
        String descripcion,
        Long categoriaId,
        String categoria,
        Long materialId,
        String material,
        String colorMaterial,
        Long proveedorId,
        String proveedor,
        BigDecimal precio,
        int stockActual,
        int stockMinimo,
        BigDecimal valorTotal,
        /** Disponible, Stock bajo o Agotado (RN-04 y RN-05). */
        String estado,
        LocalDateTime fechaRegistro
) { }
