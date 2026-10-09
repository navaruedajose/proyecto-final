package co.edu.sistematickets.modulos.autenticacion.controller;

import co.edu.sistematickets.comun.RespuestaApi;
import co.edu.sistematickets.modulos.autenticacion.dto.InicioSesionDto;
import co.edu.sistematickets.modulos.autenticacion.dto.RegistroDto;
import co.edu.sistematickets.modulos.autenticacion.dto.SesionDto;
import co.edu.sistematickets.modulos.autenticacion.dto.TokenRefrescoDto;
import co.edu.sistematickets.modulos.autenticacion.dto.UsuarioAutenticadoDto;
import co.edu.sistematickets.modulos.autenticacion.service.AutenticacionServicio;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Endpoints REST de autenticacion.
 *
 * <p>Es el unico modulo que mezcla rutas publicas y protegidas:</p>
 *
 * <ul>
 *   <li>Públicas (sin sesion): {@code /registro} e {@code /iniciar-sesion}.
 *       Tienen que serlo, obviamente: son la puerta de entrada.</li>
 *   <li>Protegidas: {@code /perfil}, {@code /cerrar-sesion} y el resto.</li>
 * </ul>
 *
 * <p>Las rutas publicas son las que hay que proteger con mayor cuidado fuera
 * del codigo: son las que reciben trafico de cualquiera, y por eso el proyecto
 * les aplica un limitador de peticiones mucho mas estricto que al resto (10
 * intentos frente a 1.000 generales). El limitador no se ve en el controlador;
 * es una capa por delante que conviene mencionar al explicar la seguridad.</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AutenticacionControlador {

    private final AutenticacionServicio autenticacionServicio;

    /**
     * @param autenticacionServicio servicio de autenticacion
     */
    public AutenticacionControlador(AutenticacionServicio autenticacionServicio) {
        this.autenticacionServicio = autenticacionServicio;
    }

    /**
     * Registra una cuenta publica.
     *
     * <p>El rol se fija internamente en {@code SOLICITANTE}; no viene del
     * cuerpo de la peticion. Devuelve {@code 201}.</p>
     *
     * @param datos datos del registro
     * @return perfil del usuario creado
     */
    @PostMapping("/registro")
    public ResponseEntity<RespuestaApi<UsuarioAutenticadoDto>> registro(@Valid @RequestBody RegistroDto datos) {
        UsuarioAutenticadoDto creado = autenticacionServicio.registrar(datos);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespuestaApi.creado(creado, "Cuenta creada correctamente."));
    }

    /**
     * Inicia sesion y devuelve los dos tokens.
     *
     * @param credenciales correo y contrasena
     * @return sesion con token de acceso, token de refresco y perfil
     */
    @PostMapping("/iniciar-sesion")
    public ResponseEntity<RespuestaApi<SesionDto>> iniciarSesion(@Valid @RequestBody InicioSesionDto credenciales) {
        return ResponseEntity.ok(RespuestaApi.ok(
                autenticacionServicio.iniciarSesion(credenciales),
                "Sesion iniciada correctamente."));
    }

    /**
     * Crea una cuenta interna (agente o administrador).
     *
     * <p>Ruta separada de {@code /registro} porque tiene un requisito distinto:
     * aqui si se decide el rol, y por eso solo un administrador puede
     * llamarla. Mantenerlas separadas evita que un cambio de permisos en una
     * afecte sin querer a la otra.</p>
     *
     * @param datos datos de la cuenta
     * @return perfil del usuario creado
     */
    @PostMapping("/usuarios")
    public ResponseEntity<RespuestaApi<UsuarioAutenticadoDto>> registrarUsuarioInterno(
            @Valid @RequestBody RegistroDto datos) {

        // Pendiente: tomar el rol del cuerpo validado y el adminId del token.
        UUID adminId = null;
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(RespuestaApi.creado(
                        autenticacionServicio.registrarUsuarioInterno(datos, null, adminId),
                        "Cuenta interna creada correctamente."));
    }

    /**
     * Renueva el token de acceso.
     *
     * @param datos token de refresco vigente
     * @return sesion nueva
     */
    @PostMapping("/refrescar")
    public ResponseEntity<RespuestaApi<SesionDto>> refrescar(@Valid @RequestBody TokenRefrescoDto datos) {
        return ResponseEntity.ok(RespuestaApi.ok(
                autenticacionServicio.refrescar(datos.tokenRefresco()),
                "Token renovado correctamente."));
    }

    /**
     * Cierra la sesion actual.
     *
     * @param datos token de refresco de la sesion
     * @return respuesta sin cuerpo
     */
    @PostMapping("/cerrar-sesion")
    public ResponseEntity<Void> cerrarSesion(@Valid @RequestBody TokenRefrescoDto datos) {
        autenticacionServicio.cerrarSesion(datos.tokenRefresco());
        return ResponseEntity.noContent().build();
    }

    /**
     * Cierra todas las sesiones del usuario autenticado.
     *
     * @return respuesta sin cuerpo
     */
    @PostMapping("/cerrar-todas-las-sesiones")
    public ResponseEntity<Void> cerrarTodasLasSesiones() {
        UUID usuarioId = null;
        autenticacionServicio.cerrarTodasLasSesiones(usuarioId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Devuelve el perfil del usuario autenticado.
     *
     * <p>El cliente lo usa al arrancar: con el token guardado pide el perfil
     * para saber quien es y que puede ver, sin tener que volver a pedir la
     * contrasena.</p>
     *
     * @return perfil sin datos sensibles
     */
    @GetMapping("/perfil")
    public ResponseEntity<RespuestaApi<UsuarioAutenticadoDto>> perfil() {
        UUID usuarioId = null;
        return ResponseEntity.ok(RespuestaApi.ok(autenticacionServicio.perfil(usuarioId)));
    }
}
