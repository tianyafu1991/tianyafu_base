package com.tianyafu.bigdata.mapreduce.sort.all;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @Author:tianyafu
 * @Date:2020/12/30
 * @Description:
 */
public class Traffic implements WritableComparable<Traffic> {

    private Long up;
    private Long down;
    private Long sum;

    public Traffic() {
    }

    public Traffic(Long up, Long down) {
        this.up = up;
        this.down = down;
        this.sum = up + down;
    }

    @Override
    public int compareTo(Traffic o) {
        return this.getSum() > o.getSum() ? -1 : 1;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(up);
        out.writeLong(down);
        out.writeLong(sum);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.up = in.readLong();
        this.down = in.readLong();
        this.sum = in.readLong();
    }

    @Override
    public String toString() {
        return up + "\t" + down + "\t" + sum;
    }

    public Long getUp() {
        return up;
    }

    public void setUp(Long up) {
        this.up = up;
    }

    public Long getDown() {
        return down;
    }

    public void setDown(Long down) {
        this.down = down;
    }

    public Long getSum() {
        return sum;
    }

    public void setSum(Long sum) {
        this.sum = sum;
    }
}
