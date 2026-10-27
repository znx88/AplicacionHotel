public class IngresoFecha {

    private final String fecha;
    private final int cantidad;
    private final double ingresos;

    public IngresoFecha(String fecha, int cantidad, double ingresos) {
        this.fecha = fecha;
        this.cantidad = cantidad;
        this.ingresos = ingresos;
    }

    public String getFecha() { return fecha; }
    public int getCantidad() { return cantidad; }
    public double getIngresos() { return ingresos; }

    public String getIngresosFormatted() {
        return String.format("%.2f €", ingresos);
    }
}