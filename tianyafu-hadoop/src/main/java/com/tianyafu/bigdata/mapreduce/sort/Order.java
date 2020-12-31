package com.tianyafu.bigdata.mapreduce.sort;

import org.apache.hadoop.io.WritableComparable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @Author:tianyafu
 * @Date:2020/12/31
 * @Description:
 */
public class Order implements WritableComparable<Order> {

    private Integer id;

    private Double price;

    @Override
    public int compareTo(Order o) {
        int result;
        if(this.getId() > o.getId()){
            result = 1;
        }else if (this.getId() < o.getId()) {
            result = -1;
        }else {
            if(this.getPrice() > o.getPrice() ){
                result = -1;
            }else if(this.getPrice() < o.getPrice()) {
                result = 1;
            }else {
                result = 0;
            }
        }
        return result;
    }

    public Order() {
    }

    @Override
    public String toString() {
        return id + "\t" + price;
    }

    public Order(Integer id, Double price) {
        this.id = id;
        this.price = price;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(id);
        out.writeDouble(price);
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        this.id = in.readInt();
        this.price = in.readDouble();
    }
}
