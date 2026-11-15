package negocio;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import modelo.Hogar;
import modelo.LecturaMedidor;


public class GestorHogares {


    public static final double LITROS_POR_PERSONA_DIA = 100.0;

    private ArrayList<Hogar> hogares;
    private String mensajeError;

    public GestorHogares() {
        hogares = new ArrayList<>();
        mensajeError = "";
    }




    public boolean registrar(String idTxt, String responsable, String barrio,
                             String medidor, String habitantesTxt) {

        if (idTxt.trim().isEmpty() || responsable.trim().isEmpty() || barrio.trim().isEmpty()
                || medidor.trim().isEmpty() || habitantesTxt.trim().isEmpty()) {
            return error("Todos los campos son obligatorios.");
        }


        int id;
        int habitantes;
        try {
            id = Integer.parseInt(idTxt.trim());
        } catch (NumberFormatException e) {
            return error("El ID debe ser un número entero.");
        }
        try {
            habitantes = Integer.parseInt(habitantesTxt.trim());
        } catch (NumberFormatException e) {
            return error("El número de habitantes debe ser un número entero.");
        }


        if (id <= 0) {
            return error("El ID debe ser mayor que 0.");
        }
        if (habitantes < 1) {
            return error("El número de habitantes debe ser mayor o igual a 1.");
        }
        if (buscarPorId(id) != null) {
            return error("Ya existe un hogar con ese ID.");
        }
        if (buscarPorMedidor(medidor.trim()) != null) {
            return error("Ya existe un hogar con ese código de medidor.");
        }

        hogares.add(new Hogar(id, responsable.trim(), barrio.trim(), medidor.trim(), habitantes));
        return true;
    }


    public boolean actualizarHabitantes(String idTxt, String habitantesTxt) {
        if (idTxt.trim().isEmpty() || habitantesTxt.trim().isEmpty()) {
            return error("Debe indicar el ID del hogar y el nuevo número de habitantes.");
        }
        int id;
        int habitantes;
        try {
            id = Integer.parseInt(idTxt.trim());
        } catch (NumberFormatException e) {
            return error("El ID debe ser un número entero.");
        }
        try {
            habitantes = Integer.parseInt(habitantesTxt.trim());
        } catch (NumberFormatException e) {
            return error("El número de habitantes debe ser un número entero.");
        }
        if (habitantes < 1) {
            return error("El número de habitantes debe ser mayor o igual a 1.");
        }
        Hogar hogar = buscarPorId(id);
        if (hogar == null) {
            return error("No existe un hogar con el ID " + id + ".");
        }
        hogar.actualizarHabitantes(habitantes);
        return true;
    }

    public boolean eliminar(int id) {
        Hogar hogar = buscarPorId(id);
        if (hogar == null) {
            return error("No existe un hogar con el ID " + id + ".");
        }
        hogares.remove(hogar);
        return true;
    }

    public Hogar buscarPorId(int id) {
        for (Hogar h : hogares) {
            if (h.getId() == id) {
                return h;
            }
        }
        return null;
    }

    private Hogar buscarPorMedidor(String codigo) {
        for (Hogar h : hogares) {
            if (h.getCodigoMedidor().equalsIgnoreCase(codigo)) {
                return h;
            }
        }
        return null;
    }

    public ArrayList<Hogar> getHogares() {
        return hogares;
    }


    public double calcularConsumoIdeal(Hogar hogar) {
        return hogar.getHabitantes() * LITROS_POR_PERSONA_DIA;
    }


    public double calcularConsumoRealDiario(Hogar hogar) {
        ArrayList<LecturaMedidor> lecturas = hogar.obtenerLecturas();
        if (lecturas.size() < 2) {
            return -1;
        }
        LecturaMedidor primera = lecturas.get(0);
        LecturaMedidor ultima = lecturas.get(lecturas.size() - 1);

        long minutos = Duration.between(primera.getFechaHora(), ultima.getFechaHora()).toMinutes();
        if (minutos <= 0) {
            return -1;
        }
        double litros = primera.litrosHasta(ultima);
        double dias = minutos / 1440.0;   // 1440 minutos tiene un día
        return litros / dias;
    }

    public ComparacionConsumo comparar(Hogar hogar, LocalDate desde, LocalDate hasta) {
        if (hogar == null) {
            error("Debe seleccionar un hogar.");
            return null;
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            error("La fecha «Desde» no puede ser posterior a la fecha «Hasta».");
            return null;
        }
        List<LecturaMedidor> lecturas = CalculadoraConsumo.lecturasEnRango(hogar, desde, hasta);
        double dias = CalculadoraConsumo.dias(lecturas);
        double ideal = calcularConsumoIdeal(hogar);
        OptionalDouble real = CalculadoraConsumo.consumoRealDiario(lecturas);
        return new ComparacionConsumo(hogar, desde, hasta, lecturas.size(), dias, ideal, real);
    }


    public double calcularDiferencia(Hogar hogar) {
        double real = calcularConsumoRealDiario(hogar);
        if (real < 0) {
            return 0;
        }
        return real - calcularConsumoIdeal(hogar);
    }


    public String obtenerEstadoConsumo(Hogar hogar) {
        double real = calcularConsumoRealDiario(hogar);
        if (real < 0) {
            return "Sin datos";
        }
        if (real <= calcularConsumoIdeal(hogar)) {
            return "Eficiente";
        }
        return "Excesivo";
    }



    public String getMensajeError() {
        return mensajeError;
    }

    private boolean error(String mensaje) {
        mensajeError = mensaje;
        return false;
    }
}
