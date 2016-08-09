package ch4

import org.learningconcurrency._
/**
 * Future ����һ���������ͣ�����һ���������ջ᷵�ص�T���ͽ����������������ܻ�����ִ�г�ʱ��
 * ���Ե�Future���ʱ�����п�����ȫû�б��ɹ�ִ�У���ʱ�������һ���쳣
 *  Future ��ʾһ�����ܻ�û��ʵ����ɵ��첽����Ľ��,
 *  ���������������� Callback �Ա�������ִ�гɹ���ʧ�ܺ�������Ӧ�Ĳ���
 */
object FuturesComputation extends App {
  /**
   * Computation����
   *  Futures ִ�м���
   * 1,��������globalִ��������,����ȷ����ȫ����������ִ��Future����
   * ����log�����Ĵ�����ȷ��,���������Futures��������,��Ϊ����Apply����������﷨��
   * 
   */
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  Future {
    log(s"the future is here")
  }

  log(s"the future is coming")

}

/**
 *  ͨ��Futures����,ʹ��Source.fromFile�����ȡbuild.sbt�ļ�����
 * 1,��������globalִ��������,����ȷ����ȫ����������ִ��Future����
 * 
 * 
 */
object FuturesDataType extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source
 //ͨ��Futures����,ʹ��Source.fromFile�����ȡbuild.sbt�ļ�����
  val buildFile: Future[String] = Future {
    val f = Source.fromFile("build.sbt")
    try f.getLines.mkString("\n") finally f.close()
  }

  log(s"started reading build file asynchronously")
  /**
   * main�̻߳����Future�����е�isCompleted����,��Future����Ϊͨ��ִ��Future������buildFile����
   * ��ȡbuild.sbt�ļ��Ĳ����ܿ��ܺܿ����,���isCompleted�����᷵��false,
   * ��250�����,main�̻߳��ٴε���isCompleted�����᷵��true,
   * ���main�̻߳����value����,�÷����᷵��build.sb�ļ�������
   * 
   */
  log(s"status: ${buildFile.isCompleted}")//�Ƿ����
  Thread.sleep(250)
  log(s"status: ${buildFile.isCompleted}")//�Ƿ����
  log(s"status: ${buildFile.value}")//����ֵ

}
/**
 * Futures����Ļص�����
 * ��w3���в��ҳ����е���telnet.
 */

object FuturesCallbacks extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source
/**
 * ��w3��վ���url�淶���ĵ�,ʹ��Source����洢���ļ�������,��ʹ��getUrlSpec�ļ��е�Future����
 * ���첽��ʽִ��Http�������,getUrlSpec�������ȵ���fromURL��ȡ�����ı��ĵ���Source����,Ȼ������
 * getLines������ȡ�ĵ��е����б�.
 */
  
  def getUrlSpec(): Future[Seq[String]] = Future {    
    val f = Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt")
    try {
      f.getLines.toList
      }finally{
      f.close()
     }
  }

  val urlSpec: Future[Seq[String]] = getUrlSpec()
/**
 * Ҫ��Future����urlSpec���ҵ������е���telnet����,����ʹ��find����
 * �÷������е��б����Ҫ�����ĵ��ʽ���Ϊ����,���ҷ��غ���ƥ�����ݵ��ַ���.
 * ����һ��Seq���͵Ĳ���,��urlSpec���󷵻�Future[Seq[String]],����޷���Future����urlSpec
 * ֱ�ӷ��͸�find����,�����ڳ������find����ʱ,��Future�ܿ��ܻ��޷�ʹ��.
 */
  def find(lines: Seq[String], word: String) = lines.zipWithIndex.collect {
    case (line, n) if line.contains(word) => (n, line)
  }.mkString("\n")
/**
 * ����ʹ��foreach����Ϊ���Future���һ���ص�����,ע��onSuccess������foreach�����ȼ�,��onSuccess��������
 * ����scala 2.11֮������,foreach��������ƫ������Ϊ�����
 * �˴���Ҫ����:��ӻص������Ƿ���������,�ص��ú���ע���,main�߳����õ�log��������ִ��
 * ����ִ�лص�������log����ʱ�������ö�
 * ��Future�������ƺ�,�������̵��ûص�����,�����ִ��������ͨ����������,���첽��ʽ����ص�����
 */
  urlSpec.foreach {
    lines => log(s"Found occurrences of 'telnet'\n${find(lines, "telnet")}\n")
  }
   Thread.sleep(2000)
   log("callbacks registered, continuing with other work")

/**
 * ForkJoinPool-1-worker-5: Found occurrences of 'telnet'
 * (207,  telnet , rlogin and tn3270 )
 * (745,                         nntpaddress | prosperoaddress | telnetaddress)
 * (806,  telnetaddress           t e l n e t : / / login )
 * (931,   for a given protocol (for example,  CR and LF characters for telnet)
   ForkJoinPool-1-worker-5: Found occurrences of 'password'
 * (107,                         servers). The password, is present, follows)
 * (109,                         the user name and optional password are)
 * (111,                         user of user name and passwords which are)
 * (222,      User name and password)
 * (225,   password for those systems which do not use the anonymous FTP)
 * (226,   convention. The default, however, if no user or password is)
 * (234,   is "anonymous" and the password the user's Internet-style mail)
 * (240,   currently vary in their treatment of the anonymous password.  )
 * (816,  login                   [ user [ : password ] @ ] hostport )
 * (844,  password                alphanum2 [ password ] )
 * (938,   The use of URLs containing passwords is clearly unwise. )
 */
  urlSpec.foreach {
    lines => log(s"Found occurrences of 'password'\n${find(lines, "password")}\n")
  }
  Thread.sleep(1000)

  log("callbacks installed, continuing with other work")
  

}

/**
 * Failure �ص�
 */
object FuturesFailure extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val urlSpec: Future[String] = Future {
    Source.fromURL("http://www.w3.org/non-existent-url-spec.txt").mkString
  }

  urlSpec.failed.foreach {    
    case t => {      
      log(s"exception occurred - $t")     
    }    
  }

}


object FuturesExceptions extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val file = Future { Source.fromFile(".gitignore-SAMPLE").getLines.mkString("\n") }

  file foreach {
    text => log(text)
  }

  file.failed foreach {//�쳣����,�׳��쳣����FileNotFoundException
    case fnfe: java.io.FileNotFoundException => log(s"Cannot find file - $fnfe")
    case t => log(s"Failed due to $t")
  }

  import scala.util.{Try, Success, Failure}

  file onComplete {
    case Success(text) => log(text)
   //onComplete �ص���ʽ
    case Failure(t) => log(s"Failed due to $t")
  }

}


object FuturesTry extends App {
  import scala.util._

  val threadName: Try[String] = Try(Thread.currentThread.getName)
  val someText: Try[String] = Try("Try objects are created synchronously")
  val message: Try[String] = for {
    tn <- threadName
    st <- someText
  } yield s"$st, t = $tn"

  message match {
    case Success(msg) => log(msg)
    case Failure(error) => log(s"There should be no $error here.")
  }

}


object FuturesNonFatal extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val f = Future { throw new InterruptedException }
  val g = Future { throw new IllegalArgumentException }
  f.failed foreach { case t => log(s"error - $t") }
  g.failed foreach { case t => log(s"error - $t") }
}


object FuturesClumsyCallback extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import org.apache.commons.io.FileUtils._
  import java.io._
  import scala.io.Source
  import scala.collection.convert.decorateAsScala._

  def blacklistFile(filename: String) = Future {
    val lines = Source.fromFile(filename).getLines
    lines.filter(!_.startsWith("#")).toList
  }
  
  def findFiles(patterns: List[String]): List[String] = {
    val root = new File(".")
    for {      
      f <- iterateFiles(root, null, true).asScala.toList
      pat <- patterns
      abspat = root.getCanonicalPath + File.separator + pat
      if f.getCanonicalPath.contains(abspat)
    } yield f.getCanonicalPath
  }

  blacklistFile(".gitignore") foreach {
    case lines =>
      val files = findFiles(lines)
      log(s"matches: ${files.mkString("\n")}")
  }
}


object FuturesMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source
  import scala.util.Success

  val buildFile = Future { Source.fromFile("build.sbt").getLines }
  val gitignoreFile = Future { Source.fromFile(".gitignore-SAMPLE").getLines }

  val longestBuildLine = buildFile.map(lines => lines.maxBy(_.length))
  val longestGitignoreLine = for (lines <- gitignoreFile) yield lines.maxBy(_.length)

  longestBuildLine onComplete {
    case Success(line) => log(s"the longest line is '$line'")
  }

  longestGitignoreLine.failed foreach {
    case t => log(s"no longest line, because ${t.getMessage}")
  }
}


object FuturesFlatMapRaw extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val netiquette = Future { Source.fromURL("http://www.ietf.org/rfc/rfc1855.txt").mkString }
  val urlSpec = Future { Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").mkString }
  val answer = netiquette.flatMap { nettext =>
    urlSpec.map { urltext =>
      "First, read this: " + nettext + ". Now, try this: " + urltext
    }
  }

  answer foreach {
    case contents => log(contents)
  }
}


object FuturesFlatMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val netiquette = Future { Source.fromURL("http://www.ietf.org/rfc/rfc1855.txt").mkString }
  val urlSpec = Future { Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").mkString }
  val answer = for {
    nettext <- netiquette
    urltext <- urlSpec
  } yield {
    "First of all, read this: " + nettext + " Once you're done, try this: " + urltext
  }

  answer foreach {
    case contents => log(contents)
  }

}


object FuturesDifferentFlatMap extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val answer = for {
    nettext <- Future { Source.fromURL("http://www.ietf.org/rfc/rfc1855.txt").mkString }
    urltext <- Future { Source.fromURL("http://www.w3.org/Addressing/URL/url-spec.txt").mkString }
  } yield {
    "First of all, read this: " + nettext + " Once you're done, try this: " + urltext
  }

  answer foreach {
    case contents => log(contents)
  }

}


object FuturesRecover extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.io.Source

  val netiquetteUrl = "http://www.ietf.org/rfc/rfc1855.doc"
  val netiquette = Future { Source.fromURL(netiquetteUrl).mkString } recover {
    case f: java.io.FileNotFoundException =>
      "Dear boss, thank you for your e-mail." +
      "You might be interested to know that ftp links " +
      "can also point to regular files we keep on our servers."
  }

  netiquette foreach {
    case contents => log(contents)
  }

}


object FuturesReduce extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val squares = for (i <- 0 until 10) yield Future { i * i }
  val sumOfSquares = Future.reduce(squares)(_ + _)

  sumOfSquares foreach {
    case sum => log(s"Sum of squares = $sum")
  }
}



