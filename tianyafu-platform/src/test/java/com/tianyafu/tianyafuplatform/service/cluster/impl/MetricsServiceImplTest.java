package com.tianyafu.tianyafuplatform.service.cluster.impl;

import com.tianyafu.tianyafuplatform.domain.cluster.HDFSSummary;
import com.tianyafu.tianyafuplatform.service.cluster.MetricsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

import java.util.List;

import static org.junit.Assert.*;
@SpringBootTest
@RunWith(value = SpringRunner.class)
public class MetricsServiceImplTest {

    @Resource
    private MetricsService metricsService;

    @Test
    public void addHDFSSummary() {
        metricsService.addHDFSSummary(null);
    }

    @Test
    public void findHDFSSummary() {
        HDFSSummary hdfsSummary = metricsService.findHDFSSummary(1639040435L);
        System.out.println(hdfsSummary);
    }

    @Test
    public void findHDFSSummaries() {
        List<HDFSSummary> hdfsSummaries = metricsService.findHDFSSummaries(1639040412L, 1639040452L);
        for (HDFSSummary hdfsSummary : hdfsSummaries) {
            System.out.println(hdfsSummary);
        }
    }
}