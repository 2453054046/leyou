package com.leyou.search.mq;


import com.leyou.search.service.SearchService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 消息监听商品的新增和修改
 */
@Component
public class ItemListener {

    @Autowired
    SearchService searchService;
    /*@RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "创建的消息队列",durable = "是否持久化"),
            exchange = @Exchange(name = "交换机名称",type = 消息类型),
            key = {"消息标记","消息标记"}
    ))*/

    //监听增加和修改
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.insert.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.insert","item.update"}
    ))
    public void listenInsertOrUpdate(Long  spuId){
        System.out.println("搜索监听器开始处理--------------------------");
        if(spuId==null){
            return;
        }
        System.out.println("搜索监听器开始处理--------------------------");
        //处理消息,对索引库增加或修改
        searchService.createOrUpdateIndex(spuId);
    }

    //监听删除
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.delete.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))
    public void listenDelete(Long  spuId){
        if(spuId==null){
            return;
        }
        //处理消息,对索引库增加或修改
        searchService.deleteIndex(spuId);
    }
}
