package turnos.Servicio;


import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import turnos.DTOs.LoginRequest;
import turnos.DTOs.LoginResponse;
import turnos.Modelo.AdminUser;
import turnos.Repositorio.AdminUserRepository;
import turnos.Security.JwtUtil;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        // Buscar el usuario por email
        AdminUser user = adminUserRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        // Verificar la contraseña con BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Credenciales inválidas");
        }

        // Generar JWT
        String token = jwtUtil.generateToken(user.getEmail());

        return new LoginResponse(token, user.getNombre(), user.getEmail());
    }
}

