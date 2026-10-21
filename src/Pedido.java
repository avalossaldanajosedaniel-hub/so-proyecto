
public class Pedido {

    private int id;
    private String producto;
    private int cantidad;
    private String estado;

    public Pedido(int id, String producto, int cantidad) {
        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;
        this.estado = "PENDIENTE";
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

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return id + ";" + producto + ";" + cantidad
                + ";" + estado;
    }
}
