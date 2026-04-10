package turnos.Controlador;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turnos.DTOs.LoginRequest;
import turnos.DTOs.LoginResponse;
import turnos.Servicio.AuthService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login
    // Body: { "email": "admin@consultorio.com", "password": "tu_password" }
    // Devuelve: { "token": "eyJ...", "nombre": "Dra. García", "email": "..." }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}

