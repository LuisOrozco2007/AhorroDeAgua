package negocio;

import modelo.AlertaFuga;
import modelo.Hogar;
import modelo.LecturaMedidor;
import modelo.PruebaFuga;

/**
 * RF7: evalúa las pruebas de fuga con el criterio de consumo nocturno:
 * de 00:00 a 05:00 un hogar sin problemas consume casi 0 litros (tolerancia de 5 L
 * por el medidor). Si lo supera, el estado pasa a "Posible fuga" y se crea una
 * AlertaFuga con recomendaciones automáticas.
 */
public class GestorFugas {

    // contadorPruebas y contadorAlertas: dan un ID único a cada prueba y a cada alerta nueva
    // mensajeError: guarda el texto del último error de validación
    private int contadorPruebas;
    private int contadorAlertas;
    private String mensajeError;

    /** Crea el gestor con los contadores de ID en 1 y sin mensaje de error. */
    public GestorFugas() {
        contadorPruebas = 1;
        contadorAlertas = 1;
        mensajeError = "";
    }

    /**
     * Ejecuta una prueba de fuga con dos lecturas del mismo hogar.
     * Devuelve la prueba ya evaluada, o null si hubo un error de validación
     * (el mensaje se obtiene con getMensajeError()).
     */
    public PruebaFuga ejecutarPrueba(Hogar hogar, LecturaMedidor inicial, LecturaMedidor fin,
                                     String limiteTxt, boolean sinUsoConfirmado) {
        // Validaciones de los datos recibidos
        if (hogar == null) {
            error("Debe seleccionar un hogar.");
            return null;
        }
        if (inicial == null || fin == null) {
            error("Debe seleccionar la lectura inicial y la lectura final.\n"
                    + "Si el hogar tiene menos de dos lecturas, registre más en la pestaña Lecturas.");
            return null;
        }
        if (inicial == fin) {
            error("La lectura inicial y la final deben ser distintas.");
            return null;
        }
        if (!fin.getFechaHora().isAfter(inicial.getFechaHora())) {
            error("La lectura final debe ser posterior a la lectura inicial.");
            return null;
        }
        if (limiteTxt.trim().isEmpty()) {
            error("Debe ingresar el límite de consumo nocturno en litros.");
            return null;
        }

        // Conversión y validación del límite ingresado
        double limite;
        try {
            limite = Double.parseDouble(limiteTxt.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            error("El límite debe ser un número (por ejemplo 5.0).");
            return null;
        }
        if (Double.isNaN(limite) || Double.isInfinite(limite) || limite < 0) {
            error("El límite de consumo no puede ser negativo.");
            return null;
        }
        if (limite > PruebaFuga.TOLERANCIA_LITROS) {
            error("En la madrugada el consumo debe ser casi cero. El límite no puede superar "
                    + PruebaFuga.TOLERANCIA_LITROS + " L (tolerancia del medidor).");
            return null;
        }
        if (!sinUsoConfirmado) {
            error("Para ejecutar la prueba debe confirmar que no se usó agua en la vivienda durante el periodo.");
            return null;
        }

        // Se crea la prueba y se revisa que cumpla el criterio nocturno
        PruebaFuga prueba = new PruebaFuga(contadorPruebas, inicial, fin, sinUsoConfirmado, limite);
        if (!prueba.finalEnMadrugada()) {
            error("La lectura final debe ser de la madrugada (entre 00:00 y "
                    + String.format("%02d:00", PruebaFuga.HORA_FIN_MADRUGADA) + ").");
            return null;
        }
        if (prueba.calcularHoras() > PruebaFuga.DURACION_MAXIMA_HORAS) {
            error("Una prueba nocturna no puede durar más de " + (int) PruebaFuga.DURACION_MAXIMA_HORAS
                    + " horas. Elija una lectura inicial de la noche anterior (por ejemplo 23:00).");
            return null;
        }
        contadorPruebas++;
        prueba.evaluar();

        // Si supera el límite, el estado es "Posible fuga" y se genera la alerta con recomendaciones
        if (prueba.getResultado().equals(PruebaFuga.POSIBLE_FUGA)) {
            AlertaFuga alerta = new AlertaFuga(contadorAlertas);
            contadorAlertas++;
            alerta.setRecomendaciones(generarRecomendaciones());
            alerta.setObservacion("Se detectaron " + String.format("%.1f", prueba.calcularPerdidaLitros())
                    + " L en " + String.format("%.1f", prueba.calcularHoras())
                    + " h de madrugada, más que el límite de " + String.format("%.1f", limite) + " L.");
            prueba.setAlerta(alerta);
            prueba.registrarObservacion(alerta.getObservacion());
        } else {
            prueba.registrarObservacion("Consumo nocturno dentro de la tolerancia del medidor.");
        }

        hogar.registrarPrueba(prueba);
        return prueba;
    }

    /** Texto con las recomendaciones automáticas cuando hay posible fuga (grifos, tuberías, inodoros, mangueras). */
    public String generarRecomendaciones() {
        return "En la madrugada nadie usa agua, por lo que el medidor no debería avanzar.\n"
                + "1. Revise que todos los grifos (cocina, baños, patio) estén bien cerrados.\n"
                + "2. Inspeccione las tuberías visibles y las uniones en busca de humedad o goteo.\n"
                + "3. Revise los inodoros: una fuga silenciosa en el tanque es muy común\n"
                + "   (ponga unas gotas de colorante en el tanque y espere 15 minutos).\n"
                + "4. Verifique las mangueras de lavadora y de jardín.\n"
                + "5. Si el medidor sigue avanzando con todo cerrado, llame a un plomero.";
    }

    /** Pasa la alerta de "Pendiente" a "En revisión" y guarda, si se indicó, el lugar sospechoso. */
    public boolean iniciarRevision(AlertaFuga alerta, String ubicacion) {
        if (alerta == null) {
            return error("Seleccione una prueba que tenga una alerta.");
        }
        if (!alerta.getEstado().equals(AlertaFuga.PENDIENTE)) {
            return error("Solo se puede iniciar la revisión de una alerta pendiente.");
        }
        alerta.iniciarRevision();
        if (ubicacion != null && !ubicacion.trim().isEmpty()) {
            alerta.registrarUbicacion(ubicacion.trim());
        }
        return true;
    }

    /** Cierra la alerta guardando la acción realizada (la acción es obligatoria). */
    public boolean cerrarAlerta(AlertaFuga alerta, String accion) {
        if (alerta == null) {
            return error("Seleccione una prueba que tenga una alerta.");
        }
        if (alerta.getEstado().equals(AlertaFuga.CERRADA)) {
            return error("La alerta ya está cerrada.");
        }
        if (accion == null || accion.trim().isEmpty()) {
            return error("Escriba la acción realizada para poder cerrar la alerta.");
        }
        alerta.registrarAccion(accion.trim());
        alerta.cerrar(accion.trim());
        return true;
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
