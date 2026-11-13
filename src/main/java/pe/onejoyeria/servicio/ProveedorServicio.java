package pe.onejoyeria.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.onejoyeria.dto.ProductoDto;
import pe.onejoyeria.dto.ProveedorDto;
import pe.onejoyeria.dto.ProveedorRequest;
import pe.onejoyeria.excepcion.ApiException;
import pe.onejoyeria.modelo.Proveedor;
import pe.onejoyeria.repositorio.ProductoRepositorio;
import pe.onejoyeria.repositorio.ProveedorRepositorio;

import java.time.LocalDateTime;
import java.util.List;

/** Módulo de proveedores: RF-15 (registro) y RF-16 (búsqueda). */
@Service
@Transactional(readOnly = true)
public class ProveedorServicio {

    private final ProveedorRepositorio proveedores;
    private final ProductoRepositorio productos;

    public ProveedorServicio(ProveedorRepositorio proveedores, ProductoRepositorio productos) {
        this.proveedores = proveedores;
        this.productos = productos;
    }

    /** Busca por razón social, RUC o persona de contacto. */
    public List<ProveedorDto> listar(String busqueda) {
        String q = Mapeo.normalizar(busqueda);
        return proveedores.findByActivoTrueOrderByNombreAsc().stream()
                .filter(p -> q.isEmpty()
                        || Mapeo.contiene(p.getNombre(), q)
                        || Mapeo.contiene(p.getRuc(), q)
                        || Mapeo.contiene(p.getContacto(), q)
                        || Mapeo.contiene(p.getSuministra(), q))
                .map(p -> Mapeo.proveedor(p, null))
                .toList();
    }

    /** Detalle del proveedor con las piezas que suministra. */
    public ProveedorDto obtener(Long id) {
        Proveedor p = buscarActivo(id);
        List<ProductoDto> piezas = productos.findByProveedorIdAndActivoTrueOrderByNombreAsc(id)
                .stream().map(Mapeo::producto).toList();
        return Mapeo.proveedor(p, piezas);
    }

    @Transactional
    public ProveedorDto crear(ProveedorRequest req) {
        Proveedor p = new Proveedor();
        aplicar(p, req);
        p.setFechaRegistro(LocalDateTime.now());
        proveedores.save(p);
        return Mapeo.proveedor(p, List.of());
    }

    @Transactional
    public ProveedorDto actualizar(Long id, ProveedorRequest req) {
        Proveedor p = buscarActivo(id);
        aplicar(p, req);
        proveedores.save(p);
        return obtener(id);
    }

    /** Baja lógica: el proveedor deja de listarse. */
    @Transactional
    public void eliminar(Long id) {
        Proveedor p = buscarActivo(id);
        p.setActivo(false);
        proveedores.save(p);
    }

    private Proveedor buscarActivo(Long id) {
        return proveedores.findByIdAndActivoTrue(id)
                .orElseThrow(() -> ApiException.noEncontrado("No existe un proveedor activo con id " + id + "."));
    }

    private void aplicar(Proveedor p, ProveedorRequest req) {
        if (req == null) throw ApiException.invalido("El cuerpo de la petición es obligatorio.");

        String nombre = Mapeo.limpio(req.nombre());
        if (nombre == null) throw ApiException.invalido("La razón social del proveedor es obligatoria.");
        if (nombre.length() > 150) throw ApiException.invalido("La razón social no puede superar 150 caracteres.");

        String ruc = Mapeo.limpio(req.ruc());
        if (ruc != null) {
            if (!ruc.matches("\\d{11}"))
                throw ApiException.invalido("El RUC debe tener exactamente 11 dígitos.");
            proveedores.findFirstByRucAndActivoTrue(ruc)
                    .filter(otro -> !otro.getId().equals(p.getId()))
                    .ifPresent(otro -> {
                        throw ApiException.conflicto("El RUC " + ruc + " ya está registrado para " + otro.getNombre() + ".");
                    });
        }

        String email = Mapeo.limpio(req.email());
        if (email != null && !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))
            throw ApiException.invalido("El correo electrónico no es válido.");

        String telefono = Mapeo.limpio(req.telefono());
        if (telefono != null && !telefono.matches("[0-9+()\\s-]{6,30}"))
            throw ApiException.invalido("El teléfono sólo admite números, espacios, +, ( ) y guiones.");

        p.setNombre(nombre);
        p.setRuc(ruc);
        p.setContacto(Mapeo.limpio(req.contacto()));
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setDireccion(Mapeo.limpio(req.direccion()));
        p.setSuministra(Mapeo.limpio(req.suministra()));
        p.setObservaciones(Mapeo.limpio(req.observaciones()));
    }
}
