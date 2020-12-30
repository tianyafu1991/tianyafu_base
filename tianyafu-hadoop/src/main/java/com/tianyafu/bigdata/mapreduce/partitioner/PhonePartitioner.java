package com.tianyafu.bigdata.mapreduce.partitioner;

import com.tianyafu.bigdata.mapreduce.ser.Access;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * @Author:tianyafu
 * @Date:2020/12/30
 * @Description:
 */
public class PhonePartitioner extends Partitioner<Text, Access> {
    @Override
    public int getPartition(Text text, Access access, int numPartitions) {
        String phone = text.toString();
        if(phone.startsWith("13")){
            return 0;
        }else if(phone.startsWith("15")) {
            return 1;
        }
        return 2;
    }
}
