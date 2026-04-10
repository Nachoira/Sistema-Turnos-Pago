package turnos.Repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import turnos.Modelo.Configuracion;

@Repository
public interface ConfiguracionRepository extends JpaRepository<Configuracion, String> {
    // La clave primaria es String (ej: "monto_sesion")
    // findById("monto_sesion") ya viene gratis con JpaRepository
}
