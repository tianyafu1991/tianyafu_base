package com.tianyafu.bigdata.mock;

import com.tianyafu.bigdata.domain.Access;
import com.tianyafu.bigdata.utils.UploadUtils;
import org.junit.Test;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Author:tianyafu
 * @Date:2021/3/9
 * @Description:
 */
public class MockData {

    public static final String URL = "http://localhost:9527/log/upload";

    @Test
    public void testUploadData() throws Exception{
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

        for (int i = 1; i <= 10000; i++) {
            Thread.sleep(1000);
            Access access = new Access();
            access.setId(i);
            access.setName("tianyafu"+i);
            access.setTime(format.format(new Date()));
            UploadUtils.upload(URL,access.toString());
        }


    }
}
