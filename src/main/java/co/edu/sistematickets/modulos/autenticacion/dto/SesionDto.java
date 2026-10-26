package co.edu.sistematickets.modulos.autenticacion.dto;

/**
 * Respuesta del inicio de sesión: la sesión completa en un solo objeto.
 *
 * <p>Se agrupan los dos tokens y el perfil porque el cliente los necesita
 * todos a la vez. Devolverlos en llamadas separadas obligaría a tres viajes de
 * red antes de poder pintar la primera pantalla.</p>
 *
 * <p><strong>Sobre los dos tokens.</strong> El de acceso dura poco (minutos) y
 * es el que viaja en cada petición; el de refresco dura más (días) y solo
 * sirve para pedir un token de acceso nuevo. Separarlos limita el dano si el
 * token de acceso se filtra: caduca solo. El de refresco se guarda en la base
 * <em>hasheado</em>, nunca en claro, de modo que si la base se compromete los
 * tokens no son utilizables.</p>
 *
 * @param tokenAcceso      JWT de corta duración para autenticar peticiones
 * @param tokenRefresco    token de larga duración para renovar el de acceso
 * @param expiraEnSegundos vida útil del token de acceso, en segundos
 * @param usuario          perfil público del usuario autenticado
 */
public record SesionDto(
        String tokenAcceso,
        String tokenRefresco,
        long expiraEnSegundos,
        UsuarioAutenticadoDto usuario
) {
}
