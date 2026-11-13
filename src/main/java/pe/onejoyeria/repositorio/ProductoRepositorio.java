package pe.onejoyeria.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.onejoyeria.modelo.Producto;

import java.util.List;
import java.util.Optional;

public interface ProductoRepositorio extends JpaRepository<Producto, Long> {
    List<Producto> findByActivoTrueOrderByNombreAsc();
    Optional<Producto> findByCodigoAndActivoTrue(String codigo);
    Optional<Producto> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
    List<Producto> findByProveedorIdAndActivoTrueOrderByNombreAsc(Long proveedorId);
}
