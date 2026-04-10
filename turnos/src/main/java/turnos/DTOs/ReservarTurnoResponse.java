package turnos.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Respuesta al frontend tras crear la pre-reserva
// El frontend redirige al paciente a checkoutUrl
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservarTurnoResponse {
    private Long turnoId;
    private String checkoutUrl;     // URL real de MP (producción)
    private String sandboxUrl;      // URL de pruebas de MP
}
