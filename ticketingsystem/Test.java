package ticketingsystem;
/*
本次大作业的性能测试参数是：
		route=10, coach=10, station=20, seat=100,
		inquiry70%, buyTicket20%, refundTicket10% ,
		64线程，每线程10万次操作（操作数待定）。
*/

import java.util.ArrayList;
import java.util.Random;
// 同步：调用方法后一直等待响应，直到完成再去处理别的事情
// 异步：调用方法后去做别的事情，等待方法响应后，再回来继续做
// AtomicInteger
import java.util.concurrent.atomic.AtomicInteger;
// final修饰变量
// 1. 如果是基本数据类型的变量，则其数值一旦在初始化之后便不能更改；
// 2. 如果是引用类型的变量，则在对其初始化之后便不能再让其指向另一个对象
// static修饰变量
// 1. 属于整个类所有，而不是某个对象所有，即被类的所有对象所共享。
// 2. 静态成员可以使用类名直接访问，也可以使用对象名进行访问
public class Test {
	private final static int TEST = 100000;// 测试次数
	private final static int ROUTE = 10; // 列车车次10
	private final static int COACH = 10; // 列车车厢10
	private final static int SEAT = 100; // 每节车厢座位数目100
	private final static int STATION = 10;// 车站数目10

	private final static int buy = 30; // 购买次数
	private final static int query = 100; // 总次数
	private final static int thread = 64; // 线程数目
	private final static int refund = 10; // 退票次数

	private final static long[] buyTicketTime = new long[thread];// 单线程买票时间数组
	private final static long[] refundTime = new long[thread]; 	 // 单线程退票时间数组
	private final static long[] inquiryTime = new long[thread];	 // 单线程查询时间数组

	private final static long[] buyTotalTime = new long[thread];	// 单线程买票时间数组
	private final static long[] refundTotalTime = new long[thread];	// 单线程退票时间数组
	private final static long[] inquiryTotalTime = new long[thread];// 单线程查询时间数组
	// 内存屏障，也称内存栅栏，内存栅障，屏障指令等， 是一类同步屏障指令，
	// 是 CPU 或编译器在对内存随机访问的操作中的一个同步点,使得此点之前的所有读写操作都执行后才可以开始执行此点之后的操作。

	private final static AtomicInteger threadId = new AtomicInteger(0); // 原子类型防止并发访问出现数据错误
	// CAS(CompareAndSet,比较并更新)
	// 作用：如果当前值等于 expect 的值，那么就以原子性的方式将当前值设置为 update 给定值
	// 返回值：方法会返回一个 boolean 类型，如果是 true 就表示比较并更新成功，否则表示失败
	// 特点：一种无锁并发机制，也称为 Lock Free

	static String passengerName() {
		Random rand = new Random(); // 定义一个随机对象
		long uid = rand.nextInt(TEST); // 生成[0,TEST_NUM)内随机整数序列
		return "passenger" + uid;
	}

	public static void main(String[] args) throws InterruptedException { // throws关键字对外声明该方法有可能发生的异常
		// to do
//		final int[] threadNums = { 4, 8, 16, 32, 64 }; // 初始化后数组内的值不能改变
		final int[] threadNums = {4, 8, 16, 32, 64}; // 初始化后数组内的值不能改变
		int p;
		for (p = 0; p < threadNums.length; ++p) { // 遍历执行数组的每一个线程数
			final TicketingDS tds = new TicketingDS(ROUTE, COACH, SEAT, STATION, threadNums[p]);
			Thread[] threads = new Thread[threadNums[p]]; // 创建线程数组
			for (int i = 0; i < threadNums[p]; i++) {
				// 实例化线程，但是没有执行
				threads[i] = new Thread(new Runnable() { // 匿名内部类：在创建实例的同时给出类的定义，所有这些都在一个表达式中完成
					public void run() {
						Random rand = new Random();		// 随机对象
						Ticket ticket = new Ticket();	// 票对象
						int id = threadId.getAndIncrement(); // getAndIncrement()先返回后加一
						ArrayList<Ticket> soldTicket = new ArrayList<>(); // 泛型，用于创建某个类类型的对象数组
						// 10万次对票的操作
						for (int i = 0; i < TEST; i++) {
							int tic = rand.nextInt(query); // 1到100的随机数
							// 退票测试0-10
							if (0 <= tic && tic < refund && soldTicket.size() > 0) {
								int select = rand.nextInt(soldTicket.size());	// .size()方法返回ArrayList中存储的对象的个数
								if ((ticket = soldTicket.remove(select)) != null) { // 票类型的对象数组是一个以上
									long s = System.nanoTime(); // 返回系统的时间，以纳秒ns为单位
									tds.refundTicket(ticket);	// 测试退票访问火车车次的时间
									long e = System.nanoTime();
									refundTime[id] += e - s; 	// 所有退票时间总和
									refundTotalTime[id] += 1;;		// 所有退票次数总和
								} else {
									System.out.println("Refund Error!!!");
								}
								// 买票测试：10<=随机数<40
							} else if (refund <= tic && tic < buy) {
								String passenger = passengerName();
								int route = rand.nextInt(ROUTE) + 1; // 列车的车次的随机值
								int departure = rand.nextInt(STATION - 1) + 1; // 随机的出发站
								int arrival = departure + rand.nextInt(STATION - departure) + 1; // 随机的到达站
								// 火车买票时间测试
								long s = System.nanoTime();
								ticket = tds.buyTicket(passenger, route, departure, arrival);
								long e = System.nanoTime();

								buyTicketTime[id] += e - s;
								buyTotalTime[id] += 1;
								if (ticket != null) {	// 车票买入成功
									soldTicket.add(ticket);
								}
								// 查询测试：随机值在40-100的
							} else if (buy <= tic && tic < query) {
								int route = rand.nextInt(ROUTE) + 1;
								int departure = rand.nextInt(STATION - 1) + 1;
								int arrival = departure + rand.nextInt(STATION - departure) + 1;
								// 时间测试
								long s = System.nanoTime();
								tds.inquiry(route, departure, arrival);
								long e = System.nanoTime();
								// 时间计算
								inquiryTime[id] += e - s;
								inquiryTotalTime[id] += 1;
							}
						}
					}
				}); // 线程结束实例化
			}
			// 异步多线程的执行和计时
			long start = System.currentTimeMillis();
			// 第一个for指线程异步启动自己的run方法，第二个for所有线程都阻塞主线程，等每个线程都完成后才运行主线程，而每个线程之间是并发
			for (int i = 0; i < threadNums[p]; ++i)
				threads[i].start(); // 真正启动线程，jvm异步地调用每个线程的run方法
			for (int i = 0; i < threadNums[p]; i++) {
				threads[i].join(); //主线程调用每个线程wait，直到每个线程执行完再执行
			}
			long end = System.currentTimeMillis(); // 获取毫秒级的当前时间，效率比newTime方法高

			long buyTotal = Add(buyTicketTime, threadNums[p]); // 所有线程买票花费时间的总和
			long refundTotal = Add(refundTime, threadNums[p]); // 所有线程退票花费时间的总和
			long inquiryTotal = Add(inquiryTime, threadNums[p]);// 所有线程查询花费时间的总和
			// 次数总和
			double bTotal = (double) Add(buyTotalTime, threadNums[p]);
			double rTotal = (double) Add(refundTotalTime, threadNums[p]);
			double iTotal = (double) Add(inquiryTotalTime, threadNums[p]);
			// 平均时间 = 时间 / 次数
			long buyAvgTime = (long) (buyTotal / bTotal);
			long refundAvgTime = (long) (refundTotal / rTotal);
			long inquiryAvgTime = (long) (inquiryTotal / iTotal);
			// 吞吐率throughout = 吞吐量 / 测试时间，值越大，系统的负载能力越强
			long time = end - start; // 从ms转换为s
			long t = (long) (threadNums[p] * TEST / (end - start));
			// 性能测试数据的输出
			System.out.println(String.format(
					"threadNum: %d\tbuyAvgTime: %d \trefundAvgTime: %d \tinquiryAvgTime: %d \tthroughOut: %d",
					threadNums[p], buyAvgTime, refundAvgTime, inquiryAvgTime, t));
			clear(); // 函数调用
		}
	}
	// 数组内值进行累加的函数
	private static long Add(long[] array, int threadNums) {
		long res = 0;
		for (int i = 0; i < threadNums; ++i)
			res += array[i];
		return res;
	}
	// 将线程和相关测试信息变量置为0
	private static void clear() {
		threadId.set(0);
		long[][] arrays = { buyTicketTime, refundTime, inquiryTime, buyTotalTime, refundTotalTime, inquiryTotalTime };
		for (int i = 0; i < arrays.length; ++i)
			for (int j = 0; j < arrays[i].length; ++j)
				arrays[i][j] = 0;
	}

}
