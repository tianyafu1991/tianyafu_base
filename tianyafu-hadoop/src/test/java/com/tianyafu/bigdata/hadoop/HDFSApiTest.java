package com.tianyafu.bigdata.hadoop;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import static org.junit.Assert.*;

public class HDFSApiTest {
    FileSystem fileSystem;

    @Before
    public void setUp() throws Exception {
        URI uri = new URI("hdfs://hadoop01:9000");
        Configuration conf = new Configuration();
//        conf.set("dfs.client.use.datanode.hostname","true");//云主机情况下需要用到这个参数，非云主机无需这个参数;
        conf.set("dfs.replication", "1");
        fileSystem = FileSystem.get(uri, conf, "hadoop");
        System.out.println("setUp。。。。。。。。");
    }


    @After
    public void tearDown() throws Exception {
        if (null != fileSystem) {
            fileSystem.close();
        }
        System.out.println("tearDown。。。。。。。。。");
    }

    @Test
    public void mkdir() throws Exception {
        String hdfsPath = "/hdfs_api";
        HDFSApi.mkdir(fileSystem, hdfsPath);
        System.out.println("mkdir。。。。。。。");
    }

    @Test
    public void copyFromLocalFile() throws Exception {
        String src = "src/main/resources/log4j.properties";
        String dst = "/hdfs_api";
        HDFSApi.copyFromLocalFile(fileSystem, src, dst);
    }

    @Test
    public void copyToLocalFile() throws Exception {
        String src = "/out/result/wc/part-r-00000";
        String dst = "src/main/resources/out";
        HDFSApi.copyToLocalFile(fileSystem, src, dst);
    }

    @Test
    public void rename() throws Exception {
        String src = "/hdfs_api/log4j.properties";
        String dst = "/hdfs_api/log4j2.properties";
        HDFSApi.rename(fileSystem, src, dst);

    }

    @Test
    public void listFiles() throws Exception{
        String src = "/out";
        HDFSApi.listFiles(fileSystem, src, true);
    }

    @Test
    public void delete() throws Exception{
        String src  = "/hdfs_api";
        HDFSApi.delete(fileSystem,src,true);
    }

    @Test
    public void copyFromLocalFile2() throws Exception {
        String src = "src/main/resources/log4j.properties";
        String dst = "/hdfs_api/log4j22.properties";
        HDFSApi.copyFromLocalFile(fileSystem,src,dst,true);
    }

    @Test
    public void copyToLocalFile2() throws Exception {
        String src = "/hdfs_api/log4j22.properties";
        String dst = "src/main/resources/out/log4j22.properties";
        HDFSApi.copyToLocalFile(fileSystem,src,dst,1024,true);
    }
}