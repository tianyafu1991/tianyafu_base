package com.tianyafu.spark.template

import scala.reflect.runtime.{universe => ru}
import com.tianyafu.spark.utils.ContextUtils
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

/**
 * @Author:tianyafu
 * @Date:2021/3/15
 * @Description:
 */
trait ETLTemplate {

  def setup(): Unit = {
    println("set up...................")
    val conf = new SparkConf()
    conf.set("spark.app.name", conf.get("spark.app.name", getClass.getSimpleName))
    conf.set("spark.master", conf.get("spark.master", "local[2]"))
    // 主要是为了本地连接Hive  服务器上不需要该参数
    conf.set("hive.metastore.uris", "thrift://192.168.101.223:9083")
    val spark: SparkSession = ContextUtils.getSparkSessionForSupportHive(conf)
    ContextUtils.set(spark)
  }

  def etl()

  def cleanup(): Unit = {
    println("clean up...................")
    ContextUtils.remove()
  }
}

/**
 * ETL的主类
 */
object ETLTemplateApp extends App {

  val etlMethod: String = "etl"
  val setupMethod: String = "setup"
  val cleanupMethod: String = "cleanup"

  /**
   * 总结：
   * 从以下过程中可以得出，编程套路就是通过一个symbol获取一个mirror
   *
   * 1.通过类的全限定名，使用类加载器的mirror获取 class symbol
   * 2.根据class symbol 获取 class type和 class mirror
   * 3.通过class type 获取到 构造器的method symbol
   * 4.使用 class mirror 通过 构造器的method symbol 获取 构造器的 method mirror
   * 5.通过class type 获取到 etl方法的method symbol
   * 6.使用构造器的 method mirror用类加载器获取类的实例镜像
   * 7.使用实例镜像通过etl的method symbol获取etl的method mirror
   * 8.调用etl方法
   *
   * @param args
   */
  //验证参数个数
  if (args.length != 1) {
    System.out.println("| Usage: the mainClass is necessary。。。。。。")
    System.exit(-1)
  }
  // 开启该参数可统计并打印出该应用的执行时间
  util.Properties.setProp("scala.time", "true")
  //该参数外部传入
  //val classFullName = "com.hwinfo.jhd.warning.missing.DmWarningMissFilterListDDI"
  val classFullName = args(0)
  //获取类相关信息
  val classInfo: (ru.Mirror, ru.Type, ru.MethodMirror) = getClassInfo(classFullName)
  // 模板模式
  try {
    // 执行setup方法
    invoke(classInfo, setupMethod)
    // 执行etl方法
    invoke(classInfo, etlMethod)
  } catch {
    case e: Exception => {
      e.printStackTrace()
      throw new RuntimeException(e.getMessage)
    }
  } finally {
    // 执行cleanup方法
    invoke(classInfo, cleanupMethod)
  }

  /**
   *
   * @param classInfo  类的一些对象
   * @param methodName 要执行的方法名
   */
  @throws(classOf[Exception])
  def invoke(classInfo: (ru.Mirror, ru.Type, ru.MethodMirror), methodName: String): Unit = {
    //通过class Type获取method Symbol 先从子类中去找 如果子类中重写了父类的方法 就能找到 如果没有重写 就从父trait ETLTemplate中获取方法
    val methodSymbol: ru.MethodSymbol = if (classInfo._2.decl(ru.TermName(methodName)).isMethod)
      classInfo._2.decl(ru.TermName(methodName)).asMethod else ru.typeOf[ETLTemplate].decl(ru.TermName(methodName)).asMethod
    //通过class constructor获取instance Mirror
    val im: ru.InstanceMirror = classInfo._1.reflect(classInfo._3())
    ////使用instance Mirror通过实例镜像获取method Mirror
    val mm: ru.MethodMirror = im.reflectMethod(methodSymbol)
    //调用方法
    mm()
  }

  /**
   * 根据类的全限定名获取类的相关信息对象
   *
   * @param classFullName
   * @return (类加载器镜像,类的Type类型,类的构造器对象)
   */
  @throws(classOf[Exception])
  def getClassInfo(classFullName: String): (ru.Mirror, ru.Type, ru.MethodMirror) = {
    //获取类加载器镜像
    val m: ru.Mirror = ru.runtimeMirror(getClass.getClassLoader)
    //类加载器通过类的全限定名获取class Symbol
    val classSymbol: ru.ClassSymbol = m.staticClass(classFullName)
    //通过class Symbol 获取class Type
    val classType: ru.Type = classSymbol.toType
    //通过class Symbol获取class Mirror
    val cm: ru.ClassMirror = m.reflectClass(classSymbol)
    //通过class Type获取class constructor Symbol
    val ctor: ru.MethodSymbol = classType.decl(ru.termNames.CONSTRUCTOR).asMethod
    //使用class Mirror通过class constructor Symbol 获取class constructor
    val ctorm: ru.MethodMirror = cm.reflectConstructor(ctor)
    (m, classType, ctorm)
  }

}
