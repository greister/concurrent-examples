package org.learningconcurrency






package object ch2 {
/**
 * ����һ�����������,����һ������run������ִ�иô��������߳�,��������½��߳�,Ȼ�󷵻ضԸ��̵߳�����,
 * �Ӷ�ʹ�������߳��ܹ����ø��߳��е�join����
 */
  def thread(body: =>Unit): Thread = {
    val t = new Thread {
      override def run() = body
    }
    t.start()
    t
  }

}

