package pe.onejoyeria.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.onejoyeria.dto.*;
import pe.onejoyeria.servicio.InventarioServicio;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** API REST del inventario de piezas. */
@RestController
@RequestMapping("/api/productos")
public class ProductoControlador {

    private final InventarioServicio inventario;

    public ProductoControlador(InventarioServicio inventario) {
        this.inventario = inventario;
    }

    // GET /api/productos?busqueda=&categoriaId=&estado=agotado|bajo|ok
    @GetMapping
    public List<ProductoDto> listar(@RequestParam(required = false) String busqueda,
                                    @RequestParam(required = false) Long categoriaId,
                                    @RequestParam(required = false) String estado) {
        return inventario.listar(busqueda, categoriaId, estado);
    }

    // GET /api/productos/resumen
    @GetMapping("/resumen")
    public ResumenDto resumen() {
        return inventario.resumen();
    }

    // GET /api/productos/alertas
    @GetMapping("/alertas")
    public List<ProductoDto> alertas() {
        return inventario.alertas();
    }

    // GET /api/productos/{codigo}
    @GetMapping("/{codigo}")
    public ProductoDto obtener(@PathVariable String codigo) {
        return inventario.obtener(codigo);
    }

    // POST /api/productos
    @PostMapping
    public ResponseEntity<ProductoDto> crear(@RequestBody(required = false) ProductoRequest req,
                                             @RequestHeader(value = "X-Responsable", required = false) String responsable) {
        // La página envía el nombre codificado para admitir tildes en la cabecera HTTP.
        String quien = responsable == null ? null : URLDecoder.decode(responsable, StandardCharsets.UTF_8);
        ProductoDto creado = inventario.crear(req, quien);
        return ResponseEntity.created(URI.create("/api/productos/" + creado.codigo())).body(creado);
    }

    // PUT /api/productos/{codigo}
    @PutMapping("/{codigo}")
    public ProductoDto actualizar(@PathVariable String codigo,
                                  @RequestBody(required = false) ProductoRequest req) {
        return inventario.actualizar(codigo, req);
    }

    // DELETE /api/productos/{codigo}
    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        inventario.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }

    // GET /api/productos/{codigo}/movimientos
    @GetMapping("/{codigo}/movimientos")
    public List<MovimientoDto> historial(@PathVariable String codigo) {
        return inventario.historial(codigo);
    }

    // POST /api/productos/{codigo}/movimientos
    @PostMapping("/{codigo}/movimientos")
    public ResponseEntity<MovimientoDto> registrarMovimiento(@PathVariable String codigo,
                                                             @RequestBody(required = false) MovimientoRequest req) {
        MovimientoDto m = inventario.registrarMovimiento(codigo, req);
        return ResponseEntity.created(URI.create("/api/productos/" + m.codigoProducto() + "/movimientos"))
                .body(m);
    }
}
