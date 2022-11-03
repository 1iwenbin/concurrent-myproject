package ticketingsystem.utils;

import ticketingsystem.Ticket;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SectionRange {
    private Section[] sections;
    private int seatNum;
    private LocalHashMap hashMap;

    public SectionRange(int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.seatNum = seatNum;
        sections = new Section[stationNum - 1];
        for ( int i = 0; i < sections.length; i ++) {
            sections[i] = new Section(routeNum, coachNum, seatNum);
        }
        hashMap = new LocalHashMap(routeNum, stationNum);
    }

    public void lock(InnerTicket it) {
        int s = it.departure - 1;
        int e = it.arrival - 1;
        for ( int i = s; i < e; i ++) {
            sections[i].lock(it.route, it.coach, it.seat);
        }
    }

    public void unlock(InnerTicket it) {
        int s = it.departure - 1;
        int e = it.arrival - 1;
        for (int i = s; i < e; i++) {
            sections[i].unlock(it.route, it.coach, it.seat);
        }
    }

    public boolean isAvailable(InnerTicket it) {
        boolean ans = true;
        int s = it.departure - 1;
        int e = it.arrival - 1;
        for (int i = s; i < e ; i ++) {
            ans = sections[i].isAvailable(it.route, it.coach, it.seat);
            if (!ans) {
                return false;
            }
        }
        return true;
    }

    public void occupy(InnerTicket it) throws IllegalStateException {
        int s = it.departure - 1;
        int e = it.arrival - 1;
        for (int i = s; i < e; i ++) {
            sections[i].occupy(it.route, it.coach, it.seat);
        }
    }

    public void free(InnerTicket it) throws IllegalStateException {
        int s = it.departure - 1;
        int e = it.arrival - 1;
        for (int i = s; i < e; i ++) {
            sections[i].free(it.route, it.coach, it.seat);
        }
    }

    public long[] getCompressedBitMap(int route, int departure, int arrival, boolean clear) {
        int s = departure - 1; // TODO?
        int e = arrival - 1;
        long[] bitMap = sections[s].snapshot(route, clear);
        for (int i = s; i < e; i ++) {
            long[] bm = sections[i].snapshot(route, clear);
            assert bitMap.length == bm.length : "length of route bitMap is different between sections ";
            for (int j = 0; j < bitMap.length; j ++) {
                bitMap[j] |= bm[j];
            }
        }
        return bitMap;
    }

    private InnerTicket toInnerTicket(int index, int route, int departure, int arrival) {
        int seat = index % seatNum + 1;
        int coach = index / seatNum + 1;
        return new InnerTicket(route, coach, seat, departure, arrival);
    }

    public List<InnerTicket> locateAvailable(int route, int departure, int arrival) {
        ArrayList<InnerTicket> location = new ArrayList<>();
        long[] bitMap = getCompressedBitMap(route, departure, arrival, false);
        int size = Section.bitMapElementSize();
        for (int i = 0; i < bitMap.length; i ++) {
            List<Integer> l = BitHelper.locateZeros(bitMap[i]);
            for (int index : l) {
                location.add(toInnerTicket(index + i * size, route, departure, arrival));
            }
        }
        Collections.shuffle(location);
        return location;
    }


    public int countAvailable(int route, int departure, int arrival) {
        int available = 0;
        int s = departure - 1;
        int e = arrival - 1;
        long[] bitMap = getCompressedBitMap(route, departure, arrival, true);
        boolean isChanged = false;
        for (int i = s; i < e; i ++) {
            if (isChanged) {
                break;
            }
            isChanged = sections[i].isChanged();
        }
        if (isChanged || !hashMap.containsKey(route, departure, arrival)) {
            for (int i = 0; i < bitMap.length; i ++) {
                available += BitHelper.countZeros(bitMap[i]);
            }
            hashMap.put(route, departure, arrival, available);
        } else {
            available = hashMap.get(route, departure, arrival);
        }
        return available;

    }
}
