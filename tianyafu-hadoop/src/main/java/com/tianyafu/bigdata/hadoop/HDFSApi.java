package com.tianyafu.bigdata.hadoop;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class HDFSApi {

    public static void mkdir(FileSystem fileSystem, String hdfsPath) throws Exception {
        Path path = new Path(hdfsPath);
        fileSystem.mkdirs(path);
    }

    public static void copyFromLocalFile(FileSystem fileSystem, String src, String dst) throws Exception {
        fileSystem.copyFromLocalFile(false, true, new Path(src), new Path(dst));
    }

    public static void copyToLocalFile(FileSystem fileSystem, String src, String dst) throws Exception {
        fileSystem.copyToLocalFile(false, new Path(src), new Path(dst), false);
    }

    public static void rename(FileSystem fileSystem, String src, String dst) throws Exception {
        fileSystem.rename(new Path(src), new Path(dst));
    }

    public static void listFiles(FileSystem fileSystem, String src, boolean recursive) throws Exception {
        RemoteIterator<LocatedFileStatus> statusRemoteIterator = fileSystem.listFiles(new Path(src), recursive);
        while (statusRemoteIterator.hasNext()) {
            LocatedFileStatus fileStatus = statusRemoteIterator.next();
            String isDir = fileStatus.isDirectory() ? "文件夹" : "文件";
            String permission = fileStatus.getPermission().toString();
            short replication = fileStatus.getReplication();
            long len = fileStatus.getLen();
            String path = fileStatus.getPath().toString();
            System.out.println(isDir + "\t" + permission + "\t" + replication + "\t" + len + "\t" + path);
            BlockLocation[] blockLocations = fileStatus.getBlockLocations();
            for (BlockLocation blockLocation : blockLocations) {
                String[] hosts = blockLocation.getHosts();
                for (String host : hosts) {
                    System.out.println(host);
                }
            }
        }
    }

    public static void delete(FileSystem fileSystem, String src, boolean recursive) throws Exception {
        fileSystem.delete(new Path(src), recursive);
    }

    public static void copyFromLocalFile(FileSystem fileSystem, String src, String dst,boolean overwrite) throws Exception{
        BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(src));
        FSDataOutputStream outputStream = fileSystem.create(new Path(dst), overwrite);
        IOUtils.copyBytes(inputStream,outputStream,1024,true);
    }

    public static void copyToLocalFile(FileSystem fileSystem, String src, String dst,int bufferSize,boolean close) throws Exception{
        FSDataInputStream inputStream = fileSystem.open(new Path(src));
        FileOutputStream outputStream = new FileOutputStream(dst);
        IOUtils.copyBytes(inputStream,outputStream,bufferSize,close);

    }



}
