package com.tianyafu.bigdata.hadoop;

/**
 * @Author:tianyafu
 * @Date:2020/12/29
 * @Description:
 */
public class WordCountMapper implements TianyaMapper {


    @Override
    public void map(String line, TianyaContext context) {
        String[] splits = line.toLowerCase().split(",");
        for (String word : splits) {
            Object value = context.get(word);
            if (value == null) {
                context.write(word, 1);
            } else {
                context.write(word, Integer.valueOf(value.toString()) + 1);
            }
        }
    }
}
