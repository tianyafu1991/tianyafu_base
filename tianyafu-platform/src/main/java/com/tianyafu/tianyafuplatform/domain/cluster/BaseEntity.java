package com.tianyafu.tianyafuplatform.domain.cluster;

import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseEntity {

    private Boolean isDeleted = false;

    private Long createTime;

    public BaseEntity() {
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "isDeleted=" + isDeleted +
                ", createTime=" + createTime +
                '}';
    }
}
