# HBase基础


## Column Family
```
1.不相同的cf下的列是在不同的数据文件中的  所以最优方式是将相同维度的列 相似属性的列设计在同一个cf中
因为一般请求时会查询相同维度的列

2.cf个数一般不超过3个 如果1个满足了 就设计1个

3.一个cf就是一个store  包含了1个menoryStore和0个或1个或多个StoreFile
```

## Region
```shell
逻辑层面来说 是表的横向切分 表按照rowKey的范围切分为不同的region
物理层面来说 所有数据都是存放在region中 region由 RegionServer管理

建表时默认只有一个region 如果指定了split key 就会有多个region
如果没有指定split key 则当数据量达到一个的阈值 表就会水平切分为2个region 可以把region当做子表 当region的行数
                    达到阈值 则region也会自动切分 不同的region会被HMaster分配给适当的RegionServer管理
```

## RegionServer
```shell
一个RS管理一个或多个region  一个region管理一个或多个cf
```


