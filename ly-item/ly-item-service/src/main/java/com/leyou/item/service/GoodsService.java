package com.leyou.item.service;


import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.utlis.dto.CartDTO;
import com.leyou.common.utlis.enums.ExceptionEnum;
import com.leyou.common.utlis.exception.LyException;
import com.leyou.common.utlis.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {

    //spu商品
    @Autowired
    SpuMapper spuMapper;

    //spu商品详情
    @Autowired
    SpuDetailMapper spuDetailMapper;

    //商品分类查询
    @Autowired
    CategoryService categoryService;

    //商品品牌
    @Autowired
    BrandService brandService;

    //sku
    @Autowired
    SkuMapper skuMapper;

    //库存
    @Autowired
    StockMapper stockMapper;

    //消息队列
    @Autowired
    AmqpTemplate amqpTemplate;



    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        //分页
        PageHelper.startPage(page,rows);
        //过滤：
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 是否存在搜索
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title","%"+key+"%");
        }
        //上架过滤
        if(saleable!=null){
            criteria.andEqualTo("saleable",saleable);
        }
        //默认排序
        example.setOrderByClause("last_update_time DESC");

        List<Spu> spus = spuMapper.selectByExample(example);
        //判断
        if(CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //解析分类和品牌的名称
        loadCategoryAndBrandName(spus);
        //解析分页结果
        PageInfo<Spu> spuPageInfo = new PageInfo<>(spus);

        return new PageResult(spuPageInfo.getTotal(),spus);
    }


    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            //处理分类名称
            //将查询的实体类，取出getName属性，存放在List数组内
            List<String> collect = categoryService.queryByids(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            //将数组内的数据以 / 连接起来
            spu.setCname(StringUtils.join(collect,"/"));

            //处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }


    /**
     * 商品的新增
     * @param spu
     */
    @Transactional
    public void saveGood(Spu spu) {
        //新增Spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);      //是否上架
        spu.setValid(false);        //商品是否有效
        int insert = spuMapper.insert(spu);
        if(insert!=1){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
        //新增detail
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        spuDetailMapper.insert(spuDetail);
        //定义库存集合
        List list = new ArrayList<>();
        //新增Sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            int insert1 = skuMapper.insert(sku);
            if(insert1!=1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

            //取出库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            list.add(stock);
        }
        //新增库存
        int i = stockMapper.insertList(list);
        if(i!=list.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }

        //发送消息到消息队列
        //amqpTemplate.convertAndSend(消息的标记,消息的内容);  消息的标记：topic方式接收
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }

    /**
     * 根据spu查询详情
     * @param spuid
     * @return
     */
    public SpuDetail queryDetailById(Long spuid) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuid);
        if(spuDetail==null){
            throw new LyException(ExceptionEnum.GOODS_DETAIL_NOT_FOND);
        }
        return spuDetail;
    }

    /**
     * 根据spu的id查询sku
     * @param spuid
     * @return
     */
    public List<Sku> querySkuBySpuId(Long spuid) {
        //查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuid);
        List<Sku> skuList = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FOUND);
        }

        //根据sku查询对应的库存
        List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(stocks)){
            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }
        //把查询结果变成对应stock的map  key是查询结果的sku的id，值是库存值
        Map<Long, Integer> collect = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skuList.forEach(s -> s.setStock(collect.get(s.getId())));
        return  skuList;
    }

    /**
     * 修改商品
     * @param spu
     */
    public void updateGood(Spu spu) {
        if(spu.getId() ==null){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        List<Sku> select = skuMapper.select(sku);
        if(CollectionUtils.isEmpty(select)){
            //删除sku
            skuMapper.delete(sku);
            //删除stock
            List<Long> collect = select.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(collect);
        }
        //修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        int i = spuMapper.updateByPrimaryKeySelective(spu);
        if(i!=1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //修改detail
        int i1 = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if(i1!=1){
            throw new LyException(ExceptionEnum.GOODS_UPDATE_ERROR);
        }
        //新增sku和stock
        saveGoodsAndStock(spu);

        //发送消息到消息队列
        //amqpTemplate.convertAndSend(消息的标记,消息的内容);  消息的标记：topic方式接收
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }
    private void saveGoodsAndStock(Spu spu){
        //定义库存集合
        List list = new ArrayList<>();
        //新增Sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            int insert1 = skuMapper.insert(sku);
            if(insert1!=1){
                throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
            }

            //取出库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            list.add(stock);
        }
        //新增库存
        int i = stockMapper.insertList(list);
        if(i!=list.size()){
            throw new LyException(ExceptionEnum.GOODS_SAVE_ERROR);
        }
    }

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    public Spu querySpuById(Long id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu == null){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询sku
        spu.setSkus(querySkuBySpuId(id));
        //查询detail
        spu.setSpuDetail(queryDetailById(id));
        return spu;
    }

    /**
     * 根据spu的id集合查询所有sku
     * @param ids
     * @return
     */
    public List<Sku> querySkuByids(List<Long> ids) {
        //查询所有sku
        List<Sku> skuList = skuMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(skuList)){
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //查询库存
        //根据sku查询对应的库存
        List<Stock> stocks = stockMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(stocks)){
            throw new LyException(ExceptionEnum.GOODS_STOCK_NOT_FOUND);
        }
        //把查询结果变成对应stock的map  key是查询结果的sku的id，值是库存值
        Map<Long, Integer> collect = stocks.stream().collect(Collectors.toMap(Stock::getSkuId, Stock::getStock));
        skuList.forEach(s -> s.setStock(collect.get(s.getId())));

        return skuList;
    }
    /**
     * 减库存
     * @param carts     商品的id和个数
     * @return
     */
    @Transactional
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (count != 1) {
                throw new LyException(ExceptionEnum.STOCK_NOT_ENOUGH);
            }
        }
    }
}
