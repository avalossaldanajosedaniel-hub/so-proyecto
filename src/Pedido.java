
public class Pedido {
    private int id;
    private String producto;
    private int cantidad;

    public Pedido(int id, String producto, int cantidad) {
        this.id = id;
        this.producto = producto;
        this.cantidad = cantidad;
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

    @Override
    public String toString() {
        return id + ";" + producto + ";" + cantidad;
    }
}

