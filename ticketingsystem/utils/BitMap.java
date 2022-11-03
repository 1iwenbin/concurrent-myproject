package ticketingsystem.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;

public class BitMap {
    private AtomicLongArray map;

    private static final int longSize = Long.SIZE;

    private int size;

    public BitMap(int size) {
        this.size = size;
        int mapSize = (size + longSize - 1) / longSize;
        map = new AtomicLongArray(mapSize);
        int remainSize = mapSize * longSize - size;
        int begin = longSize - remainSize;
        long old = map.get(map.length() - 1);
        map.set(map.length() - 1, BitHelper.setRange(old, begin, longSize));
    }

    public static int elementSize() {
        return longSize;
    }

    public void set(int index) throws ArrayIndexOutOfBoundsException {
        if (index >= size || index < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int mapIndex = index / longSize;
        int i = index % longSize;
        for (;;) {
            long oldVal = map.get(mapIndex);
            long newVal = BitHelper.set(oldVal, i);
            if (map.compareAndSet(mapIndex, oldVal, newVal)) {
                break;
            }
        }
    }

    public void reset(int index) throws ArrayIndexOutOfBoundsException {
        if (index >= size || index < 0) {
            throw new ArrayIndexOutOfBoundsException();
        }
        int mapIndex = index / longSize;
        int i = index % longSize;
        for (;;) {
            long oldVal = map.get(mapIndex);
            long newVal = BitHelper.reset(oldVal, i);
            if (map.compareAndSet(mapIndex, oldVal, newVal)) {
                break;
            }
        }
    }

    public long[] rawSnapshot() {
        long[] snapshot = new long[map.length()];
        for ( int i = 0; i < snapshot.length; i ++) {
            snapshot[i] = map.get(i);
        }
        return snapshot;
    }
}
