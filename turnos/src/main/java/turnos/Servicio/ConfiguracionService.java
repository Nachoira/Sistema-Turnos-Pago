package turnos.Servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import turnos.Modelo.Configuracion;
import turnos.Repositorio.ConfiguracionRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    public  String get(String clave) {
        return configuracionRepository.findById(clave)
                .map(Configuracion::getValor)
                .orElseThrow(() -> new RuntimeException("Config no encontrada: " + clave));
    }

    public String get(String clave, String valorDefault) {
        return configuracionRepository.findById(clave)
                .map(Configuracion::getValor)
                .orElse(valorDefault);
    }

    public  BigDecimal getMonto() {
        return new BigDecimal(get("monto_sesion"));
    }

    public String getNombreConsultorio() {
        return get("nombre_consultorio");
    }

    public String getNombreDoctora() {
        return get("nombre_doctora");
    }

    public String getDireccion() {
        return get("direccion");
    }

    public int getDuracionTurnoMin() {
        return Integer.parseInt(get("duracion_turno_min", "30"));
    }

    public String getHoraInicio() {
        return get("hora_inicio", "09:00");
    }

    public String getHoraFin() {
        return get("hora_fin", "18:00");
    }
}

