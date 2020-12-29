package com.tianyafu.bigdata.hadoop;

/**
 * @Author:tianyafu
 * @Date:2020/12/29
 * @Description:
 */
public interface TianyaMapper {

    void map(String line, TianyaContext context);
}
