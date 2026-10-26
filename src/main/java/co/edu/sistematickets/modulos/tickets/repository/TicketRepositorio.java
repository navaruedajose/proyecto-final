package co.edu.sistematickets.modulos.tickets.repository;

import co.edu.sistematickets.modulos.tickets.dto.FiltrosTicketDto;
import co.edu.sistematickets.modulos.tickets.dto.TicketDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos de tickets.
 *
 * <p><strong>Estado actual: solo la interfaz.</strong> No hay implementación
 * porque el alcance de esta entrega es la estructura. Cuando se implemente la
 * persistencia, lo normal es:</p>
 *
 * <pre>
 *   public interface TicketRepository extends JpaRepository&lt;TicketEntidad, UUID&gt; { ... }
 * </pre>
 *
 * <p>Y Spring genera la implementación en tiempo de arranque. Las consultas
 * con filtros dinamicos se resuelven con Specification, Criteria API o una
 * consulta JPQL, porque combinar ocho filtros opcionales con el nombre
 * derivado del método ({@code findByEstadoAndPrioridadAnd...}) es inviable.</p>
 *
 * <p><strong>Por que los métodos devuelven DTOs y no entidades:</strong> la
 * frontera de la persistencia es el mejor lugar para proyectar. Si el
 * repositorio devuelve ya el DTO, el servicio no puede filtrar por accidente
 * un campo que no debe salir, y la consulta puede traer solo las columnas
 * necesarias en lugar de la fila completa. El coste es que el repositorio
 * conoce al DTO; el beneficio es que la entidad nunca sale de la capa de
 * datos.</p>
 */
public interface TicketRepositorio {

    /**
     * Busca un ticket por su identificador interno.
     *
     * @param id identificador del ticket
     * @return el ticket, o {@link Optional#empty()} si no existe o está borrado
     */
    Optional<TicketDto> buscarPorId(UUID id);

    /**
     * Busca un ticket por su folio legible.
     *
     * @param código folio con formato {@code TK-AAAA-000000}
     * @return el ticket, o vacío si no existe
     */
    Optional<TicketDto> buscarPorCodigo(String codigo);

    /**
     * Lista tickets aplicando los filtros y la paginación indicados.
     *
     * <p>El aislamiento por rol <strong>no</strong> es responsabilidad de este
     * método: el servicio decide que filtros adicionales imponer (por ejemplo,
     * forzar {@code solicitanteId} al usuario de la sesión) antes de llamar
     * aquí. Así el repositorio es agnóstico a quien pregunta, y la regla de
     * quien puede ver que queda en una sola capa.</p>
     *
     * @param filtros criterios de búsqueda, paginación y ordenación
     * @return página de tickets; lista vacía si nada coincide
     */
    List<TicketDto> listar(FiltrosTicketDto filtros);

    /**
     * Cuenta el total de tickets que cumplen los filtros.
     *
     * <p>Se separa de {@link #listar} porque contar y traer son dos consultas
     * distintas: una devuelve una página y la otra un único número. Mezclarlas
     * obligaría a traer todas las filas solo para contarlas.</p>
     *
     * @param filtros criterios de búsqueda (se ignoran página y límite)
     * @return total de coincidencias
     */
    long contar(FiltrosTicketDto filtros);

    /**
     * Persiste un ticket nuevo y devuelve su representación de lectura.
     *
     * @param ticket datos del ticket a crear
     * @return el ticket creado, ya con folio y fechas generados
     */
    TicketDto guardar(TicketDto ticket);

    /**
     * Actualiza un ticket existente.
     *
     * @param id     identificador del ticket
     * @param ticket datos a persistir
     * @return el ticket actualizado
     */
    TicketDto actualizar(UUID id, TicketDto ticket);

    /**
     * Marca un ticket como eliminado (borrado lógico).
     *
     * <p>Nunca se borra fisicamente: escribir la fecha en {@code eliminado_en}
     * conserva la trazabilidad que exige el sistema. Un ticket que desaparece
     * de verdad hace imposible auditar que paso con el.</p>
     *
     * @param id identificador del ticket
     */
    void eliminarLogicamente(UUID id);

    /**
     * Indica si existe un ticket con ese folio.
     *
     * @param código folio a comprobar
     * @return {@code true} si ya está en uso
     */
    boolean existePorCodigo(String codigo);
}
