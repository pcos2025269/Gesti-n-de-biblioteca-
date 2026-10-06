package pablocos.gestor_biblioteca.kinal.dto;

import pablocos.gestor_biblioteca.kinal.entity.EstadoPrestamo;

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
