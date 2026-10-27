import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HotelController {

    @FXML
    private Label lblInfo;

    @FXML
    private ListView<String> listaHabitaciones;

    @FXML
    private TextField txtNumero;

    @FXML
    private Label lblResultado;

    @FXML
    private DatePicker dateDesde;

    @FXML
    private DatePicker dateHasta;

    @FXML
    private Button btnIngresosFecha;

    @FXML
    private TableView<IngresoFecha> tablaIngresosFecha;

    // numero -> "estado|tipo|capacidad|precio"
    private Map<String, String[]> habitaciones = new HashMap<>();

    private List<Reserva> reservas = new ArrayList<>();

    @FXML
    public void initialize() {
        cargarXML();
        mostrarDatos();
    }

    private void cargarXML() {
        try {
            InputStream is = getClass().getResourceAsStream("reservas.xml");
            // Permite ejecutar también desde el IDE si el XML está en resources
            if (is == null) {
                is = getClass().getResourceAsStream("/reservas.xml");
            }
            Document doc = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(is);
            doc.getDocumentElement().normalize();

            String nombre = doc.getElementsByTagName("nombre").item(0).getTextContent();
            String ciudad = doc.getElementsByTagName("ciudad").item(0).getTextContent();
            lblInfo.setText("Hotel " + nombre + " (" + ciudad + ") - datos leídos desde reservas.xml");

            NodeList listaHab = doc.getElementsByTagName("habitacion");
            for (int i = 0; i < listaHab.getLength(); i++) {
                Element h = (Element) listaHab.item(i);
                String numero = h.getAttribute("numero");
                String estado = h.getAttribute("estado");
                String tipo = h.getElementsByTagName("tipo").item(0).getTextContent();
                String capacidad = h.getElementsByTagName("capacidad").item(0).getTextContent();
                String precio = h.getElementsByTagName("precioNoche").item(0).getTextContent();
                habitaciones.put(numero.trim(), new String[]{estado, tipo, capacidad, precio});
            }

            // Cargar reservas
            NodeList listaRes = doc.getElementsByTagName("reserva");
            for (int i = 0; i < listaRes.getLength(); i++) {
                Element r = (Element) listaRes.item(i);
                String codigo = r.getAttribute("codigo");
                String estado = r.getAttribute("estado");

                Element cliente = (Element) r.getElementsByTagName("cliente").item(0);
                String nombreCli = cliente.getElementsByTagName("nombre").item(0).getTextContent();
                String apellidosCli = cliente.getElementsByTagName("apellidos").item(0).getTextContent();
                String emailCli = cliente.getElementsByTagName("email").item(0).getTextContent();

                String habRes = r.getElementsByTagName("habitacionReservada").item(0).getTextContent();
                String entrada = r.getElementsByTagName("entrada").item(0).getTextContent();
                String salida = r.getElementsByTagName("salida").item(0).getTextContent();
                String personas = r.getElementsByTagName("personas").item(0).getTextContent();
                String obs = "";
                NodeList obsList = r.getElementsByTagName("observaciones");
                if (obsList.getLength() > 0) {
                    obs = obsList.item(0).getTextContent();
                }

                reservas.add(new Reserva(codigo, estado, nombreCli, apellidosCli, emailCli,
                        habRes, entrada, salida, personas, obs));
            }
        } catch (Exception e) {
            lblInfo.setText("Error al leer reservas.xml: " + e.getMessage());
        }
    }

    private void mostrarDatos() {
        listaHabitaciones.getItems().clear();
        for (Map.Entry<String, String[]> e : habitaciones.entrySet()) {
            String num = e.getKey();
            String[] d = e.getValue();
            listaHabitaciones.getItems().add(
                "Hab. " + num + " - " + d[1] + " (" + d[2] + " pers, " + d[3] + " EUR) - " + d[0]);
        }
    }

    @FXML
    private void buscar() {
        String numero = txtNumero.getText().trim();

        if (numero.isEmpty()) {
            lblResultado.setText("Escribe un número de habitación.");
            return;
        }

        if (!habitaciones.containsKey(numero)) {
            lblResultado.setText("La habitación " + numero + " no existe.");
            lblResultado.setStyle("-fx-text-fill: purple;");
            return;
        }

        String[] d = habitaciones.get(numero);
        String estado = d[0];
        String texto = "Hab. " + numero + " (" + d[1] + ", " + d[2]
                + " pers, " + d[3] + " EUR/noche): ";

        if (estado.equalsIgnoreCase("libre")
                || estado.equalsIgnoreCase("disponible")) {
            lblResultado.setText(texto + "DISPONIBLE.");
            lblResultado.setStyle("-fx-text-fill: green;");
        } else {
            lblResultado.setText(texto + "OCUPADA.");
            lblResultado.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void calcularIngresosFecha() {
        LocalDate desde = dateDesde.getValue();
        LocalDate hasta = dateHasta.getValue();

        // Filtrar reservas por rango de fechas (si se especifican)
        List<Reserva> filtradas = new ArrayList<>();
        for (Reserva r : reservas) {
            LocalDate fechaEntrada = parseFecha(r.getEntrada());
            if (fechaEntrada == null) continue;
            if (desde != null && fechaEntrada.isBefore(desde)) continue;
            if (hasta != null && fechaEntrada.isAfter(hasta)) continue;
            filtradas.add(r);
        }

        if (filtradas.isEmpty()) {
            lblResultado.setText("No hay reservas en el rango de fechas seleccionado.");
            lblResultado.setStyle("-fx-text-fill: orange;");
            tablaIngresosFecha.getItems().clear();
            return;
        }

        // Agrupar ingresos por fecha de entrada
        Map<String, Integer> conteoPorFecha = new HashMap<>();
        Map<String, Double> ingresosPorFecha = new HashMap<>();
        for (Reserva r : filtradas) {
            String fecha = r.getEntrada();
            conteoPorFecha.put(fecha, conteoPorFecha.getOrDefault(fecha, 0) + 1);
            double ing = calcularIngresosReserva(r);
            ingresosPorFecha.put(fecha, ingresosPorFecha.getOrDefault(fecha, 0.0) + ing);
        }

        // Llenar tabla
        ObservableList<IngresoFecha> datos = FXCollections.observableArrayList();
        int totalCantidad = 0;
        double totalIngresos = 0;
        for (Map.Entry<String, Integer> e : conteoPorFecha.entrySet()) {
            String fecha = e.getKey();
            int cant = e.getValue();
            double ing = ingresosPorFecha.getOrDefault(fecha, 0.0);
            datos.add(new IngresoFecha(fecha, cant, ing));
            totalCantidad += cant;
            totalIngresos += ing;
        }
        datos.add(new IngresoFecha("TOTAL", totalCantidad, totalIngresos));
        tablaIngresosFecha.setItems(datos);

        lblResultado.setText(String.format("Ingresos calculados: %d reservas, %.2f € totales",
                totalCantidad, totalIngresos));
        lblResultado.setStyle("-fx-text-fill: green;");
    }

    private double calcularIngresosReserva(Reserva r) {
        String habNum = r.getHabitacionReservada().trim();
        String[] habData = habitaciones.get(habNum);
        if (habData == null) return 0.0;

        double precioNoche = Double.parseDouble(habData[3]);
        LocalDate entrada = parseFecha(r.getEntrada());
        LocalDate salida = parseFecha(r.getSalida());
        if (entrada == null || salida == null) return 0.0;

        long noches = java.time.temporal.ChronoUnit.DAYS.between(entrada, salida);
        if (noches <= 0) noches = 1;

        return precioNoche * noches;
    }

    private LocalDate parseFecha(String fechaStr) {
        try {
            return LocalDate.parse(fechaStr, DateTimeFormatter.ISO_DATE);
        } catch (Exception e) {
            try {
                return LocalDate.parse(fechaStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            } catch (Exception ex) {
                return null;
            }
        }
    }
}
