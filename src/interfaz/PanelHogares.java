package interfaz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import modelo.Hogar;
import negocio.GestorHogares;

/**
 * Pestaña 1: gestión de hogares (RF1) y estado del consumo ideal vs real (RF2).
 * Arriba va el formulario, en el centro la tabla de viviendas y abajo el detalle del hogar seleccionado.
 */
public class PanelHogares extends PanelBase {

    // gestorHogares: lógica de negocio de los hogares
    // selector: lista de viviendas de la parte superior (se recarga cuando cambian los hogares)
    private GestorHogares gestorHogares;
    private SelectorHogar selector;

    // Campos del formulario
    private JTextField txtId;
    private JTextField txtResponsable;
    private JTextField txtBarrio;
    private JTextField txtMedidor;
    private JTextField txtHabitantes;

    // Tabla de hogares y etiqueta con el detalle del consumo
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JLabel lblDetalle;

    /** Arma la pantalla (formulario, tabla y detalle) y carga la tabla con los hogares existentes. */
    public PanelHogares(GestorHogares gestorHogares, SelectorHogar selector) {
        this.gestorHogares = gestorHogares;
        this.selector = selector;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(crearFormulario(), BorderLayout.NORTH);
        add(crearTabla(), BorderLayout.CENTER);

        lblDetalle = new JLabel("Seleccione un hogar de la tabla para ver su consumo ideal y real.");
        lblDetalle.setBorder(BorderFactory.createTitledBorder("Consumo ideal vs consumo real"));
        add(lblDetalle, BorderLayout.SOUTH);

        actualizarTabla();
    }

    // ---------- Construcción de la pantalla ----------

    /** Crea el formulario con los campos y los botones, y conecta cada botón con su acción. */
    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new BorderLayout(5, 5));
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del hogar"));

        txtId = new JTextField();
        txtResponsable = new JTextField();
        txtBarrio = new JTextField();
        txtMedidor = new JTextField();
        txtHabitantes = new JTextField();

        // Campos en una cuadrícula de 3 filas x 4 columnas
        JPanel campos = new JPanel(new GridLayout(3, 4, 8, 8));
        campos.add(new JLabel("ID:"));
        campos.add(txtId);
        campos.add(new JLabel("Nombre del responsable:"));
        campos.add(txtResponsable);
        campos.add(new JLabel("Barrio:"));
        campos.add(txtBarrio);
        campos.add(new JLabel("Código de medidor:"));
        campos.add(txtMedidor);
        campos.add(new JLabel("Número de habitantes:"));
        campos.add(txtHabitantes);
        campos.add(new JLabel(""));
        campos.add(new JLabel(""));

        // Botones
        JButton btnRegistrar = new JButton("Registrar Hogar");
        JButton btnActualizar = new JButton("Actualizar Habitantes");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        botones.add(btnRegistrar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);

        // Eventos de los botones
        btnRegistrar.addActionListener(e -> registrarHogar());
        btnActualizar.addActionListener(e -> actualizarHabitantes());
        btnEliminar.addActionListener(e -> eliminarHogar());
        btnLimpiar.addActionListener(e -> limpiarCampos());

        return formulario;
    }

    /** Crea la tabla de hogares (no editable). */
    private JScrollPane crearTabla() {
        String[] columnas = {"ID", "Responsable", "Barrio", "Medidor", "Habitantes", "Estado Consumo"};

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Al seleccionar una fila, se llenan los campos del formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                mostrarHogarSeleccionado();
            }
        });
        return new JScrollPane(tabla);
    }

    // ---------- Acciones ----------

    /** Botón "Registrar Hogar": el gestor valida los datos; se muestra un mensaje de éxito o de error. */
    private void registrarHogar() {
        boolean ok = gestorHogares.registrar(txtId.getText(), txtResponsable.getText(),
                txtBarrio.getText(), txtMedidor.getText(), txtHabitantes.getText());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Hogar registrado correctamente.",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            actualizarTabla();
            selector.recargar();
        } else {
            mostrarError(gestorHogares.getMensajeError());
        }
    }

    /** Botón "Actualizar Habitantes": cambia los habitantes del hogar cuyo ID está en el formulario. */
    private void actualizarHabitantes() {
        boolean ok = gestorHogares.actualizarHabitantes(txtId.getText(), txtHabitantes.getText());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Número de habitantes actualizado.",
                    "Actualización exitosa", JOptionPane.INFORMATION_MESSAGE);
            actualizarTabla();
            selector.recargar();
        } else {
            mostrarError(gestorHogares.getMensajeError());
        }
    }

    /** Botón "Eliminar": elimina el hogar seleccionado en la tabla, después de pedir confirmación. */
    private void eliminarHogar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarError("Seleccione un hogar de la tabla para eliminarlo.");
            return;
        }
        int id = (Integer) modeloTabla.getValueAt(fila, 0);
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea eliminar el hogar " + id + " con todos sus datos?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            if (gestorHogares.eliminar(id)) {
                limpiarCampos();
                actualizarTabla();
                selector.recargar();
            } else {
                mostrarError(gestorHogares.getMensajeError());
            }
        }
    }

    /** Vacía los campos del formulario y quita la selección de la tabla. */
    private void limpiarCampos() {
        txtId.setText("");
        txtResponsable.setText("");
        txtBarrio.setText("");
        txtMedidor.setText("");
        txtHabitantes.setText("");
        tabla.clearSelection();
    }

    /** Copia los datos del hogar seleccionado al formulario y muestra su consumo ideal vs real (RF2). */
    private void mostrarHogarSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            lblDetalle.setText("Seleccione un hogar de la tabla para ver su consumo ideal y real.");
            return;
        }
        Hogar hogar = gestorHogares.buscarPorId((Integer) modeloTabla.getValueAt(fila, 0));
        if (hogar == null) {
            return;
        }
        txtId.setText(String.valueOf(hogar.getId()));
        txtResponsable.setText(hogar.getNombreResponsable());
        txtBarrio.setText(hogar.getBarrio());
        txtMedidor.setText(hogar.getCodigoMedidor());
        txtHabitantes.setText(String.valueOf(hogar.getHabitantes()));

        // Resultado de RF2: consumo ideal vs consumo real
        double ideal = gestorHogares.calcularConsumoIdeal(hogar);
        double real = gestorHogares.calcularConsumoRealDiario(hogar);
        if (real < 0) {
            lblDetalle.setText("Consumo ideal: " + String.format("%.1f", ideal)
                    + " L/día  |  No hay lecturas suficientes para calcular el consumo real.");
        } else {
            lblDetalle.setText("Consumo ideal: " + String.format("%.1f", ideal) + " L/día  |  Consumo real: "
                    + String.format("%.1f", real) + " L/día  |  Diferencia: "
                    + String.format("%+.1f", gestorHogares.calcularDiferencia(hogar)) + " L/día  |  Estado: "
                    + gestorHogares.obtenerEstadoConsumo(hogar));
        }
    }

    // ---------- Métodos auxiliares ----------

    /** Vuelve a llenar la tabla con los hogares del gestor. */
    public void actualizarTabla() {
        modeloTabla.setRowCount(0);
        for (Hogar h : gestorHogares.getHogares()) {
            modeloTabla.addRow(new Object[]{
                h.getId(),
                h.getNombreResponsable(),
                h.getBarrio(),
                h.getCodigoMedidor(),
                h.getHabitantes(),
                gestorHogares.obtenerEstadoConsumo(h)
            });
        }
    }

    /** Se llama al volver a esta pestaña: recarga la tabla. */
    @Override
    public void actualizar() {
        actualizarTabla();
    }

    /** Muestra un mensaje de error de validación con JOptionPane. */
    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
