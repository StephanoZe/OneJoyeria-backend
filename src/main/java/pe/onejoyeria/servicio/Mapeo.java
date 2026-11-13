package pe.onejoyeria.servicio;

import pe.onejoyeria.dto.MovimientoDto;
import pe.onejoyeria.dto.ProductoDto;
import pe.onejoyeria.dto.ProveedorDto;
import pe.onejoyeria.modelo.MovimientoStock;
import pe.onejoyeria.modelo.Producto;
import pe.onejoyeria.modelo.Proveedor;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

/** Conversión entidad → DTO y utilidades de texto compartidas por los servicios. */
public final class Mapeo {

    private Mapeo() { }

    public static ProductoDto producto(Producto p) {
        Proveedor prov = p.getProveedor();
        boolean provActivo = prov != null && prov.isActivo();
        return new ProductoDto(
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getDescripcion(),
                p.getCategoria().getId(),
                p.getCategoria().getNombre(),
                p.getMaterial().getId(),
                p.getMaterial().getNombre(),
                p.getMaterial().getColor(),
                provActivo ? prov.getId() : null,
                provActivo ? prov.getNombre() : null,
                p.getPrecio(),
                p.getStockActual(),
                p.getStockMinimo(),
                p.valorTotal(),
                p.estado(),
                p.getFechaRegistro());
    }

    public static MovimientoDto movimiento(MovimientoStock m) {
        Producto p = m.getProducto();
        return new MovimientoDto(
                m.getId(),
                p.getId(),
                p.getCodigo(),
                p.getNombre(),
                p.getCategoria().getNombre(),
                m.getTipoMovimiento(),
                m.getCantidad(),
                m.getStockAnterior(),
                m.getStockNuevo(),
                m.getObservaciones(),
                m.getResponsable(),
                m.getFechaMovimiento());
    }

    public static ProveedorDto proveedor(Proveedor p, List<ProductoDto> productos) {
        return new ProveedorDto(
                p.getId(), p.getNombre(), p.getRuc(), p.getContacto(), p.getTelefono(),
                p.getEmail(), p.getDireccion(), p.getSuministra(), p.getObservaciones(),
                p.getFechaRegistro(), productos);
    }

    /** Minúsculas y sin tildes, para que "solitario" encuentre "Solitário" y viceversa. */
    public static String normalizar(String texto) {
        if (texto == null) return "";
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase(Locale.ROOT).trim();
    }

    public static boolean contiene(String campo, String busquedaNormalizada) {
        return campo != null && normalizar(campo).contains(busquedaNormalizada);
    }

    /** Recorta espacios y convierte cadenas vacías en null. */
    public static String limpio(String texto) {
        if (texto == null) return null;
        String t = texto.trim();
        return t.isEmpty() ? null : t;
    }
}
