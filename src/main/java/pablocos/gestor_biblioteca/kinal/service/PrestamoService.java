package pablocos.gestor_biblioteca.kinal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablocos.gestor_biblioteca.kinal.dto.PrestamoRequestDto;
import pablocos.gestor_biblioteca.kinal.dto.PrestamoResponseDto;
import pablocos.gestor_biblioteca.kinal.entity.*;
import pablocos.gestor_biblioteca.kinal.exception.BusinessRuleException;
import pablocos.gestor_biblioteca.kinal.exception.ResourceNotFoundException;
import pablocos.gestor_biblioteca.kinal.repository.LibroRepository;
import pablocos.gestor_biblioteca.kinal.repository.PrestamoRepository;
import pablocos.gestor_biblioteca.kinal.repository.UsuarioRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrestamoService {

    private final PrestamoRepository prestamoRepository;
    private final LibroRepository libroRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public PrestamoResponseDto crearPrestamo(String userEmail, PrestamoRequestDto requestDto) {
        Usuario usuario = usuarioRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + userEmail));

        LocalDate hoy = LocalDate.now();

        // 1. Validar vencimientos y aplicar sanción automática
        List<Prestamo> prestamosActivos = prestamoRepository.findByUsuarioAndEstado(usuario, EstadoPrestamo.ACTIVO);
        for (Prestamo p : prestamosActivos) {
            // Nota: Asegúrate de que tu entidad Prestamo tenga el método getFechaDevolucionPrevista() o getFechaDevolucion()
            if (p.getFechaDevolucionPrevista().isBefore(hoy)) {
                try {
                    p.setEstado(EstadoPrestamo.valueOf("VENCIDO"));
                } catch (IllegalArgumentException e) {
                    // Si el enum no tiene VENCIDO, lo manejamos de forma segura
                }
                prestamoRepository.save(p);
                usuario.setEstado(EstadoUsuario.SANCIONADO);
                usuarioRepository.save(usuario);
            }
        }

        if (usuario.getEstado() == EstadoUsuario.SANCIONADO) {
            throw new BusinessRuleException("Usuario sancionado por tener préstamos vencidos.");
        }

        // 2. Validar límite máximo de 3 préstamos activos
        long activosCount = prestamosActivos.stream().filter(p -> p.getEstado() == EstadoPrestamo.ACTIVO).count();
        if (activosCount >= 3) {
            throw new BusinessRuleException("Límite máximo de 3 préstamos activos alcanzado.");
        }

        // 3. Bloqueo pesimista para concurrencia sobre el libro
        Libro libro = libroRepository.findByIdWithLock(requestDto.getLibroId())
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + requestDto.getLibroId()));

        // 4. Validar stock disponible
        if (libro.getStockDisponible() <= 0) {
            throw new BusinessRuleException("No hay stock disponible para este libro.");
        }

        // Descontar stock
        libro.setStockDisponible(libro.getStockDisponible() - 1);
        libroRepository.save(libro);

        // Crear préstamo (plazo 14 días usando LocalDate)
        Prestamo prestamo = Prestamo.builder()
                .usuario(usuario)
                .libro(libro)
                .fechaPrestamo(hoy)
                .fechaDevolucionPrevista(hoy.plusDays(14))
                .estado(EstadoPrestamo.ACTIVO)
                .build();

        Prestamo saved = prestamoRepository.save(prestamo);
        return mapToDto(saved);
    }

    @Transactional
    public PrestamoResponseDto registrarDevolucion(Long prestamoId) {
        Prestamo prestamo = prestamoRepository.findById(prestamoId)
                .orElseThrow(() -> new ResourceNotFoundException("Préstamo no encontrado con ID: " + prestamoId));

        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new BusinessRuleException("El préstamo ya fue devuelto previamente.");
        }

        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        prestamo.setFechaDevolucionReal(LocalDate.now());
        prestamoRepository.save(prestamo);

        // Devolver stock
        Libro libro = prestamo.getLibro();
        libro.setStockDisponible(libro.getStockDisponible() + 1);
        libroRepository.save(libro);

        return mapToDto(prestamo);
    }

    @Transactional(readOnly = true)
    public List<PrestamoResponseDto> obtenerPrestamos(String userEmail, String role) {
        if ("ADMIN".equals(role) || "BIBLIOTECARIO".equals(role)) {
            return prestamoRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
        } else {
            Usuario usuario = usuarioRepository.findByEmail(userEmail)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            return prestamoRepository.findByUsuario(usuario).stream().map(this::mapToDto).collect(Collectors.toList());
        }
    }

    private PrestamoResponseDto mapToDto(Prestamo p) {
        return PrestamoResponseDto.builder()
                .id(p.getId())
                .usuarioId(p.getUsuario().getId())
                .libroId(p.getLibro().getId())
                .fechaPrestamo(p.getFechaPrestamo() != null ? p.getFechaPrestamo().atStartOfDay() : null)
                .fechaDevolucionPrevista(p.getFechaDevolucionPrevista() != null ? p.getFechaDevolucionPrevista().atStartOfDay() : null)
                .fechaDevolucionReal(p.getFechaDevolucionReal() != null ? p.getFechaDevolucionReal().atStartOfDay() : null)
                .estado(p.getEstado() != null ? p.getEstado().name() : null)
                .build();
    }
}