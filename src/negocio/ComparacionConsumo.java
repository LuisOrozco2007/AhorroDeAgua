package negocio;

import java.time.LocalDate;
import java.util.Locale;
import java.util.OptionalDouble;
import modelo.Hogar;

public final class ComparacionConsumo {

    public static final String EFICIENTE = "Consumo eficiente";
    public static final String EXCESIVO = "Consumo excesivo";
    public static final String SIN_DATOS = "Datos insuficientes";

    private final String responsable;
    private final int habitantes;
    private final String codigoMedidor;
    private final LocalDate desde;
    private final LocalDate hasta;
    private final int lecturasUsadas;
    private final double dias;
    private final double ideal;
    private final OptionalDouble real;

    ComparacionConsumo(Hogar hogar, LocalDate desde, LocalDate hasta,
                       int lecturasUsadas, double dias,
                       double ideal, OptionalDouble real) {
        this.responsable = hogar.getNombreResponsable();
        this.habitantes = hogar.getHabitantes();
        this.codigoMedidor = hogar.getCodigoMedidor();
        this.desde = desde;
        this.hasta = hasta;
        this.lecturasUsadas = lecturasUsadas;
        this.dias = dias;
        this.ideal = ideal;
        this.real = real;
    }

    public double getIdeal() {
        return ideal;
    }

    public OptionalDouble getReal() {
        return real;
    }

    public int getLecturasUsadas() {
        return lecturasUsadas;
    }

    public double getDiferencia() {
        return real.isPresent() ? real.getAsDouble() - ideal : 0;
    }

    public String getEstado() {
        if (!real.isPresent()) {
            return SIN_DATOS;
        }
        return real.getAsDouble() <= ideal ? EFICIENTE : EXCESIVO;
    }

    public String resumen() {
        String periodo;
        if (desde == null && hasta == null) {
            periodo = "todo el historial";
        } else if (desde == null) {
            periodo = "hasta " + hasta;
        } else if (hasta == null) {
            periodo = "desde " + desde;
        } else {
            periodo = desde + " a " + hasta;
        }

        StringBuilder texto = new StringBuilder();
        texto.append("Vivienda: ").append(responsable)
                .append(" (").append(habitantes).append(" habitantes) - Medidor ")
                .append(codigoMedidor).append('\n');
        texto.append("Periodo: ").append(periodo)
                .append(" | lecturas usadas: ").append(lecturasUsadas).append('\n');
        texto.append(String.format(Locale.US,
                "Consumo ideal diario (OMS 100 L/persona): %.2f L/día%n", ideal));
        if (real.isPresent()) {
            texto.append(String.format(Locale.US,
                    "Consumo real promedio (%.2f días): %.2f L/día%n", dias, real.getAsDouble()));
            texto.append(String.format(Locale.US, "Diferencia: %+.2f L/día%n", getDiferencia()));
        } else {
            texto.append("Consumo real: no hay al menos 2 lecturas en el periodo.\n");
        }
        texto.append("Dictamen: ").append(getEstado());
        return texto.toString();
    }
}
