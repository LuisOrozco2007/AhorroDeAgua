package interfaz;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Objects;
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
import modelo.LecturaMedidor;
import negocio.GestorLecturas;


public class PanelLecturas extends PanelBase {

    private SelectorHogar selector;
    private GestorLecturas gestorLecturas;

    private JTextField txtFecha;
    private JTextField txtHora;
    private JTextField txtValor;
    private JTextField txtDesde;
    private JTextField txtHasta;

    private JTable tabla;
    private DefaultTableModel modeloTabla;


    private ArrayList<LecturaMedidor> mostradas = new ArrayList<>();
    private LocalDate filtroDesde = null;
    private LocalDate filtroHasta = null;
    private Integer hogarMostradoId;

    private DateTimeFormatter formatoFechaHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private DateTimeFormatter formatoHora = DateTimeFormatter.ofPattern("HH:mm");

    public PanelLecturas(SelectorHogar selector, GestorLecturas gestorLecturas) {
        this.selector = selector;
        this.gestorLecturas = gestorLecturas;

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(crearFormulario(), BorderLayout.NORTH);
        add(crearZonaTabla(), BorderLayout.CENTER);

        actualizar();
        limpiarCampos();
    }



    private JPanel crearFormulario() {
        JPanel formulario = new JPanel(new BorderLayout(5, 5));
        formulario.setBorder(BorderFactory.createTitledBorder("Lectura del medidor"));

        txtFecha = new JTextField();
        txtHora = new JTextField();
        txtValor = new JTextField();

        JPanel campos = new JPanel(new GridLayout(2, 3, 8, 8));
        campos.add(new JLabel("Fecha (AAAA-MM-DD):"));
        campos.add(txtFecha);
        campos.add(new JLabel("Hora (HH:MM):"));
        campos.add(txtHora);
        campos.add(new JLabel("Valor acumulado (m³):"));
        campos.add(txtValor);

        JButton btnRegistrar = new JButton("Registrar Lectura");
        JButton btnModificar = new JButton("Modificar");
        JButton btnEliminar = new JButton("Eliminar");
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        botones.add(btnRegistrar);
        botones.add(btnModificar);
        botones.add(btnEliminar);

        formulario.add(campos, BorderLayout.CENTER);
        formulario.add(botones, BorderLayout.SOUTH);

        // Eventos
        btnRegistrar.addActionListener(e -> registrarLectura());
        btnModificar.addActionListener(e -> modificarLectura());
        btnEliminar.addActionListener(e -> eliminarLectura());

        return formulario;
    }

    private JPanel crearZonaTabla() {
        JPanel zona = new JPanel(new BorderLayout(5, 5));


        txtDesde = new JTextField(10);
        txtHasta = new JTextField(10);
        JButton btnFiltrar = new JButton("Filtrar");
        JButton btnVerTodo = new JButton("Ver todo");

        JPanel filtro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 3));
        filtro.setBorder(BorderFactory.createTitledBorder("Consultar historial por periodo"));
        filtro.add(new JLabel("Desde (AAAA-MM-DD):"));
        filtro.add(txtDesde);
        filtro.add(new JLabel("Hasta (AAAA-MM-DD):"));
        filtro.add(txtHasta);
        filtro.add(btnFiltrar);
        filtro.add(btnVerTodo);

        btnFiltrar.addActionListener(e -> filtrar());
        btnVerTodo.addActionListener(e -> {
            txtDesde.setText("");
            txtHasta.setText("");
            filtroDesde = null;
            filtroHasta = null;
            actualizarTabla();
        });

        String[] columnas = {"ID", "Fecha/Hora", "Acumulado (m³)", "Consumo del Periodo (litros)", "Observación"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tabla = new JTable(modeloTabla);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        tabla.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                copiarFilaAlFormulario();
            }
        });

        zona.add(filtro, BorderLayout.NORTH);
        zona.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return zona;
    }


    private void registrarLectura() {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        boolean ok = gestorLecturas.registrar(hogar, txtFecha.getText(), txtHora.getText(), txtValor.getText());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Lectura registrada correctamente.",
                    "Registro exitoso", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            actualizarTabla();
        } else {
            mostrarError(gestorLecturas.getMensajeError());
        }
    }

    private void modificarLectura() {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarError("Seleccione una lectura de la tabla y cambie sus datos en el formulario.");
            return;
        }
        LecturaMedidor lectura = mostradas.get(fila);
        boolean ok = gestorLecturas.actualizar(hogar, lectura.getId(),
                txtFecha.getText(), txtHora.getText(), txtValor.getText());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Lectura modificada. Los consumos fueron recalculados.",
                    "Modificación exitosa", JOptionPane.INFORMATION_MESSAGE);
            limpiarCampos();
            actualizarTabla();
        } else {
            mostrarError(gestorLecturas.getMensajeError());
        }
    }

    private void eliminarLectura() {
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            mostrarError("Debe seleccionar un hogar.");
            return;
        }
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            mostrarError("Seleccione una lectura de la tabla para eliminarla.");
            return;
        }
        LecturaMedidor lectura = mostradas.get(fila);
        int respuesta = JOptionPane.showConfirmDialog(this,
                "¿Seguro que desea eliminar la lectura " + lectura.getId() + "?",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION);
        if (respuesta == JOptionPane.YES_OPTION) {
            if (gestorLecturas.eliminar(hogar, lectura.getId())) {
                limpiarCampos();
                actualizarTabla();
            } else {
                mostrarError(gestorLecturas.getMensajeError());
            }
        }
    }

    private void filtrar() {
        LocalDate desde = null;
        LocalDate hasta = null;
        try {
            if (!txtDesde.getText().trim().isEmpty()) {
                desde = LocalDate.parse(txtDesde.getText().trim());
            }
            if (!txtHasta.getText().trim().isEmpty()) {
                hasta = LocalDate.parse(txtHasta.getText().trim());
            }
        } catch (DateTimeParseException e) {
            mostrarError("Fecha inválida en el filtro. Use el formato AAAA-MM-DD.");
            return;
        }
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            mostrarError("La fecha 'Desde' no puede ser posterior a la fecha 'Hasta'.");
            return;
        }
        filtroDesde = desde;
        filtroHasta = hasta;
        actualizarTabla();
    }


    private void copiarFilaAlFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= mostradas.size()) {
            return;
        }
        LecturaMedidor l = mostradas.get(fila);
        txtFecha.setText(l.getFecha().toString());
        txtHora.setText(l.getFechaHora().format(formatoHora));
        txtValor.setText(String.valueOf(l.getAcumuladoM3()));
    }

    private void limpiarCampos() {
        txtFecha.setText(LocalDate.now().toString());
        txtHora.setText(LocalTime.now().format(formatoHora));
        txtValor.setText("");
        tabla.clearSelection();
    }


    private void actualizarTabla() {
        modeloTabla.setRowCount(0);
        mostradas.clear();
        Hogar hogar = selector.getHogar();
        if (hogar == null) {
            return;
        }
        mostradas.addAll(gestorLecturas.consultar(hogar, filtroDesde, filtroHasta));
        for (LecturaMedidor l : mostradas) {
            double consumo = gestorLecturas.consumoPeriodoLitros(hogar, l);
            modeloTabla.addRow(new Object[]{
                l.getId(),
                l.getFechaHora().format(formatoFechaHora),
                String.format("%.3f", l.getAcumuladoM3()),
                consumo < 0 ? "-" : String.format("%.0f", consumo),
                l.getObservacion()
            });
        }
    }

    @Override
    public void actualizar() {
        Hogar hogar = selector.getHogar();
        Integer idHogar = hogar == null ? null : hogar.getId();
        if (!Objects.equals(hogarMostradoId, idHogar)) {
            hogarMostradoId = idHogar;
            limpiarCampos();
        }
        actualizarTabla();
    }

    private void mostrarError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error de validación", JOptionPane.ERROR_MESSAGE);
    }
}
