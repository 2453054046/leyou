package com.leyou.search.repository;

import com.leyou.common.utlis.vo.PageResult;
import com.leyou.item.pojo.Spu;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.service.SearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
@RunWith(SpringRunner.class)
@SpringBootTest
public class GoodsRepositoryTest {

    @Autowired
    GoodsRepository goodsRepository;

    @Autowired
    ElasticsearchTemplate template;

    @Autowired
    GoodsClient goodsClient;

    @Autowired
    SearchService searchService;


    /**
     * 创建goods索引库
     */
    @Test
    public void createGoods(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    /**
     * 导入数据库全部数据
     */
    @Test
    public void loadData(){
        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            //查询spu信息
            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows, true, null);
            //获得信息
            List<Spu> items = result.getItems();
            //判断是否存在信息
            if(CollectionUtils.isEmpty(items)){
                break;
            }
            //调用searchService的查询spu所有信息方法转换成Goods类
            List<Goods> collect = items.stream().map(searchService::buildGoods).collect(Collectors.toList());
            //存入索引库
            goodsRepository.saveAll(collect);
            //翻页
            page++;
            size = items.size();

        }while (size == 100);
    }


}