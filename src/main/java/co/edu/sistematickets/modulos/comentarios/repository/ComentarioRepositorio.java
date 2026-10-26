package co.edu.sistematickets.modulos.comentarios.repository;

import co.edu.sistematickets.modulos.comentarios.dto.ComentarioDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos de comentarios.
 *
 * <p><strong>Estado actual: solo la interfaz.</strong></p>
 */
public interface ComentarioRepositorio {

    /**
     * Lista los comentarios de un ticket, del más antiguo al más reciente.
     *
     * <p>El orden cronológico ascendente es el correcto para una conversación:
     * se lee de arriba abajo. Ordenar al revés obligaría al usuario a leer de
     * abajo arriba.</p>
     *
     * @param ticketId    ticket del que se quieren los comentarios
     * @param incluirInternos si {@code false}, omite las notas privadas
     * @return lista de comentarios
     */
    List<ComentarioDto> listarPorTicket(UUID ticketId, boolean incluirInternos);

    /**
     * Busca un comentario por su identificador.
     *
     * @param id identificador del comentario
     * @return el comentario, o vacío si no existe
     */
    Optional<ComentarioDto> buscarPorId(UUID id);

    /**
     * Persiste un comentario nuevo.
     *
     * @param comentario datos del comentario
     * @return el comentario creado
     */
    ComentarioDto guardar(ComentarioDto comentario);

    /**
     * Elimina un comentario.
     *
     * <p>Aquí si se borra fisicamente, al contrario que en tickets y usuarios.
     * El motivo es que un comentario no es una entidad de negocio con
     * implicaciones contables: si alguien se equivoca al publicar, corregirlo
     * sin dejar rastro es razonable. Los adjuntos asociados se borran en
     * cascada por la clave foranea.</p>
     *
     * @param id identificador del comentario
     */
    void eliminar(UUID id);
}
