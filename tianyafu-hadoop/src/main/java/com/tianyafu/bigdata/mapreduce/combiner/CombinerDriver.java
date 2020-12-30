package com.tianyafu.bigdata.mapreduce.combiner;

import com.tianyafu.utils.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @Author:tianyafu
 * @Date:2020/12/29
 * @Description: 八股文编程
 */
public class CombinerDriver {

    public static void main(String[] args) throws Exception {
        if(args.length != 2){
            System.exit(1);
        }
        //1.获取Job
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);

        String input = args[0];// "data\\ruozedata.txt";
        String output = args[1];//  "out\\wc";
        //删除目标目录
        FileUtils.delete(conf, output);

        //2.设置主类
        job.setJarByClass(CombinerDriver.class);

        //3.设置Mapper和Reducer
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        //设置Combiner
        job.setCombinerClass(MyReducer.class);

        //4.设置Mapper阶段的输出的Key和Value类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        //5.设置Reducer阶段的输出的Key和Value类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        //6.设置输入输出路径
        FileInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        //7.提交Job
        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);

    }

    public static class MyMapper  extends Mapper<LongWritable, Text, Text, IntWritable> {
        IntWritable one = new IntWritable(1);

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] splits = value.toString().split(",");
            for (String word : splits) {
                context.write(new Text(word), one);
            }
        }
    }

    public static class MyReducer extends Reducer<Text, IntWritable, Text, IntWritable> {

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int count = 0;
            for (IntWritable value : values) {
                count += value.get();
            }
            context.write(key, new IntWritable(count));
        }
    }
}
