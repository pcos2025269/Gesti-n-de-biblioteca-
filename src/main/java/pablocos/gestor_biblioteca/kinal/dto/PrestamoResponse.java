package pablocos.gestor_biblioteca.kinal.dto;

import com.universidad.biblioteca.entity.EstadoPrestamo;

import java.time.LocalDate;

public record PrestamoResponse(
        Long id,
        Long usuarioId,
        Long libroId,
        LocalDate fechaPrestamo,
        LocalDate fechaDevolucionEsperada,
        LocalDate fechaDevolucionReal,
        EstadoPrestamo estado
) {
}
