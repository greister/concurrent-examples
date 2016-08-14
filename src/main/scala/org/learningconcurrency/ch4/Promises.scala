package ch4
import org.learningconcurrency._
import scala.concurrent.Future

/**
 * ���˵futures��Ϊ��һ����û�д��ڵĽ����������һ��ֻ��ռλ���Ķ�������ȥ������
 * ��ôpromise�ͱ���Ϊ��һ����д�ģ�����ʵ��һ��future�ĵ�һ��ֵ����,
 * promiseͨ������success�������Գɹ�ȥʵ��һ������ֵ��future. 
 */
object PromisesCreate extends App {
  /**
   * ����Promisesc����
   */
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  val p = Promise[String]//�洢�ַ���
  val q = Promise[String]
  //Promise����p����future������,����˻ص�����foreach
  p.future.foreach {
    case text => log(s"Promise p succeeded with '$text'")
  }
  //�ȴ�1��ʱ��,��ͨ������success�������ƶ���p֮ǰ,�ûص��������ᱻ����
  Thread.sleep(1000)
  p.success("kept")
  //trySuccess�ֱ��Ӧsuccess����,�÷������᷵�ر����ø�ֵ�����Ƿ�ɹ��Ĳ���ֵ
  val secondAttempt = p.trySuccess("kept again")

  log(s"Second attempt to complete the same promise went well? $secondAttempt")
  //q������future����ִ��ִ�����Ʋ���ʧ�ܵ����,���ڸ�future����������˻ص�����failed foreach 
  q.failure(new Exception("not kept"))
  q.future.failed.foreach {
    case t => log(s"Promise q failed with $t")
  }
  Thread.sleep(1000)
}

/**
 * �Զ��巽���첽����
 */
object PromisesCustomAsync extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import scala.util.control.NonFatal
/**
 * myFuture��������һ����Ϊbody����������,�ò�����ͨ���첽������
 * ���ȳ���ᴴ��һ��Promise����,Ȼ��,������globalִ����������ִ��һ���첽�������
 * �ü����������body��ֵ������Promise����,���body�Ĵ��������׳���һ���������쳣,
 * ���첽���������������쳣���޷�����Promise����,
 * ͬʱmyFuture�������ڸ��첽���������ʼִ�к�, ��������Future����.
 * 
 * ���ǳ�������Future�����ģʽ,�ȴ���Promise����,Ȼ��ͨ��������������������Promise����
 * ��������Ӧ��Future����,
 */
  def myFuture[T](body: =>T): Future[T] = {
    val p = Promise[T]
    global.execute(new Runnable {
      def run() = try {
        val result = body
        p.success(result)
      } catch {
        case NonFatal(e) =>
          p.failure(e)
      }
    })

    p.future
  }

  val future = myFuture {
    "naaa" + "na" * 8 + " Katamari Damacy!"
  }

  future.foreach {
    case text => log(text)
  }
  Thread.sleep(500)

}

/**
 * ת�����ڻص�����API
 */
object PromisesAndCallbacks extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import org.apache.commons.io.monitor._
  import java.io.File

  def fileCreated(directory: String): Future[String] = {
    val p = Promise[String]//����һ��Promise����,Ȼ��ͨ��һЩ��������ӳ����Ƹ�Promise����Ĳ���
    //FileAlterationMonitor�������ļ�ϵͳ�¼�,�紴����ɾ���ļ���Ŀ¼
    //FileAlterationMonitor����ᶨ��ɨ���ļ�ϵͳ,�Բ������еĸı�,֮��������Ҫ�ٴ���һ��FileAlterationObserver����
    //�ö������ص�����,��ĳ���ļ����ļ�ϵͳ�б�����ʱ,FileAlterationObserver������onFileCreate�����
    
    val fileMonitor = new FileAlterationMonitor(1000)
    val observer = new FileAlterationObserver(directory)
    val listener = new FileAlterationListenerAdaptor {
      //onFileCreate���������Ŀ¼�����Ʋ�����һ��Future����,�ö����к����½�Ŀ¼�е�һ���ļ�������
      override def onFileCreate(file: File) {
        try p.trySuccess(file.getName)//trySuccess�ֱ��Ӧsuccess����,�÷������᷵�ر����ø�ֵ�����Ƿ�ɹ��Ĳ���ֵ
        finally fileMonitor.stop()
      }
    }
    observer.addListener(listener)
    fileMonitor.addObserver(observer)
    fileMonitor.start()

    p.future
  }
/**
 * ʹ��Future����,�����ļ�ϵͳ�е�һ�����ı���ļ�,fileCreated�������ص�Future�����е�foreach����
 * �ڱ༭���д���һ�����ļ�,���۲�ó������½��ļ��ķ�ʽ.
 */
  fileCreated(".") foreach {
    case filename => log(s"Detected new file '$filename'")
  }
Thread.sleep(500)
}


object PromisesAndCustomOperations extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  implicit class FutureOps[T](val self: Future[T]) {
    def or(that: Future[T]): Future[T] = {
      val p = Promise[T]
      self onComplete { case x => p tryComplete x }
      that onComplete { case y => p tryComplete y }
      p.future
    }
  }

  val f = Future { "now" } or Future { "later" }

  f foreach {
    case when => log(s"The future is $when")
  }

}


object PromisesAndTimers extends App {
  import java.util._
  import scala.concurrent._
  import ExecutionContext.Implicits.global
  import PromisesAndCustomOperations._

  private val timer = new Timer(true)
/**
 * �жϳ�ʱ��һ���޷�ͨ��Future�����õ�ʵ�ù���,
 * �÷������մ��������ֵ�ı���t,���ڱ���t�޶���ʱ���ڷ��������Ƶ�Future����,
 * 
 */
  def timeout(millis: Long): Future[Unit] = {
    val p = Promise[Unit]//���ȴ���Promise����,��û������֮ǰ,�ö����к��е���Ϣ����û�������
    timer.schedule(new TimerTask {
     //ͨ��TimerTask����Time���е�schedule����,TimerTask������ڱ���t�޶���ʱ��������Promise����p
      def run() = p success ()
    }, millis)
    p.future
  }
//��timeout�������ص�Future�����������ӻص���������ͨ�������������Future�������
  val f = timeout(1000).map(_ => "timeout!") or Future {
    Thread.sleep(999)
    "work completed!"
  }

  f foreach {
    case text => log(text)
  }

}


object PromisesCancellation extends App {
  import scala.concurrent._
  import ExecutionContext.Implicits.global

  def cancellable[T](b: Future[Unit] => T): (Promise[Unit], Future[T]) = {
    val p = Promise[Unit]
    val f = Future {
      val r = b(p.future)
      if (!p.tryFailure(new Exception))
        throw new CancellationException
      r
    }
    (p, f)
  }

  val (cancel, value) = cancellable { cancel =>
    var i = 0
    while (i < 5) {
      if (cancel.isCompleted) throw new CancellationException
      Thread.sleep(500)
      log(s"$i: working")
      i += 1
    }
    "resulting value"
  }

  Thread.sleep(1500)

  cancel trySuccess ()

  log("computation cancelled!")
}


