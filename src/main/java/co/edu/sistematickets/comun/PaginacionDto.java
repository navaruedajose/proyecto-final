package co.edu.sistematickets.comun;

/**
 * Parámetros de paginación y ordenación que aceptan los listados.
 *
 * <p>Se modela como una clase base de la que heredan los filtros concretos,
 * para no repetir los mismos cuatro campos en ocho DTOs distintos (DRY). Cada
 * filtro añade después sus propios criterios.</p>
 *
 * <p>Los valores por defecto se eligen para que una petición sin parámetros
 * funcione: página 1, 20 elementos, orden descendente por fecha de creación.
 * Un listado nunca debería devolver la tabla entera por olvido del cliente.</p>
 */
public class PaginacionDto {

    /** Tamaño máximo de página aceptado. Evita que un cliente pida 100.000 filas. */
    public static final int LIMITE_MAXIMO = 100;

    /** Tamaño de página por defecto. */
    public static final int LIMITE_POR_DEFECTO = 20;

    private Integer pagina = 1;
    private Integer limite = LIMITE_POR_DEFECTO;
    private String ordenarPor;
    private String direccion = "DESC";

    /**
     * Normaliza los valores: corrige nulos, negativos y límites excesivos.
     *
     * <p>Se llama desde el servicio antes de consultar. Centralizar aquí la
     * corrección evita repetir la misma comprobación en cada módulo y, sobre
     * todo, evita que un {@code límite} negativo llegue al repositorio.</p>
     */
    public void normalizar() {
        if (pagina == null || pagina < 1) {
            pagina = 1;
        }
        if (limite == null || limite < 1) {
            limite = LIMITE_POR_DEFECTO;
        }
        if (limite > LIMITE_MAXIMO) {
            limite = LIMITE_MAXIMO;
        }
        if (direccion == null || !direccion.equalsIgnoreCase("ASC")) {
            direccion = "DESC";
        } else {
            direccion = "ASC";
        }
    }

    /**
     * Desplazamiento a aplicar en la consulta.
     *
     * @return número de filas a saltar
     */
    public int desplazamiento() {
        normalizar();
        return (pagina - 1) * limite;
    }

    public Integer getPagina() {
        return pagina;
    }

    public void setPagina(Integer pagina) {
        this.pagina = pagina;
    }

    public Integer getLimite() {
        return limite;
    }

    public void setLimite(Integer limite) {
        this.limite = limite;
    }

    public String getOrdenarPor() {
        return ordenarPor;
    }

    public void setOrdenarPor(String ordenarPor) {
        this.ordenarPor = ordenarPor;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
}
