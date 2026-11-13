package pe.onejoyeria.modelo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/** Registro auditable de cada entrada o salida de stock (RN-07). */
@Entity
@Table(name = "movimientos_stock")
public class MovimientoStock {

    public static final String ENTRADA = "Entrada";
    public static final String SALIDA = "Salida";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    /** "Entrada" o "Salida". */
    @Column(nullable = false, length = 10)
    private String tipoMovimiento;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false)
    private int stockAnterior;

    @Column(nullable = false)
    private int stockNuevo;

    @Column(length = 500)
    private String observaciones;

    @Column(length = 150)
    private String responsable;

    @Column(nullable = false)
    private LocalDateTime fechaMovimiento = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public String getTipoMovimiento() { return tipoMovimiento; }
    public void setTipoMovimiento(String tipoMovimiento) { this.tipoMovimiento = tipoMovimiento; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public int getStockAnterior() { return stockAnterior; }
    public void setStockAnterior(int stockAnterior) { this.stockAnterior = stockAnterior; }
    public int getStockNuevo() { return stockNuevo; }
    public void setStockNuevo(int stockNuevo) { this.stockNuevo = stockNuevo; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }
    public LocalDateTime getFechaMovimiento() { return fechaMovimiento; }
    public void setFechaMovimiento(LocalDateTime fechaMovimiento) { this.fechaMovimiento = fechaMovimiento; }
}
