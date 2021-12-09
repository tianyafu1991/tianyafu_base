package com.tianyafu.tianyafuplatform.controller.cluster;

import cn.hutool.core.date.DateUtil;
import com.tianyafu.tianyafuplatform.domain.cluster.HDFSSummary;
import com.tianyafu.tianyafuplatform.exception.ErrorCodes;
import com.tianyafu.tianyafuplatform.exception.JsonData;
import com.tianyafu.tianyafuplatform.service.cluster.MetricsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;

@Api(tags = "平台监控指标Controller")
@RestController
@RequestMapping("/metrics")
public class MetricsController {

    @Resource
    private MetricsService metricsService;


    @ApiOperation("根据当天最新的HDFS监控指标信息")
//    @ApiImplicitParam(name = "id", value = "用户id", defaultValue = "99", required = true)
    @GetMapping("/getHDFSSummary")
    public Object getHDFSSummary(){
        HDFSSummary hdfsSummary = metricsService.findHDFSSummary(Long.parseLong(DateUtil.toIntSecond(new Date()) + ""));
        return JsonData.buildSuccess(hdfsSummary, ErrorCodes.SYSTEM_SUCCESS);
    }
}
