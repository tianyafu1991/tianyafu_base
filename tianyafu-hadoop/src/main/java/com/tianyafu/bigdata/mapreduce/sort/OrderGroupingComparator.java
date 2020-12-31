package com.tianyafu.bigdata.mapreduce.sort;

import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/**
 * @Author:tianyafu
 * @Date:2020/12/31
 * @Description:
 */
public class OrderGroupingComparator extends WritableComparator {

    public OrderGroupingComparator() {
        super(Order.class, true);
    }

    @Override
    public int compare(WritableComparable a, WritableComparable b) {
        Order order1 = (Order) a;
        Order order2 = (Order) b;

        int result;

        if (order1.getId() > order2.getId()) {
            result = 1;
        } else if (order1.getId() < order2.getId()) {
            result = -1;
        } else {
            result = 0;
        }

        return result;
    }
}
