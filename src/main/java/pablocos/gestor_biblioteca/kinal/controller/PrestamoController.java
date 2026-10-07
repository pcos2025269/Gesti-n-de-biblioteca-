package pablocos.gestor_biblioteca.kinal.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pablocos.gestor_biblioteca.kinal.dto.PageResponse;
import pablocos.gestor_biblioteca.kinal.dto.PrestamoRequest;
import pablocos.gestor_biblioteca.kinal.dto.PrestamoResponse;
import pablocos.gestor_biblioteca.kinal.security.AuthenticatedUser;
import pablocos.gestor_biblioteca.kinal.service.PrestamoService;

@RestController
@RequestMapping("/api/v1/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private static final int MAX_SIZE = 100;

    private final PrestamoService prestamoService;

    /** BIBLIOTECARIO, ADMIN (autorizado en SecurityConfig). */
    @PostMapping
    public ResponseEntity<PrestamoResponse> registrar(@Valid @RequestBody PrestamoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(prestamoService.registrar(request));
    }

    /** BIBLIOTECARIO, ADMIN. */
    @PatchMapping("/{id}/devolucion")
    public PrestamoResponse devolver(@PathVariable("id") Long id) {
        return prestamoService.devolver(id);
    }

    /** LECTOR: historial y activos del usuario autenticado (el id sale del JWT, no de la URL). */
    @GetMapping("/mis-prestamos")
    public PageResponse<PrestamoResponse> misPrestamos(
            @AuthenticationPrincipal AuthenticatedUser usuario,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Sort orden = Sort.by("fechaPrestamo").descending().and(Sort.by("id").descending());
        return prestamoService.misPrestamos(usuario.id(), pagina(page, size, orden));
    }

    /** BIBLIOTECARIO, ADMIN. */
    @GetMapping("/atrasados")
    public PageResponse<PrestamoResponse> atrasados(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        Sort orden = Sort.by("fechaDevolucionEsperada").ascending().and(Sort.by("id").ascending());
        return prestamoService.atrasados(pagina(page, size, orden));
    }

    private PageRequest pagina(int page, int size, Sort orden) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_SIZE), orden);
    }
}
