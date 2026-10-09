package co.edu.sistematickets.modulos.adjuntos.repository;

import co.edu.sistematickets.modulos.adjuntos.dto.AdjuntoDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos de adjuntos.
 *
 * <p><strong>Estado actual: solo la interfaz.</strong> Este repositorio
 * gestiona dos cosas a la vez, y conviene tenerlo presente: la fila en la base
 * de datos, y el archivo en el sistema de archivos. La transaccion de base no
 * cubre el disco, asi que hay un caso limite real: si el proceso se cae entre
 * escribir el archivo y confirmar la fila, queda un archivo huerfano. La
 * estrategia es escribir primero el archivo y despues la fila, y tener una
 * tarea de limpieza para los huerfanos; es preferible un archivo de mas a una
 * fila que apunte a un archivo inexistente, porque eso ultimo rompe la
 * descarga del usuario.</p>
 */
public interface AdjuntoRepositorio {

    /**
     * Lista los adjuntos de un ticket.
     *
     * @param ticketId ticket del que se quieren los adjuntos
     * @return lista de adjuntos
     */
    List<AdjuntoDto> listarPorTicket(UUID ticketId);

    /**
     * Busca un adjunto por su identificador.
     *
     * @param id identificador del adjunto
     * @return el adjunto, o vacio si no existe
     */
    Optional<AdjuntoDto> buscarPorId(UUID id);

    /**
     * Busca un adjunto por el hash de su contenido.
     *
     * <p>Permite reutilizar un archivo ya subido en lugar de guardarlo dos
     * veces. Es util cuando varias personas adjuntan el mismo log o la misma
     * captura a tickets distintos.</p>
     *
     * @param hashSha256 hash del contenido
     * @return el adjunto existente, o vacio si es nuevo
     */
    Optional<AdjuntoDto> buscarPorHash(String hashSha256);

    /**
     * Persiste un adjunto nuevo.
     *
     * @param adjunto datos del adjunto
     * @return el adjunto creado
     */
    AdjuntoDto guardar(AdjuntoDto adjunto);

    /**
     * Elimina un adjunto y su archivo asociado.
     *
     * @param id identificador del adjunto
     */
    void eliminar(UUID id);

    /**
     * Cuenta cuantos adjuntos tiene un ticket.
     *
     * <p>Se consulta antes de subir para poder aplicar un limite por ticket y
     * evitar que alguien llene el disco con cientos de archivos.</p>
     *
     * @param ticketId identificador del ticket
     * @return numero de adjuntos
     */
    long contarPorTicket(UUID ticketId);
}
