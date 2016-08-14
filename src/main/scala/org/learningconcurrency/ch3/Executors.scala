
package ch3

import org.learningconcurrency._
import ch3._
import java.util.concurrent.TimeUnit
/**
 * ʵ����ForkJoinPool�ӿڷ�ʽ,�Լ������ύ�ܹ�ͨ���첽��ʽ���������ķ�ʽ
 */
object ExecutorsCreate extends App {
  import scala.concurrent._
  import java.util.concurrent.TimeUnit
  /**
   * Fork/Join�����Java7�ṩ�˵�һ�����ڲ���ִ������Ŀ�ܣ� ��һ���Ѵ�����ָ�����ɸ�С����
   * ���ջ���ÿ��С��������õ����������Ŀ��
   * 
   * ForkJoinPool:ForkJoinTask��Ҫͨ��ForkJoinPool��ִ�У�����ָ�������������ӵ���ǰ�����߳���ά����˫�˶����У�������е�ͷ����
   * ��һ�������̵߳Ķ�������ʱû������ʱ��������������������̵߳Ķ��е�β����ȡһ������
   **/
  val executor = new java.util.concurrent.ForkJoinPool //ʵ����ForkJoinPool��
  /**
   * ForkJoinPool�ӿ�execute����Runnable
   */
  executor.execute(new Runnable {
    def run() = log("This task is run asynchronously.")
  })
  //
   //Thread.sleep(500)
  //��ʹ��Thread.sleep,����������������������ȴ�ʱ��
   executor.awaitTermination(60, TimeUnit.SECONDS)
}
/**
 * ExecutionContext��İ���������Ĭ�ϵ�������,�ö�����ڲ�ʹ��ForkJoinPoolʵ��
 */

object ExecutionContextGlobal extends App {
  import scala.concurrent._
  val ectx = ExecutionContext.global
  ectx.execute(new Runnable {
    def run() = log("Running on the execution context.")
  })
  Thread.sleep(1000)
  //��ʹ��Thread.sleep,����������������������ȴ�ʱ��
   
}
/**
 * ExecutionContext��İ�����������������ExecutionContextExecutor��ExecutionContextExecutorService
 * ������ͨ��new forkjoin.ForkJoinPool����ExecutionContextExecutor����,��ζ�����ForkJoinPoolʵ��ͨ����
 * �������̳߳��б���Ĭ���߳���,ʵ��ȫ�ֶ���ExecutionContext������Ӽ��.
 * 
 */

object ExecutionContextCreate extends App {
  import scala.concurrent._
  val ectx = ExecutionContext.fromExecutorService(new forkjoin.ForkJoinPool)
  ectx.execute(new Runnable {
    def run() = log("Running on the execution context again.")
  })

}
/**
 * ����32����������,ÿ�����������������,��ִ�����֮ǰӵ��10��ĵȴ�ʱ��
 */

object ExecutionContextSleep extends App {
  import scala.concurrent._
  for (i <- 0 until 32) execute {
    Thread.sleep(2000)
    log(s"Task $i completed.")
  }
  Thread.sleep(10000)
}


