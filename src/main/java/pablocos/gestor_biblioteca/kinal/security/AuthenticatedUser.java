package pablocos.gestor_biblioteca.kinal.security;

/** Principal que el filtro JWT coloca en el SecurityContext (sin consultar la base de datos). */
public record AuthenticatedUser(Long id, String email, String rol) {
}
