package co.edu.sistematickets.modulos.notificaciones.service;

import co.edu.sistematickets.dominio.CanalNotificacion;
import co.edu.sistematickets.modulos.notificaciones.dto.NotificacionDto;

import java.util.UUID;

/**
 * Contrato de un proveedor de notificaciones.
 *
 * <p>Esta interfaz es el ejemplo mas claro de inversion de dependencias del
 * proyecto, y merece la pena poder explicarlo en la sustentacion. El servicio
 * de tickets no sabe si las notificaciones salen por correo, por SMS o por
 * consola: solo conoce esta interfaz. Anadir un canal nuevo (por ejemplo
 * mensajeria instantanea) consiste en escribir una implementacion mas, sin
 * tocar el servicio de tickets.</p>
 *
 * <p>Hay una segunda consecuencia practica, tan valiosa como la primera: en
 * desarrollo y en las pruebas se inyecta una implementacion que
 * <strong>simula</strong> el envio. Asi las pruebas no gastan credito real ni
 * dependen de que haya conexion, y el resultado es reproducible.</p>
 */
public interface ProveedorNotificacion {

    /**
     * @return el canal que implementa este proveedor
     */
    CanalNotificacion canal();

    /**
     * Entrega una notificacion.
     *
     * <p>Devuelve un resultado en lugar de lanzar una excepcion cuando el
     * proveedor rechaza el envio. La razon es que un fallo de entrega es un
     * resultado esperado, no un error de programacion: se registra en
     * {@code error_detalle} y se reintenta mas tarde. Lanzar una excepcion
     * obligaria a envolver cada llamada en un try y a distinguir entre «no se
     * pudo enviar» y «algo se rompio».</p>
     *
     * @param notificacion notificacion a entregar
     * @return resultado del intento de envio
     */
    ResultadoEnvio enviar(NotificacionDto notificacion);

    /**
     * Resultado de un intento de entrega.
     *
     * @param exito        si el proveedor acepto el mensaje
     * @param detalleError motivo del rechazo, si lo hubo
     * @param referenciaExterna identificador que devuelve el proveedor, util para soporte
     */
    record ResultadoEnvio(boolean exito, String detalleError, String referenciaExterna) {

        /**
         * Construye un resultado exitoso.
         *
         * @param referenciaExterna identificador del proveedor
         * @return resultado con {@code exito = true}
         */
        public static ResultadoEnvio ok(String referenciaExterna) {
            return new ResultadoEnvio(true, null, referenciaExterna);
        }

        /**
         * Construye un resultado fallido.
         *
         * @param detalleError motivo del fallo
         * @return resultado con {@code exito = false}
         */
        public static ResultadoEnvio fallo(String detalleError) {
            return new ResultadoEnvio(false, detalleError, null);
        }
    }
}
