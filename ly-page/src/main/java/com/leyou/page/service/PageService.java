package com.leyou.page.service;


import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PageService {
    //商品
    @Autowired
    GoodsClient goodsClient;
    //品牌
    @Autowired
    BrandClient brandClient;
    //分类
    @Autowired
    CategoryClient categoryClient;
    //规格参数
    @Autowired
    SpecificationClient specClient;

    @Autowired
    TemplateEngine templateEngine;

    /**
     * 根据商品id查询商品所有详情
     * @param spuid
     * @return
     */
    public Map<String, Object> loadModel(Long spuid) {
        //查询spu(包括sku，detail)
        Spu spu = goodsClient.querySpuById(spuid);
        //获得skus
        List<Sku> skus = spu.getSkus();
        //获得detail
        SpuDetail detail = spu.getSpuDetail();
        //查询品牌
        Brand brand = brandClient.queryById(spu.getBrandId());
        //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //查询规格参数
        List<SpecGroup> specs = specClient.queryGroupByCid(spu.getCid3());
        Map<String,Object> map = new HashMap<>();
        map.put("title",spu.getTitle());
        map.put("subTitle",spu.getSubTitle());
        map.put("skus",skus);
        map.put("detail",detail);
        map.put("brand",brand);
        map.put("categories",categories);
        map.put("specs",specs);
        return map;
    }

    /**
     * 根据spu的ID生成静态页面
     * @param spuId
     */
    public void createHtml(Long spuId){
        //上下文
        Context context = new Context();
        //查询spu的所有详情作为静态页面的动态数据
        context.setVariables(loadModel(spuId));
        //输出流
        File file = new File("F:\\a.code\\ideaHome03\\SpringBoot_Shopping\\loadStaticHtml", spuId + ".html");

        //判断当前历经是否存在该页面
        if(file.exists()){
            file.delete();
        }

        try (PrintWriter writer = new PrintWriter(file, "UTF-8")) {
            templateEngine.process("item", context, writer);
        }catch (Exception e){
            log.error("[生成静态页面错误：]"+e);
        }
    }

    /**
     * 删除页面
     * @param spuId
     */
    public void deleteHeml(Long spuId) {
        //输出流
        File file = new File("F:\\a.code\\ideaHome03\\SpringBoot_Shopping\\loadStaticHtml", spuId + ".html");
        file.delete();
    }
}
