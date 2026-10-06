package pablocos.gestor_biblioteca.kinal.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "libro",
        uniqueConstraints = @UniqueConstraint(name = "uk_libro_isbn", columnNames = "isbn"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Libro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String isbn;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(nullable = false, length = 150)
    private String autor;

    @Column(nullable = false, length = 100)
    private String categoria;

    @Column(name = "stock_total", nullable = false)
    private Integer stockTotal;

    @Column(name = "stock_disponible", nullable = false)
    private Integer stockDisponible;

    @OneToMany(mappedBy = "libro", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Prestamo> prestamos = new ArrayList<>();
}
