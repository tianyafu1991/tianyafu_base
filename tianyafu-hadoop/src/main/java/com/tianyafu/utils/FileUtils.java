package com.tianyafu.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

/**
 * @Author:tianyafu
 * @Date:2020/12/29
 * @Description:
 */
public class FileUtils {

    public static void delete(Configuration conf, String output) throws Exception {
        FileSystem fileSystem = FileSystem.get(conf);
        Path out = new Path(output);
        if (fileSystem.exists(out)) {
            fileSystem.delete(out, true);
        }
    }

}
