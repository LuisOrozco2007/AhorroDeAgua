package negocio;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import modelo.Hogar;
import modelo.LecturaMedidor;

public final class CalculadoraConsumo {

    private CalculadoraConsumo() {
    }

    public static List<LecturaMedidor> lecturasEnRango(Hogar hogar, LocalDate desde, LocalDate hasta) {
        List<LecturaMedidor> resultado = new ArrayList<>();
        for (LecturaMedidor lectura : hogar.obtenerLecturas()) {
            LocalDate fecha = lectura.getFecha();
            if ((desde == null || !fecha.isBefore(desde))
                    && (hasta == null || !fecha.isAfter(hasta))) {
                resultado.add(lectura);
            }
        }
        return resultado;
    }

    public static double dias(List<LecturaMedidor> lecturas) {
        if (lecturas.size() < 2) {
            return 0;
        }
        LecturaMedidor primera = lecturas.get(0);
        LecturaMedidor ultima = lecturas.get(lecturas.size() - 1);
        long minutos = Duration.between(primera.getFechaHora(), ultima.getFechaHora()).toMinutes();
        return minutos / 1440.0;
    }

    public static OptionalDouble consumoRealDiario(List<LecturaMedidor> lecturas) {
        double dias = dias(lecturas);
        if (dias <= 0) {
            return OptionalDouble.empty();
        }
        LecturaMedidor primera = lecturas.get(0);
        LecturaMedidor ultima = lecturas.get(lecturas.size() - 1);
        return OptionalDouble.of(primera.litrosHasta(ultima) / dias);
    }
}
