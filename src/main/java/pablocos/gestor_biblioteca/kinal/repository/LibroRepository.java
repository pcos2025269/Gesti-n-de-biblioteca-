package pablocos.gestor_biblioteca.kinal.repository;

import pablocos.gestor_biblioteca.kinal.entity.Libro;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndIdNot(String isbn, Long id);

    /**
     * Listado paginado con filtros opcionales. La coincidencia por titulo es "contiene";
     * la sensibilidad a mayusculas depende de la collation de MySQL (por defecto, insensible).
     */
    @Query("""
            select l from Libro l
            where (:titulo is null or l.titulo like concat('%', :titulo, '%'))
              and (:categoria is null or l.categoria = :categoria)
            """)
    Page<Libro> buscar(@Param("titulo") String titulo,
                       @Param("categoria") String categoria,
                       Pageable pageable);

    /** Bloquea la fila del libro para que una actualizacion no se mezcle con prestamos concurrentes. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from Libro l where l.id = :id")
    Optional<Libro> findByIdForUpdate(@Param("id") Long id);

    /** Decremento atomico del stock: devuelve 1 si habia stock y 0 si no. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Libro l set l.stockDisponible = l.stockDisponible - 1 "
            + "where l.id = :id and l.stockDisponible > 0")
    int decrementarStock(@Param("id") Long id);

    /** Incremento atomico del stock, sin superar stockTotal. Devuelve filas afectadas. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Libro l set l.stockDisponible = l.stockDisponible + 1 "
            + "where l.id = :id and l.stockDisponible < l.stockTotal")
    int incrementarStock(@Param("id") Long id);
}
