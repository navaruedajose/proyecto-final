package co.edu.sistematickets.modulos.usuarios.controller;

import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.dominio.RolUsuario;
import co.edu.sistematickets.modulos.usuarios.dto.ActualizarUsuarioDto;
import co.edu.sistematickets.modulos.usuarios.dto.CambiarContrasenaDto;
import co.edu.sistematickets.modulos.usuarios.dto.UsuarioDto;
import co.edu.sistematickets.modulos.usuarios.service.UsuarioServicio;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoints REST de gestion de usuarios.
 *
 * <p>Casi todo este controlador requiere rol de administrador. Las excepciones
 * son {@code /agentes} (lo consultan los agentes al repartirse trabajo) y el
 * cambio de la propia contrasena.</p>
 */
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    /**
     * @param usuarioServicio servicio de usuarios
     */
    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    /**
     * Lista usuarios con filtros y paginacion.
     *
     * @param rol      filtro por rol; puede ser {@code null}
     * @param activo   filtro por estado; puede ser {@code null}
     * @param busqueda texto libre; puede ser {@code null}
     * @param pagina   numero de pagina
     * @param limite   tamano de pagina
     * @return pagina de usuarios
     */
    @GetMapping
    public ResponseEntity<RespuestaApi<List<UsuarioDto>>> listar(
            @RequestParam(required = false) RolUsuario rol,
            @RequestParam(required = false) Boolean activo,
            @RequestParam(required = false) String busqueda,
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "20") int limite) {

        return ResponseEntity.ok(usuarioServicio.listar(rol, activo, busqueda, pagina, limite));
    }

    /**
     * Devuelve los agentes ordenados por carga de trabajo.
     *
     * <p><strong>Declarada antes de {@code /{id}}</strong>: si no, Spring
     * intentaria leer «agentes» como un UUID y la peticion fallaria con un
     * error de conversion en lugar de llegar a este metodo.</p>
     *
     * @return agentes de menor a mayor carga
     */
    @GetMapping("/agentes")
    public ResponseEntity<RespuestaApi<List<UsuarioDto>>> agentes() {
        return ResponseEntity.ok(RespuestaApi.ok(usuarioServicio.listarAgentes()));
    }

    /**
     * Devuelve un usuario por su identificador.
     *
     * @param id identificador del usuario
     * @return el usuario
     */
    @GetMapping("/{id}")
    public ResponseEntity<RespuestaApi<UsuarioDto>> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(RespuestaApi.ok(usuarioServicio.obtener(id)));
    }

    /**
     * Actualiza un usuario.
     *
     * @param id      usuario a editar
     * @param cambios campos a modificar
     * @return el usuario actualizado
     */
    @PatchMapping("/{id}")
    public ResponseEntity<RespuestaApi<UsuarioDto>> actualizar(
            @PathVariable UUID id,
            @Valid @RequestBody ActualizarUsuarioDto cambios) {

        // Pendiente: tomar identidad y rol del token.
        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.SOLICITANTE;

        return ResponseEntity.ok(RespuestaApi.ok(
                usuarioServicio.actualizar(id, cambios, usuarioId, rol),
                "Usuario actualizado correctamente."));
    }

    /**
     * Cambia la contrasena del usuario autenticado.
     *
     * <p>Ruta {@code /contrasena} sin identificador: solo se puede cambiar la
     * propia. Que el endpoint no acepte un identificador hace imposible
     * siquiera intentar cambiar la de otro.</p>
     *
     * @param datos contrasena actual y nueva
     * @return respuesta sin cuerpo
     */
    @PatchMapping("/contrasena")
    public ResponseEntity<Void> cambiarContrasena(@Valid @RequestBody CambiarContrasenaDto datos) {
        UUID usuarioId = null;
        usuarioServicio.cambiarContrasena(usuarioId, datos);
        return ResponseEntity.noContent().build();
    }

    /**
     * Desactiva un usuario.
     *
     * @param id identificador del usuario
     * @return respuesta sin cuerpo
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id) {
        UUID usuarioId = null;
        RolUsuario rol = RolUsuario.ADMINISTRADOR;

        usuarioServicio.desactivar(id, usuarioId, rol);
        return ResponseEntity.noContent().build();
    }
}
