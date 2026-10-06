package pablocos.gestor_biblioteca.kinal.dto;


import pablocos.gestor_biblioteca.kinal.entity.EstadoUsuario;
import pablocos.gestor_biblioteca.kinal.entity.Rol;

public record UsuarioResponse(
        Long id,
        String nombre,
        String email,
        EstadoUsuario estado,
        Rol rol
) {
}