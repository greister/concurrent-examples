package ch2

object SynchronizedProtectedMain {
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
  
  def main(args: Array[String]): Unit = {

    var result:String=null
    val t=thread{result="\n title\n"+"="*5}
    t.join()
    /**
     * result��Զ������ʾnull,��Ϊ����join�����Ĳ��������ڵ���println����֮ǰִ��,���ҶԱ���resultִ�е�
     *join() //�÷�������ͣmain�̵߳�ִ�й���,��main�߳��л����ȴ�״ֱ̬��MyThread�߳�ִ�����Ϊֹ
     */
    println(s"${Thread.currentThread.getName}: $result")
    
    var uidCount = 0L
   /**
    * synchronized ���ȷ��һ���߳�ִ�е�ͬ�������벻��ͬʱ���������߳�ִ��,
    * ��ȷ����ͬһ����(this)�е�����ͬ������鲻�ᱻ����
    */
    def getUniqueIdSyn() = this.synchronized {
      val freshUid = uidCount + 1
      uidCount = freshUid
      freshUid
    }
    /**
     * ���ɲ�ͬ���� �߳�����Ӧ����ִ�д��������,
     * ����t�е��߳���ʱ���Բ�����ʽ����,getUniqueId����,�ڳ���ͷ���Բ�����ʽ��ȡ����uidCount��ֵ
     * �ñ����ĳ�ʼ��ֵΪ0,Ȼ�������ƶ��Լ��ı���freshUidΪֵ1,freshUid��һ���ֲ�����,�������õ����߳��ڴ�
     * �����̶߳��ܹ������ñ����Ķ���ʵ��.
     * 
     */
    def getUniqueId()={
      val freshUid = uidCount + 1
      uidCount = freshUid
      freshUid
    }
    //
    def printUniqueIds(n: Int): Unit = {
      val uids = for (i <- 0 until n) yield getUniqueIdSyn()
      println(s"Generated uids: $uids")
    }
    /** 
     *  ���synchronized����
     * Generated uids: Vector(2, 3, 5, 8, 10)
     * Generated uids: Vector(1, 4, 6, 7, 9)
     * 
     * δ��synchronized����
     * Generated uids: Vector(1, 3, 4, 5, 6)
		 * Generated uids: Vector(2, 4, 7, 8, 9)		
     */
    val th = thread {
      printUniqueIds(5)
    }
    printUniqueIds(5)
    th.join()
    
    
     for (i <- 0 until 10000) {
    var t1started = false
    var t2started = false
    var t1index = 0
    var t2index = 0
  //���������߳�(t1,t2)����һ�Բ����ͱ���(a��b)��һ�Ա���(x,y)
  //�߳�t1������a����Ϊtrue,Ȼ���ȡ����b��ֵ,�������b��ֵΪtrue
  //�߳�t1�ͻὫ0�������y,����ͻὫ1�������y
    val t1 = thread {
      Thread.sleep(1)
      this.synchronized { t1started = true }
      //t1started = true
      val t2s = this.synchronized { t2started }
      t2index = if (t2started) 0 else 1
    }
    val t2 = thread {
      Thread.sleep(1)
       //t2started = true
      this.synchronized { t2started = true }
      val t1s = this.synchronized { t1started }
      t1index = if (t1s) 0 else 1
    }
  
    t1.join()
    t2.join()
    assert(!(t1index == 1 && t2index == 1), s"t1 = $t1index, t2 = $t2index")
  }

  }
}