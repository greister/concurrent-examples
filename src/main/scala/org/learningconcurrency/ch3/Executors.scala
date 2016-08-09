
package ch3

import org.learningconcurrency._
import ch3._


object ExecutorsCreate extends App {
  import scala.concurrent._
  /**
   * Fork/Join�����Java7�ṩ�˵�һ�����ڲ���ִ������Ŀ�ܣ� ��һ���Ѵ�����ָ�����ɸ�С����
   * ���ջ���ÿ��С��������õ����������Ŀ��
   * 
   * ForkJoinPool:ForkJoinTask��Ҫͨ��ForkJoinPool��ִ�У�����ָ�������������ӵ���ǰ�����߳���ά����˫�˶����У�������е�ͷ����
   * ��һ�������̵߳Ķ�������ʱû������ʱ��������������������̵߳Ķ��е�β����ȡһ������
   **/
  val executor = new java.util.concurrent.ForkJoinPool
  executor.execute(new Runnable {
    def run() = log("This task is run asynchronously.")
  })
}


object ExecutionContextGlobal extends App {
  import scala.concurrent._
  val ectx = ExecutionContext.global
  ectx.execute(new Runnable {
    def run() = log("Running on the execution context.")
  })
}


object ExecutionContextCreate extends App {
  import scala.concurrent._
  val ectx = ExecutionContext.fromExecutorService(new forkjoin.ForkJoinPool)
  ectx.execute(new Runnable {
    def run() = log("Running on the execution context again.")
  })
}


object ExecutionContextSleep extends App {
  import scala.concurrent._
  for (i <- 0 until 32) execute {
    Thread.sleep(2000)
    log(s"Task $i completed.")
  }
  Thread.sleep(10000)
}


