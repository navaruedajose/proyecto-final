package co.edu.sistematickets.modulos.categorias.repository;

import co.edu.sistematickets.modulos.categorias.dto.CategoriaDto;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acceso a datos de categorias.
 *
 * <p><strong>Estado actual: solo la interfaz.</strong></p>
 */
public interface CategoriaRepositorio {

    /**
     * Lista categorias.
     *
     * @param soloActivas si es {@code true}, omite las desactivadas
     * @return lista de categorias
     */
    List<CategoriaDto> listar(boolean soloActivas);

    /**
     * Busca una categoria por su identificador.
     *
     * @param id identificador de la categoria
     * @return la categoria, o vacio si no existe
     */
    Optional<CategoriaDto> buscarPorId(UUID id);

    /**
     * Busca una categoria por su nombre.
     *
     * <p>La comparacion debe ser insensible a mayusculas y espacios sobrantes:
     * «Hardware» y «hardware » son la misma categoria, y aceptar ambas
     * partiria los informes en dos.</p>
     *
     * @param nombre nombre a buscar
     * @return la categoria, o vacio si no existe
     */
    Optional<CategoriaDto> buscarPorNombre(String nombre);

    /**
     * Persiste una categoria nueva.
     *
     * @param categoria datos de la categoria
     * @return la categoria creada
     */
    CategoriaDto guardar(CategoriaDto categoria);

    /**
     * Actualiza una categoria existente.
     *
     * @param id        identificador de la categoria
     * @param categoria datos a persistir
     * @return la categoria actualizada
     */
    CategoriaDto actualizar(UUID id, CategoriaDto categoria);

    /**
     * Desactiva una categoria (borrado logico).
     *
     * <p>No se borra la fila: los tickets historicos la referencian y
     * eliminarla dejaria sus informes sin categoria.</p>
     *
     * @param id identificador de la categoria
     */
    void desactivar(UUID id);

    /**
     * Cuenta cuantos tickets activos usan una categoria.
     *
     * <p>Se consulta antes de desactivar, para poder avisar de cuantos tickets
     * quedan afectados en lugar de hacerlo en silencio.</p>
     *
     * @param id identificador de la categoria
     * @return numero de tickets vigentes en esa categoria
     */
    long contarTicketsActivos(UUID id);
}
