package co.edu.sistematickets.comun;

import org.springframework.http.HttpStatus;

/**
 * La operación choca con el estado actual del recurso (HTTP 409).
 *
 * <p>Es la excepción clave del sistema: se lanza cuando se intenta una
 * transición de estado que la máquina de estados no permite. El caso típico es
 * intentar cerrar un ticket que ya estaba cerrado.</p>
 *
 * <p>El código es {@code 409} y no {@code 400} a propósito. La petición esta
 * bien formada y el estado destino es válido <em>por si mismo</em>; lo que no
 * encaja es el estado <em>actual</em> del recurso. Devolver 400 confundiria al
 * cliente y le haría corregir un dato que no tiene nada de malo. Esta
 * distinción es la que documenta la diferencia entre «has escrito mal algo» y
 * «no puedes hacer eso ahora».</p>
 */
public class ConflictoEstadoExcepcion extends ExcepcionAplicacion {

    /**
     * @param mensaje descripción del conflicto, con los estados implicados
     */
    public ConflictoEstadoExcepcion(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT, "CONFLICTO_ESTADO");
    }
}
