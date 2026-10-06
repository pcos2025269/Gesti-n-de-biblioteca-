package pablocos.gestor_biblioteca.kinal.dto;

import jakarta.validation.constraints.NotNull;

public record PrestamoRequest(
        @NotNull Long usuarioId,
        @NotNull Long libroId
) {
}
