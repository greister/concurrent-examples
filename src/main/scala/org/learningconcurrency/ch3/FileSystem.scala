package ch3


import org.learningconcurrency._
import ch3._

import java.io._
import java.util.concurrent._
import java.util.concurrent.atomic._
import scala.annotation.tailrec
import scala.collection._
import scala.collection.convert.decorateAsScala._
import org.apache.commons.io.FileUtils

/***
 * �ļ�ϵͳAPI�����ܹ����������м���:
 * 1)��ĳ���̴߳������ļ�ʱ,�����ļ������޷����ƺ�ɾ��
 * 2)��һ�������߳����ڸ����ļ�ʱ,�����ļ�����Ϊ�޷�ɾ��
 * 3)��ĳ���߳�ɾ��ĳ���ļ�ʱ,�����ļ�����Ϊ�޷�����
 * 4)���ļ�������ͬһʱ��ֻ���ɵ����߳�ɾ���ļ�
 */

object FileSystemTest extends App {
  val fileSystem = new FileSystem(".")

  fileSystem.logMessage("Testing log!")

  fileSystem.deleteFile("test.txt")

  fileSystem.copyFile("build.sbt", "build.sbt.backup")

  val rootFiles = fileSystem.filesInDir("")
  log("All files in the root dir: " + rootFiles.mkString(", "))
}


class FileSystem(val root: String) {
//һ����logger�Ķ����ػ��߳�
  val logger = new Thread {
    setDaemon(true)
    override def run() {
      while (true) {
        /**
         * ����take�����ǳ��Ӳ����������汾,������ֹ�߳�logger������,ֱ�������к�����ϢΪֹ.
         * ����take��,�߳�logger��ͨ������log���������Ϣ.
         */
        val msg = messages.take() 
        log(msg)
      }
    }
  }
//һ��main�߳���ֹ���к�,logger�߳�Ҳ���Զ�ֹͣ����
  logger.start()
/**
 * LinkedBlockingQueue һ�������ӽڵ�֧�ֵĿ�ѡ�н����
 * �ö�������Ϊmessages��˽�б���,
 */
  private val messages = new LinkedBlockingQueue[String]
/**
 * messages����������ö���messages�е�offer,Ҳ����ʹ��add����put����,��Ϊ����������޽��
 * ������Щ������Զ�������׳��쳣���������������ǵ��߳�
 */
  def logMessage(msg: String): Unit = messages.add(msg)
//��ʾ������״̬
  sealed trait State
//����
  class Idle extends State
//���ڴ��� 
  class Creating extends State
//���ڸ���״̬��,�ֶ�n����׷�ٵ�ǰ�Բ�����ʽ����ִ�еĸ��Ʋ���������
  class Copying(val n: Int) extends State
//����ɾ��
  class Deleting extends State
//isDir�ֶα�������Ӧ�Ķ������ļ�����Ŀ¼,
  class Entry(val isDir: Boolean) {
    //AtomicReference���Ӧ��ͨ�Ķ�������,Ҳ���������Ա�֤�����޸Ķ�������ʱ���̰߳�ȫ�ԡ�
    val state = new AtomicReference[State](new Idle)
  }
//�ò���ӳ�京��·������Ӧ��Entry����,������FileSystem�����,����Map�ͻᱻ�������
  val files: concurrent.Map[String, Entry] =
    //new ConcurrentHashMap().asScala
   new concurrent.TrieMap()//TrieMapȷ��ִ��ɾ�������ļ��������߳�,�޷�����ִ�ж�ȡ�ļ��������߳�
 //iterateFiles �ڶ��������������������������Ƿ�ݹ�
  for (file <- FileUtils.iterateFiles(new File(root), null, false).asScala) {
    //asScala�����ܹ�ȷ��java���Ͽ��Ի��scala����API.
    files.put(file.getName, new Entry(false))//�ļ�����ʵ�����(false)�ļ�
  }
/**
 * prepareForDelete�������ȶ�ȡԭ�ӱ�����state������,Ȼ�󽫸�ֵ�洢���ֲ�����S0��.
 * ֮��÷�������ֲ�����s0��ֵ�Ƿ�ΪIdle,��������ԭ�Ӵ���ʽ����ֵ����ΪDeleting
 * ����һ���߳����ڴ������Ƹ��ļ�ʱ,���޷���������ļ�,��˸��ļ�������������󲢷���false
 * �����һ���߳��Ѿ�ɾ��������ļ�,��ô���ļ����������᷵��false
 * ԭ�ӱ���Stats���þ�����һ��,��������û������ �����߳�Ҳû��ʹ�������̴߳��ڵȴ�״̬,���prepareForDelete����true
 * ��˵�����ǿ������̰߳�ȫɾ������ļ�,��Ϊ����ʹ���߳���Ψһһ����state������ֵ����Ϊdelete���߳�
 * ���prepareForDelete��������false,�ļ��������ͻ���UI�б������,����������ִ��ɾ���������߳�
 * 
 */
  @tailrec private def prepareForDelete(entry: Entry): Boolean = {
    val s0 = entry.state.get
    s0 match {
      case i: Idle =>
        /**
         * compareAndSet �����ǰֵ == Ԥ��ֵ������ԭ�ӷ�ʽ����ֵ����Ϊ�����ĸ���ֵ��
         * ������
         * expect - Ԥ��ֵ
         * update - ��ֵ
         * ���أ�����ɹ����򷵻� true������ false ָʾʵ��ֵ��Ԥ��ֵ����ȡ�
         */        
        if (entry.state.compareAndSet(s0, new Deleting)) true
        else prepareForDelete(entry)
      case c: Creating =>
        logMessage("File currently being created, cannot delete.")
        false
      case c: Copying =>
        logMessage("File currently being copied, cannot delete.")
        false
      case d: Deleting =>
        false
    }
  }
  /**
   * prepareForDelete����ͨ��ԭ�ӷ�ʽ�����ļ�,�Ա�ִ��ɾ������,Ȼ���files�����洢�Ĳ���ӳ����ɾ�����ļ�,
   * ��ͨ��ʵ��deleteFile����ɾ��Ŀ¼
   */

  def deleteFile(filename: String): Unit = {
    files.get(filename) match {
      case None =>
        logMessage(s"Cannot delete - path '$filename' does not exist!")
      case Some(entry) if entry.isDir =>
        logMessage(s"Cannot delete - path '$filename' is a directory!")
      case Some(entry) =>
        /**
         * execute�����첽��ʽɾ�����ļ�,�÷����������������߳�,ͨ������execute�������еĲ�������,�����prepareForDelete����
         * �������true,��ô����deleteQuietly�����Ĳ������ǰ�ȫ.
         */
        execute {
          if (prepareForDelete(entry)) {
            //��ȫɾ��
            if (FileUtils.deleteQuietly(new File(filename)))
              files.remove(filename)//���ļ��ʹӱ���files�洢�Ĳ���Map��ɾ��.
          }
        }
    }
  }
/**
 * ֻ�е��ļ�����Idle��Copying״̬ʱ,���ܶ���ִ�и��Ʋ���,���������Ҫͨ��ԭ�ӷ�ʽ���ļ�״̬��Idle�л���Copying
 * ����ͨ������ֵn���ļ�״̬��һ��Copying״̬�л�����һ��Copying״̬
 */
  @tailrec private def acquire(entry: Entry): Boolean = {//��ȡacquire
    val s0 = entry.state.get
    s0 match {
      case _: Creating | _: Deleting =>
        logMessage("File inaccessible, cannot copy.")
        false
      case i: Idle =>
        if (entry.state.compareAndSet(s0, new Copying(1))) true
        else acquire(entry)
      case c: Copying =>
        /**
         * compareAndSet�������������ȼ�鵱ǰ�����Ƿ����Ԥ�����ã����ҵ�ǰ��־�Ƿ����Ԥ�ڱ�־�����ȫ����ȣ�
         * ����ԭ�ӷ�ʽ�������ú͸ñ�־��ֵ����Ϊ�����ĸ���ֵ
         */
        if (entry.state.compareAndSet(s0, new Copying(c.n + 1))) true
        else acquire(entry)
    }
  }
/**
 * �߳���ɸ����ļ�������,���̱߳����ͷ�Copying��,ͨ����Ӧ��release������������
 * �÷�������Copying�������߽��ļ�״̬����ΪIdle,�˴�Ҫ����:�����ڱ����Ƶ��ļ��������.
 * �մ�Creating״̬�л���Idle״̬��͵��ø÷���
 */
  @tailrec private def release(entry: Entry): Unit = {
    val s0 = entry.state.get
    s0 match {
      case i: Idle =>
        sys.error("Error - released more times than acquired.")
      case c: Creating =>
        if (!entry.state.compareAndSet(s0, new Idle)) release(entry)
      case c: Copying if c.n <= 0 =>
        sys.error("Error - cannot have 0 or less copies in progress!")
      case c: Copying =>
        val newState = if (c.n == 1) new Idle else new Copying(c.n - 1)
        if (!entry.state.compareAndSet(s0, newState)) release(entry)
      case d: Deleting =>
        sys.error("Error - releasing a file that is being deleted!")
    }
  }
/***
 * �÷�������filesӳ���а�������Ŀ,��ôcopyFile�����ͻ�ͨ������һ���������Ƹ���Ŀ������ļ�
 * 
 */
  def copyFile(src: String, dest: String): Unit = {
    files.get(src) match {
      case None =>
        logMessage(s"File '$src' does not exist.")
      case Some(srcEntry) if srcEntry.isDir =>
        sys.error(s"Path '$src' is a directory!")
      case Some(srcEntry) =>
        execute {
          if (acquire(srcEntry)) try {
            //acquire��ȡ���ļ��ļ�����Ա�ִ�и��Ʋ���,������һ������creating״̬��destEntry��Ŀ
            val destEntry = new Entry(false)            
            destEntry.state.set(new Creating)
            //putIfAbsent ������ָ������������ǰ��ֵ�������û�м�ӳ��None
            /**
             * putIfAbsent�÷�������filesӳ�����Ƿ��д����ļ�·��dest�ļ�
             * ���filesӳ��û��dest��destEntry��ֵ��,�÷�����files��Ӹ���Ŀ            
             * �˿�srcEntry��destEntry��Ŀ��������,
             */
            if (files.putIfAbsent(dest, destEntry) == None) try {
              //�ļ�����
              FileUtils.copyFile(new File(src), new File(dest))
            } finally release(destEntry)//�ͷ�destEntry
          } finally release(srcEntry)//�ͷ�srcEntry
        }
    }
  }

  def filesInDir(dir: String): Iterable[String] = {
    // trie map snapshots
    for ((name, state) <- files; if name.startsWith(dir)) yield name
  }

}