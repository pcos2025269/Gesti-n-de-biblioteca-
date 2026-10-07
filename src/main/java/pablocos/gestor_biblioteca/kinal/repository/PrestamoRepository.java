package pablocos.gestor_biblioteca.kinal.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pablocos.gestor_biblioteca.kinal.entity.EstadoPrestamo;
import pablocos.gestor_biblioteca.kinal.entity.Prestamo;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

public interface PrestamoRepository extends JpaRepository<Prestamo, Long> {

    /** Cuenta prestamos del usuario en los estados indicados (limite de 3 activos). */
    long countByUsuarioIdAndEstadoIn(Long usuarioId, Collection<EstadoPrestamo> estados);

    /** Indica si el usuario tiene algun prestamo sin devolver cuya fecha esperada ya paso. */
    boolean existsByUsuarioIdAndEstadoInAndFechaDevolucionEsperadaBefore(
            Long usuarioId, Collection<EstadoPrestamo> estados, LocalDate fecha);

    /** Historial completo (activos y devueltos) del usuario, paginado. */
    Page<Prestamo> findByUsuarioId(Long usuarioId, Pageable pageable);

    /** Prestamos en los estados dados cuya fecha esperada es anterior a la fecha dada. */
    Page<Prestamo> findByEstadoInAndFechaDevolucionEsperadaBefore(
            Collection<EstadoPrestamo> estados, LocalDate fecha, Pageable pageable);

    boolean existsByLibroId(Long libroId);

    /** Id del usuario dueno del prestamo (para bloquear primero al usuario y evitar deadlocks). */
    @Query("select p.usuario.id from Prestamo p where p.id = :id")
    Optional<Long> findUsuarioIdById(@Param("id") Long id);

    /** Bloquea la fila del prestamo para evitar devoluciones dobles concurrentes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Prestamo p where p.id = :id")
    Optional<Prestamo> findByIdForUpdate(@Param("id") Long id);

    /** Marca como ATRASADO todos los prestamos ACTIVO vencidos. Devuelve filas afectadas. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Prestamo p set p.estado = :atrasado "
            + "where p.estado = :activo and p.fechaDevolucionEsperada < :hoy")
    int marcarAtrasados(@Param("hoy") LocalDate hoy,
                        @Param("activo") EstadoPrestamo activo,
                        @Param("atrasado") EstadoPrestamo atrasado);

    /** Igual que marcarAtrasados pero solo para un usuario (se usa al sancionar). */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Prestamo p set p.estado = :atrasado "
            + "where p.usuario.id = :usuarioId and p.estado = :activo and p.fechaDevolucionEsperada < :hoy")
    int marcarAtrasadosDeUsuario(@Param("usuarioId") Long usuarioId,
                                 @Param("hoy") LocalDate hoy,
                                 @Param("activo") EstadoPrestamo activo,
                                 @Param("atrasado") EstadoPrestamo atrasado);
}
