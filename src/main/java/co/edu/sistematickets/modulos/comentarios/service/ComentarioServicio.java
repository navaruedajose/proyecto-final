package co.edu.sistematickets.modulos.comentarios.service;

import co.edu.sistematickets.comun.NoEncontradoExcepcion;
import co.edu.sistematickets.comun.SinPermisoExcepcion;
import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.comentarios.dto.ComentarioDto;
import co.edu.sistematickets.modulos.comentarios.repository.ComentarioRepositorio;

import java.util.List;
import java.util.UUID;

/**
 * Reglas de negocio de comentarios.
 *
 * <p>El punto delicado de este módulo es el filtrado de las notas internas. La
 * regla es sencilla de enunciar y fácil de romper sin darse cuenta:</p>
 *
 * <p><strong>Un solicitante nunca ve los comentarios marcados como internos.</strong>
 * El filtro se aplica al consultar, no al devolver: se pide al repositorio que
 * los excluya en lugar de traerlos todos y descartarlos después. Filtrar en
 * memoria funciona, pero deja los datos sensibles en el proceso un instante
 * de más y, sobre todo, depende de que nadie olvide el descarte.</p>
 *
 * <p>Regla complementaria: solo el personal de soporte puede crear notas
 * internas. Si un solicitante pudiera marcarlas, el campo perderia su
 * significado y acabaría ocultando información al propio agente.</p>
 */
public class ComentarioServicio {

    private final ComentarioRepositorio comentarioRepositorio;

    /**
     * @param comentarioRepositorio acceso a datos de comentarios
     */
    public ComentarioServicio(ComentarioRepositorio comentarioRepositorio) {
        this.comentarioRepositorio = comentarioRepositorio;
    }

    /**
     * Lista los comentarios visibles para quien consulta.
     *
     * @param ticketId ticket del que se quieren los comentarios
     * @param rol      rol del usuario que consulta
     * @return comentarios, sin las notas internas si es un solicitante
     */
    public List<ComentarioDto> listar(UUID ticketId, RolUsuario rol) {
        boolean incluirInternos = rol.esPersonalDeSoporte();
        return comentarioRepositorio.listarPorTicket(ticketId, incluirInternos);
    }

    /**
     * Publica un comentario.
     *
     * @param ticketId ticket al que se comenta
     * @param datos    contenido del comentario
     * @param autorId  usuario que comenta
     * @param rol      rol del usuario
     * @return el comentario creado
     * @throws SinPermisoExcepcion si un solicitante intenta crear una nota interna
     */
    public ComentarioDto crear(UUID ticketId, ComentarioDto datos, UUID autorId, RolUsuario rol) {
        if (Boolean.TRUE.equals(datos.esInterno()) && !rol.esPersonalDeSoporte()) {
            throw new SinPermisoExcepcion(
                    "Solo un agente o un administrador puede crear notas internas.");
        }

        throw new UnsupportedOperationException(
                "La publicación se completara al conectar la persistencia. "
                        + "Nota interna solicitada: " + datos.esInterno());
    }

    /**
     * Elimina un comentario.
     *
     * <p>Solo el autor o un administrador. Que un agente pudiera borrar los
     * comentarios de otro borraria evidencia de la conversación.</p>
     *
     * <p><strong>Por que se comprueba la pertenencia antes que el permiso.</strong>
     * La ruta es anidada ({@code /tickets/{ticketId}/comentarios/{comentarioId}}),
     * así que el cliente afirma dos cosas a la vez: que el comentario existe y
     * que pertenece a ese ticket. Si solo se comprobara el permiso, un usuario
     * legitimado en el ticket A podría borrar un comentario del ticket B
     * enviando el identificador correcto bajo la ruta del ticket A. La
     * comprobación de pertenencia cierra ese hueco.</p>
     *
     * <p>El orden importa: primero la existencia, luego la pertenencia y por
     * último el permiso. Así una ruta con un {@code ticketId} ajeno devuelve
     * 404 sin revelar si el comentario existe en otro ticket, y solo quien
     * tiene derecho a ver el comentario llega a la comprobación de permisos.</p>
     *
     * @param ticketId     ticket al que pertenece
     * @param comentarioId comentario a borrar
     * @param usuarioId    usuario que borra
     * @param rol          rol del usuario
     * @throws NoEncontradoExcepcion si el comentario no existe o no es de ese ticket
     * @throws SinPermisoExcepcion   si no es el autor ni un administrador
     */
    public void eliminar(UUID ticketId, UUID comentarioId, UUID usuarioId, RolUsuario rol) {
        ComentarioDto comentario = comentarioRepositorio.buscarPorId(comentarioId)
                .orElseThrow(() -> new NoEncontradoExcepcion("Comentario", comentarioId));

        // La ruta promete que el comentario es de este ticket. Si no lo es, la
        // petición apunta a un recurso que no existe: 404, sin filtrar que el
        // comentario si exista en otro ticket.
        if (ticketId == null || !ticketId.equals(comentario.ticketId())) {
            throw new NoEncontradoExcepcion("Comentario", comentarioId);
        }

        boolean esAutor = usuarioId != null && usuarioId.equals(comentario.autorId());
        if (!esAutor && rol != RolUsuario.ADMINISTRADOR) {
            throw new SinPermisoExcepcion("Solo el autor o un administrador puede eliminar un comentario.");
        }
        comentarioRepositorio.eliminar(comentarioId);
    }
}
