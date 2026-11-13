package pe.onejoyeria.dto;

import java.math.BigDecimal;

public record ResumenDto(
        int totalProductos,
        BigDecimal valorInventario,
        int productosStockBajo,
        int productosAgotados,
        int totalUnidades,
        int entradasSemana,
        int salidasSemana
) { }
