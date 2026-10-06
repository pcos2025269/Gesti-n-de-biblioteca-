package pablocos.gestor_biblioteca.kinal.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record LibroRequest(
        @NotBlank @Size(max = 20) String isbn,
        @NotBlank @Size(max = 255) String titulo,
        @NotBlank @Size(max = 150) String autor,
        @NotBlank @Size(max = 100) String categoria,
        @NotNull @PositiveOrZero Integer stockTotal
) {
}
