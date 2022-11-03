package ticketingsystem.utils;

public class Section {
    private int routeTotal;
    private int coachTotal;
    private int seatTotal;
    private Seat[] sectionSeats;
    private BitMap[] bitMaps;
    private boolean isChanged;


    public Section(int routeTotal, int coachTotal, int seatTotal) {
        this.coachTotal = coachTotal;
        this.routeTotal = routeTotal;
        this.seatTotal = seatTotal;
        this.isChanged = false;
        sectionSeats = new Seat[routeTotal * coachTotal * seatTotal];

        for (int i = 0; i < sectionSeats.length; i ++) {
            sectionSeats[i] = new Seat();
        }
        bitMaps = new BitMap[routeTotal];
        for (int i = 0; i < bitMaps.length; i ++) {
            bitMaps[i] = new BitMap(coachTotal * seatTotal);
        }
    }

    public static int bitMapElementSize() {
        return BitMap.elementSize();
    }

    public void lock(int route, int coach, int seat) {
        int index = this.getSeatIndex(route, coach, seat);
        sectionSeats[index].lock();
    }

    public void unlock(int route, int coach, int seat) {
        int index = this.getSeatIndex(route, coach ,seat);
        sectionSeats[index].unlock();
    }

    public void occupy(int route, int coach, int seat) throws IllegalStateException {
        int index = this.getSeatIndex(route, coach, seat);
        sectionSeats[index].occupy();
        if (getBitIndex(coach, seat) > 800 ) {
            System.out.printf("[2]Err: coach: %d, seat: %d\n", coach, seat);
        }
        bitMaps[route - 1].set(getBitIndex(coach, seat));
        isChanged = true;
    }

    public void free(int route, int coach, int seat) throws IllegalStateException {
        int index = this.getSeatIndex(route, coach, seat);
        bitMaps[route - 1].reset(getBitIndex(coach, seat));
        sectionSeats[index].free();
        isChanged = true;
    }

    private int getSeatIndex(int route, int coach, int seat) {
        route -= 1;
        coach -= 1;
        seat -= 1;
        return route * coachTotal * seatTotal + coach * seatTotal + seat;
    }

    public boolean isAvailable(int route, int coach, int seat) {
        return sectionSeats[this.getSeatIndex(route, coach, seat)].isAvailable();
    }

    public long[] snapshot(int route, boolean clear) {
        if (clear) {
            this.isChanged = false;
        }
        return bitMaps[route - 1].rawSnapshot();
    }

    public boolean isChanged() {
        return isChanged;
    }

    private int getBitIndex(int coach, int seat) {
        coach -= 1;
        seat -= 1;
        return coach * seatTotal + seat;
    }
}
