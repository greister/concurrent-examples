package com.sosnoski.concur.article4

import java.util.Timer
import java.util.TimerTask

import scala.concurrent._

/** Create Future[T] instances which will be completed after a delay.
 *  ��ģ�첽�¼�
  */
object TimedEvent {
  //Scala ����ʹ��һ�� java.util.Timer ������ java.util.TimerTask ��һ���ӳ�֮��ִ��
  val timer = new Timer
/**
 * delayedSuccess ����������һ������������ʱ�ɹ����һ�� Scala Future[T]��Ȼ�󽫸� future ���ظ����÷���
 * delayedSuccess ����������ͬ���͵� future����ʹ����һ������� future ʱ���� IllegalArgumentException �쳣��ʧ������
 */
  /** Return a Future which completes successfully with the supplied value after secs seconds. */
  def delayedSuccess[T](secs: Int, value: T): Future[T] = {
    val result = Promise[T]
    //java.util.TimerTask ��һ���ӳ�֮��ִ�С�ÿ�� TimerTask ������ʱ���һ���й����� future
    timer.schedule(new TimerTask() {
      def run() = {
        result.success(value)
      }
    }, secs * 1000)
    result.future
  }

  /** Return a Future which completes failing with an IllegalArgumentException after secs
    * seconds. */
  def delayedFailure(secs: Int, msg: String): Future[Int] = {
    val result = Promise[Int]
    timer.schedule(new TimerTask() {
      def run() = {
        result.failure(new IllegalArgumentException(msg))
      }
    }, secs * 1000)
    result.future
  }
}