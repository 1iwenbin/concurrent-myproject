package ticketingsystem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {

	private final static int ROUTE_NUM = 5;

	private final static int COACH_NUM = 8;

	private final static int SEAT_NUM = 100;

	private final static int STATION_NUM = 10;

	/* -*-*-*-*-*-*-*-*-*-*-*-*- */

	private final static int TEST_NUM = 50000;

	private final static int refund = 10;

	private final static int buy = 40;

	private final static int query = 100;

	private final static int thread = 64;

	private final static long[] buyTicketTime = new long[thread];

	private final static long[] refundTime = new long[thread];

	private final static long[] inquiryTime = new long[thread];

	private final static long[] buyTotal = new long[thread];

	private final static long[] refundTotal = new long[thread];

	private final static long[] inquiryTotal = new long[thread];

	private final static AtomicInteger threadId = new AtomicInteger(0);

	static String passengerName() {
		Random random = new Random();
		long uid = random.nextInt(TEST_NUM);
		return "passenger" + uid;
	}


	public static void main(String[] args) throws InterruptedException {

		final int[] threadNums = { 4, 8, 16, 32, 64 };
		int p;
		for ( p = 0; p < threadNums.length; p ++) {
			final TicketingDS tds = new TicketingDS(ROUTE_NUM, COACH_NUM, SEAT_NUM, STATION_NUM, threadNums[p]);
			Thread[] threads = new Thread[threadNums[p]];
			for ( int i = 0; i < threadNums[p]; i ++) {
				threads[i] = new Thread(() -> {
					Random rand = new Random();
					Ticket ticket;
					int id = threadId.getAndIncrement();
					ArrayList<Ticket> soldTicket = new ArrayList<>();
					for (int j = 0; j < TEST_NUM; j ++) {
						int sel = (rand.nextInt(query)) % query;
						if (sel < refund && soldTicket.size() > 0) {
							int select = (rand.nextInt(soldTicket.size())) % soldTicket.size();
							if ((ticket = soldTicket.remove(select)) != null) {
								long s = System.nanoTime();
								tds.refundTicket(ticket);
								long e = System.nanoTime();
								refundTime[id] += e - s;
								refundTotal[id] += 1;
							} else {
								System.out.println("ErrOfRefund2");
							}
						} else if (refund <= sel && sel < buy) {
							String passenger = passengerName();
							int route = (rand.nextInt(ROUTE_NUM)) % ROUTE_NUM + 1;
							int departure = rand.nextInt(STATION_NUM - 1) + 1;
							int arrival = departure + rand.nextInt(STATION_NUM - departure) + 1;
							long s = System.nanoTime();
							ticket = tds.buyTicket(passenger, route, departure, arrival);
							long e = System.nanoTime();
							buyTicketTime[id] += e - s;
							buyTotal[id] += 1;
							if (ticket != null) {
								soldTicket.add(ticket);
							}
						} else if (buy <= sel) {
							int route = rand.nextInt(ROUTE_NUM) + 1;
							int departure = rand.nextInt(ROUTE_NUM - 1) + 1;
							int arrival = departure + rand.nextInt(STATION_NUM - departure) + 1;
							long s = System.nanoTime();
							tds.inquiry(route, departure, arrival);
							long e = System.nanoTime();
							inquiryTime[id] += e - s;
							inquiryTotal[id] += 1;
						}
					}
				});
			}
			long start = System.currentTimeMillis();
			for (int j = 0; j < threadNums[p]; j ++) {
				threads[j].start();
			}
			for (int j = 0; j < threadNums[p]; j ++) {
				threads[j].join();
			}
			long end = System.currentTimeMillis();
			long buyTotalTime = Arrays.stream(buyTicketTime).sum();
			long refundTotalTime = Arrays.stream(refundTime).sum();
			long inquiryTotalTime = Arrays.stream(inquiryTime).sum();

			double bTotal = (double) Arrays.stream(buyTotal).sum();
			double rTotal = (double) Arrays.stream(refundTotal).sum();
			double iTotal = (double) Arrays.stream(inquiryTotal).sum();

			long buyAvgTime = (long) (buyTotalTime / bTotal);
			long refundAvgTime = (long) (refundTotalTime / rTotal);
			long inquiryAvgTime = (long) (inquiryTotalTime / iTotal);

			long time = end - start;

			long t = (long) (threadNums[p] * TEST_NUM / (double) time) * 1000;

			System.out.printf(
					"ThreadNum: %d BuyAvgTime(ns): %d RefundAvgTime(ns): %d InquiryAvgTime(ns): %d ThroughOut(t/s): %d\n",
					threadNums[p], buyAvgTime, refundAvgTime, inquiryAvgTime, t);

			clear();
		}



	}

	private static void clear() {
		threadId.set(0);
		long[][] arrays = { buyTicketTime, refundTime, inquiryTime, buyTotal, refundTotal, inquiryTotal };
		for (long[] array : arrays) Arrays.fill(array, 0);
	}
}
