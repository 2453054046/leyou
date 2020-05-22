package com.leyou.common.utlis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 商品
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartDTO {
    private Long skuId;     //商品id
    private Integer num;       //数量

}
