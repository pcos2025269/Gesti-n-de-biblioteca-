package pablocos.gestor_biblioteca.kinal.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/** Respuesta paginada con contrato estable (no se serializa PageImpl directamente). */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}
