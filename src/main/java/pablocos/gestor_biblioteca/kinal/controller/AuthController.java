package pablocos.gestor_biblioteca.kinal.controller;

import  pablocos.gestor_biblioteca.kinal.dto.AuthResponse;
import  pablocos.gestor_biblioteca.kinal.dto.LoginRequest;
import  pablocos.gestor_biblioteca.kinal.dto.RegisterRequest;
import  pablocos.gestor_biblioteca.kinal.dto.UsuarioResponse;
import  pablocos.gestor_biblioteca.kinal.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}