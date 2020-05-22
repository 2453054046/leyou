package com.leyou.item.mapper;

import com.leyou.common.utlis.mapper.BaseMapper;
import com.leyou.item.pojo.Stock;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;


public interface StockMapper extends BaseMapper<Stock> {

    /**
     * 减库存
     * @param id
     * @param num
     * @return
     */
    @Update("UPDATE tb_stock SET stock = stock - #{num} WHERE sku_id = #{id} AND stock >= #{num}")
    int decreaseStock(@Param("id") Long id, @Param("num") Integer num);
}
