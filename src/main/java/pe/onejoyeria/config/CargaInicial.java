package pe.onejoyeria.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.onejoyeria.modelo.*;
import pe.onejoyeria.repositorio.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Datos de ejemplo. Se insertan sólo la primera vez (cuando la base está vacía),
 * para que el panel, las alertas y los reportes tengan información desde el inicio.
 * Si quieres empezar de cero, borra la carpeta "data" del proyecto.
 */
@Component
public class CargaInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(CargaInicial.class);

    private final CategoriaRepositorio categorias;
    private final MaterialRepositorio materiales;
    private final ProveedorRepositorio proveedores;
    private final ProductoRepositorio productos;
    private final MovimientoRepositorio movimientos;

    public CargaInicial(CategoriaRepositorio categorias, MaterialRepositorio materiales,
                        ProveedorRepositorio proveedores, ProductoRepositorio productos,
                        MovimientoRepositorio movimientos) {
        this.categorias = categorias;
        this.materiales = materiales;
        this.proveedores = proveedores;
        this.productos = productos;
        this.movimientos = movimientos;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (categorias.count() > 0) return;
        log.info("Base de datos vacía: cargando datos de ejemplo de One Joyería...");

        // ── Categorías (tipos de joya) ──
        Map<String, Categoria> cat = new HashMap<>();
        String[][] datosCat = {
                {"Anillos", "Solitarios, alianzas y anillos de moda"},
                {"Aretes", "Aretes, argollas y pendientes"},
                {"Collares", "Collares, cadenas y gargantillas"},
                {"Pulseras", "Pulseras, esclavas y brazaletes"},
                {"Broches", "Broches y prendedores decorativos"}};
        for (int i = 0; i < datosCat.length; i++) {
            Categoria c = categorias.save(new Categoria(datosCat[i][0], datosCat[i][1], i + 1));
            cat.put(c.getNombre(), c);
        }

        // ── Materiales, con el color de su "pastilla" en la página ──
        Map<String, Material> mat = new HashMap<>();
        String[][] datosMat = {
                {"Oro 18k", "Oro amarillo de 18 kilates", "#C9A13B"},
                {"Oro 14k", "Oro amarillo de 14 kilates", "#D8BD6A"},
                {"Plata 925", "Plata esterlina 925", "#B9C0C4"},
                {"Acero quirúrgico", "Acero inoxidable hipoalergénico", "#7E8A91"},
                {"Oro rosa 18k", "Oro rosa de 18 kilates", "#D29A86"}};
        for (String[] m : datosMat) {
            Material x = materiales.save(new Material(m[0], m[1], m[2]));
            mat.put(x.getNombre(), x);
        }

        // ── Proveedores ──
        Proveedor sur = proveedor("Joyas del Sur SAC", "20601234567", "Miguel Torres", "987 654 321",
                "ventas@joyasdelsur.pe", "Jr. Ucayali 452, Cercado de Lima", "Oro 18k y oro 14k", 120);
        Proveedor andina = proveedor("Platería Andina EIRL", "20456789123", "Rosa Quispe", "976 543 210",
                "contacto@plateriaandina.pe", "Av. Grau 1180, Barranco", "Plata 925 y perlas", 95);
        Proveedor acero = proveedor("Acero & Diseño SAC", "20789456123", "Jorge Injoque", "965 432 109",
                "info@aceroydiseno.pe", "Av. Argentina 2200, Callao", "Acero quirúrgico", 60);
        Proveedor rose = proveedor("Orfebrería Rosé SAC", "20512398745", "Lucía Paredes", "954 321 876",
                "pedidos@orfebreriarose.pe", "Calle Las Begonias 310, San Isidro", "Oro rosa 18k", 30);
        Map<String, Proveedor> provPorMaterial = Map.of(
                "Oro 18k", sur, "Oro 14k", sur, "Plata 925", andina,
                "Acero quirúrgico", acero, "Oro rosa 18k", rose);

        // ── Piezas: código, nombre, descripción, categoría, material, precio, stock, mínimo, días ──
        Object[][] piezas = {
                {"JOY-001", "Anillo solitario oro 18k", "Solitario clásico con circonia central.", "Anillos", "Oro 18k", 1850, 8, 3, 70},
                {"JOY-002", "Alianza clásica", "Alianza matrimonial de media caña.", "Anillos", "Oro 14k", 950, 2, 3, 65},
                {"JOY-003", "Anillo de compromiso diamante", "Diamante central de 0.25 ct.", "Anillos", "Oro 18k", 3200, 0, 2, 50},
                {"JOY-004", "Anillo vintage grabado", "Diseño vintage grabado a mano.", "Anillos", "Plata 925", 280, 15, 4, 40},
                {"JOY-005", "Anillo eternity", "Banda eternity con circonias.", "Anillos", "Plata 925", 220, 18, 5, 30},
                {"JOY-006", "Aretes de perla", "Perla cultivada montada en plata.", "Aretes", "Plata 925", 180, 20, 5, 68},
                {"JOY-007", "Argollas medianas", "Argollas de 2 cm en oro 18k.", "Aretes", "Oro 18k", 620, 1, 3, 55},
                {"JOY-008", "Aretes hipoalergénicos", "Ideales para piel sensible.", "Aretes", "Acero quirúrgico", 65, 30, 8, 45},
                {"JOY-009", "Aretes corazón", "Corazón calado en oro rosa.", "Aretes", "Oro rosa 18k", 540, 0, 3, 33},
                {"JOY-010", "Cadena cubana 45 cm", "Cadena cubana maciza.", "Collares", "Oro 18k", 2100, 5, 3, 80},
                {"JOY-011", "Collar de perlas", "Una hilera de perlas cultivadas.", "Collares", "Plata 925", 390, 3, 4, 62},
                {"JOY-012", "Gargantilla ajustable", "Gargantilla fina con dije de gota.", "Collares", "Oro rosa 18k", 780, 12, 3, 28},
                {"JOY-013", "Choker minimalista", "Choker liso de uso diario.", "Collares", "Acero quirúrgico", 75, 40, 10, 20},
                {"JOY-014", "Pulsera tenis", "Pulsera tenis con circonias.", "Pulseras", "Oro 18k", 1650, 4, 3, 58},
                {"JOY-015", "Pulsera de dijes", "Dijes intercambiables.", "Pulseras", "Plata 925", 210, 0, 4, 42},
                {"JOY-016", "Esclava deportiva", "Diseño resistente para uso diario.", "Pulseras", "Acero quirúrgico", 90, 25, 6, 25},
                {"JOY-017", "Pulsera tejida fina", "Cadena fina tejida a mano.", "Pulseras", "Oro rosa 18k", 690, 6, 3, 15},
                {"JOY-018", "Broche flor", "Broche artesanal con motivo floral.", "Broches", "Plata 925", 150, 9, 3, 38},
                {"JOY-019", "Broche perla vintage", "Perla central con filigrana.", "Broches", "Oro 14k", 430, 2, 3, 22},
                {"JOY-020", "Broche mariposa", "Mariposa calada en oro rosa.", "Broches", "Oro rosa 18k", 350, 7, 3, 10}};

        Map<String, Producto> prod = new HashMap<>();
        for (Object[] d : piezas) {
            Producto p = new Producto();
            p.setCodigo((String) d[0]);
            p.setNombre((String) d[1]);
            p.setDescripcion((String) d[2]);
            p.setCategoria(cat.get((String) d[3]));
            p.setMaterial(mat.get((String) d[4]));
            p.setProveedor(provPorMaterial.get((String) d[4]));
            p.setPrecio(BigDecimal.valueOf((Integer) d[5]).setScale(2));
            p.setStockActual((Integer) d[6]);
            p.setStockMinimo((Integer) d[7]);
            p.setFechaRegistro(LocalDateTime.now().minusDays((Integer) d[8]));
            prod.put(p.getCodigo(), productos.save(p));
        }

        // ── Movimientos de los últimos 7 días (código, tipo, cantidad, hace N días, hora, nota) ──
        Object[][] movs = {
                {"JOY-001", "Entrada", 5, 6, 10, "Reposición desde proveedor"},
                {"JOY-006", "Salida", 2, 6, 16, "Venta en tienda"},
                {"JOY-014", "Entrada", 2, 6, 17, "Reposición desde proveedor"},
                {"JOY-002", "Salida", 3, 5, 11, "Venta en tienda"},
                {"JOY-011", "Salida", 1, 5, 15, "Venta en tienda"},
                {"JOY-018", "Entrada", 4, 5, 18, "Reposición desde proveedor"},
                {"JOY-003", "Salida", 2, 4, 12, "Venta: se agotó el modelo"},
                {"JOY-009", "Salida", 3, 4, 13, "Venta: se agotó el modelo"},
                {"JOY-016", "Entrada", 8, 4, 17, "Reposición desde proveedor"},
                {"JOY-004", "Entrada", 5, 3, 10, "Reposición desde proveedor"},
                {"JOY-015", "Salida", 2, 3, 16, "Venta: se agotó el modelo"},
                {"JOY-020", "Entrada", 3, 3, 18, "Reposición desde proveedor"},
                {"JOY-005", "Salida", 4, 2, 11, "Venta en tienda"},
                {"JOY-007", "Salida", 1, 2, 14, "Venta en tienda"},
                {"JOY-012", "Entrada", 4, 2, 17, "Reposición desde proveedor"},
                {"JOY-010", "Salida", 2, 1, 12, "Venta en tienda"},
                {"JOY-017", "Entrada", 3, 1, 15, "Reposición desde proveedor"},
                {"JOY-019", "Salida", 1, 1, 19, "Venta en tienda"},
                {"JOY-008", "Salida", 3, 0, 9, "Venta en tienda"},
                {"JOY-013", "Entrada", 5, 0, 9, "Reposición desde proveedor"}};

        // Se reconstruye el stock hacia atrás para que cada asiento cuadre con el stock final.
        Map<String, Integer> stock = new HashMap<>();
        for (Object[] m : movs) {
            String codigo = (String) m[0];
            int cantidad = (Integer) m[2];
            int signo = "Entrada".equals(m[1]) ? 1 : -1;
            stock.merge(codigo, prod.get(codigo).getStockActual() - signo * cantidad,
                    (actual, ignorado) -> actual - signo * cantidad);
        }

        String[] responsables = {"Rosa Fernández", "Claudia Espinoza"};
        int i = 0;
        for (Object[] m : movs) {
            String codigo = (String) m[0];
            String tipo = (String) m[1];
            int cantidad = (Integer) m[2];
            int anterior = stock.get(codigo);
            int nuevo = "Entrada".equals(tipo) ? anterior + cantidad : anterior - cantidad;
            stock.put(codigo, nuevo);

            LocalDateTime fecha = LocalDate.now().minusDays((Integer) m[3]).atTime((Integer) m[4], 15 + (i * 7) % 40);
            if (fecha.isAfter(LocalDateTime.now())) fecha = LocalDateTime.now().minusMinutes(30L - i);

            MovimientoStock mov = new MovimientoStock();
            mov.setProducto(prod.get(codigo));
            mov.setTipoMovimiento(tipo);
            mov.setCantidad(cantidad);
            mov.setStockAnterior(anterior);
            mov.setStockNuevo(nuevo);
            mov.setObservaciones((String) m[5]);
            mov.setResponsable(responsables[i % 2]);
            mov.setFechaMovimiento(fecha);
            movimientos.save(mov);
            i++;
        }

        log.info("Datos de ejemplo cargados: {} piezas, {} proveedores, {} movimientos.",
                productos.count(), proveedores.count(), movimientos.count());
    }

    private Proveedor proveedor(String nombre, String ruc, String contacto, String telefono, String email,
                                String direccion, String suministra, int diasAtras) {
        Proveedor p = new Proveedor();
        p.setNombre(nombre);
        p.setRuc(ruc);
        p.setContacto(contacto);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setDireccion(direccion);
        p.setSuministra(suministra);
        p.setFechaRegistro(LocalDateTime.now().minusDays(diasAtras));
        return proveedores.save(p);
    }
}
