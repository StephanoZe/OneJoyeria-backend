package pe.onejoyeria.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.onejoyeria.modelo.MovimientoStock;

import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoRepositorio extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findByProductoIdOrderByFechaMovimientoDescIdDesc(Long productoId);
    List<MovimientoStock> findTop10ByOrderByFechaMovimientoDescIdDesc();
    List<MovimientoStock> findByFechaMovimientoGreaterThanEqual(LocalDateTime desde);
    List<MovimientoStock> findAllByOrderByFechaMovimientoDescIdDesc();
}
