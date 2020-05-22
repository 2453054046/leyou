package com.leyou.item.service;


import com.leyou.common.utlis.enums.ExceptionEnum;
import com.leyou.common.utlis.exception.LyException;
import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 商品分类
 */
@Service
public class CategoryService {

    @Autowired
    CategoryMapper categoryMapper;

    /**
     * 树形插件，查询上一级下的子级
     * @param pid
     * @return
     */
    public List<Category> queryListByParent(Long pid){
        Category category = new Category();
        category.setParentId(pid);
        List<Category> select = categoryMapper.select(category);
        //CollectionUtils.isEmpty():判断集合是否为空
        if(CollectionUtils.isEmpty(select)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return select;
    }

    /**
     *  根据多个id查询
     * @return
     */
    public List<Category> queryByids(List<Long> ids){
        List<Category> categories = categoryMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(categories)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOND);
        }
        return categories;
    }
}
