package modelo;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Representa una vivienda con su medidor de agua.
 * Es la clase central del modelo: un hogar tiene muchas lecturas, pruebas de fuga,
 * metas de ahorro y puntos de consumo (relaciones de asociación del diagrama UML).
 */
public class Hogar {

    // Datos básicos del hogar (atributos privados: encapsulamiento)
    private int id;
    private String nombreResponsable;
    private String barrio;
    private String codigoMedidor;
    private int habitantes;

    // Listas con los objetos relacionados al hogar (asociaciones del diagrama UML)
    private ArrayList<LecturaMedidor> lecturas;
    private ArrayList<PruebaFuga> pruebas;
    private ArrayList<MetaAhorro> metas;
    private ArrayList<PuntoConsumo> puntos;

    /** Crea un hogar con sus datos básicos y con las listas vacías. */
    public Hogar(int id, String nombreResponsable, String barrio, String codigoMedidor, int habitantes) {
        this.id = id;
        this.nombreResponsable = nombreResponsable;
        this.barrio = barrio;
        this.codigoMedidor = codigoMedidor;
        this.habitantes = habitantes;
        this.lecturas = new ArrayList<>();
        this.pruebas = new ArrayList<>();
        this.metas = new ArrayList<>();
        this.puntos = new ArrayList<>();
    }

    // ---------- Operaciones del hogar ----------

    /** Cambia el número de habitantes de la vivienda. */
    public void actualizarHabitantes(int n) {
        this.habitantes = n;
    }

    /** Agrega una lectura a la lista y la deja ordenada por fecha y hora. */
    public void registrarLectura(LecturaMedidor lectura) {
        lecturas.add(lectura);
        ordenarLecturas();
    }

    /** Ordena las lecturas de la más antigua a la más reciente. */
    public void ordenarLecturas() {
        lecturas.sort(Comparator.comparing(LecturaMedidor::getFechaHora));
    }

    /** Guarda una prueba de fuga en el historial del hogar. */
    public void registrarPrueba(PruebaFuga prueba) {
        pruebas.add(prueba);
    }

    /** Guarda una meta de ahorro del hogar. */
    public void agregarMeta(MetaAhorro meta) {
        metas.add(meta);
    }

    /** Guarda un artefacto (punto de consumo) del hogar. */
    public void agregarPunto(PuntoConsumo punto) {
        puntos.add(punto);
    }

    /** Devuelve las lecturas del medidor (ordenadas por fecha y hora). */
    public ArrayList<LecturaMedidor> obtenerLecturas() {
        return lecturas;
    }

    /** Devuelve las pruebas de fuga del hogar. */
    public ArrayList<PruebaFuga> obtenerPruebas() {
        return pruebas;
    }

    /** Devuelve las metas de ahorro del hogar. */
    public ArrayList<MetaAhorro> obtenerMetas() {
        return metas;
    }

    /** Devuelve los artefactos (puntos de consumo) del hogar. */
    public ArrayList<PuntoConsumo> obtenerPuntos() {
        return puntos;
    }

    // ---------- Getters y setters ----------
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombreResponsable() {
        return nombreResponsable;
    }

    public void setNombreResponsable(String nombreResponsable) {
        this.nombreResponsable = nombreResponsable;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getCodigoMedidor() {
        return codigoMedidor;
    }

    public void setCodigoMedidor(String codigoMedidor) {
        this.codigoMedidor = codigoMedidor;
    }

    public int getHabitantes() {
        return habitantes;
    }

    public void setHabitantes(int habitantes) {
        this.habitantes = habitantes;
    }

    /** Texto que se muestra en las listas desplegables. Ej: "1 - Carlos Mendoza (MED-1001)". */
    @Override
    public String toString() {
        return id + " - " + nombreResponsable + " (" + codigoMedidor + ")";
    }
}
