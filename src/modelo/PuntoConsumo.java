package modelo;


public class PuntoConsumo {


    public static final String OTROS = "Otros";
    public static final String UNIDAD_MINUTO = "L/min";
    public static final String UNIDAD_DESCARGA = "L/descarga";
    public static final String UNIDAD_CICLO = "L/ciclo";


    public static final String[] ARTEFACTOS = {
        "Ducha",
        "Grifo de cocina / lavabo",
        "Inodoro",
        "Lavadora",
        "Manguera de jardín",
        OTROS
    };
    public static final double[] CAUDALES_PREDETERMINADOS = {9.0, 5.0, 6.0, 50.0, 15.0, 0.0};
    public static final String[] UNIDADES = {
        UNIDAD_MINUTO, UNIDAD_MINUTO, UNIDAD_DESCARGA, UNIDAD_CICLO, UNIDAD_MINUTO, UNIDAD_MINUTO
    };


    private static int buscarIndice(String artefacto) {
        for (int i = 0; i < ARTEFACTOS.length; i++) {
            if (ARTEFACTOS[i].equals(artefacto)) {
                return i;
            }
        }
        return -1;
    }


    public static double obtenerCaudalPredeterminado(String artefacto) {
        int i = buscarIndice(artefacto);
        if (i < 0) {
            return 0.0;
        }
        return CAUDALES_PREDETERMINADOS[i];
    }


    public static String obtenerUnidad(String artefacto) {
        int i = buscarIndice(artefacto);
        if (i < 0) {
            return UNIDAD_MINUTO;
        }
        return UNIDADES[i];
    }

    private int id;
    private String nombre;
    private String tipo;
    private double caudal;
    private String unidad;
    private double duracionMinutos;
    private int frecuenciaDiaria;
    public PuntoConsumo(int id, String nombre, String tipo, double caudal, String unidad,
                        double duracionMinutos, int frecuenciaDiaria) {
        this.id = id;
        this.nombre = nombre;
        this.tipo = tipo;
        this.caudal = caudal;
        this.unidad = unidad;
        this.duracionMinutos = duracionMinutos;
        this.frecuenciaDiaria = frecuenciaDiaria;
    }


    public boolean usaDuracion() {
        return unidad.equals(UNIDAD_MINUTO);
    }


    public double calcularVolumenDiario() {
        if (usaDuracion()) {
            return caudal * duracionMinutos * frecuenciaDiaria;
        }
        return caudal * frecuenciaDiaria;
    }


    public double calcularConsumoMensualM3() {
        return (calcularVolumenDiario() * 30) / 1000.0;
    }


    public double calcularPorcentajeImpacto(double consumoMensualTotalM3) {
        if (consumoMensualTotalM3 <= 0) {
            return 0;
        }
        return (calcularConsumoMensualM3() / consumoMensualTotalM3) * 100;
    }

    // ---------- Getters y setters ----------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public double getCaudal() {
        return caudal;
    }

    public void setCaudal(double caudal) {
        this.caudal = caudal;
    }

    public String getUnidad() {
        return unidad;
    }

    public void setUnidad(String unidad) {
        this.unidad = unidad;
    }

    public double getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(double duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public int getFrecuenciaDiaria() {
        return frecuenciaDiaria;
    }

    public void setFrecuenciaDiaria(int frecuenciaDiaria) {
        this.frecuenciaDiaria = frecuenciaDiaria;
    }
}
