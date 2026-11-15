package interfaz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import modelo.Hogar;
import negocio.ComparacionConsumo;
import negocio.GestorHogares;

public class PanelIdealReal extends PanelBase {

    private final GestorHogares gestorHogares;
    private final SelectorHogar selector;
    private final JTextField txtDesde;
    private final JTextField txtHasta;
    private final JTextArea areaResumen;

    public PanelIdealReal(GestorHogares gestorHogares, SelectorHogar selector) {
        this.gestorHogares = gestorHogares;
        this.selector = selector;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtDesde = new JTextField(LocalDate.now().minusDays(7).toString(), 10);
        txtHasta = new JTextField(LocalDate.now().toString(), 10);
        JButton btnComparar = new JButton("Comparar consumo real vs ideal (OMS 100 L/persona/día)");
        controles.add(new JLabel("Desde (AAAA-MM-DD):"));
        controles.add(txtDesde);
        controles.add(new JLabel("Hasta (AAAA-MM-DD):"));
        controles.add(txtHasta);
        controles.add(btnComparar);
        btnComparar.addActionListener(e -> calcular());

        areaResumen = new JTextArea();
        areaResumen.setEditable(false);
        areaResumen.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 14));

        add(controles, BorderLayout.NORTH);
        add(new JScrollPane(areaResumen), BorderLayout.CENTER);
    }

    private void calcular() {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            areaResumen.setText("Registre y seleccione una vivienda en la barra superior.");
            return;
        }

        LocalDate desde;
        LocalDate hasta;
        try {
            desde = LocalDate.parse(txtDesde.getText().trim());
            hasta = LocalDate.parse(txtHasta.getText().trim());
        } catch (DateTimeParseException e) {
            areaResumen.setText("Fecha inválida. Use el formato AAAA-MM-DD (por ejemplo 2026-11-05).");
            return;
        }

        ComparacionConsumo comparacion = gestorHogares.comparar(hogar, desde, hasta);
        if (comparacion == null) {
            areaResumen.setText(gestorHogares.getMensajeError());
            return;
        }
        areaResumen.setText(comparacion.resumen());
    }

    @Override
    public void actualizar() {
        calcular();
    }
}
