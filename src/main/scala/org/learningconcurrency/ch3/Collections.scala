package ch3

import org.learningconcurrency._
import ch3._
/**
 * Ԥ�����̶߳Լ���״̬����Ӱ��,ʹ�������߳���ArrayBuffer�������������
 */
object CollectionsBad extends App {
  import scala.collection._
/**
 * ��׼���ϵ�ʵ�ִ�����û��ʹ���κ�ͬ������,�ɱ伯�ϵĻ������ݽṹ���ܻ�ǳ�����.
 */
  val buffer = mutable.ArrayBuffer[Int]()

  def add(numbers: Seq[Int]) = execute {
    buffer ++= numbers
    log(s"buffer = $buffer")
  }
  /**
   * ������Ӳ����������20����ͬ��ֵԪ�ص�ArrayBuffer����,������ÿ������ʱ�����ͬ�������������׳��쳣
   */
  add(0 until 10)
  add(10 until 20)
  Thread.sleep(500)
}
/**
 * ͬ������
 */

object CollectionsSynchronized extends App {
  import scala.collection._

  val buffer = new mutable.BufferProxy[Int] with mutable.SynchronizedBuffer[Int] {
    val self = mutable.ArrayBuffer[Int]()
  }

  execute {
    buffer ++= (0 until 10)
    log(s"buffer = $buffer")
  }

  execute {
    buffer ++= (10 until 20)
    log(s"buffer = $buffer")
  }
 Thread.sleep(500)
}


object MiscSyncVars extends App {
  import scala.concurrent._
  val sv = new SyncVar[String]

  execute {
    Thread.sleep(500)
    log("sending a message")
    sv.put("This is secret.")
  }

  log(s"get  = ${sv.get}")
  log(s"take = ${sv.take()}")

  execute {
    Thread.sleep(500)
    log("sending another message")
    sv.put("Secrets should not be logged!")
  }

  log(s"take = ${sv.take()}")
  log(s"take = ${sv.take(timeout = 1000)}")
}


object MiscDynamicVars extends App {
  import scala.util.DynamicVariable

  val dynlog = new DynamicVariable[String => Unit](log)
  def secretLog(msg: String) = println(s"(unknown thread): $msg")

  execute {
    dynlog.value("Starting asynchronous execution.")
    dynlog.withValue(secretLog) {
      dynlog.value("Nobody knows who I am.")
    }
    dynlog.value("Ending asynchronous execution.")
  }

  dynlog.value("is calling the log method!")
}

/**
 * main�̴߳�����һ������5500��Ԫ�صĶ���,��ִ����һ�������������Ĳ�������,�������ʾ��ЩԪ��.���ͬʱ,main�̻߳���
 * �ձ���˳��,�Ӹö��������ɾ������Ԫ��.
 * ˳������벢������֮���һ����Ҫ�����ǲ�������ӵ����һ���Ե�����
 */
object CollectionsIterators extends App {
  import java.util.concurrent._
/**
 * BlockingQueue�ӿڻ�Ϊ˳��������Ѿ����ڵķ���,�����ṩ�������̵߳İ汾
 */
  val queue = new LinkedBlockingQueue[String]
  //һ�����о���һ�������ȳ���FIFO�������ݽṹ,
  //�������һ�����Ķ����м���һ������������ͻᱻ�ܾ�,
  //��ʱ�µ� offer �����Ϳ����������ˡ������ǶԵ��� add() �����׳�һ�� unchecked �쳣����ֻ�ǵõ��� offer() ���ص� false
  for (i <- 1 to 5500) queue.offer(i.toString)//����
  execute {
    val it = queue.iterator //ͨ��iterator����������,һ����������ɾͻ���������е�Ԫ��,���������������ǰ�Զ���ִ��������
    //����в���,��ô�ñ��������ͻ���ȫʧЧ
    while (it.hasNext) log(it.next())
  }
  for (i <- 1 to 5500) log(queue.poll())//����
}

/**
 * 
 */
object CollectionsConcurrentMap extends App {
  import java.util.concurrent.ConcurrentHashMap
  import scala.collection._
  import scala.collection.convert.decorateAsScala._
  import scala.annotation.tailrec

  val emails = new ConcurrentHashMap[String, List[String]]().asScala

  execute {
    emails("James Gosling") = List("james@javalove.com")
    log(s"emails = $emails")
  }

  execute {
    emails.putIfAbsent("Alexey Pajitnov", List("alexey@tetris.com"))
    log(s"emails = $emails")
  }

  execute {
    emails.putIfAbsent("Alexey Pajitnov", List("alexey@welltris.com"))
    log(s"emails = $emails")
  }

}


object CollectionsConcurrentMapIncremental extends App {
  import java.util.concurrent.ConcurrentHashMap
  import scala.collection._
  import scala.collection.convert.decorateAsScala._
  import scala.annotation.tailrec

  val emails = new ConcurrentHashMap[String, List[String]]().asScala

  @tailrec def addEmail(name: String, address: String) {
    emails.get(name) match {
      case Some(existing) =>
        //
        if (!emails.replace(name, existing, address :: existing)) addEmail(name, address)
      case None =>
        //putIfAbsent()���������� map �н������,���������Ҫ��ӵ� ConcurrentMapʵ���еļ���ֵΪ������������ͨ�� put() ������
        //����ֻ���� map �����������ʱ�����ܽ������뵽 map �С���� map �Ѿ��������������ô�����������ֵ�ͻᱣ����
        if (emails.putIfAbsent(name, address :: Nil) != None) addEmail(name, address)
    }
  }

  execute {
    addEmail("Yukihiro Matsumoto", "ym@ruby.com")
    log(s"emails = $emails")
  }

  execute {
    addEmail("Yukihiro Matsumoto", "ym@ruby.io")
    log(s"emails = $emails")
  }

}

/**
 * ����һ�������������ֶ�Ӧ�����Ĳ���ӳ��,����:Janice����0��Ӧ
 * �������������һ����,���Ǿͻῴ����ʼʱӳ���лẬ��3������,���Ҹ��ݵ�һ������������ֵ�����
 * John��Ӧ�����ֻ���0��n֮���ֵ,�ý��������John1,John2,John3,�����John 8��John 5֮���������������
 */
object CollectionsConcurrentMapBulk extends App {
  import scala.collection._
  import scala.collection.convert.decorateAsScala._
  import java.util.concurrent.ConcurrentHashMap

  val names = new ConcurrentHashMap[String, Int]().asScala
  names("Johnny") = 0
  names("Jane") = 0
  names("Jack") = 0

  execute {
    for (n <- 0 until 10) names(s"John $n") = n
  }

  execute {
    for (n <- names) log(s"name: $n")
  }
  /**
   * ForkJoinPool-1-worker-3: name: (Jane,0)
   * ForkJoinPool-1-worker-3: name: (Jack,0)
   * ForkJoinPool-1-worker-3: name: (John 8,8)
   * ForkJoinPool-1-worker-3: name: (John 0,0)
   * ForkJoinPool-1-worker-3: name: (John 5,5)
   * ForkJoinPool-1-worker-3: name: (Johnny,0)
   * ForkJoinPool-1-worker-3: name: (John 6,6)
   * ForkJoinPool-1-worker-3: name: (John 4,4)
   */
Thread.sleep(500)
}
/**
 * TrieMap��ConcurrentHashMap����,���Ӧ�ó��� ��Ҫʹ��һ���Ե�����ʹ��TrieMap����
 * ���Ӧ�ó������� ʹ��һ���Ե��������Ҽ���ִ���޸Ĳ���ӳ��Ĳ���,��Ӧ��ʹ��ConcurrentHashMap����
 * ��Ϊ������ִ�в�ѯ�������Ի�ýϿ���ٶ�
 */

object CollectionsTrieMapBulk extends App {
  import scala.collection._
/**
 * TrieMap��Զ����������������,TrieMap������������ͬһ������,���������Щ����ǰ����ĸ˳������ǽ�������
 * TrieMapȷ��ִ��ɾ�������ļ��������߳�,�޷�����ִ�ж�ȡ�ļ��������߳�
 */
  val names = new concurrent.TrieMap[String, Int]
  names("Janice") = 0
  names("Jackie") = 0
  names("Jill") = 0

  execute {
    for (n <- 10 until 100) names(s"John $n") = n
  }

  execute {
    log("snapshot time!")
    for (n <- names.map(_._1).toSeq.sorted) log(s"name: $n")
  }
Thread.sleep(500)
}





