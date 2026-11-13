package pe.onejoyeria.controlador;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.onejoyeria.dto.ProveedorDto;
import pe.onejoyeria.dto.ProveedorRequest;
import pe.onejoyeria.servicio.ProveedorServicio;

import java.net.URI;
import java.util.List;

/** API REST de proveedores. */
@RestController
@RequestMapping("/api/proveedores")
public class ProveedorControlador {

    private final ProveedorServicio servicio;

    public ProveedorControlador(ProveedorServicio servicio) {
        this.servicio = servicio;
    }

    // GET /api/proveedores?busqueda=
    @GetMapping
    public List<ProveedorDto> listar(@RequestParam(required = false) String busqueda) {
        return servicio.listar(busqueda);
    }

    // GET /api/proveedores/{id}
    @GetMapping("/{id}")
    public ProveedorDto obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    // POST /api/proveedores
    @PostMapping
    public ResponseEntity<ProveedorDto> crear(@RequestBody(required = false) ProveedorRequest req) {
        ProveedorDto creado = servicio.crear(req);
        return ResponseEntity.created(URI.create("/api/proveedores/" + creado.id())).body(creado);
    }

    // PUT /api/proveedores/{id}
    @PutMapping("/{id}")
    public ProveedorDto actualizar(@PathVariable Long id, @RequestBody(required = false) ProveedorRequest req) {
        return servicio.actualizar(id, req);
    }

    // DELETE /api/proveedores/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
