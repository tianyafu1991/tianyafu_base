package com.tianyafu.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;

import java.io.IOException;
import java.util.ArrayList;

public class HBaseCRUD {

    public static Configuration conf;
    public static Connection connection;

    static {
        conf = HBaseConfiguration.create();
        conf.set(HConstants.ZOOKEEPER_QUORUM, "mdw,sdw1,sdw2");
        conf.set(HConstants.ZOOKEEPER_CLIENT_PORT, "2181");

        try {
            connection = ConnectionFactory.createConnection(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void createTable(String tableName, String[] columnFamily) {
        try {
            Admin admin = connection.getAdmin();
            if (admin.tableExists(TableName.valueOf(tableName))) {
                System.out.println("table is exists");
            } else {
                HTableDescriptor hTableDescriptor = new HTableDescriptor(TableName.valueOf(tableName));
                for (int i = 0; i < columnFamily.length; i++) {
                    hTableDescriptor.addFamily(new HColumnDescriptor(columnFamily[i]));
                }
                admin.createTable(hTableDescriptor);
                System.out.println("table created");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void putRecord(String tableName, String columnFamily, String rowKey, String qualifier, String value) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Put put = new Put(Bytes.toBytes(rowKey));
            put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(qualifier), Bytes.toBytes(value));
            table.put(put);
            System.out.println("put data into " + tableName + " completed!!!!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void getOneRecord(String tableName, String rowKey) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Get get = new Get(Bytes.toBytes(rowKey));
            Result result = table.get(get);

            for (Cell cell : result.rawCells()) {
                String row = Bytes.toString(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                String family = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                String qualifier = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                System.out.println(row + ":" + family + ":" + qualifier + ":" + value);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void getAllRecord(String tableName) {
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Scan scan = new Scan();

            // 获取表数据的版本数 如果表设置了多版本 使用 scan.setMaxVersions(int maxVersions)方法
            System.out.println(scan.getMaxVersions());

            ResultScanner resultScanner = table.getScanner(scan);

            for (Result result : resultScanner) {
                for (Cell cell : result.rawCells()) {
                    String row = Bytes.toString(cell.getRowArray(), cell.getRowOffset(), cell.getRowLength());
                    String family = Bytes.toString(cell.getFamilyArray(), cell.getFamilyOffset(), cell.getFamilyLength());
                    String qualifier = Bytes.toString(cell.getQualifierArray(), cell.getQualifierOffset(), cell.getQualifierLength());
                    String value = Bytes.toString(cell.getValueArray(), cell.getValueOffset(), cell.getValueLength());
                    System.out.println(row + ":" + family + ":" + qualifier + ":" + value);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void deleteRecord(String tableName,String rowKey){
        try {
            Table table = connection.getTable(TableName.valueOf(tableName));
            Delete delete = new Delete(Bytes.toBytes(rowKey));
            ArrayList<Delete> list = new ArrayList<>();
            list.add(delete);
            table.delete(list);
            System.out.println("delete record completed!!!!!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String tableName = "tyf:java_create_table_test";
        String columnFamily = "o";
        String rowKey1 = "row1";
        String rowKey2 = "row2";
//        createTable(tableName,new String[]{columnFamily});

//        putRecord(tableName,columnFamily,rowKey1,"id","1");
//        putRecord(tableName,columnFamily,rowKey1,"name","tianyafu");

//        putRecord(tableName, columnFamily, rowKey2, "id", "2");
//        putRecord(tableName, columnFamily, rowKey2, "name", "tianyafu2");

//        getOneRecord(tableName,rowKey);


//        getAllRecord(tableName);

        deleteRecord(tableName,rowKey2);
        try {
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
