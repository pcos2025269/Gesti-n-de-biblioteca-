package pablocos.gestor_biblioteca.kinal.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pablocos.gestor_biblioteca.kinal.dto.LibroDto;
import pablocos.gestor_biblioteca.kinal.service.LibroService;

import java.util.List;

@RestController
@RequestMapping("/libros")
@RequiredArgsConstructor
public class LibroController {

    private final LibroService libroService;

    @GetMapping
    public ResponseEntity<List<LibroDto>> listarLibros() {
        return ResponseEntity.ok(libroService.listarLibros());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LibroDto> obtenerLibro(@PathVariable Long id) {
        return ResponseEntity.ok(libroService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<LibroDto> crearLibro(@RequestBody LibroDto libroDto) {
        return ResponseEntity.ok(libroService.crearLibro(libroDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'BIBLIOTECARIO')")
    public ResponseEntity<LibroDto> actualizarLibro(@PathVariable Long id, @RequestBody LibroDto libroDto) {
        return ResponseEntity.ok(libroService.actualizarLibro(id, libroDto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminarLibro(@PathVariable Long id) {
        libroService.eliminarLibro(id);
        return ResponseEntity.noContent().build();
    }
}
