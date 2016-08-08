package org.learningconcurrency
package ch2




/**
 * volatile变量能够以原子方式被读取和修改,而且大多用作状态标志
 * 优点:1,在单个线程中,对volatile变量执行写入和读取操作的次序是不会改变
 * 			2,对volatile变量执行写入操作的情况会立刻展示给所有线程
 */

object Volatile extends App {
  /**
   * 下面程序查明几页文本中是否会至少含有一个!符号,几个独立线程开始扫描由一个人撰写的几页英雄故事
   * 一旦某个线程找到!符号,我们就需要停止其他线程的搜索操作
   */
  case class Page(txt: String, var position: Int)
  val pages = for (i <- 1 to 5) yield
    new Page("Na" * (100 - 20 * i) + " Batman!", -1)
  @volatile var found = false //代表某个线程已经找到感叹号,@volatile注解以便将之声明为volatile变量
  //当某个线程在某一页中找到感叹号时,position变量就会被存入数值,found标志也会被设置,这样其他线程就可以早一点停止
  //它们的搜索操作
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


object VolatileScan extends App {
  val document: Seq[String] = for (i <- 1 to 5) yield "lorem ipsum " * (1000 - 200 * i) + "Scala"
  var results = Array.fill(document.length)(-1)
  @volatile var found = false
  val threads = for (i <- 0 until document.length) yield thread {
    def scan(n: Int, words: Seq[String], query: String): Unit =
      if (words(n) == query) {
        results(i) = n
        found = true
      } else if (!found) scan(n + 1, words, query)
    scan(0, document(i).split(" "), "Scala")
  }
  for (t <- threads) t.join()
  log(s"Found: ${results.find(_ != -1)}")
}


object VolatileUnprotectedUid extends App {

  @volatile var uidCount = 0L

  def getUniqueId() = {
    val freshUid = uidCount + 1
    uidCount = freshUid
    freshUid
  }

  def printUniqueIds(n: Int): Unit = {
    val uids = for (i <- 0 until n) yield getUniqueId()
    log(s"Generated uids: $uids")
  }

  val t = thread {
    printUniqueIds(5)
  }
  printUniqueIds(5)
  t.join()

}


object VolatileSharedStateAccess extends App {
  for (i <- 0 until 10000) {
    @volatile var t1started = false
    @volatile var t2started = false
    var t1index = -1
    var t2index = -1
  
    val t1 = thread {
      Thread.sleep(1)
      t1started = true
      t2index = if (t2started) 0 else 1
    }
    val t2 = thread {
      Thread.sleep(1)
      t2started = true
      t1index = if (t1started) 0 else 1
    }
  
    t1.join()
    t2.join()
    assert(!(t1index == 1 && t2index == 1), s"t1 = $t1index, t2 = $t2index")
  }
}

