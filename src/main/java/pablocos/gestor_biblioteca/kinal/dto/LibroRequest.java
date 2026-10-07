package pablocos.gestor_biblioteca.kinal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * stockDisponible es opcional: al crear, si no se envia, vale stockTotal;
 * al actualizar, si no se envia, se recalcula segun el cambio de stockTotal.
 * La coherencia stockDisponible <= stockTotal se valida en LibroService.
 */
public record LibroRequest(
        @NotBlank @Size(max = 20) String isbn,
        @NotBlank @Size(max = 255) String titulo,
        @NotBlank @Size(max = 150) String autor,
        @NotBlank @Size(max = 100) String categoria,
        @NotNull @PositiveOrZero Integer stockTotal,
        @PositiveOrZero Integer stockDisponible
) {
}
