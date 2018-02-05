package org.learningconcurrency

import scala.collection.mutable
import scala.runtime.IntRef
import scala.util.Random
import java.util.concurrent.Executor

package object learningconcurrency {
  def log(str: String) = {
    println(s"${Thread.currentThread().getName} $str")
  }
}

object HelloWorld {

  private var number = 1

  def main(args: Array[String]): Unit = {
    Seq().combinations(1)
    val t = Thread.currentThread

    class MyThread extends Thread {
      override def run(): Unit = println("123")
    }

    val mt = new MyThread
    mt.start()
    mt.join()

    //mt.interrupt()

    println(s"Hello, world! ${t.getName} ${t.isDaemon}")

    var inc: () => Unit = null
    val t2 = thread(if (inc != null) inc())
    inc = () => {number += 1}
    //number = new IntRef(1)
  }

  def thread(body: => Unit) = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }

  private def a() = this.synchronized {}

}

object SynchronizedBadPool {
  def a (b: => Unit) = {}
  type t = () => Unit
  type t2 = _ => Unit
//  type t3 = _
  private val tasks = mutable.Queue[() => Unit]()
}

object Ex2 extends App {

  def fuse[A, B](a: Option[A], b: Option[B]): Option[(A, B)] = for {
    aVal <- a
    bVal <- b
  } yield (aVal, bVal)

  def fuse2[A, B](a: Option[A], b: Option[B]): Option[(A, B)] =
    a.flatMap {
      aVal =>
        b.map {
          bVal =>
            (aVal, bVal)
        }
    }

}

object Ex3 {

  def check2[T](xs: Seq[T])(pred: T => Boolean): Boolean = xs.forall { x =>
    try {
      pred(x)
    } catch {
      case e: Exception =>
        println(e)
        false
    }
  }

  def check22[T](xs: Seq[T])(pred: T => Boolean): Boolean = xs.forall(pred)

  def main(args: Array[String]): Unit = {
    println(check22(List(1, 2, 3))(_ > 1))
  }

}

object Ex4a extends App {

  def permutations(s: String): Seq[String] = {
    if (s.length == 0) Seq("")
    else for {
      i <- 0 until s.length
      q <- permutations(s.take(i) + s.takeRight(s.length - i - 1))
    } yield s(i) + q
  }

  println(permutations("abba"))

}


// for those who consider the same characters the same elements
object Ex4b extends App {

  def permutations(s: String): Seq[String] = {
    if (s.length == 0) Seq("")
    else {
      for {
        i <- s.map(c => s.indexOf(c)).toSet[Int].toSeq
        //a = println(i)
        q <- permutations(s.take(i) + s.takeRight(s.length - i - 1))
      } yield s(i) + q
    }
  }

  println(permutations("abba"))

}


// for those who in love with the standard library :)
object Ex4c extends App {

  def permutations(x: String): Seq[String] = x.permutations.toList

  println(permutations("abba"))

}

object SynchronizedProtectedUid {
  var id = 0L
  def getUniqueId() = this.synchronized {
    val t = id
    id = t + 1
    t
  }
}

object Ex7 extends App {

  import SynchronizedProtectedUid._

  class Account(val name: String, var money: Int) {
    val uid = getUniqueId()
  }

  def send(a1: Account, a2: Account, n: Int) {
    def adjust() {
      a1.money -= n
      a2.money += n
    }

    if (a1.uid < a2.uid) {
      a1.synchronized {
        a2.synchronized {
          adjust()
        }
      }
    } else {
      a2.synchronized {
        a1.synchronized {
          adjust()
        }
      }
    }
  }


  def sendAll(accounts: Set[Account], target: Account): Unit = {

    def adjust() = {
      target.money = accounts.foldLeft(0)((s, a) => {
        val money = a.money
        a.money = 0
        s + money
      }
      )
    }

    def sendAllWithSynchronize(la: List[Account]): Unit = la match {
      case h :: t => h synchronized {
        sendAllWithSynchronize(t)
      }
      case _ => adjust()
    }

    sendAllWithSynchronize((target :: accounts.toList).sortBy(_.uid))
  }

  val accounts = (1 to 100).map((i) => new Account(s"Account: $i",i*10)).toSet
  val target = new Account("Target account", 0)

  sendAll(accounts,target)

  import learningconcurrency.log
  accounts.foreach((a) => log(s"${a.name}, money = ${a.money}"))
  log(s"${target.name} - money = ${target.money}")

}

object Ex11 {
  import HelloWorld.thread
  import learningconcurrency.log
  class ConcurrentBiMap[K, V] {
    var mapStraight: mutable.Map[K,V] = mutable.Map()
    var mapReverse: mutable.Map[V,K] = mutable.Map()

    def reserve[T](t: => T): T = mapStraight.synchronized{
      mapReverse.synchronized{
        t
      }
    }

    def put(k:K, v:V): Option[(K, V)] = reserve{
      val res1 = mapStraight.put(k,v)
      val res2 = mapReverse.put(v,k)
      for(r1 <- res1; r2 <- res2) yield (r2, r1)
    }

    def removeKey(k:K): Option[V]= reserve{
      val res1 = mapStraight.remove(k)
      res1 match {
        case Some(r1) =>
          val res2 = mapReverse.remove(r1)
          res2 match {
            case Some(_) => res1
            case None => None
          }
        case None => None
      }
    }
    def removeValue(v:V): Option[K]= getKey(v) match {
      case None => None
      case Some(k) => removeKey(k) match {
        case None => None
        case Some(v) => Some(k)
      }
    }
    def getValue(k:K): Option[V]= mapStraight.synchronized{
      mapStraight.get(k)
    }
    def getKey(v:V): Option[K]= mapReverse.synchronized{
      mapReverse.get(v)
    }
    def size: Int = mapStraight.synchronized(mapStraight.size)
    def iterator: Iterator[(K, V)]= ???

    def replace(k1: K, v1: V, k2: K, v2: V): Unit = {
      removeKey(k1)
      put(k2, v2)
    }
  }

  val m = new ConcurrentBiMap[Int, Int]()
  for (i <- 0 to 5 ) thread { for(i <- 0 to 1000000) m.put(Random.nextInt(1000000), Random.nextInt(1000000))}
  for (i <- 0 to 5 ) thread { for(i <- 0 to 1000000) m.removeKey(Random.nextInt(1000000))}
  for (i <- 0 to 5 ) thread { for(i <- 0 to 1000000) m.removeValue(Random.nextInt(1000000))}
  thread { for (i <- 0 to 5) {
    log(m.size + "")
    Thread.sleep(1000)
  }
  }

  /**
    * 14) Cache
    */

  def cache[K, V](f: K => V): K => V = {
    var innerCache: mutable.Map[K,V] = mutable.Map()

    (k: K) => {
      var check: Option[V] = None
      innerCache.synchronized{
        check = innerCache.get(k)
      }
      check match {
        case Some(x) => x
        case None =>
          val comp = f(k)
          innerCache.synchronized{
            innerCache.put(k, comp)
          }
          comp
      }
    }
  }

}