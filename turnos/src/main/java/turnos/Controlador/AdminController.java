package turnos.Controlador;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turnos.DTOs.TurnoResponse;
import turnos.Servicio.ConfiguracionService;
import turnos.Servicio.TurnoService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

// Todas las rutas /api/admin/** requieren JWT (configurado en SecurityConfig)
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TurnoService turnoService;
    private final ConfiguracionService configuracionService;

    // GET /api/admin/turnos?fecha=2024-03-15
    // Turnos de un día específico (para el panel diario)
    @GetMapping("/turnos")
    public ResponseEntity<List<TurnoResponse>> getTurnosPorDia(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        LocalDate fechaConsulta = fecha != null ? fecha : LocalDate.now();
        return ResponseEntity.ok(turnoService.getTurnosPorDia(fechaConsulta));
    }

    // GET /api/admin/turnos/proximos
    // Todos los turnos confirmados futuros (vista general)
    @GetMapping("/turnos/proximos")
    public ResponseEntity<List<TurnoResponse>> getTurnosProximos() {
        return ResponseEntity.ok(turnoService.getTurnosProximos());
    }

    // GET /api/admin/configuracion
    // Devuelve los datos del consultorio para mostrar en el panel
    @GetMapping("/configuracion")
    public ResponseEntity<Map<String, String>> getConfiguracion() {
        return ResponseEntity.ok(Map.of(
                "nombreConsultorio", configuracionService.getNombreConsultorio(),
                "nombreDoctora", configuracionService.getNombreDoctora(),
                "direccion", configuracionService.getDireccion(),
                "montoSesion", configuracionService.getMonto().toPlainString(),
                "horaInicio", configuracionService.getHoraInicio(),
                "horaFin", configuracionService.getHoraFin()
        ));
    }
}

