package com.tianyafu.tianyafuplatform.domain.cluster;


import javax.persistence.*;

/**
 * HDFS常用指标监控
 * 取自http://mdw:50070/页面
 * 通过调用http://mdw:50070/jmx可以得到所有暴露出来的指标
 */
@Entity(name = "platform_hdfs_summary")
public class HDFSSummary extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // 集群hdfs总容量大小
    private Long total;
    // 集群hdfs已使用的容量大小
    private Long dfsUsed;
    // 集群hdfs使用百分比
    private Float percentUsed;
    // 集群hdfs剩余的容量大小
    private Long dfsFree;
    // 集群非hdfs已使用的容量大小
    private Long nonDfsUsed;
    // 总的文件数量
    private Long totalFiles;
    // 总的block数量
    private Long totalBlocks;
    // 丢失的block数量
    private Long missingBlocks;
    // 集群该namespace的hdfs使用容量大小
    private Long blockPoolUsedSpace;
    // 存活的DN数量
    private Integer liveDataNodeNums;
    // 丢失的DN数量
    private Integer deadDataNodeNums;
    // 坏盘的数量
    private Long volumeFailuresTotal;


    public HDFSSummary() {
    }

    @Override
    public String toString() {
        return "HDFSSummary{" +
                "id=" + id +
                ", total=" + total +
                ", dfsUsed=" + dfsUsed +
                ", percentUsed=" + percentUsed +
                ", dfsFree=" + dfsFree +
                ", nonDfsUsed=" + nonDfsUsed +
                ", totalFiles=" + totalFiles +
                ", totalBlocks=" + totalBlocks +
                ", missingBlocks=" + missingBlocks +
                ", blockPoolUsedSpace=" + blockPoolUsedSpace +
                ", liveDataNodeNums=" + liveDataNodeNums +
                ", deadDataNodeNums=" + deadDataNodeNums +
                ", volumeFailuresTotal=" + volumeFailuresTotal +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public Long getDfsUsed() {
        return dfsUsed;
    }

    public void setDfsUsed(Long dfsUsed) {
        this.dfsUsed = dfsUsed;
    }

    public Float getPercentUsed() {
        return percentUsed;
    }

    public void setPercentUsed(Float percentUsed) {
        this.percentUsed = percentUsed;
    }

    public Long getDfsFree() {
        return dfsFree;
    }

    public void setDfsFree(Long dfsFree) {
        this.dfsFree = dfsFree;
    }

    public Long getNonDfsUsed() {
        return nonDfsUsed;
    }

    public void setNonDfsUsed(Long nonDfsUsed) {
        this.nonDfsUsed = nonDfsUsed;
    }

    public Long getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(Long totalFiles) {
        this.totalFiles = totalFiles;
    }

    public Long getTotalBlocks() {
        return totalBlocks;
    }

    public void setTotalBlocks(Long totalBlocks) {
        this.totalBlocks = totalBlocks;
    }

    public Long getMissingBlocks() {
        return missingBlocks;
    }

    public void setMissingBlocks(Long missingBlocks) {
        this.missingBlocks = missingBlocks;
    }

    public Integer getLiveDataNodeNums() {
        return liveDataNodeNums;
    }

    public void setLiveDataNodeNums(Integer liveDataNodeNums) {
        this.liveDataNodeNums = liveDataNodeNums;
    }

    public Integer getDeadDataNodeNums() {
        return deadDataNodeNums;
    }

    public void setDeadDataNodeNums(Integer deadDataNodeNums) {
        this.deadDataNodeNums = deadDataNodeNums;
    }

    public Long getVolumeFailuresTotal() {
        return volumeFailuresTotal;
    }

    public Long getBlockPoolUsedSpace() {
        return blockPoolUsedSpace;
    }

    public void setBlockPoolUsedSpace(Long blockPoolUsedSpace) {
        this.blockPoolUsedSpace = blockPoolUsedSpace;
    }

    public void setVolumeFailuresTotal(Long volumeFailuresTotal) {


        this.volumeFailuresTotal = volumeFailuresTotal;
    }

}
