package turnos.Controlador;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turnos.DTOs.ReservarTurnoRequest;
import turnos.DTOs.ReservarTurnoResponse;
import turnos.DTOs.TurnoResponse;
import turnos.Servicio.TurnoService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    // GET /api/turnos/slots?fecha=2024-03-15
    // Devuelve los horarios disponibles para una fecha
    // Lo llama el frontend cuando el paciente elige fecha
    @GetMapping("/slots")
    public ResponseEntity<List<LocalDateTime>> getSlotsDisponibles(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha
    ) {
        return ResponseEntity.ok(turnoService.getSlotsDisponibles(fecha));
    }

    // POST /api/turnos
    // Crea la pre-reserva y devuelve la URL de pago de MP
    // Body: { pacienteNombre, pacienteEmail, pacienteTel, fechaHora }
    @PostMapping
    public ResponseEntity<ReservarTurnoResponse> reservarTurno(
            @Valid @RequestBody ReservarTurnoRequest request
    ) {
        return ResponseEntity.ok(turnoService.reservarTurno(request));
    }

    // GET /api/turnos/{id}
    // Devuelve los datos de un turno (para la página de confirmación)
    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponse> getTurno(@PathVariable Long id) {
        return ResponseEntity.ok(turnoService.getTurnoPorId(id));
    }
}