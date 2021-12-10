package com.tianyafu.tianyafuplatform.domain.cluster;

import javax.persistence.*;

@Entity(name = "platform_yarn_summary")
public class YARNSummary extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    // NM存活节点数量监控
    private Integer numActiveNMs;
    // NM丢失节点数量监控
    private Integer numLostNMs;
    // NM不健康节点数量监控
    private Integer numUnhealthyNMs;
    // RM可用的总内存
    private Integer fairShareMB;
    // RM可用的总虚拟核数
    private Integer fairShareVCores;
    // app提交数量
    private Integer appsSubmitted;
    // app的运行数量
    private Integer appsRunning;
    // app等待数量
    private Integer appsPending;
    // app完成数量
    private Integer appsCompleted;
    // app被kill的数量
    private Integer appsKilled;
    // app失败数量
    private Integer appsFailed;
    // 已分配的内存大小
    private Integer allocatedMB;
    // 已分配的虚拟核数量
    private Integer allocatedVCores;
    // 已分配的Container数量
    private Integer allocatedContainers;
    // 可用的内存大小
    private Integer availableMB;
    // 可用的虚拟核数量
    private Integer availableVCores;
    // 等待分配的内存大小
    private Integer pendingMB;
    // 等待分配的虚拟核数量
    private Integer pendingVCores;
    // 等待分配的Container数量
    private Integer pendingContainers;
    // 预留的内存大小
    private Integer reservedMB;
    // 预留的虚拟核数量
    private Integer reservedVCores;
    // 预留的Container数量
    private Integer reservedContainers;
    // 当前活动的用户数
    private Integer activeUsers;
    // 当前活动的app数
    private Integer activeApplications;
    // 堆内存使用监控
    private Float memHeapUsedM;
    // 线程阻塞数量
    private Integer threadsBlocked;
    // 线程等待数量
    private Integer threadsWaiting;


    public YARNSummary() {
    }

    @Override
    public String toString() {
        return "YARNSummary{" +
                "id=" + id +
                ", numActiveNMs=" + numActiveNMs +
                ", numLostNMs=" + numLostNMs +
                ", numUnhealthyNMs=" + numUnhealthyNMs +
                ", fairShareMB=" + fairShareMB +
                ", fairShareVCores=" + fairShareVCores +
                ", appsSubmitted=" + appsSubmitted +
                ", appsRunning=" + appsRunning +
                ", appsPending=" + appsPending +
                ", appsCompleted=" + appsCompleted +
                ", appsKilled=" + appsKilled +
                ", appsFailed=" + appsFailed +
                ", allocatedMB=" + allocatedMB +
                ", allocatedVCores=" + allocatedVCores +
                ", allocatedContainers=" + allocatedContainers +
                ", availableMB=" + availableMB +
                ", availableVCores=" + availableVCores +
                ", pendingMB=" + pendingMB +
                ", pendingVCores=" + pendingVCores +
                ", pendingContainers=" + pendingContainers +
                ", reservedMB=" + reservedMB +
                ", reservedVCores=" + reservedVCores +
                ", reservedContainers=" + reservedContainers +
                ", activeUsers=" + activeUsers +
                ", activeApplications=" + activeApplications +
                ", memHeapUsedM=" + memHeapUsedM +
                ", threadsBlocked=" + threadsBlocked +
                ", threadsWaiting=" + threadsWaiting +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumActiveNMs() {
        return numActiveNMs;
    }

    public void setNumActiveNMs(Integer numActiveNMs) {
        this.numActiveNMs = numActiveNMs;
    }

    public Integer getNumLostNMs() {
        return numLostNMs;
    }

    public void setNumLostNMs(Integer numLostNMs) {
        this.numLostNMs = numLostNMs;
    }

    public Integer getNumUnhealthyNMs() {
        return numUnhealthyNMs;
    }

    public void setNumUnhealthyNMs(Integer numUnhealthyNMs) {
        this.numUnhealthyNMs = numUnhealthyNMs;
    }

    public Integer getFairShareMB() {
        return fairShareMB;
    }

    public void setFairShareMB(Integer fairShareMB) {
        this.fairShareMB = fairShareMB;
    }

    public Integer getFairShareVCores() {
        return fairShareVCores;
    }

    public void setFairShareVCores(Integer fairShareVCores) {
        this.fairShareVCores = fairShareVCores;
    }

    public Integer getAppsSubmitted() {
        return appsSubmitted;
    }

    public void setAppsSubmitted(Integer appsSubmitted) {
        this.appsSubmitted = appsSubmitted;
    }

    public Integer getAppsRunning() {
        return appsRunning;
    }

    public void setAppsRunning(Integer appsRunning) {
        this.appsRunning = appsRunning;
    }

    public Integer getAppsPending() {
        return appsPending;
    }

    public void setAppsPending(Integer appsPending) {
        this.appsPending = appsPending;
    }

    public Integer getAppsCompleted() {
        return appsCompleted;
    }

    public void setAppsCompleted(Integer appsCompleted) {
        this.appsCompleted = appsCompleted;
    }

    public Integer getAppsKilled() {
        return appsKilled;
    }

    public void setAppsKilled(Integer appsKilled) {
        this.appsKilled = appsKilled;
    }

    public Integer getAppsFailed() {
        return appsFailed;
    }

    public void setAppsFailed(Integer appsFailed) {
        this.appsFailed = appsFailed;
    }

    public Integer getAllocatedMB() {
        return allocatedMB;
    }

    public void setAllocatedMB(Integer allocatedMB) {
        this.allocatedMB = allocatedMB;
    }

    public Integer getAllocatedVCores() {
        return allocatedVCores;
    }

    public void setAllocatedVCores(Integer allocatedVCores) {
        this.allocatedVCores = allocatedVCores;
    }

    public Integer getAllocatedContainers() {
        return allocatedContainers;
    }

    public void setAllocatedContainers(Integer allocatedContainers) {
        this.allocatedContainers = allocatedContainers;
    }

    public Integer getAvailableMB() {
        return availableMB;
    }

    public void setAvailableMB(Integer availableMB) {
        this.availableMB = availableMB;
    }

    public Integer getAvailableVCores() {
        return availableVCores;
    }

    public void setAvailableVCores(Integer availableVCores) {
        this.availableVCores = availableVCores;
    }

    public Integer getPendingMB() {
        return pendingMB;
    }

    public void setPendingMB(Integer pendingMB) {
        this.pendingMB = pendingMB;
    }

    public Integer getPendingVCores() {
        return pendingVCores;
    }

    public void setPendingVCores(Integer pendingVCores) {
        this.pendingVCores = pendingVCores;
    }

    public Integer getPendingContainers() {
        return pendingContainers;
    }

    public void setPendingContainers(Integer pendingContainers) {
        this.pendingContainers = pendingContainers;
    }

    public Integer getReservedMB() {
        return reservedMB;
    }

    public void setReservedMB(Integer reservedMB) {
        this.reservedMB = reservedMB;
    }

    public Integer getReservedVCores() {
        return reservedVCores;
    }

    public void setReservedVCores(Integer reservedVCores) {
        this.reservedVCores = reservedVCores;
    }

    public Integer getReservedContainers() {
        return reservedContainers;
    }

    public void setReservedContainers(Integer reservedContainers) {
        this.reservedContainers = reservedContainers;
    }

    public Integer getActiveUsers() {
        return activeUsers;
    }

    public void setActiveUsers(Integer activeUsers) {
        this.activeUsers = activeUsers;
    }

    public Integer getActiveApplications() {
        return activeApplications;
    }

    public void setActiveApplications(Integer activeApplications) {
        this.activeApplications = activeApplications;
    }

    public Float getMemHeapUsedM() {
        return memHeapUsedM;
    }

    public void setMemHeapUsedM(Float memHeapUsedM) {
        this.memHeapUsedM = memHeapUsedM;
    }

    public Integer getThreadsBlocked() {
        return threadsBlocked;
    }

    public void setThreadsBlocked(Integer threadsBlocked) {
        this.threadsBlocked = threadsBlocked;
    }

    public Integer getThreadsWaiting() {
        return threadsWaiting;
    }

    public void setThreadsWaiting(Integer threadsWaiting) {
        this.threadsWaiting = threadsWaiting;
    }
}
