package pablocos.gestor_biblioteca.kinal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pablocos.gestor_biblioteca.kinal.dto.PageResponse;
import pablocos.gestor_biblioteca.kinal.dto.PrestamoRequest;
import pablocos.gestor_biblioteca.kinal.dto.PrestamoResponse;
import pablocos.gestor_biblioteca.kinal.entity.EstadoPrestamo;
import pablocos.gestor_biblioteca.kinal.entity.EstadoUsuario;
import pablocos.gestor_biblioteca.kinal.entity.Prestamo;
import pablocos.gestor_biblioteca.kinal.entity.Rol;
import pablocos.gestor_biblioteca.kinal.entity.Usuario;
import pablocos.gestor_biblioteca.kinal.exception.BusinessRuleException;
import pablocos.gestor_biblioteca.kinal.exception.ResourceNotFoundException;
import pablocos.gestor_biblioteca.kinal.repository.LibroRepository;
import pablocos.gestor_biblioteca.kinal.repository.PrestamoRepository;
import pablocos.gestor_biblioteca.kinal.repository.UsuarioRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Orden de bloqueos (igual en prestamo y devolucion para evitar deadlocks):
 * usuario -> (prestamo) -> libro.
 * Se usa READ_COMMITTED para que, tras obtener el bloqueo del usuario, las lecturas
 * vean siempre lo ultimo confirmado por otras transacciones.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PrestamoService {

    static final int PLAZO_DIAS = 14;
    static final int LIMITE_PRESTAMOS_ACTIVOS = 3;

    /** Prestamos "sin devolver": ACTIVO y ATRASADO. */
    private static final List<EstadoPrestamo> SIN_DEVOLVER =
            List.of(EstadoPrestamo.ACTIVO, EstadoPrestamo.ATRASADO);

    private final PrestamoRepository prestamoRepository;
    private final UsuarioRepository usuarioRepository;
    private final LibroRepository libroRepository;

    /**
     * noRollbackFor: cuando se aplica la sancion (Regla 4) la solicitud se rechaza con 409,
     * pero el cambio de estado a SANCIONADO debe quedar guardado. Ninguna escritura de stock
     * ni de prestamo ocurre antes de las validaciones, asi que no hay estados a medias.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED, noRollbackFor = BusinessRuleException.class)
    public PrestamoResponse registrar(PrestamoRequest request) {
        LocalDate hoy = LocalDate.now();
        Long usuarioId = request.usuarioId();
        Long libroId = request.libroId();

        // 1. Bloqueo del usuario: serializa los prestamos concurrentes de la misma persona
        Usuario usuario = usuarioRepository.findByIdForUpdate(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId));
        Rol rol = usuario.getRol();
        EstadoUsuario estado = usuario.getEstado();

        if (!libroRepository.existsById(libroId)) {
            throw new ResourceNotFoundException("Libro no encontrado con id " + libroId);
        }

        // 2. Regla 4: sancionado, o con prestamos vencidos sin devolver -> pasa a SANCIONADO y se rechaza
        if (estado == EstadoUsuario.SANCIONADO) {
            throw new BusinessRuleException("El usuario está sancionado y no puede realizar nuevos préstamos");
        }
        if (prestamoRepository.existsByUsuarioIdAndEstadoInAndFechaDevolucionEsperadaBefore(
                usuarioId, SIN_DEVOLVER, hoy)) {
            prestamoRepository.marcarAtrasadosDeUsuario(
                    usuarioId, hoy, EstadoPrestamo.ACTIVO, EstadoPrestamo.ATRASADO);
            usuarioRepository.actualizarEstado(usuarioId, EstadoUsuario.SANCIONADO);
            throw new BusinessRuleException(
                    "El usuario pasó a estado SANCIONADO por tener préstamos vencidos sin devolver");
        }

        // 3. Regla 2: maximo 3 prestamos activos para un LECTOR
        if (rol == Rol.LECTOR
                && prestamoRepository.countByUsuarioIdAndEstadoIn(usuarioId, SIN_DEVOLVER)
                >= LIMITE_PRESTAMOS_ACTIVOS) {
            throw new BusinessRuleException("El lector ya tiene " + LIMITE_PRESTAMOS_ACTIVOS
                    + " préstamos activos; debe devolver alguno antes de pedir otro");
        }

        // 4. Regla 1: decremento atomico del stock (0 filas afectadas = sin stock)
        if (libroRepository.decrementarStock(libroId) == 0) {
            throw new BusinessRuleException("No hay stock disponible para este libro");
        }

        // 5. Regla 3: plazo de 14 dias
        Prestamo prestamo = Prestamo.builder()
                .usuario(usuarioRepository.getReferenceById(usuarioId))
                .libro(libroRepository.getReferenceById(libroId))
                .fechaPrestamo(hoy)
                .fechaDevolucionEsperada(hoy.plusDays(PLAZO_DIAS))
                .estado(EstadoPrestamo.ACTIVO)
                .build();
        return toResponse(prestamoRepository.save(prestamo), hoy);
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PrestamoResponse devolver(Long prestamoId) {
        LocalDate hoy = LocalDate.now();

        // Primero el usuario (mismo orden que en registrar), luego el prestamo, al final el libro
        Long usuarioId = prestamoRepository.findUsuarioIdById(prestamoId)
                .orElseThrow(() -> noEncontrado(prestamoId));
        Usuario usuario = usuarioRepository.findByIdForUpdate(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id " + usuarioId));
        boolean estabaSancionado = usuario.getEstado() == EstadoUsuario.SANCIONADO;

        Prestamo prestamo = prestamoRepository.findByIdForUpdate(prestamoId)
                .orElseThrow(() -> noEncontrado(prestamoId));
        if (prestamo.getEstado() == EstadoPrestamo.DEVUELTO) {
            throw new BusinessRuleException("El préstamo ya fue devuelto");
        }

        Long libroId = prestamo.getLibro().getId();
        prestamo.setFechaDevolucionReal(hoy);
        prestamo.setEstado(EstadoPrestamo.DEVUELTO);
        PrestamoResponse respuesta = toResponse(prestamo, hoy);

        // Incremento atomico del stock (hace flush del prestamo antes de ejecutarse)
        if (libroRepository.incrementarStock(libroId) == 0) {
            log.warn("No se pudo incrementar el stock del libro {} al devolver el prestamo {} "
                    + "(stockDisponible ya igualaba stockTotal)", libroId, prestamoId);
        }

        // Si estaba sancionado y ya no tiene vencidos sin devolver, vuelve a ACTIVO
        if (estabaSancionado
                && !prestamoRepository.existsByUsuarioIdAndEstadoInAndFechaDevolucionEsperadaBefore(
                usuarioId, SIN_DEVOLVER, hoy)) {
            usuarioRepository.actualizarEstado(usuarioId, EstadoUsuario.ACTIVO);
        }
        return respuesta;
    }

    @Transactional(readOnly = true)
    public PageResponse<PrestamoResponse> misPrestamos(Long usuarioId, Pageable pageable) {
        LocalDate hoy = LocalDate.now();
        return PageResponse.from(
                prestamoRepository.findByUsuarioId(usuarioId, pageable).map(p -> toResponse(p, hoy)));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public PageResponse<PrestamoResponse> atrasados(Pageable pageable) {
        LocalDate hoy = LocalDate.now();
        // Persiste el estado ATRASADO de los vencidos que seguian como ACTIVO y los lista
        prestamoRepository.marcarAtrasados(hoy, EstadoPrestamo.ACTIVO, EstadoPrestamo.ATRASADO);
        return PageResponse.from(
                prestamoRepository.findByEstadoInAndFechaDevolucionEsperadaBefore(
                        List.of(EstadoPrestamo.ATRASADO), hoy, pageable).map(p -> toResponse(p, hoy)));
    }

    /** El estado mostrado es ATRASADO si esta ACTIVO pero ya vencio, aunque aun no se haya persistido. */
    private PrestamoResponse toResponse(Prestamo p, LocalDate hoy) {
        EstadoPrestamo estado = p.getEstado();
        if (estado == EstadoPrestamo.ACTIVO && p.getFechaDevolucionEsperada().isBefore(hoy)) {
            estado = EstadoPrestamo.ATRASADO;
        }
        return new PrestamoResponse(p.getId(), p.getUsuario().getId(), p.getLibro().getId(),
                p.getFechaPrestamo(), p.getFechaDevolucionEsperada(), p.getFechaDevolucionReal(), estado);
    }

    private ResourceNotFoundException noEncontrado(Long id) {
        return new ResourceNotFoundException("Préstamo no encontrado con id " + id);
    }
}
