package com.tianyafu.scala

import java.io.File

object RmRemoteRepositoriesFile {


  /**
   * 递归删除_remote.repositories文件
   * @param file
   */
  def rmRemoteRepositories(file: File): Unit = {
    val subFiles = file.listFiles()
    subFiles.foreach(f => {
      if (f.isDirectory) {
        rmRemoteRepositories(f)
      } else {
        if (f.getName.contains("_remote")) {
          println(s"删除${f.getAbsolutePath}")
          f.delete()
        }
      }
    })
  }


  def main(args: Array[String]): Unit = {
    val path = args(0)
    val file = new File(path)
    rmRemoteRepositories(file)
    println("完成了")
  }

}
