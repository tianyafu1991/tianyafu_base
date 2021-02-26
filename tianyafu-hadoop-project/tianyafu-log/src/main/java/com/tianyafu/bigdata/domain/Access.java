package com.tianyafu.bigdata.domain;

/**
 * @Author:tianyafu
 * @Date:2021/2/4
 * @Description:
 */
public class Access {

    private Integer id;

    private String name;

    private String time;


    public Access() {
    }

    public Access(Integer id, String name, String time) {
        this.id = id;
        this.name = name;
        this.time = time;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public String toString() {
        return id + "\t" + name + "\t" + time;
    }
}
