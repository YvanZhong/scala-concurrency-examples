package org.learningconcurrency

import java.util.Timer

import scala.concurrent._
import java.util.concurrent.ForkJoinPool

object Three {

  object ExecutorsCreate extends App {
    val executor = new ForkJoinPool
//    new forkjoin.ForkJoinPool(2)
    executor.execute(new Runnable {
      def run() = println("This task is run asynchronously.")
    })
    List()
    new Thread()
    val a = executor.##
    Thread.sleep(500)
  }

}

object FuturesNonFatal extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val a = throw new InterruptedException
  val a1 = {}
  val f = Future { throw new InterruptedException }
  val g = Future { throw new IllegalArgumentException }
  val p = Promise[String]
  val b = f.flatten
  f.failed foreach { t => println(s"error - $t") }
  g.failed foreach { t => println(s"error - $t") }

  val timer = new Timer(true)

  Thread.sleep(1000)
}
