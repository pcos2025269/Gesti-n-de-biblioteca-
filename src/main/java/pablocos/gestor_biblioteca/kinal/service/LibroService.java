package pablocos.gestor_biblioteca.kinal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pablocos.gestor_biblioteca.kinal.dto.LibroDto;
import pablocos.gestor_biblioteca.kinal.entity.Libro;
import pablocos.gestor_biblioteca.kinal.exception.ResourceNotFoundException;
import pablocos.gestor_biblioteca.kinal.repository.LibroRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;

    @Transactional(readOnly = true)
    public List<LibroDto> listarLibros() {
        return libroRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public LibroDto obtenerPorId(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + id));
        return mapToDto(libro);
    }

    @Transactional
    public LibroDto crearLibro(LibroDto dto) {
        Libro libro = Libro.builder()
                .titulo(dto.getTitulo())
                .autor(dto.getAutor())
                .isbn(dto.getIsbn())
                .stockTotal(dto.getStockTotal())
                .stockDisponible(dto.getStockTotal())
                .build();
        Libro saved = libroRepository.save(libro);
        return mapToDto(saved);
    }

    @Transactional
    public LibroDto actualizarLibro(Long id, LibroDto dto) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + id));
        
        libro.setTitulo(dto.getTitulo());
        libro.setAutor(dto.getAutor());
        libro.setIsbn(dto.getIsbn());
        libro.setStockTotal(dto.getStockTotal());
        
        Libro updated = libroRepository.save(libro);
        return mapToDto(updated);
    }

    @Transactional
    public void eliminarLibro(Long id) {
        Libro libro = libroRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Libro no encontrado con ID: " + id));
        libroRepository.delete(libro);
    }

    private LibroDto mapToDto(Libro l) {
        return LibroDto.builder()
                .id(l.getId())
                .titulo(l.getTitulo())
                .autor(l.getAutor())
                .isbn(l.getIsbn())
                .stockTotal(l.getStockTotal())
                .stockDisponible(l.getStockDisponible())
                .build();
    }
}
