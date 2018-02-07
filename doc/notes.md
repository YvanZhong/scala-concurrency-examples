多范式：命令式编程(Imperative programming)、函数式编程(Functional programming)、面向对象编程(Object-oriented programming)

并发层次：计算机硬件层次、操作系统层次、编程语言层次

现代并发范式与传统并发编程技术的差异：高级并发框架描述要实现的目标（而不是描述实现目标的方式）

#1
scala并发框架以Doug Lea的工作成果为基础。Fork/Join架构是Actor、Parallel Collections、Future、Promise的基础。

了解Scala宏。

Rx框架为多种编程语言提供了接口。

ScalaSTM项目。

#2
OS Threads是操作系统提供的programming facility

race condition竞态条件

guarded blocks: 调用wait之前的synchronized叫做保卫锁

为什么需要关闭线程池中的线程：线程即使wait，也需要占用内存，这部分内存是无用的
消耗，不能被其他线程使用。

关闭线程：中断（只有notify不能唤醒线程时，才使用）和正常关闭（设置一个变量terminated）。stop不能用。

@tailrec

volatile:单个线程中，对volatile变量执行写入和读取操作的次序不变；写入操作结果立刻展示给
所有线程，在该线程中，写入volatile之前的所有操作也可以被其他线程看到。

内存模型是并发程序的可预知行为和编译器优化能力权衡（trade-off）的结果。不支持突变的纯函数式编程语言不需要内存模型。

处理器架构的差异会导致不同不同的内存模型。通过happens-before，方便编写跨平台的并发程序。

happens-before是用来确保线程之间的可见性的，不是用来确保语句实际执行次序的。如果执行次序不影响可见性，可以重排序。

被lambda表达式捕获的变量会被提升为对象。

<code>

private var n = 1

var inc: () => Unit = null

val t2 = thread(if (inc != null) inc())

inc = () => {number += 1}

//compile

number = new IntRef(1)

inc = new Function0 {
    
    val $number = number
    def apply() = $number.elem += 1
}

</code>

#3

为什么需要线程池：创建线程的代价比为对象赋值、获取监控器锁、更新集合中某个条目的操作的代价高得多。
如果应用程序需要执行大量的小型并发任务，需要拥有高吞吐量，如果要为每个任务创建一个线程，
这样的花费要比以并发方式完成任务花费的时间还多。所以大多数并发框架将一组线程保持在等待状态。

To allow programmers to encapsulate the decision of how to run concurrently executable
work tasks, JDK comes with an abstraction called Executor.

Executor对象决定由哪个线程在哪个时刻调度run方法。

Executor用于解除并发操作逻辑和这些操作如何被执行的耦合。

ExecutorService: 扩展了Executor，定义了多个便捷的方法，如shutdown。

ExecutionContext：类似Executor的功能，更针对Scala程序。

ExecutionContext companion object contains the default execution context called global,
which internally uses a ForkJoinPool instance:

<code>
object ExecutionContextGlobal extends App {

    val ectx = ExecutionContext.global
    
    ectx.execute(new Runnable {
    
    def run() = log("Running on the execution context.")
    
    })
    
    Thread.sleep(500)
}
</code>

Executor和ExecutionContext很优秀，但是当所有的线程都被占用时，而且有阻塞操作，会遇到问题。
比如8个线程需要其他线程的notify，导致饥饿。

CAS是无锁程序中的基础材料。等价于synchronized，但是更高效，在大多数JVM上不会被阻塞，是处理器
指令层面实现的。

尾递归操作要用@tailrec注解，让编译器帮助检查。

为什么要使用无锁编程：容易引起死锁，当一个拥有锁的线程被操作系统pre-empt时，其他线程可能延迟运行。

无锁操作定义：Given a set of threads executing an operation, an operation is lock-free if at least one
       thread always completes the operation after a finite number of steps, regardless of the
       speed at which different threads progress.
       
线程应该在获取不到锁的时候做其他有意义的操作。

AtomicReference的compareAndSet方法通过比较对象的引用判断是否相等。

ABA问题：两次读取的值相同，但是这段时间内可能发生过变化。

处理ABA问题的指导原则：4条

lazy value和单例对象都是通过双重检查锁定实现的。

惰性值的循环依赖在顺序程序中会导致堆栈溢出，在并发程序中导致死锁。

应该使用专用的私有对象实现同步化。

除非要使传统的java并发实用组件带有Scala语言风格，否则没有必要重新开发已经存在的组件。

并发集合的难点：遍历和修改操作不能同时执行。

scala TrieMap基于Ctire数据结构。调用iterator时，会以原子的方式获取集合中所有元素的快照。

#4

Asynchronous programming refers to
the programming style in which executions occur independently of the main program flow.
Asynchronous programming helps you to eliminate blocking instead of suspending the
thread whenever a resource is not available; a separate computation is scheduled to
proceed once the resource becomes available.

Future & Promise

Thread.sleep()和Timer区别？

#7

ScalaSTM

如果需要同时修改多个原子变量，必须用事务的方式才能保证可线性化。

#8

Actor

Futures
and promises, concurrent data structures, and software transactional memory, are best
suited to shared-memory systems. While the shared-memory assumption ensures that
these facilities are efficient, it also limits them to applications running on a single computer.
In this chapter, we consider a programming model that is equally applicable to a sharedmemory
machine or a distributed system, namely, the actor model.

之前的并发编程抽象都是共享内存系统，actor适用于共享内存系统和分布式系统。

The actor model aims to resolve issues associated with using shared memory, such as data
races or synchronization, by eliminating the need for shared memory altogether.

看actor论文




