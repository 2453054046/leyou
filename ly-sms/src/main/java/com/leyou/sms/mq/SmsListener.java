package com.leyou.sms.mq;

import com.leyou.common.utlis.utils.JsonUtils;
import com.leyou.sms.config.SmsProperties;
import com.leyou.sms.util.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


import java.util.Map;

/**
 * 消息监听，监听短信发送
 */

@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsListener {
    @Autowired
    SmsUtils smsUtils;
    @Autowired
    SmsProperties smsProperties;

    /**
     * 监听短信验证码
     * @param msg      手机号，发送到的验证码
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "sms.verify.code.queue",durable = "true"),
            exchange = @Exchange(name = "ly.sms.exchange",type = ExchangeTypes.TOPIC),
            key = "sms.verify.code"
    ))
    public void  listenSmsCode(Map<String,String> msg){
        if(CollectionUtils.isEmpty(msg)){
            return;
        }
        String phone = msg.remove("phone");
        if(StringUtils.isBlank(phone)){
            return;
        }
        //处理消息
        smsUtils.sendSms(phone,smsProperties.getVerifyCodeTemplate(),smsProperties.getSignName(), JsonUtils.toString(msg));
    }
}
