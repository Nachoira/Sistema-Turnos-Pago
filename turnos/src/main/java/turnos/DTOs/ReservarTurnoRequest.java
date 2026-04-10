package turnos.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReservarTurnoRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String pacienteNombre;

    @Email(message = "Email inválido")
    @NotBlank(message = "El email es obligatorio")
    private String pacienteEmail;

    @NotBlank(message = "El teléfono es obligatorio")
    private String pacienteTel;

    @NotNull(message = "La fecha y hora son obligatorias")
    private LocalDateTime fechaHora;
}

