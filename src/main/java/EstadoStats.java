public class EstadoStats {

    private final String estado;
    private final int cantidad;
    private final double ingresos;

    public EstadoStats(String estado, int cantidad, double ingresos) {
        this.estado = estado;
        this.cantidad = cantidad;
        this.ingresos = ingresos;
    }

    public String getEstado() { return estado; }
    public int getCantidad() { return cantidad; }
    public double getIngresos() { return ingresos; }

    public String getIngresosFormatted() {
        return String.format("%.2f €", ingresos);
    }
}