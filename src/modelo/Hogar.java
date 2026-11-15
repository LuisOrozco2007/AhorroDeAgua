package modelo;

import java.util.ArrayList;
import java.util.Comparator;


public class Hogar {


    private int id;
    private String nombreResponsable;
    private String barrio;
    private String codigoMedidor;
    private int habitantes;


    private ArrayList<LecturaMedidor> lecturas;
    private ArrayList<PruebaFuga> pruebas;
    private ArrayList<MetaAhorro> metas;
    private ArrayList<PuntoConsumo> puntos;


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




    public void actualizarHabitantes(int n) {
        this.habitantes = n;
    }


    public void registrarLectura(LecturaMedidor lectura) {
        lecturas.add(lectura);
        ordenarLecturas();
    }

    public void ordenarLecturas() {
        lecturas.sort(Comparator.comparing(LecturaMedidor::getFechaHora));
    }

    public void registrarPrueba(PruebaFuga prueba) {
        pruebas.add(prueba);
    }

    public void agregarMeta(MetaAhorro meta) {
        metas.add(meta);
    }

    public void agregarPunto(PuntoConsumo punto) {
        puntos.add(punto);
    }

    public ArrayList<LecturaMedidor> obtenerLecturas() {
        return lecturas;
    }

    public ArrayList<PruebaFuga> obtenerPruebas() {
        return pruebas;
    }

    public ArrayList<MetaAhorro> obtenerMetas() {
        return metas;
    }

    public ArrayList<PuntoConsumo> obtenerPuntos() {
        return puntos;
    }



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


    @Override
    public String toString() {
        return id + " - " + nombreResponsable + " (" + codigoMedidor + ")";
    }
}
