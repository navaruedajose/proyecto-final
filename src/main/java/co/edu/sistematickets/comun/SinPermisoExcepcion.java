package co.edu.sistematickets.comun;

import org.springframework.http.HttpStatus;

/**
 * El usuario está autenticado pero no tiene permiso (HTTP 403).
 *
 * <p>Se diferencia de la falta de sesión (HTTP 401): aquí el sistema sabe
 * quién eres, y aún así no puedes hacer esto. Por ejemplo, un solicitante
 * intentando cambiar el estado de un ticket que no es suyo.</p>
 */
public class SinPermisoExcepcion extends ExcepcionAplicacion {

    /**
     * @param mensaje explicación del permiso que falta
     */
    public SinPermisoExcepcion(String mensaje) {
        super(mensaje, HttpStatus.FORBIDDEN, "SIN_PERMISO");
    }
}
