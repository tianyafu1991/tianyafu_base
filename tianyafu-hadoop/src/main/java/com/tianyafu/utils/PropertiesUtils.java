package com.tianyafu.utils;

import com.tianyafu.constant.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @Author:tianyafu
 * @Date:2020/12/29
 * @Description:
 */
public class PropertiesUtils {

    public static Properties prop = new Properties();
    public static String PROPERTIES_FILE_NAME = "wc.properties";

    static {
        try {
            InputStream in = PropertiesUtils.class.getClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME);
            prop.load(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getProperties(String key) {
        return prop.getProperty(key);
    }

}
