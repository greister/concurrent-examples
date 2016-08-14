
package ch3

import org.learningconcurrency._
import ch3._



/**
 * ����ֵ:��ָ�ڵ�һ�α���ȡʱ,����ֵ�������Ҳ���ʽ������г�ʼ����ֵ,
 * ������ֵͨ��ͬ,��ֵͨ���ڱ�����ʱ����ʼ��,�������ֵ�ڳ����д�û�б���ȡ,��ô��Զ�����ʼ��
 * ���Ҳ���Ḷ����ʼ������Ĵ���.
 */

object LazyValsCreate extends App {
  import scala.concurrent._
  /**
   * ����ֵֻӦ���̷߳�����ʱ���г�ʼ��,�������ʼ����������ֻ��ִ��һ��,
   * �����̷߳���������obj��nonֵ
   */
  lazy val obj = new AnyRef
  lazy val nondeterministic = s"made by ${Thread.currentThread.getName}"

  execute {
    log(s"Execution context thread sees object = $obj")
    log(s"Execution context thread sees nondeterministic = $nondeterministic")
  }

  log(s"Main thread sees object = $obj")
  log(s"Main thread sees nondeterministic = $nondeterministic")
}

/**
 * ����ֵ
 */
object LazyValsObject extends App {
  object Lazy {
    log("Running Lazy constructor.")
  }

  log("Main thread is about to reference Lazy.")
  Lazy //�ڵ����д����е�һ�α�����,���ʼ�������Ż�����,���Ҳ����������ö��������г�ʼ������
  log("Main thread completed.")
}


object LazyValsUnderTheHood extends App {
  @volatile private var _bitmap = false
  private var _obj: AnyRef = _
  def obj = if (_bitmap) _obj else this.synchronized {
    if (!_bitmap) {
      _obj = new AnyRef
      _bitmap = true
    }
    _obj
  }

  log(s"$obj"); log(s"$obj")
}


object LazyValsInspectMainThread extends App {
  val mainThread = Thread.currentThread

  lazy val x = {
    log(s"running Lazy ctor")
    Thread.sleep(1000)
    log(s"main thread state - ${mainThread.getState}")
  }

  execute { x }

  log("started asynchronous thread")
  Thread.sleep(200)
  log("log about to access x")
  x
}


object LazyValsDeadlock extends App {
  object A {
    lazy val x: Int = B.y
  }
  object B {
    lazy val y: Int = A.x
  }

  execute { B.y }

  A.x
}


object LazyValsAndSynchronized extends App {
  lazy val terminatedThread = {
    val t = ch2.thread {
      LazyValsAndSynchronized.synchronized {}
    }
    t.join()
    t.getState
  }

  terminatedThread
}


object LazyValsAndBlocking extends App {
  lazy val x: Int = {
    val t = ch2.thread {
      println(s"Initializing $x.")
    }
    t.join()
    1
  }
  x
}


object LazyValsAndMonitors extends App {
  lazy val x = 1
  this.synchronized {
    val t = ch2.thread { x }
    t.join()
  }
}




