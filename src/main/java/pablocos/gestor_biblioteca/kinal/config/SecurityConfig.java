package pablocos.gestor_biblioteca.kinal.config;

import pablocos.gestor_biblioteca.kinal.security.JwtAccessDeniedHandler;
import pablocos.gestor_biblioteca.kinal.security.JwtAuthenticationEntryPoint;
import pablocos.gestor_biblioteca.kinal.security.JwtAuthenticationFilter;
import pablocos.gestor_biblioteca.kinal.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtService jwtService;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .authorizeHttpRequests(auth -> auth
                        // Autenticacion
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Libros
                        .requestMatchers(HttpMethod.GET, "/api/v1/libros", "/api/v1/libros/*").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/libros").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/libros/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/libros/*").hasRole("ADMIN")
                        // Prestamos
                        .requestMatchers(HttpMethod.POST, "/api/v1/prestamos")
                                .hasAnyRole("BIBLIOTECARIO", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/v1/prestamos/*/devolucion")
                                .hasAnyRole("BIBLIOTECARIO", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/prestamos/mis-prestamos")
                                .hasRole("LECTOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/prestamos/atrasados")
                                .hasAnyRole("BIBLIOTECARIO", "ADMIN")
                        .anyRequest().authenticated())
                // Se instancia aqui (no es @Component) para que Boot no lo registre dos veces
                .addFilterBefore(new JwtAuthenticationFilter(jwtService), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
