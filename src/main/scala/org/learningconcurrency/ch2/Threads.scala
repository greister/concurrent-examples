package ch2

import org.learningconcurrency._
import ch2._

object ThreadsMain extends App {
  //��ȡ��ǰ�̵߳�����
  val name = Thread.currentThread.getName
  println(s"I am the thread $name")
}


object ThreadsStart extends App {
  class MyThread extends Thread {
    /**
     * �����߳�
     */
    override def run(): Unit = {
      println(s"I am ${Thread.currentThread.getName}")
    }
  }

  val t = new MyThread()
  t.start()
  println(s"I am ${Thread.currentThread.getName}")
}


object ThreadsCreation extends App {

 /**
  *�̴߳���
  */
  class MyThread extends Thread {
    override def run(): Unit = {
      println("New thread running.")
    }
  }
  val t = new MyThread

  t.start()
  /**
   * ��main�߳��л����ȴ�״̬,ֱ������t�е����߳�ִ�����Ϊֹ,�ص��Ǵ��ڵȴ�״̬���̻߳ύ������������Ȩ,
   * OS���Խ�����������������ȴ��������߳�.
   */
  
  t.join() 
  println("New thread joined.")

}


object ThreadsSleep extends App {
  


  val t = thread {
    Thread.sleep(1000)
    log("New thread running.")
    Thread.sleep(1000)
    log("Still running.")
    Thread.sleep(1000)
    log("Completed.")
  }
   /**
   * ��main�߳��л����ȴ�״̬,ֱ������t�е����߳�ִ�����Ϊֹ,�ص��Ǵ��ڵȴ�״̬���̻߳ύ������������Ȩ,
   * OS���Խ�����������������ȴ��������߳�.
   */
  t.join()
  log("New thread joined.")

}


object ThreadsNondeterminism extends App {
  /**
   * log���,�ڱ���t�߳��еĵ���log���������֮ǰ,����֮�����.������̷߳�ȷ��
   */
  val t = thread { log("New thread running.") }
  log("...")
  log("...")
  t.join()
  log("New thread joined.")

}


object ThreadsCommunicate extends App {
   /**
    * ԭ��ʽִ��, t.join()�˴˵ȴ��Է�ֱ��ִ�����,��������������Ȩ֮ǰִ��,��result����ִ�еĸ�ֵ����.
    * ����result��Զ����Ϊnullֵ
    */
  var result: String = null
  val t = thread { result = "\nTitle\n" + "=" * 5 }
  t.join()
  log(result)
}

/***
 * �̲߳���ȫ����
 */
object ThreadsUnprotectedUid extends App {
  /**
   * 
   */
  var uidCount = 0L//������ʽ��ȡ����uidCount��ֵ,�ñ����ĳ�ʼֵΪ0,
  def getUniqueId() = {
    val freshUid = uidCount + 1  //freshUid ��һ���ֲ�����,��������߳�ջ�ڴ�,�����̶߳��ܹ������ñ����Ķ���ʵ��
    uidCount = freshUid 
    freshUid
  }

  def printUniqueIds(n: Int): Unit = {
    //yield��������
    val uids = for (i <- 0 until n) yield getUniqueId()
    log(s"Generated uids: $uids")
  }
  //�̴߳���
  val t = thread {
    printUniqueIds(5)//�̲߳���ִ��
  }
  //main�߳�ִ��
  printUniqueIds(5)
  t.join()
  /**
   * �������߳�ͨ�����˳����ֵ1д�ر���uidCount��,Ȼ�����Ƕ�����һ����Ψһ��ʶ����1
   * Thread-0: Generated uids: Vector(1, 11, 13, 15, 17)
	 * main: Generated uids: Vector(1, 3, 5, 7, 9)
	 * 
   */
  
}
/**
 * ��ʹ��synchronized��䵼�µ����ش���,ʹ�ò���ֵ�ƹ�synchronized���
 */

object ThreadSharedStateAccessReordering extends App {
   /**
    * ����ĳ����������߳�(T1��T2)����һ�Բ�������(a��b)��һ�����ͱ���(x��y),
    * �߳�t1������a����Ϊtrue,Ȼ���ȡ����b��ֵ,�������b��ֵΪtrue,��ô�߳�t1�ͻὫ0�������y
    * ����1�������y
    */
  for (i <- 0 until 100000) {
    var a = false
    var b = false
    var x = -1
    var y = -1

    val t1 = thread {
      //Thread.sleep(2)
      a = true 
      y = if (b) 0 else 1
    }
    val t2 = thread {
      //Thread.sleep(2)
      b = true
      x = if (a) 0 else 1
    }
    /**
     * ���ϳ��򷶵Ĵ���һ���̵߳�д������ܹ����̱������̶߳���,Ҫȷ�������߳��ܹ���һ���߳�д����������
     * �ͱ����ʵ�ʹ��ͬ��������
     */
  
    t1.join()
    t2.join()
    assert(!(x == 1 && y == 1), s"x = $x, y = $y")
  }
}