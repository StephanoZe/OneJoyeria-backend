package pe.onejoyeria.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.onejoyeria.modelo.Proveedor;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepositorio extends JpaRepository<Proveedor, Long> {
    List<Proveedor> findByActivoTrueOrderByNombreAsc();
    Optional<Proveedor> findByIdAndActivoTrue(Long id);
    Optional<Proveedor> findFirstByRucAndActivoTrue(String ruc);
}
