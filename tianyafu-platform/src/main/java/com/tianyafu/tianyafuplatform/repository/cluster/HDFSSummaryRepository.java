package com.tianyafu.tianyafuplatform.repository.cluster;

import com.tianyafu.tianyafuplatform.domain.cluster.HDFSSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HDFSSummaryRepository extends JpaRepository<HDFSSummary,Long> {

    HDFSSummary findTop1ByIsDeletedFalseAndCreateTimeLessThanEqualOrderByCreateTimeDesc(Long time);

    List<HDFSSummary> findByIsDeletedFalseAndCreateTimeBetweenOrderByCreateTimeAsc(Long start,Long end);
}
