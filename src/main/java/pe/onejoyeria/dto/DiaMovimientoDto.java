package pe.onejoyeria.dto;

import java.time.LocalDate;

public record DiaMovimientoDto(LocalDate fecha, String dia, int entradas, int salidas) { }
