package co.edu.sistematickets.comun;

/**
 * Error asociado a un campo concreto de la peticion.
 *
 * <p>Permite que el frontend pinte el mensaje debajo del campo que lo provoca
 * en lugar de mostrar un aviso generico al pie del formulario.</p>
 *
 * @param campo   nombre del campo tal como lo envia el cliente
 * @param mensaje motivo del rechazo, redactado para el usuario final
 */
public record DetalleError(String campo, String mensaje) {
}
