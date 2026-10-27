public class Reserva {

    private final String codigo;
    private final String estado;
    private final String clienteNombre;
    private final String clienteApellidos;
    private final String clienteEmail;
    private final String habitacionReservada;
    private final String entrada;
    private final String salida;
    private final String personas;
    private final String observaciones;

    public Reserva(String codigo, String estado, String clienteNombre, String clienteApellidos,
                   String clienteEmail, String habitacionReservada, String entrada, String salida,
                   String personas, String observaciones) {
        this.codigo = codigo;
        this.estado = estado;
        this.clienteNombre = clienteNombre;
        this.clienteApellidos = clienteApellidos;
        this.clienteEmail = clienteEmail;
        this.habitacionReservada = habitacionReservada;
        this.entrada = entrada;
        this.salida = salida;
        this.personas = personas;
        this.observaciones = observaciones;
    }

    public String getCodigo() { return codigo; }
    public String getEstado() { return estado; }
    public String getClienteNombre() { return clienteNombre; }
    public String getClienteApellidos() { return clienteApellidos; }
    public String getClienteEmail() { return clienteEmail; }
    public String getHabitacionReservada() { return habitacionReservada; }
    public String getEntrada() { return entrada; }
    public String getSalida() { return salida; }
    public String getPersonas() { return personas; }
    public String getObservaciones() { return observaciones; }

    @Override
    public String toString() {
        return "Reserva{" +
                "codigo='" + codigo + '\'' +
                ", estado='" + estado + '\'' +
                ", cliente=" + clienteNombre + " " + clienteApellidos +
                ", habitacion=" + habitacionReservada +
                ", entrada=" + entrada + ", salida=" + salida +
                '}';
    }
}