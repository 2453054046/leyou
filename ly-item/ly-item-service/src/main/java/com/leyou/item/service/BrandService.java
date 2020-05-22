package com.leyou.item.service;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.utlis.enums.ExceptionEnum;
import com.leyou.common.utlis.exception.LyException;
import com.leyou.common.utlis.vo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {

    @Autowired
    BrandMapper brandMapper;


    /**
     * 分页查询商品品牌
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @return
     */
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //开始分页
        PageHelper.startPage(page,rows);
        //查询条件
        Example example = new Example(Brand.class);
        //过滤搜索
        if(StringUtils.isNotBlank(key)){
            //andLike:模糊查询   orEqualTo：对应 or 关键字
            example.createCriteria().orLike("name","%"+key+"%").orEqualTo("letter",key.toUpperCase());
        }
        //排序
        if(StringUtils.isNotBlank(sortBy)){
            String s = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(s);
        }
        //查询
        List<Brand> list =  brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.PRICE_CANNOT_BE_NULL);
        }
        PageInfo<Brand> pageInfo = new PageInfo<>(list);
        return new PageResult(pageInfo.getTotal(),list);
    }


    /**
     * 商品品牌增加
     * @param brand         品牌参数
     * @param categories    品牌对应的商品类型id
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> categories) {
        int insert = brandMapper.insert(brand);
        if(insert!=1){
            throw  new LyException(ExceptionEnum.CATEGORY_NOT_FUND);
        }
        for(Long cid:categories){
            int i = brandMapper.insertCategoryBrand(cid, brand.getId());
            if(i!=1){
                throw  new LyException(ExceptionEnum.CATEGORY_NOT_FUND);
            }
        }
    }

    public Brand queryById(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand==null){
            throw  new LyException(ExceptionEnum.CATEGORY_NOT_FUND);
        }
        return brand;
    }

    public List<Brand> queryBrandByCid(Long cid){
        List<Brand> list = brandMapper.queryByCategory(cid);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.PRICE_CANNOT_BE_NULL);
        }
        return list;
    }


    public List<Brand> queryByIds(List<Long> ids) {
        return brandMapper.selectByIdList(ids);
    }
}
