package com.tianyafu.tianyafuplatform.service.cluster;

import com.tianyafu.tianyafuplatform.domain.cluster.HDFSSummary;
import com.tianyafu.tianyafuplatform.domain.cluster.YARNSummary;

import java.util.List;

public interface MetricsService {

    void addHDFSSummary(HDFSSummary hdfsSummary);

    HDFSSummary findHDFSSummary(Long time);

    List<HDFSSummary> findHDFSSummaries(Long start,Long end);

    void addYARNSummary(YARNSummary yarnSummary);
}
