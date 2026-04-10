package turnos.Servicio;

import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.payment.Payment;
import com.mercadopago.resources.preference.Preference;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import turnos.Modelo.Turno;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    @Value("${mp.access.token}")
    private String accessToken;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${backend.url}")
    private String backendUrl;

    // Inicializa el SDK con el access token al arrancar la app
    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
        log.info("✅ Mercado Pago SDK inicializado.");
    }

    // Crea una preferencia de pago y devuelve los URLs de checkout
    public PreferenciaResult crearPreferencia(Turno turno) {
        try {
            PreferenceItemRequest item = PreferenceItemRequest.builder()
                    .id(String.valueOf(turno.getId()))
                    .title("Consulta médica - " + turno.getFechaHora().toLocalDate())
                    .quantity(1)
                    .unitPrice(turno.getMonto())
                    .currencyId("ARS")
                    .build();

            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success(frontendUrl + "/turno/confirmado?id=" + turno.getId())
                    .failure(frontendUrl + "/turno/fallido?id=" + turno.getId())
                    .pending(frontendUrl + "/turno/pendiente?id=" + turno.getId())
                    .build();

            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(List.of(item))
                    .backUrls(backUrls).build();


            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            return new PreferenciaResult(
                    preference.getId(),
                    preference.getInitPoint(),
                    preference.getSandboxInitPoint()
            );

        }catch (MPApiException e) {
            log.error("MP API error - status: {}, content: {}",
                    e.getApiResponse().getStatusCode(),
                    e.getApiResponse().getContent());
            throw new RuntimeException("Error MP: " + e.getApiResponse().getContent());
        } catch (Exception e) {
            log.error("Error general MP: {}", e.getMessage(), e);
            throw new RuntimeException("Error al conectar con Mercado Pago: " + e.getMessage());
        }
    }

    // Consulta el estado real de un pago directamente a la API de MP
    // ⚠️ Siempre verificar acá, nunca confiar solo en los datos del webhook
    public Payment consultarPago(Long paymentId) {
        try {
            PaymentClient client = new PaymentClient();
            return client.get(paymentId);
        } catch (Exception e) {
            log.error("Error al consultar pago {}: {}", paymentId, e.getMessage());
            throw new RuntimeException("Error al consultar pago en Mercado Pago");
        }
    }

    // DTO interno para devolver los datos de la preferencia creada
    @Data
    public static class PreferenciaResult {
        private final String preferenceId;
        private final String initPoint;
        private final String sandboxInitPoint;
    }
}
