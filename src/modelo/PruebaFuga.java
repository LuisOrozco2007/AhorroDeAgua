package modelo;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;


public class PruebaFuga {

    public static final String NORMAL = "Normal";
    public static final String POSIBLE_FUGA = "Posible fuga";
    public static final String DATOS_INVALIDOS = "Datos inválidos";


    public static final double TOLERANCIA_LITROS = 5.0;
    public static final int HORA_FIN_MADRUGADA = 5;
    public static final double DURACION_MAXIMA_HORAS = 8.0;

    private int id;
    private LecturaMedidor lecturaInicial;
    private LecturaMedidor lecturaFinal;
    private boolean sinUsoConfirmado;
    private double limiteLitros;
    private String resultado;
    private LocalDateTime fechaRegistro;
    private String observaciones;
    private AlertaFuga alerta;

    public PruebaFuga(int id, LecturaMedidor lecturaInicial, LecturaMedidor lecturaFinal,
                      boolean sinUsoConfirmado, double limiteLitros) {
        this.id = id;
        this.lecturaInicial = lecturaInicial;
        this.lecturaFinal = lecturaFinal;
        this.sinUsoConfirmado = sinUsoConfirmado;
        this.limiteLitros = limiteLitros;
        this.resultado = "Sin evaluar";
        this.fechaRegistro = LocalDateTime.now();
        this.observaciones = "";
        this.alerta = null;
    }


    public boolean finalEnMadrugada() {
        LocalTime hora = lecturaFinal.getHora();
        return !hora.isAfter(LocalTime.of(HORA_FIN_MADRUGADA, 0));
    }


    public boolean esValida() {
        if (lecturaInicial == null || lecturaFinal == null) {
            return false;
        }
        if (!lecturaFinal.getFechaHora().isAfter(lecturaInicial.getFechaHora())) {
            return false;
        }
        if (lecturaFinal.getAcumuladoM3() < lecturaInicial.getAcumuladoM3()) {
            return false;
        }
        if (!finalEnMadrugada() || calcularHoras() > DURACION_MAXIMA_HORAS) {
            return false;
        }
        if (limiteLitros < 0 || limiteLitros > TOLERANCIA_LITROS) {
            return false;
        }
        return sinUsoConfirmado;
    }


    public double calcularPerdidaLitros() {
        double litros = lecturaInicial.litrosHasta(lecturaFinal);

        return Math.round(litros * 100.0) / 100.0;
    }


    public double calcularPerdidaLHora() {
        double horas = calcularHoras();
        if (horas <= 0) {
            return 0;
        }
        return calcularPerdidaLitros() / horas;
    }

    public double calcularHoras() {
        long minutos = Duration.between(lecturaInicial.getFechaHora(), lecturaFinal.getFechaHora()).toMinutes();
        return minutos / 60.0;
    }

    public String evaluar() {
        if (!esValida()) {
            resultado = DATOS_INVALIDOS;
        } else if (calcularPerdidaLitros() > limiteLitros) {
            resultado = POSIBLE_FUGA;
        } else {
            resultado = NORMAL;
        }
        return resultado;
    }

    public void registrarObservacion(String texto) {
        this.observaciones = texto;
    }



    public int getId() {
        return id;
    }

    public LecturaMedidor getLecturaInicial() {
        return lecturaInicial;
    }

    public LecturaMedidor getLecturaFinal() {
        return lecturaFinal;
    }

    public boolean isSinUsoConfirmado() {
        return sinUsoConfirmado;
    }

    public double getLimiteLitros() {
        return limiteLitros;
    }

    public void setLimiteLitros(double limiteLitros) {
        this.limiteLitros = limiteLitros;
    }

    public String getResultado() {
        return resultado;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public AlertaFuga getAlerta() {
        return alerta;
    }

    public void setAlerta(AlertaFuga alerta) {
        this.alerta = alerta;
    }
}
