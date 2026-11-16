package negocio;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import modelo.Hogar;
import modelo.MetaAhorro;

/**
 * Crea y evalúa las metas de ahorro mensual de cada hogar.
 * La meta se calcula sobre el consumo ideal de la OMS (100 L por persona al día).
 */
public class GestorMetas {

    // contadorId: da un número de ID único a cada elemento que se registra
    // mensajeError: guarda el texto del último error de validación
    private int contadorId;
    private String mensajeError;

    /** Crea el gestor con el contador de ID en 1 y sin mensaje de error. */
    public GestorMetas() {
        contadorId = 1;
        mensajeError = "";
    }

    /**
     * Crea una meta de ahorro para el hogar. Valida que no haya campos vacíos, que las fechas tengan
     * el formato AAAA-MM-DD, que la fecha fin sea posterior a la de inicio y que la reducción esté
     * entre 0 y 100. Devuelve false (y guarda el mensaje de error) si algo falla.
     */
    public boolean crear(Hogar hogar, String inicioTxt, String finTxt, String reduccionTxt) {
        if (hogar == null) {
            return error("Debe seleccionar un hogar.");
        }
        if (inicioTxt.trim().isEmpty() || finTxt.trim().isEmpty() || reduccionTxt.trim().isEmpty()) {
            return error("Todos los campos son obligatorios.");
        }

        LocalDate inicio;
        LocalDate fin;
        double reduccion;
        try {
            inicio = LocalDate.parse(inicioTxt.trim());
        } catch (DateTimeParseException e) {
            return error("Fecha de inicio inválida. Use el formato AAAA-MM-DD.");
        }
        try {
            fin = LocalDate.parse(finTxt.trim());
        } catch (DateTimeParseException e) {
            return error("Fecha fin inválida. Use el formato AAAA-MM-DD.");
        }
        try {
            reduccion = Double.parseDouble(reduccionTxt.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return error("El porcentaje de reducción debe ser un número (por ejemplo 15).");
        }

        if (!fin.isAfter(inicio)) {
            return error("La fecha fin debe ser posterior a la fecha de inicio.");
        }
        if (Double.isNaN(reduccion) || reduccion <= 0 || reduccion >= 100) {
            return error("El porcentaje de reducción debe estar entre 0 y 100.");
        }

        MetaAhorro meta = new MetaAhorro(contadorId, inicio, fin,
                GestorHogares.LITROS_POR_PERSONA_DIA, reduccion, hogar.getHabitantes());
        contadorId++;
        hogar.agregarMeta(meta);
        return true;
    }

    /** Evalúa una meta con el consumo real diario actual del hogar (-1 si no hay datos). */
    public String evaluar(MetaAhorro meta, double consumoRealDiario) {
        return meta.evaluar(consumoRealDiario);
    }

    // ---------- Manejo de errores ----------

    /** Devuelve el mensaje del último error de validación (para mostrarlo con JOptionPane en la interfaz). */
    public String getMensajeError() {
        return mensajeError;
    }

    /** Guarda el mensaje de error y devuelve false, así los métodos pueden escribir "return error(...)". */
    private boolean error(String mensaje) {
        mensajeError = mensaje;
        return false;
    }
}
