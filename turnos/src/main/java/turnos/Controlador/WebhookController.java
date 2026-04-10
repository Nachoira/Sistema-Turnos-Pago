package turnos.Controlador;

import com.mercadopago.resources.payment.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import turnos.Servicio.MercadoPagoService;
import turnos.Servicio.TurnoService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class WebhookController {

    private final TurnoService turnoService;
    private final MercadoPagoService mercadoPagoService;

    // POST /api/webhook/mercadopago
    // Mercado Pago llama a este endpoint cuando el estado de un pago cambia
    @PostMapping("/mercadopago")
    public ResponseEntity<Void> recibirWebhook(
            @RequestBody Map<String, Object> payload,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "data.id", required = false) String dataId
    ) {
        log.info("Webhook MP recibido - type: {}, payload: {}", type, payload);

        try {
            // MP puede mandar la notificación de dos formas distintas
            String tipoNotificacion = type != null ? type : (String) payload.get("type");
            String paymentIdStr = dataId != null ? dataId
                    : extractPaymentId(payload);

            // Solo nos interesan las notificaciones de pago
            if (!"payment".equals(tipoNotificacion) || paymentIdStr == null) {
                return ResponseEntity.ok().build();
            }

            Long paymentId = Long.parseLong(paymentIdStr);

            // ⚠️ IMPORTANTE: consultamos el estado real del pago a la API de MP
            // Nunca confiamos solo en los datos del webhook
            Payment payment = mercadoPagoService.consultarPago(paymentId);

            String preferenceId = payment.getExternalReference();
            String status = payment.getStatus().toString().toLowerCase();

            log.info("Pago {} - preferencia: {} - status: {}", paymentId, preferenceId, status);

            turnoService.confirmarTurno(preferenceId, String.valueOf(paymentId), status);

        } catch (Exception e) {
            // Devolvemos 200 igual para que MP no reintente indefinidamente
            // Los errores los vemos en los logs
            log.error("Error procesando webhook MP: {}", e.getMessage(), e);
        }

        // MP necesita un 200 para saber que recibió la notificación
        return ResponseEntity.ok().build();
    }

    // MP a veces hace GET para verificar que el endpoint existe
    @GetMapping("/mercadopago")
    public ResponseEntity<String> verificar() {
        return ResponseEntity.ok("ok");
    }

    private String extractPaymentId(Map<String, Object> payload) {
        try {
            Object data = payload.get("data");
            if (data instanceof Map) {
                Object id = ((Map<?, ?>) data).get("id");
                return id != null ? id.toString() : null;
            }
        } catch (Exception e) {
            log.warn("No se pudo extraer payment_id del payload");
        }
        return null;
    }
}