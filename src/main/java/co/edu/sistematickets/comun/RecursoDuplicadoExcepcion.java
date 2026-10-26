package co.edu.sistematickets.comun;

import org.springframework.http.HttpStatus;

/**
 * El recurso ya existe y no admite duplicados (HTTP 409).
 *
 * <p>Se lanza, por ejemplo, al registrar un correo que ya está en uso o al
 * crear una categoría con un nombre repetido. Es un conflicto de unicidad, no
 * de estado; comparten código HTTP pero son causas distintas y por eso son
 * excepciones distintas.</p>
 */
public class RecursoDuplicadoExcepcion extends ExcepcionAplicacion {

    /**
     * @param recurso nombre del tipo de recurso
     * @param campo   campo que debe ser único
     * @param valor   valor que ya estaba en uso
     */
    public RecursoDuplicadoExcepcion(String recurso, String campo, Object valor) {
        super("Ya existe un %s con %s = %s.".formatted(recurso, campo, valor),
                HttpStatus.CONFLICT, "RECURSO_DUPLICADO");
    }
}
