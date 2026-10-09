package co.edu.sistematickets.dominio;

/**
 * Perfiles de usuario del sistema.
 *
 * <p>Espeja el tipo {@code rol_usuario} de PostgreSQL declarado en
 * {@code database/init/01-schema.sql}.</p>
 */
public enum RolUsuario {

    /** Crea tickets y solo ve los suyos. Es el rol por defecto al registrarse. */
    SOLICITANTE,

    /** Atiende tickets: cambia estados, comenta y resuelve. Ve lo asignado. */
    AGENTE,

    /** Administra el sistema: usuarios, categorias, SLA y reportes. */
    ADMINISTRADOR;

    /**
     * Indica si el rol pertenece al personal de soporte.
     *
     * <p>Se usa para decidir quien puede cambiar el estado de un ticket o
     * asignarlo: un solicitante nunca deberia poder hacerlo.</p>
     *
     * @return {@code true} para {@code AGENTE} y {@code ADMINISTRADOR}
     */
    public boolean esPersonalDeSoporte() {
        return this == AGENTE || this == ADMINISTRADOR;
    }
}
