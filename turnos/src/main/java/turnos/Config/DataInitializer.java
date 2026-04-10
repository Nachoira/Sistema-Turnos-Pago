package turnos.Config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import turnos.Modelo.AdminUser;
import turnos.Modelo.Configuracion;
import turnos.Repositorio.AdminUserRepository;
import turnos.Repositorio.ConfiguracionRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final ConfiguracionRepository configuracionRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        cargarConfiguracionInicial();
        crearAdminInicial();
    }

    // Carga la configuración del consultorio si no existe todavía
    private void cargarConfiguracionInicial() {
        if (configuracionRepository.count() > 0) {
            log.info("⚙️  Configuración ya existente, no se sobreescribe.");
            return;
        }

        List<Configuracion> configs = List.of(
                new Configuracion("nombre_consultorio", "Consultorio Dra. García"),
                new Configuracion("nombre_doctora",     "Dra. María García"),
                new Configuracion("direccion",          "Av. San Martín 456, Concepción, Tucumán"),
                new Configuracion("monto_sesion",       "8000"),
                new Configuracion("duracion_turno_min", "30"),
                new Configuracion("hora_inicio",        "09:00"),
                new Configuracion("hora_fin",           "18:00")
        );

        configuracionRepository.saveAll(configs);
        log.info("⚙️  Configuración inicial cargada en la BD.");
        log.info("⚠️  Editá los valores en la tabla 'configuracion' de PgAdmin según el consultorio real.");
    }

    // Crea el usuario admin si no existe todavía
    private void crearAdminInicial() {
        String emailAdmin = "admin@consultorio.com";

        if (adminUserRepository.findByEmail(emailAdmin).isPresent()) {
            log.info("👤 Usuario admin ya existe, no se crea de nuevo.");
            return;
        }

        AdminUser admin = AdminUser.builder()
                .email(emailAdmin)
                .passwordHash(passwordEncoder.encode("admin1234"))
                .nombre("Dra. María García")
                .build();

        adminUserRepository.save(admin);

        log.info("👤 Usuario admin creado:");
        log.info("   Email:    {}", emailAdmin);
        log.info("   Password: admin1234");
        log.warn("⚠️  CAMBIÁ LA CONTRASEÑA DEL ADMIN ANTES DE IR A PRODUCCIÓN.");
    }
}

