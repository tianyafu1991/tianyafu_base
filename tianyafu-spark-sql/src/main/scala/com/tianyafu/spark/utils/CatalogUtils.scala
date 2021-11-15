package com.tianyafu.spark.utils

import com.tianyafu.spark.constants.Constants
import org.apache.spark.SparkConf
import org.apache.spark.internal.Logging
import org.apache.spark.sql.{SparkSession}
import org.apache.spark.sql.catalyst.TableIdentifier
import org.apache.spark.sql.catalyst.catalog.CatalogTypes.TablePartitionSpec
import org.apache.spark.sql.catalyst.catalog.{ CatalogTablePartition, SessionCatalog}

object CatalogUtils extends Logging {


  def main(args: Array[String]): Unit = {
    val conf = new SparkConf()
    conf.setAppName(getClass.getSimpleName).setMaster("local[1]").set("hive.metastore.uris", "thrift://cdh-master:9083")
    val spark: SparkSession = ContextUtils.getSparkSessionForSupportHive(conf)

    val hiveDbName: String = conf.get(Constants.SPARK_HIVE_DATABASE, "jhd_dw")
    val reservePartitionNum: Int = conf.get("spark.hive.partition.reserve.num", "3").toInt

    val needCleanTables: String = conf.get("spark.hive.partition.clean.table.names", "dm_pu_dd_f,ods_cjb_dd_f,ods_edu_grade_all_yy_f,ods_ly_xxbz_gxxs_cjzl_mm_f,ods_m_rec_consume_dd_f,ods_pu_dd_f,ods_pu_event_signs_dd_f,ods_sg_m_id_record_out_dd_i,ods_t_ts_jyls_dd_f,ods_t_ts_mm_f,ods_v_groupinfo_dd_i,ods_xsdjksb_dd_f")

    handleDropPartitionsForSpecifiedTablesInSpecifiedDatabase(spark, hiveDbName, reservePartitionNum,needCleanTables)

    spark.stop()
  }

  /**
   * 根据传入的需要清理的表名(用英文状态下的逗号分隔),在指定的库中 删除每张表的多余的分区，只保留reservePartitionNum个分区
   *
   * @param spark
   * @param hiveDbName
   * @param reservePartitionNum
   * @param needCleanTables
   * @throws
   */
  @throws[Exception]
  def handleDropPartitionsForSpecifiedTablesInSpecifiedDatabase(spark: SparkSession, hiveDbName: String, reservePartitionNum: Int, needCleanTables: String): Unit = {
    val catalogSession: SessionCatalog = spark.sessionState.catalog
    val tableIdentifiers: List[TableIdentifier] = needCleanTables.split(",").map(x => TableIdentifier(x, Some(hiveDbName))).toList
    handleDropPartitionsByTableIdentifiers(catalogSession, tableIdentifiers, reservePartitionNum)
  }

  /**
   *
   * 根据指定库名 获取库中所有表的分区信息 保留表中需要保留的分区数 删除多余的分区
   *
   * @param spark               sparkSession
   * @param hiveDbName          指定的Hive库名
   * @param reservePartitionNum 每张表要保留的分区数
   * @throws
   */
  @throws[Exception]
  def handleDropPartitionsForAllTablesInSpecifiedDatabase(spark: SparkSession, hiveDbName: String, reservePartitionNum: Int): Unit = {
    val catalogSession: SessionCatalog = spark.sessionState.catalog
    val tableIdentifiers: Seq[TableIdentifier] = catalogSession.listTables(hiveDbName)
    handleDropPartitionsByTableIdentifiers(catalogSession, tableIdentifiers, reservePartitionNum)
  }


  /**
   * 根据传入的tableIdentifiers 保留表中需要保留的分区数 删除多余的分区
   *
   * @param catalogSession      sparkSession中的 SessionState 的catalog
   * @param tableIdentifiers    表的信息
   * @param reservePartitionNum 需要保留的分区数
   * @throws
   */
  @throws[Exception]
  def handleDropPartitionsByTableIdentifiers(catalogSession: SessionCatalog, tableIdentifiers: Seq[TableIdentifier], reservePartitionNum: Int): Unit = {
    tableIdentifiers.foreach(tableIdentifier => {
      val partitions: Seq[CatalogTablePartition] = catalogSession.listPartitionsByFilter(tableIdentifier, Seq())
      val specs: List[TablePartitionSpec] = partitions.map(x => x.spec).toList

      if (specs.length > reservePartitionNum) {
        val needDropTablePartitionSpecs: List[TablePartitionSpec] = specs.take(specs.length - reservePartitionNum)
        println(s"需要删除的分区数为:${needDropTablePartitionSpecs.length}")
        catalogSession.dropPartitions(tableIdentifier, needDropTablePartitionSpecs, true, false, false)
      }
    })
  }


}
