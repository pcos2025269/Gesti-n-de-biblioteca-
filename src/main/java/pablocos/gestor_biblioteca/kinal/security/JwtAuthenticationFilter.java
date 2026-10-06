package pablocos.gestor_biblioteca.kinal.security;

import pablocos.gestor_biblioteca.kinal.entity.Rol;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee "Authorization: Bearer <token>", valida el JWT y puebla el SecurityContext.
 * Si el token falta o es invalido NO responde aqui: deja la solicitud sin autenticar y el
 * AuthenticationEntryPoint devuelve 401 solo si el recurso lo exige.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String ERROR_ATTRIBUTE = "jwt.error";

    private static final String BEARER = "Bearer ";

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (header != null && header.startsWith(BEARER)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = header.substring(BEARER.length()).trim();
            try {
                Claims claims = jwtService.parsear(token);
                Number uid = claims.get(JwtService.CLAIM_USER_ID, Number.class);
                Rol rol = Rol.valueOf(claims.get(JwtService.CLAIM_ROL, String.class));

                AuthenticatedUser principal =
                        new AuthenticatedUser(uid.longValue(), claims.getSubject(), rol.name());
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, List.of(new SimpleGrantedAuthority("ROLE_" + rol.name())));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (ExpiredJwtException ex) {
                request.setAttribute(ERROR_ATTRIBUTE, "Token expirado");
            } catch (JwtException | IllegalArgumentException | NullPointerException ex) {
                request.setAttribute(ERROR_ATTRIBUTE, "Token inválido");
            }
        }

        filterChain.doFilter(request, response);
    }
}
