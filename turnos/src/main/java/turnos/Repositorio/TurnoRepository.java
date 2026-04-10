package turnos.Repositorio;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import turnos.Modelo.EstadoTurno;
import turnos.Modelo.Turno;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    // Verificar si un horario ya está ocupado (CONFIRMADO o PENDIENTE_PAGO vigente)
    @Query("""
        SELECT COUNT(t) > 0 FROM Turno t
        WHERE t.fechaHora = :fechaHora
        AND t.estado IN ('CONFIRMADO', 'PENDIENTE_PAGO')
        AND (t.estado = 'CONFIRMADO' OR t.expiraEn > :ahora)
    """)
    boolean existeTurnoActivo(
            @Param("fechaHora") LocalDateTime fechaHora,
            @Param("ahora") LocalDateTime ahora
    );

    // Obtener todos los horarios ocupados de un día (para calcular slots disponibles)
    @Query("""
        SELECT t.fechaHora FROM Turno t
        WHERE t.fechaHora >= :inicioDia
        AND t.fechaHora <= :finDia
        AND t.estado IN ('CONFIRMADO', 'PENDIENTE_PAGO')
        AND (t.estado = 'CONFIRMADO' OR t.expiraEn > :ahora)
    """)
    List<LocalDateTime> findHorariosOcupados(
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("finDia") LocalDateTime finDia,
            @Param("ahora") LocalDateTime ahora
    );

    // Panel admin: turnos de un día ordenados por hora
    @Query("""
        SELECT t FROM Turno t
        WHERE t.fechaHora >= :inicioDia
        AND t.fechaHora <= :finDia
        ORDER BY t.fechaHora ASC
    """)
    List<Turno> findTurnosPorDia(
            @Param("inicioDia") LocalDateTime inicioDia,
            @Param("finDia") LocalDateTime finDia
    );

    // Panel admin: turnos futuros confirmados
    List<Turno> findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(
            EstadoTurno estado,
            LocalDateTime desde
    );

    // Buscar por payment_id de MP (para el webhook)
    Optional<Turno> findByMpPreferenceId(String mpPreferenceId);
    Optional<Turno> findByMpPaymentId(String mpPaymentId);

    // Expirar turnos pendientes de pago vencidos (cron job)
    @Modifying
    @Query("""
        UPDATE Turno t SET t.estado = 'EXPIRADO'
        WHERE t.estado = 'PENDIENTE_PAGO'
        AND t.expiraEn < :ahora
    """)
    int expirarTurnosVencidos(@Param("ahora") LocalDateTime ahora);
}
