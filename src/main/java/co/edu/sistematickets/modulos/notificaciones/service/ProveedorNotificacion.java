package co.edu.sistematickets.modulos.notificaciones.service;

import co.edu.sistematickets.dominio.CanalNotificacion;
import co.edu.sistematickets.modulos.notificaciones.dto.NotificacionDto;

import java.util.UUID;

/**
 * Contrato de un proveedor de notificaciones.
 *
 * <p>Esta interfaz es el ejemplo más claro de inversión de dependencias del
 * proyecto, y merece la pena poder explicarlo en la sustentación. El servicio
 * de tickets no sabe si las notificaciones salen por correo, por SMS o por
 * consola: solo conoce esta interfaz. Añadir un canal nuevo (por ejemplo
 * mensajería instantanea) consiste en escribir una implementación más, sin
 * tocar el servicio de tickets.</p>
 *
 * <p>Hay una segunda consecuencia práctica, tan valiosa como la primera: en
 * desarrollo y en las pruebas se inyecta una implementación que
 * <strong>simula</strong> el envío. Así las pruebas no gastan credito real ni
 * dependen de que haya conexión, y el resultado es reproducible.</p>
 */
public interface ProveedorNotificacion {

    /**
     * @return el canal que implementa este proveedor
     */
    CanalNotificacion canal();

    /**
     * Entrega una notificación.
     *
     * <p>Devuelve un resultado en lugar de lanzar una excepción cuando el
     * proveedor rechaza el envío. La razón es que un fallo de entrega es un
     * resultado esperado, no un error de programación: se registra en
     * {@code error_detalle} y se reintenta más tarde. Lanzar una excepción
     * obligaría a envolver cada llamada en un try y a distinguir entre «no se
     * pudo enviar» y «algo se rompio».</p>
     *
     * @param notificación notificación a entregar
     * @return resultado del intento de envío
     */
    ResultadoEnvio enviar(NotificacionDto notificacion);

    /**
     * Resultado de un intento de entrega.
     *
     * @param éxito        si el proveedor acepto el mensaje
     * @param detalleError motivo del rechazo, si lo hubo
     * @param referenciaExterna identificador que devuelve el proveedor, útil para soporte
     */
    record ResultadoEnvio(boolean exito, String detalleError, String referenciaExterna) {

        /**
         * Construye un resultado exitoso.
         *
         * @param referenciaExterna identificador del proveedor
         * @return resultado con {@code éxito = true}
         */
        public static ResultadoEnvio ok(String referenciaExterna) {
            return new ResultadoEnvio(true, null, referenciaExterna);
        }

        /**
         * Construye un resultado fallido.
         *
         * @param detalleError motivo del fallo
         * @return resultado con {@code éxito = false}
         */
        public static ResultadoEnvio fallo(String detalleError) {
            return new ResultadoEnvio(false, detalleError, null);
        }
    }
}
