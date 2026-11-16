package interfaz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Locale;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import modelo.Hogar;
import negocio.GestorFugas;
import negocio.GestorHogares;
import negocio.GestorLecturas;
import negocio.GestorMetas;
import negocio.GestorPuntosConsumo;

/**
 * Ventana principal de la aplicación. Contiene las pestañas (JTabbedPane)
 * y crea los gestores que comparten todos los paneles. Aquí también está el
 * método main y la carga de los datos de prueba.
 */
public class VentanaPrincipal extends JFrame {

    // Gestores de la capa de negocio (se comparten entre todos los paneles)
    private GestorHogares gestorHogares;
    private GestorPuntosConsumo gestorPuntos;
    private GestorLecturas gestorLecturas;
    private GestorFugas gestorFugas;
    private GestorMetas gestorMetas;

    // Selector de vivienda (barra superior) y paneles (una pestaña cada uno)
    private SelectorHogar selector;
    private PanelHogares panelHogares;
    private PanelPuntosConsumo panelPuntos;
    private PanelLecturas panelLecturas;
    private PanelIdealReal panelIdealReal;
    private PanelPruebaFugas panelPruebas;
    private PanelMetasAhorro panelMetas;

    /** Crea los gestores con datos de prueba, el selector, los paneles y las pestañas, y configura la ventana (900x650, centrada). */
    public VentanaPrincipal() {
        super("Asistente de Ahorro de Agua y Detección de Fugas");

        // 1. Se crean los gestores y se cargan los datos de prueba
        gestorHogares = new GestorHogares();
        gestorPuntos = new GestorPuntosConsumo();
        gestorLecturas = new GestorLecturas();
        gestorFugas = new GestorFugas();
        gestorMetas = new GestorMetas();
        cargarDatosDePrueba(gestorHogares, gestorPuntos, gestorLecturas, gestorFugas, gestorMetas);

        // Selector de vivienda compartido por todas las pestañas
        selector = new SelectorHogar(gestorHogares);

        // 2. Se crean los paneles
        panelHogares = new PanelHogares(gestorHogares, selector);
        panelPuntos = new PanelPuntosConsumo(selector, gestorPuntos);
        panelLecturas = new PanelLecturas(selector, gestorLecturas);
        panelIdealReal = new PanelIdealReal(gestorHogares, selector);
        panelPruebas = new PanelPruebaFugas(selector, gestorFugas);
        panelMetas = new PanelMetasAhorro(selector, gestorHogares, gestorMetas);

        // 3. Se arman las pestañas
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("🏠 Hogares", panelHogares);
        pestanas.addTab("🚰 Puntos de Consumo", panelPuntos);
        pestanas.addTab("📊 Lecturas de Medidor", panelLecturas);
        pestanas.addTab("Ideal vs real", panelIdealReal);
        pestanas.addTab("🔍 Pruebas y Alertas de Fuga", panelPruebas);
        pestanas.addTab("🎯 Metas de Ahorro", panelMetas);

        // Al cambiar de pestaña, se recarga el hogar seleccionado y los datos del panel.
        pestanas.addChangeListener(e -> {
            selector.recargar();
            ((PanelBase) pestanas.getSelectedComponent()).actualizar();
        });

        // Al elegir otra vivienda se actualiza la pestaña que está abierta
        selector.alCambiar(() -> ((PanelBase) pestanas.getSelectedComponent()).actualizar());

        // Barra superior con la lista de viviendas (la misma para todas las pestañas)
        JPanel selectorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 8));
        selectorPanel.setBackground(new Color(227, 242, 253));
        selectorPanel.add(new JLabel("Vivienda seleccionada:"));
        selector.getComponente().setPreferredSize(new Dimension(420, 28));
        selectorPanel.add(selector.getComponente());

        // Selector de vivienda arriba y pestañas en el centro
        setLayout(new BorderLayout());
        add(selectorPanel, BorderLayout.NORTH);
        add(pestanas, BorderLayout.CENTER);

        // 4. Configuración de la ventana
        setSize(900, 650);
        setLocationRelativeTo(null);   // centrada en la pantalla
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Carga hogares, artefactos, lecturas, pruebas de fuga y metas de ejemplo
     * para que las tablas no estén vacías al abrir la aplicación.
     */
    public static void cargarDatosDePrueba(GestorHogares gHogares, GestorPuntosConsumo gPuntos,
                                           GestorLecturas gLecturas, GestorFugas gFugas,
                                           GestorMetas gMetas) {
        // ----- Hogares -----
        gHogares.registrar("1", "Carlos Mendoza", "La Floresta", "MED-1001", "4");
        gHogares.registrar("2", "Ana Villacís", "Cumbayá", "MED-2002", "5");
        gHogares.registrar("3", "Luis Paredes", "Carcelén", "MED-3003", "2");

        Hogar h1 = gHogares.buscarPorId(1);
        Hogar h2 = gHogares.buscarPorId(2);
        Hogar h3 = gHogares.buscarPorId(3);

        // ----- Puntos de consumo -----
        // (hogar, ubicación, artefacto de la lista, nombre si es "Otros", caudal, duración min, veces al día)
        // Inodoro (L/descarga) y Lavadora (L/ciclo) no usan duración, por eso va vacía.
        gPuntos.registrar(h1, "Baño principal", "Ducha", "", "9.0", "5", "4");
        gPuntos.registrar(h1, "Cocina", "Grifo de cocina / lavabo", "", "5.0", "6", "3");
        gPuntos.registrar(h1, "Baño principal", "Inodoro", "", "6.0", "", "10");
        gPuntos.registrar(h1, "Lavandería", "Lavadora", "", "50.0", "", "1");

        gPuntos.registrar(h2, "Baño principal", "Ducha", "", "9.0", "6", "5");
        gPuntos.registrar(h2, "Baño social", "Grifo de cocina / lavabo", "", "5.0", "3", "10");
        gPuntos.registrar(h2, "Patio", "Manguera de jardín", "", "15.0", "15", "1");
        gPuntos.registrar(h2, "Patio", "Otros", "Bomba de agua", "20.0", "10", "1");

        gPuntos.registrar(h3, "Baño", "Ducha", "", "9.0", "5", "2");
        gPuntos.registrar(h3, "Cocina", "Grifo de cocina / lavabo", "", "5.0", "10", "2");

        // ----- Lecturas del medidor (fecha, hora, valor en m3) -----
        // Hogar 1: consumo normal (aprox. 383 L/día)
        gLecturas.registrar(h1, "2026-11-01", "07:00", "120.000");
        gLecturas.registrar(h1, "2026-11-01", "23:00", "120.250");
        gLecturas.registrar(h1, "2026-11-02", "05:00", "120.253");
        gLecturas.registrar(h1, "2026-11-02", "07:00", "120.380");
        gLecturas.registrar(h1, "2026-11-03", "07:00", "120.760");
        gLecturas.registrar(h1, "2026-11-04", "07:00", "121.150");

        // Hogar 2: consumo excesivo y consumo nocturno sospechoso (posible fuga)
        gLecturas.registrar(h2, "2026-11-01", "07:00", "80.000");
        gLecturas.registrar(h2, "2026-11-02", "07:00", "80.700");
        gLecturas.registrar(h2, "2026-11-02", "23:00", "80.950");
        gLecturas.registrar(h2, "2026-11-03", "05:00", "81.020");
        gLecturas.registrar(h2, "2026-11-03", "07:00", "81.080");

        // Hogar 3: consumo eficiente (aprox. 185 L/día)
        gLecturas.registrar(h3, "2026-11-01", "07:00", "45.000");
        gLecturas.registrar(h3, "2026-11-01", "23:00", "45.100");
        gLecturas.registrar(h3, "2026-11-02", "05:00", "45.103");
        gLecturas.registrar(h3, "2026-11-02", "07:00", "45.185");
        gLecturas.registrar(h3, "2026-11-03", "07:00", "45.370");

        // ----- Pruebas de fuga (lectura de la noche -> lectura de la madrugada, tolerancia 5 L) -----
        // Hogar 1: 3 L (normal) | Hogar 2: 70 L (posible fuga) | Hogar 3: 3 L (normal)
        gFugas.ejecutarPrueba(h1, h1.obtenerLecturas().get(1), h1.obtenerLecturas().get(2), "5", true);
        gFugas.ejecutarPrueba(h2, h2.obtenerLecturas().get(2), h2.obtenerLecturas().get(3), "5", true);
        gFugas.ejecutarPrueba(h3, h3.obtenerLecturas().get(1), h3.obtenerLecturas().get(2), "5", true);

        // ----- Metas de ahorro (inicio, fin, % de reducción) -----
        gMetas.crear(h1, "2026-11-01", "2026-11-30", "15");
        gMetas.crear(h1, "2026-11-01", "2026-11-30", "3");
        gMetas.crear(h2, "2026-11-01", "2026-11-30", "10");
        gMetas.crear(h3, "2026-11-01", "2026-11-30", "5");
    }

    /** Punto de entrada del programa: abre la ventana principal dentro del hilo de Swing. */
    public static void main(String[] args) {
        // Para que los decimales se muestren con punto (igual que al escribirlos)
        Locale.setDefault(Locale.US);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                // Si falla, se usa el aspecto por defecto de Swing
            }
            new VentanaPrincipal().setVisible(true);
        });
    }
}
