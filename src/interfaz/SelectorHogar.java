package interfaz;

import java.util.Objects;
import javax.swing.JComboBox;
import modelo.Hogar;
import negocio.GestorHogares;

class SelectorHogar {

    private final GestorHogares gestorHogares;
    private final JComboBox<Hogar> combo;
    private boolean cargando;
    private Runnable oyente = () -> { };

    SelectorHogar(GestorHogares gestorHogares) {
        this.gestorHogares = gestorHogares;
        this.combo = new JComboBox<>();
        combo.addActionListener(e -> {
            if (!cargando) {
                oyente.run();
            }
        });
        recargar();
    }

    JComboBox<Hogar> getComponente() {
        return combo;
    }

    Hogar getHogar() {
        return (Hogar) combo.getSelectedItem();
    }

    void alCambiar(Runnable oyente) {
        this.oyente = Objects.requireNonNull(oyente);
    }

    void recargar() {
        Hogar seleccionado = getHogar();
        Integer idSeleccionado = seleccionado == null ? null : seleccionado.getId();
        cargando = true;
        try {
            combo.removeAllItems();
            Hogar seleccionActualizada = null;
            for (Hogar hogar : gestorHogares.getHogares()) {
                combo.addItem(hogar);
                if (idSeleccionado != null && hogar.getId() == idSeleccionado) {
                    seleccionActualizada = hogar;
                }
            }
            if (seleccionActualizada != null) {
                combo.setSelectedItem(seleccionActualizada);
            }
        } finally {
            cargando = false;
        }
    }
}
