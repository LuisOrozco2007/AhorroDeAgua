package negocio;

import modelo.Hogar;
import modelo.PuntoConsumo;


public class GestorPuntosConsumo {

    private int contadorId;
    private String mensajeError;

    public GestorPuntosConsumo() {
        contadorId = 1;
        mensajeError = "";
    }

    // ---------- RF3: Gestión de puntos de consumo ----------

    /**
     * Registra un artefacto en el hogar. Devuelve false si hay un error de validación.
     *
     * @param ubicacion            dónde está el artefacto (ej. "Baño principal")
     * @param artefacto            opción elegida en la lista (ej. "Ducha" u "Otros")
     * @param nombrePersonalizado  nombre escrito por el usuario (solo se usa si eligió "Otros")
     */
    public boolean registrar(Hogar hogar, String ubicacion, String artefacto, String nombrePersonalizado,
                             String caudalTxt, String duracionTxt, String frecuenciaTxt) {
        if (hogar == null) {
            return error("Debe seleccionar un hogar.");
        }
        if (artefacto == null || artefacto.trim().isEmpty()) {
            return error("Debe seleccionar un tipo de artefacto.");
        }
        if (ubicacion.trim().isEmpty()) {
            return error("Debe ingresar el nombre o la ubicación del artefacto (por ejemplo: Baño principal).");
        }

        boolean esOtros = artefacto.equals(PuntoConsumo.OTROS);
        if (esOtros && nombrePersonalizado.trim().isEmpty()) {
            return error("Escriba el nombre del artefacto personalizado.");
        }

        // La unidad depende del artefacto: solo los de L/min necesitan la duración de uso
        String unidad = PuntoConsumo.obtenerUnidad(artefacto);
        boolean usaDuracion = unidad.equals(PuntoConsumo.UNIDAD_MINUTO);

        if (caudalTxt.trim().isEmpty() || frecuenciaTxt.trim().isEmpty()
                || (usaDuracion && duracionTxt.trim().isEmpty())) {
            return error("Todos los campos son obligatorios.");
        }

        double caudal;
        double duracion = 0;
        int frecuencia;
        try {
            caudal = Double.parseDouble(caudalTxt.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return error("El caudal debe ser un número (por ejemplo 9.0).");
        }
        if (usaDuracion) {
            try {
                duracion = Double.parseDouble(duracionTxt.trim().replace(',', '.'));
            } catch (NumberFormatException e) {
                return error("La duración debe ser un número (por ejemplo 5).");
            }
        }
        try {
            frecuencia = Integer.parseInt(frecuenciaTxt.trim());
        } catch (NumberFormatException e) {
            return error("La frecuencia diaria debe ser un número entero.");
        }

        // RF4: los datos deben ser mayores a cero y coherentes
        if (Double.isNaN(caudal) || Double.isInfinite(caudal) || caudal <= 0) {
            return error("El caudal debe ser mayor que 0.");
        }
        if (usaDuracion && (Double.isNaN(duracion) || Double.isInfinite(duracion) || duracion <= 0)) {
            return error("La duración debe ser mayor que 0.");
        }
        if (frecuencia <= 0) {
            return error("La frecuencia diaria debe ser mayor que 0.");
        }
        if (usaDuracion && duracion * frecuencia > 1440) {
            return error("El tiempo total de uso al día no puede superar 1440 minutos (24 horas).");
        }


        String tipo = esOtros ? nombrePersonalizado.trim() : artefacto;

        hogar.agregarPunto(new PuntoConsumo(contadorId, ubicacion.trim(), tipo,
                caudal, unidad, duracion, frecuencia));
        contadorId++;
        return true;
    }


    public boolean eliminar(Hogar hogar, int idPunto) {
        if (hogar == null) {
            return error("Debe seleccionar un hogar.");
        }
        for (PuntoConsumo p : hogar.obtenerPuntos()) {
            if (p.getId() == idPunto) {
                hogar.obtenerPuntos().remove(p);
                return true;
            }
        }
        return error("No se encontró el artefacto seleccionado.");
    }




    public double calcularTotalDiarioLitros(Hogar hogar) {
        double total = 0;
        for (PuntoConsumo p : hogar.obtenerPuntos()) {
            total += p.calcularVolumenDiario();
        }
        return total;
    }


    public double calcularTotalMensualM3(Hogar hogar) {
        double total = 0;
        for (PuntoConsumo p : hogar.obtenerPuntos()) {
            total += p.calcularConsumoMensualM3();
        }
        return total;
    }


    public double calcularPorcentajeImpacto(Hogar hogar, PuntoConsumo punto) {
        return punto.calcularPorcentajeImpacto(calcularTotalMensualM3(hogar));
    }



    public String getMensajeError() {
        return mensajeError;
    }

    private boolean error(String mensaje) {
        mensajeError = mensaje;
        return false;
    }
}
