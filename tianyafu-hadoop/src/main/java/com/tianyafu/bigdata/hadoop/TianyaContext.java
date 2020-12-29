package com.tianyafu.bigdata.hadoop;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author:tianyafu
 * @Date:2020/12/29
 * @Description: 自定义上下文
 */
public class TianyaContext {

    //定义一个缓存Map
    private Map<Object, Object> cacheMap = new HashMap<>();

    // 获取Map
    public Map<Object, Object> getCacheMap() {
        return cacheMap;
    }
    // 写入到上下文中
    public void write(Object key, Object value) {
        cacheMap.put(key, value);
    }
    //从上下文中获取
    public Object get(Object key) {
        return cacheMap.get(key);
    }
}
