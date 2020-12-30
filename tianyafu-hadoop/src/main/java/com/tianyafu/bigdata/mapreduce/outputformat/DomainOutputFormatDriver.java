package com.tianyafu.bigdata.mapreduce.outputformat;

import com.tianyafu.utils.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

/**
 * @Author:tianyafu
 * @Date:2020/12/30
 * @Description:
 */
public class DomainOutputFormatDriver {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
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
        job.setJarByClass(DomainOutputFormatDriver.class);

        //3.设置Mapper和Reducer
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        job.setOutputFormatClass(MyOutputFormat.class);

        //4.设置Mapper阶段的输出的Key和Value类型
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(NullWritable.class);
        //5.设置Reducer阶段的输出的Key和Value类型
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(NullWritable.class);



        //6.设置输入输出路径
        FileInputFormat.setInputPaths(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(output));

        //7.提交Job
        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);

    }

    public static class MyMapper extends Mapper<LongWritable, Text, Text, NullWritable> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            context.write(value, NullWritable.get());
        }
    }

    public static class MyReducer extends Reducer<Text, NullWritable, Text, NullWritable> {
        @Override
        protected void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            for (NullWritable value : values) {
                context.write(key,value);
            }
        }
    }

    public static class MyOutputFormat extends FileOutputFormat<Text, NullWritable> {

        @Override
        public RecordWriter<Text, NullWritable> getRecordWriter(TaskAttemptContext job) throws IOException, InterruptedException {
            return new MyRecordWriter(job);
        }
    }

    public static class MyRecordWriter extends RecordWriter<Text, NullWritable> {

        FileSystem fileSystem = null;
        FSDataOutputStream ruozedataOut = null;
        FSDataOutputStream otherOut = null;

        public MyRecordWriter() {
        }

        public MyRecordWriter(TaskAttemptContext job) {
            try {
                fileSystem = FileSystem.get(job.getConfiguration());
                ruozedataOut = fileSystem.create(new Path("tianyafu-hadoop/src/main/resources/out/outputformat/ruozedata"));
                otherOut = fileSystem.create(new Path("tianyafu-hadoop/src/main/resources/out/outputformat/other"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void write(Text key, NullWritable value) throws IOException, InterruptedException {
            if (key.toString().contains("ruozedata.com")) {
                ruozedataOut.write((key.toString() + "\n").getBytes());
            } else {
                otherOut.write((key.toString() + "\n").getBytes());
            }
        }

        @Override
        public void close(TaskAttemptContext context) throws IOException, InterruptedException {
            IOUtils.closeStream(otherOut);
            IOUtils.closeStream(ruozedataOut);
            if (null != fileSystem) {
                fileSystem.close();
            }

        }
    }
}
