package negocio;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import modelo.Hogar;
import modelo.LecturaMedidor;

/**
 * RF1 y RF2: administra los hogares (registrar, consultar, actualizar habitantes y eliminar)
 * y compara el consumo ideal con el consumo real.
 */
public class GestorHogares {

    // Consumo ideal por persona al día (referencia de la OMS: 100 litros)
    public static final double LITROS_POR_PERSONA_DIA = 100.0;

    // hogares: lista de viviendas registradas
    // mensajeError: guarda el texto del último error de validación
    private ArrayList<Hogar> hogares;
    private String mensajeError;

    /** Crea el gestor con la lista de hogares vacía. */
    public GestorHogares() {
        hogares = new ArrayList<>();
        mensajeError = "";
    }

    // ---------- RF1: Gestionar viviendas ----------

    /**
     * Registra un hogar nuevo. Valida que no haya campos vacíos, que el ID y los habitantes sean
     * números enteros, que los habitantes sean al menos 1 y que no se repitan el ID ni el código de medidor.
     * Devuelve false (y guarda el mensaje de error) si algo falla.
     */
    public boolean registrar(String idTxt, String responsable, String barrio,
                             String medidor, String habitantesTxt) {

        // Campos vacíos
        if (idTxt.trim().isEmpty() || responsable.trim().isEmpty() || barrio.trim().isEmpty()
                || medidor.trim().isEmpty() || habitantesTxt.trim().isEmpty()) {
            return error("Todos los campos son obligatorios.");
        }

        // Conversión de los números (si el texto no es un número, se avisa del error)
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

        // Reglas de negocio
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

    /** Cambia el número de habitantes de un hogar existente (se busca por su ID). */
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

    /** Elimina un hogar por su ID (junto con todos sus datos). */
    public boolean eliminar(int id) {
        Hogar hogar = buscarPorId(id);
        if (hogar == null) {
            return error("No existe un hogar con el ID " + id + ".");
        }
        hogares.remove(hogar);
        return true;
    }

    /** Busca un hogar por su ID. Devuelve null si no existe. */
    public Hogar buscarPorId(int id) {
        for (Hogar h : hogares) {
            if (h.getId() == id) {
                return h;
            }
        }
        return null;
    }

    /** Busca un hogar por su código de medidor (sin distinguir mayúsculas). Devuelve null si no existe. */
    private Hogar buscarPorMedidor(String codigo) {
        for (Hogar h : hogares) {
            if (h.getCodigoMedidor().equalsIgnoreCase(codigo)) {
                return h;
            }
        }
        return null;
    }

    /** Devuelve la lista de hogares registrados. */
    public ArrayList<Hogar> getHogares() {
        return hogares;
    }

    // ---------- RF2: Consumo ideal vs consumo real ----------

    /** Consumo ideal diario (litros) = habitantes * 100 L. */
    public double calcularConsumoIdeal(Hogar hogar) {
        return hogar.getHabitantes() * LITROS_POR_PERSONA_DIA;
    }

    /**
     * Consumo real diario (litros por día), calculado con la primera y la última lectura
     * del medidor del hogar. Devuelve -1 si no hay datos suficientes.
     */
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

    /**
     * Compara el consumo real con el ideal usando solo las lecturas entre "desde" y "hasta"
     * (null = sin límite). Devuelve null si el hogar es null o si las fechas están al revés
     * (el mensaje queda en getMensajeError()).
     */
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

    /** Diferencia entre el consumo real y el ideal (positivo = se gasta de más). Es 0 si no hay datos. */
    public double calcularDiferencia(Hogar hogar) {
        double real = calcularConsumoRealDiario(hogar);
        if (real < 0) {
            return 0;
        }
        return real - calcularConsumoIdeal(hogar);
    }

    /** Devuelve "Eficiente", "Excesivo" o "Sin datos". */
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
