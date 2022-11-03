package ticketingsystem;

public class Ticket {
    public long tid;
    public String passenger;

    public int route;
    public int coach;
    public int seat;
    public int departure;
    public int arrival;

    public Ticket() {}
    public Ticket(long tid, String passenger, int route, int coach, int seat, int departure, int arrival) {
        this.tid = tid;
        this.passenger = passenger;
        this.route = route;
        this.coach = coach;
        this.seat = seat;
        this.departure = departure;
        this.arrival = arrival;
    }
}
