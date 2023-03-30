# 实验报告

# 用于列车售票可线性化并发数据结构

### 一、实验要求【详见附录】

### 二、实验概述

> ​		本实验是并发数据结构与多核编程课程的实验作业，并以学习的并发编程相关知识为基础，完成了该实验作业项目。具体计划及完成时间为：
>
> - [x] 实验具体要求的研读  2021.11.26  
>
> - [x] 实验相关并发知识的深入理解  11.27~11.28
>
> - [x] 理解项目并进行实验环境的搭建   11.29~11.30
>
> - [x] 非并发的单线程串行项目的实现   12.1
>
> - [x] 基于粗粒度锁的并发数据结构的实现   12.2
>
> - [x] 基于细粒度锁的并发数据结构的实现   12.3
>
> - [x] 基于乐观锁的并发数据结构的实现及实验报告的编写   12.4
>
> ​	    最终，实验项目实现了基于乐观锁的并发数据结构，性能较细粒度锁提升不大，可能是受到笔记本性能的限制，但是吞吐率上下波动更小，并且在测试过程中，未出现并发线程增加而吞吐率下降的现象。

### 三、实现流程

- TicketingDS

  基于TicketingSystem接口，对其中的方法进行了重写，具体实现了以下方法：

  - inquiry方法：进行列车具体到【车次】【出发站】【终点站】的余票查询
  - buyTicket方法：进行列车具体【车次】【出发站】【终点站】的购买以及车票对象的返回
  - refundTicket方法：进行列车具体【车次】【出发站】【终点站】车票的退回
  - check方法：完成对于输入【车次】【出发站】【终点站】正确性验证


<div STYLE="page-break-after: always;"></div>

- Train

  TicketingDS类进行了Train类的实例化，而Train类的作用是列车座位和售票系统之间信息的交换，具体方法：

  - trainInQuiry方法：对每个车次座位在区间空余信息的查询
  - trainBuy方法：判断座位是否空余，购买成功返回SeatID
  - trainRefund方法：判断退票信息正确性后进行退票操作

- Seat

  Seat类中的座位是细粒度锁实现的基本单位，具体对于抽象的列车座位的区间占用信息进行了修改，其中核心是使用二进制进行座位占用区间的标识，相比布尔数组极大的提高了性能，具体方法：

  - seatInquiry方法：使用二进制的与运算进行原座位占用区间与查询区间的信息对比
  - seatBuy方法：使用二进制的或运算进行座位占用区间的增加
  - seatRefund方法：使用二进制的与和非运算进行占用区间的置位

- Test

  Test类实现了对于该并发数据结构的多线程性能测试，具体依赖以下方法：

  - Runnable方法：线程实例化的匿名内部类，对于线程调用TicketingDS方法的随机测试
  - start和join方法：进行异步多线程的执行
  - System.currentTimeMillis方法：获取毫秒级的当前时间，效率比newTime方法高
  - 随机测试下的平均买票时间、平均退票时间、平均查询时间以及吞吐率的计算

### 四、实验分析

1. 正确性分析

   - 使用AtomicLong的数据结构，获取车票ID使用getAndIncrement方法，保证车票ID的原子性和唯一性
   - 买票、退票和查询余票⽅法均需要通过check方法的验证，避免无效输入带来系统的错误
   - 每个区段有余票时,系统一定可以满足票的购买，使用CAS进行余票的购买，必定有一个线程可以获得票
   - 每个线程进行车票购买的时候需要进行余票的验证，如果没有余票将不能购买
   - 如果查询没有余票那么将不能进行购买，并且保证了在进行购买时的并发性，不会出现错误的返回

2. 流程分析

   项目按照老师上课讲解的递进式开发，从最简单的锁一步一步进行高性能并发程序的编写：

   - 非并发数据结构：按照要求先进行基础售票系统的开发，并根据老师的要求进行符合要求的正确性检验
   - 粗粒度锁：对的每个车次使用可重入锁进行加锁，但是性能表现很差
   - 细粒度锁：对列车的座位进行加锁，具有良好的并发性，但由于对于座位的查询操作需要大量进行加锁和解锁操作，导致仍然具有性能的瓶颈
   - 乐观锁：仍然是对列车的座位进行加锁，但是使用的是基于版本控制的CAS进行数据的并发修改正确性的保证，具有良好的性能表现

3. 并发数据结构锁的性质

   > - [x] deadlock-free
   >
   >   由于使用的是CAS，这是一种乐观锁算法，实质是基于版本的校验，并不对线程进行加锁，所以也不会导致死锁的现象。
   >
   > - [x] starvation-free
   >
   >   线程执行CAS进行列车座位区间的修改，并不会导致出现无限等待，即每个线程一定会在有限步内完成，所以是无饥饿的。
   >
   > - [x] lock-free 
   >
   >   每个线程进行操作无论成功与否一定会在有限步内完成，进程之间的竞争也一定会有胜出者完成事务的处理，所以满足无锁
   >
   > - [x] wait-free
   >
   >   系统中的所有线程，都会在有限时间内结束，无论如何也不可能出现饿死的情况，乐观锁保证了系统中的所有线程都能处于工作状态，没有线程会被饿死，只要时间够，所有线程都能结束。

### 五、实验总结

1. 本机Test测试：

> 环境：R7-4800H （8核16线程）CPU，16G DDR4内存，512G PCI协议固态硬盘

![image-20211205202341183](https://gitee.com/water_stop/blog-image/raw/master/img/image-20211205202341183.png)

2. 教学服务器Trace测试

 ![image-20211205203031301](https://gitee.com/water_stop/blog-image/raw/master/img/image-20211205203031301.png)

3. 教学服务器Verify.sh测试

 ![image-20211205203221357](https://gitee.com/water_stop/blog-image/raw/master/img/image-20211205203221357.png)

4. 教学服务器Verify.sh测试100次，每个方法调用100000次，均Finished

 <img src="https://gitee.com/water_stop/blog-image/raw/master/img/image-20211205205324997.png" alt="image-20211205205324997" style="zoom: 67%;" />

<div STYLE="page-break-after: always;"></div>

### 附录：

#### 数据结构说明

给定`Ticket`类：

```java
class Ticket{
    long tid;
    String passenger;
    int route;
    int coach;
    int seat;
    int departure;
    int arrival;
}
```

其中，`tid`是车票编号，`passenger`是乘客名字，`route`是列车车次，`coach`是车厢号，`seat`是座位号，`departure`是出发站编号，`arrival`是到达站编号。

给定`TicketingSystem`接口：

```java
public interface TicketingSystem {
    Ticket buyTicket(String passenger, int route, int departure, int arrival);
    int inquiry(int route, int departure, int arrival);
    boolean refundTicket(Ticket ticket);
}
```

其中：

- `buyTicket`是购票方法，即乘客`passenger`购买`route`车次从`departure`站到`arrival`站的车票1张。若购票成功，返回有效的`Ticket`对象；若失败（即无余票），返回无效的`Ticket`对象（即`return null`）。
- `refundTicket`是退票方法，对有效的`Ticket`对象返回`true`，对错误或无效的`Ticket`对象返回`false`。
- `inquriy`是查询余票方法，即查询`route`车次从`departure`站到`arrival`站的余票数。

#### 完成`TicketingDS`类

完成一个用于列车售票的可线性化并发数据结构：`TicketingDS`类：

1. 实现`TicketingSystem`接口，
2. 提供`TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);`构造函数。

其中：

- `routenum`是车次总数（缺省为5个），
- `coachnum`是列车的车厢数目（缺省为8个），
- `seatnum`是每节车厢的座位数（缺省为100个），
- `stationnum`是每个车次经停站的数量（缺省为10个，含始发站和终点站），
- `threadnum`是并发购票的线程数（缺省为16个）。

为简单起见，假设每个车次的`coachnum`、`seatnum`和`stationnum`都相同。
车票涉及的各项参数均从1开始计数，例如车厢从1到8号，车站从1到10编号等。

#### 完成多线程测试程序

需编写多线程测试程序，在`main`方法中用下述语句创建`TicketingDS`类的一个实例。

```java
final TicketingDS tds = new TicketingDS(routenum, coachnum, seatnum, stationnum, threadnum);
```

系统中同时存在`threadnum`个线程（缺省为16个），每个线程是一个票务代理，需要：

1. 按照60%查询余票，30%购票和10%退票的比率反复调用`TicketingDS`类的三种方法若干次
2. 按照线程数为4，8，16，32，64个的情况分别调用。

需要最后给出：

1. 给出每种方法调用的平均执行时间
2. 同时计算系统的总吞吐率（单位时间内完成的方法调用总数）

#### 正确性要求

需要保证以下正确性：

- 每张车票都有一个唯一的编号`tid`，不能重复。
- 每一个`tid`的车票只能出售一次。退票后，原车票的`tid`作废。
- 每个区段有余票时，系统必须满足该区段的购票请求。
- 车票不能超卖，系统不能卖无座车票。
- 买票、退票和查询余票方法均需满足可线性化要求。

#### 文件清单

所有Java程序放在`ticketingsystem`目录中，`trace.sh`文件放在`ticketingsystem`目录的上层目录中。
如果程序有多重目录，那么将主Java程序放在`ticketingsystem`目录中。

文件清单如下：

1. `TicketingSystem.java`是规范文件，不能更改。
2. `Trace.java`是trace生成程序，用于正确性验证，不能更改。
3. `trace.sh`是trace生成脚本，用于正确性验证，不能更改。
4. `TicketingDS.java`是并发数据结构的实现。
5. `Test.java`实现多线程性能测试。

