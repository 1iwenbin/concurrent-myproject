package ticketingsystem.utils.bitoniccounter;


import java.util.concurrent.atomic.AtomicBoolean;

public class Balancer {
    private AtomicBoolean toggle;

    public Balancer() {
        this.toggle = new AtomicBoolean(true);
    }

    public int traverse() {
        for (;;) {
            boolean old = toggle.get();
            if (toggle.compareAndSet(old, !old)) {
                return (old ? 0 : 1);
            }
        }
    }
}


