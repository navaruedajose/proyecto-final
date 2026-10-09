package co.edu.sistematickets.comun;

import org.springframework.http.HttpStatus;

/**
 * Los datos enviados no superan la validacion de negocio (HTTP 400).
 *
 * <p>Se distingue de las validaciones de formato que hace Bean Validation
 * directamente sobre el DTO: esta excepcion se lanza desde el servicio cuando
 * el problema requiere conocer el estado del sistema (por ejemplo, fechas
 * incoherentes entre si).</p>
 */
public class SolicitudInvalidaExcepcion extends ExcepcionAplicacion {

    /**
     * @param mensaje descripcion del problema
     */
    public SolicitudInvalidaExcepcion(String mensaje) {
        super(mensaje, HttpStatus.BAD_REQUEST, "SOLICITUD_INVALIDA");
    }
}
