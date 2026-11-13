package pe.onejoyeria.dto;

import java.util.List;

/** Todo lo que necesita el panel principal en una sola llamada. */
public record PanelDto(
        ResumenDto resumen,
        List<CategoriaResumenDto> categorias,
        List<DiaMovimientoDto> semana,
        List<MovimientoDto> recientes,
        List<ProductoDto> alertas
) { }
