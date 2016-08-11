
package ch5

import org.learningconcurrency._
import ch5._




object ParBasic extends App {
  import scala.collection._
/**
 * ʾ��ʹ��Vector��,������һ������500������ֵ�Vector����,Ȼ��ʹ��Random�������ʽ�����ö�������
 * Ȼ����γ���Ƚ�˳���max�����Ͳ���max����������ʱ��,��������������Ѱ���������е��������
 */
  val numbers = scala.util.Random.shuffle(Vector.tabulate(5000000)(i => i))

  val seqtime = timed {
    val n = numbers.max
    println(s"largest number $n")
  }
//˳���max��������ʱ��Ϊ381.411 ����
  log(s"Sequential time $seqtime ms")

  val partime = timed {
    val n = numbers.par.max
    println(s"largest number $n")
  }
//���е�max����������293.74 ����
  log(s"Parallel time $partime ms")
}


object ParUid extends App {
  import scala.collection._
  import java.util.concurrent.atomic._
  private val uid = new AtomicLong(0L)
/**
 * ԭ�ӱ���ʵ�ֵ�incrementAndGet����,ʹ�ò��м���,������ֵ��Ψһ��ʶ��
 */
  val seqtime = timed {
    for (i <- 0 until 10000000) uid.incrementAndGet()
  }
  //Sequential time 370.134 ms
  log(s"Sequential time $seqtime ms")

  val partime = timed {
    //forѭ����ʹ���˲��м���,�����ò��м����е�foreach����,�����е�Ԫ�ػ��Բ�����ʽ������
    //����ζ�Ŷ������߳�ͬʱ����ָ���ĺ���,��˱����ʵ���ͬ������.
    for (i <- (0 until 10000000).par) uid.incrementAndGet()
  }
  //Parallel time 714.495 ms  
  log(s"Parallel time $partime ms")
 /**
  * ����������а汾�������ٶ���������,�ó��������������,˳��foreach��������ʱ��Ϊ370.134����
  * ������foreach��������ʱ��Ϊ714.495����.
  * ��Ҫԭ��:����߳�ͬʱ������ԭ�ӱ����uid�е�incrementAndGet����,����ͬʱ��һ���ڴ�λ��ִ��д�����
  */
}


object ParGeneric extends App {
  import scala.collection._
  import scala.io.Source

  def findLongestLine(xs: GenSeq[String]): Unit = {
    val line = xs.maxBy(_.length)
    log(s"Longest line - $line")
  }

  val doc = Array.tabulate(1000)(i => "lorem ipsum " * (i % 10))

  findLongestLine(doc)
  findLongestLine(doc.par)

}


object ParConfig extends App {
  import scala.collection._
  import scala.concurrent.forkjoin.ForkJoinPool

  val fjpool = new ForkJoinPool(2)
  val myTaskSupport = new parallel.ForkJoinTaskSupport(fjpool)
  val numbers = scala.util.Random.shuffle(Vector.tabulate(5000000)(i => i))
  val partime = timed {
    val parnumbers = numbers.par
    parnumbers.tasksupport = myTaskSupport
    val n = parnumbers.max
    println(s"largest number $n")
  }
  log(s"Parallel time $partime ms")  
}
/**
 * ��������Ҫ����TEXTAREA�����Html�ļ��е�����,����Ա�дһ������ ,����HTML�淶�ĵ���������һ�����ֵ�TEXTAREA�ַ��� 
 * getHtmlSpec����ͨ���첽�����������HTML�淶,Ȼ�󷵻���HTML�淶�������Ƶ�Future����,֮����ö������һ���ص�����
 * һ�������HTML�淶�����ݺ�,�Ϳ��Ե���Future�����е�indexWhere����,�ҵ���������ʽ".*TEXTAREA.*"ƥ�����
 */

object ParHtmlSpecSearch extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.collection._
  import scala.io.Source

  def getHtmlSpec() = Future {
    val specSrc: Source = Source.fromURL("http://www.w3.org/MarkUp/html-spec/html-spec.txt")
    try specSrc.getLines.toArray finally specSrc.close()
  }

  getHtmlSpec() foreach { case specDoc =>
    log(s"Download complete!")
    //GenSeq˳��Ͳ���������,������ʹ�øó����˳��Ͳ��а汾������.
    def search(d: GenSeq[String]) = warmedTimed() {
      d.indexWhere(line => line.matches(".*TEXTAREA.*"))
    }

    val seqtime = search(specDoc)
    //Sequential time 3.711 ms
    log(s"Sequential time $seqtime ms")

    val partime = search(specDoc.par)
    //Parallel time 2.539 ms
    log(s"Parallel time $partime ms")
  }
  /**
   * ��������ʱ��ֱ�:3.729 ms,2.419 ms,3.711,2.539 
   * �ó�JVM�Ѿ��ﵽ�ȶ�״̬�Ĵ������,ʵ������JVM�ʵ��Ż��ó���֮ǰ,����Ӧ�����иó������Ĵ���
   * 
   * 
   */
Thread.sleep(5000)
}

/**
 * �ǲ��л�����,���÷ǿɲ��л������е�par����,��Ҫ�����ǵ�Ԫ�ظ��Ƶ��¼�����
 */
object ParNonParallelizableCollections extends App {
  import scala.collection._
 //����List�����е�par����ʱ,��Ҫ��List�����е�Ԫ�ظ��Ƶ�Vector������
  val list = List.fill(1000000)("")
  val vector = Vector.fill(1000000)("")
  log(s"list conversion time: ${timed(list.par)} ms")
  log(s"vector conversion time: ${timed(vector.par)} ms")
  /**
   * ����:��˳�򼯺����м��ϵ�ת�����������ǲ���,�����п��ܳ�Ϊ����ƿ��
   * main: list conversion time: 206.077 ms
	 * main: vector conversion time: 0.053 ms
   */
}


object ParNonParallelizableOperations extends App {
  import scala.collection._
  import scala.concurrent.ExecutionContext.Implicits.global
  import ParHtmlSpecSearch.getHtmlSpec

  getHtmlSpec() foreach { case specDoc =>
    def allMatches(d: GenSeq[String]) = warmedTimed() {
      val results = d.foldLeft("")((acc, line) => if (line.matches(".*TEXTAREA.*")) s"$acc\n$line" else acc)
      // Note: must use "aggregate" instead of "foldLeft"!
    }

    val seqtime = allMatches(specDoc)
    log(s"Sequential time - $seqtime ms")

    val partime = allMatches(specDoc.par)
    log(s"Parallel time   - $partime ms")
  }
}


object ParNonDeterministicOperation extends App {
  import scala.collection._
  import scala.concurrent.ExecutionContext.Implicits.global
  import ParHtmlSpecSearch.getHtmlSpec

  getHtmlSpec() foreach { case specDoc =>
    val seqresult = specDoc.find(line => line.matches(".*TEXTAREA.*"))
    val parresult = specDoc.par.find(line => line.matches(".*TEXTAREA.*"))
    log(s"Sequential result - $seqresult")
    log(s"Parallel result   - $parresult")
  }
}


object ParNonCommutativeOperator extends App {
  import scala.collection._
  
  val doc = mutable.ArrayBuffer.tabulate(20)(i => s"Page $i, ")
  def test(doc: GenIterable[String]) {
    val seqtext = doc.seq.reduceLeft(_ + _)
    val partext = doc.par.reduce(_ + _)
    log(s"Sequential result - $seqtext\n")
    log(s"Parallel result   - $partext\n")
  }
  test(doc)
  test(doc.toSet)
}


object ParNonAssociativeOperator extends App {
  import scala.collection._

  def test(doc: GenIterable[Int]) {
    val seqtext = doc.seq.reduceLeft(_ - _)
    val partext = doc.par.reduce(_ - _)
    log(s"Sequential result - $seqtext\n")
    log(s"Parallel result   - $partext\n")
  }
  test(0 until 30)
}


object ParMultipleOperators extends App {
  import scala.collection._
  import scala.concurrent.ExecutionContext.Implicits.global
  import ParHtmlSpecSearch.getHtmlSpec

  getHtmlSpec() foreach { case specDoc =>
    val length = specDoc.aggregate(0)(
      (count: Int, line: String) => count + line.length,
      (count1: Int, count2: Int) => count1 + count2
    )
    log(s"Total characters in HTML spec - $length")
  }
}


object ParSideEffectsIncorrect extends App {
  import scala.collection._

  def intSize(a: GenSet[Int], b: GenSet[Int]): Int = {
    var count = 0
    for (x <- a) if (b contains x) count += 1
    count
  }
  val seqres = intSize((0 until 1000).toSet, (0 until 1000 by 4).toSet)
  val parres = intSize((0 until 1000).par.toSet, (0 until 1000 by 4).par.toSet)
  log(s"Sequential result - $seqres")
  log(s"Parallel result   - $parres")
}


object ParSideEffectsCorrect extends App {
  import scala.collection._
  import java.util.concurrent.atomic._

  def intSize(a: GenSet[Int], b: GenSet[Int]): Int = {
    val count = new AtomicInteger(0)
    for (x <- a) if (b contains x) count.incrementAndGet()
    count.get
  }
  val seqres = intSize((0 until 1000).toSet, (0 until 1000 by 4).toSet)
  val parres = intSize((0 until 1000).par.toSet, (0 until 1000 by 4).par.toSet)
  log(s"Sequential result - $seqres")
  log(s"Parallel result   - $parres")
}


object ParMutableWrong extends App {
  import scala.collection._

  val buffer = mutable.ArrayBuffer[Int]() ++= (0 until 250)
  for (x <- buffer.par) buffer += x
  log(buffer.toString)
}






