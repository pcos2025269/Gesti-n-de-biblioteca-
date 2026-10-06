package pablocos.gestor_biblioteca.kinal.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pablocos.gestor_biblioteca.kinal.entity.Libro;

import java.util.Optional;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT l FROM Libro l WHERE l.id = :id")
    Optional<Libro> findByIdWithLock(@Param("id") Long id);
}
