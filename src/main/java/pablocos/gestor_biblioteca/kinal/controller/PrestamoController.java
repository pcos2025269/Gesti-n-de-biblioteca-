package pablocos.gestor_biblioteca.kinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pablocos.gestor_biblioteca.kinal.dto.PrestamoRequestDto;
import pablocos.gestor_biblioteca.kinal.dto.PrestamoResponseDto;
import pablocos.gestor_biblioteca.kinal.service.PrestamoService;

import java.util.List;

@RestController
@RequestMapping("/prestamos")
@RequiredArgsConstructor
public class PrestamoController {

    private final PrestamoService prestamoService;

    @GetMapping
    public ResponseEntity<List<PrestamoResponseDto>> listarPrestamos(Authentication authentication) {
        String email = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        return ResponseEntity.ok(prestamoService.obtenerPrestamos(email, role));
    }

    @PostMapping
    public ResponseEntity<PrestamoResponseDto> solicitarPrestamo(Authentication authentication, @RequestBody PrestamoRequestDto requestDto) {
        String email = authentication.getName();
        return ResponseEntity.ok(prestamoService.crearPrestamo(email, requestDto));
    }

    @PostMapping("/{id}/devolucion")
    public ResponseEntity<PrestamoResponseDto> registrarDevolucion(@PathVariable Long id) {
        return ResponseEntity.ok(prestamoService.registrarDevolucion(id));
    }
}
