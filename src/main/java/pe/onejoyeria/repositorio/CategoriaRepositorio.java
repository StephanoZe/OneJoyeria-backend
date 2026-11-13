package pe.onejoyeria.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.onejoyeria.modelo.Categoria;

import java.util.List;

public interface CategoriaRepositorio extends JpaRepository<Categoria, Long> {
    List<Categoria> findByActivoTrueOrderByOrdenAsc();
}
