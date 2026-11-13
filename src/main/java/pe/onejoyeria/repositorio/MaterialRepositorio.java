package pe.onejoyeria.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.onejoyeria.modelo.Material;

import java.util.List;

public interface MaterialRepositorio extends JpaRepository<Material, Long> {
    List<Material> findByActivoTrueOrderByNombreAsc();
}
