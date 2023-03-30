package ticketingsystem;

public class Train {
    private int trainSeat;// 火车总座位数目
    private Seat[] trainSeats; // 火车的座位
 //   private int[][] remainingTicket; // 指定始发站的剩余的票数
    // 构造方法
    public Train(int route, int coach, int seat, int station ){
        this.trainSeat = coach * seat; // 每个火车内的总座位数
        // 实例化座位
        trainSeats = new Seat[trainSeat];
        for(int i = 0; i < trainSeat; i++){
            trainSeats[i] = new Seat(i);

        }
    }

    // 查询列车的余座
    public int trainInquiry(int departure,int arrival) {
        int remaining = 0;// 余票
        for (int i = 0; i < trainSeat; i++) {
            if (trainSeats[i].seatInquiry(departure,arrival))
                remaining++;
        }
        return remaining;

    }

    // 有票就买上
    public int trainBuy(int departure,int arrival){
        for (int i = 0; i < trainSeat; i++)
            // 座位有空并且买成功
            if (trainSeats[i].seatInquiry(departure, arrival) && trainSeats[i].seatBuy(departure, arrival)) {
//                for(int j = departure; j < arrival; j++ )
//                    remainingTicket[departure][j]--;
                return trainSeats[i].getSeatID();
            }
        return -1; // 其他情况
    }

    // 退票
    public boolean trainRefund(int seatId,int departure,int arrival) {
       if (trainSeats[seatId - 1].seatInquiry(departure, arrival)) {
            return false;// 本来就没人，说明错了
       } else {
            trainSeats[seatId - 1].seatRefund(departure, arrival);
//            for(int j = departure+1; j < arrival; j++ )
//                remainingTicket[departure][j]++;
            return true;
       }
    }




}
