package ticketingsystem;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Seat {
    private  int seatId;// 座位id
    private AtomicInteger seatOccupy; // 每一个二进制位标识经过车站的座位占用情况，第i位为true标识该座位在[i,i+1]被占用
    //private boolean using; // 表示车座是否占用，没占用可以直接买，被占用就可能退
    //private AtomicBoolean using;// 是否正在使用

    // 构造函数
    public Seat(int id) {
        this.seatId = id;
        seatOccupy = new AtomicInteger(0);
        //using = new AtomicBoolean(false) ;
   //     using = true;
    }


    // 二进制标识火车占用的区间的核心就是((1<<(arrival)) - (1<<departure))来使经过的区间二进制为1其余为0
    // 查询空座,有空位是true，没空位是false
    public boolean seatInquiry(int departure,int arrival ) { // 多加一个参数站总数用于正确性判断
        // 如果座位在所要经过的车站被占用则false
        if( (((1<<(arrival)) - (1<<departure)) & seatOccupy.get()) !=  0)
            return false;
        return true;
    }

    // 买票
    public boolean seatBuy(int departure,int arrival ) { // 多加一个参数站总数用于正确性判断
        // 买票，其中第i位表示经过[i,i+1]区间的车站座位被占用
        // 从0号站到5号站，(100000-000001)-(000010-000001) =011111
        //seatOccupy |= ((1<<(arrival-departure+1))-1) - ((1<<departure)-1); // 分解计算后是下面

        // CAS锁
        seatOccupy.set( seatOccupy.get()|((1<<(arrival)) - (1<<departure)));
        return true;
    }
    // 退票
    public boolean seatRefund(int departure,int arrival) { // 多加一个参数站总数用于正确性判断
       // seatOccupy &= ~((1<<(arrival-departure+1)) - (1<<departure)); // 退票区间取非后与原来车站信息相与得到最后的车站信息
        seatOccupy.set(seatOccupy.get() & (~((1<<(arrival)) - (1<<departure))));
        return true;

    }

    public int getSeatID() {
        return seatId ;
    }
//    public boolean getUsing() {
//        return using ;
//    }





}
