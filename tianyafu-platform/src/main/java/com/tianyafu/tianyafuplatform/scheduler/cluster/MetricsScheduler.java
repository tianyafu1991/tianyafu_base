package com.tianyafu.tianyafuplatform.scheduler.cluster;

import cn.hutool.core.date.DateUtil;
import com.tianyafu.tianyafuplatform.domain.cluster.HDFSSummary;
import com.tianyafu.tianyafuplatform.domain.cluster.HadoopMetrics;
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
import java.util.Date;

@Component
@EnableScheduling
public class MetricsScheduler {

    private Logger logger = LoggerFactory.getLogger(MetricsScheduler.class);

    public static final FastDateFormat FORMATTER = FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

    private HttpClientUtils client= new HttpClientUtils(null);

    public static final String NAMENODE_INFO = "Hadoop:service=NameNode,name=NameNodeInfo";
    public static final String FS_NAME_SYSTEM_STATE = "Hadoop:service=NameNode,name=FSNamesystemState";

    public static final String NAMENODE_URI_FORMATTER = "http://%s/jmx?qry=%s";

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
        collectHDFSMetrics();
    }

    public void collectHDFSMetrics(){
        HDFSSummary hdfsSummary = new HDFSSummary();
        String nameNodeUrl = String.format(NAMENODE_URI_FORMATTER,nameNodeUri,NAMENODE_INFO);

        String fsNameSystemStateUrl = String.format(NAMENODE_URI_FORMATTER,nameNodeUri,FS_NAME_SYSTEM_STATE);
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
