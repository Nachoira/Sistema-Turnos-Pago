package turnos.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import turnos.Modelo.EstadoTurno;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Lo que devuelve la API cuando se consulta un turno
// (panel admin, página de confirmación, etc.)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponse {
    private Long id;
    private String pacienteNombre;
    private String pacienteEmail;
    private String pacienteTel;
    private LocalDateTime fechaHora;
    private EstadoTurno estado;
    private BigDecimal monto;
    private String mpPaymentId;
    private LocalDateTime createdAt;
}

