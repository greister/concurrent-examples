package org.learningconcurrency


import scala.concurrent._

package object ch3 {
/**
 * ����һ�����������,����һ������run������ִ�иô��������߳�,��������½��߳�,Ȼ�󷵻ضԸ��̵߳�����,
 * �Ӷ�ʹ�������߳��ܹ����ø��߳��е�run����
 * ExecutionContext ��Ϊ��ִ�е��߼��߳���ص�������Ϣ�ṩ��������
 */
  def execute(body: =>Unit) = ExecutionContext.global.execute(new Runnable {
    def run() = body
  })

}

