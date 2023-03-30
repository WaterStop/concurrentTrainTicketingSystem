package ticketingsystem;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TicketingDS implements TicketingSystem {
    private   int ROUTE; // 列车车次
    private   int SEAT; // 每节车厢座位数目
    private   int COACH; // 列车车厢
    private   int STATION;// 车站数目
    private static AtomicLong tId = new AtomicLong(1);// 分配车票的id
    // To do
    // 定义列车对象
    private static Train[] trains;
    // 构造方法
    public TicketingDS(int routeNum, int coachNum, int seatNum,
                       int stationNum, int threadNum) {
        this.ROUTE = routeNum;
        this.COACH = coachNum;
        this.SEAT = seatNum ;
        this.STATION = stationNum;
        // 实例化火车
        trains = new Train[ROUTE]; // 赋值的同时才实例化
        for (int i = 0; i < ROUTE; i++)
            trains[i] = new Train(i+1, COACH, SEAT, STATION);
    }

    // 查询记录由车次，起始站，终点站确定
    public  int inquiry (int route, int departure, int arrival)  {

        if (check(route,departure,arrival)) {//正确性验证

            return trains[route-1].trainInquiry(departure-1, arrival-1);

        }
        return 0;
    }
    //
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        if(check(route,departure, arrival)) {
            int ticBuy = trains[route - 1].trainBuy(departure - 1, arrival - 1);
            if (ticBuy != -1) {
                Ticket ticket = new Ticket();
                ticket.tid = tId.getAndIncrement();
                ticket.passenger = "passenger" + Long.toString(ticket.tid);
                ticket.route = route;
                ticket.coach = (ticBuy / SEAT) + 1; // 车厢号
                ticket.seat = (ticBuy % SEAT) + 1;
                ticket.departure = departure;
                ticket.arrival = arrival;
                return ticket;
            }
            return null;
        }
        return null;

    }
    public  boolean refundTicket(Ticket ticket) {
        if (check(ticket.route,ticket.departure,ticket.arrival)) {
            int seatId = (ticket.coach - 1) * SEAT + ticket.seat; // 还原座位号
            //ticket.tid = -1;// 退票后tid失效
            return trains[ticket.route - 1].trainRefund(seatId, ticket.departure - 1, ticket.arrival - 1);
        }
        return false;
    }
    // 对于输入数据的正确性验证
    public boolean check(int route,int departure,int arrival){
        if ((departure > 0) && (departure < arrival) && (arrival <= STATION)
                && (route > 0) && (route <= ROUTE)){
            return true;
        }
        System.out.println("check error!!");
        return false;
    }



    @Override
    public boolean buyTicketReplay(Ticket ticket) {
        return false;
    }

    @Override
    public boolean refundTicketReplay(Ticket ticket) {
        return false;
    }



}
