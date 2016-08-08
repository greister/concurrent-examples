package ch2
/**
 *
 */
object VolatileMain {

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
  def main(args: Array[String]): Unit = {
    /**
     * ������������ҳ�ı����Ƿ�����ٺ���һ��!����,���������߳̿�ʼɨ����һ����׫д�ļ�ҳӢ�۹���
     * һ��ĳ���߳��ҵ�!����,���Ǿ���Ҫֹͣ�����̵߳���������
     */
    case class Page(txt: String, var position: Int)
    val pages = for (i <- 1 to 5) yield new Page("Na" * (100 - 20 * i) + " Batman!", -1)
    @volatile var found = false //����ĳ���߳��Ѿ��ҵ���̾��
    for (p <- pages) yield thread {
      var i = 0
      while (i < p.txt.length && !found)
        if (p.txt(i) == '!') {
          p.position = i
          found = true
        } else i += 1
    }
    while (!found) {}
    log(s"results: ${pages.map(_.position)}")

  }
}