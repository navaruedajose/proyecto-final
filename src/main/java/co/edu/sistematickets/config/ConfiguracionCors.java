package co.edu.sistematickets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuracion de CORS.
 *
 * <p>CORS es la regla que aplica el navegador cuando la pagina y la API estan
 * en origenes distintos. Sin esta configuracion, el frontend recibiria las
 * respuestas pero el navegador se las ocultaria al codigo, con un error que
 * confunde mucho porque la peticion <em>si</em> llego al servidor.</p>
 *
 * <p><strong>Los origenes permitidos no son {@code "*"}.</strong> Es una
 * decision deliberada: con comodin, cualquier sitio web podria llamar a esta
 * API desde el navegador del usuario. Se enumeran los origenes conocidos
 * (desarrollo local y la URL de produccion).</p>
 *
 * <p><strong>Nota sobre {@code allowCredentials}.</strong> Se deja en
 * {@code false} porque la autenticacion viaja en la cabecera
 * {@code Authorization}, no en cookies. Con comodin en los origenes y
 * credenciales activadas a la vez, el navegador rechazaria la respuesta por
 * insegura; como aqui no hacen falta credenciales de navegador, el problema ni
 * se plantea.</p>
 */
@Configuration
public class ConfiguracionCors implements WebMvcConfigurer {

    /**
     * Registra las reglas de CORS de la API.
     *
     * @param registro registro de configuracion de CORS
     */
    @Override
    public void addCorsMappings(CorsRegistry registro) {
        registro.addMapping("/api/**")
                .allowedOrigins(
                        // Desarrollo: el frontend de Vite.
                        "http://localhost:5173",
                        "http://127.0.0.1:5173",
                        // Si el frontend se sirve detras del mismo nginx del proyecto.
                        "http://localhost:8080",
                        // Produccion: sustituir por el dominio real antes de desplegar.
                        "https://sistema-tickets.example.com"
                )
                .allowedMethods("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type", "Authorization", "Accept")
                // El frontend necesita leer el nombre del archivo al descargar
                // reportes y evidencias. Sin exponer esta cabecera, el navegador
                // no le deja acceder a ella aunque venga en la respuesta.
                .exposedHeaders("Content-Disposition")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
