package com.tianyafu.kafka.producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

public class TianyafuKafkaApp {

    KafkaProducer<String,String> kafkaProducer;
    public static final String TOPIC = "tyf_kafka_1";


    @Before
    public void setUp(){
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "mdw:9092,sdw1:9092,sdw2:9092");
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "DemoProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        kafkaProducer = new KafkaProducer<>(props);
    }

    @Test
    public void test01(){
        System.out.println(kafkaProducer);
    }

    /**
     * 异步发送方式
     *
     * fire-and-forget 发后即忘 吞吐量最高  会丢数据
     */
    @Test
    public void test02(){
        for (int i = 0; i < 5; i++) {
            kafkaProducer.send(new ProducerRecord<>(TOPIC, "tyf" + i));
        }
    }

    /**
     * 带回调的异步发送方式
     * 主要是判断exception是否为null
     * @throws Exception
     */
    @Test
    public void test03() throws Exception{
        for (int i = 0; i < 5; i++) {
            RecordMetadata recordMetadata = kafkaProducer.send(new ProducerRecord<>(TOPIC, "tyf" + i), new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    if (null == exception) {
                        System.out.println("发送成功，topic:" + metadata.topic() + " ,分区是:" + metadata.partition() + " ,offset是:" + metadata.offset());
                    } else {
                        System.out.println("发送失败..." + exception.getMessage());
                    }
                }
            }).get();
        }
    }

    /**
     * 同步发送 吞吐肯定较异步的有下滑
     * 通过get()方法 阻塞住直到发送成功返回metadata
     * @throws Exception
     */
    @Test
    public void test04() throws Exception{
        for (int i = 0; i < 5; i++) {
            RecordMetadata recordMetadata = kafkaProducer.send(new ProducerRecord<>(TOPIC, "tyf" + i)).get();
        }
    }

    @After
    public void tearDown(){
        if(null != kafkaProducer){
            kafkaProducer.close();
        }
    }
}
