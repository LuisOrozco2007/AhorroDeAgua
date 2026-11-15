package modelo;

import java.time.LocalDate;


public class MetaAhorro {


    public static final String CUMPLIDA = "Meta cumplida";
    public static final String NO_CUMPLIDA = "Meta no cumplida";
    public static final String EXCESIVO = "Consumo excesivo";
    public static final String SIN_DATOS = "Sin datos";

    private int id;
    private LocalDate inicio;
    private LocalDate fin;
    private double baseLPersonaDia;        // Consumo de referencia por persona (100 L)
    private double reduccionPorcentaje;    // Ej: 15 significa 15 % de ahorro
    private String descripcion;
    private int habitantesPeriodoBase;     // Habitantes cuando se creó la meta
    private String estado;

    public MetaAhorro(int id, LocalDate inicio, LocalDate fin, double baseLPersonaDia,
                      double reduccionPorcentaje, int habitantesPeriodoBase) {
        this.id = id;
        this.inicio = inicio;
        this.fin = fin;
        this.baseLPersonaDia = baseLPersonaDia;
        this.reduccionPorcentaje = reduccionPorcentaje;
        this.habitantesPeriodoBase = habitantesPeriodoBase;
        this.descripcion = "Reducir " + reduccionPorcentaje + "% el consumo diario del hogar";
        this.estado = SIN_DATOS;
    }


    public double calcularIdeal() {
        return baseLPersonaDia * habitantesPeriodoBase;
    }


    public double calcularObjetivo() {
        return calcularIdeal() * (1 - reduccionPorcentaje / 100.0);
    }


    public String evaluar(double consumoActual) {
        if (consumoActual < 0) {
            estado = SIN_DATOS;
        } else if (consumoActual <= calcularObjetivo()) {
            estado = CUMPLIDA;
        } else if (consumoActual <= calcularIdeal()) {
            estado = NO_CUMPLIDA;
        } else {
            estado = EXCESIVO;
        }
        return estado;
    }

    public void actualizarDescripcion(String texto) {
        this.descripcion = texto;
    }



    public int getId() {
        return id;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public void setInicio(LocalDate inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFin() {
        return fin;
    }

    public void setFin(LocalDate fin) {
        this.fin = fin;
    }

    public double getBaseLPersonaDia() {
        return baseLPersonaDia;
    }

    public double getReduccionPorcentaje() {
        return reduccionPorcentaje;
    }

    public void setReduccionPorcentaje(double reduccionPorcentaje) {
        this.reduccionPorcentaje = reduccionPorcentaje;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getHabitantesPeriodoBase() {
        return habitantesPeriodoBase;
    }

    public String getEstado() {
        return estado;
    }
}
