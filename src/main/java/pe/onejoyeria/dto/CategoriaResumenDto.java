package pe.onejoyeria.dto;

import java.math.BigDecimal;

/** Una fila del resumen por tipo de joya (compartimento de la bandeja). */
public record CategoriaResumenDto(
        Long categoriaId,
        String categoria,
        int productos,
        int unidades,
        BigDecimal valor,
        int agotados,
        int stockBajo
) { }
