package pablocos.gestor_biblioteca.kinal.service;



import pablocos.gestor_biblioteca.kinal.dto.AuthResponse;
import pablocos.gestor_biblioteca.kinal.dto.LoginRequest;
import pablocos.gestor_biblioteca.kinal.dto.RegisterRequest;
import pablocos.gestor_biblioteca.kinal.dto.UsuarioResponse;
import pablocos.gestor_biblioteca.kinal.entity.EstadoUsuario;
import pablocos.gestor_biblioteca.kinal.entity.Rol;
import pablocos.gestor_biblioteca.kinal.entity.Usuario;
import pablocos.gestor_biblioteca.kinal.exception.BusinessRuleException;
import pablocos.gestor_biblioteca.kinal.repository.UsuarioRepository;
import pablocos.gestor_biblioteca.kinal.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /** Registro publico: siempre rol LECTOR y estado ACTIVO. */
    @Transactional
    public UsuarioResponse register(RegisterRequest request) {
        String email = normalizar(request.email());
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Ya existe un usuario registrado con ese email");
        }

        Usuario usuario = Usuario.builder()
                .nombre(request.nombre().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .estado(EstadoUsuario.ACTIVO)
                .rol(Rol.LECTOR)
                .build();

        Usuario guardado = usuarioRepository.save(usuario);
        return new UsuarioResponse(guardado.getId(), guardado.getNombre(), guardado.getEmail(),
                guardado.getEstado(), guardado.getRol());
    }

    /** Valida credenciales (BCrypt) y devuelve el JWT. Credenciales invalidas -> 401. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = normalizar(request.email());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.password()));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));
        return new AuthResponse(jwtService.generarToken(usuario));
    }

    private String normalizar(String email) {
        return email.trim().toLowerCase();
    }
}