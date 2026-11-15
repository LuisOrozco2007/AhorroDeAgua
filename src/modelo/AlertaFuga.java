package modelo;

import java.time.LocalDateTime;


public class AlertaFuga {

    // Posibles estados de la alerta
    public static final String PENDIENTE = "Pendiente";
    public static final String EN_REVISION = "En revisión";
    public static final String CERRADA = "Cerrada";

    private int id;
    private String estado;
    private String observacion;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaCierre;
    private String ubicacionSospechada;
    private String accionRealizada;
    private String recomendaciones;

    public AlertaFuga(int id) {
        this.id = id;
        this.estado = PENDIENTE;
        this.observacion = "";
        this.fechaCreacion = LocalDateTime.now();
        this.ubicacionSospechada = "";
        this.accionRealizada = "";
        this.recomendaciones = "";
    }


    public void iniciarRevision() {
        if (estado.equals(PENDIENTE)) {
            estado = EN_REVISION;
        }
    }

    public void registrarUbicacion(String lugar) {
        this.ubicacionSospechada = lugar;
    }

    public void registrarAccion(String accion) {
        this.accionRealizada = accion;
    }


    public void cerrar(String nota) {
        this.observacion = nota;
        this.estado = CERRADA;
        this.fechaCierre = LocalDateTime.now();
    }



    public int getId() {
        return id;
    }

    public String getEstado() {
        return estado;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public String getUbicacionSospechada() {
        return ubicacionSospechada;
    }

    public String getAccionRealizada() {
        return accionRealizada;
    }

    public String getRecomendaciones() {
        return recomendaciones;
    }

    public void setRecomendaciones(String recomendaciones) {
        this.recomendaciones = recomendaciones;
    }
}
