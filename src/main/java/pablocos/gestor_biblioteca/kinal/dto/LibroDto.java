package pablocos.gestor_biblioteca.kinal.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LibroDto {
    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private Integer stockTotal;
    private Integer stockDisponible;
}
