package co.edu.sistematickets.comun;

import org.springframework.http.HttpStatus;

/**
 * El usuario esta autenticado pero no tiene permiso (HTTP 403).
 *
 * <p>Se diferencia de la falta de sesion (HTTP 401): aqui el sistema sabe
 * quien eres, y aun asi no puedes hacer esto. Por ejemplo, un solicitante
 * intentando cambiar el estado de un ticket que no es suyo.</p>
 */
public class SinPermisoExcepcion extends ExcepcionAplicacion {

    /**
     * @param mensaje explicacion del permiso que falta
     */
    public SinPermisoExcepcion(String mensaje) {
        super(mensaje, HttpStatus.FORBIDDEN, "SIN_PERMISO");
    }
}
