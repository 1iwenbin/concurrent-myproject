package ticketingsystem.utils;


import ticketingsystem.Ticket;
import ticketingsystem.TicketingDS;

public class InnerTicket {

    public int route;
    public int coach;
    public int seat;
    public int departure;
    public int arrival;

    public InnerTicket(int route, int coach, int seat, int departure, int arrival) {
        this.route = route;
        this.seat = seat;
        this.coach = coach;
        this.departure = departure;
        this.arrival = arrival;
    }

    public InnerTicket(int route, int coach, int seat) {
        this.route = route;
        this.coach = coach;
        this.seat = seat;
    }

    public InnerTicket(Ticket t) {
        this.route = t.route;
        this.coach = t.coach;
        this.seat = t.seat;
        this.departure = t.departure;
        this.arrival = t.arrival;
    }

    public Ticket toTicket(long tid, String passenger) {
        return new Ticket(tid, passenger, route, coach, seat, departure, arrival);
    }
}
