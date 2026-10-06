package pablocos.gestor_biblioteca.kinal.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PrestamoResponseDto {
    private Long id;
    private Long usuarioId;
    private Long libroId;
    private LocalDateTime fechaPrestamo;
    private LocalDateTime fechaDevolucionPrevista;
    private LocalDateTime fechaDevolucionReal;
    private String estado;
}
