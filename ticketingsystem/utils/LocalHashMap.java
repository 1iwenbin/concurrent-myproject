package ticketingsystem.utils;

import java.util.concurrent.atomic.AtomicStampedReference;

public class LocalHashMap {
    class AtomicStampedInteger {
        private final AtomicStampedReference<Integer> item;

        public AtomicStampedInteger(int initInteger, int initStamp) {
            item = new AtomicStampedReference<>(initInteger, initStamp);
        }

        public boolean compareAndSet(int expectedInteger, int newInteger, int expectedStamp, int newStamp) {
            return this.item.compareAndSet(expectedInteger, newInteger, expectedStamp, newStamp);
        }

        public int getInteger() {
            return item.getReference();
        }

        public int get(int[] stampHolder) {
            return item.get(stampHolder);
        }
    }

    private final AtomicStampedInteger[] record;
    private final int groupSize;
    private final int[] innerGroupOffset;

    public LocalHashMap(int route, int stationNum) {
        int groupSize = stationNum * (stationNum - 1) / 2;
        this.groupSize = groupSize;
        this.innerGroupOffset = new int[stationNum - 1];
        for (int i = 1, offset = stationNum - 1; i < this.innerGroupOffset.length; i ++, offset --) {
            innerGroupOffset[i] = innerGroupOffset[i - 1] + offset;
        }
        record = new AtomicStampedInteger[route * groupSize];

        for (int i = 0 ; i < record.length; i ++) {
            record[i] = new AtomicStampedInteger(-1, Integer.MIN_VALUE);
        }
    }

    public boolean containsKey(int route, int departure, int arrival) {
        int index = getIndex(route, departure, arrival);
        int ans = record[index].getInteger();
        return ans != -1;
    }

    public void put(int route, int departure, int arrival, int value) {
        int index = getIndex(route, departure, arrival);
        int[] stampHolder = { 0 };
        int oldVal = record[index].get(stampHolder);
        record[index].compareAndSet(oldVal, value, stampHolder[0], stampHolder[0] + 1);
    }

    public int get(int route, int departure, int arrival) {
        int index = getIndex(route, departure, arrival);
        return record[index].getInteger();
    }

    private int getIndex(int route, int departure, int arrival) {
        return (route - 1) * groupSize + innerGroupOffset[arrival - departure - 1] + departure - 1;
    }
}
