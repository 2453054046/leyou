package com.leyou;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class test {

    @Autowired
    AmqpTemplate amqpTemplate;

    @Test
    public void testSend() throws InterruptedException {
        Map<String,String> map = new HashMap<>();
        map.put("phone","18339646181");
        map.put("name","54321");
        //发送消息
        amqpTemplate.convertAndSend("ly.sms.exchange","sms.verify.code",map);

        Thread.sleep(10000);
    }
}
