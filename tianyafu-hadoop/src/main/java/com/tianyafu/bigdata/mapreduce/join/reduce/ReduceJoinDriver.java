package com.tianyafu.bigdata.mapreduce.join.reduce;

import com.tianyafu.bigdata.mapreduce.sort.Traffic;
import com.tianyafu.utils.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author:tianyafu
 * @Date:2020/12/30
 * @Description:
 */
public class ReduceJoinDriver {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.exit(1);
        }

        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf);

        String input = args[0]; //"tianyafu-hadoop/src/main/resources/data/emp_and_dept";
        String out = args[1]; //"tianyafu-hadoop/src/main/resources/out/emp_and_dept";

        FileUtils.delete(conf, out);

        job.setJarByClass(ReduceJoinDriver.class);

        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);

        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(Info.class);
        job.setOutputKeyClass(Info.class);
        job.setOutputValueClass(NullWritable.class);

        FileInputFormat.addInputPath(job, new Path(input));
        FileOutputFormat.setOutputPath(job, new Path(out));

        boolean result = job.waitForCompletion(true);
        System.exit(result ? 0 : 1);


    }

    public static class MyMapper extends Mapper<LongWritable, Text, IntWritable, Info> {
        String name ;
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            FileSplit fileSplit = (FileSplit)context.getInputSplit();
            name = fileSplit.getPath().getName();
        }

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] splits = value.toString().split("\t");
            Info info = new Info();
            if(name.contains("emp")){
                Integer empno = Integer.parseInt(splits[0].trim());
                String ename = splits[1].trim();
                Integer deptno = Integer.parseInt(splits[7].trim());
                info.setEmpno(empno);
                info.setEname(ename);
                info.setDeptno(deptno);
                info.setDname("");
                info.setFlag(1);
                context.write(new IntWritable(deptno),info);
            }else {
                Integer deptno = Integer.parseInt(splits[0].trim());
                String dname = splits[1].trim();
                info.setEmpno(0);
                info.setEname("");
                info.setDeptno(deptno);
                info.setDname(dname);
                info.setFlag(2);
                context.write(new IntWritable(deptno),info);
            }
        }
    }

    public static class MyReducer extends Reducer<IntWritable, Info, Info, NullWritable> {
        @Override
        protected void reduce(IntWritable key, Iterable<Info> values, Context context) throws IOException, InterruptedException {
            List<Info> infos = new ArrayList<>();
            String dname = "";

            for (Info info : values) {
                if(info.getFlag() == 1){ //emp
                    Info tmp = new Info();
                    tmp.setEmpno(info.getEmpno());
                    tmp.setEname(info.getEname());
                    tmp.setDeptno(info.getDeptno());
                    infos.add(tmp);
                } else { //dept
                    dname = info.getDname();
                }
            }

            for (Info info : infos) {
                info.setDname(dname);
                context.write(info,NullWritable.get());
            }
        }
    }
}
