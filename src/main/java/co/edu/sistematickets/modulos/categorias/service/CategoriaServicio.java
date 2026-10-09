package co.edu.sistematickets.modulos.categorias.service;

import co.edu.sistematickets.comun.NoEncontradoExcepcion;
import co.edu.sistematickets.comun.RecursoDuplicadoExcepcion;
import co.edu.sistematickets.comun.SinPermisoExcepcion;
import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.categorias.dto.CategoriaDto;
import co.edu.sistematickets.modulos.categorias.repository.CategoriaRepositorio;

import java.util.List;
import java.util.UUID;

/**
 * Reglas de negocio de categorias.
 *
 * <p>Parece un modulo de mantenimiento sencillo, pero tiene dos reglas que
 * importan:</p>
 *
 * <ol>
 *   <li><strong>El nombre es unico.</strong> Del nombre depende el SLA, y dos
 *       categorias que se llamen casi igual parten los informes en dos. Se
 *       comprueba antes de insertar para devolver un error claro en lugar de
 *       dejar que salte la restriccion de la base.</li>
 *   <li><strong>Desactivar no borra.</strong> Se comprueba cuantos tickets
 *       activos usan la categoria y se informa, porque desactivar una
 *       categoria con incidencias abiertas deja esos tickets sin SLA
 *       aplicable.</li>
 * </ol>
 *
 * <p>Solo un administrador puede crear, modificar o desactivar categorias. Que
 * un agente pudiera cambiarlas alteraria los SLA de todo el equipo, que no es
 * una decision suya.</p>
 */
public class CategoriaServicio {

    private final CategoriaRepositorio categoriaRepositorio;

    /**
     * @param categoriaRepositorio acceso a datos de categorias
     */
    public CategoriaServicio(CategoriaRepositorio categoriaRepositorio) {
        this.categoriaRepositorio = categoriaRepositorio;
    }

    /**
     * Lista categorias.
     *
     * <p>La consulta esta abierta a cualquier usuario autenticado: el
     * formulario de creacion de ticket necesita mostrar las categorias
     * disponibles para que el usuario elija.</p>
     *
     * @param soloActivas si es {@code true}, omite las desactivadas
     * @return lista de categorias
     */
    public List<CategoriaDto> listar(boolean soloActivas) {
        return categoriaRepositorio.listar(soloActivas);
    }

    /**
     * Devuelve una categoria por su identificador.
     *
     * @param id identificador de la categoria
     * @return la categoria
     * @throws NoEncontradoExcepcion si no existe
     */
    public CategoriaDto obtener(UUID id) {
        return categoriaRepositorio.buscarPorId(id)
                .orElseThrow(() -> new NoEncontradoExcepcion("Categoria", id));
    }

    /**
     * Crea una categoria.
     *
     * @param datos datos de la categoria
     * @param rol   rol de quien realiza la operacion
     * @return la categoria creada
     * @throws SinPermisoExcepcion       si no es administrador
     * @throws RecursoDuplicadoExcepcion si el nombre ya esta en uso
     */
    public CategoriaDto crear(CategoriaDto datos, RolUsuario rol) {
        exigirAdministrador(rol);

        categoriaRepositorio.buscarPorNombre(datos.nombre()).ifPresent(existente -> {
            throw new RecursoDuplicadoExcepcion("categoria", "nombre", datos.nombre());
        });

        throw new UnsupportedOperationException(
                "La creacion se completara al conectar la persistencia.");
    }

    /**
     * Actualiza una categoria.
     *
     * <p>Al cambiar el SLA, los tickets ya abiertos de esa categoria mantienen
     * la fecha limite que se les calculo al crearse. Es una decision
     * deliberada: recalcularla hacia atras moveria incumplimientos historicos
     * y falsearia los informes de meses ya cerrados.</p>
     *
     * @param id    identificador de la categoria
     * @param datos datos a persistir
     * @param rol   rol de quien realiza la operacion
     * @return la categoria actualizada
     */
    public CategoriaDto actualizar(UUID id, CategoriaDto datos, RolUsuario rol) {
        exigirAdministrador(rol);
        obtener(id);

        categoriaRepositorio.buscarPorNombre(datos.nombre()).ifPresent(existente -> {
            if (!existente.id().equals(id)) {
                throw new RecursoDuplicadoExcepcion("categoria", "nombre", datos.nombre());
            }
        });

        throw new UnsupportedOperationException(
                "La actualizacion se completara al conectar la persistencia.");
    }

    /**
     * Desactiva una categoria.
     *
     * @param id  identificador de la categoria
     * @param rol rol de quien realiza la operacion
     * @return numero de tickets activos que quedaron afectados
     */
    public long desactivar(UUID id, RolUsuario rol) {
        exigirAdministrador(rol);
        obtener(id);

        long afectados = categoriaRepositorio.contarTicketsActivos(id);
        categoriaRepositorio.desactivar(id);
        return afectados;
    }

    /**
     * Comprueba que quien opera sea administrador.
     *
     * @param rol rol del usuario
     * @throws SinPermisoExcepcion si no lo es
     */
    private void exigirAdministrador(RolUsuario rol) {
        if (rol != RolUsuario.ADMINISTRADOR) {
            throw new SinPermisoExcepcion("Solo un administrador puede gestionar las categorias.");
        }
    }
}
