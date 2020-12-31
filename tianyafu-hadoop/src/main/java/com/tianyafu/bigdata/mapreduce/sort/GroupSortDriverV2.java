package com.tianyafu.bigdata.mapreduce.sort;

import com.tianyafu.utils.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @Author:tianyafu
 * @Date:2020/12/30
 * @Description: 分组TopN
 */
public class GroupSortDriverV2 {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.exit(1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);

        String input = args[0]; //"tianyafu-hadoop/src/main/resources/data/order.txt";
        String out = args[1]; //"tianyafu-hadoop/src/main/resources/out/order";

        FileUtils.delete(conf, out);

        job.setJarByClass(GroupSortDriverV2.class);

        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        job.setMapOutputKeyClass(Order.class);
        job.setMapOutputValueClass(NullWritable.class);
        job.setOutputKeyClass(Order.class);
        job.setOutputValueClass(NullWritable.class);

        // 设置分组
        job.setGroupingComparatorClass(OrderGroupingComparator.class);

        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(out));

        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);


    }

    public static class MyMapper extends Mapper<LongWritable, Text, Order, NullWritable> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] splits = value.toString().split(",");
            Integer id = Integer.parseInt(splits[0].trim());
            Double price = Double.parseDouble(splits[2].trim());
            context.write(new Order(id, price), NullWritable.get());
        }
    }

    public static class MyReducer extends Reducer<Order, NullWritable, Order, NullWritable> {
        int topN = 2;

        @Override
        protected void reduce(Order key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            int index = 1;
            for (NullWritable value : values) {
                index ++;
                context.write(key, NullWritable.get());
                if(index > topN){
                    break;
                }
            }
        }
    }
}
