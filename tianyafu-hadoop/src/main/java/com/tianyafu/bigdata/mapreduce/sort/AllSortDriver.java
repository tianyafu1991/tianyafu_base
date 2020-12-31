package com.tianyafu.bigdata.mapreduce.sort;

import com.tianyafu.utils.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
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
 * @Date:2020/12/30
 * @Description:
 */
public class AllSortDriver {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.exit(1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);

        String input = args[0]; //"tianyafu-hadoop/src/main/resources/data/access.log";
        String out = args[1]; //"tianyafu-hadoop/src/main/resources/out/access";

        FileUtils.delete(conf, out);

        job.setJarByClass(AllSortDriver.class);

        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        job.setMapOutputKeyClass(Traffic.class);
        job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Traffic.class);

        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(out));

        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);


    }

    public static class MyMapper extends Mapper<LongWritable, Text, Traffic, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] splits = value.toString().split("\t");
            String phone = splits[1];
            long up = Long.parseLong(splits[splits.length - 3]);
            long down = Long.parseLong(splits[splits.length - 2]);
            context.write( new Traffic(up, down),new Text(phone));
        }
    }

    public static class MyReducer extends Reducer<Traffic, Text, Text, Traffic> {
       /* @Override
        protected void reduce(Text key, Iterable<Traffic> values, Context context) throws IOException, InterruptedException {
            Long allUp = 0L;
            Long allDown = 0L;
            for (Traffic traffic : values) {
                allUp += traffic.getUp();
                allDown += traffic.getDown();
            }
            context.write(NullWritable.get(), new Traffic(allUp, allDown));
        }*/

        @Override
        protected void reduce(Traffic key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            /*Long allUp = 0L;
            Long allDown = 0L;*/
            for (Text phone : values) {
                context.write(phone,key);
            }
        }
    }
}
