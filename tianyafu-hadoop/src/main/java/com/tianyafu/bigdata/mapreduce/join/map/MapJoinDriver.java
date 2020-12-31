package com.tianyafu.bigdata.mapreduce.join.map;

import com.tianyafu.bigdata.mapreduce.join.reduce.Info;
import com.tianyafu.utils.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author:tianyafu
 * @Date:2020/12/30
 * @Description:
 */
public class MapJoinDriver {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.exit(1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);

        String input = args[0]; //"tianyafu-hadoop/src/main/resources/data/emp_and_dept/emp.txt";
        String out = args[1]; //"tianyafu-hadoop/src/main/resources/out/emp_and_dept_map_join";

        FileUtils.delete(conf, out);

        job.setJarByClass(MapJoinDriver.class);

        job.setMapperClass(MyMapper.class);
        job.setNumReduceTasks(0);

        job.addCacheFile(new URI("tianyafu-hadoop/src/main/resources/data/emp_and_dept/dept.txt"));

        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Info.class);
        job.setOutputKeyClass(Info.class);
        job.setOutputValueClass(NullWritable.class);

        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(out));

        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);


    }

    public static class MyMapper extends Mapper<LongWritable, Text, Info, NullWritable> {
        String name ;

        Map<String ,String> cachedMap = new HashMap<>();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            URI[] uris = context.getCacheFiles();
            String path = uris[0].getPath();
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String line = "";
            while (null != (line = reader.readLine())) {
                String[] splits = line.split("\t");
                cachedMap.put(splits[0].trim(),splits[1].trim());
            }


            IOUtils.closeStream(reader);
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] splits = value.toString().split("\t");
            Info info = new Info();
            info.setEmpno(Integer.parseInt(splits[0].trim()));
            info.setEname(splits[1].trim());
            int deptno = Integer.parseInt(splits[7].trim());
            info.setDeptno(deptno);
            String dname = cachedMap.getOrDefault(deptno + "", "");
            info.setDname(dname);
            context.write(info,NullWritable.get());
        }
    }

}
