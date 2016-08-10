
package ch4

import org.learningconcurrency._

/**
 * Future�������������,
 * Await����result��ready���������߳�
 * ready�����������������߳�,ֱ��ָ����Future��������Ϊֹ
 * result�����������������߳�,�������Future�����Ѿ����ɹ�����,��ô�÷����ͻ᷵��Future���������ֵ
 * �������Future�������ִ��ʧ����,��ô�����Ὣ�쳣����Future����.
 */

object BlockingAwait extends App {
  /**
   * ����main�߳�ִ����һ���������,�ò��������URL�淶������,Ȼ�����ȴ�״̬
   */
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.concurrent.duration._
  import scala.io.Source

  val urlSpecSizeFuture = Future { Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").size }
  //�������߳��ܹ��ȴ����ʱ��
  val urlSpecSize = Await.result(urlSpecSizeFuture, 10.seconds)

  log(s"url spec contains $urlSpecSize characters")

}

/**
 * ִ��������ͨ����ʹ���̳߳�ʵ��,����ִ��������̻߳ᵼ�³����̼߳������,����Future����ή�Ͳ��ж�,������������.
 */
object BlockingSleepBad extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  val startTime = System.nanoTime
/**
 * ��������չʾ���������,16��������Future���������sleep����,����main�߳̽����˵ȴ�״̬
 * ֱ����Щ����������Ϊֹ.
 */
  val futures = for (_ <- 0 until 16) yield Future {
    Thread.sleep(1000)
  }

  for (f <- futures) Await.ready(f, Duration.Inf)

  val endTime = System.nanoTime

  log(s"Total execution time of the program = ${(endTime - startTime) / 1000000} ms")
  log(s"Note: there are ${Runtime.getRuntime.availableProcessors} CPUs on this machine")

}


object BlockingSleepOk extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.concurrent.duration._

  val startTime = System.nanoTime
  /**
   * �����Ҫ�ڳ�����ʹ����������,��ô��ִ�����������Ĵ����װ��blocking���������,�����ִ֪ͨ��������,
   * ����������̱߳�������,������ִ������ ��������ʱ�Ķ����߳�.
   */

  val futures = for (_ <- 0 until 16) yield Future {
    /**
     * ͨ��ʹ��blocking����װ����sleep���������,��globalִ�������ļ�⵽����������ȴ��������̵߳�������ʱ
     * globalִ�������ľ����ɶ�����߳�.16��Future���㶼���Բ�����ʽִ��,������γ������1��֮��ִ�����
     */
    blocking {//������ִ���첽�����Ĵ�����,ָ������װ�Ĵ�����к���ʵ�����������Ĵ���.�������޷�ʵ����������
      Thread.sleep(1000)
    }
  }

  for (f <- futures) Await.ready(f, Duration.Inf)

  val endTime = System.nanoTime

  log(s"Total execution time of the program = ${(endTime - startTime) / 1000000} ms")

}

