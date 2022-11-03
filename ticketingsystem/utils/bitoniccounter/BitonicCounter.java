package ticketingsystem.utils.bitoniccounter;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLongArray;

public class BitonicCounter {
    private final Bitonic bc;
    private final AtomicLongArray counter;
    private final int width;

    public BitonicCounter(int width) {
        this.width = width;
        bc = new Bitonic(width);
        counter = new AtomicLongArray(width);
        for (int i = 0; i < counter.length(); i ++) {
            counter.set(i, i);
        }
    }

    public long getNext() {
        int input = ThreadLocalRandom.current().nextInt(width);
        int out = bc.traverse(input);
        return counter.getAndAdd(out, width);
    }
}
