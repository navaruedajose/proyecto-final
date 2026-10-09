package co.edu.sistematickets.comun;

import org.springframework.http.HttpStatus;

/**
 * La operacion choca con el estado actual del recurso (HTTP 409).
 *
 * <p>Es la excepcion clave del sistema: se lanza cuando se intenta una
 * transicion de estado que la maquina de estados no permite. El caso tipico es
 * intentar cerrar un ticket que ya estaba cerrado.</p>
 *
 * <p>El codigo es {@code 409} y no {@code 400} a proposito. La peticion esta
 * bien formada y el estado destino es valido <em>por si mismo</em>; lo que no
 * encaja es el estado <em>actual</em> del recurso. Devolver 400 confundiria al
 * cliente y le haria corregir un dato que no tiene nada de malo. Esta
 * distincion es la que documenta la diferencia entre «has escrito mal algo» y
 * «no puedes hacer eso ahora».</p>
 */
public class ConflictoEstadoExcepcion extends ExcepcionAplicacion {

    /**
     * @param mensaje descripcion del conflicto, con los estados implicados
     */
    public ConflictoEstadoExcepcion(String mensaje) {
        super(mensaje, HttpStatus.CONFLICT, "CONFLICTO_ESTADO");
    }
}
