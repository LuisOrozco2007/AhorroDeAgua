package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;


public class LecturaMedidor {

    private int id;
    private LocalDateTime fechaHora;
    private double acumuladoM3;
    private String codigoMedidor;
    private String origenDato;     // Ej: "Manual"
    private String observacion;

    public LecturaMedidor(int id, LocalDateTime fechaHora, double acumuladoM3, String codigoMedidor) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.acumuladoM3 = acumuladoM3;
        this.codigoMedidor = codigoMedidor;
        this.origenDato = "Manual";
        this.observacion = "";
    }


    public boolean validar() {
        return fechaHora != null && acumuladoM3 >= 0;
    }


    public double litrosHasta(LecturaMedidor fin) {
        return (fin.getAcumuladoM3() - this.acumuladoM3) * 1000;
    }

    public void actualizarObservacion(String texto) {
        this.observacion = texto;
    }



    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public LocalDate getFecha() {
        return fechaHora.toLocalDate();
    }

    public LocalTime getHora() {
        return fechaHora.toLocalTime();
    }

    public double getAcumuladoM3() {
        return acumuladoM3;
    }

    public void setAcumuladoM3(double acumuladoM3) {
        this.acumuladoM3 = acumuladoM3;
    }

    public String getCodigoMedidor() {
        return codigoMedidor;
    }

    public void setCodigoMedidor(String codigoMedidor) {
        this.codigoMedidor = codigoMedidor;
    }

    public String getOrigenDato() {
        return origenDato;
    }

    public void setOrigenDato(String origenDato) {
        this.origenDato = origenDato;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }


    @Override
    public String toString() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "#" + id + "  " + fechaHora.format(formato) + "  (" + String.format("%.3f", acumuladoM3) + " m3)";
    }
}
