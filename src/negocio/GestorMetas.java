package negocio;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import modelo.Hogar;
import modelo.MetaAhorro;


public class GestorMetas {

    private int contadorId;
    private String mensajeError;

    public GestorMetas() {
        contadorId = 1;
        mensajeError = "";
    }


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


    public String evaluar(MetaAhorro meta, double consumoRealDiario) {
        return meta.evaluar(consumoRealDiario);
    }

    // ---------- Manejo de errores ----------

    public String getMensajeError() {
        return mensajeError;
    }

    private boolean error(String mensaje) {
        mensajeError = mensaje;
        return false;
    }
}
