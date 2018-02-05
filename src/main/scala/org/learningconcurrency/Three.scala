package org.learningconcurrency

import scala.concurrent._
import java.util.concurrent.ForkJoinPool

object Three {

  object ExecutorsCreate extends App {
    val executor = new ForkJoinPool
//    new forkjoin.ForkJoinPool(2)
    executor.execute(new Runnable {
      def run() = println("This task is run asynchronously.")
    })
    Thread.sleep(500)
  }

}
