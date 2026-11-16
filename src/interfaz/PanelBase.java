package interfaz;

import javax.swing.JPanel;

/**
 * Clase base de todas las pestañas (paneles) de la aplicación.
 * Obliga a cada panel a tener el método actualizar(), que la ventana principal llama
 * al cambiar de pestaña o de vivienda para que el panel recargue sus datos.
 */
public abstract class PanelBase extends JPanel {

    /** Recarga los datos que muestra el panel. Cada pestaña lo implementa a su manera. */
    public abstract void actualizar();
}
