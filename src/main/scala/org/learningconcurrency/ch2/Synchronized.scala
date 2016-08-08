
package ch2


import org.learningconcurrency._
import ch2._



object SynchronizedProtectedUid extends App {

  var uidCount = 0L

  def getUniqueId() = this.synchronized {
    val freshUid = uidCount + 1
    uidCount = freshUid
    freshUid
  }

  def printUniqueIds(n: Int): Unit = {
    val uids = for (i <- 0 until n) yield getUniqueId()
    println(s"Generated uids: $uids")
  }

  val t = thread{
    printUniqueIds(5)
  }
  printUniqueIds(5)
  t.join()

}


// we should skip this one
object SynchronizedSharedStateAccess extends App {
  for (i <- 0 until 10000) {
    var t1started = false
    var t2started = false
    var t1index = 0
    var t2index = 0
  //���������߳�(t1,t2)����һ�Բ����ͱ���(a��b)��һ�Ա���(x,y)
  //�߳�t1������a����Ϊtrue,Ȼ���ȡ����b��ֵ,�������b��ֵΪtrue
  //�߳�t1�ͻὫ0�������y,����ͻὫ1�������y
    val t1 = thread {
      Thread.sleep(1)    
      //�����ֶ���̶߳�����ʶ�ȡ��д��ĳ��״̬ʱ,��Ӧ�öԶ���ʹ��synchronized���
      //���������������̶���ȷ�������߳���ʱִ�ж�����synchronized���,����ȷ��һ���̶߳��ڴ�
      //ִ�е�����д�����,����ͬһ������ִ��synchronized�������к����̶߳��ǿɼ���
      this.synchronized { t1started = true }
      val t2s = this.synchronized { t2started }
      t2index = if (t2started) 0 else 1
    }
    val t2 = thread {
      Thread.sleep(1)
      this.synchronized { t2started = true }
      val t1s = this.synchronized { t1started }
      t1index = if (t1s) 0 else 1
    }
  
    t1.join()
    t2.join()
    assert(!(t1index == 1 && t2index == 1), s"t1 = $t1index, t2 = $t2index")
  }
}

/**
 * ͬ��
 */
object SynchronizedNesting extends App {
  import scala.collection._
  private val transfers = mutable.ArrayBuffer[String]()
  /**
   * ArrayBuffer����ʵ�ֵ���һ��ר���ɵ��߳�ʹ�õļ���,�������Ӧ�÷�ֹ����ִ�в���д�����
   */
  def logTransfer(name: String, n: Int): Unit = transfers.synchronized {
    transfers += s"transfer to account '$name' = $n"
  }
  /**
   * �˺�Account�������������ߵ���Ϣ�Լ�����ӵ�е��ʽ�����
   */
  class Account(val name: String, var money: Int)
  /**
   * ���˺��г�ֵ,��ϵͳ��ʹ��add������ȡָ��Account����ļ����
   * ���޸ĸö����е�money�ֶ�
   */
  def add(account: Account, n: Int) = account.synchronized {//���transfers�����
    account.money += n//���ת�˽���10�����ҵ�λ,��Ҫ�ò�����¼����
    if (n > 10) logTransfer(account.name, n)
  }
  //��Ӧ�ó��򴴽������������˺ź�����ִ��ת�˵�3���߳�,һ�������̶߳���������ǵ�ת�˲���
  //main�߳̾ͻ���������Ѽ�¼��ת����Ϣ
  val jane = new Account("Jane", 100)
  val john = new Account("John", 200)
  val t1 = thread { add(jane, 5) }
  val t2 = thread { add(john, 50) }
  val t3 = thread { add(jane, 70) }
  /**
   * �������ʹ��synchronized���,�ܹ���ֹ�߳�t1��t3�Բ�����ʽ�޸�Janer ���˺���Ϣ,�ƻ�����˺�
   * �߳�t2��t3�������transfers�������ʹ�ü�¼
   */
  t1.join(); 
  t2.join(); 
  t3.join()
  log(s"--- transfers ---\n$transfers")
}

/**
 * ����:��ָ��������������ʵ���ڼ���ִ���Լ��Ĳ���ǰ,�ȴ��Է�����ɲ��������.
 * �ȴ���ԭ����ÿ��������ʵ�嶼��ռ��,����������ʵ������ú���ܼ���ִ��������������Դ
 * 
 */
object SynchronizedDeadlock extends App {
  import SynchronizedNesting.Account
  //
  def send(a: Account, b: Account, n: Int) = a.synchronized {
    b.synchronized {
      a.money -= n
      b.money += n
    }
  }
  val a = new Account("Jill", 1000)
  val b = new Account("Jack", 2000)
  val t1 = thread { for (i <- 0 until 100) send(a, b, 1) }
  val t2 = thread { for (i <- 0 until 100) send(b, a, 1) }
  t1.join()
  t2.join()
  log(s"a = ${a.money}, b = ${b.money}")
}

/**
 * ��ֹ�����ķ���
 */
object SynchronizedNoDeadlock extends App {
  import SynchronizedProtectedUid._
  class Account(val name: String, var money: Int) {
    val uid = getUniqueId()//��ȡ�˺ŵ�˳��
  }
  def send(a1: Account, a2: Account, n: Int) {
    def adjust() {
      a1.money -= n
      a2.money += n
    }
    if (a1.uid < a2.uid)
      //
      a1.synchronized { a2.synchronized { adjust() } }
    else
      a2.synchronized { a1.synchronized { adjust() } }
  }
  val a = new Account("Jill", 1000)
  val b = new Account("Jack", 2000)
  val t1 = thread { for (i <- 0 until 100) send(a, b, 1) }
  val t2 = thread { for (i <- 0 until 100) send(b, a, 1) }
  t1.join()
  t2.join()
  log(s"a = ${a.money}, b = ${b.money}")
}


object SynchronizedDuplicates extends App {
  import scala.collection._
  val duplicates = mutable.Set[Int]()
  val numbers = mutable.ArrayBuffer[Int]()
  def isDuplicate(n: Int): Unit = duplicates.synchronized {
    duplicates.contains(n)
  }
  def addDuplicate(n: Int): Unit = duplicates.synchronized {
    duplicates += n
  }
  def addNumber(n: Int): Unit = numbers.synchronized {
    numbers += n
    if (numbers.filter(_ == n).size > 1) addDuplicate(n)
  }
  val threads = for (i <- 1 to 2) yield thread {
    for (n <- 0 until i * 10) addNumber(n)
  }
  for (t <- threads) t.join()
  println(duplicates.mkString("\n"))
}

/**
 * �̳߳�:ͬһ���߳�Ӧ�����������󷴸�ʹ��,��Щ���ظ����߳���ͨ����Ϊ�̳߳�
 */
object SynchronizedBadPool extends App {
  import scala.collection._
  /**
 * ʹ��Queue�洢�����ȵĴ����,ʹ�ú���() => Unit������Щ�����
 */
  private val tasks = mutable.Queue[() => Unit]()
  /**
 * �߳�Worker��������poll����,�ڱ���task�洢�Ķ�����ʵ��ͬ����,
 * ���ö������Ķ����Ƿ�Ϊ��,poll����չʾ��synchronized�����Է���һ��ֵ
 */
  val worker = new Thread {
    def poll(): Option[() => Unit] = tasks.synchronized {
      //����һ����ѡ��Someֵ,�������䷵��һ��None,dequeue��������
      if (tasks.nonEmpty) Some(tasks.dequeue()) else None
    }
   
    override def run() = while (true) poll() match {
      case Some(task) => task()//������������
      case None =>
    }
  }
  //�����ػ��߳�,�����ػ��̵߳�ԭ��,synchronized����������������,
  //�÷��������ָ���Ĵ����,�Ա�����ִ��worker�߳�
  worker.setDaemon(true)
  //����work�߳�
  worker.start()
  //�����ػ��̵߳�ԭ��,synchronized����������������,
  //�÷��������ָ���Ĵ����,�Ա�����ִ��worker�߳�
  def asynchronous(body: =>Unit) = tasks.synchronized {
    tasks.enqueue(() => body)//�������
  }

  asynchronous { log("Hello") }
  asynchronous { log(" world!")}
  Thread.sleep(100)
}

/**
 * main�߳�׼����Some��Ϣ,ʹ֮��ʾ����Ϣ
 */
object SynchronizedGuardedBlocks extends App {
  val lock = new AnyRef //locak�����еļ����,
  var message: Option[String] = None
  //greeter�߳�ͨ����ȡlock����ļ������ʼ�����й���,�����main�߳�Ϊ��׼������Ϣ�Ƿ�ΪNone����
  //�������ϢΪNone����,��ôgreeter�߳̾Ͳ�����ʾ������Ϣ,���һ����lock�����е�wait����
  val greeter = thread {
    lock.synchronized {
      //���߳�T������ĳ�������е�wait������,�߳�T�ͻ��ͷŸö���ļ�������л����ȴ�״̬,ֱ�������̵߳�����
      //�ö����е�notify������,�߳�T�Ż��л�����������״̬.
      while (message == None) lock.wait()//���߳��л�Ϊ�ȴ�״̬
      log(message.get)
    }
  }
  lock.synchronized {
    message = Some("Hello!")
    lock.notify()//���߳��л�Ϊ��������״̬
  }
  greeter.join()
}

/**
 * �̳߳�:ͬһ���߳�Ӧ�����������󷴸�ʹ��,��Щ���ظ����߳���ͨ����Ϊ�̳߳�
 */
object SynchronizedPool extends App {
  import scala.collection._
/**
 * ʹ��Queue�洢�����ȵĴ����,ʹ�ú���() => Unit������Щ�����
 */
  private val tasks = mutable.Queue[() => Unit]()
/**
 * �߳�Worker��������poll����,�ڱ���task�洢�Ķ�����ʵ��ͬ����,
 * ���ö������Ķ����Ƿ�Ϊ��,poll����չʾ��synchronized�����Է���һ��ֵ
 */
  object Worker extends Thread {//��Worker����һ����������,�ڸó�����,��poll�̵߳���tasks�����е�wait����
    //Ȼ����߳̽���ȴ�״̬,ֱ��main�߳���tasks���������һ������鲢����synchronized�����е�notify����Ϊֹ
    
    setDaemon(true)
    def poll() = tasks.synchronized {
      while (tasks.isEmpty) tasks.wait()
      tasks.dequeue()
    }
    override def run() = while (true) {
      val task = poll()
      task()
    }
  }

  Worker.start()

  def asynchronous(body: =>Unit) = tasks.synchronized {
    tasks.enqueue(() => body)
    tasks.notify()
  }

  asynchronous { log("Hello ") }
  asynchronous { log("World!") }
}


object SynchronizedGracefulShutdown extends App {
  import scala.collection._
  import scala.annotation.tailrec

  private val tasks = mutable.Queue[() => Unit]()

  object Worker extends Thread {
    var terminated = false
    def poll(): Option[() => Unit] = tasks.synchronized {
      while (tasks.isEmpty && !terminated) tasks.wait()
      if (!terminated) Some(tasks.dequeue()) else None
    }
    @tailrec override def run() = poll() match {
      case Some(task) => task(); run()
      case None =>
    }
    def shutdown() = tasks.synchronized {
      terminated = true
      tasks.notify()
    }
  }

  Worker.start()

  def asynchronous(body: =>Unit) = tasks.synchronized {
    tasks.enqueue(() => body)
    tasks.notify()
  }

  asynchronous { log("Hello ") }
  asynchronous { log("World!") }

  Thread.sleep(1000)
  //�߳������ر�,
  Worker.shutdown()
}



