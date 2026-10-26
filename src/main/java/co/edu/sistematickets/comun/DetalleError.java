package co.edu.sistematickets.comun;

/**
 * Error asociado a un campo concreto de la petición.
 *
 * <p>Permite que el frontend pinte el mensaje debajo del campo que lo provoca
 * en lugar de mostrar un aviso genérico al pie del formulario.</p>
 *
 * @param campo   nombre del campo tal como lo envía el cliente
 * @param mensaje motivo del rechazo, redactado para el usuario final
 */
public record DetalleError(String campo, String mensaje) {
}
