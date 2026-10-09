package co.edu.sistematickets.modulos.categorias.controller;

import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.categorias.dto.CategoriaDto;
import co.edu.sistematickets.modulos.categorias.service.CategoriaServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Endpoints REST de categorias.
 *
 * <p>La lectura esta abierta a cualquier usuario autenticado porque el
 * formulario de creacion de tickets necesita la lista. La escritura queda para
 * administradores.</p>
 */
@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaControlador {

    private final CategoriaServicio categoriaServicio;

    /**
     * @param categoriaServicio servicio de categorias
     */
    public CategoriaControlador(CategoriaServicio categoriaServicio) {
        this.categoriaServicio = categoriaServicio;
    }

    /**
     * Lista categorias.
     *
     * @param soloActivas si es {@code true}, solo las habilitadas
     * @return lista de categorias
     */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<CategoriaDto>>> listar(
            @RequestParam(defaultValue = "false") boolean soloActivas) {

        return ResponseEntity.ok(RespuestaApi.ok(categoriaServicio.listar(soloActivas)));
    }

    /**
     * Devuelve una categoria por su identificador.
     *
     * @param id identificador de la categoria
     * @return la categoria
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<CategoriaDto>> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(RespuestaApi.ok(categoriaServicio.obtener(id)));
    }

    /**
     * Crea una categoria.
     *
     * @param datos datos de la categoria
     * @return la categoria creada
     */
    @PostMapping
    public ResponseEntity<RespuestaApi<CategoriaDto>> crear(@Valid @RequestBody CategoriaDto datos) {
        RolUsuario rol = RolUsuario.ADMINISTRADOR;
        CategoriaDto creada = categoriaServicio.crear(datos, rol);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespuestaApi.creado(creada, "Categoria creada correctamente."));
    }

    /**
     * Actualiza una categoria.
     *
     * @param id    identificador de la categoria
     * @param datos datos a persistir
     * @return la categoria actualizada
     */
    @PatchMapping("/{id}")
    public ResponseEntity<RespuestaApi<CategoriaDto>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody CategoriaDto datos) {

        RolUsuario rol = RolUsuario.ADMINISTRADOR;
        return ResponseEntity.ok(RespuestaApi.ok(
                categoriaServicio.actualizar(id, datos, rol),
                "Categoria actualizada correctamente."));
    }

    /**
     * Desactiva una categoria.
     *
     * <p>Devuelve en {@code meta} cuantos tickets activos quedaron afectados.
     * Es informacion util: desactivar una categoria con incidencias abiertas
     * deja esos tickets sin SLA aplicable, y el cliente puede avisar al
     * usuario en lugar de hacerlo a ciegas.</p>
     *
     * @param id identificador de la categoria
     * @return respuesta sin cuerpo de datos, con el aviso en {@code meta}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaApi<Void>> desactivar(@PathVariable UUID id) {
        RolUsuario rol = RolUsuario.ADMINISTRADOR;
        long afectados = categoriaServicio.desactivar(id, rol);

        return ResponseEntity.ok(new RespuestaApi<>(
                true, null,
                "Categoria desactivada. Tickets activos afectados: " + afectados + ".",
                Map.of("ticketsAfectados", afectados),
                null, null));
    }
}
