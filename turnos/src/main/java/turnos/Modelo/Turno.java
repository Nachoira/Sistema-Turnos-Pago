package turnos.Modelo;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "turnos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Turno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Datos del paciente
    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "paciente_nombre", nullable = false, length = 100)
    private String pacienteNombre;

    @Email(message = "Email inválido")
    @NotBlank(message = "El email es obligatorio")
    @Column(name = "paciente_email", nullable = false, length = 150)
    private String pacienteEmail;

    @NotBlank(message = "El teléfono es obligatorio")
    @Column(name = "paciente_tel", nullable = false, length = 20)
    private String pacienteTel;

    // Datos del turno
    @NotNull(message = "La fecha y hora son obligatorias")
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    @Builder.Default
    private EstadoTurno estado = EstadoTurno.PENDIENTE_PAGO;

    @NotNull
    @Column(name = "monto", nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    // Mercado Pago
    @Column(name = "mp_preference_id", length = 200)
    private String mpPreferenceId;

    @Column(name = "mp_payment_id", length = 100)
    private String mpPaymentId;

    @Column(name = "mp_status", length = 50)
    private String mpStatus;

    // TTL: el turno expira si no se paga en 15 minutos
    @Column(name = "expira_en")
    private LocalDateTime expiraEn;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
