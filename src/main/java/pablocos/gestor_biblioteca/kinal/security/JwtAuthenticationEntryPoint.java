package pablocos.gestor_biblioteca.kinal.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** 401: token ausente, invalido o expirado en un recurso protegido. */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object detalle = request.getAttribute(JwtAuthenticationFilter.ERROR_ATTRIBUTE);
        String mensaje = detalle != null ? detalle.toString() : "Autenticación requerida: token ausente";
        SecurityErrorWriter.write(request, response, HttpStatus.UNAUTHORIZED, mensaje);
    }
}