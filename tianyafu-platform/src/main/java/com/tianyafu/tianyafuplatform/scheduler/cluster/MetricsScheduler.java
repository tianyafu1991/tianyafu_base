package com.tianyafu.tianyafuplatform.scheduler.cluster;

import cn.hutool.core.date.DateUtil;
import com.tianyafu.tianyafuplatform.domain.cluster.HDFSSummary;
import com.tianyafu.tianyafuplatform.domain.cluster.HadoopMetrics;
import com.tianyafu.tianyafuplatform.domain.cluster.YARNSummary;
import com.tianyafu.tianyafuplatform.service.cluster.MetricsService;
import com.tianyafu.tianyafuplatform.utils.HttpClientUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
@EnableScheduling
public class MetricsScheduler {

    private Logger logger = LoggerFactory.getLogger(MetricsScheduler.class);

    public static final FastDateFormat FORMATTER = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    private HttpClientUtils client= new HttpClientUtils(null);

    // NameNode相关的Bean Name
    public static final String NAMENODE_INFO = "Hadoop:service=NameNode,name=NameNodeInfo";
    public static final String FS_NAME_SYSTEM_STATE = "Hadoop:service=NameNode,name=FSNamesystemState";

    // ResourceManager相关的Bean Name
    public static final String CLUSTER_METRICS = "Hadoop:service=ResourceManager,name=ClusterMetrics";
    public static final String QUEUE_METRICS = "Hadoop:service=ResourceManager,name=QueueMetrics,q0=root";
    public static final String JVM_METRICS = "Hadoop:service=ResourceManager,name=JvmMetrics";

    public static final String METRICS_URI_FORMATTER = "http://%s/jmx?qry=%s";

    @Value("${hwinfo.hadoop.nn.uri}")
    private String nameNodeUri;
    @Value("${hwinfo.hadoop.rm.uri}")
    private String resourceManagerUri;


    @Resource
    private MetricsService metricsService;



    @Scheduled(fixedRate = 5000)
    public void testScheduler(){
//        logger.info("执行调度的时间为:" + FORMATTER.format(new Date()));
//        logger.info("nameNode uri:" + nameNodeUri + " | resourceManager uri:" + resourceManagerUri);
//        collectHDFSMetrics();
        collectYarnMetrics();
    }

    /**
     * 收集YARN监控指标
     */
    public void collectYarnMetrics(){
        YARNSummary yarnSummary = new YARNSummary();
        String clusterMetricsUrl = String.format(METRICS_URI_FORMATTER,resourceManagerUri,CLUSTER_METRICS);
        String queueMetricsUrl = String.format(METRICS_URI_FORMATTER,resourceManagerUri,QUEUE_METRICS);
        String jvmMetricsUrl = String.format(METRICS_URI_FORMATTER,resourceManagerUri,JVM_METRICS);
        try {
            HadoopMetrics clusterMetrics = client.get(HadoopMetrics.class, clusterMetricsUrl, null, null);
            HadoopMetrics queueMetrics = client.get(HadoopMetrics.class, queueMetricsUrl, null, null);
            HadoopMetrics jvmMetrics = client.get(HadoopMetrics.class, jvmMetricsUrl, null, null);
            yarnSummary.setNumActiveNMs(Integer.parseInt(clusterMetrics.getMetricsValue("NumActiveNMs").toString()));
            yarnSummary.setNumLostNMs(Integer.parseInt(clusterMetrics.getMetricsValue("NumLostNMs").toString()));
            yarnSummary.setNumUnhealthyNMs(Integer.parseInt(clusterMetrics.getMetricsValue("NumUnhealthyNMs").toString()));
            yarnSummary.setFairShareMB(Integer.parseInt(queueMetrics.getMetricsValue("FairShareMB").toString()));
            yarnSummary.setFairShareVCores(Integer.parseInt(queueMetrics.getMetricsValue("FairShareVCores").toString()));
            yarnSummary.setAppsSubmitted(Integer.parseInt(queueMetrics.getMetricsValue("AppsSubmitted").toString()));
            yarnSummary.setAppsRunning(Integer.parseInt(queueMetrics.getMetricsValue("AppsRunning").toString()));
            yarnSummary.setAppsPending(Integer.parseInt(queueMetrics.getMetricsValue("AppsPending").toString()));
            yarnSummary.setAppsCompleted(Integer.parseInt(queueMetrics.getMetricsValue("AppsCompleted").toString()));
            yarnSummary.setAppsKilled(Integer.parseInt(queueMetrics.getMetricsValue("AppsKilled").toString()));
            yarnSummary.setAppsFailed(Integer.parseInt(queueMetrics.getMetricsValue("AppsFailed").toString()));
            yarnSummary.setAllocatedMB(Integer.parseInt(queueMetrics.getMetricsValue("AllocatedMB").toString()));
            yarnSummary.setAllocatedVCores(Integer.parseInt(queueMetrics.getMetricsValue("AllocatedVCores").toString()));
            yarnSummary.setAllocatedContainers(Integer.parseInt(queueMetrics.getMetricsValue("AllocatedContainers").toString()));
            yarnSummary.setAvailableMB(Integer.parseInt(queueMetrics.getMetricsValue("AvailableMB").toString()));
            yarnSummary.setAvailableVCores(Integer.parseInt(queueMetrics.getMetricsValue("AvailableVCores").toString()));
            yarnSummary.setPendingMB(Integer.parseInt(queueMetrics.getMetricsValue("PendingMB").toString()));
            yarnSummary.setPendingVCores(Integer.parseInt(queueMetrics.getMetricsValue("PendingVCores").toString()));
            yarnSummary.setPendingContainers(Integer.parseInt(queueMetrics.getMetricsValue("PendingContainers").toString()));
            yarnSummary.setReservedMB(Integer.parseInt(queueMetrics.getMetricsValue("ReservedMB").toString()));
            yarnSummary.setReservedVCores(Integer.parseInt(queueMetrics.getMetricsValue("ReservedVCores").toString()));
            yarnSummary.setReservedContainers(Integer.parseInt(queueMetrics.getMetricsValue("ReservedContainers").toString()));
            yarnSummary.setActiveUsers(Integer.parseInt(queueMetrics.getMetricsValue("ActiveUsers").toString()));
            yarnSummary.setActiveApplications(Integer.parseInt(queueMetrics.getMetricsValue("ActiveApplications").toString()));
            yarnSummary.setMemHeapUsedM(Float.parseFloat(jvmMetrics.getMetricsValue("MemHeapUsedM").toString()));
            yarnSummary.setThreadsBlocked(Integer.parseInt(jvmMetrics.getMetricsValue("ThreadsBlocked").toString()));
            yarnSummary.setThreadsWaiting(Integer.parseInt(jvmMetrics.getMetricsValue("ThreadsWaiting").toString()));
            yarnSummary.setDeleted(false);
            yarnSummary.setCreateTime(DateUtil.currentSeconds());
            logger.info(yarnSummary.toString());
            metricsService.addYARNSummary(yarnSummary);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * 收集HDFS监控指标
     */
    public void collectHDFSMetrics(){
        HDFSSummary hdfsSummary = new HDFSSummary();
        String nameNodeUrl = String.format(METRICS_URI_FORMATTER,nameNodeUri,NAMENODE_INFO);
        String fsNameSystemStateUrl = String.format(METRICS_URI_FORMATTER,nameNodeUri,FS_NAME_SYSTEM_STATE);
        try {
            HadoopMetrics nameNodeInfoMetrics = client.get(HadoopMetrics.class, nameNodeUrl, null, null);
            HadoopMetrics fsNameSystemStateMetrics = client.get(HadoopMetrics.class,fsNameSystemStateUrl , null, null);
            hdfsSummary.setTotal(Long.parseLong(nameNodeInfoMetrics.getMetricsValue("Total").toString()));
            hdfsSummary.setDfsUsed(Long.parseLong(nameNodeInfoMetrics.getMetricsValue("Used").toString()));
            hdfsSummary.setPercentUsed(Float.parseFloat(nameNodeInfoMetrics.getMetricsValue("PercentUsed").toString()));
            hdfsSummary.setDfsFree(Long.parseLong(nameNodeInfoMetrics.getMetricsValue("Free").toString()));
            hdfsSummary.setNonDfsUsed(Long.parseLong(nameNodeInfoMetrics.getMetricsValue("NonDfsUsedSpace").toString()));
            hdfsSummary.setTotalFiles(Long.parseLong(nameNodeInfoMetrics.getMetricsValue("TotalFiles").toString()));
            hdfsSummary.setTotalBlocks(Long.parseLong(nameNodeInfoMetrics.getMetricsValue("TotalBlocks").toString()));
            hdfsSummary.setMissingBlocks(Long.parseLong(nameNodeInfoMetrics.getMetricsValue("NumberOfMissingBlocks").toString()));
            hdfsSummary.setBlockPoolUsedSpace(Long.parseLong(nameNodeInfoMetrics.getMetricsValue("BlockPoolUsedSpace").toString()));
            hdfsSummary.setLiveDataNodeNums(Integer.parseInt(fsNameSystemStateMetrics.getMetricsValue("NumLiveDataNodes").toString()));
            hdfsSummary.setDeadDataNodeNums(Integer.parseInt(fsNameSystemStateMetrics.getMetricsValue("NumDeadDataNodes").toString()));
            hdfsSummary.setVolumeFailuresTotal(Long.parseLong(fsNameSystemStateMetrics.getMetricsValue("VolumeFailuresTotal").toString()));
            hdfsSummary.setDeleted(false);
            hdfsSummary.setCreateTime(DateUtil.currentSeconds());
            logger.info(hdfsSummary.toString());
            metricsService.addHDFSSummary(hdfsSummary);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }






}
