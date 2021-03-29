package com.tianyafu.flink.basic;

import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.Collector;

public class BatchWCAppJava {

    public static void main(String[] args) throws Exception {
        ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        String path = "data/ruozedata.txt";

        env.readTextFile(path).flatMap(new FlatMapFunction<String, String>() {
            @Override
            public void flatMap(String s, Collector<String> collector) throws Exception {
                String[] words = s.split(",");
                for (String word : words) {
                    collector.collect(word);
                }
            }
        }).filter(new FilterFunction<String>() {
            @Override
            public boolean filter(String s) throws Exception {
                return null != s && s.length()> 0;
            }
        }).map(new MapFunction<String, Tuple2<String,Integer>>() {
            @Override
            public Tuple2<String, Integer> map(String s) throws Exception {
                return new Tuple2<>(s,1);
            }
        }).groupBy(0).sum(1).print();


    }
}
