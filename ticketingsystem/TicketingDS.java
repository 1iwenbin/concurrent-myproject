package ticketingsystem;

import ticketingsystem.utils.BitHelper;
import ticketingsystem.utils.InnerTicket;
import ticketingsystem.utils.SectionRange;
import ticketingsystem.utils.bitoniccounter.BitonicCounter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class TicketingDS implements TicketingSystem {


    private int routeNum = 5;
    private int coachNum = 8;
    private int seatNum = 100;
    private int stationNum = 10;
    private int threadNum = 16;

    private BitonicCounter counter;
    private static final int fallbackThreshold = 10;
    private SectionRange range;
    private ConcurrentHashMap<Long, Ticket> record;


    private void initSide() {
        range = new SectionRange(routeNum, coachNum, seatNum, stationNum, threadNum);
        counter = new BitonicCounter((int) BitHelper.floor2power(threadNum));
        int initialCapacity = (int) (routeNum * coachNum * seatNum * stationNum * 0.5);
        record = new ConcurrentHashMap<>(initialCapacity, 0.75f, threadNum);
    }

    public TicketingDS() {
        initSide();
    }

    public TicketingDS(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.threadNum = threadNum;
        initSide();
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if (isIllegal(passenger, route, departure, arrival))
            return null;
        /* Randomly choose a ticket. */
        ThreadLocalRandom random = ThreadLocalRandom.current();
        InnerTicket it = new InnerTicket(route, 0, 0, departure, arrival);
        for (int i = 0 ; i < fallbackThreshold; i ++) {
            it.coach = random.nextInt(coachNum) + 1;
            if (it.coach > coachNum ) {
                System.out.printf("[1]Err: %d\n", it.coach);
            }
            it.seat = random.nextInt(seatNum) + 1;
            if (tryBuyTicket(it)) {
                return constructTicket(passenger, it);
            }
        }
        List<InnerTicket> mayAvailable = range.locateAvailable(route, departure, arrival);
        for (InnerTicket t : mayAvailable) {
            if (tryBuyTicket(t)) {
                return constructTicket(passenger, t);
            }
        }
        return null;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        if (isIllegal("Inquiry", route, departure, arrival)) {
            return 0;
        }
        return range.countAvailable(route, departure, arrival);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        if (ticket == null) {
            return false;
        }
        long tid = ticket.tid;
        InnerTicket it = new InnerTicket(ticket);
        /* Ticket is illegal */
        if (isIllegal(ticket) || range.isAvailable(it) || !record.containsKey(tid)) {
            return false;
        }
        /* Ticket is not exist */
        if (!isEqual(ticket, record.get(tid))) {
            return false;
        }
        /* Cancel record log */
        if (record.remove(tid) == null) {
            return false;
        }
        /* Actual free step */
        range.free(it);
        return true;
    }

    @Override
    public boolean buyTicketReplay(Ticket ticket) {
        return false;
    }

    @Override
    public boolean refundTicketReplay(Ticket ticket) {
        return false;
    }

    //ToDo

    private boolean tryBuyTicket(InnerTicket it) {
        if (range.isAvailable(it)) {
            try {
                range.lock(it);
                if (!range.isAvailable(it)) {
                    return false;
                }
                range.occupy(it);
                return true;
            } finally {
                range.unlock(it);
            }
        }
        return false;
    }

    private Ticket constructTicket(String passenger, InnerTicket it) {
        Ticket t = it.toTicket(counter.getNext(), passenger);
        record.put(t.tid, t);
        return t;
    }

    private boolean isEqual(Ticket a, Ticket b) {
        return !isIllegal(a) && !isIllegal(b) && a.tid == b.tid && !a.passenger.equals(b.passenger)
                && a.route == b.route && a.coach == b.coach && a.seat == b.seat && a.departure == b.departure && a.arrival == b.arrival;
    }

    private boolean isIllegal(Ticket t) {
        String passenger = t.passenger;
        long tid = t.tid;
        int route = t.route;
        int coach = t.coach;
        int seat = t.seat;
        int departure = t.departure;
        int arrival = t.arrival;
        return tid < 0 || coach <= 0 || coach > coachNum || seat <= 0 || seat > seatNum
                || isIllegal(passenger, route, departure, arrival);
    }

    private boolean isIllegal(String passenger, int route, int departure, int arrival) {
        return passenger == null || passenger.equals("") || route <= 0 || route > routeNum || departure <= 0
                || departure > stationNum || arrival <= 0 || arrival > stationNum || departure >= arrival;
    }
}
