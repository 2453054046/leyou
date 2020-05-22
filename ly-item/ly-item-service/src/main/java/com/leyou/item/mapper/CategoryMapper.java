package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * 查询商品分类
 * 继承统用mapper生成CRUD方法
 * IdListMapper生成对于主键id查询的方法
 */
public interface CategoryMapper extends Mapper<Category>, IdListMapper<Category,Long> {
}
