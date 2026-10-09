package co.edu.sistematickets;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Punto de entrada del backend del Sistema de Tickets.
 *
 * <p>Este proyecto es la version Spring Boot (Java) del backend que en el
 * proyecto original estaba escrito en Node.js + Express. La estructura de
 * paquetes replica la misma arquitectura por capas para que ambas
 * implementaciones sean comparables:</p>
 *
 * <pre>
 *   co.edu.sistematickets
 *   |-- config/       Configuracion transversal (CORS, OpenAPI, propiedades)
 *   |-- comun/        Piezas compartidas: respuestas, errores, excepciones
 *   |-- dominio/      Enumeraciones del negocio (rol, estado, prioridad, canal)
 *   \-- modulos/      Un paquete por dominio funcional
 *                   autenticacion · usuarios · categorias · tickets
 *                   comentarios · adjuntos · reportes · notificaciones
 * </pre>
 *
 * <p>Dentro de cada modulo se repiten siempre las mismas capas, y en este
 * mismo orden de dependencia:</p>
 *
 * <pre>
 *   dto/          → lo que entra y sale por HTTP (nunca la entidad de base)
 *   repository/   → acceso a datos (interfaz; sin implementacion aun)
 *   service/      → reglas de negocio (no conoce HTTP ni SQL)
 *   controller/   → traduce HTTP ↔ servicio
 * </pre>
 *
 * <p>La regla de dependencia va en un solo sentido: el controlador conoce al
 * servicio, el servicio conoce al repositorio y el repositorio no conoce a
 * nadie. Ninguna capa sabe que existe la que tiene por encima. Eso es lo que
 * permite, por ejemplo, probar las reglas de negocio sin levantar un servidor
 * ni una base de datos.</p>
 */
@SpringBootApplication
public class SistemaTicketsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SistemaTicketsApplication.class, args);
    }
}
