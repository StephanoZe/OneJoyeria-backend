package pe.onejoyeria.controlador;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import pe.onejoyeria.dto.MovimientoDto;
import pe.onejoyeria.dto.OpcionDto;
import pe.onejoyeria.dto.PanelDto;
import pe.onejoyeria.servicio.InventarioServicio;

import java.time.LocalDate;
import java.util.List;

/** Panel, listas de apoyo y consulta general de movimientos. */
@RestController
@RequestMapping("/api")
public class CatalogoControlador {

    private final InventarioServicio inventario;

    public CatalogoControlador(InventarioServicio inventario) {
        this.inventario = inventario;
    }

    // GET /api/panel  → todo lo que muestra el panel principal
    @GetMapping("/panel")
    public PanelDto panel() {
        return inventario.panel();
    }

    // GET /api/categorias
    @GetMapping("/categorias")
    public List<OpcionDto> categorias() {
        return inventario.categorias();
    }

    // GET /api/materiales
    @GetMapping("/materiales")
    public List<OpcionDto> materiales() {
        return inventario.materiales();
    }

    // GET /api/movimientos?desde=2026-11-01&hasta=2026-11-07&categoriaId=1&tipo=Entrada&limite=50
    @GetMapping("/movimientos")
    public List<MovimientoDto> movimientos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer limite) {
        return inventario.movimientos(desde, hasta, categoriaId, tipo, limite);
    }
}
