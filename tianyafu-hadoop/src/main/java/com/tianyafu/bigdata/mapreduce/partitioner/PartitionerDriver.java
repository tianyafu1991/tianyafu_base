package com.tianyafu.bigdata.mapreduce.partitioner;

import com.tianyafu.bigdata.mapreduce.ser.Access;
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
 * @Description:
 */
public class PartitionerDriver {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.exit(1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);

        String input = args[0]; //"tianyafu-hadoop/src/main/resources/data/access.log";
        String out = args[1]; //"tianyafu-hadoop/src/main/resources/out/access";

        FileUtils.delete(conf, out);

        job.setJarByClass(PartitionerDriver.class);

        //设置分区器 并 设置reduce的个数
        job.setPartitionerClass(PhonePartitioner.class);
        job.setNumReduceTasks(3);

        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Access.class);
        job.setOutputKeyClass(NullWritable.class);
        job.setOutputValueClass(Access.class);

        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(out));

        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);


    }

    public static class MyMapper extends Mapper<LongWritable, Text, Text, Access> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] splits = value.toString().split("\t");
            String phone = splits[1];
            long up = Long.parseLong(splits[splits.length - 3]);
            long down = Long.parseLong(splits[splits.length - 2]);
            context.write(new Text(phone), new Access(phone, up, down));
        }
    }

    public static class MyReducer extends Reducer<Text, Access, NullWritable, Access> {
        @Override
        protected void reduce(Text key, Iterable<Access> values, Context context) throws IOException, InterruptedException {
            Long allUp = 0L;
            Long allDown = 0L;
            for (Access access : values) {
                allUp += access.getUp();
                allDown += access.getDown();
            }
            context.write(NullWritable.get(), new Access(key.toString(), allUp, allDown));
        }
    }
}
