package pablocos.gestor_biblioteca.kinal.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pablocos.gestor_biblioteca.kinal.entity.EstadoUsuario;
import pablocos.gestor_biblioteca.kinal.entity.Usuario;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Bloquea la fila del usuario (SELECT ... FOR UPDATE) para serializar
     * los prestamos y devoluciones concurrentes del mismo usuario. Debe usarse dentro de una transaccion.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from Usuario u where u.id = :id")
    Optional<Usuario> findByIdForUpdate(@Param("id") Long id);

    /** Cambia el estado del usuario con un UPDATE directo (evita problemas de entidades desconectadas). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Usuario u set u.estado = :estado where u.id = :id")
    int actualizarEstado(@Param("id") Long id, @Param("estado") EstadoUsuario estado);
}
