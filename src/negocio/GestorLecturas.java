package negocio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import modelo.Hogar;
import modelo.LecturaMedidor;
import modelo.PruebaFuga;


public class GestorLecturas {

    private int contadorId;
    private String mensajeError;

    public GestorLecturas() {
        contadorId = 1;
        mensajeError = "";
    }



    public boolean registrar(Hogar hogar, String fechaTxt, String horaTxt, String valorTxt) {
        if (hogar == null) {
            return error("Debe seleccionar un hogar.");
        }
        if (fechaTxt.trim().isEmpty() || horaTxt.trim().isEmpty() || valorTxt.trim().isEmpty()) {
            return error("Todos los campos son obligatorios (fecha, hora y valor del medidor).");
        }

        LocalDateTime fechaHora = convertirFechaHora(fechaTxt, horaTxt);
        if (fechaHora == null) {
            return false;   // el mensaje ya quedó guardado
        }
        double valor = convertirValor(valorTxt);
        if (valor < 0) {
            return false;
        }
        if (!validarOrden(hogar, fechaHora, valor, -1)) {
            return false;
        }

        LecturaMedidor lectura = new LecturaMedidor(contadorId, fechaHora, valor, hogar.getCodigoMedidor());
        contadorId++;
        hogar.registrarLectura(lectura);
        recalcularObservaciones(hogar);
        return true;
    }


    public ArrayList<LecturaMedidor> consultar(Hogar hogar, LocalDate desde, LocalDate hasta) {
        ArrayList<LecturaMedidor> resultado = new ArrayList<>();
        for (LecturaMedidor l : hogar.obtenerLecturas()) {
            boolean despuesDeDesde = (desde == null) || !l.getFecha().isBefore(desde);
            boolean antesDeHasta = (hasta == null) || !l.getFecha().isAfter(hasta);
            if (despuesDeDesde && antesDeHasta) {
                resultado.add(l);
            }
        }
        return resultado;
    }


    public boolean actualizar(Hogar hogar, int idLectura, String fechaTxt, String horaTxt, String valorTxt) {
        LecturaMedidor lectura = buscarPorId(hogar, idLectura);
        if (lectura == null) {
            return error("Seleccione una lectura de la tabla.");
        }
        if (fechaTxt.trim().isEmpty() || horaTxt.trim().isEmpty() || valorTxt.trim().isEmpty()) {
            return error("Todos los campos son obligatorios (fecha, hora y valor del medidor).");
        }
        if (estaUsadaEnPrueba(hogar, lectura)) {
            return error("Esta lectura se usa en una prueba de fuga y no se puede modificar.");
        }

        LocalDateTime fechaHora = convertirFechaHora(fechaTxt, horaTxt);
        if (fechaHora == null) {
            return false;
        }
        double valor = convertirValor(valorTxt);
        if (valor < 0) {
            return false;
        }
        if (!validarOrden(hogar, fechaHora, valor, idLectura)) {
            return false;
        }

        lectura.setFechaHora(fechaHora);
        lectura.setAcumuladoM3(valor);
        hogar.ordenarLecturas();
        recalcularObservaciones(hogar);
        return true;
    }

    public boolean eliminar(Hogar hogar, int idLectura) {
        LecturaMedidor lectura = buscarPorId(hogar, idLectura);
        if (lectura == null) {
            return error("Seleccione una lectura de la tabla.");
        }
        if (estaUsadaEnPrueba(hogar, lectura)) {
            return error("Esta lectura se usa en una prueba de fuga y no se puede eliminar.");
        }
        hogar.obtenerLecturas().remove(lectura);
        recalcularObservaciones(hogar);
        return true;
    }


    public double consumoPeriodoLitros(Hogar hogar, LecturaMedidor lectura) {
        ArrayList<LecturaMedidor> lista = hogar.obtenerLecturas();
        int posicion = lista.indexOf(lectura);
        if (posicion <= 0) {
            return -1;
        }
        return lista.get(posicion - 1).litrosHasta(lectura);
    }


    public void recalcularObservaciones(Hogar hogar) {
        ArrayList<LecturaMedidor> lista = hogar.obtenerLecturas();
        for (int i = 0; i < lista.size(); i++) {
            if (i == 0) {
                lista.get(i).actualizarObservacion("Lectura inicial");
            } else {
                double litros = lista.get(i - 1).litrosHasta(lista.get(i));
                lista.get(i).actualizarObservacion("Consumo de " + String.format("%.0f", litros) + " L");
            }
        }
    }



    private LecturaMedidor buscarPorId(Hogar hogar, int id) {
        if (hogar == null) {
            return null;
        }
        for (LecturaMedidor l : hogar.obtenerLecturas()) {
            if (l.getId() == id) {
                return l;
            }
        }
        return null;
    }

    private boolean estaUsadaEnPrueba(Hogar hogar, LecturaMedidor lectura) {
        for (PruebaFuga p : hogar.obtenerPruebas()) {
            if (p.getLecturaInicial() == lectura || p.getLecturaFinal() == lectura) {
                return true;
            }
        }
        return false;
    }


    private LocalDateTime convertirFechaHora(String fechaTxt, String horaTxt) {
        LocalDate fecha;
        LocalTime hora;
        try {
            fecha = LocalDate.parse(fechaTxt.trim());
        } catch (DateTimeParseException e) {
            error("Fecha inválida. Use el formato AAAA-MM-DD (por ejemplo 2026-11-05).");
            return null;
        }
        try {
            hora = LocalTime.parse(horaTxt.trim(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (DateTimeParseException e) {
            error("Hora inválida. Use el formato HH:MM (por ejemplo 07:30).");
            return null;
        }
        return LocalDateTime.of(fecha, hora);
    }


    private double convertirValor(String valorTxt) {
        double valor;
        try {
            valor = Double.parseDouble(valorTxt.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            error("El valor del medidor debe ser un número (por ejemplo 120.450).");
            return -1;
        }
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            error("El valor del medidor no es válido.");
            return -1;
        }
        if (valor < 0) {
            error("El valor del medidor no puede ser negativo.");
            return -1;
        }
        return valor;
    }


    private boolean validarOrden(Hogar hogar, LocalDateTime fechaHora, double valor, int idIgnorar) {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        for (LecturaMedidor l : hogar.obtenerLecturas()) {
            if (l.getId() == idIgnorar) {
                continue;
            }
            if (l.getFechaHora().equals(fechaHora)) {
                return error("Ya existe una lectura con esa fecha y hora.");
            }
            if (l.getFechaHora().isBefore(fechaHora) && l.getAcumuladoM3() > valor) {
                return error("La lectura (" + valor + " m3) no puede ser menor a la anterior ("
                        + l.getAcumuladoM3() + " m3 del " + l.getFechaHora().format(formato) + ").");
            }
            if (l.getFechaHora().isAfter(fechaHora) && l.getAcumuladoM3() < valor) {
                return error("La lectura (" + valor + " m3) no puede ser mayor a una lectura posterior ("
                        + l.getAcumuladoM3() + " m3 del " + l.getFechaHora().format(formato) + ").");
            }
        }
        return true;
    }



    public String getMensajeError() {
        return mensajeError;
    }

    private boolean error(String mensaje) {
        mensajeError = mensaje;
        return false;
    }
}
