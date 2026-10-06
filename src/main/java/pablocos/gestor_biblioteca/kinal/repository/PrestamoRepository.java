package pablocos.gestor_biblioteca.kinal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pablocos.gestor_biblioteca.kinal.entity.Prestamo;
import pablocos.gestor_biblioteca.kinal.entity.Usuario;
import pablocos.gestor_biblioteca.kinal.entity.EstadoPrestamo;

import java.util.List;

@Repository
public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {
    List<Prestamo> findByUsuarioAndEstado(Usuario usuario, EstadoPrestamo estado);
    List<Prestamo> findByUsuario(Usuario usuario);
}
