package com.tianyafu.hdfs.wc;

import com.tianyafu.bigdata.hadoop.TianyaContext;
import com.tianyafu.bigdata.hadoop.TianyaMapper;
import com.tianyafu.constant.Constants;
import com.tianyafu.utils.PropertiesUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Map;

/**
 * @Author:tianyafu
 * @Date:2020/12/29
 * @Description:
 */
public class HdfsWcApp {

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        URI uri = new URI(PropertiesUtils.getProperties(Constants.HDFS_URI));
        FileSystem fileSystem = FileSystem.get(uri, conf, "hadoop");
        String src = PropertiesUtils.getProperties(Constants.HDFS_INPUT);

        TianyaContext context = new TianyaContext();
        String mapperClass = PropertiesUtils.getProperties(Constants.MAPPER_CLASS);
        Class<?> clazz = Class.forName(mapperClass);
        TianyaMapper mapper = (TianyaMapper) clazz.newInstance();

        RemoteIterator<LocatedFileStatus> iterator = fileSystem.listFiles(new Path(src), false);
        while (iterator.hasNext()) {
            LocatedFileStatus fileStatus = iterator.next();
            FSDataInputStream in = fileSystem.open(fileStatus.getPath());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                mapper.map(line, context);
            }
            reader.close();
            in.close();
        }
        FSDataOutputStream outputStream = fileSystem.create(new Path(PropertiesUtils.getProperties(Constants.HDFS_OUTPUT)));
        for (Map.Entry<Object, Object> entry : context.getCacheMap().entrySet()) {
            Object key = entry.getKey();
            Object value = entry.getValue();
            System.out.println(String.format("key为%s,value为%s", key, value));
            outputStream.write(String.format("key为%s,value为%s\n", key, value).getBytes());
        }

        outputStream.close();


        fileSystem.close();
    }
}
