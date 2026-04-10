package turnos.Modelo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "configuracion")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Configuracion {

    // Clave primaria es la clave del config (ej: "monto_sesion", "nombre_consultorio")
    @Id
    @Column(length = 50)
    private String clave;

    @Column(nullable = false, length = 255)
    private String valor;
}
