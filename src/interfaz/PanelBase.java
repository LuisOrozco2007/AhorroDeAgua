package interfaz;

import javax.swing.JPanel;

/**
 * Clase base de todas las pestañas (paneles) de la aplicación.
 * Obliga a cada panel a tener el método actualizar(), que la ventana principal llama
 * al cambiar de pestaña o de vivienda para que el panel recargue sus datos.
 */
public class PanelBase extends JPanel {

    /** Cada pestaña sobrescribe este método con @Override para recargar sus datos. */
    public void actualizar() {
    }
}
