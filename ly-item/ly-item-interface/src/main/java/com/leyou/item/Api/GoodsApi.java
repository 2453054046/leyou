package com.leyou.item.Api;

import com.leyou.common.utlis.dto.CartDTO;
import com.leyou.common.utlis.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping
public interface GoodsApi {


    /**
     * 分页查询spu
     * @param page          第几页
     * @param rows          每页显示多少
     * @param saleable      是否查询上架
     * @param key           搜索的值
     * @return
     */
    @GetMapping("spu/page")
    PageResult<Spu> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "saleable",required = false) Boolean saleable,
            @RequestParam(value = "key", required = false) String key
    );

    /**
     * 商品添加
     * @param spu
     * @return
     */
    @PostMapping("goods")
    Void saveGoods(@RequestBody Spu spu);

    /**
     * 根据spu查询商品详情
     * @param spuid
     * @return
     */
    @GetMapping("/spu/detail/{id}")
    SpuDetail queryDetailById(@PathVariable("id") Long spuid);

    /**
     * 根据spu的id查询sku
     * @param spuId
     * @return
     */
    @GetMapping("sku/list")
    List<Sku> querySkuBySpuId(@RequestParam("spuId") Long spuId);

    @PutMapping("goods")
    Void updateGoods(@RequestBody Spu spu);

    /**
     * 根据spu的id查询spu
     * @param id
     * @return
     */
    @GetMapping("spu/{id}")
    Spu querySpuById(@PathVariable("id") Long id);

    /**
     * 根据spu的id集合查询所有sku
     * @param ids
     * @return
     */
    @GetMapping("sku/list/ids")
    List<Sku> querySkuByids(@RequestParam("ids") List<Long> ids);

    /**
     * 减库存
     * @param carts
     * @return
     */
    @PostMapping("stock/decrease")
    Void decreaseStock(@RequestBody List<CartDTO> carts);
}
