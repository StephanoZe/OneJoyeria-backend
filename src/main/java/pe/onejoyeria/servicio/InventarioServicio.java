package pe.onejoyeria.servicio;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.onejoyeria.dto.*;
import pe.onejoyeria.excepcion.ApiException;
import pe.onejoyeria.modelo.*;
import pe.onejoyeria.repositorio.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

/** Lógica del inventario: catálogo, piezas, movimientos de stock y panel. */
@Service
@Transactional(readOnly = true)
public class InventarioServicio {

    /** Prefijo de los códigos que genera el sistema automáticamente. */
    public static final String PREFIJO_CODIGO = "JOY-";
    private static final Locale ES_PE = Locale.forLanguageTag("es-PE");
    private static final int CANTIDAD_MAXIMA = 9999;

    private final ProductoRepositorio productos;
    private final CategoriaRepositorio categorias;
    private final MaterialRepositorio materiales;
    private final ProveedorRepositorio proveedores;
    private final MovimientoRepositorio movimientos;

    public InventarioServicio(ProductoRepositorio productos, CategoriaRepositorio categorias,
                              MaterialRepositorio materiales, ProveedorRepositorio proveedores,
                              MovimientoRepositorio movimientos) {
        this.productos = productos;
        this.categorias = categorias;
        this.materiales = materiales;
        this.proveedores = proveedores;
        this.movimientos = movimientos;
    }

    // ═════════════════════════════ CATÁLOGO ═════════════════════════════

    /** RF-01, RF-07, RF-08, RF-09: catálogo con búsqueda y filtros. */
    public List<ProductoDto> listar(String busqueda, Long categoriaId, String estado) {
        String q = Mapeo.normalizar(busqueda);
        String est = Mapeo.normalizar(estado);

        return productos.findByActivoTrueOrderByNombreAsc().stream()
                .filter(p -> q.isEmpty()
                        || Mapeo.contiene(p.getNombre(), q)
                        || Mapeo.contiene(p.getCodigo(), q))
                .filter(p -> categoriaId == null || p.getCategoria().getId().equals(categoriaId))
                .filter(p -> switch (est) {
                    case "agotado" -> p.estaAgotado();
                    case "bajo" -> p.tieneStockBajo();
                    case "ok", "disponible" -> !p.estaAgotado() && !p.tieneStockBajo();
                    default -> true;
                })
                .sorted(Comparator.comparingInt((Producto p) -> p.getCategoria().getOrden())
                        .thenComparing(Producto::getNombre))
                .map(Mapeo::producto)
                .toList();
    }

    public ProductoDto obtener(String codigo) {
        return Mapeo.producto(buscarActivo(codigo));
    }

    /** RF-13: piezas en o por debajo de su stock mínimo, de la más crítica a la menos. */
    public List<ProductoDto> alertas() {
        return productos.findByActivoTrueOrderByNombreAsc().stream()
                .filter(p -> p.getStockActual() <= p.getStockMinimo())
                .sorted(Comparator.comparingInt(Producto::getStockActual)
                        .thenComparingInt(p -> p.getStockActual() - p.getStockMinimo()))
                .map(Mapeo::producto)
                .toList();
    }

    public List<OpcionDto> categorias() {
        return categorias.findByActivoTrueOrderByOrdenAsc().stream()
                .map(c -> new OpcionDto(c.getId(), c.getNombre(), c.getDescripcion(), null))
                .toList();
    }

    public List<OpcionDto> materiales() {
        return materiales.findByActivoTrueOrderByNombreAsc().stream()
                .map(m -> new OpcionDto(m.getId(), m.getNombre(), m.getDescripcion(), m.getColor()))
                .toList();
    }

    // ═════════════════════════════ CRUD DE PIEZAS ═════════════════════════════

    /** RF-02: registra una pieza nueva. Si trae stock inicial, se audita como entrada. */
    @Transactional
    public ProductoDto crear(ProductoRequest req, String responsable) {
        if (req == null) throw ApiException.invalido("El cuerpo de la petición es obligatorio.");

        String codigo = Mapeo.limpio(req.codigo());
        if (codigo == null) {
            codigo = generarCodigo();
        } else {
            codigo = validarCodigo(codigo);
            if (productos.existsByCodigo(codigo))
                throw ApiException.conflicto("Ya existe una pieza con el código " + codigo + ".");
        }

        int stockInicial = req.stockActual() == null ? 0 : req.stockActual();
        if (stockInicial < 0) throw ApiException.invalido("El stock inicial no puede ser negativo.");
        if (stockInicial > CANTIDAD_MAXIMA)
            throw ApiException.invalido("El stock inicial no puede superar " + CANTIDAD_MAXIMA + " unidades.");

        Producto p = new Producto();
        p.setCodigo(codigo);
        aplicarDatos(p, req);
        p.setStockActual(stockInicial);
        p.setFechaRegistro(LocalDateTime.now());
        productos.save(p);

        if (stockInicial > 0) {
            guardarMovimiento(p, MovimientoStock.ENTRADA, stockInicial, 0, stockInicial,
                    "Stock inicial al registrar la pieza", responsable);
        }
        return Mapeo.producto(p);
    }

    /** RF-03: edita los datos de una pieza. El stock sólo cambia con movimientos. */
    @Transactional
    public ProductoDto actualizar(String codigoActual, ProductoRequest req) {
        if (req == null) throw ApiException.invalido("El cuerpo de la petición es obligatorio.");
        Producto p = buscarActivo(codigoActual);

        String nuevoCodigo = Mapeo.limpio(req.codigo());
        if (nuevoCodigo != null) {
            nuevoCodigo = validarCodigo(nuevoCodigo);
            if (!nuevoCodigo.equals(p.getCodigo()) && productos.existsByCodigo(nuevoCodigo))
                throw ApiException.conflicto("Ya existe una pieza con el código " + nuevoCodigo + ".");
            p.setCodigo(nuevoCodigo);
        }
        aplicarDatos(p, req);
        productos.save(p);
        return Mapeo.producto(p);
    }

    /** RF-04: baja lógica. La pieza deja de verse pero su historial se conserva. */
    @Transactional
    public void eliminar(String codigo) {
        Producto p = buscarActivo(codigo);
        p.setActivo(false);
        productos.save(p);
    }

    // ═════════════════════════════ MOVIMIENTOS ═════════════════════════════

    /** RF-12: historial de la pieza, del más reciente al más antiguo. */
    public List<MovimientoDto> historial(String codigo) {
        Producto p = buscarActivo(codigo);
        return movimientos.findByProductoIdOrderByFechaMovimientoDescIdDesc(p.getId()).stream()
                .map(Mapeo::movimiento)
                .toList();
    }

    /**
     * RF-10 / RF-11: registra una entrada o salida.
     * RN-06: una salida nunca deja el stock en negativo; si se pide más de lo que hay,
     *        se rechaza y no se toca nada.
     * RN-07: cada movimiento queda auditado con stock anterior y nuevo.
     */
    @Transactional
    public MovimientoDto registrarMovimiento(String codigo, MovimientoRequest req) {
        if (req == null) throw ApiException.invalido("El cuerpo de la petición es obligatorio.");

        String tipo = Mapeo.normalizar(req.tipoMovimiento());
        boolean esEntrada = tipo.equals("entrada");
        if (!esEntrada && !tipo.equals("salida"))
            throw ApiException.invalido("El tipo de movimiento debe ser Entrada o Salida.");

        if (req.cantidad() == null || req.cantidad() < 1)
            throw ApiException.invalido("La cantidad debe ser mayor a 0.");
        int cantidad = req.cantidad();
        if (cantidad > CANTIDAD_MAXIMA)
            throw ApiException.invalido("La cantidad no puede superar " + CANTIDAD_MAXIMA + " unidades.");

        Producto p = buscarActivo(codigo);
        int anterior = p.getStockActual();

        if (!esEntrada && anterior == 0)
            throw ApiException.invalido("La pieza " + p.getCodigo()
                    + " está agotada: no hay unidades para registrar una salida.");
        if (!esEntrada && cantidad > anterior)
            throw ApiException.invalido("Stock insuficiente: sólo hay " + anterior
                    + " unidades de " + p.getCodigo() + ".");

        int nuevo = esEntrada ? anterior + cantidad : anterior - cantidad;
        p.setStockActual(nuevo);
        productos.save(p);

        MovimientoStock m = guardarMovimiento(p,
                esEntrada ? MovimientoStock.ENTRADA : MovimientoStock.SALIDA,
                cantidad, anterior, nuevo,
                Mapeo.limpio(req.observaciones()), Mapeo.limpio(req.responsable()));
        return Mapeo.movimiento(m);
    }

    /** Movimientos con filtros para reportes. Las fechas incluyen el día completo de "hasta". */
    public List<MovimientoDto> movimientos(LocalDate desde, LocalDate hasta, Long categoriaId,
                                           String tipo, Integer limite) {
        List<MovimientoStock> lista = filtrarMovimientos(desde, hasta, categoriaId, tipo);
        var flujo = lista.stream().map(Mapeo::movimiento);
        if (limite != null && limite > 0) flujo = flujo.limit(limite);
        return flujo.toList();
    }

    /** Versión con entidades, usada también por la exportación a Excel. */
    public List<MovimientoStock> filtrarMovimientos(LocalDate desde, LocalDate hasta,
                                                    Long categoriaId, String tipo) {
        if (desde != null && hasta != null && hasta.isBefore(desde))
            throw ApiException.invalido("La fecha 'hasta' no puede ser anterior a 'desde'.");

        LocalDateTime ini = desde == null ? null : desde.atStartOfDay();
        LocalDateTime fin = hasta == null ? null : hasta.plusDays(1).atStartOfDay();
        String t = Mapeo.normalizar(tipo);

        return movimientos.findAllByOrderByFechaMovimientoDescIdDesc().stream()
                .filter(m -> ini == null || !m.getFechaMovimiento().isBefore(ini))
                .filter(m -> fin == null || m.getFechaMovimiento().isBefore(fin))
                .filter(m -> categoriaId == null
                        || m.getProducto().getCategoria().getId().equals(categoriaId))
                .filter(m -> t.isEmpty() || Mapeo.normalizar(m.getTipoMovimiento()).equals(t))
                .toList();
    }

    // ═════════════════════════════ PANEL ═════════════════════════════

    /** RF-14: indicadores del panel. */
    public ResumenDto resumen() {
        List<Producto> activos = productos.findByActivoTrueOrderByNombreAsc();
        List<DiaMovimientoDto> semana = semana();
        return new ResumenDto(
                activos.size(),
                activos.stream().map(Producto::valorTotal).reduce(BigDecimal.ZERO, BigDecimal::add),
                (int) activos.stream().filter(Producto::tieneStockBajo).count(),
                (int) activos.stream().filter(Producto::estaAgotado).count(),
                activos.stream().mapToInt(Producto::getStockActual).sum(),
                semana.stream().mapToInt(DiaMovimientoDto::entradas).sum(),
                semana.stream().mapToInt(DiaMovimientoDto::salidas).sum());
    }

    public PanelDto panel() {
        List<MovimientoDto> recientes = movimientos.findTop10ByOrderByFechaMovimientoDescIdDesc()
                .stream().map(Mapeo::movimiento).toList();
        return new PanelDto(resumen(), resumenPorCategoria(null), semana(), recientes, alertas());
    }

    /** Agrupa las piezas activas por tipo de joya, en el orden de la vitrina. */
    public List<CategoriaResumenDto> resumenPorCategoria(Long soloCategoriaId) {
        Map<Long, List<Producto>> porCategoria = productos.findByActivoTrueOrderByNombreAsc().stream()
                .collect(Collectors.groupingBy(p -> p.getCategoria().getId()));

        List<CategoriaResumenDto> filas = new ArrayList<>();
        for (Categoria c : categorias.findByActivoTrueOrderByOrdenAsc()) {
            if (soloCategoriaId != null && !c.getId().equals(soloCategoriaId)) continue;
            List<Producto> lista = porCategoria.getOrDefault(c.getId(), List.of());
            filas.add(new CategoriaResumenDto(
                    c.getId(),
                    c.getNombre(),
                    lista.size(),
                    lista.stream().mapToInt(Producto::getStockActual).sum(),
                    lista.stream().map(Producto::valorTotal).reduce(BigDecimal.ZERO, BigDecimal::add),
                    (int) lista.stream().filter(Producto::estaAgotado).count(),
                    (int) lista.stream().filter(Producto::tieneStockBajo).count()));
        }
        return filas;
    }

    /** Entradas y salidas de los últimos 7 días (incluido hoy). */
    public List<DiaMovimientoDto> semana() {
        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.minusDays(6);
        List<MovimientoStock> lista = movimientos.findByFechaMovimientoGreaterThanEqual(desde.atStartOfDay());

        List<DiaMovimientoDto> dias = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate fecha = desde.plusDays(i);
            int entradas = 0, salidas = 0;
            for (MovimientoStock m : lista) {
                if (!m.getFechaMovimiento().toLocalDate().equals(fecha)) continue;
                if (MovimientoStock.ENTRADA.equals(m.getTipoMovimiento())) entradas += m.getCantidad();
                else salidas += m.getCantidad();
            }
            String dia = fecha.getDayOfWeek().getDisplayName(TextStyle.SHORT, ES_PE).replace(".", "");
            dia = dia.substring(0, 1).toUpperCase(ES_PE) + dia.substring(1);
            dias.add(new DiaMovimientoDto(fecha, dia, entradas, salidas));
        }
        return dias;
    }

    // ═════════════════════════════ AUXILIARES ═════════════════════════════

    public Producto buscarActivo(String codigo) {
        String c = codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
        return productos.findByCodigoAndActivoTrue(c)
                .orElseThrow(() -> ApiException.noEncontrado(
                        "No existe una pieza activa con el código " + c + "."));
    }

    private void aplicarDatos(Producto p, ProductoRequest req) {
        String nombre = Mapeo.limpio(req.nombre());
        if (nombre == null) throw ApiException.invalido("El nombre de la pieza es obligatorio.");
        if (nombre.length() > 200) throw ApiException.invalido("El nombre no puede superar 200 caracteres.");

        String descripcion = Mapeo.limpio(req.descripcion());
        if (descripcion != null && descripcion.length() > 500)
            throw ApiException.invalido("La descripción no puede superar 500 caracteres.");

        if (req.precio() == null || req.precio().compareTo(BigDecimal.ZERO) <= 0)
            throw ApiException.invalido("El precio debe ser mayor a 0.");

        int minimo = req.stockMinimo() == null ? 3 : req.stockMinimo();
        if (minimo < 0) throw ApiException.invalido("El stock mínimo no puede ser negativo.");

        if (req.categoriaId() == null) throw ApiException.invalido("Selecciona la categoría de la pieza.");
        Categoria categoria = categorias.findById(req.categoriaId())
                .filter(Categoria::isActivo)
                .orElseThrow(() -> ApiException.invalido("La categoría " + req.categoriaId() + " no existe."));

        if (req.materialId() == null) throw ApiException.invalido("Selecciona el material de la pieza.");
        Material material = materiales.findById(req.materialId())
                .filter(Material::isActivo)
                .orElseThrow(() -> ApiException.invalido("El material " + req.materialId() + " no existe."));

        Proveedor proveedor = null;
        if (req.proveedorId() != null) {
            proveedor = proveedores.findByIdAndActivoTrue(req.proveedorId())
                    .orElseThrow(() -> ApiException.invalido("El proveedor " + req.proveedorId() + " no existe."));
        }

        p.setNombre(nombre);
        p.setDescripcion(descripcion);
        p.setPrecio(req.precio().setScale(2, java.math.RoundingMode.HALF_UP));
        p.setStockMinimo(minimo);
        p.setCategoria(categoria);
        p.setMaterial(material);
        p.setProveedor(proveedor);
    }

    private String validarCodigo(String codigo) {
        String c = codigo.trim().toUpperCase(Locale.ROOT);
        if (c.length() > 30) throw ApiException.invalido("El código no puede superar 30 caracteres.");
        if (!c.matches("[A-Z0-9-]+"))
            throw ApiException.invalido("El código sólo admite letras, números y guiones.");
        return c;
    }

    private String generarCodigo() {
        long n = productos.count() + 1;
        String codigo;
        do {
            codigo = PREFIJO_CODIGO + String.format("%03d", n++);
        } while (productos.existsByCodigo(codigo));
        return codigo;
    }

    private MovimientoStock guardarMovimiento(Producto p, String tipo, int cantidad, int anterior,
                                              int nuevo, String observaciones, String responsable) {
        MovimientoStock m = new MovimientoStock();
        m.setProducto(p);
        m.setTipoMovimiento(tipo);
        m.setCantidad(cantidad);
        m.setStockAnterior(anterior);
        m.setStockNuevo(nuevo);
        m.setObservaciones(observaciones);
        m.setResponsable(responsable);
        m.setFechaMovimiento(LocalDateTime.now());
        return movimientos.save(m);
    }
}
