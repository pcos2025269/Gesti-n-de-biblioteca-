package pablocos.gestor_biblioteca.kinal.controller;

import pablocos.gestor_biblioteca.kinal.dto.LibroRequest;
import pablocos.gestor_biblioteca.kinal.dto.LibroResponse;
import pablocos.gestor_biblioteca.kinal.dto.PageResponse;
import pablocos.gestor_biblioteca.kinal.service.LibroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/libros")
@RequiredArgsConstructor
public class LibroController {

    private static final int MAX_SIZE = 100;

    private final LibroService libroService;

    @GetMapping
    public PageResponse<LibroResponse> listar(
            @RequestParam(name = "titulo", required = false) String titulo,
            @RequestParam(name = "categoria", required = false) String categoria,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_SIZE),
                Sort.by("titulo").ascending().and(Sort.by("id").ascending()));
        return libroService.listar(titulo, categoria, pageable);
    }

    @GetMapping("/{id}")
    public LibroResponse obtener(@PathVariable("id") Long id) {
        return libroService.obtener(id);
    }

    @PostMapping
    public ResponseEntity<LibroResponse> crear(@Valid @RequestBody LibroRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(libroService.crear(request));
    }

    @PutMapping("/{id}")
    public LibroResponse actualizar(@PathVariable("id") Long id, @Valid @RequestBody LibroRequest request) {
        return libroService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable("id") Long id) {
        libroService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
