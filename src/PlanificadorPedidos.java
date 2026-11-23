import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlanificadorPedidos {

    public enum Politica { FIFO, SJF }

    private final List<Pedido> cola = new ArrayList<>();
    private final Politica politica;

    // Estadisticas para poder comparar las dos politicas.
    private long esperaTotal = 0;
    private int atendidos = 0;

    public PlanificadorPedidos(Politica politica) {
        this.politica = politica;
    }

    public synchronized void encolar(Pedido p) {
        cola.add(p);
        notifyAll();          // despierta a un hilo que este esperando trabajo
    }

    // Saca el siguiente pedido segun la politica. Se bloquea si no hay.
    public synchronized Pedido siguiente() throws InterruptedException {

        while (cola.isEmpty()) {
            wait();           // espera PASIVA: no gasta CPU
        }

        Pedido elegido;

        if (politica == Politica.FIFO) {
            // TODO: el primero que llego. pista: cola.remove(0)
            elegido = null;
        } else {
            // TODO: el mas corto primero.
            //       "Mas corto" en tu dominio = el de menor cantidad.
            //       pista: cola.stream().min(Comparator.comparingInt(Pedido::getCantidad))
            elegido = null;
        }

        cola.remove(elegido);
        esperaTotal += System.currentTimeMillis() - elegido.getHoraLlegada();
        atendidos++;
        return elegido;
    }

    public synchronized int pendientes() { return cola.size(); }

    public synchronized double esperaPromedio() {
        return atendidos == 0 ? 0 : (double) esperaTotal / atendidos;
    }
}
