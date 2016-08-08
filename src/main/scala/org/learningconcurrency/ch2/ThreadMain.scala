
package ch2

object ThreadMain {
  /**
   * �߳�ͨ�ŷ�ʽ:�˴˵ȴ��Է�ֱ��ִ�����,��������������Ȩ���̷߳�������Ϣ�����Ѿ�ִ�����.
   */
  def log(msg: String) {
    println(s"${Thread.currentThread.getName}: $msg")
  }
  def thread(body: => Unit): Thread = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }
  def main(args: Array[String]) {
    //��õ�ǰ�߳�����,���������ô洢����Ϊt�ľֲ������.
    val t = Thread.currentThread()
    //��õ�ǰ�̵߳����� 
    val name = t.getName
    println(s"I am the thread $name")

    class MyThread extends Thread {
      override def run(): Unit = {
        println("New thread running.")
      }
    }
    val tmy = new MyThread

    tmy.start()
    //��main�߳��л����ȴ�״ֱ̬��MyThread�߳�ִ�����Ϊֹ
    tmy.join() //�÷�������ͣmain�̵߳�ִ�й���,
    println("New thread joined.")
    val th = thread {
      Thread.sleep(1000)
      log("New thread running.")
      Thread.sleep(1000)
      log("Still running.")
      Thread.sleep(1000)
      log("Completed.")
    }
    th.join()
    log("New thread joined.")
    /**
     * ���������������Զ����"New thread joined.",
     * ʹ��Thread���еľ�̬����sleep,�÷���������ͣ����ִ�е��߳�
     * ʹ֮��һ��ָ����ʱ��(��λΪ����)�ڵȴ�,���������ʹ�ø��߳̽���
     * ��ʱ�ȴ�״̬,������sleep����ʱ,OS���Խ������߳�����ʹ�õĴ�����
     * ���·���������߳�.,join������main�߳��л����˵ȴ�״̬,ֱ�����
     * t�е����߳�ִ�����Ϊֹ.
     */

    /*for (i <- 0 until 100000) {
      var a = false
      var b = false
      var x = -1
      var y = -1
      
       // ��������������,���ǻ�õ�����Ľ��,�ý����������X��Y��ͬʱ����1��
       

      val t1 = thread {
        Thread.sleep(2)
        a = true
        y = if (b) 0 else 1
      }
      val t2 = thread {
        Thread.sleep(2)
        b = true
        x = if (a) 0 else 1
      }

      t1.join()
      t2.join()
      assert(!(x == 1 && y == 1), s"x = $x, y = $y")
    }*/

    import scala.collection._
    val transfers = mutable.ArrayBuffer[String]()
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
    def add(account: Account, n: Int) = account.synchronized { //���transfers�����
      account.money += n //���ת�˽���10�����ҵ�λ,��Ҫ�ò�����¼����
      if (n > 10) logTransfer(account.name, n)
    }
    val jane = new Account("Jane", 100)
    val john = new Account("John", 200)
    val t1 = thread { add(jane, 5) }
    val t2 = thread { add(john, 50) }
    val t3 = thread { add(jane, 70) }
    t1.join(); t2.join(); t3.join()
    log(s"--- transfers ---\n$transfers")
    
    
    import scala.collection._
  /**
 * ʹ��Queue�洢�����ȵĴ����,ʹ�ú���() => Unit������Щ�����
 */
   val tasks = mutable.Queue[() => Unit]()
   
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
      case Some(task) => task() //������������
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

  asynchronous { log("Hello") }//���ݷ���
  asynchronous { log(" world!")}//���ݷ���
  Thread.sleep(100)

  }

}

