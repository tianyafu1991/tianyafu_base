package com.tianyafu.scala

import com.huaban.analysis.jieba.{JiebaSegmenter, WordDictionary}

import java.io.File
import java.nio.file.{Path, Paths}

object JieBaScala {

  def main(args: Array[String]): Unit = {
    val sentences = "要投诉稠城街道市场监管所工作人员渎职，2020年10月19号，在稠城街道赵宅路56号二楼张迪二手科技，通过抖音支付了2980元购买了一个二手苹果手机，当时说没有维修过的，寄到以后发现手机不能人脸识别，存在欺骗消费者，来电人就到市场监管所反映，第一次工作人员表示没有找到商家，等第二次来电人又投诉的时候才说找到了商家，并且强制要求来电人接受调解，退回了2680元，还有300元没有退回，来电人表示要投诉市场监管所工作人员第一次没有仔细去找，工作不认真，要求处罚该名工作人员，并且退回剩余的300元，望处理"
    val sentences2 = "北京京天威科技发展有限公司大庆车务段的装车数量"
    val dictionary: WordDictionary = WordDictionary.getInstance()

    val path1 = Paths.get(new File("data/city_village_name.txt").getCanonicalPath())
    val path2 = Paths.get(new File("data/phy.txt").getCanonicalPath())
    dictionary.loadUserDict(path1)
    dictionary.loadUserDict(path2)
    val segmenter  = new JiebaSegmenter
    // [北京, 京, 天威, 科技, 发展, 有限公司, 大庆, 车务段, 的, 装车, 数量]
    println(segmenter.sentenceProcess(sentences))
    println(segmenter.sentenceProcess(sentences2))
  }

}
