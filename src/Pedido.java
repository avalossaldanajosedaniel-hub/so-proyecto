
public class Pedido {

    private int id;
    private String producto;
    private int cantidad;
    private String estado;

    // Semana 10: momento en que se crea el pedido
    private final long horaLlegada;

    public Pedido(int id, String producto, int cantidad) {
        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;
        this.estado = "PENDIENTE";

        // Registrar el instante de llegada en milisegundos
        this.horaLlegada = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public String getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getEstado() {
        return estado;
    }

    // Semana 10: consultar el momento de llegada
    public long getHoraLlegada() {
        return horaLlegada;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return id + ";" + producto + ";" + cantidad
                + ";" + estado;
    }
}
