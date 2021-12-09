package com.tianyafu.tianyafuplatform.service.cluster.impl;

import com.tianyafu.tianyafuplatform.domain.cluster.HDFSSummary;
import com.tianyafu.tianyafuplatform.repository.cluster.HDFSSummaryRepository;
import com.tianyafu.tianyafuplatform.service.cluster.MetricsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class MetricsServiceImpl implements MetricsService {

    @Resource
    private HDFSSummaryRepository hdfsSummaryRepository;


    /**
     * 添加HDFSSummary
     * @param hdfsSummary
     */
    @Override
    public void addHDFSSummary(HDFSSummary hdfsSummary) {
        hdfsSummaryRepository.save(hdfsSummary);
    }

    /**
     * 根据时间找HDFSSummary
     * @param time
     * @return
     */
    @Override
    public HDFSSummary findHDFSSummary(Long time) {
        return hdfsSummaryRepository.findTop1ByIsDeletedFalseAndCreateTimeLessThanEqualOrderByCreateTimeDesc(time);
    }

    /**
     * 根据开始时间和结束时间找区间内的HDFSSummary
     * @param start
     * @param end
     * @return
     */
    @Override
    public List<HDFSSummary> findHDFSSummaries(Long start, Long end) {
        return hdfsSummaryRepository.findByIsDeletedFalseAndCreateTimeBetweenOrderByCreateTimeAsc(start,end);
    }
}
