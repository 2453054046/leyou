package com.leyou.search.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utlis.utils.JsonUtils;
import com.leyou.common.utlis.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SearchService {

    @Autowired
    BrandClient brandClient;

    @Autowired
    CategoryClient categoryClient;

    @Autowired
    GoodsClient goodsClient;

    @Autowired
    SpecificationClient specificationClient;

    @Autowired
    GoodsRepository goodsRepository;
    //索引库原生类
    @Autowired
    ElasticsearchTemplate template;

    /**
     * 构建goods
     * 根据spu查询sku，商品规格，商品品牌转化为goods类
     * @param spu
     * @return
     */
    public Goods buildGoods(Spu spu){
        Goods goods = new Goods();

        Long spuId = spu.getId();
        //查询分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        //将商品分类的name存放
        List<String> names = categories.stream().map(Category::getName).collect(Collectors.toList());

        //查询品牌
        Brand brand = brandClient.queryById(spu.getBrandId());

        //搜索字段
        String all = spu.getTitle() + StringUtils.join(names," ") + brand.getName();

        //查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spuId);
        //对sku进行处理
        List<Map<String,Object>> skus = new ArrayList<>();
        //价格集合
        List<Long> priceList = new ArrayList<>();
        //将查询到的sku转换Map集合存放到sku的List集合
        for (Sku sku : skuList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("title",sku.getTitle());
            map.put("price",sku.getPrice());
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));  //将字符串用 , 号隔开  获得第一个符号前的字符串
            skus.add(map);
            //处理价格
            priceList.add(sku.getPrice());
        }

        //查询规格参数
        List<SpecParam> params = specificationClient.queryParamById(null, spu.getCid3(), true);

        //查询商品详情
        SpuDetail spuDetail = goodsClient.queryDetailById(spuId);
        //获得统用规格参数
        Map<Long, String>  genericSpec= JsonUtils.toMap(spuDetail.getGenericSpec(), Long.class, String.class);
        //获得特有规格参数 key:Long value:list
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });

        //规格参数，key是规格参数的名字，值是规格参数的值
        Map<String,Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            //规格名称
            String key = param.getName();
            Object value = "";
            //判断是否是统用规格
            if(param.getGeneric()){
                value = genericSpec.get(param.getId());
                //判断该规格是否是数值类型
                if(param.getNumeric()){
                    value = chooseSegment(value.toString(),param);
                }
            }else {
                value = specialSpec.get(param.getId());
            }
            //存入map
            specs.put(key,value);
        }

        goods.setId(spu.getId());
        goods.setSubTitle(spu.getSubTitle());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setAll(all);
        goods.setPrice(priceList);
        goods.setSkus(JsonUtils.toString(skus));
        goods.setSpecs(specs);

        return goods;
    }

    /**
     * 将数值类型转换为数值区间：  1000-5000 计算 1000以上5000以下  或 1000以上
     * @param value     要转换的数值类型
     * @param p         SpecParam实体类
     * @return
     */
    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    /**
     * 搜索
     * @param request
     * @return
     */
    public PageResult<Goods> search(SearchRequest request) {
        int page = request.getPage()-1;
        int size = request.getSize();
        //创建搜索构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //结果过滤
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "subTitle", "skus"}, null));
        //分页
        queryBuilder.withPageable(PageRequest.of(page,size));
        //查询字段
        QueryBuilder matchQuery = buildBasicQuery(request);
        queryBuilder.withQuery(matchQuery);
        //聚合分类和品牌
        //分类聚合
        String categoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        //品牌聚合
        String branAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(branAggName).field("brandId"));
        //查询
        //Page<Goods> search = goodsRepository.search(queryBuilder.build());
        AggregatedPage<Goods> search = template.queryForPage(queryBuilder.build(), Goods.class);

        //解析结果
        long total = search.getTotalElements();
        double v = new Long(total).doubleValue();
        double v1 = new Integer(size).doubleValue();
        double v2 = Math.ceil(v/v1);
        int totalPages = new Double(v2).intValue();
        //解析聚合结果
        Aggregations aggs = search.getAggregations();
        //解析分类聚合结果
        List<Category> categories = parserCategoryAgg(aggs.get(categoryAggName));
        //解析品牌聚合结果
        List<Brand> brands = parserBrandAgg(aggs.get(branAggName));
        //搜索结果
        List<Goods> goodsList = search.getContent();

        //规格搜索
        List<Map<String,Object>> specs = null;
        //判断当前搜索是否有第三级分类
        if(categories.size() == 1 && categories!=null){
            //商品规格查询
            specs = buildSpecficationAgg(categories.get(0).getId(),matchQuery);
        }
        return new SearchResult(total,totalPages,goodsList,categories,brands,specs);

    }

    /**
     * 规格过滤
     * @param request
     * @return
     */
    private QueryBuilder buildBasicQuery(SearchRequest request) {
        //创建布尔查询
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()));
        //过滤条件
        Map<String,String> map = request.getFilter();
        /**
         * map.entrySet():迭代器获得map中的键值对
         */
        for(Map.Entry<String,String> entry:map.entrySet()){
            String key = entry.getKey();
            //如果key不是分类和品牌（表示key如果是规格参数的键值）
            if(!"cid3".equals(key) && !"brandId".equals(key)){
                key = "specs." + key + ".keyword";
            }
            boolQueryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return boolQueryBuilder;

    }

    /**
     * 查询商品规格
     * @param id            商品三级分类id
     * @param matchQuery    查询的字段和查询的搜索key关键字
     * @return
     */
    private List<Map<String, Object>> buildSpecficationAgg(Long id, QueryBuilder matchQuery) {

        //查询该商品的多个规格
        List<SpecParam> specParams = specificationClient.queryParamById(null, id, true);

        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //查询字段
        queryBuilder.withQuery(matchQuery);
        //设置多个规格名字 聚合查询条件
        for (SpecParam param : specParams) {
            //规格名称
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
        }
        //查询
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //解析结果
        Aggregations aggs = result.getAggregations();
        //存放规格聚合结果数据
        List<Map<String,Object>> specs = new ArrayList<>();
        //获得聚合的多个桶
        for (SpecParam param : specParams) {
            String name = param.getName();
            StringTerms aggregation = aggs.get(name);

            //准备map  存放格式： 第一个k:规格名称  第二个k:规格的聚合数据数组  例如：  k:内存   k：[4G，5G]
            Map<String,Object> map = new HashMap<>();
            map.put("k",name);
            map.put("options",aggregation.getBuckets().stream().map(b -> b.getKeyAsString()).collect(Collectors.toList()));

            //将map封装返回
            specs.add(map);
        }
        return specs;
    }

    //解析品牌聚合结果
    private List<Brand> parserBrandAgg(LongTerms terms) {
        try {
            //获得聚合桶的多个key的值
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            //根据品牌的多个id查询品牌结果
            List<Brand> brands = brandClient.queryByIds(ids);
            return brands;
        }catch (Exception e){
            log.error("搜索服务品牌："+e);
            return null;
        }
    }

    //解析分类聚合结果
    private List<Category> parserCategoryAgg(LongTerms terms) {
        try{
            List<Long> ids = terms.getBuckets().stream().map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
            return categories;
        }catch (Exception e){
            log.error("搜索服务分类："+e);
            return null;
        }
    }

    /**
     * 对索引库增加或修改
     * 调用于监听器
     * @param spuId
     */
    public void createOrUpdateIndex(Long spuId) {
        //查询数据库
        Spu spu = goodsClient.querySpuById(spuId);
        //调用构建goods类方法
        Goods goods = buildGoods(spu);
        //存入索引库
        goodsRepository.save(goods);
    }

    /**
     * 根据spu的ID对索引库删除
     * @param spuId
     */
    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
