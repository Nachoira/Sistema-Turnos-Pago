package turnos.Servicio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import turnos.DTOs.ReservarTurnoRequest;
import turnos.DTOs.ReservarTurnoResponse;
import turnos.DTOs.TurnoResponse;
import turnos.Modelo.EstadoTurno;
import turnos.Modelo.Turno;
import turnos.Repositorio.TurnoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final ConfiguracionService configuracionServicio;
    private final MercadoPagoService mercadoPagoService;

    // ─────────────────────────────────────────
    // SLOTS DISPONIBLES
    // ─────────────────────────────────────────

    public List<LocalDateTime> getSlotsDisponibles(LocalDate fecha) {
        // Parsear configuración
        LocalTime horaInicio = LocalTime.parse(configuracionServicio.getHoraInicio());
        LocalTime horaFin    = LocalTime.parse(configuracionServicio.getHoraFin());
        int duracion         = configuracionServicio.getDuracionTurnoMin();

        // Generar todos los slots del día
        List<LocalDateTime> todosLosSlots = new ArrayList<>();
        LocalDateTime slot = LocalDateTime.of(fecha, horaInicio);
        LocalDateTime fin  = LocalDateTime.of(fecha, horaFin);
        LocalDateTime ahora = LocalDateTime.now();

        while (slot.isBefore(fin)) {
            // Solo agregar slots futuros (con al menos 1 hora de anticipación)
            if (slot.isAfter(ahora.plusHours(1))) {
                todosLosSlots.add(slot);
            }
            slot = slot.plusMinutes(duracion);
        }

        // Obtener horarios ya ocupados de la BD
        LocalDateTime inicioDia = LocalDateTime.of(fecha, LocalTime.MIN);
        LocalDateTime finDia    = LocalDateTime.of(fecha, LocalTime.MAX);
        Set<LocalDateTime> ocupados = turnoRepository
                .findHorariosOcupados(inicioDia, finDia, ahora)
                .stream()
                .collect(Collectors.toSet());

        // Filtrar los ocupados
        return todosLosSlots.stream()
                .filter(s -> !ocupados.contains(s))
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // RESERVAR TURNO (pre-reserva + MP)
    // ─────────────────────────────────────────

    @Transactional
    public ReservarTurnoResponse reservarTurno(ReservarTurnoRequest request) {
        LocalDateTime ahora = LocalDateTime.now();

        // 1. Verificar disponibilidad
        boolean ocupado = turnoRepository.existeTurnoActivo(request.getFechaHora(), ahora);
        if (ocupado) {
            throw new RuntimeException("El horario seleccionado ya no está disponible.");
        }

        // 2. Crear pre-reserva usando la INSTANCIA configuracionServicio
        Turno turno = Turno.builder()
                .pacienteNombre(request.getPacienteNombre())
                .pacienteEmail(request.getPacienteEmail())
                .pacienteTel(request.getPacienteTel())
                .fechaHora(request.getFechaHora())
                .estado(EstadoTurno.PENDIENTE_PAGO)
                .monto(configuracionServicio.getMonto()) // <--- CORREGIDO (minúscula)
                .expiraEn(ahora.plusMinutes(15))
                .build();

        turno = turnoRepository.save(turno);

        // 3. Crear preferencia usando la INSTANCIA mercadoPagoService
        // Asegúrate de que el método crearPreferencia NO sea static en su clase original
        MercadoPagoService.PreferenciaResult preferencia =
                mercadoPagoService.crearPreferencia(turno); // <--- CORREGIDO (llamada de instancia)

        // 4. Guardar ID de preferencia
        turno.setMpPreferenceId(preferencia.getPreferenceId());
        turnoRepository.save(turno);

        return ReservarTurnoResponse.builder()
                .turnoId(turno.getId())
                .checkoutUrl(preferencia.getInitPoint())
                .sandboxUrl(preferencia.getSandboxInitPoint())
                .build();
    }
    @Transactional
    public void confirmarTurno(String mpPreferenceId, String mpPaymentId, String mpStatus) {
        Turno turno = turnoRepository.findByMpPreferenceId(mpPreferenceId)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado para preferencia: " + mpPreferenceId));

        turno.setMpPaymentId(mpPaymentId);
        turno.setMpStatus(mpStatus);

        if ("approved".equals(mpStatus)) {
            turno.setEstado(EstadoTurno.CONFIRMADO);
            turno.setExpiraEn(null); // Ya no necesita TTL
            log.info("✅ Turno {} CONFIRMADO. Pago {}", turno.getId(), mpPaymentId);
        } else if ("rejected".equals(mpStatus) || "cancelled".equals(mpStatus)) {
            turno.setEstado(EstadoTurno.EXPIRADO); // Libera el slot
            log.info("❌ Turno {} EXPIRADO. Pago {} rechazado.", turno.getId(), mpPaymentId);
        }

        turnoRepository.save(turno);
    }

    // ─────────────────────────────────────────
    // OBTENER TURNO POR ID (página de confirmación)
    // ─────────────────────────────────────────

    public TurnoResponse getTurnoPorId(Long id) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Turno no encontrado"));
        return toResponse(turno);
    }

    // ─────────────────────────────────────────
    // PANEL ADMIN
    // ─────────────────────────────────────────

    public List<TurnoResponse> getTurnosPorDia(LocalDate fecha) {
        LocalDateTime inicioDia = LocalDateTime.of(fecha, LocalTime.MIN);
        LocalDateTime finDia    = LocalDateTime.of(fecha, LocalTime.MAX);
        return turnoRepository.findTurnosPorDia(inicioDia, finDia)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TurnoResponse> getTurnosProximos() {
        return turnoRepository
                .findByEstadoAndFechaHoraAfterOrderByFechaHoraAsc(
                        EstadoTurno.CONFIRMADO,
                        LocalDateTime.now()
                )
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // CRON JOB: expirar turnos no pagados
    // Se ejecuta cada 5 minutos automáticamente
    // ─────────────────────────────────────────

    @Scheduled(fixedRate = 300_000) // cada 5 minutos
    @Transactional
    public void expirarTurnosVencidos() {
        int expirados = turnoRepository.expirarTurnosVencidos(LocalDateTime.now());
        if (expirados > 0) {
            log.info("🕐 {} turno(s) expirado(s) por falta de pago.", expirados);
        }
    }

    // ─────────────────────────────────────────
    // MAPPER
    // ─────────────────────────────────────────

    private TurnoResponse toResponse(Turno t) {
        return TurnoResponse.builder()
                .id(t.getId())
                .pacienteNombre(t.getPacienteNombre())
                .pacienteEmail(t.getPacienteEmail())
                .pacienteTel(t.getPacienteTel())
                .fechaHora(t.getFechaHora())
                .estado(t.getEstado())
                .monto(t.getMonto())
                .mpPaymentId(t.getMpPaymentId())
                .createdAt(t.getCreatedAt())
                .build();
    }
}

