package co.edu.sistematickets.comun;

import org.springframework.http.HttpStatus;

/**
 * El recurso solicitado no existe (HTTP 404).
 */
public class NoEncontradoExcepcion extends ExcepcionAplicacion {

    /**
     * @param recurso nombre del tipo de recurso, por ejemplo {@code Ticket}
     * @param id      identificador buscado
     */
    public NoEncontradoExcepcion(String recurso, Object id) {
        super("%s no encontrado con id %s.".formatted(recurso, id),
                HttpStatus.NOT_FOUND, "NO_ENCONTRADO");
    }

    /**
     * @param mensaje mensaje ya redactado
     */
    public NoEncontradoExcepcion(String mensaje) {
        super(mensaje, HttpStatus.NOT_FOUND, "NO_ENCONTRADO");
    }
}
