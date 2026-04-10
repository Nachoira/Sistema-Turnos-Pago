package turnos.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;       // JWT que el frontend guarda en localStorage
    private String nombre;      // Nombre de la doctora para mostrar en el panel
    private String email;
}