package pablocos.gestor_biblioteca.kinal.service;

import pablocos.gestor_biblioteca.kinal.dto.LibroRequest;
import pablocos.gestor_biblioteca.kinal.dto.LibroResponse;
import pablocos.gestor_biblioteca.kinal.dto.PageResponse;
import pablocos.gestor_biblioteca.kinal.entity.Libro;
import pablocos.gestor_biblioteca.kinal.exception.BusinessRuleException;
import pablocos.gestor_biblioteca.kinal.exception.InvalidRequestException;
import pablocos.gestor_biblioteca.kinal.exception.ResourceNotFoundException;
import pablocos.gestor_biblioteca.kinal.repository.LibroRepository;
import pablocos.gestor_biblioteca.kinal.repository.PrestamoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LibroService {

    private final LibroRepository libroRepository;
    private final PrestamoRepository prestamoRepository;

    @Transactional(readOnly = true)
    public PageResponse<LibroResponse> listar(String titulo, String categoria, Pageable pageable) {
        return PageResponse.from(
                libroRepository.buscar(vacioANulo(titulo), vacioANulo(categoria), pageable)
                        .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public LibroResponse obtener(Long id) {
        return toResponse(libroRepository.findById(id).orElseThrow(() -> noEncontrado(id)));
    }

    @Transactional
    public LibroResponse crear(LibroRequest request) {
        validarStockInformado(request);
        String isbn = request.isbn().trim();
        if (libroRepository.existsByIsbn(isbn)) {
            throw new BusinessRuleException("Ya existe un libro con el ISBN " + isbn);
        }

        int disponible = request.stockDisponible() != null
                ? request.stockDisponible()
                : request.stockTotal();

        Libro libro = Libro.builder()
                .isbn(isbn)
                .titulo(request.titulo().trim())
                .autor(request.autor().trim())
                .categoria(request.categoria().trim())
                .stockTotal(request.stockTotal())
                .stockDisponible(disponible)
                .build();
        return toResponse(libroRepository.save(libro));
    }

    @Transactional
    public LibroResponse actualizar(Long id, LibroRequest request) {
        validarStockInformado(request);
        Libro libro = libroRepository.findByIdForUpdate(id).orElseThrow(() -> noEncontrado(id));

        String isbn = request.isbn().trim();
        if (libroRepository.existsByIsbnAndIdNot(isbn, id)) {
            throw new BusinessRuleException("Ya existe otro libro con el ISBN " + isbn);
        }

        int nuevoTotal = request.stockTotal();
        int nuevoDisponible = request.stockDisponible() != null
                ? request.stockDisponible()
                : libro.getStockDisponible() + (nuevoTotal - libro.getStockTotal());
        if (nuevoDisponible < 0 || nuevoDisponible > nuevoTotal) {
            throw new BusinessRuleException(
                    "Stock inconsistente: el stock disponible debe estar entre 0 y el stock total "
                            + "(hay ejemplares actualmente prestados)");
        }

        libro.setIsbn(isbn);
        libro.setTitulo(request.titulo().trim());
        libro.setAutor(request.autor().trim());
        libro.setCategoria(request.categoria().trim());
        libro.setStockTotal(nuevoTotal);
        libro.setStockDisponible(nuevoDisponible);
        return toResponse(libro);
    }

    /** Eliminacion fisica. Si el libro tiene prestamos asociados se responde 409. */
    @Transactional
    public void eliminar(Long id) {
        Libro libro = libroRepository.findByIdForUpdate(id).orElseThrow(() -> noEncontrado(id));
        if (prestamoRepository.existsByLibroId(id)) {
            throw new BusinessRuleException("No se puede eliminar un libro que tiene préstamos asociados");
        }
        libroRepository.delete(libro);
    }

    private void validarStockInformado(LibroRequest request) {
        if (request.stockDisponible() != null && request.stockDisponible() > request.stockTotal()) {
            throw new InvalidRequestException("stockDisponible no puede ser mayor que stockTotal");
        }
    }

    private LibroResponse toResponse(Libro libro) {
        return new LibroResponse(libro.getId(), libro.getIsbn(), libro.getTitulo(), libro.getAutor(),
                libro.getCategoria(), libro.getStockTotal(), libro.getStockDisponible());
    }

    private ResourceNotFoundException noEncontrado(Long id) {
        return new ResourceNotFoundException("Libro no encontrado con id " + id);
    }

    private String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
