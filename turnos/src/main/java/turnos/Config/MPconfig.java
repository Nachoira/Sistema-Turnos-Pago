package turnos.Config;

import com.mercadopago.MercadoPagoConfig; // <--- IMPORTANTE: La clase del SDK
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class MPconfig { // Tu clase de configuración se llama MPconfig (con 'c' minúscula)

    @Value("${mp.access.token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        try {
            // USAMOS la clase del SDK oficial, NO 'MPconfig'
            MercadoPagoConfig.setAccessToken(accessToken);
            log.info("✅ Mercado Pago SDK inicializado correctamente.");
        } catch (Exception e) {
            log.error("❌ Error al inicializar el SDK de Mercado Pago: {}", e.getMessage());
        }
    }
}