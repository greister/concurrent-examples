package org.learningconcurrency

package object ch5 {
  @volatile var dummy: Any = _  
/**
 * �÷�������մ����,������ִ�д������������ѵ�ʱ��
 */
  def timed[T](body: =>T): Double = {
    val start = System.nanoTime //��¼��ǰʱ��
    dummy = body //���ô����
    /**
     * JVM�е�ĳЩ����ʱ�Ż�����(������������),���ܻ�ȥ������body���������,
     * Ϊ�˱�������������,�������body�ķ���ֵ,������Ϊdummy��@volatile�ֶ�
     */
    val end = System.nanoTime //��¼ִ����body���ʱ��
    ((end - start) / 1000) / 1000.0 //��������ʱ��Ĳ�ֵ
  }

  def warmedTimed[T](times: Int = 200)(body: =>T): Double = {
    for (_ <- 0 until times) body
    timed(body)
  }
}

