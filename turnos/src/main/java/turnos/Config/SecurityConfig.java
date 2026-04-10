package turnos.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import turnos.Security.JwtFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Deshabilitamos CSRF porque usamos JWT (stateless)
                .csrf(AbstractHttpConfigurer::disable)

                // Sin sesión en el servidor, cada request se autentica con JWT
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                .authorizeHttpRequests(auth -> auth

                        // ── RUTAS PÚBLICAS ──────────────────────────────────────
                        // Página principal y slots disponibles (cualquier visitante)
                        .requestMatchers(HttpMethod.GET, "/api/turnos/slots").permitAll()
                        // Reservar turno (el paciente)
                        .requestMatchers(HttpMethod.POST, "/api/turnos").permitAll()
                        // Consultar turno por ID (página de confirmación)
                        .requestMatchers(HttpMethod.GET, "/api/turnos/**").permitAll()
                        // Login de la doctora
                        .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                        // Webhook de Mercado Pago (MP llama desde sus servidores)
                        .requestMatchers("/api/webhook/**").permitAll()

                        // ── RUTAS PROTEGIDAS ────────────────────────────────────
                        // Todo lo demás bajo /api/admin/** requiere JWT válido
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Cualquier otra ruta no listada requiere autenticación
                        .anyRequest().authenticated()
                )

                // Agregar nuestro filtro JWT antes del filtro de autenticación estándar
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // BCrypt para hashear contraseñas
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

