package co.edu.sistematickets.modulos.tickets.service;

import co.edu.sistematickets.comun.ConflictoEstadoExcepcion;
import co.edu.sistematickets.comun.NoEncontradoExcepcion;
import co.edu.sistematickets.comun.PaginacionDto;
import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.comun.SinPermisoExcepcion;
import co.edu.sistematickets.comun.SolicitudInvalidaExcepcion;
import co.edu.sistematickets.dominio.EstadoTicket;
import co.edu.sistematickets.dominio.PrioridadTicket;
import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.tickets.dto.ActualizarTicketDto;
import co.edu.sistematickets.modulos.tickets.dto.AsignarAgenteDto;
import co.edu.sistematickets.modulos.tickets.dto.CambiarEstadoDto;
import co.edu.sistematickets.modulos.tickets.dto.CrearTicketDto;
import co.edu.sistematickets.modulos.tickets.dto.FiltrosTicketDto;
import co.edu.sistematickets.modulos.tickets.dto.TicketDto;
import co.edu.sistematickets.modulos.tickets.mapper.TicketMapper;
import co.edu.sistematickets.modulos.tickets.repository.TicketRepositorio;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Reglas de negocio del modulo de tickets.
 *
 * <p>Esta clase es el corazon del sistema. No conoce HTTP ni SQL: si se
 * quisiera usar desde una consola de linea de comandos o desde una tarea
 * programada, funcionaria igual. Esa independencia es lo que la hace testeable
 * sin levantar un servidor.</p>
 *
 * <p>Las decisiones de negocio que viven aqui, y que son las que hay que
 * defender en la sustentacion:</p>
 *
 * <ol>
 *   <li><strong>Quien puede ver que.</strong> Un solicitante solo ve sus
 *       tickets; un agente ve los asignados y los sin asignar; un
 *       administrador ve todo. El filtro se impone aqui, nunca se confia en
 *       que el cliente lo envie.</li>
 *   <li><strong>Quien puede cambiar el estado.</strong> Solo personal de
 *       soporte. Y ademas, la transicion tiene que ser legal segun la maquina
 *       de estados.</li>
 *   <li><strong>La asignacion.</strong> Solo se asigna a alguien con rol de
 *       soporte; asignar a un solicitante no tiene sentido.</li>
 *   <li><strong>Resolver exige solucion.</strong> No se puede pasar a
 *       {@code RESUELTO} sin texto de solucion, porque ese texto alimenta los
 *       informes y sin el el cierre no es auditable.</li>
 * </ol>
 *
 * <p>Todos los metodos que consultan por identificador comprueban la
 * existencia <em>antes</em> de comprobar los permisos. El orden importa por
 * seguridad: si se comprobara el permiso primero, un usuario sin acceso
 * recibiria un 403 y sabria que el recurso existe; devolviendo 404 no se
 * filtra ni eso.</p>
 */
public class TicketServicio {

    private final TicketRepositorio ticketRepositorio;
    private final TicketMapper ticketMapper;

    /**
     * @param ticketRepositorio acceso a datos de tickets
     * @param ticketMapper      utilidades de mapeo y calculo de SLA
     */
    public TicketServicio(TicketRepositorio ticketRepositorio, TicketMapper ticketMapper) {
        this.ticketRepositorio = ticketRepositorio;
        this.ticketMapper = ticketMapper;
    }

    // =================================================================
    // Consultas
    // =================================================================

    /**
     * Lista tickets visibles para el usuario que consulta.
     *
     * <p>El aislamiento por rol se aplica sobreescribiendo los filtros que
     * llegan del cliente, no anadiendo condiciones al final: para un
     * solicitante se fuerza {@code solicitanteId} a su propio identificador,
     * de modo que aunque intente consultar los de otro con un parametro, el
     * servicio lo ignora.</p>
     *
     * @param filtros      criterios de busqueda enviados por el cliente
     * @param usuarioId    identificador del usuario autenticado
     * @param rol          rol del usuario autenticado
     * @return pagina de tickets y metadatos de paginacion
     */
    public RespuestaApi<List<TicketDto>> listar(FiltrosTicketDto filtros, UUID usuarioId, RolUsuario rol) {
        filtros.normalizar();
        aplicarAislamientoPorRol(filtros, usuarioId, rol);

        List<TicketDto> pagina = ticketRepositorio.listar(filtros);
        long total = ticketRepositorio.contar(filtros);

        return RespuestaApi.paginado(pagina, filtros.getPagina(), filtros.getLimite(), total);
    }

    /**
     * Recupera un ticket concreto, comprobando que el usuario pueda verlo.
     *
     * @param id        identificador del ticket
     * @param usuarioId usuario autenticado
     * @param rol       rol del usuario autenticado
     * @return el ticket
     * @throws NoEncontradoExcepcion si no existe
     * @throws SinPermisoExcepcion   si existe pero el usuario no puede verlo
     */
    public TicketDto obtener(UUID id, UUID usuarioId, RolUsuario rol) {
        TicketDto ticket = ticketRepositorio.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoExcepcion("Ticket", id));

        if (!puedeVer(ticket, usuarioId, rol)) {
            throw new SinPermisoExcepcion("No tiene acceso a este ticket.");
        }
        return ticket;
    }

    /**
     * Devuelve el historial de cambios de un ticket.
     *
     * <p>Reutiliza {@link #obtener} para no duplicar la comprobacion de
     * existencia y permisos. Que el historial sea visible solo para quien ve el
     * ticket es una decision: contiene notas internas entre agentes.</p>
     *
     * @param id        identificador del ticket
     * @param usuarioId usuario autenticado
     * @param rol       rol del usuario autenticado
     * @return el ticket con su historial (detalle a ampliar con la persistencia)
     */
    public TicketDto obtenerHistorial(UUID id, UUID usuarioId, RolUsuario rol) {
        return obtener(id, usuarioId, rol);
    }

    /**
     * Calcula los indicadores del panel de tickets.
     *
     * <p>Se apoya en las vistas {@code vista_resumen_tickets} y
     * {@code vista_desempeno_agentes} de PostgreSQL. Delegar la agregacion en
     * la base es lo correcto: sumar en memoria obligaria a traer todas las
     * filas a la aplicacion, que es exactamente lo que hay que evitar cuando
     * la tabla crece.</p>
     *
     * @param usuarioId usuario autenticado
     * @param rol       rol del usuario autenticado
     * @return indicadores agregados (estructura por definir con la persistencia)
     */
    public Object estadisticas(UUID usuarioId, RolUsuario rol) {
        throw new UnsupportedOperationException(
                "Las estadisticas se implementaran contra las vistas de resumen al conectar la base de datos.");
    }

    // =================================================================
    // Comandos
    // =================================================================

    /**
     * Crea un ticket.
     *
     * <p>Decisiones que aplica:</p>
     * <ul>
     *   <li>El estado inicial es siempre {@code NUEVO}; no se acepta del
     *       cliente.</li>
     *   <li>Si no llega prioridad, se aplica {@code MEDIA}.</li>
     *   <li>Un solicitante no puede crear un ticket a nombre de otra persona,
     *       aunque envie {@code solicitanteId}. Se ignora el campo y se usa su
     *       identidad de sesion.</li>
     *   <li>Solo el personal de soporte puede autoasignarse el ticket al
     *       crearlo.</li>
     * </ul>
     *
     * @param datos     datos del ticket
     * @param usuarioId usuario autenticado
     * @param rol       rol del usuario autenticado
     * @return el ticket creado
     */
    public TicketDto crear(CrearTicketDto datos, UUID usuarioId, RolUsuario rol) {
        UUID solicitanteFinal = resolverSolicitante(datos.solicitanteId(), usuarioId, rol);
        UUID agenteFinal = resolverAgenteAlCrear(datos.agenteId(), rol);
        PrioridadTicket prioridad = (datos.prioridad() != null) ? datos.prioridad() : PrioridadTicket.MEDIA;

        throw new UnsupportedOperationException(
                "La creacion se completara al conectar la persistencia. "
                        + "Solicitante resuelto: " + solicitanteFinal
                        + ", agente: " + agenteFinal
                        + ", prioridad: " + prioridad);
    }

    /**
     * Actualiza los campos descriptivos de un ticket.
     *
     * <p>Solo el solicitante dueño o el personal de soporte pueden editar. Se
     * rechaza la peticion si no trae ningun campo, para que un PATCH vacio no
     * se registre como una modificacion.</p>
     *
     * @param id        identificador del ticket
     * @param cambios   campos a modificar
     * @param usuarioId usuario autenticado
     * @param rol       rol del usuario autenticado
     * @return el ticket actualizado
     */
    public TicketDto actualizar(UUID id, ActualizarTicketDto cambios, UUID usuarioId, RolUsuario rol) {
        TicketDto actual = obtener(id, usuarioId, rol);

        if (cambios.estaVacio()) {
            throw new SolicitudInvalidaExcepcion("Debe indicar al menos un campo a actualizar.");
        }
        if (actual.estado().esFinal()) {
            throw new ConflictoEstadoExcepcion(
                    "No se puede modificar un ticket en estado %s.".formatted(actual.estado()));
        }
        if (!puedeEditar(actual, usuarioId, rol)) {
            throw new SinPermisoExcepcion("No tiene permiso para modificar este ticket.");
        }

        throw new UnsupportedOperationException(
                "La actualizacion se completara al conectar la persistencia.");
    }

    /**
     * Cambia el estado de un ticket validando la maquina de estados.
     *
     * <p>Es el metodo mas importante del modulo, y el que hay que saber
     * explicar. El orden de las comprobaciones no es casual:</p>
     *
     * <ol>
     *   <li>El ticket existe y el usuario puede verlo (si no, 404 o 403).</li>
     *   <li>Solo el personal de soporte cambia estados: un solicitante no
     *       puede ni cerrar su propio ticket, porque el cierre implica una
     *       contabilidad de SLA que no le corresponde decidir.</li>
     *   <li>La transicion es legal segun el mapa de {@link EstadoTicket}. Si
     *       no lo es, se responde <strong>409</strong>, no 400: la peticion
     *       esta bien formada, lo que no encaja es el estado actual.</li>
     *   <li>Resolver exige texto de solucion. Sin el, el cierre no seria
     *       auditable ni serviria para los informes.</li>
     * </ol>
     *
     * <p>El cambio a {@code RESUELTO} es ademas el que dispara la notificacion
     * por correo y SMS. Esa llamada no vive aqui: el servicio de tickets
     * publica el hecho y el modulo de notificaciones reacciona. Si el envio
     * falla, el cambio de estado ya esta confirmado y la notificacion queda en
     * la cola para reintento; no se pierde ni bloquea al usuario.</p>
     *
     * @param id      identificador del ticket
     * @param datos   estado destino, solucion y observacion
     * @param usuarioId usuario autenticado
     * @param rol     rol del usuario autenticado
     * @return el ticket ya en su nuevo estado
     * @throws ConflictoEstadoExcepcion si la transicion no es legal
     */
    public TicketDto cambiarEstado(UUID id, CambiarEstadoDto datos, UUID usuarioId, RolUsuario rol) {
        TicketDto actual = obtener(id, usuarioId, rol);
        EstadoTicket estadoNuevo = datos.estado();

        if (!rol.esPersonalDeSoporte()) {
            throw new SinPermisoExcepcion("Solo un agente o un administrador puede cambiar el estado de un ticket.");
        }

        if (!ticketMapper.esTransicionValida(actual.estado(), estadoNuevo)) {
            throw new ConflictoEstadoExcepcion(
                    ticketMapper.describirTransicionIlegal(actual.estado(), estadoNuevo));
        }

        if (estadoNuevo == EstadoTicket.RESUELTO) {
            exigirSolucion(datos.solucion());
        }

        throw new UnsupportedOperationException(
                "El cambio de estado se completara al conectar la persistencia. "
                        + "Transicion validada: " + actual.estado() + " -> " + estadoNuevo);
    }

    /**
     * Asigna o reasigna el agente de un ticket.
     *
     * <p>{@code agenteId} nulo significa desasignar, y es una operacion
     * valida: devuelve el ticket a la cola comun, lo que hace falta cuando un
     * agente se ausenta. En ese caso no se comprueba el rol del destinatario,
     * porque no hay destinatario.</p>
     *
     * @param id        identificador del ticket
     * @param datos     agente destino y observacion
     * @param usuarioId usuario autenticado
     * @param rol       rol del usuario autenticado
     * @return el ticket con el agente actualizado
     */
    public TicketDto asignarAgente(UUID id, AsignarAgenteDto datos, UUID usuarioId, RolUsuario rol) {
        TicketDto actual = obtener(id, usuarioId, rol);

        if (!rol.esPersonalDeSoporte()) {
            throw new SinPermisoExcepcion("Solo un agente o un administrador puede asignar tickets.");
        }
        if (actual.estado().esFinal()) {
            throw new ConflictoEstadoExcepcion(
                    "No se puede reasignar un ticket en estado %s.".formatted(actual.estado()));
        }
        if (datos.agenteId() != null) {
            // Aqui se comprobara contra el repositorio de usuarios que el
            // destinatario exista y tenga rol de soporte. Se deja escrito para
            // que la regla no se pierda al implementar la persistencia.
            throw new UnsupportedOperationException(
                    "La comprobacion del destinatario se implementara con el repositorio de usuarios.");
        }

        throw new UnsupportedOperationException(
                "La asignacion se completara al conectar la persistencia.");
    }

    /**
     * Elimina logicamente un ticket.
     *
     * <p>Solo un administrador puede hacerlo, y nunca se borra la fila: se
     * escribe la fecha en {@code eliminado_en}. Un ticket que desaparece de
     * verdad hace imposible auditar que paso con el.</p>
     *
     * @param id        identificador del ticket
     * @param usuarioId usuario autenticado
     * @param rol       rol del usuario autenticado
     */
    public void eliminar(UUID id, UUID usuarioId, RolUsuario rol) {
        obtener(id, usuarioId, rol);

        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new SinPermisoExcepcion("Solo un administrador puede eliminar un ticket.");
        }
        ticketRepositorio.eliminarLogicamente(id);
    }

    // =================================================================
    // Reglas de acceso (privadas: son detalle de implementacion)
    // =================================================================

    /**
     * Impone el filtro de visibilidad segun el rol.
     *
     * <p>Sobreescribe lo que venga del cliente en lugar de anadir condiciones:
     * si un solicitante envia {@code solicitanteId} de otra persona, se
     * ignora y se usa el suyo. Asi el parametro no se convierte en una via
     * para leer datos ajenos.</p>
     *
     * @param filtros   filtros a ajustar (se modifican en el sitio)
     * @param usuarioId usuario autenticado
     * @param rol       rol del usuario autenticado
     */
    private void aplicarAislamientoPorRol(FiltrosTicketDto filtros, UUID usuarioId, RolUsuario rol) {
        switch (rol) {
            case SOLICITANTE -> {
                // Solo lo suyo: se ignora cualquier otro filtro de propiedad.
                filtros.setSolicitanteId(usuarioId);
                filtros.setAgenteId(null);
            }
            case AGENTE -> {
                // Lo asignado a el y lo que sigue sin dueño.
                // El repositorio lo interpretara como "agente_id = :id OR agente_id IS NULL".
                filtros.setSolicitanteId(null);
            }
            case ADMINISTRADOR -> {
                // Sin restricciones: puede filtrar por cualquier campo.
            }
        }
    }

    /**
     * Determina si un usuario puede ver un ticket concreto.
     *
     * @param ticket    ticket en cuestion
     * @param usuarioId usuario autenticado
     * @param rol       rol del usuario autenticado
     * @return {@code true} si puede verlo
     */
    private boolean puedeVer(TicketDto ticket, UUID usuarioId, RolUsuario rol) {
        return switch (rol) {
            // El administrador ve todo; el agente ve lo suyo y lo sin asignar.
            case ADMINISTRADOR -> true;
            case AGENTE -> usuarioId.equals(ticket.agenteId()) || ticket.agenteId() == null;
            // El solicitante solo ve lo que el mismo reporto.
            case SOLICITANTE -> usuarioId.equals(ticket.solicitanteId());
        };
    }

    /**
     * Determina si un usuario puede editar un ticket.
     *
     * <p>Un agente solo edita lo que tiene asignado; el administrador, todo.
     * Un solicitante puede corregir su propio ticket, pero solo mientras no
     * este resuelto (eso se comprueba en {@link #actualizar}).</p>
     *
     * @param ticket    ticket en cuestion
     * @param usuarioId usuario autenticado
     * @param rol       rol del usuario autenticado
     * @return {@code true} si puede editarlo
     */
    private boolean puedeEditar(TicketDto ticket, UUID usuarioId, RolUsuario rol) {
        return switch (rol) {
            case ADMINISTRADOR -> true;
            case AGENTE -> usuarioId.equals(ticket.agenteId());
            case SOLICITANTE -> usuarioId.equals(ticket.solicitanteId());
        };
    }

    /**
     * Resuelve quien sera el solicitante efectivo del ticket.
     *
     * @param solicitanteSolicitado valor enviado por el cliente; puede ser nulo
     * @param usuarioId             usuario autenticado
     * @param rol                   rol del usuario autenticado
     * @return identificador del solicitante a usar
     * @throws SinPermisoExcepcion si un no administrador intenta crear a nombre de otro
     */
    private UUID resolverSolicitante(UUID solicitanteSolicitado, UUID usuarioId, RolUsuario rol) {
        if (solicitanteSolicitado == null || solicitanteSolicitado.equals(usuarioId)) {
            return usuarioId;
        }
        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new SinPermisoExcepcion(
                    "Solo un administrador puede crear un ticket a nombre de otra persona.");
        }
        return solicitanteSolicitado;
    }

    /**
     * Resuelve si el ticket nace ya asignado.
     *
     * @param agenteSolicitado agente enviado por el cliente; puede ser nulo
     * @param rol              rol del usuario autenticado
     * @return identificador del agente, o {@code null} si nace sin asignar
     * @throws SinPermisoExcepcion si un solicitante intenta autoasignarse
     */
    private UUID resolverAgenteAlCrear(UUID agenteSolicitado, RolUsuario rol) {
        if (agenteSolicitado == null) {
            return null;
        }
        if (!rol.esPersonalDeSoporte()) {
            throw new SinPermisoExcepcion(
                    "Solo un agente o un administrador puede asignar un ticket al crearlo.");
        }
        return agenteSolicitado;
    }

    /**
     * Exige texto de solucion al pasar a resuelto.
     *
     * @param solucion texto enviado por el cliente
     * @throws SolicitudInvalidaExcepcion si viene vacio
     */
    private void exigirSolucion(String solucion) {
        if (solucion == null || solucion.isBlank()) {
            throw new SolicitudInvalidaExcepcion(
                    "Para marcar un ticket como RESUELTO debe indicar la solucion aplicada.");
        }
    }
}
